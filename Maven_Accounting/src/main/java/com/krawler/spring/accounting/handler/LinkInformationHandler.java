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
package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class LinkInformationHandler {

    private AccLinkDataDao accLinkDataDao;

    public void setAccLinkDataDao(AccLinkDataDao accLinkDataDao) {
        this.accLinkDataDao = accLinkDataDao;
    }

//    
    public String getPurchaseSideLinkingHTML(String companyid, HashMap<String, Object> params, JSONArray jSONArray) throws Exception {
        String tablehtml;
        tablehtml = generateHTMl(companyid, params,jSONArray);
        return tablehtml;
    }

    public String getSalesSideLinkingHTML(String companyid, HashMap<String, Object> params,JSONArray jSONArray) throws Exception {
        String tablehtml;
        tablehtml = generateSalesSideHTML(companyid, params,jSONArray);
        return tablehtml;
    }

    public String generateHTMl(String companyid, HashMap<String, Object> params,JSONArray jSONArray) {
        List<String> purchase_requisition = new ArrayList();
        List<String> sales_order = new ArrayList();
        List<Object[]> next_data_within_date_range = new ArrayList();
        String tableHTML = "", tr = "" ,mainTable = "";
        JSONObject jSONObject=new JSONObject();

        try {

            HashMap<String, Object> requestParams = new HashMap<>();
            HashMap<String, Object> dataParmas = new HashMap<>();
            List<Object[]> vendor_quotation = new ArrayList();
            List<Object[]> purchase_order = new ArrayList();
            List<Object[]> purchase_invoice = new ArrayList();
            List<Object[]> goods_receipt = new ArrayList();
            List<Object[]> empty_list = new ArrayList();
            List<String> empty_list_RFQ = new ArrayList();

            requestParams.put("companyid", companyid);
            requestParams.put("includeRFQlinkedPR", true);
            requestParams.put("includeVqlinkedPR", true);
            requestParams.put("includePolinkedPR", true);

            KwlReturnObject kwlq = accLinkDataDao.getPurchaseRequisition(requestParams, params);
            purchase_requisition = kwlq.getEntityList();

            dataParmas.put("companyid", companyid);
            dataParmas.put("isVqWithinRange", true);

            kwlq = accLinkDataDao.getVendorQuotation(dataParmas, params);
            vendor_quotation = kwlq.getEntityList();
            
            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("linkedflag", 0);
            
            kwlq = accLinkDataDao.getVendorQuotation(dataParmas, params);
            next_data_within_date_range = kwlq.getEntityList();
            
            vendor_quotation = checkDuplicates(vendor_quotation,next_data_within_date_range,0);
            
            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("isStartFromPO", true);
            dataParmas.put("isLinkedInSO", true);
            kwlq = accLinkDataDao.getPurchaseOrder(dataParmas, params);
            purchase_order = kwlq.getEntityList();
            
            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("isStartFromPO", true);
            dataParmas.put("linkedflag", 0);

            kwlq = accLinkDataDao.getPurchaseOrder(dataParmas, params);
            purchase_order = checkDuplicates(purchase_order,kwlq.getEntityList(),0);
            
            dataParmas.remove("linkedflag");
            dataParmas.put("POWithinDateRange", true);
            kwlq = accLinkDataDao.getPurchaseOrder(dataParmas, params);
            next_data_within_date_range= kwlq.getEntityList();

            purchase_order = checkDuplicates(purchase_order,next_data_within_date_range,0);
            
            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("isStartFromPI", true);
            dataParmas.put("excludeChecks", true);

            kwlq = accLinkDataDao.getPurchaseInvoice(dataParmas, params);
            purchase_invoice = kwlq.getEntityList();
            
            dataParmas.remove("excludeChecks");
            dataParmas.put("InvoiceWithinDateRange", true);
            kwlq = accLinkDataDao.getPurchaseInvoice(dataParmas, params);
            next_data_within_date_range = kwlq.getEntityList();
            
            purchase_invoice = checkDuplicates(purchase_invoice,next_data_within_date_range,0);

            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("isStartFromGR", true);
            dataParmas.put("POModuleId", Constants.Acc_Purchase_Order_ModuleId);
            dataParmas.put("invoiceModuleId", Constants.Acc_Vendor_Invoice_ModuleId);
            dataParmas.put("groModuleId", Constants.Acc_Goods_Receipt_ModuleId);
            dataParmas.put("POsourceFlag", Constants.LINK_SOURCE_FLAG_1);
            
            dataParmas.put("isConsignment", Constants.SQL_FALSE);
            dataParmas.put("isFixedassetGro", Constants.SQL_FALSE);
            dataParmas.put("excludePOLinkedGRO", true);
            dataParmas.put("approveStatusLevel", Constants.INVOICEAPPROVED);

            kwlq = accLinkDataDao.getGoodsReceipt(dataParmas, params);
            goods_receipt = kwlq.getEntityList();

            dataParmas.put("GRWithinDateRange", true);
            kwlq = accLinkDataDao.getGoodsReceipt(dataParmas, params);
            next_data_within_date_range = kwlq.getEntityList();
            
            goods_receipt = checkDuplicates(goods_receipt,next_data_within_date_range,0);
            
            dataParmas.clear();
            dataParmas.put("companyid", companyid);
            dataParmas.put("isStartFromGR", true);
            dataParmas.put("invoiceModuleId", Constants.Acc_Purchase_Return_ModuleId);
            
            dataParmas.put("isConsignment", Constants.SQL_FALSE);
            dataParmas.put("isFixedassetGro", Constants.SQL_FALSE);
            dataParmas.put("excludePOLinkedGRO", true);
            dataParmas.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            dataParmas.put("isAdvanceSearch", true);

            kwlq = accLinkDataDao.getGoodsReceipt(dataParmas, params);
            List gro_In_PR = kwlq.getEntityList();
            goods_receipt = checkDuplicates(goods_receipt,gro_In_PR,0);
            
            mainTable += "<div id=\"linkedpurchasetable\" style=\"width:100%;overflow: auto; height: 100%;position: absolute;\"><table border=0 style=\"width:100%; border-spacing: 0; border-collapse: collapse; overflow: scroll;\">";
            mainTable += "<tbody>";

            mainTable += "<tr>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "</tr>";

            for (String prq : purchase_requisition) {

                tr = "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + prq + "','" + Constants.Acc_Purchase_Requisition_ModuleId + "')\">" + prq + "</a></td>";

                List<String> RFQ = new ArrayList();
                 List<String> purchaseorder = new ArrayList();
                HashMap<String, Object> RFQrequestParams = new HashMap<String, Object>();

                RFQrequestParams.put("companyid", companyid);
                RFQrequestParams.put("number", prq);

                KwlReturnObject kwlrfq = accLinkDataDao.getRFQ(RFQrequestParams, params);
                RFQ = kwlrfq.getEntityList();
                
                /*--Fetching PO linked with PR--- */
                RFQrequestParams.put("isFromPR", true);
                RFQrequestParams.put("RequisitionNumber", prq);
                KwlReturnObject kwlvq = accLinkDataDao.getPurchaseOrder(RFQrequestParams, params);
                purchaseorder = kwlvq.getEntityList();
                
                if (RFQ.size() == 0) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"></td>";
                }
                if (purchaseorder.size() !=0) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"></td>";
                }
                tr += "</tr>";
                
                //Adding purchase requisition entry for export linking information 
                jSONObject=new JSONObject();
                jSONObject.put(Constants.Purchase_Requisition_Key, prq);
                
                JSONObject rfq_json = generate_request_for_quotation_HTML(RFQ, prq, companyid, params, jSONObject,jSONArray);
                if (rfq_json.has("html") && !StringUtil.isNullOrEmpty(rfq_json.getString("html"))) {
                    tr += rfq_json.getString("html");
                }
                if (rfq_json.has("soHtml") && !StringUtil.isNullOrEmpty(rfq_json.getString("soHtml"))) {
                    tr = rfq_json.getString("soHtml") + tr;
                }
                //tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tableHTML += tr;
                
                //Adding json content in json array
                if (jSONObject.length() >= 1) {
                   jSONArray.put(jSONObject);
            }
                jSONObject=new JSONObject();
            }
            if(vendor_quotation.size() > 0 ){
                JSONObject vq_json = generate_vendor_quotation_HTML(empty_list_RFQ, 0, vendor_quotation, companyid, true, params,jSONObject,jSONArray);
                if (vq_json.has("html") && !StringUtil.isNullOrEmpty(vq_json.getString("html"))) {
                    tableHTML += vq_json.getString("html");
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                }
                    jSONObject = new JSONObject();             
            }
            }
            if(purchase_order.size() > 0 ){
                JSONObject purchase_order_json = generate_purchase_order_HTML(empty_list_RFQ, 0, empty_list, 0, purchase_order, companyid, "", false, params, true,jSONObject,jSONArray);
                if (purchase_order_json.has("html") && !StringUtil.isNullOrEmpty(purchase_order_json.getString("html"))) {
                    tableHTML += purchase_order_json.getString("html");
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                }
                    jSONObject = new JSONObject();             
            }
            }
            if(goods_receipt.size() > 0 ){
                JSONObject goods_receipt_json = generate_goods_receipt_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, goods_receipt, companyid, "", "", false, false, params, false, true,jSONObject,jSONArray);
                if (goods_receipt_json.has("html") && !StringUtil.isNullOrEmpty(goods_receipt_json.getString("html"))) {
                    tableHTML += goods_receipt_json.getString("html");
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                }
                    jSONObject = new JSONObject(); 
            }
            }
            if(purchase_invoice.size() > 0 ){
                JSONObject invoice_json = generate_purchase_invoice_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, empty_list, 0, purchase_invoice, companyid, false, params, false, true, false,"",jSONObject,jSONArray);
                if (invoice_json.has("html") && !StringUtil.isNullOrEmpty(invoice_json.getString("html"))) {
                    tableHTML += invoice_json.getString("html");
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    if (jSONObject.length() >= 1) {
                       jSONArray.put(jSONObject);
                }
                    jSONObject = new JSONObject();
            }
            }
            
            requestParams.put("companyid", companyid);
            requestParams.put("iscreditnote", true);
            requestParams.put("excludePaymentsUsedInInvoice", true);
            
            KwlReturnObject kwl = accLinkDataDao.getPaymentInformation(requestParams, params);
            List<String> payment = kwl.getEntityList();
            
            JSONObject paymentjson = generate_payment_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, empty_list, 0, empty_list, 0, payment, companyid, empty_list, false, params, false, false, false,false,0,0,true,jSONObject,jSONArray);
            if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                tableHTML += paymentjson.getString("html");
                if (jSONObject.length() >= 1) {
                   jSONArray.put(jSONObject);
            }
                jSONObject = new JSONObject();
            }
            
            requestParams.clear();
            requestParams.put("isCallFromPO", true);
            requestParams.put("soWithinDateRange", true);
            requestParams.put("companyid", companyid);

            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseSO", 0);
            requestParams.put("isReplacementSo", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

            kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
            sales_order = kwlq.getEntityList();
            
            if( sales_order.size() > 0 ){
               for (String so : sales_order) {
                    jSONObject.put("so",so);
                    tableHTML += "<tr>";
                    tableHTML += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + so + "','" + Constants.Acc_Sales_Order_ModuleId + "')\">" + so + "</a></td>";
                    tableHTML += "</tr>";
                    if (jSONObject.length() >= 1){
                    jSONArray.put(jSONObject);
                }
                    jSONObject=new JSONObject();
                }
                tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
            }
            
            mainTable += tableHTML;
            mainTable += "</tbody>";
            mainTable += "</table></div>";

        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }

        return mainTable;

    }

    public JSONObject generate_request_for_quotation_HTML(List<String> RFQ, String prq, String companyid, HashMap<String, Object> params, JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        JSONObject vendor_quotation_json = new JSONObject();
        List<Object[]> vendor_quotation = new ArrayList();
        List<Object[]> purchase_order = new ArrayList();
        JSONObject purchase_order_json = new JSONObject();
        HashMap<String, Object> VQrequestParams = new HashMap<String, Object>();
        int rfqCnt = 0;
        boolean isPRLinkedToVq=false;

        try {
            if (RFQ.size() > 0) {
                for (String rfq : RFQ) {
                    tr += "<tr>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + rfq + "','" + Constants.Acc_RFQ_ModuleId + "')\">" + rfq + "</a></td>";
                    tr += "</tr>";
                    if(jSONObject.has(Constants.Request_For_Quotation_Key)){
                       JSONObject copy=new JSONObject(jSONObject.toString());
                       jSONArray.put(copy);
                       jSONObject= removeDuplicateEntries(jSONObject);
                    }
                    jSONObject.put(Constants.Request_For_Quotation_Key, rfq);
                    
                    
                    if (rfqCnt== RFQ.size() - 1) {
                    if (jSONObject.has(Constants.Request_For_Quotation_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                  
                    rfqCnt++;
                }
                
                /*----PR->RFQ->VQ----- */

                VQrequestParams.put("companyid", companyid);
                VQrequestParams.put("number", prq);
                VQrequestParams.put("isFromRFQ", true);

                KwlReturnObject kwlvq = accLinkDataDao.getVendorQuotation(VQrequestParams, params);
                vendor_quotation = kwlvq.getEntityList();

                vendor_quotation_json = generate_vendor_quotation_HTML(RFQ, rfqCnt, vendor_quotation, companyid, false, params,jSONObject,jSONArray);
                if (vendor_quotation_json.has("html") && !StringUtil.isNullOrEmpty(vendor_quotation_json.getString("html"))) {
                    tr += vendor_quotation_json.getString("html");
                }
            } else {
                /*---PR->VQ-- */
                VQrequestParams.put("companyid", companyid);
                VQrequestParams.put("number", prq);
                VQrequestParams.put("isFromRFQ", true);

                KwlReturnObject kwlvq = accLinkDataDao.getVendorQuotation(VQrequestParams, params);
                vendor_quotation = kwlvq.getEntityList();
                if (vendor_quotation.size() > 0) {
                   isPRLinkedToVq=true;
                    vendor_quotation_json = generate_vendor_quotation_HTML(RFQ, 0, vendor_quotation, companyid, false, params,jSONObject,jSONArray);
                    if (vendor_quotation_json.has("html") && !StringUtil.isNullOrEmpty(vendor_quotation_json.getString("html"))) {
                        tr += vendor_quotation_json.getString("html");
                    }
                }
            }
            
            
            if (!isPRLinkedToVq) {
                /* ----Check PR->PO----  */
                VQrequestParams.put("companyid", companyid);
                VQrequestParams.put("RequisitionNumber", prq);
                VQrequestParams.put("isFromPR", true);

                /*----- fetch only those PR which are linked directly to PO i.e PR->PO----- */
                KwlReturnObject kwlvq = accLinkDataDao.getPurchaseOrder(VQrequestParams, params);
                purchase_order = kwlvq.getEntityList();
                if (purchase_order.size() > 0) {
//                 JSONObject purchase_order_json = generate_purchase_order_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, companyid, vq, isDirectfromVQ, params, false);
                    purchase_order_json = generate_purchase_order_HTML(RFQ, 0, vendor_quotation, 0, purchase_order, companyid, "", false, params, false,jSONObject,jSONArray);
                    if (purchase_order_json.has("html") && !StringUtil.isNullOrEmpty(purchase_order_json.getString("html"))) {
                        tr += purchase_order_json.getString("html");
                    }
                }
            }
                       
            data.put("html", tr);
            if (vendor_quotation_json.has("soHtml") && !StringUtil.isNullOrEmpty(vendor_quotation_json.getString("soHtml"))) {
                data.put("soHtml", vendor_quotation_json.getString("soHtml"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_vendor_quotation_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, String companyid, boolean isDirectfromVQ, HashMap<String, Object> params,JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        String soHtml = "";
        boolean isSoPresent;
        JSONObject data = new JSONObject();
        List<Object[]> purchase_order = new ArrayList();
        List<Object[]> purchase_invoice = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        int vqCnt = 0;

        try {
            for (Object[] vendorQuote : vendor_quotation ) {
                String vq = vendorQuote[0].toString();
                isSoPresent = false;
                requestParams.put("companyid", companyid);
                requestParams.put("number", vq);
                if(jSONObject.has(Constants.Vendor_Quotation_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Vendor_Quotation_Key,vq);

                KwlReturnObject kwl = accLinkDataDao.getPurchaseOrder(requestParams, params);
                purchase_order = kwl.getEntityList();

                if (!StringUtil.isNullOrEmpty(vq)) {
                    requestParams.clear();
                    requestParams.put("companyid", companyid);
                    requestParams.put("number", vq);
                    requestParams.put("isFromVQ", true);

                    kwl = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                    purchase_invoice = kwl.getEntityList();
                }

                JSONObject purchase_order_json = generate_purchase_order_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, companyid, vq, isDirectfromVQ, params, false, jSONObject, jSONArray);
                
                if (purchase_order_json.has("soHtml") && !StringUtil.isNullOrEmpty(purchase_order_json.getString("soHtml"))) {
                   soHtml += purchase_order_json.getString("soHtml");
                }
                if(isDirectfromVQ){
                    if(!StringUtil.isNullOrEmpty(soHtml)){
                       tr +=  soHtml;
                       soHtml = "";
                       isSoPresent = true;
                    }
                }
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if(isSoPresent && isDirectfromVQ){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else{
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if(isSoPresent && isDirectfromVQ){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\" >&nbsp;</td>";
                } else{
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + vq + "','" + Constants.Acc_Vendor_Quotation_ModuleId + "')\">" + vq + "</a></td>";
                if (purchase_invoice.size() > 0 && purchase_order.size() == 0) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"></td>";
                    }    
                tr += "</tr>";
                if (purchase_order_json.has("html") && !StringUtil.isNullOrEmpty(purchase_order_json.getString("html"))) {
                    tr += purchase_order_json.getString("html");
                }
                if (isDirectfromVQ) {
                    tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                }
                if (vqCnt == vendor_quotation.size() - 1) {
                    if (jSONObject.has(Constants.Vendor_Quotation_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                vqCnt++;
            }
            data.put("html", tr);
            if(!StringUtil.isNullOrEmpty(soHtml) && !isDirectfromVQ){
                data.put("soHtml", soHtml);
            }
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_purchase_order_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, String companyid, String vq, boolean isDirectfromVQ, HashMap<String, Object> params, boolean isDirectfromPO, JSONObject jSONObject, JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<Object[]> goods_receipt = new ArrayList();
        List<String> sales_order = new ArrayList();
        List<Object[]> invoice = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        int poCnt = 0;
        String soHtml = "";
        KwlReturnObject kwlq;
        boolean isSoPresent;

        try {
            if (purchase_order.size() > 0) {
                
                for (Object[] purchaseOrder : purchase_order ) {
                    String po = purchaseOrder[0].toString();
                    isSoPresent = false;
                    requestParams.put("companyid", companyid);
                    requestParams.put("isFromPO", true);
                    requestParams.put("number", po);

                    if(jSONObject.has(Constants.Purchase_Order_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                    }
                    jSONObject.put(Constants.Purchase_Order_Key,po);
                    
                    KwlReturnObject kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                    invoice = kwlvi.getEntityList();

                    if (!StringUtil.isNullOrEmpty(vq)) {
                        requestParams.put("isFromVQ", true);
                        requestParams.put("isFromPO", false);
                        requestParams.put("number", vq);

                        kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                        invoice = checkDuplicates(invoice,kwlvi.getEntityList(),0);
                    }

                    requestParams.clear();
                    requestParams.put("companyid", companyid);
                    requestParams.put("number", po);
                    requestParams.put("isFromPO", true);
                    requestParams.put("isConsignment", Constants.SQL_FALSE);
                    requestParams.put("isFixedassetGro", Constants.SQL_FALSE);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                    KwlReturnObject kwlgrofromPO = accLinkDataDao.getGoodsReceipt(requestParams, params);

                    goods_receipt = kwlgrofromPO.getEntityList();

                    if (invoice.size() > 0) {
                        requestParams.clear();
                        requestParams.put("companyid", companyid);
                        requestParams.put("isFromPI", true);
                        requestParams.put("isFromPO", false);
                        requestParams.put("isConsignment", Constants.SQL_FALSE);
                        requestParams.put("isFixedassetGro", Constants.SQL_FALSE);
                        requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                        for (Object[] purchaseInvoice : invoice) {
                            String pi = purchaseInvoice[0].toString();
                            requestParams.put("number", pi);
                            KwlReturnObject kwlgrofromPI = accLinkDataDao.getGoodsReceipt(requestParams, params);
                            goods_receipt=checkDuplicates(goods_receipt, kwlgrofromPI.getEntityList(), 0);
                        }
                    }

                    requestParams.clear();
                    requestParams.put("isCallFromPO", true);
                    requestParams.put("companyid", companyid);
                    requestParams.put("poNumber", po);
                    
                    requestParams.put("isConsignment", "F");
                    requestParams.put("isLeaseSO", 0);
                    requestParams.put("isReplacementSo", 0);
                    requestParams.put("isFixedAssetDO", 0);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                    
                    kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
                    sales_order = kwlq.getEntityList();
                    
                    requestParams.put("poLinkedSo", true);
                    kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
                    sales_order.addAll(kwlq.getEntityList());
                    
                    Set set = new HashSet(sales_order);
                    sales_order = new ArrayList(set);
                    
                    if (sales_order.size() > 0) {
                        for (String so : sales_order) {
                            if (jSONObject.has(Constants.Sales_Order_Key)) {
                                JSONObject copy = new JSONObject(jSONObject.toString());
                                jSONArray.put(copy);
                                jSONObject = removeDuplicateEntries(jSONObject);
                            }
                            jSONObject.put(Constants.Sales_Order_Key, so);
                            soHtml += "<tr>";
                            soHtml += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + so + "','" + Constants.Acc_Sales_Order_ModuleId + "')\">" + so + "</a></td>";
                            soHtml += "</tr>";
                        }
                    }
                    if(isDirectfromPO){
                        if(!StringUtil.isNullOrEmpty(soHtml)){
                           tr +=  soHtml;
                           soHtml = "";
                           isSoPresent = true;
                        }
                    }
                    
                    tr += "<tr>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    if(isSoPresent && isDirectfromPO){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    } else{
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    if(isSoPresent && isDirectfromPO){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    } else{
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        if(isSoPresent){
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                        } else{
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                        }
                    }
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + po + "','" + Constants.Acc_Purchase_Order_ModuleId + "')\">" + po + "</a></td>";
                    tr += "</tr>";
                    
                    JSONObject goods_receipt_json = generate_goods_receipt_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, companyid, po, vq, false, isDirectfromVQ, params, isDirectfromPO, false,jSONObject,jSONArray);
                    if (goods_receipt_json.has("html") && !StringUtil.isNullOrEmpty(goods_receipt_json.getString("html"))) {
                        tr += goods_receipt_json.getString("html");
                    }
                    if (isDirectfromPO) {
                        tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    }
                }
            } else {
                if (!StringUtil.isNullOrEmpty(vq)) {
                    requestParams.put("companyid", companyid);
                    requestParams.put("isFromVQ", true);
                    requestParams.put("number", vq);

                    KwlReturnObject kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                    invoice = kwlvi.getEntityList();
                    if (invoice.size() > 0) {
                        requestParams.clear();
                        requestParams.put("companyid", companyid);
                        requestParams.put("isFromPI", true);
                        requestParams.put("isConsignment", Constants.SQL_FALSE);
                        requestParams.put("isFixedassetGro", Constants.SQL_FALSE);
                        requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                        for (Object[] purchaseInvoice : invoice) {
                            String pi = purchaseInvoice[0].toString();
                            requestParams.put("number", pi);
                            KwlReturnObject kwlgrofromPI = accLinkDataDao.getGoodsReceipt(requestParams, params);
                            goods_receipt = kwlgrofromPI.getEntityList();
                        }
                        JSONObject goods_receipt_json = generate_goods_receipt_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, 0, goods_receipt, companyid, "", vq, true, isDirectfromVQ, params, isDirectfromPO, false, jSONObject,jSONArray);
                        if (goods_receipt_json.has("html") && !StringUtil.isNullOrEmpty(goods_receipt_json.getString("html"))) {
                            tr += goods_receipt_json.getString("html");
                        }
                    }
                }

            }
            data.put("html", tr);
            if(!StringUtil.isNullOrEmpty(soHtml) && !isDirectfromPO){
                data.put("soHtml", soHtml);
            }
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_goods_receipt_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, int poCnt, List<Object[]> goods_receipt, String companyid, String po, String vq, boolean isFromVQ, boolean isDirectfromVQ, HashMap<String, Object> params, boolean isDirectfromPO, boolean isDirectfromGR, JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<Object[]> purchase_invoice = new ArrayList();
        List<Object[]> purchase_invoice_po = new ArrayList();
        List<Object[]> purchase_invoice_vq = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        int groCnt = 0;
        KwlReturnObject kwlvi;

        try {
            if (goods_receipt.size() > 0) {
                for (Object[] goods_order : goods_receipt) {
                    String gro = goods_order[0].toString();
                    
                    if(jSONObject.has(Constants.Goods_Receipt_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                    }
                    jSONObject.put(Constants.Goods_Receipt_Key,gro);
                    
                    tr += "<tr>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    if (purchase_order.size() > 1 && poCnt != purchase_order.size() - 1 && !isDirectfromPO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + "  height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + gro + "','" + Constants.Acc_Goods_Receipt_ModuleId + "')\">" + gro + "</td>";
                    tr += "</tr>";

                    if (!StringUtil.isNullOrEmpty(gro)) {
                        requestParams.clear();
                        requestParams.put("companyid", companyid);
                        requestParams.put("number", gro);
                        requestParams.put("isFromGR", true);

                        kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                        purchase_invoice = kwlvi.getEntityList();
                    }

                    if (!StringUtil.isNullOrEmpty(po)) {
                        requestParams.clear();
                        requestParams.put("number", po);
                        requestParams.put("isFromPO", true);
                        requestParams.put("companyid", companyid);
                        kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                        purchase_invoice_po = kwlvi.getEntityList();
                    }
                    if (!StringUtil.isNullOrEmpty(vq)) {
                        requestParams.clear();
                        requestParams.put("number", vq);
                        requestParams.put("isFromVQ", true);
                        requestParams.put("companyid", companyid);
                        kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                        purchase_invoice_vq = kwlvi.getEntityList();
                    }
                    purchase_invoice = checkDuplicates(purchase_invoice,purchase_invoice_po,0);
                    purchase_invoice = checkDuplicates(purchase_invoice,purchase_invoice_vq,0);
                    
                    JSONObject purchase_json1 = generate_purchase_invoice_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, groCnt, purchase_invoice, companyid, isDirectfromVQ, params, isDirectfromPO, false, isDirectfromGR,gro,jSONObject,jSONArray);
                    if (purchase_json1.has("html") && !StringUtil.isNullOrEmpty(purchase_json1.getString("html"))) {
                        tr += purchase_json1.getString("html");
                    }
                    if (isDirectfromGR) {
                        tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    }
                    if (groCnt == goods_receipt.size() - 1) {
                    if (jSONObject.has(Constants.Goods_Receipt_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                    groCnt++;
                }
            } else {
                requestParams.put("companyid", companyid);
                if (isFromVQ) {
                    requestParams.put("isFromVQ", true);
                    requestParams.put("number", vq);
                } else if (!StringUtil.isNullOrEmpty(po)) {
                    requestParams.put("isFromPO", true);
                    requestParams.put("number", po);
                }
                if ((!StringUtil.isNullOrEmpty(vq) && isFromVQ) || (!isFromVQ && !StringUtil.isNullOrEmpty(po))) {
                    kwlvi = accLinkDataDao.getPurchaseInvoice(requestParams, params);
                    purchase_invoice = kwlvi.getEntityList();
                }

                if (purchase_invoice.size() > 0) {
                    JSONObject purchase_json = generate_purchase_invoice_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, 0, purchase_invoice, companyid, isDirectfromVQ, params, isDirectfromPO, false, isDirectfromGR,"",jSONObject,jSONArray);
                    if (purchase_json.has("html") && !StringUtil.isNullOrEmpty(purchase_json.getString("html"))) {
                        tr += purchase_json.getString("html");
                    }
                }
            }
            data.put("html", tr);
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_purchase_invoice_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, int poCnt, List<Object[]> goods_receipt, int groCnt, List<Object[]> purchase_invoice, String companyid, boolean isDirectfromVQ, HashMap<String, Object> params, boolean isDirectfromPO, boolean isDirectfromPI, boolean isDirectfromGR,String gro,JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<Object[]> JE = new ArrayList();    // ERP-22014 as JE getting Object array from resp get*JE method of diff types
        List<String> payment = new ArrayList();
        List<Object[]> debit_note = new ArrayList();
        List<String> purchase_return = new ArrayList();
        List<String> purchase_return_pi = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        int piCnt = 0;
        KwlReturnObject kwlvi;
        
        if(!StringUtil.isNullOrEmpty(gro)){
            try {
                requestParams.put("isFromGR", true);
                requestParams.put("number", gro);
                requestParams.put("companyid", companyid);
                requestParams.put("isConsignment", Constants.SQL_FALSE);
                requestParams.put("isFixedAsset", 0);
                kwlvi = accLinkDataDao.getPurchaseReturn(requestParams, params);
                purchase_return = kwlvi.getEntityList();
            } catch (ServiceException ex) {
                Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            for (Object[] purchaseInvoice : purchase_invoice) {
                String pi = purchaseInvoice[0].toString();
                
                 if(jSONObject.has(Constants.Purchase_Invoice_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Purchase_Invoice_Key, pi);
                int isCashPurchase = 0;
                if(purchaseInvoice[2]!= null && !StringUtil.isNullOrEmpty(purchaseInvoice[2].toString())){
                    isCashPurchase =Integer.parseInt(purchaseInvoice[2].toString());
                }
                if(!StringUtil.isNullOrEmpty(pi)){
                    requestParams.clear();
                    requestParams.put("isFromPI", true);
                    requestParams.put("number", pi);
                    requestParams.put("companyid", companyid);
                    requestParams.put("isConsignment", Constants.SQL_FALSE);
                    requestParams.put("isFixedAsset", 0);
                    kwlvi = accLinkDataDao.getPurchaseReturn(requestParams, params);
                    purchase_return_pi= kwlvi.getEntityList();
                }
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (purchase_order.size() > 1 && poCnt != purchase_order.size() - 1 && !isDirectfromPO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (goods_receipt.size() > 1 && groCnt != goods_receipt.size() - 1 && (!isDirectfromPI && !isDirectfromGR)) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else if (goods_receipt.size() == 0 && (!isDirectfromPI && !isDirectfromGR)) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if(isCashPurchase == 0){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pi + "','" + Constants.Acc_Vendor_Invoice_ModuleId + "')\">" + pi + "</a></td>";
                } else{
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pi + "','" + Constants.Acc_Cash_Purchase_ModuleId + "')\">" + pi + "</a></td>";
                }

                requestParams.put("companyid", companyid);
                requestParams.put("number", pi);

                KwlReturnObject kwlJE = accLinkDataDao.getInvoiceJE(requestParams, params);
                JE = kwlJE.getEntityList();
                
                if (JE.size() > 0) {
                    Object[] jed = JE.get(0);   // to access each value in object array JE.get(0)
                    jSONObject.put("gl",jed[0]);
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }
                }
                tr += "</tr>";

                requestParams.put("companyid", companyid);
                requestParams.put("number", pi);

                KwlReturnObject kwl = accLinkDataDao.getPaymentInformation(requestParams, params);
                payment = kwl.getEntityList();
                
                requestParams.clear();
                requestParams.put("companyid", companyid);
                requestParams.put("number", pi);

                kwl = accLinkDataDao.getDebitNote(requestParams, params);
                debit_note = kwl.getEntityList();
                for (Object[] debitnote : debit_note) {
                    String dn = debitnote[0].toString();
                    payment.add(dn);
                }

                JSONObject paymentjson = generate_payment_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, groCnt, purchase_invoice, piCnt, payment, companyid, debit_note, isDirectfromVQ, params, isDirectfromPO, isDirectfromPI, isDirectfromGR,false,purchase_return_pi.size()+purchase_return.size(),0,false,jSONObject,jSONArray);
                if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                    tr += paymentjson.getString("html");
                }
                if (isDirectfromPI && purchase_return_pi.size()==0) {
                    tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                }
                JSONObject purchase_return_json = generate_purchase_return_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, 0, purchase_invoice, companyid, isDirectfromVQ, params, isDirectfromPO, isDirectfromPI, isDirectfromGR,purchase_return_pi,jSONObject,jSONArray);
                if (purchase_return_json.has("html") && !StringUtil.isNullOrEmpty(purchase_return_json.getString("html"))) {
                    tr += purchase_return_json.getString("html");
                }
                
                if (piCnt == purchase_invoice.size() - 1) {
                    if (jSONObject.has(Constants.Purchase_Invoice_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                
                piCnt++;
            }
            if(purchase_return.size()>0){
                 JSONObject purchase_return_json = generate_purchase_return_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, 0, purchase_invoice, companyid, isDirectfromVQ, params, isDirectfromPO, isDirectfromPI, isDirectfromGR,purchase_return,jSONObject,jSONArray);
                if (purchase_return_json.has("html") && !StringUtil.isNullOrEmpty(purchase_return_json.getString("html"))) {
                    tr += purchase_return_json.getString("html");
                }
            }
            data.put("html", tr);
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_payment_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, int poCnt, List<Object[]> goods_receipt, int groCnt, List<Object[]> purchase_invoice, int piCnt, List<String> payment, String companyid, List<Object[]> debit_note, boolean isDirectfromVQ, HashMap<String, Object> params, boolean isDirectfromPO, boolean isDirectfromPI, boolean isDirectfromGR, boolean isPR, int totalPr , int prCnt,boolean isStartFromPayment,JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<Object[]> JE = new ArrayList();    // ERP-22014 as JE getting Object array from resp get*JE method of diff types
        List<Object[]> credit_note = new ArrayList();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwl;
        int payCnt = 0;

        try {
            int payments = payment.size() - debit_note.size();
            for (String pay : payment) {
                if (jSONObject.has(Constants.Make_Payment_Key)) {
                    JSONObject copy = new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                 /*
                 show payment on new row
                */
                
                JSONObject copy = new JSONObject(jSONObject.toString());
                jSONArray.put(copy);
                jSONObject.remove(Constants.Purchase_Invoice_Key);
                jSONObject.remove(Constants.Purchase_Order_Key);
                jSONObject.remove(Constants.Vendor_Quotation_Key);
                jSONObject.remove(Constants.Purchase_Requisition_Key);
                jSONObject.remove(Constants.Goods_Receipt_Key);
                jSONObject.remove(Constants.Request_For_Quotation_Key);
                jSONObject.remove(Constants.Credit_Note_Key);
                jSONObject.remove(Constants.Journal_Entry_Key);
                jSONObject.put(Constants.Make_Payment_Key, pay);
                
               
                try {
                    tr += "<tr>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    if (purchase_order.size() > 1 && poCnt != purchase_order.size() - 1 && !isDirectfromPO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    if (goods_receipt.size() > 1 && groCnt != goods_receipt.size() - 1 && !isDirectfromGR) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                    }
                    if ((purchase_invoice.size() > 1 && piCnt != purchase_invoice.size() - 1 && !isDirectfromPI)) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    } else if( totalPr > 0  ){
                        if(totalPr == 0){
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                        } else if(isPR && prCnt == (totalPr-1)){
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                        } else{
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                        }
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }

                    requestParams.put("companyid", companyid);
                    requestParams.put("number", pay);
                    if (payCnt < payments) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pay + "','" + Constants.Acc_Make_Payment_ModuleId + "')\">" + pay + "</td>";
                        kwl = accLinkDataDao.getPaymentJE(requestParams, params);
                        JE = kwl.getEntityList();
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pay + "','" + Constants.Acc_Debit_Note_ModuleId + "')\">" + pay + "</td>";
                        kwl = accLinkDataDao.getDebitNoteJE(requestParams, params);
                        JE = kwl.getEntityList();
                    }                    
                    
                    if (JE.size() > 0) {
                        Object[] jed = JE.get(0);   // to access each value in object array JE.get(0)
                        jSONObject.put(Constants.Journal_Entry_Key,jed[0]);
                       
                        
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                        if(!StringUtil.isNullObject(jed)){
                            tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                        }
                    }
                    tr += "</tr>";
                    //Only for payments. (exclude Debit note)
                    if (payCnt < payments) {
                        requestParams.clear();
                        requestParams.put("paymentNumber", pay);
                        requestParams.put("isFromPayment", true);
                        requestParams.put("companyid", companyid);

                        kwl = accLinkDataDao.getCreditNote(requestParams, params);
                        credit_note = kwl.getEntityList();

                        JSONObject generate_cn = generate_CN_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, groCnt, purchase_invoice, piCnt, totalPr , prCnt, payments, payCnt, isDirectfromVQ, isDirectfromPO,isDirectfromPI,isDirectfromGR,isPR, params,credit_note,companyid,isStartFromPayment,jSONObject,jSONArray);
                        if (generate_cn.has("html") && !StringUtil.isNullOrEmpty(generate_cn.getString("html"))) {
                            tr += generate_cn.getString("html");
                        }
                    }
                } catch (ServiceException ex) {
                    Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                payCnt++;
            }
            data.put("html", tr);
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public JSONObject generate_purchase_return_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, int poCnt, List<Object[]> goods_receipt, int groCnt, List<Object[]> purchase_invoice, String companyid, boolean isDirectfromVQ, HashMap<String, Object> params, boolean isDirectfromPO, boolean isDirectfromPI, boolean isDirectfromGR,List<String> purchase_return,JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<String> JE = new ArrayList();
        List<String> payment = new ArrayList();
        List<Object[]> debit_note = new ArrayList();
        boolean isDirectfromPR = false;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwl;
        int prCnt = 0;
        if(purchase_invoice.size() > 0){
            isDirectfromPR = true;
        }
        try {
            for (String pr : purchase_return) {
                if (jSONObject.has("pi")) {
                    JSONObject copy = new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);

                }
                jSONObject.put("pi", pr);
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (purchase_order.size() > 1 && poCnt != purchase_order.size() - 1 && !isDirectfromPO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (goods_receipt.size() > 1 && groCnt != goods_receipt.size() - 1 && (!isDirectfromPI && !isDirectfromGR && !isDirectfromPR)) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else if (goods_receipt.size() == 0 && (!isDirectfromPI && !isDirectfromGR && !isDirectfromPR )) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pr + "','" + Constants.Acc_Purchase_Return_ModuleId + "')\">" + pr + "</a></td>";

                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                tr += "</tr>";

                requestParams.clear();
                requestParams.put("companyid", companyid);
                requestParams.put("number", pr);
                requestParams.put("isFromPR", true);

                kwl = accLinkDataDao.getDebitNote(requestParams, params);
                debit_note = kwl.getEntityList();
                for (Object[] debitnote : debit_note) {
                    String dn = debitnote[0].toString();
                    payment.add(dn);
                }

                JSONObject paymentjson = generate_payment_HTML(RFQ, rfqCnt, vendor_quotation, vqCnt, purchase_order, poCnt, goods_receipt, groCnt, purchase_invoice, prCnt, payment, companyid, debit_note, isDirectfromVQ, params, isDirectfromPO, isDirectfromPI, isDirectfromGR,true,purchase_return.size(),prCnt,false,jSONObject,jSONArray);
                if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                    tr += paymentjson.getString("html");
                }
                 if (prCnt == purchase_return.size() - 1) {
                    if (jSONObject.has("pi")) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                prCnt++;
            }
            if (isDirectfromPI) {
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
            }
            data.put("html", tr);
        } catch (JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public JSONObject generate_CN_HTML(List<String> RFQ, int rfqCnt, List<Object[]> vendor_quotation, int vqCnt, List<Object[]> purchase_order, int poCnt, List<Object[]> goods_receipt, int groCnt, List<Object[]> purchase_invoice, int piCnt, int totalPr , int prCnt, int totalpayment, int payCnt, boolean isDirectfromVQ, boolean isDirectfromPO, boolean isDirectfromPI, boolean isDirectfromGR, boolean isPR, HashMap<String, Object> params,List<Object[]> creditNote, String companyid,boolean isStartFromPayment,JSONObject jSONObject,JSONArray jSONArray) {
        String tr = "";
        JSONObject data = new JSONObject();
        List<Object[]> JE = new ArrayList();    // ERP-22014 as JE getting Object array from resp get*JE method of diff types
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwl;
        int cntype = 1;

        try {
            for (Object[] credit_note : creditNote) {
                String cn = credit_note[1].toString();
                if(credit_note[2] != null && !StringUtil.isNullOrEmpty(credit_note[2].toString())){
                    cntype = (int)credit_note[2];
                }
                if (jSONObject.has(Constants.Credit_Note_Key)) {
                    JSONObject copy = new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Credit_Note_Key, cn);
                
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (vendor_quotation.size() > 1 && vqCnt != vendor_quotation.size() - 1 && !isDirectfromVQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (purchase_order.size() > 1 && poCnt != purchase_order.size() - 1 && !isDirectfromPO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (goods_receipt.size() > 1 && groCnt != goods_receipt.size() - 1 && !isDirectfromGR) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                }
                if ((purchase_invoice.size() > 1 && piCnt != purchase_invoice.size() - 1 && !isDirectfromPI)) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else if( totalPr > 0 && !isDirectfromPI  ){
                    if(totalPr == 0){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    } else if(isPR && prCnt == (totalPr-1)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    } else{
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    }
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (totalpayment > 1 && payCnt != totalpayment && payCnt < totalpayment && !isStartFromPayment) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + cn + "','" + Constants.Acc_Credit_Note_ModuleId + "','"+cntype+"')\">" + cn + "</td>";
                requestParams.put("companyid", companyid);
                requestParams.put("number", cn);
                kwl = accLinkDataDao.getCreditNoteJE(requestParams, params);
                JE = kwl.getEntityList();
                if (JE.size() > 0) {
                    Object[] jed = JE.get(0);   // to access each value in object array JE.get(0)
                    jSONObject.put("gl",jed[0]);
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }
                }
                tr += "</tr>";
            }
            if (isStartFromPayment) {
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
            }
            data.put("html", tr);
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public String generateSalesSideHTML(String companyid, HashMap<String, Object> params,JSONArray jSONArray) {
        List<Object[]> customer_quotation = new ArrayList();
        List<Object[]> next_data = new ArrayList();
        List<Object[]> invoice = new ArrayList();
        List<Object[]> next_data_within_date_range = new ArrayList();
        List<Object[]> sales_order = new ArrayList();
        List<Object[]> purchase_order = new ArrayList();
        List<Object[]> start_From_Delivery_order = new ArrayList();
        List<String> receipt = new ArrayList();
        List delivery_order = new ArrayList();
        Set<String> used_DO = new HashSet();
        Set<String> used_Invoices = new HashSet();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String tableHTML = "",mainTable = "",tr ="";
        JSONObject jSONObject=new JSONObject();
        try {

            requestParams.put("companyid", companyid);

            /**
             * **************************** Get Customer Quotation  *********************************************
             */
            KwlReturnObject kwlq = accLinkDataDao.getCqlinked(requestParams, params);
            customer_quotation = kwlq.getEntityList();

            mainTable += "<div id=\"linkedsalestable\" style=\"width:100%;overflow: auto; height: 100%;position: absolute;\"><table border=0 style=\"width:100%; border-spacing: 0; border-collapse: collapse; overflow: scroll;\">";
            mainTable += "<tbody>";
            mainTable += "<tr>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
            mainTable += "</tr>";

            for (int cqCnt = 0; cqCnt < customer_quotation.size(); cqCnt++) {
                Object[] cq = customer_quotation.get(cqCnt);
                String cq_number = (String) cq[0];
                int moduleid = (int) cq[1];
                String docId = (String) cq[2];

                tr = "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + cq_number + "','" + Constants.Acc_Customer_Quotation_ModuleId + "')\">" + cq_number + "</td>";
                tr += "</tr>";
                jSONObject=new JSONObject();
                jSONObject.put(Constants.Customer_Quatation_Key, cq_number);
                // If moduleid is sales order create sales order html Tr
                if (Constants.Acc_Sales_Order_ModuleId == moduleid) {
                    requestParams.put("id", docId);
                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams.put("companyid", companyid);
                    
                    requestParams.put("isConsignment", "F");
                    requestParams.put("isLeaseSO", 0);
                    requestParams.put("isReplacementSo", 0);
                    requestParams.put("isFixedAssetDO", 0);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                    
                    kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
                    next_data = kwlq.getEntityList();

//                    tableHTML += generate_Sales_Order_HTML(params, next_data, customer_quotation.size(), cqCnt, companyid, false);
                    JSONObject sales_order_json = generate_Sales_Order_HTML(params, next_data, customer_quotation.size(), cqCnt, companyid, false, used_DO, used_Invoices,jSONArray,jSONObject);
                    if (sales_order_json.has("poHtml") && !StringUtil.isNullOrEmpty(sales_order_json.getString("poHtml"))) {
                        tr = sales_order_json.getString("poHtml")+tr;
                    }
                    if (sales_order_json.has("html") && !StringUtil.isNullOrEmpty(sales_order_json.getString("html"))) {
                        tr += sales_order_json.getString("html");
                    }
                } else {
                    // If moduleid is invoice create invoice html Tr

                    requestParams.clear();
                    requestParams.put("id", docId);
                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams.put("companyid", companyid);
                    requestParams.put("isConsignment", Constants.SQL_FALSE);
                    requestParams.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
                    requestParams.put("isFixedAssetInvoice", Constants.SQL_FALSE);
                    requestParams.put("isDraft", Constants.SQL_FALSE);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                    
                    kwlq = accLinkDataDao.getInvoices(requestParams, params);
                    next_data = kwlq.getEntityList();

                    for (Object[] obj : next_data) {

                        // Check whether any DO's created by invoice
                        requestParams.clear();
                        requestParams.put("id", obj[0]);
                        requestParams.put("sourceflag", 1);
                        requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        requestParams.put("companyid", companyid);
                        
                        requestParams.put("isConsignment", "F");
                        requestParams.put("isLeaseDO", 0);
                        requestParams.put("isFixedAssetDO", 0);
                        requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                        
                        kwlq = accLinkDataDao.getDeliveryOrder(requestParams, params);
                        delivery_order = kwlq.getEntityList();
                        if (delivery_order.size() > 0) {
//                            tableHTML += generate_Delivery_Order_HTML(params, delivery_order, customer_quotation.size(), cqCnt, 0, 0, companyid, false, false, true);
                            JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, delivery_order, customer_quotation.size(), cqCnt, 0, 0, companyid, false, false, true,used_DO,used_Invoices,jSONArray,jSONObject);
                            if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                                tr += delivery_order_json.getString("html");
                            }
                        } else {
//                            tableHTML += generate_Customer_Invoice_HTML(params, next_data, 0, 0, 0, 0, 0, 0, true, false, false, companyid, false, false,"");
                            JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, next_data, 0, 0, 0, 0, 0, 0, true, false, false, companyid, false, false,"",used_Invoices,jSONArray,jSONObject);
                            if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                                tr += sales_invoice.getString("html");
                            }
                        }
                    }
                }

                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tableHTML += tr;
                if (jSONObject.length() >= 1) {
                    jSONArray.put(jSONObject);
                }
                jSONObject = new JSONObject();
            }          

           
            /**
             * *********************** Get links Starting from SO directly  *********************************************************************
             */
            jSONObject=new JSONObject();
            requestParams.clear();
            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseSO", 0);
            requestParams.put("isReplacementSo", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("onlyPOLinkedSO", true);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            requestParams.put("companyid", companyid);
            
            kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
            sales_order = kwlq.getEntityList();
            
            requestParams.clear();
            requestParams.put("isStartFromSalesOrder", true);
            requestParams.put("companyid", companyid);
            requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
            requestParams.put("SOModuleid", Constants.Acc_Sales_Order_ModuleId);
            requestParams.put("sourceflag", 0);
            
            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseSO", 0);
            requestParams.put("isReplacementSo", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            
            kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
            next_data = kwlq.getEntityList();
            sales_order = checkDuplicates(sales_order,next_data,1);
            
            requestParams.put("SalesOrderWithinDateRange", true);
            kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
            next_data_within_date_range = kwlq.getEntityList();
            
            sales_order = checkDuplicates(sales_order,next_data_within_date_range,1);
            
            if(sales_order.size() > 0){
//                tableHTML += generate_Sales_Order_HTML(params, sales_order, 0, 0, companyid, true);
                JSONObject sales_order_json = generate_Sales_Order_HTML(params, sales_order, 0, 0, companyid, true, used_DO, used_Invoices,jSONArray,jSONObject);
                if (sales_order_json.has("html") && !StringUtil.isNullOrEmpty(sales_order_json.getString("html"))) {
                    tableHTML += sales_order_json.getString("html");
                }
                tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                if (jSONObject.length() >= 1) {
                    jSONArray.put(jSONObject);
                }
                jSONObject=new JSONObject();
            }


            /**
             * *********************** Get links Starting from DO directly  *********************************************************************
             */
            jSONObject=new JSONObject();
            requestParams.clear();
            requestParams.put("isStartFromDeliveryOrder", true);
            requestParams.put("companyid", companyid);
            requestParams.put("isFromCQL", true);
            requestParams.put("sourceflag", Constants.LINK_SOURCE_FLAG_0);
            
            requestParams.put("DOModuleId", Constants.Acc_Delivery_Order_ModuleId);
            requestParams.put("DOSourceFlag", Constants.LINK_SOURCE_FLAG_1);
            
            requestParams.put("invoiceModuleId", Constants.Acc_Invoice_ModuleId);
            requestParams.put("invoiceSourceFlag", Constants.LINK_SOURCE_FLAG_0);
            
            requestParams.put("includeInvoiceLinking", true);
            
            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseDO", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

            kwlq = accLinkDataDao.getDeliveryOrder(requestParams, params);
            next_data = kwlq.getEntityList();
            start_From_Delivery_order.addAll(next_data);
            
            requestParams.remove("includeInvoiceLinking");
            requestParams.put("DOWithinDateRange", true);
            
            kwlq = accLinkDataDao.getDeliveryOrder(requestParams, params);
            next_data_within_date_range= kwlq.getEntityList();
            
            
            start_From_Delivery_order = checkDuplicates(start_From_Delivery_order,next_data_within_date_range,1);
            start_From_Delivery_order = removeDuplicateRecords( start_From_Delivery_order, used_DO, 0 );
            
            requestParams.clear();
            requestParams.put("companyid", companyid);
            requestParams.put("isCallFromSO", true);
            requestParams.put("poWithinDateRange", true);
            kwlq = accLinkDataDao.getPurchaseOrder(requestParams, params);
            purchase_order = kwlq.getEntityList();
            
            if( purchase_order.size() > 0 ){
               for (Object[] purchaseOrder : purchase_order ) {
                    String po = purchaseOrder[0].toString();
                    tableHTML += "<tr>";
                    tableHTML += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + po + "','" + Constants.Acc_Purchase_Order_ModuleId + "')\">" + po + "</a></td>";
                    tableHTML += "</tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                   if (jSONObject.length() >= 1) {
                       jSONArray.put(jSONObject);
                   }
                   jSONObject = new JSONObject();
                }
            }
            
            if (start_From_Delivery_order.size() > 0) {
                JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, start_From_Delivery_order, 0, 0, 0, 0, companyid, false, true, false, used_DO, used_Invoices,jSONArray,jSONObject);
                if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                    tableHTML += delivery_order_json.getString("html");
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }
            }
//            tableHTML += generate_Delivery_Order_HTML(params, start_From_Delivery_order, 0, 0, 0, 0, companyid, false, true, false);

            /**
             * *********************** Get links Starting from Invoice directly  *********************************************************************
             */
            jSONObject=new JSONObject();
            requestParams.clear();
            requestParams.put("isStartFromInvoice", true);
            requestParams.put("companyid", companyid);
            requestParams.put("addReceiptModuleCheck", true);
            requestParams.put("invoiceModuleId", Constants.Acc_Invoice_ModuleId);
            requestParams.put("invoiceSourceFlag", Constants.LINK_SOURCE_FLAG_0);
            
            requestParams.put("isConsignment", Constants.SQL_FALSE);
            requestParams.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
            requestParams.put("isFixedAssetInvoice", Constants.SQL_FALSE);
            requestParams.put("isDraft", Constants.SQL_FALSE);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            
            kwlq = accLinkDataDao.getInvoices(requestParams, params);
            invoice = kwlq.getEntityList();
            
            requestParams.put("InvoiceWithinDateRange", true);
            requestParams.put("DOSourceFlag", Constants.LINK_SOURCE_FLAG_0);
            kwlq = accLinkDataDao.getInvoices(requestParams, params);
            next_data_within_date_range = kwlq.getEntityList();
            
            invoice = checkDuplicates(invoice,next_data_within_date_range,1);
            invoice = removeDuplicateRecords( invoice, used_Invoices, 0 );
            
//            tableHTML += generate_Customer_Invoice_HTML(params, invoice, 0, 0, 0, 0, 0, 0, false, false, true, companyid, false, false,"");
            if (invoice.size() > 0) {
                JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, invoice, 0, 0, 0, 0, 0, 0, false, false, true, companyid, false, false, "", used_Invoices,jSONArray,jSONObject);
                if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                    tableHTML += sales_invoice.getString("html");
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }

            }
            /**
             * *********************** Get links Starting from Receipt directly  *********************************************************************
             */
            jSONObject = new JSONObject();
            requestParams.clear();
            requestParams.put("isdebitnote", true);
            requestParams.put("companyid", companyid);
            requestParams.put("excludePaymentsUsedInInvoice", true);
            kwlq = accLinkDataDao.getReceivedPayments(requestParams, params);
            receipt = kwlq.getEntityList();
            
            if (receipt.size() > 0) {
                JSONObject paymentjson = generate_Payment_CN_HTML(params, receipt, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, companyid, false, false, true, 0, 0, true,jSONArray,jSONObject);
                if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                    tableHTML += paymentjson.getString("html");
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }
            }
            
            mainTable += tableHTML;
            mainTable += "</tbody>";
            mainTable += "</table></div>";

        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }

        return mainTable;

    }

    public JSONObject generate_Sales_Order_HTML(HashMap<String, Object> params, List<Object[]> sales_order, int totalcq, int cqCnt, String companyid, boolean isStartFromSO, Set<String> used_DO, Set<String> used_Invoices,JSONArray jSONArray,JSONObject jSONObject) {
            String tr = "",tableHTML= "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject kwlq;
            List<Object[]> next_data = new ArrayList();
            List<Object[]> delivery_order = new ArrayList();
            List<Object[]> purchase_order = new ArrayList();
            List<Object[]> cross_linked_data = new ArrayList();
            JSONObject data = new JSONObject();
            String poHtml = "";
            
            try {
                for (int soCnt = 0; soCnt < sales_order.size(); soCnt++) {
                    boolean isPoPresent = false;
                    Object[] so = sales_order.get(soCnt);
                    tr= "";
                    String docId = (String) so[0];
                    String so_number = (String) so[1];
                    if (jSONObject.has(Constants.Sales_Order_Key)) {
                    JSONObject copy=new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                    /**
                     * Remove all Keys because need to show SO at first row.
                     */
                    if (jSONObject.length() > 0) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject.remove(Constants.Customer_Invoice_Key);
                        jSONObject.remove(Constants.Sales_Order_Key);
                        jSONObject.remove(Constants.Customer_Quatation_Key);
                        jSONObject.remove(Constants.Delivery_Order_Key);
                        jSONObject.remove(Constants.Journal_Entry_Key);
                        jSONObject.remove(Constants.Purchase_Order_Key);
                        jSONObject.remove(Constants.Received_Payment_Key);
                    }
                    jSONObject.put(Constants.Sales_Order_Key, so_number);
                    if(!StringUtil.isNullOrEmpty(so_number)){
                        requestParams.clear();
                        requestParams.put("companyid", companyid);
                        requestParams.put("isCallFromSO", true);
                        requestParams.put("soNumber", so_number);

                        kwlq = accLinkDataDao.getPurchaseOrder(requestParams, params);
                        purchase_order = kwlq.getEntityList();
                        
                        requestParams.put("solinkedpo", true);
                        kwlq = accLinkDataDao.getPurchaseOrder(requestParams, params);
                        cross_linked_data = kwlq.getEntityList();
                        
                        purchase_order = checkDuplicates(purchase_order,cross_linked_data,0);
                        
                        if( purchase_order.size() > 0 ){
                           isPoPresent = true;
                            for (Object[] purchaseOrder : purchase_order ) {
                                String po = purchaseOrder[0].toString();
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put(Constants.Purchase_Order_Key, po);
                                poHtml += "<tr>";
                                poHtml += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + po + "','" + Constants.Acc_Purchase_Order_ModuleId + "')\">" + po + "</a></td>";
                                poHtml += "</tr>";
                                if (jSONObject.length() >= 1) {
                                    jSONArray.put(jsonObject);
                                }
                                jsonObject = new JSONObject();
                            }
                        }
                        if(!StringUtil.isNullOrEmpty(poHtml) && isStartFromSO){
                            tr = poHtml + tr;
                            poHtml = "";
                        }
                    }
                    
                    tr += "<tr>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    if(isPoPresent && isStartFromSO){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\" >&nbsp;</td>";
                    } else{
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    }
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + so_number + "','" + Constants.Acc_Sales_Order_ModuleId + "')\">" + so_number + "</td>";
                    tr += "</tr>";
                    
                    requestParams.put("id", docId);
                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("companyid", companyid);
                    
                    requestParams.put("isConsignment", "F");
                    requestParams.put("isLeaseDO", 0);
                    requestParams.put("isFixedAssetDO", 0);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                    
                    kwlq = accLinkDataDao.getDeliveryOrder(requestParams, params);
                    delivery_order = kwlq.getEntityList();
                    
                    requestParams.clear();
                    requestParams.put("id", docId);
                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    requestParams.put("companyid", companyid);
                    requestParams.put("isConsignment", Constants.SQL_FALSE);
                    requestParams.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
                    requestParams.put("isFixedAssetInvoice", Constants.SQL_FALSE);
                    requestParams.put("isDraft", Constants.SQL_FALSE);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                    
                    kwlq = accLinkDataDao.getInvoices(requestParams, params);
                    next_data = kwlq.getEntityList();
                    
                    for (Object[] obj : next_data) {
                        requestParams.clear();
                        requestParams.put("id", obj[0]);
                        requestParams.put("sourceflag", 1);
                        requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        requestParams.put("companyid", companyid);
                        
                        requestParams.put("isConsignment", "F");
                        requestParams.put("isLeaseDO", 0);
                        requestParams.put("isFixedAssetDO", 0);
                        requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
                        
                        kwlq = accLinkDataDao.getDeliveryOrder(requestParams, params);
                        delivery_order = checkDuplicates(delivery_order,kwlq.getEntityList(),1);
                    }
                    // If DO's then go for Do HTMl Tr
                    if (delivery_order.size() > 0) {
//                        tr += generate_Delivery_Order_HTML(params, delivery_order, totalcq, cqCnt, sales_order.size(), soCnt, companyid, isStartFromSO, false, false);
                        JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, delivery_order, totalcq, cqCnt, sales_order.size(), soCnt, companyid, isStartFromSO, false, false,used_DO,used_Invoices,jSONArray,jSONObject);
                        if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                            tr += delivery_order_json.getString("html");
                        }
                    } else {
                        // If NO DO's then go for Invoices
//                        tr += generate_Customer_Invoice_HTML(params, next_data, totalcq, cqCnt, sales_order.size(), soCnt, 0, 0, false, true, false, companyid, isStartFromSO, false,"");
                        JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, next_data, totalcq, cqCnt, sales_order.size(), soCnt, 0, 0, false, true, false, companyid, isStartFromSO, false,"",used_Invoices,jSONArray,jSONObject);
                        if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                            tr += sales_invoice.getString("html");
                        }
                    }
                    // Add Blanks
                    if (isStartFromSO) {
                        tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    }
                    
                    tableHTML+=tr;
                }
            data.put("html", tableHTML);
            if(!StringUtil.isNullOrEmpty(poHtml)){
                data.put("poHtml", poHtml);
            }
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject removeDuplicateEntries(JSONObject jSONObject) {
        /*
         remove all entries from json object
         */
          
        jSONObject.remove(Constants.Vendor_Quotation_Key);
        jSONObject.remove(Constants.Goods_Receipt_Key);
        jSONObject.remove(Constants.Purchase_Requisition_Key);
        jSONObject.remove(Constants.Request_For_Quotation_Key);
        jSONObject.remove(Constants.Credit_Note_Key);
        jSONObject.remove(Constants.Purchase_Invoice_Key);
        jSONObject.remove(Constants.Make_Payment_Key);
        jSONObject.remove(Constants.Purchase_Order_Key);
        jSONObject.remove(Constants.Customer_Invoice_Key);
        jSONObject.remove(Constants.Sales_Order_Key);
        jSONObject.remove(Constants.Customer_Quatation_Key);
        jSONObject.remove(Constants.Delivery_Order_Key);
        jSONObject.remove(Constants.Received_Payment_Key);
        jSONObject.remove(Constants.Journal_Entry_Key);
        jSONObject.remove(Constants.Debit_Note_Key);
        return jSONObject;
    }
    public JSONObject generate_Delivery_Order_HTML(HashMap<String, Object> params, List<Object[]> delivery_order, int totalcq, int cqCnt, int totalso, int soCnt, String companyid, boolean isStartFromSO, boolean isStartFromDO, boolean isStartFromCQ, Set<String> used_DO, Set<String> used_Invoices,JSONArray jSONArray,JSONObject jSONObject) {
        String tr = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwlq;
        JSONObject data = new JSONObject();
        List<Object[]> next_data = new ArrayList();

        try {
            for (int doCnt = 0; doCnt < delivery_order.size(); doCnt++) {
                Object[] delivery_o = delivery_order.get(doCnt);

                String docid = (String) delivery_o[0];
                String do_number = (String) delivery_o[1];
                if (jSONObject.has(Constants.Delivery_Order_Key)) {
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Delivery_Order_Key, do_number);
                used_DO.add(docid);
                /**
                 * to Show DO JE in Linking Information Report.
                 */
                requestParams.put("companyid", companyid);
                requestParams.put("id", docid);

                kwlq = accLinkDataDao.getDeliveryOrderJE(requestParams, params);
                next_data = kwlq.getEntityList();
                requestParams.clear();
               
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (isStartFromCQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + do_number + "','" + Constants.Acc_Delivery_Order_ModuleId + "')\">" + do_number + "</td>";
                if (!StringUtil.isNullObject(next_data) && next_data.size() > 0) {
                    Object[] jed = next_data.get(0);      // to access each value in object array JE.get(0)              
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }
                    jSONObject.put("gl", jed[0]);
                }
                tr += "</tr>";

                requestParams.clear();
                requestParams.put("id", docid);
//                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                requestParams.put("companyid", companyid);
                
                requestParams.put("isConsignment", Constants.SQL_FALSE);
                requestParams.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
                requestParams.put("isFixedAssetInvoice", Constants.SQL_FALSE);
                requestParams.put("isDraft", Constants.SQL_FALSE);
                requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                kwlq = accLinkDataDao.getInvoices(requestParams, params);
                next_data = kwlq.getEntityList();
                
//                tr += generate_Customer_Invoice_HTML(params, next_data, totalcq, cqCnt, totalso, soCnt, delivery_order.size(), doCnt, false, false, false, companyid, isStartFromSO, isStartFromDO, docid);
                JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, next_data, totalcq, cqCnt, totalso, soCnt, delivery_order.size(), doCnt, false, false, false, companyid, isStartFromSO, isStartFromDO, docid,used_Invoices,jSONArray,jSONObject);
                if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                    tr += sales_invoice.getString("html");
                }
                if (isStartFromDO) {
                    tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                }
                if (doCnt == delivery_order.size() - 1) {
                    if (jSONObject.has(Constants.Delivery_Order_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
            }
            data.put("html", tr);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_Customer_Invoice_HTML(HashMap<String, Object> params, List customer_invoice, int totalcq, int cqCnt, int totalso, int soCnt, int totaldo, int doCnt, boolean isStartFromCQ, boolean isStartFromPo, boolean isStartFromInvoice, String companyid, boolean isStartFromSO, boolean isStartFromDO, String doId,Set<String> used_Invoices,JSONArray jSONArray,JSONObject jSONObject) {
        String tr = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwlq;
        List next_data = new ArrayList();
        List credit_note = new ArrayList();
        List Sales_return = new ArrayList();
        List Sales_return_invoice = new ArrayList();
        JSONObject data = new JSONObject();
        
        
        if(!StringUtil.isNullOrEmpty(doId)){
            try {
                requestParams.put("id", doId);
                requestParams.put("companyid", companyid);
                requestParams.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                requestParams.put("isConsignment", Constants.SQL_FALSE);
                requestParams.put("isFixedAsset", 0);
                requestParams.put("isLeaseSalesReturn", 0);
                kwlq = accLinkDataDao.getSalesReturn(requestParams, params);
                Sales_return = kwlq.getEntityList();
            } catch (ServiceException ex) {
                Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {

            for (int invoiceCnt = 0; invoiceCnt < customer_invoice.size(); invoiceCnt++) {
                Object[] invoices = (Object[]) customer_invoice.get(invoiceCnt);
                String docid = (String) invoices[0];
                String invoice_number = (String) invoices[1];
                if(jSONObject.has(Constants.Customer_Invoice_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                /**
                 * Remove All keys from existing because show delivery order on
                 * new row.
                 */
                if (jSONObject.length() > 0) {
                    JSONObject copy = new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject.remove(Constants.Customer_Invoice_Key);
                    jSONObject.remove(Constants.Sales_Order_Key);
                    jSONObject.remove(Constants.Customer_Quatation_Key);
                    jSONObject.remove(Constants.Delivery_Order_Key);
                    jSONObject.remove(Constants.Journal_Entry_Key);
                    jSONObject.remove(Constants.Purchase_Order_Key);
                    jSONObject.remove(Constants.Received_Payment_Key);
                }
                jSONObject.put(Constants.Customer_Invoice_Key, invoice_number);
                int cashtransaction = 0;
                if(invoices[2] != null && !StringUtil.isNullOrEmpty(invoices[2].toString())){
                    cashtransaction = Integer.parseInt(invoices[2].toString());
                }
                
                used_Invoices.add(docid);
                
                requestParams.put("companyid", companyid);
                requestParams.put("id", docid);

                kwlq = accLinkDataDao.getSalesInvoiceJE(requestParams, params);
                next_data = kwlq.getEntityList();
                
                requestParams.clear();
                requestParams.put("id", docid);
                requestParams.put("companyid", companyid);
                requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                requestParams.put("isConsignment", Constants.SQL_FALSE);
                requestParams.put("isFixedAsset", 0);
                requestParams.put("isLeaseSalesReturn", 0);
                kwlq = accLinkDataDao.getSalesReturn(requestParams, params);
                Sales_return_invoice = kwlq.getEntityList();

                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";

                // If Start from CQ add two lines
                if (isStartFromCQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else if (isStartFromPo && invoiceCnt == 0) {
                    if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;border-top:1px solid black;\">&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    }
                } else {
                    if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + "  style= \"border-left:1px solid black;\" >&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                    }
                }
                if(cashtransaction == 0){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + invoice_number + "','" + Constants.Acc_Invoice_ModuleId + "')\">" + invoice_number + "</td>";
                } else{
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + invoice_number + "','" + Constants.Acc_Cash_Sales_ModuleId + "')\">" + invoice_number + "</td>";
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                if (next_data.size() > 0) {
                    Object[] jed = (Object[])next_data.get(0);  // to access each value in object array JE.get(0)
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }
                    jSONObject.put("gl", jed[0]);
                }
                tr += "</tr>";                
                requestParams.clear();
                requestParams.put("invoiceid", docid);
                requestParams.put("companyid", companyid);
                kwlq = accLinkDataDao.getReceivedPayments(requestParams, params);
                next_data = kwlq.getEntityList();
                
                requestParams.clear();
                requestParams.put("id", docid);
                requestParams.put("sourceflag", 1);
                requestParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
                requestParams.put("companyid", companyid);

                kwlq = accLinkDataDao.getCreditNote(requestParams, params);
                credit_note = kwlq.getEntityList();
                for (Object obj : credit_note) {
                    Object[] obj1 = (Object[]) obj;
                    next_data.add(obj1[1]);
                }

//                tr += generate_Payment_CN_HTML(params, next_data, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt, customer_invoice.size(), invoiceCnt, credit_note.size(), isStartFromInvoice, companyid, isStartFromSO, isStartFromDO,false,0,0);
                JSONObject paymentjson = generate_Payment_CN_HTML(params, next_data, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt, customer_invoice.size()+Sales_return.size()+Sales_return_invoice.size(), invoiceCnt, credit_note.size(), isStartFromInvoice, companyid, isStartFromSO, isStartFromDO,false,Sales_return_invoice.size(),0,false,jSONArray,jSONObject);
                if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                    tr += paymentjson.getString("html");
                }
                JSONObject sales_return = generate_Sales_Return_HTML(params, Sales_return_invoice, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt,customer_invoice.size(),0, false, false, isStartFromInvoice, companyid, isStartFromSO, isStartFromDO, false,0,jSONArray,jSONObject);
                if (sales_return.has("html") && !StringUtil.isNullOrEmpty(sales_return.getString("html"))) {
                    tr += sales_return.getString("html");
                }
                
                if ( isStartFromInvoice && Sales_return_invoice.size() == 0 ) {
                    tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                }
                  if (invoiceCnt == customer_invoice.size() - 1) {
                    if (jSONObject.has(Constants.Customer_Invoice_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
                
            }
//            Sales_return = checkDuplicates(Sales_return, Sales_return_invoice, 1);
            JSONObject sales_return = generate_Sales_Return_HTML(params, Sales_return, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt,customer_invoice.size(),0, false, false, isStartFromInvoice, companyid, isStartFromSO, isStartFromDO, false,0,jSONArray,jSONObject);
            if (sales_return.has("html") && !StringUtil.isNullOrEmpty(sales_return.getString("html"))) {
                tr += sales_return.getString("html");
            }
//            tr += generate_Sales_Return_HTML(params, Sales_return, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt,customer_invoice.size(),0, false, false, isStartFromInvoice, companyid, isStartFromSO, isStartFromDO, true);
            if (isStartFromDO) {
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
            }
            data.put("html", tr);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    public JSONObject generate_Sales_Return_HTML(HashMap<String, Object> params, List sales_return, int totalcq, int cqCnt, int totalso, int soCnt, int totaldo, int doCnt,int totalInvoice, int invoiceCnt, boolean isStartFromCQ, boolean isStartFromPo, boolean isStartFromInvoice, String companyid, boolean isStartFromSO, boolean isStartFromDO, boolean isSalesReturnFromDo,int totalSRDO,JSONArray jSONArray,JSONObject jSONObject) {
        String tr = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwlq;
        List next_data = new ArrayList();
        List credit_note = new ArrayList();
        JSONObject data = new JSONObject();
        boolean isDirectfromSR = false;
        if(totalInvoice > 0){
            isDirectfromSR = true;
        }
        try {

            for (int srCnt = 0; srCnt < sales_return.size(); srCnt++) {
                Object[] sr = (Object[]) sales_return.get(srCnt);
                String docid = (String) sr[0];
                String sr_number = (String) sr[1];
                if(jSONObject.has(Constants.Customer_Invoice_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Customer_Invoice_Key, sr_number);
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";

                // If Start from CQ add two lines
                if (isStartFromCQ) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                } else if (isStartFromPo && srCnt == 0) {
                    if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;border-top:1px solid black;\">&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-top:1px solid black;\">&nbsp;</td>";
                    }
                } else {
                    if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + "  style= \"border-left:1px solid black;\" >&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " >&nbsp;</td>";
                    }
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + sr_number + "','" + Constants.Acc_Sales_Return_ModuleId + "')\">" + sr_number + "</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\" >&nbsp;</td>";
                tr += "</tr>";
                 if(!isSalesReturnFromDo){
                    requestParams.clear();
                    requestParams.put("number", sr_number);
                    requestParams.put("companyid", companyid);
                    requestParams.put("isFromSR", true);

                    kwlq = accLinkDataDao.getCreditNote(requestParams, params);
                    List li = kwlq.getEntityList();
                    for (Object obj : li) {
                        Object[] obj1 = (Object[]) obj;
                        credit_note.add(obj1[1]);
                    }
                    JSONObject paymentjson = generate_Payment_CN_HTML(params, credit_note, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt, totalInvoice, invoiceCnt, credit_note.size(), isStartFromInvoice, companyid, isStartFromSO, isStartFromDO,true,sales_return.size(),srCnt,false,jSONArray,jSONObject);
                    if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                        tr += paymentjson.getString("html");
                    }
                    if (isStartFromInvoice && !isDirectfromSR) {
                        tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    }
                }
                if (srCnt == sales_return.size() - 1) {
                    if (jSONObject.has(Constants.Customer_Invoice_Key)) {
                        JSONObject copy = new JSONObject(jSONObject.toString());
                        jSONArray.put(copy);
                        jSONObject = removeDuplicateEntries(jSONObject);
                    }
                }
            }
            if (isStartFromInvoice ) {
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
            }
            data.put("html", tr);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public JSONObject generate_Payment_CN_HTML(HashMap<String, Object> params, List<String> payments, int totalcq, int cqCnt, int totalso, int soCnt, int totaldo, int doCnt, int totalin, int inCnt, int cnCnt, boolean isStartFromInvoice, String companyid, boolean isStartFromSO, boolean isStartFromDO,boolean isSR, int totalSr, int srCnt, boolean isStartFromPayment,JSONArray jSONArray,JSONObject jSONObject) {
        String tr = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwl;
        List<Object[]> next_data = new ArrayList();
        List<Object[]> debit_note = new ArrayList();
        int payCnt = 0;
        JSONObject data = new JSONObject();
        int difference = payments.size() - cnCnt;

        try {
            for (String pay : payments) {
                requestParams.put("companyid", companyid);
                requestParams.put("number", pay);
                if (jSONObject.has(Constants.Received_Payment_Key)) {
                    JSONObject copy = new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                /*
                Remove All keys from existing becoz show payment on new row
                */
                JSONObject copy = new JSONObject(jSONObject.toString());
                jSONArray.put(copy);
                jSONObject.remove(Constants.Customer_Invoice_Key);
                jSONObject.remove(Constants.Sales_Order_Key);
                jSONObject.remove(Constants.Customer_Quatation_Key);
                jSONObject.remove(Constants.Delivery_Order_Key);
                jSONObject.put(Constants.Received_Payment_Key, pay);
                if (payCnt < difference) {
                    kwl = accLinkDataDao.getSalesPaymentJE(requestParams, params);
                    next_data = kwl.getEntityList();
                } else {
                    kwl = accLinkDataDao.getCreditNoteJE(requestParams, params);
                    next_data = kwl.getEntityList();
                }
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (totaldo > 1 && doCnt != (totaldo - 1) && !isStartFromDO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (totalin > 1 && inCnt != (totalin - 1) && !isStartFromInvoice) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else if(isStartFromInvoice && totalSr > 0 && inCnt!= (totalSr - 1) ){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (payCnt < difference) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pay + "','" + Constants.Acc_Receive_Payment_ModuleId + "')\">" + pay + "</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + pay + "','" + Constants.Acc_Credit_Note_ModuleId + "',1)\">" + pay + "</td>";
                }
                if (next_data.size() > 0) {
                    Object[] jed = next_data.get(0);   // to access each value in object array JE.get(0)
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " style= \"border-bottom:1px solid black;\">&nbsp;</td>";
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }   
                    jSONObject.put("gl", jed[0]);
                }
                tr += "</tr>";                
                requestParams.clear();
                requestParams.put("companyid", companyid);
                requestParams.put("isFromPayment", true);
                requestParams.put("paymentNumber", pay);

                kwl = accLinkDataDao.getDebitNote(requestParams, params);
                debit_note = kwl.getEntityList();
                
                JSONObject debit_note_json = generate_DN_HTML(params, debit_note, totalcq, cqCnt, totalso, soCnt, totaldo, doCnt, totalin, inCnt, cnCnt, isStartFromInvoice, companyid, isStartFromSO, isStartFromDO, isSR, totalSr, srCnt, difference, payCnt,isStartFromPayment,jSONArray,jSONObject);
                if (debit_note_json.has("html") && !StringUtil.isNullOrEmpty(debit_note_json.getString("html"))) {
                    tr += debit_note_json.getString("html");
                }
                payCnt++;
        }
            data.put("html", tr);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    public JSONObject generate_DN_HTML(HashMap<String, Object> params, List<Object[]> debit_note, int totalcq, int cqCnt, int totalso, int soCnt, int totaldo, int doCnt, int totalin, int inCnt, int cnCnt, boolean isStartFromInvoice, String companyid, boolean isStartFromSO, boolean isStartFromDO,boolean isSR, int totalSr, int srCnt,int totalPayment, int payCnt, boolean isStartFromPayment,JSONArray jSONArray,JSONObject jSONObject) {
        String tr = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwl;
        JSONObject data = new JSONObject();
        List<Object[]> JE = new ArrayList();    // ERP-22014 as JE getting Object array from resp get*JE method of diff types
        int dntype = 1;

        try {
            for (Object[] debitnote : debit_note) {
                String dn = debitnote[0].toString();
                if(debitnote[2] != null && !StringUtil.isNullOrEmpty(debitnote[2].toString())){
                    dntype = (int)debitnote[2];
                }
                if(jSONObject.has(Constants.Debit_Note_Key)){
                    JSONObject copy=new JSONObject(jSONObject.toString());
                    jSONArray.put(copy);
                    jSONObject = removeDuplicateEntries(jSONObject);
                }
                jSONObject.put(Constants.Debit_Note_Key, dn);
                tr += "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                if (totalso > 1 && soCnt != (totalso - 1) && !isStartFromSO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (totaldo > 1 && doCnt != (totaldo - 1) && !isStartFromDO) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if (totalin > 1 && inCnt != (totalin - 1) && !isStartFromInvoice) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else if( totalSr > 0 && !isStartFromInvoice ){
                    if (totalSr == 0) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    } else if (isSR && srCnt == (totalSr - 1)) {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                    } else {
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                    }
                }else {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                if(totalPayment > 1 && payCnt != totalPayment && !isStartFromPayment){
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " style= \"border-left:1px solid black;\">&nbsp;</td>";
                } else{
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                }
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + dn + "','" + Constants.Acc_Debit_Note_ModuleId +"','"+dntype+"')\">" + dn + "</td>";
                
                requestParams.put("companyid", companyid);
                requestParams.put("number", dn);
                kwl = accLinkDataDao.getDebitNoteJE(requestParams, params);
                JE = kwl.getEntityList();
                
                if (JE.size() > 0) {
                    Object[] jed = JE.get(0);   // to access each value in object array JE.get(0)
                    if(!StringUtil.isNullObject(jed)){
                        tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"callJournalEntryDetails('" + jed[1] + "',true,null,null,null,null,'"+ jed[2] +"','"+ jed[2] +"')\">" + jed[0] + "</a></td>";
                    }
                    jSONObject.put("gl", jed[0]);
                }
                tr += "</tr>";
            }
            if (isStartFromPayment) {
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
            }
        data.put("html", tr);
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public List<Object[]> checkDuplicates(List<Object[]> original_Data, List<Object[]> data_To_Add,int index){
        for (Object dataWithinRange : data_To_Add) {
            Object[] data_to_be_added = (Object[]) dataWithinRange;
            boolean ispresent = false;

            if(data_to_be_added[index] != null){
                for (Object originalObj : original_Data) {
                    Object[] real_data = (Object[]) originalObj;
                    if(real_data[index] != null && real_data[index].toString().equals(data_to_be_added[index].toString()) ){
                        ispresent = true;
                        break;
                    }
                }
                if(!ispresent){
                    original_Data.add(data_to_be_added);
                }
            }
        }
        return original_Data;
    }
   
     public KwlReturnObject getLinkedSINo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            requestParams.put("isConsignment", Constants.SQL_FALSE);
            requestParams.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
            requestParams.put("isFixedAssetInvoice", Constants.SQL_FALSE);
            requestParams.put("isDraft", Constants.SQL_FALSE);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

            kwlq= accLinkDataDao.getInvoices(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedDONo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseDO", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            
            kwlq= accLinkDataDao.getDeliveryOrder(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedSONo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            
            requestParams.put("isConsignment", "F");
            requestParams.put("isLeaseSO", 0);
            requestParams.put("isReplacementSo", 0);
            requestParams.put("isFixedAssetDO", 0);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            
            kwlq= accLinkDataDao.getSalesOrder(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedCQNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            kwlq= accLinkDataDao.getCqlinked(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
     
    public KwlReturnObject getLinkedVQNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            kwlq= accLinkDataDao.getVendorQuotation(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedPONo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            kwlq= accLinkDataDao.getPurchaseOrder(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedGRNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            requestParams.put("isStartFromGR", true);
            requestParams.put("invoiceModuleId", requestParams.get("moduleid")!=null?requestParams.get("moduleid").toString():"-1");
            requestParams.put("isConsignment", Constants.SQL_FALSE);
            requestParams.put("isFixedassetGro", Constants.SQL_FALSE);
            requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);
            kwlq= accLinkDataDao.getGoodsReceipt(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    
    public KwlReturnObject getLinkedPINo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try{
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq=null;
            kwlq= accLinkDataDao.getPurchaseInvoice(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size()); 
    }
    public KwlReturnObject getLinkedPRNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            kwlq = accLinkDataDao.getPurchaseRequisition(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }

    public KwlReturnObject getLinkedRFQNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            kwlq = accLinkDataDao.getRFQ(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public KwlReturnObject getLinkedDebitNoteNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            kwlq = accLinkDataDao.getDebitNote(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public KwlReturnObject getLinkedCreditNoteNo(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object[]> list = new ArrayList();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            kwlq = accLinkDataDao.getCreditNote(requestParams, params);
            list = kwlq.getEntityList();
        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }    
    public List<Object[]> removeDuplicateRecords(List<Object[]> original_Data, Set<String> data_To_Remove,int index){
        List<Object[]> returnList = new ArrayList<Object[]>();
        for (Object datatoAdd : original_Data) {
            Object[] data_to_be_added = (Object[]) datatoAdd;
            boolean ispresent = false;

            if(data_to_be_added[index] != null){
                for (String datatoremove : data_To_Remove) {
                    if(data_to_be_added[index].toString().equals(datatoremove) ){
                        ispresent = true;
                        break;
                    }
                }
                if(!ispresent){
                    returnList.add(data_to_be_added);
                }
            }
        }
        return returnList;
    }

    public String getDOLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<Object[]> next_data_within_date_range = new ArrayList();
        Set<String> used_DO = new HashSet();
        Set<String> used_Invoices = new HashSet();
        boolean predecessorFlag = false;
        String tableHTML = "", mainTable = "", tr = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            boolean isCallFromSIorDO= requestParams.get("isCallFromSIorDO") != null ? (Boolean) requestParams.get("isCallFromSIorDO") : false;
            if (documentType==Constants.Acc_Delivery_Order_ModuleId) {
                kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
                      
            if (!isAllowToCheck) {
               
                kwlq = accLinkDataDao.getSOLinkingInfo(requestParams);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    for (Object[] salesOrder : documentList) {
                        String salesOrderNo = salesOrder[1].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", salesOrderNo);
                        duplicateHtml = getSOLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }
                    }

                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                    predecessorFlag = true;

                } else {
                    if (!isCallFromSIorDO) {
                        isCallFromSIorDO = true;
                        kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);
                        documentList = kwlq.getEntityList();
                        if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                            if (!isHtmlAppended) {
                                mainTable = getCommonHtmlTopForSalesSide();
                                isHtmlAppended = true;
                                isFooterHtmlAppended = true;
                            }
                            requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));

                            for (Object[] invoice : documentList) {
                                String invoiceNo = invoice[1].toString();
                                requestParams.put("isHtmlAppended", isHtmlAppended);
                                requestParams.put("documentNo", invoiceNo);
                                requestParams.put("isCallFromSIorDO", isCallFromSIorDO);
                                
                                duplicateHtml = getSILinkingInfo(requestParams,jSONArray);
                                if (!tableHTML.contains(duplicateHtml)) {
                                    tableHTML += duplicateHtml;
                                }
                            }
                            if (isFooterHtmlAppended) {
                                mainTable += tableHTML;
                                mainTable += "</tbody>";
                                mainTable += "</table></div>";
                                tableHTML = mainTable;
                            }
                            predecessorFlag = true;

                        }
                    }
                }
            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForSalesSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }
                requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));

                kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);
                next_data_within_date_range = kwlq.getEntityList();
                if (next_data_within_date_range.size() > 0) {
                    JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, next_data_within_date_range, 0, 0, 0, 0, companyid, false, true, false, used_DO, used_Invoices,jSONArray,jSONObject);
                    if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                        tableHTML += delivery_order_json.getString("html");
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    }
                    if(jSONObject.length()>=1){
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }

                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getSOLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<Object[]> sales_order = new ArrayList();
        Set<String> used_DO = new HashSet();
        Set<String> used_Invoices = new HashSet();
        String tableHTML = "", mainTable = "", tr = "";
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        List<Object[]> documentList = new ArrayList();
        String duplicateHtml = "";
        boolean predecessorFlag = false;
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Sales_Order_ModuleId) {
                kwlq = accLinkDataDao.getSOLinkingInfo(requestParams);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {
                
                kwlq = accLinkDataDao.getCQLinkingInfo(requestParams);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }

                    for (Object[] Quotation : documentList) {
                        String quotationNo = Quotation[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", quotationNo);

                        duplicateHtml = getCQLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                    predecessorFlag = true;
                }
            }

            if (!predecessorFlag) {
                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForSalesSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }
                requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                kwlq = accLinkDataDao.getSOLinkingInfo(requestParams);
                sales_order = kwlq.getEntityList();
                if (sales_order.size() > 0) {
                    JSONObject sales_order_json = generate_Sales_Order_HTML(params, sales_order, 0, 0, companyid, true, used_DO, used_Invoices,jSONArray,jSONObject);
                    if (sales_order_json.has("html") && !StringUtil.isNullOrEmpty(sales_order_json.getString("html"))) {
                        tableHTML += sales_order_json.getString("html");
                    }
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getCQLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<Object[]> customer_quotation = new ArrayList();
        Set<String> used_DO = new HashSet();
        Set<String> used_Invoices = new HashSet();

        String tableHTML = "", mainTable = "", tr = "";
        List<Object[]> next_data = new ArrayList();
        List<Object[]> delivery_order = new ArrayList();
        int htmlCount = 0;
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        HashMap requestParams1 = new HashMap();

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            if (!isHtmlAppended) {
                mainTable = getCommonHtmlTopForSalesSide();
                isHtmlAppended = true;
                isFooterHtmlAppended = true;
            }
            requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
            kwlq = accLinkDataDao.getCQLinkingInfo(requestParams);
            customer_quotation = kwlq.getEntityList();
            for (int cqCnt = 0; cqCnt < customer_quotation.size(); cqCnt++) {
                Object[] cq = customer_quotation.get(cqCnt);
                String cq_number = (String) cq[0];
                int moduleid = (int) cq[1];
                String docId = (String) cq[2];
                jSONObject.put(Constants.Customer_Quatation_Key, cq_number);
                tr = "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + cq_number + "','" + Constants.Acc_Customer_Quotation_ModuleId + "')\">" + cq_number + "</td>";
                tr += "</tr>";

                // If moduleid is sales order create sales order html Tr
                if (Constants.Acc_Sales_Order_ModuleId == moduleid) {
                    htmlCount++;
                    requestParams.put("id", docId);
                    requestParams.put("sourceflag", 1);
                    requestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams.put("companyid", companyid);

                    requestParams.put("isConsignment", "F");
                    requestParams.put("isLeaseSO", 0);
                    requestParams.put("isReplacementSo", 0);
                    requestParams.put("isFixedAssetDO", 0);
                    requestParams.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                    kwlq = accLinkDataDao.getSalesOrder(requestParams, params);
                    next_data = kwlq.getEntityList();

//                    tableHTML += generate_Sales_Order_HTML(params, next_data, customer_quotation.size(), cqCnt, companyid, false);
                    JSONObject sales_order_json = generate_Sales_Order_HTML(params, next_data, customer_quotation.size(), cqCnt, companyid, false, used_DO, used_Invoices,jSONArray,jSONObject);
                    if (sales_order_json.has("poHtml") && !StringUtil.isNullOrEmpty(sales_order_json.getString("poHtml"))) {
                        tr = sales_order_json.getString("poHtml") + tr;
                    }
                    if (sales_order_json.has("html") && !StringUtil.isNullOrEmpty(sales_order_json.getString("html"))) {
                        tr += sales_order_json.getString("html");
                    }
                } else {
                    // If moduleid is invoice create invoice html Tr

                    //requestParams.clear();
                    requestParams1.put("id", docId);
                    requestParams1.put("sourceflag", 1);
                    requestParams1.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    requestParams1.put("companyid", companyid);
                    requestParams1.put("isConsignment", Constants.SQL_FALSE);
                    requestParams1.put("isFixedAssetLeaseInvoice", Constants.SQL_FALSE);
                    requestParams1.put("isFixedAssetInvoice", Constants.SQL_FALSE);
                    requestParams1.put("isDraft", Constants.SQL_FALSE);
                    requestParams1.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                    kwlq = accLinkDataDao.getInvoices(requestParams1, params);
                    next_data = kwlq.getEntityList();

                    for (Object[] obj : next_data) {

                        htmlCount++;
                        // Check whether any DO's created by invoice
                        requestParams1.clear();
                        requestParams1.put("id", obj[0]);
                        requestParams1.put("sourceflag", 1);
                        requestParams1.put("moduleid", Constants.Acc_Invoice_ModuleId);
                        requestParams1.put("companyid", companyid);

                        requestParams1.put("isConsignment", "F");
                        requestParams1.put("isLeaseDO", 0);
                        requestParams1.put("isFixedAssetDO", 0);
                        requestParams1.put("approveStatusLevel", Constants.INVOICEAPPROVED);

                        kwlq = accLinkDataDao.getDeliveryOrder(requestParams1, params);
                        delivery_order = kwlq.getEntityList();
                        if (delivery_order.size() > 0) {
//                            tableHTML += generate_Delivery_Order_HTML(params, delivery_order, customer_quotation.size(), cqCnt, 0, 0, companyid, false, false, true);
                            JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, delivery_order, customer_quotation.size(), cqCnt, 0, 0, companyid, false, false, true, used_DO, used_Invoices,jSONArray,jSONObject);
                            if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                                tr += delivery_order_json.getString("html");
                            }
                        } else {
//                            tableHTML += generate_Customer_Invoice_HTML(params, next_data, 0, 0, 0, 0, 0, 0, true, false, false, companyid, false, false,"");
                            JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, next_data, 0, 0, 0, 0, 0, 0, true, false, false, companyid, false, false, "", used_Invoices,jSONArray,jSONObject);
                            if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                                tr += sales_invoice.getString("html");
                            }
                        }
                    }
                }
                if (jSONObject.length() >= 1) {
                    jSONArray.put(jSONObject);
                }
                jSONObject = new JSONObject();
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tr += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                tableHTML += tr;
                if (htmlCount == 0) {
                    tableHTML = "";
                }
            }

            if (isFooterHtmlAppended) {
                mainTable += tableHTML;
                mainTable += "</tbody>";
                mainTable += "</table></div>";
                tableHTML = mainTable;
            }
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getSILinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<Object[]> next_data_within_date_range = new ArrayList();
        Set<String> used_Invoices = new HashSet();
        boolean predecessorFlag = false;
        String tableHTML = "", mainTable = "", tr = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        Set<String> used_DO = new HashSet();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;

            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
             boolean isCallFromSIorDO= requestParams.get("isCallFromSIorDO") != null ? (Boolean) requestParams.get("isCallFromSIorDO") : false;
            if (documentType==Constants.Acc_Invoice_ModuleId) {
                kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
  
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getSOLinkingInfo(requestParams);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    for (Object[] salesOrder : documentList) {
                        String salesOrderNo = salesOrder[1].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", salesOrderNo);

                        duplicateHtml = getSOLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }
                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                    predecessorFlag = true;
                } else {

                    kwlq = accLinkDataDao.getCQLinkingInfo(requestParams);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));

                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForSalesSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }
                        for (Object[] quotation : documentList) {
                            String quotationNo = quotation[0].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", quotationNo);
                            duplicateHtml = getCQLinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }
                        }
                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }

                        predecessorFlag = true;
                    } else {
                        if (!isCallFromSIorDO) {
                            isCallFromSIorDO = true;
                            kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);
                            documentList = kwlq.getEntityList();
                            if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                                requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));

                                if (!isHtmlAppended) {
                                    mainTable = getCommonHtmlTopForSalesSide();
                                    isHtmlAppended = true;
                                    isFooterHtmlAppended = true;
                                }
                                for (Object[] deliveryOrder : documentList) {
                                    String deliveryOrderNo = deliveryOrder[1].toString();
                                    requestParams.put("isHtmlAppended", isHtmlAppended);
                                    requestParams.put("documentNo", deliveryOrderNo);
                                    requestParams.put("isCallFromSIorDO", isCallFromSIorDO);
                                     
                                    duplicateHtml = getDOLinkingInfo(requestParams,jSONArray);
                                    if (!tableHTML.contains(duplicateHtml)) {
                                        tableHTML += duplicateHtml;
                                    }
                                }
                                if (isFooterHtmlAppended) {
                                    mainTable += tableHTML;
                                    mainTable += "</tbody>";
                                    mainTable += "</table></div>";
                                    tableHTML = mainTable;
                                }
                                predecessorFlag = true;
                            }
                        }

                    }
                }
            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForSalesSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }
               
                HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                List<Object[]> delivery_order = new ArrayList();
                /* If SI->DO then call function DeliveryOrder for HTML */
                if (documentType==Constants.Acc_Delivery_Order_ModuleId) {
                    String reserveddocumentNo = (String) requestParams.get("reserveddocumentNo");
                    requestParams.put("isSiParentOnly", true);
                    kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);
                    next_data_within_date_range = kwlq.getEntityList();

                    for (Object[] deliveryOrder : next_data_within_date_range) {
                        if (reserveddocumentNo.equalsIgnoreCase(deliveryOrder[1].toString())) {
                            delivery_order.add(deliveryOrder);
                        }

                    }
                    next_data_within_date_range=delivery_order;
                } else {
                    requestParams.put("isSiParentOnly", true);
                    kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);
                    next_data_within_date_range = kwlq.getEntityList();
                }
                if (next_data_within_date_range.size() > 0) {
                    JSONObject delivery_order_json = generate_Delivery_Order_HTML(params, next_data_within_date_range, 0, 0, 0, 0, companyid, false, true, false, used_DO, used_Invoices,jSONArray,jSONObject);
                    if (delivery_order_json.has("html") && !StringUtil.isNullOrEmpty(delivery_order_json.getString("html"))) {
                        tableHTML += delivery_order_json.getString("html");
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    }
                } else {
                    String documentNumber=(String) requestParams.get("documentNo");
                    requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                    kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);
                    next_data_within_date_range = kwlq.getEntityList();
                    
                    /*Check here fetched invoice have not any parent if yes then discard those invoices*/
                    List<Object[]> customer_invoice = new ArrayList();
                  
                    requestParams1.put("companyId", companyid);
                    for (Object[] salesInvoice : next_data_within_date_range) {
                        requestParams1.put("invoiceID", salesInvoice[0].toString());
                        kwlq = accLinkDataDao.checkFetchedSIhavePredecessor(requestParams1);
                        if (!(kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) && documentNumber.equalsIgnoreCase(salesInvoice[1].toString())) {
                            customer_invoice.add(salesInvoice);
                        } 

                    }
                    if (next_data_within_date_range.size() > 0) {
                        JSONObject sales_invoice = generate_Customer_Invoice_HTML(params, customer_invoice, 0, 0, 0, 0, 0, 0, false, false, false, companyid, false, false, "", used_Invoices,jSONArray,jSONObject);
                        if (sales_invoice.has("html") && !StringUtil.isNullOrEmpty(sales_invoice.getString("html"))) {
                            tableHTML += sales_invoice.getString("html");
                            tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                            tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        }
                    }
                }
                if (jSONObject.length() >= 1) {
                    jSONArray.put(jSONObject);
                }
                jSONObject = new JSONObject();
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getSRLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<Object[]> next_data_within_date_range = new ArrayList();
        boolean predecessorFlag = false;
        String tableHTML = "", mainTable = "", tr = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Sales_Return_ModuleId) {
                kwlq = accLinkDataDao.getSRLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
            
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getDOLinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }

                    for (Object[] deliveryOrder : documentList) {
                        String deliveryOrderNo = deliveryOrder[1].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", deliveryOrderNo);
                        duplicateHtml = getDOLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }

                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                    predecessorFlag = true;
                } else {
                    kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForSalesSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }

                        for (Object[] invoice : documentList) {
                            String invoiceNo = invoice[1].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", invoiceNo);

                            duplicateHtml = getSILinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }

                        }

                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }
                        predecessorFlag = true;
                    }
                }
            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForSalesSide();
                    isFooterHtmlAppended = true;
                }

                kwlq = accLinkDataDao.getSRLinkingInfo(requestParams, params);
                next_data_within_date_range = kwlq.getEntityList();
                if (next_data_within_date_range.size() > 0) {
                    JSONObject sales_return = generate_Sales_Return_HTML(params, next_data_within_date_range, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, companyid, false, false, false, 0,jSONArray,jSONObject);
                    if (sales_return.has("html") && !StringUtil.isNullOrEmpty(sales_return.getString("html"))) {
                        tableHTML += sales_return.getString("html");
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                    }
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                    }
                    jSONObject = new JSONObject();
                }
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getCNLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        String tableHTML = "", mainTable = "", tr = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            String documentNumber=(String) requestParams.get("documentNo");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Credit_Note_ModuleId) {
                 kwlq = accLinkDataDao.getCNLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {
                kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                   
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }

                    for (Object[] invoice : documentList) {
                        String invoceNo = invoice[1].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", invoceNo);
                        requestParams.put("reserveddocumentNo", documentNumber);
                        
                        duplicateHtml = getSILinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                }else{
                    kwlq = accLinkDataDao.getSRLinkingInfo(requestParams, params);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForSalesSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }

                        for (Object[] salesReturn : documentList) {
                            String srno = salesReturn[0].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", srno);
                            duplicateHtml = getSRLinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }

                        }
                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }

                    }  
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getRPLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        List<String> receipt = new ArrayList();
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        boolean predecessorFlag = false;

        String tableHTML = "", mainTable = "", tr = "";
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            
            
           int documentType=(Integer)requestParams.get("documentType");
            String documentNumber=(String) requestParams.get("documentNo");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Receive_Payment_ModuleId) {
                requestParams.put("isCheckPayment", true);
                kwlq = accLinkDataDao.getRPLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {
                kwlq = accLinkDataDao.getSILinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {

                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForSalesSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    /* If only one level successor Documnet i.e SI->SO it means that SO is not linked further*/
                   
                    for (Object[] invoice : documentList) {
                        String invocieNo = invoice[1].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", invocieNo);
                        requestParams.put("reserveddocumentNo", documentNumber);
                         
                        duplicateHtml = getSILinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                    predecessorFlag = true;

                }

            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForSalesSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }
                requestParams.put("isCheckPayment", false);
                kwlq = accLinkDataDao.getRPLinkingInfo(requestParams, params);
                receipt = kwlq.getEntityList();

                if (receipt.size() > 0) {
                    JSONObject paymentjson = generate_Payment_CN_HTML(params, receipt, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, companyid, false, false, true, 0, 0, true,jSONArray,jSONObject);
                    if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                        tableHTML += paymentjson.getString("html");
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        tableHTML += "<tr> <td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td> </tr>";
                        if (jSONObject.length() >= 1) {
                            jSONArray.put(jSONObject);
                        }
                        jSONObject = new JSONObject();
                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                }

            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getCommonHtmlTopForSalesSide() {

        String mainTable = "";
        mainTable += "<div id=\"linkedsalestable\" style=\"width:100%;overflow: auto; height: 100%;position: absolute;\"><table border=0 style=\"width:100%; border-spacing: 0; border-collapse: collapse; overflow: scroll;\">";
        mainTable += "<tbody>";
        mainTable += "<tr>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH_SALES + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "</tr>";
        return mainTable;
    }

    public String getGRLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        List<Object[]> goods_receipt = new ArrayList();
        boolean predecessorFlag = false;
        String tableHTML = "", mainTable = "";
        List<Object[]> empty_list = new ArrayList();
        List<String> empty_list_RFQ = new ArrayList();
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        JSONObject jSONObject=new JSONObject();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            boolean isCallFromPIorGR= requestParams.get("isCallFromPIorGR") != null ? (Boolean) requestParams.get("isCallFromPIorGR") : false;
            
            if (documentType==Constants.Acc_Goods_Receipt_ModuleId) {
                kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
         
            if (!isAllowToCheck) {
               
                kwlq = accLinkDataDao.getPOLinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    for (Object[] purchaseOrder : documentList) {
                        String pono = purchaseOrder[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", pono);
                        duplicateHtml = getPOLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }
                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                    predecessorFlag = true;

                } else {
                    if (!isCallFromPIorGR) {
                        isCallFromPIorGR = true;
                        kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                        documentList = kwlq.getEntityList();
                        if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                            if (!isHtmlAppended) {
                                mainTable = getCommonHtmlTopForPurchaseSide();
                                isHtmlAppended = true;
                                isFooterHtmlAppended = true;
                            }

                            requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                            for (Object[] purchaseInvoice : documentList) {
                                String invoiceNo = purchaseInvoice[0].toString();
                                requestParams.put("isHtmlAppended", isHtmlAppended);
                                requestParams.put("documentNo", invoiceNo);
                                 requestParams.put("isCallFromPIorGR", isCallFromPIorGR);

                                duplicateHtml = getPILinkingInfo(requestParams,jSONArray);
                                if (!tableHTML.contains(duplicateHtml)) {
                                    tableHTML += duplicateHtml;
                                }
                            }
                            if (isFooterHtmlAppended) {
                                mainTable += tableHTML;
                                mainTable += "</tbody>";
                                mainTable += "</table></div>";
                                tableHTML = mainTable;
                            }

                            predecessorFlag = true;

                        }
                    }

                }
            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForPurchaseSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }
                requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);
                goods_receipt = kwlq.getEntityList();
                if (goods_receipt.size() > 0) {
                    JSONObject goods_receipt_json = generate_goods_receipt_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, goods_receipt, companyid, "", "", false, false, params, false, true,jSONObject,jSONArray);
                    if (goods_receipt_json.has("html") && !StringUtil.isNullOrEmpty(goods_receipt_json.getString("html"))) {
                        tableHTML += goods_receipt_json.getString("html");
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        if (jSONObject.length() >= 1) {
                            jSONArray.put(jSONObject);
                    }
                        jSONObject = new JSONObject();
                }
                }

                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getPOLinkingInfo(HashMap<String, Object> requestParams, JSONArray jSONArray) throws ServiceException {

        List<Object[]> purchase_order = new ArrayList();
        String tableHTML = "", mainTable = "";
        List<Object[]> empty_list = new ArrayList();
        List<String> empty_list_RFQ = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        List<Object[]> documentList = new ArrayList();
        String duplicateHtml = "";
        boolean predecessorFlag = false;
        JSONObject jSONObject= new JSONObject();
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Purchase_Order_ModuleId) {
                kwlq = accLinkDataDao.getPOLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
            
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getVQLinkingInfo(requestParams);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    for (Object[] vendorQuotation : documentList) {
                        String quotationNo = vendorQuotation[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", quotationNo);

                        duplicateHtml = getVQLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                    predecessorFlag = true;
                }
            }

            if (!predecessorFlag) {
                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForPurchaseSide();
                    isHtmlAppended = true;
                    isFooterHtmlAppended = true;
                }

                /* putting immediate successor document no if it is not further linked*/
                requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                kwlq = accLinkDataDao.getPOLinkingInfo(requestParams, params);
                purchase_order = kwlq.getEntityList();
                if (purchase_order.size() > 0) {
                    JSONObject purchase_order_json = generate_purchase_order_HTML(empty_list_RFQ, 0, empty_list, 0, purchase_order, companyid, "", false, params, true, jSONObject,jSONArray);
                    if (purchase_order_json.has("html") && !StringUtil.isNullOrEmpty(purchase_order_json.getString("html"))) {
                        tableHTML += purchase_order_json.getString("html");
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        if (jSONObject.length() >= 1) {
                            jSONArray.put(jSONObject);
                    }
                        jSONObject = new JSONObject();
                }
                }
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }

            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getPILinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        List<Object[]> purchase_invoice = new ArrayList();
        boolean predecessorFlag = false;
        String tableHTML = "", mainTable = "";
        List<Object[]> empty_list = new ArrayList();
        List<String> empty_list_RFQ = new ArrayList();
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        List<Object[]> goods_receipt = new ArrayList();
        JSONObject jSONObject= new JSONObject();
                
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            int documentType=(Integer)requestParams.get("documentType");
            boolean isCallFromPIorGR= requestParams.get("isCallFromPIorGR") != null ? (Boolean) requestParams.get("isCallFromPIorGR") : false;
            
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Vendor_Invoice_ModuleId) {
                kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                purchase_invoice = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
         
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getPOLinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    for (Object[] purchaseOrder : documentList) {
                        String poNo = purchaseOrder[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", poNo);

                        duplicateHtml = getPOLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }
                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                    predecessorFlag = true;
                } else {

                    kwlq = accLinkDataDao.getVQLinkingInfo(requestParams);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));

                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForPurchaseSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }
                        for (Object[] vendorQuotation : documentList) {
                            String vendorQuotationNo = vendorQuotation[0].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", vendorQuotationNo);
                            duplicateHtml = getVQLinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }
                        }
                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }

                        predecessorFlag = true;
                    } else {
                        if (!isCallFromPIorGR) {
                            isCallFromPIorGR = true;
                            kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);
                            documentList = kwlq.getEntityList();
                            if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                                requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));

                                if (!isHtmlAppended) {
                                    mainTable = getCommonHtmlTopForPurchaseSide();
                                    isHtmlAppended = true;
                                    isFooterHtmlAppended = true;
                                }
                                for (Object[] goodsReceipt : documentList) {
                                    String goodsReceiptNo = goodsReceipt[0].toString();
                                    requestParams.put("isHtmlAppended", isHtmlAppended);
                                    requestParams.put("documentNo", goodsReceiptNo);
                                    requestParams.put("isCallFromPIorGR", isCallFromPIorGR);
                                    
                                    duplicateHtml = getGRLinkingInfo(requestParams,jSONArray);
                                    if (!tableHTML.contains(duplicateHtml)) {
                                        tableHTML += duplicateHtml;
                                    }
                                }
                                if (isFooterHtmlAppended) {
                                    mainTable += tableHTML;
                                    mainTable += "</tbody>";
                                    mainTable += "</table></div>";
                                    tableHTML = mainTable;
                                }
                                predecessorFlag = true;
                            }
                        }

                    }
                }
            }
           
            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForPurchaseSide();
                    isFooterHtmlAppended = true;
                }
                /* If PI->GR then call function GoodsReceipt for HTML */
                List<Object[]> delivery_order = new ArrayList();
                if (documentType==Constants.Acc_Goods_Receipt_ModuleId) {
                    String reserveddocumentNo = (String) requestParams.get("reserveddocumentNo");

                    requestParams.put("isOnlyPiParent", true);
                    kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);
                    goods_receipt = kwlq.getEntityList();
                    for (Object[] deliveryOrder : goods_receipt) {
                        if (reserveddocumentNo.equalsIgnoreCase(deliveryOrder[0].toString())) {
                            delivery_order.add(deliveryOrder);
                        }

                    }
                    goods_receipt = delivery_order;
                } else {
                    requestParams.put("isOnlyPiParent", true);
                    kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);
                    goods_receipt = kwlq.getEntityList();
                }
           
                if (goods_receipt.size() > 0) {
                    JSONObject goods_receipt_json = generate_goods_receipt_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, goods_receipt, companyid, "", "", false, false, params, false, true,jSONObject,jSONArray);
                    if (goods_receipt_json.has("html") && !StringUtil.isNullOrEmpty(goods_receipt_json.getString("html"))) {
                        tableHTML += goods_receipt_json.getString("html");
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                    }
                } else {

                    requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                    kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                    /* this is temporary solution */
                    purchase_invoice = kwlq.getEntityList();
//                    Object[] pi = purchase_invoice.get(2);
//                    purchase_invoice.clear();
//                    purchase_invoice.add(pi);
                    /*Check here fetched invoice have not any parent if yes then discard those invoices*/
                    List<Object[]> vendor_invoice = new ArrayList();
                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("companyId", companyid);
                    for (Object[] purchaseInvoice : purchase_invoice) {
                        requestParams1.put("invoiceID", purchaseInvoice[1].toString());
                        kwlq = accLinkDataDao.checkFetchedPIhavePredecessor(requestParams1);
                        if (!(kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0)) {
                            vendor_invoice.add(purchaseInvoice);
                        } else {

                        }

                    }
                    if (purchase_invoice.size() > 0) {
                        JSONObject invoice_json = generate_purchase_invoice_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, empty_list, 0, vendor_invoice, companyid, false, params, false, true, false, "",jSONObject,jSONArray);
                        if (invoice_json.has("html") && !StringUtil.isNullOrEmpty(invoice_json.getString("html"))) {
                            tableHTML += invoice_json.getString("html");
                            tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                            tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                            if (jSONObject.length() >= 1) {
                                jSONArray.put(jSONObject);
                        }
                            jSONObject = new JSONObject();
                    }
                    }

                }

                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getPRLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        String tableHTML = "", mainTable = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            
           int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Purchase_Return_ModuleId) {

                kwlq = accLinkDataDao.getPRLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getGRLinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }

                    for (Object[] goodsreceipt : documentList) {
                        String goodsReceiptNo = goodsreceipt[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", goodsReceiptNo);
                        duplicateHtml = getGRLinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }

                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                } else {
                    kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForPurchaseSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }
                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                        for (Object[] purchaseInvoice : documentList) {
                            String invoiceNo = purchaseInvoice[0].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", invoiceNo);
                            duplicateHtml = getPILinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }

                        }

                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }

                    }
                }
            }


        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getVQLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        String tableHTML = "", mainTable = "", tr = "";
        List<String> empty_list_RFQ = new ArrayList();
        List<Object[]> vendor_quotation = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        List<Object[]> documentList = new ArrayList();
        boolean predecessorFlag=false;
        JSONObject jSONObject = new JSONObject();

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            int documentType=(Integer)requestParams.get("documentType");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Vendor_Quotation_ModuleId) {
                kwlq = accLinkDataDao.getVQLinkingInfo(requestParams);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {
           kwlq = accLinkDataDao.getRequisitionLinkingInfo(requestParams);
            if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    tableHTML = getRequisitionLinkingInfo(requestParams,jSONArray);
                    predecessorFlag = true;
            }
            }
            if (!predecessorFlag) {
                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForPurchaseSide();
                    isFooterHtmlAppended = true;
                }
                requestParams.put("documentNo", requestParams.get("reserveddocumentNo") != null ? requestParams.get("reserveddocumentNo") : requestParams.get("documentNo"));
                kwlq = accLinkDataDao.getVQLinkingInfo(requestParams);
                vendor_quotation = kwlq.getEntityList();
                if (vendor_quotation.size() > 0) {
                    JSONObject vq_json = generate_vendor_quotation_HTML(empty_list_RFQ, 0, vendor_quotation, companyid, true, params,jSONObject,jSONArray);
                    if (vq_json.has("html") && !StringUtil.isNullOrEmpty(vq_json.getString("html"))) {
                        tableHTML += vq_json.getString("html");
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        tableHTML += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                        if (jSONObject.length() >= 1) {
                            jSONArray.put(jSONObject);
                    }
                        jSONObject = new JSONObject();
                }
                }
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getDNLinkingInfo(HashMap<String, Object> requestParams, JSONArray jSONArray) throws ServiceException {

        String tableHTML = "", mainTable = "";
        List<Object[]> documentList = new ArrayList();
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            String companyid = (String) requestParams.get("companyId");
            requestParams.put("companyid", companyid);

            int documentType=(Integer)requestParams.get("documentType");
            String documentNumber=(String) requestParams.get("documentNo");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Debit_Note_ModuleId) {
                kwlq = accLinkDataDao.getDNLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
                      
            if (!isAllowToCheck) {
               
                kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    
                    for (Object[] purchaseInvoice : documentList) {
                        String invoceNo = purchaseInvoice[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", invoceNo);
                        requestParams.put("reserveddocumentNo", documentNumber);

                        duplicateHtml = getPILinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }

                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }

                } else {
                    kwlq = accLinkDataDao.getPRLinkingInfo(requestParams, params);
                    documentList = kwlq.getEntityList();
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                        if (!isHtmlAppended) {
                            mainTable = getCommonHtmlTopForPurchaseSide();
                            isHtmlAppended = true;
                            isFooterHtmlAppended = true;
                        }
                        requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                        for (Object[] purchaseReturn : documentList) {
                            String returnNo = purchaseReturn[0].toString();
                            requestParams.put("isHtmlAppended", isHtmlAppended);
                            requestParams.put("documentNo", returnNo);

                            duplicateHtml = getPRLinkingInfo(requestParams,jSONArray);
                            if (!tableHTML.contains(duplicateHtml)) {
                                tableHTML += duplicateHtml;
                            }

                        }
                        if (isFooterHtmlAppended) {
                            mainTable += tableHTML;
                            mainTable += "</tbody>";
                            mainTable += "</table></div>";
                            tableHTML = mainTable;
                        }

                    }
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getMPLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {
        List<String> payment = new ArrayList();
        List<Object[]> empty_list = new ArrayList();
        List<String> empty_list_RFQ = new ArrayList();
        List<Object[]> documentList = new ArrayList();
      String tableHTML = "", mainTable = "";
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;
        String duplicateHtml = "";
        boolean predecessorFlag = false;
        JSONObject jSONObject = new JSONObject();

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;
            
             int documentType=(Integer)requestParams.get("documentType");
            String documentNumber=(String) requestParams.get("documentNo");
            boolean isAllowToCheck = false;
            if (documentType==Constants.Acc_Make_Payment_ModuleId) {
                requestParams.put("isCheckPayment", true);
                kwlq = accLinkDataDao.getMPLinkingInfo(requestParams, params);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    isAllowToCheck = false;
                } else {
                    isAllowToCheck = true;
                }
            }
            
           
            if (!isAllowToCheck) {

                kwlq = accLinkDataDao.getPILinkingInfo(requestParams, params);
                documentList = kwlq.getEntityList();
                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    if (!isHtmlAppended) {
                        mainTable = getCommonHtmlTopForPurchaseSide();
                        isHtmlAppended = true;
                        isFooterHtmlAppended = true;
                    }
                    requestParams.put("reserveddocumentNo", requestParams.get("documentNo"));
                    for (Object[] purchaseInvoice : documentList) {
                        String invocieNo = purchaseInvoice[0].toString();
                        requestParams.put("isHtmlAppended", isHtmlAppended);
                        requestParams.put("documentNo", invocieNo);
                        requestParams.put("reserveddocumentNo", documentNumber);
                
                        duplicateHtml = getPILinkingInfo(requestParams,jSONArray);
                        if (!tableHTML.contains(duplicateHtml)) {
                            tableHTML += duplicateHtml;
                        }
               
                    }
                    if (isFooterHtmlAppended) {
                        mainTable += tableHTML;
                        mainTable += "</tbody>";
                        mainTable += "</table></div>";
                        tableHTML = mainTable;
                    }
                    predecessorFlag = true;

                }
            }

            if (!predecessorFlag) {

                if (!isHtmlAppended) {
                    mainTable = getCommonHtmlTopForPurchaseSide();
                    isFooterHtmlAppended = true;
                }
                 requestParams.put("isCheckPayment", false);
                kwlq = accLinkDataDao.getMPLinkingInfo(requestParams, params);
                payment = kwlq.getEntityList();

                JSONObject paymentjson = generate_payment_HTML(empty_list_RFQ, 0, empty_list, 0, empty_list, 0, empty_list, 0, empty_list, 0, payment, companyid, empty_list, false, params, false, false, false, false, 0, 0, true,jSONObject,jSONArray);
                if (paymentjson.has("html") && !StringUtil.isNullOrEmpty(paymentjson.getString("html"))) {
                    tableHTML += paymentjson.getString("html");
                    if (jSONObject.length() >= 1) {
                        jSONArray.put(jSONObject);
                }
                    jSONObject = new JSONObject();
                }
                if (isFooterHtmlAppended) {
                    mainTable += tableHTML;
                    mainTable += "</tbody>";
                    mainTable += "</table></div>";
                    tableHTML = mainTable;
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getRequisitionLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        List<String> purchase_requisition = new ArrayList();

        String tableHTML = "", mainTable = "", tr = "";
        boolean isHtmlAppended = false;
        boolean isFooterHtmlAppended = false;

        JSONObject jSONObject= new JSONObject();

        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            KwlReturnObject kwlq = null;
            String companyid = (String) requestParams.get("companyId");
            isHtmlAppended = requestParams.get("isHtmlAppended") != null ? (Boolean) requestParams.get("isHtmlAppended") : false;

            if (!isHtmlAppended) {
                mainTable = getCommonHtmlTopForPurchaseSide();
                isHtmlAppended = true;
                isFooterHtmlAppended = true;
            }

            kwlq = accLinkDataDao.getRequisitionLinkingInfo(requestParams);
            purchase_requisition = kwlq.getEntityList();

            for (String prq : purchase_requisition) {

                tr = "<tr>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
                tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-left:1px solid black;border-bottom:1px solid black;\"><a class = \"jumplink\" href=\"#\" onclick=\"WtfGlobal.callViewMode('" + prq + "','" + Constants.Acc_Purchase_Requisition_ModuleId + "')\">" + prq + "</a></td>";

                jSONObject=new JSONObject();
                jSONObject.put(Constants.Purchase_Requisition_Key, prq);
                
                List<String> RFQ = new ArrayList();
                List<String> purchaseorder = new ArrayList();
                HashMap<String, Object> RFQrequestParams = new HashMap<String, Object>();

                RFQrequestParams.put("companyid", companyid);
                RFQrequestParams.put("number", prq);

                KwlReturnObject kwlrfq = accLinkDataDao.getRFQ(RFQrequestParams, params);
                RFQ = kwlrfq.getEntityList();
                
                RFQrequestParams.put("isFromPR", true);
                RFQrequestParams.put("RequisitionNumber", prq);
                KwlReturnObject kwlvq = accLinkDataDao.getPurchaseOrder(RFQrequestParams, params);
                purchaseorder = kwlvq.getEntityList();
                
                if (RFQ.size() == 0) {
                    tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"></td>";
                }
                if(purchaseorder.size()!=0){
                 tr += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + " align=\"center\" style= \"border-bottom:1px solid black;\"></td>";   
                }
                tr += "</tr>";
                JSONObject rfq_json = generate_request_for_quotation_HTML(RFQ, prq, companyid, params,jSONObject,jSONArray);
                if (rfq_json.has("html") && !StringUtil.isNullOrEmpty(rfq_json.getString("html"))) {
                    tr += rfq_json.getString("html");
                }
                if (rfq_json.has("soHtml") && !StringUtil.isNullOrEmpty(rfq_json.getString("soHtml"))) {
                    tr = rfq_json.getString("soHtml") + tr;
                }
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tr += "<tr><td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td></tr>";
                tableHTML += tr;
                
                
                
                if (jSONObject.length() >= 1) {
                    jSONArray.put(jSONObject);
            }
                jSONObject = new JSONObject();
            }
            if (isFooterHtmlAppended) {
                mainTable += tableHTML;
                mainTable += "</tbody>";
                mainTable += "</table></div>";
                tableHTML = mainTable;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }
    
        public String getRFQLinkingInfo(HashMap<String, Object> requestParams,JSONArray jSONArray) throws ServiceException {

        String tableHTML = "";

        try {
            KwlReturnObject kwlq = null;
            HashMap<String, Object> params = new HashMap<String, Object>();
            String companyid = (String) requestParams.get("companyId");
            requestParams.put("companyid", companyid);

            kwlq = accLinkDataDao.getRFQLinkingInfo(requestParams, params);
            if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                kwlq = accLinkDataDao.getRequisitionLinkingInfo(requestParams);

                if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                    if (kwlq != null && kwlq.getEntityList() != null && kwlq.getEntityList().size() > 0) {
                        tableHTML = getRequisitionLinkingInfo(requestParams,jSONArray);
                    }

                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(LinkInformationHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return tableHTML;
    }

    public String getCommonHtmlTopForPurchaseSide() {

        String mainTable = "";
        mainTable += "<div id=\"linkedpurchasetable\" style=\"width:100%;overflow: auto; height: 100%;position: absolute;\"><table border=0 style=\"width:100%; border-spacing: 0; border-collapse: collapse; overflow: scroll;\">";
        mainTable += "<tbody>";

        mainTable += "<tr>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "<td width=" + Constants.LINK_TABLE_TD_WIDTH + " height=" + Constants.LINK_TABLE_TD + ">&nbsp;</td>";
        mainTable += "</tr>";
        return mainTable;
    }
}
