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
package com.krawler.documentdesigner;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.apache.velocity.app.VelocityEngine;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.spring.accounting.account.accAccountControllerCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.purchaseorder.AccPurchaseOrderServiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderControllerCMN;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceDAO;
import com.krawler.spring.accounting.vendorpayment.AccVendorPaymentServiceImpl;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author krawler
 */
public class AccDocumentDesignServiceImpl implements AccDocumentDesignService {

    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accInvoiceCMN accInvoiceCommon;
    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    public ImportHandler importHandler;
    private accAccountControllerCMN accAccountCMNObj;
    private MessageSource messageSource;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private AccReceiptServiceDAO accReceiptServiceDAOobj;
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;
    private accPurchaseOrderControllerCMN accPurchaseOrderControllerCMN;

    public void setAccPurchaseOrderControllerCMN(accPurchaseOrderControllerCMN accPurchaseOrderControllerCMN) {
        this.accPurchaseOrderControllerCMN = accPurchaseOrderControllerCMN;
    }

   public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAOobj) {
        this.accReceiptServiceDAOobj = accReceiptServiceDAOobj;
    }
    
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    public void setaccVendorPaymentServiceDAO(AccVendorPaymentServiceDAO accVendorPaymentServiceDAOobj) {
        this.accVendorPaymentServiceDAOobj = accVendorPaymentServiceDAOobj;
    }
    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccGoodsReceiptServiceDAOObj(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAOObj) {
        this.accGoodsReceiptServiceDAOObj = accGoodsReceiptServiceDAOObj;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setAccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setCustomDesignDAOObj(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    
    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccAccountCMNObj(accAccountControllerCMN accAccountCMNObj) {
        this.accAccountCMNObj = accAccountCMNObj;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setaccPurchaseOrderServiceDAO(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }
    
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }
        
    @Override
    @Deprecated
    public String getHTMLContentForEmailWithDDTemplate(JSONObject requestJobj) {
        String mainHtml = "", buildHtml = "", pageheaderhtml = "", pagefooterhtml = "";

        JSONObject jobj = new JSONObject();
        try {

            jobj = getDocumentDesignerEmailTemplateJson(requestJobj);
            if (jobj.has("buildHtml") && !StringUtil.isNullOrEmpty(jobj.getString("buildHtml"))) {
                buildHtml = jobj.getString("buildHtml");
            }
            if (jobj.has("pageheaderhtml") && !StringUtil.isNullOrEmpty(jobj.getString("pageheaderhtml"))) {
                pageheaderhtml = jobj.getString("pageheaderhtml");
            }
            if (jobj.has("pagefooterhtml") && !StringUtil.isNullOrEmpty(jobj.getString("pagefooterhtml"))) {
                pagefooterhtml = jobj.getString("pagefooterhtml");
            }

            // Replace the columns by Table td
            buildHtml = placeColumnsInTable(buildHtml);
            
            Document jsoupHeaderDoc = Jsoup.parse( pageheaderhtml);
            Elements headers = jsoupHeaderDoc.getElementsByTag("body");
            
            if(headers != null && !headers.isEmpty()){
                Element header = headers.first();
                pageheaderhtml = placeColumnsInTableHeaderFooter(header.html());
            }
            
            Document jsoupFooterDoc = Jsoup.parse( pagefooterhtml);
            Elements footers = jsoupFooterDoc.getElementsByTag("body");
            if(footers != null && !footers.isEmpty()){
                Element footer = footers.first();
                pagefooterhtml = placeColumnsInTableHeaderFooter(footer.html());
            }
            mainHtml = pageheaderhtml + buildHtml + pagefooterhtml;
            mainHtml = mainHtml.replaceAll("overflow:auto", "overflow:hidden");
            mainHtml = mainHtml.replaceAll("overflow :auto", "overflow:hidden");
            mainHtml = mainHtml.replaceAll("overflow: auto", "overflow:hidden");
            mainHtml = mainHtml.replaceAll("overflow : auto", "overflow:hidden");
        } catch (Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mainHtml;
    }

    @Override
    public JSONObject getDocumentDesignerEmailTemplateJson(JSONObject requestJobj) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> otherconfigrequestParams = new HashMap();
            String invoiceID = requestJobj.optString("bills", "");
            String companyid = requestJobj.optString(Constants.companyKey, "");
            boolean isConsignment = requestJobj.optBoolean(Constants.isConsignment, false);
            int moduleid = requestJobj.optInt(Constants.moduleid, 0);

            ArrayList<String> invoiceIDList = CustomDesignHandler.getSelectedBillIDs(invoiceID);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            replaceFieldMap = new HashMap<String, String>();
            /*
             * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
             */
            fieldrequestParams.clear();
            HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
            HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, dimensionFieldMap);

            fieldrequestParams.clear();
            HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
            HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, customfieldFieldMap);

            //For product custom field
            fieldrequestParams.clear();
            HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, productCustomfieldFieldMap);


            HashMap<String, JSONArray> itemDataAgainstInvoice = new HashMap<String, JSONArray>();
            
            HashMap<String, Object> paramMap = new HashMap();
            paramMap.put(Constants.fieldMap, FieldMap);
            paramMap.put(Constants.replaceFieldMap, replaceFieldMap);
            paramMap.put(Constants.dimensionFieldMap, DimensionFieldMap);
            paramMap.put(Constants.lineLevelCustomFieldMap, LineLevelCustomFieldMap);
            paramMap.put(Constants.productLevelCustomFieldMap, ProductLevelCustomFieldMap);
            
            for (int count = 0; count < invoiceIDList.size(); count++) {
                JSONArray lineItemsArr = new JSONArray();
                if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    lineItemsArr = accInvoiceCommon.getInvoiceDetailsItemJSON(requestJobj, invoiceIDList.get(count), paramMap);
                } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                    lineItemsArr = accInvoiceCommon.getCustomerQuotationDetailsItemJSON(requestJobj, invoiceIDList.get(count), paramMap);
                } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    lineItemsArr =accPurchaseOrderServiceDAOobj.getPODetailsItemJSON(requestJobj, invoiceIDList.get(count), paramMap);
                } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                    lineItemsArr =accGoodsReceiptCommon.getGoodsReceiptDetailsItemJSON(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    lineItemsArr =accSalesOrderServiceDAOobj.getSODetailsItemJSON(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    lineItemsArr =accInvoiceCommon.getDODetailsItemJSON(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                    lineItemsArr =accGoodsReceiptCommon.getGRODetailsItemJSON(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    lineItemsArr =accInvoiceCommon.getSalesReturnDetailsItemJSON(requestJobj, invoiceID, moduleid);
                } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    lineItemsArr =accGoodsReceiptCommon.getPurchaseReturnDetailsItemJSON(requestJobj, invoiceID, moduleid);
                } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                    lineItemsArr =accGoodsReceiptCommon.getVendorQuotationDetailsItemJSON(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    lineItemsArr =accVendorPaymentServiceDAOobj.getMPDetailsItemJSONNew(requestJobj, invoiceID, paramMap);
                } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                    lineItemsArr =accReceiptServiceDAOobj.getRPDetailsItemJSONNew(requestJobj, invoiceID, paramMap);
                } else if(moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                    lineItemsArr = accPurchaseOrderControllerCMN.getPRDetailsItemJSON(requestJobj, invoiceID, paramMap);
                }
                itemDataAgainstInvoice.put(invoiceIDList.get(count), lineItemsArr);
            }
            otherconfigrequestParams.put(Constants.moduleid, moduleid);
            otherconfigrequestParams.put(Constants.isConsignment, isConsignment);

            jobj = ExportRecordHandler.exportEmailWithTemplate(requestJobj, itemDataAgainstInvoice, otherconfigrequestParams,
                    accountingHandlerDAOobj, accCommonTablesDAO, customDesignDAOObj, accAccountDAOobj, accountingHandlerDAOobj, velocityEngine, accInvoiceServiceDAO, accGoodsReceiptServiceDAOObj);

        } catch (Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
  
   @Override 
    public String PrintTemplateRestUrl(JSONObject paramJobj) throws JSONException {
        JSONObject userData = new JSONObject();
        StringBuilder appendString=new StringBuilder();
        String endpoint="";
        try {

            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
            String templateid = paramJobj.optString("templateid");
            String billid = paramJobj.optString(Constants.billid);
            String accRestURL = URLUtil.buildRestURL("accURL");
            
            userData.put("companyid", paramJobj.optString(Constants.companyKey));
            userData.put("bills", billid);
            userData.put("recordids", billid);
            userData.put("isConsignment", paramJobj.optString(Constants.isConsignment, "false"));
            userData.put("isLetterHead", paramJobj.optString("isLetterHead", "false"));
            userData.put("moduleid", moduleid);
            userData.put("templateid", templateid);
            userData.put("filetype", "print");
            userData.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap,false) );
            userData.put("filename", paramJobj.optString("billno"));
            userData.put(Constants.userid, paramJobj.optString(Constants.userid,""));
            userData.put(Constants.useridKey, paramJobj.optString(Constants.userid,""));
            endpoint = accRestURL + CustomDesignerConstants.documentDesignerprintTemplateUrl;
           appendString.append(endpoint+userData);
        } catch (JSONException ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return appendString.toString();
    }
   
   public String placeColumnsInTable(String html){
       org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<table id='replacedtable'>" + html + "</table>");
       Elements rows = jsoupDoc.getElementsByClass("sectionclass_parent");
        
       for (Element row : rows) {
           Elements columns = row.getElementsByClass("sectionclass_element");
           Elements row_innercts = row.select("div > span > div");
           Element row_innerct = row_innercts.first();
           if(row_innerct != null){
                String table = "<table> <tr>";
                for (Element column : columns) {
                    table += " <td>" + column.outerHtml().toString() + "</td>";
                    column.remove();
                }
                table += "</tr> </table>";
                row_innerct.html(table);
           }
       }
       html = jsoupDoc.getElementById("replacedtable").html();
       return html;
   }
   public String placeColumnsInTableHeaderFooter(String html){
       org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
       Elements rows = jsoupDoc.getElementsByClass("sectionclass_parent");
        
       for (Element row : rows) {
           Elements columns = row.getElementsByClass("sectionclass_element");
           Elements row_innercts = row.select("div > span > div");
           Element row_innerct = row_innercts.first();
           String table = "<table> <tr>";
           for (Element column : columns) {
               table += " <td>" + column.outerHtml().toString() + "</td>";
               column.remove();
           }
           table += "</tr> </table>";
           row_innerct.html(table);
       }
       html = jsoupDoc.html();
       html = html.replace("<html>", "");
       html = html.replace("<head>", "");
       html = html.replace("<body>", "");
       html = html.replace("</html>", "");
       html = html.replace("</head>", "");
       html = html.replace("</body>", "");
       return html;
   }
    

 @Override  
     public JSONObject ImportCustomTemplates(JSONObject paramJobj, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = null;
        boolean commitedEx = false;
        boolean issuccess = false;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = paramJobj.getString(Constants.companyKey);
        String userId = paramJobj.getString(Constants.useridKey);
        String filePath = jobj.getString("filename");
        int fileIndex = filePath.lastIndexOf("/");
        String fileName = filePath.substring(fileIndex+1);

        JSONObject returnObj = new JSONObject();

        try {
            def.setName("import_Tx");
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            status = txnManager.getTransaction(def);

            fileInputStream = new FileInputStream(filePath);
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cont = 0;
            
            StringBuilder failedRecords = new StringBuilder();
            
            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            JSONArray dataArr = jobj.getJSONArray("data");
            for (int count = 0 ; count < dataArr.length(); count++) {
              if (count != 0) {
                    String[] recarr = record.split(",");
                    JSONObject dataObj = dataArr.getJSONObject(count);
                    try {
                                     
                           String templatename = dataObj.optString("A","");
                            if (!StringUtil.isNullOrEmpty(templatename)) {
                                templatename = templatename.replaceAll("\"", "");
                                templatename=templatename.trim();
                            } else {
                                throw new AccountingException("Template Name is not Available");
                            }
                            String moduleid = dataObj.optString("B","");
                            if (!StringUtil.isNullOrEmpty(moduleid)) {
                                moduleid = moduleid.replaceAll("\"", "");
                                moduleid = moduleid.trim();
                            } else {
                                throw new AccountingException("Module ID is not Available");
                            }
                            String json = dataObj.optString("C","");
                            if (!StringUtil.isNullOrEmpty(json)) {
                                json = json.trim();
                            } else {
                                throw new AccountingException("JSON  is not Available");
                            }
                            String pagelayoutproperty = dataObj.optString("D","");
                            if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
                                pagelayoutproperty = pagelayoutproperty.trim();
                            } 
                            String footerjson = dataObj.optString("E","");
                            if (!StringUtil.isNullOrEmpty(footerjson)) {
                                footerjson = footerjson.trim();
                            } 
                            String headerjson = dataObj.optString("F","");
                            if (!StringUtil.isNullOrEmpty(headerjson)) {
                                headerjson = headerjson.trim();
                            }
                            String html = dataObj.optString("G","");
                            if (!StringUtil.isNullOrEmpty(html)) {
                                html = html.trim();
                            }
                            String headerhtml = dataObj.optString("H","");
                            if (!StringUtil.isNullOrEmpty(headerhtml)) {
                                headerhtml = headerhtml.trim();
                            }
                            String footerhtml = dataObj.optString("I","");
                            if (!StringUtil.isNullOrEmpty(footerhtml)) {
                                footerhtml = footerhtml.trim();
                            }
                            String sqlquery = dataObj.optString("J","");
                            if (!StringUtil.isNullOrEmpty(sqlquery)) {
                                sqlquery = sqlquery.replaceAll("\"", "");
                                sqlquery = sqlquery.trim();
                            }
                            String headersqlquery = dataObj.optString("K","");
                            if (!StringUtil.isNullOrEmpty(headersqlquery)) {
                                headersqlquery = headersqlquery.replaceAll("\"", "");
                                headersqlquery = headersqlquery.trim();
                            }
                            String footersqlquery = dataObj.optString("L","");
                            if (!StringUtil.isNullOrEmpty(footersqlquery)) {
                                footersqlquery = footersqlquery.replaceAll("\"", "");
                                footersqlquery = footersqlquery.trim();
                            }
                            String imageData = dataObj.optString("N","");
                            if (!StringUtil.isNullOrEmpty(imageData)) {
                                imageData = imageData.trim();
                            }
                            String templatesubtype = dataObj.optString("O", "");  //Template subtype is stored at position of 'o'
                            if (!StringUtil.isNullOrEmpty(templatesubtype)) {
                                 templatesubtype = templatesubtype.trim();
                            } else {
                                 templatesubtype = "0";                         //default template subtype.
                            }
                            JSONObject resultJson = new JSONObject();
                            KwlReturnObject kmsg = null, fresult = null;
                            boolean isTemplateExists = false;
                            // code to check if the templaet exists in DB
                            JSONObject paramObj = new JSONObject();
                            paramObj.put("companyid", companyid);
                            paramObj.put("templatename", templatename);
                            paramObj.put("moduleid", moduleid);
                            KwlReturnObject checkKmsg = null;
                            checkKmsg = customDesignDAOObj.getTemplates(paramObj);
                            if (checkKmsg.getRecordTotalCount() > 0 ) {
                                isTemplateExists = true;
                                msg = "Template already exist";
                                throw new AccountingException("Template already exist");
                            }
                            if (!isTemplateExists) {
                                //code to add that template in DB
                                if (!StringUtil.isNullOrEmpty(imageData)) {
                                    try {
                                     html = saveImageFromXLSX(imageData,filePath,html); 
                                    } catch ( Exception ex) {
                                        msg += "<br> Image(s) not Imported. Please import your image(s) from Doucument Designer Manually.";
                                    }
                                }
                                kmsg = customDesignDAOObj.copyTemplate(companyid, userId, Integer.parseInt(moduleid), templatename, templatesubtype, html, json, pagelayoutproperty, footerhtml, footerjson, headerhtml, headerjson, sqlquery, footersqlquery, headersqlquery, "0", "1");
                                msg = messageSource.getMessage("acc.customedesigner.Templatehasbeensuccessfullyimported", null,  Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                                issuccess = true;
                                String moduleName = getModuleName(Integer.valueOf(moduleid));
                                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                                auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                                auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                                auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
//                                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has imported Template " + templatename + " for " + moduleName, request, companyid);
                                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has imported Template " + templatename + " for " + moduleName, auditRequestParams, companyid);
                            } else {
                                // code to add error messages
                            }

                          
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cont++;
            }
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//            throw new AccountingException("Error While Importing Records.");
            
        } finally {
            fileInputStream.close();
            br.close();
        }
        returnObj.put("success", issuccess);
        returnObj.put("msg", msg);

        return returnObj;
    }
       
     public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
           rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public JSONObject createCustomFields(JSONObject paramJobj,JSONObject datajobj) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx1");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        String id="";
        try {
            JSONObject jobj = new JSONObject(paramJobj.optString("customFieldJson","{}"));
            JSONArray jarr = jobj.optJSONArray("data");
            HashMap<String, Object> requesttoprocess = new HashMap<String, Object>();
            String companyid = paramJobj.getString(Constants.companyKey);
            String moduleid = "";
            String fieldlabel="";
            JSONObject modifiedJson=new JSONObject();
            boolean createField = true;
            for (int i = 0; i < jarr.length(); i++) {
                fieldlabel=jarr.optJSONObject(i).optString("fieldname", "");
                requesttoprocess.put("fieldlabel", fieldlabel);
                requesttoprocess.put("maxlength", 255);
                requesttoprocess.put("isessential", "No");
                requesttoprocess.put("fieldType", jarr.optJSONObject(i).optString("xtype", "1"));
                requesttoprocess.put("iseditable",  true);
                requesttoprocess.put("sendnotification", "No");
                requesttoprocess.put("notificationDays", "No");
                requesttoprocess.put("isforproject", "No");
                requesttoprocess.put("isfortask", "No");
                requesttoprocess.put("iscustomfield", "Yes");
                requesttoprocess.put("lineitem", jarr.optJSONObject(i).optString("isLineItem", "No"));
                requesttoprocess.put("companyid", companyid);
                requesttoprocess.put("combodata", jarr.optJSONObject(i).optString("values", ""));
                requesttoprocess.put("defaultval", "");
                moduleid = jarr.optJSONObject(i).optString("moduleid", "");
                createField = true;
                Map<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel.replaceAll("&nbsp;"," "), companyid, Integer.valueOf(moduleid)));  // replacing &nbsp; with space, as html should not be saved in DB.
                KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParamsIds(dupCheckRequestParams); //duplicate check for custom field i.e to check if custom field is present or not in Master Config/Field Params  
                if (resultDupCheck.getEntityList().size() > 0) {
                    createField = false;                //Custom Field already present in Field Params /Master Config. Setting create field flag to false.
                }
                if (createField) {  // checking createfield flag.
                    Map<String, Object> result = accAccountCMNObj.processrequestforImport(requesttoprocess, moduleid, "", "");//custom field, if not present then create that custom field 
                    JSONObject json1 = new JSONObject(String.valueOf(result.get("response")));
                    id = json1.getString("ID");      //assign created custom fields id to the placeholder
                }else{
                    id=String.valueOf(resultDupCheck.getEntityList().get(0));   //If custom field present then assign id of that custom field to placeholder.
                }
                
                //change placeholder of custom field , because When we import template in some subdomain and edit this template in that subdomain . when we export and import this template in another subdomain , the custom fields are not get imported.
                int jobjLength = datajobj.getJSONArray("data").length();
                for (int index = 1; index < jobjLength; index++) {
                    JSONObject JSON = new JSONObject("{JSON:" + datajobj.getJSONArray("data").getJSONObject(index).optString("C", "[]") + "}");
                    modifiedJson = changePlaceholderInJSON(JSON, fieldlabel, id);      //asign id of custom field to placeholder in JSON
                    datajobj.getJSONArray("data").getJSONObject(index).put("C", modifiedJson.getJSONArray("JSON").toString());
                    String html = datajobj.getJSONArray("data").getJSONObject(index).optString("G", "");
                    html = changePlaceholderInHTML(html, fieldlabel, id);     //asign id of custom field to placeholder in HTML
                    datajobj.getJSONArray("data").getJSONObject(index).put("G", html);
                }
            }
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            System.out.println("\nError file write [success/failed] " + ex);
        }
        return datajobj;
    }
    
    
     public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }
    
     public JSONObject getCustomColumnFromTemplateIfAny(JSONObject jobj,Map requestParams) {
        JSONObject returnJobj = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            String jsonString = "{JSON:"+jobj.getString("JSON")+"}";
            if ( jobj.has("JSON")) {
                json = new JSONObject(jsonString);
                json.put("moduleid", jobj.getString("moduleid"));
            }
            JSONObject custJson = getCustomColJsonFromJson(json,requestParams);
            returnJobj = custJson;
        } catch (Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnJobj;
     }
     
    public JSONObject getCustomColJsonFromJson(JSONObject jobj,Map requestParams) {
        JSONObject returnJobj =  new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONArray imgJarr = new JSONArray();
        Set<String> custSet = new HashSet<String>();
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        String companyid = requestParams.get("companyid").toString();
        order_by.add("value");  // to fetch data in ascending order on "value" field.
        order_type.add("asc");
        try {
            JSONArray rows = jobj.optJSONArray("JSON");
            String moduleid = jobj.getString("moduleid");
            for( int rowIndex = 0 ; rowIndex<rows.length() ; rowIndex++) {
                JSONObject row = rows.getJSONObject(rowIndex);
                JSONArray columns = row.optJSONArray("data");
                for (int columnIndex = 0 ; columnIndex< columns.length(); columnIndex++) {
                    JSONObject column = columns.getJSONObject(columnIndex);
                    JSONArray fields = column.optJSONArray("data");
                    for ( int fieldIndex = 0 ; fieldIndex<fields.length(); fieldIndex++) {
                        JSONObject field = fields.getJSONObject(fieldIndex);
                        JSONObject tempJson = new  JSONObject();
                        JSONObject tempImgJson = new JSONObject();
                        if ( field.optInt("fieldType",0) == 2 ) { // For Select Field
                            if (!StringUtil.isNullOrEmpty(field.optString("customfield","")) && field.getBoolean("customfield") && !custSet.contains(field.optString("label",""))) {
                                String fieldlabel=field.optString("label","");
                                String xtype=field.optString("xType","1");
                                tempJson.put("fieldname", fieldlabel);
                                tempJson.put("xtype", xtype);
                                tempJson.put("typeName", "Custom Field");
                                tempJson.put("fieldLevelName", "Global");
                                tempJson.put("isLineItem", "No");
                                tempJson.put("moduleid", moduleid);
                                if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {   // get values of global level custom combo ,multiselect and checklist fields.
                                    filter_names.clear();
                                    filter_params.clear();
                                    filterRequestParams.clear();
                                    filter_names.add("field.company.companyID");
                                    filter_params.add(companyid);
                                    filter_names.add("field.fieldlabel");
                                    filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));  //replace &nbsp; with space because in db fieldlabel is store as seperated by space.
                                    filter_names.add("field.moduleid");
                                    filter_params.add(Integer.valueOf(moduleid));
                                    filterRequestParams.put("filter_names", filter_names);
                                    filterRequestParams.put("filter_params", filter_params);
                                    filterRequestParams.put("order_by", order_by);
                                    filterRequestParams.put("order_type", order_type);
                                    KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                    List combodatalist = combodataresult.getEntityList();
                                    String comboData = "";
                                    Iterator masteritr = combodatalist.iterator();
                                    while (masteritr.hasNext()) {
                                        FieldComboData item = (FieldComboData) masteritr.next();
                                        comboData = comboData + (item.getValue()) + ";";           //get values seperated by semicolon(;)
                                    }
                                    if(!StringUtil.isNullOrEmpty(comboData)){
                                        comboData = comboData.substring(0, comboData.length() - 1);
                                    }
                                    tempJson.put("values", comboData);
                                }
                                jarr.put(tempJson);
                                custSet.add(field.optString("label",""));
                            }
                        } 
                        if ( field.optInt("fieldType",0) == 17 ) { // For Select Field
                            if (!StringUtil.isNullOrEmpty(field.optString("customfield","")) && field.getBoolean("customfield") && !custSet.contains(field.optString("label",""))) {
                                String fieldlabel=field.optString("label","");
                                String xtype=field.optString("xtype","1");
                                tempJson.put("fieldname", fieldlabel);
                                tempJson.put("xtype", xtype);
                                tempJson.put("typeName", "Custom Field");
                                tempJson.put("fieldLevelName", "Global");
                                tempJson.put("isLineItem", "No");
                                tempJson.put("moduleid", moduleid);
                                if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {   // get values of global level custom combo ,multiselect and checklist fields.
                                    filter_names.clear();
                                    filter_params.clear();
                                    filterRequestParams.clear();
                                    filter_names.add("field.company.companyID");
                                    filter_params.add(companyid);
                                    filter_names.add("field.fieldlabel");
                                    filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));  //replace &nbsp; with space because in db fieldlabel is store as seperated by space.
                                    filter_names.add("field.moduleid");
                                    filter_params.add(Integer.valueOf(moduleid));
                                    filterRequestParams.put("filter_names", filter_names);
                                    filterRequestParams.put("filter_params", filter_params);
                                    filterRequestParams.put("order_by", order_by);
                                    filterRequestParams.put("order_type", order_type);
                                    KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                    List combodatalist = combodataresult.getEntityList();
                                    String comboData = "";
                                    Iterator masteritr = combodatalist.iterator();
                                    while (masteritr.hasNext()) {
                                        FieldComboData item = (FieldComboData) masteritr.next();
                                        comboData = comboData + (item.getValue()) + ";";           //get values seperated by semicolon(;)
                                    }
                                    if(!StringUtil.isNullOrEmpty(comboData)){
                                        comboData = comboData.substring(0, comboData.length() - 1);
                                    }
                                    tempJson.put("values", comboData);
                                }
                                jarr.put(tempJson);
                                custSet.add(field.optString("label",""));
                            }
                        } 
                        if ( field.optInt("fieldType",0) == 11 ) { // For Line Item Table
                            JSONArray dataArr = field.optJSONArray("data");
                            for ( int dataIndex =0; dataIndex < dataArr.length(); dataIndex++ ) {
                                if ( dataIndex == 0 ) {
                                    JSONObject dataObj = dataArr.getJSONObject(dataIndex);
                                    JSONArray lineItems = dataObj.optJSONArray("lineitems");
                                    for ( int LIIndex = 0; LIIndex < lineItems.length(); LIIndex++) {
                                        JSONObject lineItem = lineItems.getJSONObject(LIIndex);
                                        String fieldid = lineItem.getString("fieldid");
                                        if (((fieldid.length() > 3 && fieldid.substring(0, 3).equalsIgnoreCase("col")) || (fieldid.length() > 7 && fieldid.substring(0, 7).equalsIgnoreCase("custom_"))) && !custSet.contains(lineItem.optString("label",""))) {
                                            JSONObject tempJson1 = new JSONObject();
                                            String fieldlabel = lineItem.optString("label", "");
                                            String xtype = lineItem.optString("xtype", "1");
                                            tempJson1.put("fieldname",fieldlabel );
                                            tempJson1.put("xtype",xtype );
                                            tempJson1.put("typeName", "Custom Field");
                                            tempJson1.put("fieldLevelName", "Line Item");
                                            tempJson1.put("isLineItem", "Yes");
                                            tempJson1.put("moduleid", moduleid);
                                            if (xtype.equals("4") || xtype.equals("7")) {     // get values of line level custom combo ,multiselect fields
                                                filter_names.clear();
                                                filter_params.clear();
                                                filterRequestParams.clear();
                                                filter_names.add("field.company.companyID");
                                                filter_params.add(companyid);
                                                filter_names.add("field.fieldlabel");
                                                filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));
                                                filter_names.add("field.moduleid");
                                                filter_params.add(Integer.valueOf(moduleid));
                                                filterRequestParams.put("filter_names", filter_names);
                                                filterRequestParams.put("filter_params", filter_params);
                                                filterRequestParams.put("order_by", order_by);
                                                filterRequestParams.put("order_type", order_type);
                                                KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                                List combodatalist = combodataresult.getEntityList();
                                                String comboData = "";
                                                Iterator masteritr = combodatalist.iterator();
                                                while (masteritr.hasNext()) {
                                                    FieldComboData item = (FieldComboData) masteritr.next();
                                                    comboData = comboData + (item.getValue()) + ";";
                                                }
                                                if (!StringUtil.isNullOrEmpty(comboData)) {
                                                    comboData = comboData.substring(0, comboData.length() - 1);
                                                }
                                                tempJson1.put("values", comboData);
                                            }
                                            jarr.put(tempJson1);
                                            custSet.add(lineItem.optString("label",""));
                                        }
                                    }
                                } else {
                                    JSONArray lineItemCells = dataArr.getJSONArray(dataIndex);
                                    for ( int lineItemCellIndex = 0 ; lineItemCellIndex < lineItemCells.length(); lineItemCellIndex++ ) {
                                        JSONObject lineItemColJobj = lineItemCells.getJSONObject(lineItemCellIndex);
                                        JSONArray lineItemFieldArr = lineItemColJobj.getJSONArray("data");
                                        for( int lineIemFieldIndex = 0 ; lineIemFieldIndex < lineItemFieldArr.length(); lineIemFieldIndex++ ) {
                                            JSONObject lineItemfield = lineItemFieldArr.getJSONObject(fieldIndex);
                                            if (lineItemfield.optInt("fieldType", 0) == 2) { // For Select Field
                                                if (!StringUtil.isNullOrEmpty(lineItemfield.optString("customfield","")) && lineItemfield.getBoolean("customfield") && !custSet.contains(lineItemfield.optString("label", ""))) {
                                                    JSONObject tempJson1 = new JSONObject();
                                                    String fieldlabel = lineItemfield.optString("label", "");
                                                    String xtype = lineItemfield.optString("xType", "1");
                                                    tempJson1.put("fieldname",fieldlabel );
                                                    tempJson1.put("xtype", xtype);
                                                    tempJson1.put("typeName", "Custom Field");
                                                    tempJson1.put("fieldLevelName", "Global");
                                                    tempJson1.put("isLineItem", "No");
                                                    tempJson1.put("moduleid", moduleid);
                                                    if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {// get values of column level custom combo ,multiselect and checklist fields
                                                        filter_names.clear();
                                                        filter_params.clear();
                                                        filterRequestParams.clear();
                                                        filter_names.add("field.company.companyID");
                                                        filter_params.add(companyid);
                                                        filter_names.add("field.fieldlabel");
                                                        filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));
                                                        filter_names.add("field.moduleid");
                                                        filter_params.add(Integer.valueOf(moduleid));
                                                        filterRequestParams.put("filter_names", filter_names);
                                                        filterRequestParams.put("filter_params", filter_params);
                                                        filterRequestParams.put("order_by", order_by);
                                                        filterRequestParams.put("order_type", order_type);
                                                        KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                                        List combodatalist = combodataresult.getEntityList();
                                                        String comboData = "";
                                                        Iterator masteritr = combodatalist.iterator();
                                                        while (masteritr.hasNext()) {
                                                            FieldComboData item = (FieldComboData) masteritr.next();
                                                            comboData = comboData + (item.getValue()) + ";";
                                                        }
                                                        if (!StringUtil.isNullOrEmpty(comboData)) {
                                                            comboData = comboData.substring(0, comboData.length() - 1);
                                                        }
                                                        tempJson1.put("values", comboData);
                                                    }
                                                    jarr.put(tempJson1);
                                                    custSet.add(lineItemfield.optString("label", ""));
                                                }
                                            } 
                                            if (lineItemfield.optInt("fieldType", 0) == 17) { // For Select Field
                                                if (!StringUtil.isNullOrEmpty(lineItemfield.optString("customfield","")) && lineItemfield.getBoolean("customfield") && !custSet.contains(lineItemfield.optString("label", ""))) {
                                                    JSONObject tempJson1 = new JSONObject();
                                                    String fieldlabel = lineItemfield.optString("label", "");
                                                    String xtype = lineItemfield.optString("xtype","1");
                                                    tempJson1.put("fieldname",fieldlabel );
                                                    tempJson1.put("xtype", xtype);
                                                    tempJson1.put("typeName", "Custom Field");
                                                    tempJson1.put("fieldLevelName", "Global");
                                                    tempJson1.put("isLineItem", "No");
                                                    tempJson1.put("moduleid", moduleid);
                                                    if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {// get values of column level custom combo ,multiselect and checklist fields
                                                        filter_names.clear();
                                                        filter_params.clear();
                                                        filterRequestParams.clear();
                                                        filter_names.add("field.company.companyID");
                                                        filter_params.add(companyid);
                                                        filter_names.add("field.fieldlabel");
                                                        filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));
                                                        filter_names.add("field.moduleid");
                                                        filter_params.add(Integer.valueOf(moduleid));
                                                        filterRequestParams.put("filter_names", filter_names);
                                                        filterRequestParams.put("filter_params", filter_params);
                                                        filterRequestParams.put("order_by", order_by);
                                                        filterRequestParams.put("order_type", order_type);
                                                        KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                                        List combodatalist = combodataresult.getEntityList();
                                                        String comboData = "";
                                                        Iterator masteritr = combodatalist.iterator();
                                                        while (masteritr.hasNext()) {
                                                            FieldComboData item = (FieldComboData) masteritr.next();
                                                            comboData = comboData + (item.getValue()) + ";";
                                                        }
                                                        if (!StringUtil.isNullOrEmpty(comboData)) {
                                                            comboData = comboData.substring(0, comboData.length() - 1);
                                                        }
                                                        tempJson1.put("values", comboData);
                                                    }
                                                    jarr.put(tempJson1);
                                                    custSet.add(lineItemfield.optString("label", ""));
                                                }
                                            } 
                                            if (lineItemfield.optInt("fieldType", 0) == 12) { // For Global Table
                                                JSONArray cellPlaceHolders = lineItemfield.optJSONArray("cellplaceholder");
                                                if (cellPlaceHolders != null) {
                                                    for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                                        JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                                        if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield","")) && cellPlaceHolder.getBoolean("customfield") && !custSet.contains(cellPlaceHolder.optString("label", ""))) {
                                                            JSONObject tempJson2 = new JSONObject();
                                                            String fieldlabel = cellPlaceHolder.optString("label", "");
                                                            String xtype = cellPlaceHolder.optString("xtype", "1");
                                                            tempJson2.put("fieldname", fieldlabel);
                                                            tempJson2.put("xtype", xtype);
                                                            tempJson2.put("typeName", "Custom Field");
                                                            tempJson2.put("fieldLevelName", "Global");
                                                            tempJson2.put("isLineItem", "No");
                                                            tempJson2.put("moduleid", moduleid);
                                                            if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {   // get values of global table level custom combo ,multiselect and checklist fields
                                                                filter_names.clear();
                                                                filter_params.clear();
                                                                filterRequestParams.clear();
                                                                filter_names.add("field.company.companyID");
                                                                filter_params.add(companyid);
                                                                filter_names.add("field.fieldlabel");
                                                                filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));
                                                                filter_names.add("field.moduleid");
                                                                filter_params.add(Integer.valueOf(moduleid));
                                                                filterRequestParams.put("filter_names", filter_names);
                                                                filterRequestParams.put("filter_params", filter_params);
                                                                filterRequestParams.put("order_by", order_by);
                                                                filterRequestParams.put("order_type", order_type);
                                                                KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                                                List combodatalist = combodataresult.getEntityList();
                                                                String comboData = "";
                                                                Iterator masteritr = combodatalist.iterator();
                                                                while (masteritr.hasNext()) {
                                                                    FieldComboData item = (FieldComboData) masteritr.next();
                                                                    comboData = comboData + (item.getValue()) + ";";
                                                                }
                                                                if (!StringUtil.isNullOrEmpty(comboData)) {
                                                                    comboData = comboData.substring(0, comboData.length() - 1);
                                                                }
                                                                tempJson2.put("values", comboData);
                                                            }
                                                            jarr.put(tempJson2);
                                                            custSet.add(cellPlaceHolder.optString("label", ""));
                                                        }
                                                    }
                                                }
                                            }

                                            if (lineItemfield.optInt("fieldType", 0) == 3) {
                                                String src = lineItemfield.optString("src", "");
                                                if (!StringUtil.isNullOrEmpty(src)) {
                                                    String srcArr[] = src.split("=");
                                                    tempImgJson.put("image", srcArr[1]);
                                                    imgJarr.put(tempImgJson);
                                                }

                                            }

                                        }
                                    }
                                }
                            }
                        }
                        if ( field.optInt("fieldType",0) == 12 ) { // For Global Table
                            JSONArray cellPlaceHolders = field.optJSONArray("cellplaceholder");
                            if (cellPlaceHolders != null) {
                                for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                    JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                    if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield","")) && cellPlaceHolder.getBoolean("customfield") && !custSet.contains(cellPlaceHolder.optString("label", ""))) {
                                        JSONObject tempJson1 = new JSONObject();
                                        String fieldlabel = cellPlaceHolder.optString("label", "");
                                        String xtype = cellPlaceHolder.optString("xtype", "1");
                                        tempJson1.put("fieldname", cellPlaceHolder.optString("label", ""));
                                        tempJson1.put("xtype", cellPlaceHolder.optString("xtype", "1"));
                                        tempJson1.put("typeName", "Custom Field");
                                        tempJson1.put("fieldLevelName", "Global");
                                        tempJson1.put("isLineItem", "No");
                                        tempJson1.put("moduleid", moduleid);
                                        if (xtype.equals("4") || xtype.equals("7") || xtype.equals("12")) {    // get values of global table level custom combo ,multiselect and checklist fields
                                            filter_names.clear();
                                            filter_params.clear();
                                            filterRequestParams.clear();
                                            filter_names.add("field.company.companyID");
                                            filter_params.add(companyid);
                                            filter_names.add("field.fieldlabel");
                                            filter_params.add(fieldlabel.replaceAll("&nbsp;"," "));
                                            filter_names.add("field.moduleid");
                                            filter_params.add(Integer.valueOf(moduleid));
                                            filterRequestParams.put("filter_names", filter_names);
                                            filterRequestParams.put("filter_params", filter_params);
                                            filterRequestParams.put("order_by", order_by);
                                            filterRequestParams.put("order_type", order_type);
                                            KwlReturnObject combodataresult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                                            List combodatalist = combodataresult.getEntityList();
                                            String comboData = "";
                                            Iterator masteritr = combodatalist.iterator();
                                            while (masteritr.hasNext()) {
                                                FieldComboData item = (FieldComboData) masteritr.next();
                                                comboData = comboData + (item.getValue()) + ";";
                                            }
                                            if (!StringUtil.isNullOrEmpty(comboData)) {
                                                comboData = comboData.substring(0, comboData.length() - 1);
                                            }
                                            tempJson1.put("values", comboData);
                                        }
                                        jarr.put(tempJson1);
                                        custSet.add(cellPlaceHolder.optString("label", ""));
                                    }
                                }
                            }
                        }
                        
                        if (field.optInt("fieldType",0) == 3 ) {
                            String src = field.optString("src","");
                            if ( !StringUtil.isNullOrEmpty(src) ) {
                                String srcArr [] = src.split("=");
                                tempImgJson.put("image", srcArr[1]);
                                imgJarr.put(tempImgJson);
                            }
                             
                        }
                    }
                }
            }
            returnJobj.put("imgData", imgJarr);
            returnJobj.put("data", jarr);
        } catch (Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnJobj;
    }
 
   @Override 
    public JSONObject modifyHtmlJsonofTemplate(JSONObject paramJobj, JSONObject jobj) {
        JSONObject returnJobj = new JSONObject();
        try {
            JSONObject discardJobj = new JSONObject(paramJobj.optString("discardFieldJson","{}"));
            JSONArray discardJarr = discardJobj.optJSONArray("data");
            int jobjLength = jobj.getJSONArray("data").length();
            for (int index = 1; index < jobjLength; index++) {
                JSONObject JSON = new JSONObject("{JSON:" + jobj.getJSONArray("data").getJSONObject(index).optString("C", "[]") + "}");
                for (int i = 0; i < discardJarr.length(); i++) {
                    String fieldName = discardJarr.optJSONObject(i).optString("fieldname", "");
                    JSON = removecustomfieldfromjson(JSON, fieldName);
                }
                
                JSON = removecustomfieldfromjson(JSON, "image_");
                jobj.getJSONArray("data").getJSONObject(index).put("C", JSON.getJSONArray("JSON").toString());
                jobj.getJSONArray("data").getJSONObject(index).put("N", JSON.getJSONArray("imageData").toString());
                
                String html = jobj.getJSONArray("data").getJSONObject(index).optString("G", "");
                String headerhtml = jobj.getJSONArray("data").getJSONObject(index).optString("H", "");
                String footerhtml = jobj.getJSONArray("data").getJSONObject(index).optString("I", "");
                for (int i = 0; i < discardJarr.length(); i++) {
                    String fieldName = discardJarr.optJSONObject(i).optString("fieldname", "");
                    html = removecustomfieldfromHTML(html, fieldName);
                }
                /*
                 * After import, image id is replaced with new id in the code. 
                 * Then while printing template, image was not printed.  
                 * Following code is to replace the image path in HTML.
                 */
                if(JSON != null && JSON.has("imageData") && JSON.get("imageData")!=null){
                    JSONArray imagedata = (JSONArray)JSON.get("imageData");
                    for(int imgcnt = 0; imgcnt < imagedata.length(); imgcnt++ ){
                        JSONObject imageJson = imagedata.getJSONObject(imgcnt);
                        String earlierSrc = "",image = "";
                        if(imageJson.has("earlierSrc") && !StringUtil.isNullOrEmpty(imageJson.getString("earlierSrc"))){
                            // old image id
                            earlierSrc = imageJson.getString("earlierSrc");
                        }
                        if(imageJson.has("image") && !StringUtil.isNullOrEmpty(imageJson.getString("image"))){
                            // new image id 
                            image = imageJson.getString("image");
                        }
                        if(!StringUtil.isNullOrEmpty(earlierSrc) && !StringUtil.isNullOrEmpty(image)){
                            String src = "video.jsp?id="+image+".png";
                            html = html.replace(earlierSrc, src);
                            headerhtml = headerhtml.replace(earlierSrc, src);
                            footerhtml = footerhtml.replace(earlierSrc, src);
                        }
                    }
                }   
                jobj.getJSONArray("data").getJSONObject(index).put("G", html);
                jobj.getJSONArray("data").getJSONObject(index).put("H", headerhtml);
                jobj.getJSONArray("data").getJSONObject(index).put("I", footerhtml);
            }
            returnJobj = jobj;
        } catch(Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return returnJobj;
    }
    
    public String removecustomfieldfromHTML(String HTML, String fieldName) {
        String retHtml = "";
        try {
            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(HTML);
            List<org.jsoup.nodes.Element> globalTables = htmlDoc.getElementsByClass("globaltable");
            Iterator ite = globalTables.iterator();
            while(ite.hasNext()) { //  global table
                org.jsoup.nodes.Element globalTable = (org.jsoup.nodes.Element) ite.next();
                List<org.jsoup.nodes.Element> divs = globalTable.getElementsContainingOwnText("#" + fieldName + "#");
                Iterator divIte = divs.iterator();
                while(divIte.hasNext()) {
                   org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                   org.jsoup.nodes.Element td = div.parent();
                   div.remove();
                   td.append("&nbsp;");
                }
            }
             //For Repeate row Table
            List<org.jsoup.nodes.Element> globaltablerepeat = htmlDoc.getElementsByClass("globaltablerepeat");
            Iterator repeat_ite = globaltablerepeat.iterator();
            while(ite.hasNext()) { //  Repeate row table
                org.jsoup.nodes.Element repeateGlobalTable = (org.jsoup.nodes.Element) repeat_ite.next();
                List<org.jsoup.nodes.Element> divs = repeateGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");
                Iterator divIte = divs.iterator();
                while(divIte.hasNext()) {
                   org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                   org.jsoup.nodes.Element td = div.parent();
                   div.remove();
                   td.append("&nbsp;");
                }
            }
            List<org.jsoup.nodes.Element> selectFields = htmlDoc.getElementsContainingOwnText("#"+fieldName + "#");  // for select fields
            ite = selectFields.iterator();
            while ( ite.hasNext()) {
               org.jsoup.nodes.Element selectField = (org.jsoup.nodes.Element) ite.next();
               org.jsoup.nodes.Element parentDiv = selectField.parent();
               parentDiv.remove();
            }
            
            org.jsoup.nodes.Element lineItemTable = htmlDoc.getElementById("itemlistconfigsectionPanelGrid");
            if ( lineItemTable != null ) {
                List<org.jsoup.nodes.Element> lineItems = lineItemTable.getElementsContainingOwnText("#" + fieldName + "#");
                Iterator LIIte = lineItems.iterator();
                while(LIIte.hasNext()) {
                    org.jsoup.nodes.Element lineItem = (org.jsoup.nodes.Element) LIIte.next();
                    lineItem.remove();
                }
                List<org.jsoup.nodes.Element> lineItemSelectFields = lineItemTable.getElementsContainingOwnText("#" + fieldName + "#");
                LIIte = lineItemSelectFields.iterator();
                while (LIIte.hasNext()) {
                    org.jsoup.nodes.Element selectField = (org.jsoup.nodes.Element) LIIte.next();
                    org.jsoup.nodes.Element parentDiv = selectField.parent();
                    parentDiv.remove();
                }
                List<org.jsoup.nodes.Element> lineItemglobalTables = htmlDoc.getElementsByClass("globaltable");
                LIIte = lineItemglobalTables.iterator();
                while (LIIte.hasNext()) { //  global table
                    org.jsoup.nodes.Element globalTable = (org.jsoup.nodes.Element) LIIte.next();
                    List<org.jsoup.nodes.Element> divs = globalTable.getElementsContainingOwnText("#" + fieldName + "#");
                    Iterator divIte = divs.iterator();
                    while (divIte.hasNext()) {
                        org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                        org.jsoup.nodes.Element td = div.parent();
                        div.remove();
                        td.append("&nbsp;");
                    }
            }


            }
            
            retHtml = htmlDoc.outerHtml();
        } catch(Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return retHtml;
    }
    
    public JSONObject removecustomfieldfromjson(JSONObject jobj, String fieldName) {
        JSONObject retjobj = new JSONObject();
        JSONArray imgArr = new JSONArray();
        try {
            JSONArray rows = jobj.optJSONArray("JSON");
//            String moduleid = jobj.getString("moduleid");
            for( int rowIndex = 0 ; rowIndex<rows.length() ; rowIndex++) {
                JSONObject row = rows.getJSONObject(rowIndex);
                JSONArray columns = row.optJSONArray("data");
                for (int columnIndex = 0 ; columnIndex< columns.length(); columnIndex++) {
                    JSONObject column = columns.getJSONObject(columnIndex);
                    JSONArray fields = column.optJSONArray("data");
                    JSONArray newFields = new JSONArray();
                    for ( int fieldIndex = 0 ; fieldIndex<fields.length(); fieldIndex++) {
                        JSONObject field = fields.getJSONObject(fieldIndex);
                        JSONObject tempJson = new  JSONObject();
                        JSONObject imgTempJson = new JSONObject();
                        if ( field.optInt("fieldType",0) == 2 ) { // For Select Field
                            if (!StringUtil.isNullOrEmpty(field.optString("customfield","")) && field.getBoolean("customfield")) {
                                String fieldname = field.optString("label","");
                                if ( !fieldName.equalsIgnoreCase(fieldname) ) {
                                    newFields.put(field);
                                }
                            } else {
                                newFields.put(field);
                            }
                        } else if (field.optInt("fieldType", 0) == 17) {      // get data Element Field
                            if (!StringUtil.isNullOrEmpty(field.getString("customfield")) && field.getBoolean("customfield")) {
                                String fieldname = field.optString("label", "");
                                if ( !fieldName.equalsIgnoreCase(fieldname) ) {
                                    newFields.put(field);
                                }
                            } else {
                                newFields.put(field);
                            }
                        }  else if ( field.optInt("fieldType", 0) == 11) { // For Line Item Table
                            try {
                                JSONArray dataArr = field.optJSONArray("data");
                                for (int dataIndex = 0; dataIndex < dataArr.length(); dataIndex++) {
                                    if ( dataIndex == 0 ) {
                                        JSONArray lineItems = new JSONArray();
                                        JSONObject dataObj = new JSONObject();
                                        try {
                                            dataObj = dataArr.getJSONObject(dataIndex);
                                            lineItems = dataObj.optJSONArray("lineitems");
                                        } catch (Exception ex) {

                                        }
                                        JSONArray newLineItems = new JSONArray();
                                        for (int LIIndex = 0; LIIndex < lineItems.length(); LIIndex++) {
                                            JSONObject lineItem = lineItems.getJSONObject(LIIndex);
                                            String fieldid = lineItem.getString("fieldid");
                                            if (((fieldid.length() > 7 && fieldid.substring(0, 7).equalsIgnoreCase("custom_")))) {
                                                String fieldname = fieldid.substring(7);
                                                if (!fieldName.equalsIgnoreCase(fieldname)) {
                                                    newLineItems.put(lineItem);
                                                }
                                            } else {
                                                newLineItems.put(lineItem);
                                            }
                                        }
//                                        rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).getJSONArray("data").getJSONObject(fieldIndex).getJSONArray("data").getJSONObject(dataIndex).put("lineitems", newLineItems);
                                        field.getJSONArray("data").getJSONObject(dataIndex).put("lineitems", newLineItems);
                                        JSONArray headerItems = dataObj.optJSONArray("headeritems");
                                        JSONArray newHeaderItems = new JSONArray();
                                        for (int HIIndex = 0; HIIndex < headerItems.length(); HIIndex++) {
                                            JSONObject headerItem = headerItems.getJSONObject(HIIndex);
                                            String fieldid = headerItem.getString("fieldid");
                                            if (((fieldid.length() > 7 && fieldid.substring(0, 7).equalsIgnoreCase("custom_")))) {
                                                String fieldname = fieldid.substring(7);
                                                if (!fieldName.equalsIgnoreCase(fieldname)) {
                                                    newHeaderItems.put(headerItem);
                                                }
                                            } else {
                                                newHeaderItems.put(headerItem);
                                            }
                                        }
                                        field.getJSONArray("data").getJSONObject(dataIndex).put("headeritems", newHeaderItems);
//                                        rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).getJSONArray("data").getJSONObject(fieldIndex).getJSONArray("data").getJSONObject(dataIndex).put("headeritems", newHeaderItems);
                                    } else {
                                        JSONArray lineItemCells = dataArr.getJSONArray(dataIndex);
                                        for (int lineItemCellIndex = 0; lineItemCellIndex < lineItemCells.length(); lineItemCellIndex++) {
                                            JSONObject lineItemColJobj = lineItemCells.getJSONObject(lineItemCellIndex);
                                            JSONArray lineItemFieldArr = lineItemColJobj.getJSONArray("data");
                                            JSONArray newlineItemFieldArr = new JSONArray();
                                            for (int lineIemFieldIndex = 0; lineIemFieldIndex < lineItemFieldArr.length(); lineIemFieldIndex++) {
                                                JSONObject lineItemfield = lineItemFieldArr.getJSONObject(lineIemFieldIndex);
                                                if (lineItemfield.optInt("fieldType", 0) == 2) { // For Select Field
                                                    if (!StringUtil.isNullOrEmpty(lineItemfield.optString("customfield", "")) && lineItemfield.getBoolean("customfield")) {
                                                        String fieldname = lineItemfield.optString("label", "");
                                                        if (!fieldName.equalsIgnoreCase(fieldname)) {
                                                            newlineItemFieldArr.put(lineItemfield);
                                                        }
                                                    } else {
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    }
                                                }else if (lineItemfield.optInt("fieldType", 0) == 17) { // For Data Element in line Item
                                                    if (!StringUtil.isNullOrEmpty(lineItemfield.getString("customfield")) && lineItemfield.getBoolean("customfield")) {
                                                        String fieldname = lineItemfield.optString("label", "");
                                                        if (!fieldName.equalsIgnoreCase(fieldname)) {
                                                            newlineItemFieldArr.put(lineItemfield);
                                                        }
                                                    } else {
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    }
                                                } else if (lineItemfield.optInt("fieldType", 0) == 12) { // For Global Table
                                                    JSONArray cellPlaceHolders = lineItemfield.optJSONArray("cellplaceholder");
                                                    JSONArray newCellPlaceHolders = new JSONArray();
                                                    if (cellPlaceHolders != null) {
                                                        for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                                            JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                                            if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield", "")) && cellPlaceHolder.getBoolean("customfield")) {
                                                                String fieldname = cellPlaceHolder.optString("label", "");
                                                                if (!fieldName.equalsIgnoreCase(fieldname)) {
                                                                    newCellPlaceHolders.put(cellPlaceHolder);
                                                                }
                                                            } else {
                                                                newCellPlaceHolders.put(cellPlaceHolder);
                                                            }
                                                        }
                                                        lineItemfield.put("cellplaceholder", newCellPlaceHolders);
                                                    }
                                                    newlineItemFieldArr.put(lineItemfield);
                                                } else if (lineItemfield.optInt("fieldType", 0) == 3) { // For Image
                                                    if (fieldName.equals("image_")) {
                                                        String uid = UUID.randomUUID().toString();
                                                        imgTempJson.put("image", uid);
                                                        imgArr.put(imgTempJson);
                                                        String src = "video.jsp?id=" + uid + ".png";
                                                        lineItemfield.put("src",src);
                                                    }
                                                    newlineItemFieldArr.put(lineItemfield);
                                                } else {
                                                    newlineItemFieldArr.put(lineItemfield);
                                                }
                                            }
                                            field.getJSONArray("data").getJSONArray(dataIndex).getJSONObject(lineItemCellIndex).put("data", newlineItemFieldArr);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                
                            }
                            String labelHtml = field.optString("labelhtml", "");
                            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(labelHtml);
                            org.jsoup.nodes.Element lineItemTable = htmlDoc.getElementById("itemlistconfigsectionPanelGrid");
                            if (lineItemTable != null) {
                                List<org.jsoup.nodes.Element> lineItems = lineItemTable.getElementsContainingOwnText("#" + fieldName + "#");
                                Iterator LIIte = lineItems.iterator();
                                while (LIIte.hasNext()) {
                                    org.jsoup.nodes.Element lineItem = (org.jsoup.nodes.Element) LIIte.next();
                                    lineItem.remove();
                                }
                                List<org.jsoup.nodes.Element> lineItemSelectFields = lineItemTable.getElementsContainingOwnText("#" + fieldName + "#");
                                LIIte = lineItemSelectFields.iterator();
                                while (LIIte.hasNext()) {
                                    org.jsoup.nodes.Element selectField = (org.jsoup.nodes.Element) LIIte.next();
                                    org.jsoup.nodes.Element parentDiv = selectField.parent();
                                    parentDiv.remove();
                                }
                                List<org.jsoup.nodes.Element> lineItemglobalTables = htmlDoc.getElementsByClass("globaltable");
                                LIIte = lineItemglobalTables.iterator();
                                while (LIIte.hasNext()) { //  global table
                                    org.jsoup.nodes.Element globalTable = (org.jsoup.nodes.Element) LIIte.next();
                                    List<org.jsoup.nodes.Element> divs = globalTable.getElementsContainingOwnText("#" + fieldName + "#");
                                    Iterator divIte = divs.iterator();
                                    while (divIte.hasNext()) {
                                        org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                        org.jsoup.nodes.Element td = div.parent();
                                        div.remove();
                                        td.append("&nbsp;");
                                    }
                                }
                                
                                
                            }
                            String modifiedHtml = lineItemTable.outerHtml();
                            field.put("labelhtml",modifiedHtml);
                            newFields.put(field);
                        } else if (field.optInt("fieldType", 0) == 12) { // For Global Table
                            JSONArray cellPlaceHolders = field.optJSONArray("cellplaceholder");
                            JSONArray newCellPlaceHolders = new JSONArray();
                            if (cellPlaceHolders != null) {
                                for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                    JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                    if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield","")) && cellPlaceHolder.getBoolean("customfield")) {
                                        String fieldname = cellPlaceHolder.optString("label", "");
                                        if (!fieldName.equalsIgnoreCase(fieldname)) {
                                            newCellPlaceHolders.put(cellPlaceHolder);
                                        }
                                    } else {
                                        newCellPlaceHolders.put(cellPlaceHolder);
                                    }
                                }
                                field.put("cellplaceholder", newCellPlaceHolders);
//                                rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).getJSONArray("data").getJSONObject(fieldIndex).put("cellplaceholder", newCellPlaceHolders);
                            }
                            
                            String labelHtml = field.optString("labelhtml", "");
                            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(labelHtml);
                            List<org.jsoup.nodes.Element> globalTables = htmlDoc.getElementsByClass("globaltable");
                            org.jsoup.nodes.Element mainGlobalTable = null;
                            Iterator ite = globalTables.iterator();
                            while (ite.hasNext()) { //  global table
                                mainGlobalTable = (org.jsoup.nodes.Element) ite.next();
                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");
                                Iterator divIte = divs.iterator();
                                while (divIte.hasNext()) {
                                    org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                    org.jsoup.nodes.Element td = div.parent();
                                    div.remove();
                                    td.append("&nbsp;");
                                }
                            }
                            //For Repeate row Table
                            List<org.jsoup.nodes.Element> globaltablerepeat = htmlDoc.getElementsByClass("globaltablerepeat");
                            Iterator repeat_ite = globaltablerepeat.iterator();
                            while (repeat_ite.hasNext()) { //  global Repeate Row table
                                mainGlobalTable = (org.jsoup.nodes.Element) repeat_ite.next();
                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");
                                Iterator divIte = divs.iterator();
                                while (divIte.hasNext()) {
                                    org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                    org.jsoup.nodes.Element td = div.parent();
                                    div.remove();
                                    td.append("&nbsp;");
                                }
                            }
                            String modifiedHtml = " ";
                            if ( mainGlobalTable != null) {
                                modifiedHtml = mainGlobalTable.outerHtml();
                            }
                            field.put("labelhtml",modifiedHtml);
                            newFields.put(field);
                        } else if ( field.optInt("fieldType",0) == 3 ) { // For Image
                            if ( fieldName.equals("image_") ) {
                                String uid = UUID.randomUUID().toString();
                                String initialSrc = field.optString("src","");
                                imgTempJson.put("image", uid);
                                imgTempJson.put("earlierSrc", initialSrc);
                                imgArr.put(imgTempJson);
                                String src = "video.jsp?id="+uid+".png";
                                field.put("src",src);
//                                rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).getJSONArray("data").getJSONObject(fieldIndex).putOpt("src",src );
                            }
                            newFields.put(field);
                        } else {
                            newFields.put(field);
                        }
                    }
                    rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).put("data", newFields);
                }
            }
            retjobj.put("imageData", imgArr);
            retjobj.put("JSON",rows);
        } catch (Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return retjobj;
    }
    
    public String saveImageFromXLSX(String imageData, String filePath,String html) {
        String retHtml = "";
        try {
            XSSFWorkbook wb = new XSSFWorkbook(filePath);
            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(html);
            List imgList =  wb.getAllPictures();
            Iterator imgIte = imgList.iterator(); 
            JSONArray imgJarr = new JSONArray(imageData);
            int i = 0;
            while ( imgIte.hasNext() ) {
                XSSFPictureData picture = (XSSFPictureData)imgIte.next();
                byte[] data = picture.getData();
                String src = storageHandlerImpl.GetDocStorePath() + imgJarr.getJSONObject(i).getString("image") + ".png"; 
                List<org.jsoup.nodes.Element> images = htmlDoc.getElementsByAttributeValue("src", imgJarr.getJSONObject(i).optString("earlierSrc",""));
                for (org.jsoup.nodes.Element img : images) {
                    img.attr("src","video.jsp?id=" + imgJarr.getJSONObject(i).getString("image") + ".png");
                }
                FileOutputStream out = new FileOutputStream(src);
                out.write(data);
                out.close();
                i++;
            } 
            retHtml = htmlDoc.outerHtml();
        } catch ( Exception ex ) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return retHtml;
    }
    //change id assigned to placeholder of custom select field in JSON
    public JSONObject changePlaceholderInJSON(JSONObject jobj, String fieldName, String id) {
        JSONObject retjobj = new JSONObject();
        try {
            JSONArray rows = jobj.optJSONArray("JSON");//fetch all rows
            for (int rowIndex = 0; rowIndex < rows.length(); rowIndex++) {
                JSONObject row = rows.getJSONObject(rowIndex);
                JSONArray columns = row.optJSONArray("data");// fetch all columns from single row
                for (int columnIndex = 0; columnIndex < columns.length(); columnIndex++) {
                    JSONObject column = columns.getJSONObject(columnIndex);//fetch single column
                    JSONArray fields = column.optJSONArray("data"); // get all fields present present in single column
                    JSONArray newFields = new JSONArray();
                    for (int fieldIndex = 0; fieldIndex < fields.length(); fieldIndex++) {
                        JSONObject field = fields.getJSONObject(fieldIndex);    //get single field
                        if (field.optInt("fieldType", 0) == 2) {      // get Select Field
                            if (!StringUtil.isNullOrEmpty(field.optString("customfield", "")) && field.getBoolean("customfield")) {
                                String fieldname = field.optString("label", "");
                                if (fieldName.equalsIgnoreCase(fieldname)) {
                                    field.put("placeholder", id);        //assign id to placeholder
                                    String label = field.getString("labelhtml");    //assign id to placeholder in labelhtml
                                    String oldId = label.substring(label.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), label.indexOf("}"));   //get old id from PLACEHOLDER.
                                    label = label.replace(oldId, id);        //Replace old id with new id
                                    field.put("labelhtml", label);
                                }
                                newFields.put(field);          //put all fields in newFields
                            } else {
                                newFields.put(field);
                            }
                        } else if (field.optInt("fieldType", 0) == 17) {      // get data Element Field
                            if (!StringUtil.isNullOrEmpty(field.getString("customfield")) && field.getBoolean("customfield")) {
                                String fieldname = field.optString("label", "");
                                if (fieldName.equalsIgnoreCase(fieldname)) {
                                    field.put("placeholder", id);        //assign id to placeholder
                                    String label = field.getString("dataelementhtml");    //assign id to placeholder in labelhtml
                                    String oldId = label.substring(label.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), label.indexOf("}"));   //get old id from PLACEHOLDER.
                                    label = label.replace(oldId, id);        //Replace old id with new id
                                    field.put("dataelementhtml", label);
                                }
                                newFields.put(field);          //put all fields in newFields
                            } else {
                                newFields.put(field);
                            }
                        } else if (field.optInt("fieldType", 0) == 11) { // get Line Item Table
                            try {
                                JSONArray dataArr = field.optJSONArray("data");
                                for (int dataIndex = 0; dataIndex < dataArr.length(); dataIndex++) {
                                    if (dataIndex == 0) {
                                        JSONArray lineItems = new JSONArray();
                                        JSONObject dataObj = new JSONObject();
                                        try {
                                            dataObj = dataArr.getJSONObject(dataIndex);
                                            lineItems = dataObj.optJSONArray("lineitems");     //get all line item fields
                                        } catch (Exception ex) {
                                            retjobj = jobj;  //if exception occurs then return jobj as it is.
                                        }
                                        JSONArray newLineItems = new JSONArray();
                                        for (int LIIndex = 0; LIIndex < lineItems.length(); LIIndex++) {
                                            JSONObject lineItem = lineItems.getJSONObject(LIIndex);   //get all line Items.
                                            newLineItems.put(lineItem);      //put all line items in new line items.
                                        }
                                        field.getJSONArray("data").getJSONObject(dataIndex).put("lineitems", newLineItems);
                                        JSONArray headerItems = dataObj.optJSONArray("headeritems");
                                        JSONArray newHeaderItems = new JSONArray();
                                        for (int HIIndex = 0; HIIndex < headerItems.length(); HIIndex++) {
                                            JSONObject headerItem = headerItems.getJSONObject(HIIndex);     //get all line Items.
                                            newHeaderItems.put(headerItem);       //put all line items in new line items.
                                        }
                                        field.getJSONArray("data").getJSONObject(dataIndex).put("headeritems", newHeaderItems);
                                    } else {
                                        JSONArray lineItemCells = dataArr.getJSONArray(dataIndex);
                                        for (int lineItemCellIndex = 0; lineItemCellIndex < lineItemCells.length(); lineItemCellIndex++) {
                                            JSONObject lineItemColJobj = lineItemCells.getJSONObject(lineItemCellIndex);
                                            JSONArray lineItemFieldArr = lineItemColJobj.getJSONArray("data");
                                            JSONArray newlineItemFieldArr = new JSONArray();
                                            for (int lineIemFieldIndex = 0; lineIemFieldIndex < lineItemFieldArr.length(); lineIemFieldIndex++) {
                                                JSONObject lineItemfield = lineItemFieldArr.getJSONObject(lineIemFieldIndex);
                                                if (lineItemfield.optInt("fieldType", 0) == 2) { // For Select Field  in line Item
                                                    if (!StringUtil.isNullOrEmpty(lineItemfield.optString("customfield", "")) && lineItemfield.getBoolean("customfield")) {
                                                        String fieldname = lineItemfield.optString("label", "");
                                                        if (fieldName.equalsIgnoreCase(fieldname)) {  //change placeholder of select field in line item 
                                                            lineItemfield.put("placeholder", id);
                                                            String label = lineItemfield.getString("labelhtml");
                                                            String oldId = label.substring(label.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), label.indexOf("}"));   //get old id from PLACEHOLDER.
                                                            label = label.replace(oldId, id);
                                                            lineItemfield.put("labelhtml", label);
                                                        }
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    } else {
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    }
                                                } else if (lineItemfield.optInt("fieldType", 0) == 17) { // For Data Element in line Item
                                                    if (!StringUtil.isNullOrEmpty(lineItemfield.getString("customfield")) && lineItemfield.getBoolean("customfield")) {
                                                        String fieldname = lineItemfield.optString("label", "");
                                                        if (fieldName.equalsIgnoreCase(fieldname)) {  //change placeholder of select field in line item 
                                                            lineItemfield.put("placeholder", id);
                                                            String label = lineItemfield.getString("dataelementhtml");
                                                            String oldId = label.substring(label.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), label.indexOf("}"));   //get old id from PLACEHOLDER.
                                                            label = label.replace(oldId, id);
                                                            lineItemfield.put("dataelementhtml", label);
                                                        }
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    } else {
                                                        newlineItemFieldArr.put(lineItemfield);
                                                    }
                                                }else if (lineItemfield.optInt("fieldType", 0) == 12) { // For Global Table in line Item
                                                    JSONArray cellPlaceHolders = lineItemfield.optJSONArray("cellplaceholder");
                                                    JSONArray newCellPlaceHolders = new JSONArray();
                                                    if (cellPlaceHolders != null) {
                                                        for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                                            JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                                            if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield", "")) && cellPlaceHolder.getBoolean("customfield")) {
                                                                String fieldname = cellPlaceHolder.optString("label", "");
                                                                if (fieldName.equalsIgnoreCase(fieldname)) {   //change placeholder of select field in global table in line item 
                                                                    cellPlaceHolder.put("placeholder", id); //assign id to placeholder
                                                                }
                                                                newCellPlaceHolders.put(cellPlaceHolder);
                                                            } else {
                                                                newCellPlaceHolders.put(cellPlaceHolder);
                                                            }
                                                        }
                                                        lineItemfield.put("cellplaceholder", newCellPlaceHolders);
                                                    }
                                                    String labelHtml = lineItemfield.optString("labelhtml", "");    //assign id to placeholder in labelhtml
                                                    org.jsoup.nodes.Document htmlDoc = Jsoup.parse(labelHtml);
                                                    List<org.jsoup.nodes.Element> globalTables = htmlDoc.getElementsByClass("globaltable"); //get all global tables present in line item.
                                                    org.jsoup.nodes.Element mainGlobalTable = null;
                                                    Iterator ite = globalTables.iterator();
                                                    while (ite.hasNext()) { //  global table
                                                        mainGlobalTable = (org.jsoup.nodes.Element) ite.next();
                                                        List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");
                                                        Iterator divIte = divs.iterator();
                                                        while (divIte.hasNext()) {
                                                            org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                                            String placeholder = div.attr("attribute");
                                                            String oldId = placeholder.substring(placeholder.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), placeholder.indexOf("}"));  //get old id of placeholder.
                                                            placeholder = placeholder.replace(oldId, id);
                                                            div.attr("attribute", placeholder);     //assign id to placeholder in labelhtml
                                                        }
                                                    }
                                                    String modifiedHtml = " ";
                                                    if (mainGlobalTable != null) {
                                                        modifiedHtml = mainGlobalTable.outerHtml();  //get modified html
                                                    }
                                                    lineItemfield.put("labelhtml", modifiedHtml);
                                                    newlineItemFieldArr.put(lineItemfield);
                                                } else {
                                                    newlineItemFieldArr.put(lineItemfield);
                                                }
                                            }
                                            field.getJSONArray("data").getJSONArray(dataIndex).getJSONObject(lineItemCellIndex).put("data", newlineItemFieldArr);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                retjobj = jobj;     //if exception occurs then return JSON without changing placeholder.
                            }
                            newFields.put(field);
                        } else if (field.optInt("fieldType", 0) == 12) { // For Global Table
                            JSONArray cellPlaceHolders = field.optJSONArray("cellplaceholder");  //get all fields from Global Table
                            JSONArray newCellPlaceHolders = new JSONArray();
                            if (cellPlaceHolders != null) {
                                for (int CPHIndex = 0; CPHIndex < cellPlaceHolders.length(); CPHIndex++) {
                                    JSONObject cellPlaceHolder = cellPlaceHolders.getJSONObject(CPHIndex);
                                    if (!StringUtil.isNullOrEmpty(cellPlaceHolder.optString("customfield", "")) && cellPlaceHolder.getBoolean("customfield")) {
                                        String fieldname = cellPlaceHolder.optString("label", "");
                                        if (fieldName.equalsIgnoreCase(fieldname)) {
                                            cellPlaceHolder.put("placeholder", id);      //assign id to placeholder
                                        }
                                        newCellPlaceHolders.put(cellPlaceHolder);
                                    } else {
                                        newCellPlaceHolders.put(cellPlaceHolder);
                                    }
                                }
                                field.put("cellplaceholder", newCellPlaceHolders);
                            }

                            String labelHtml = field.optString("labelhtml", "");   //assign id to placeholder in labelhtml
                            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(labelHtml);
                            List<org.jsoup.nodes.Element> globalTables = htmlDoc.getElementsByClass("globaltable");   //get all global tables from html.
                            org.jsoup.nodes.Element mainGlobalTable = null;
                            Iterator ite = globalTables.iterator();   //traverse through each global table.
                            while (ite.hasNext()) { //  global table
                                mainGlobalTable = (org.jsoup.nodes.Element) ite.next();
//                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsByAttributeValue("columnname", fieldName);  //get all selected field from global table.
                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");  //get all selected field from global table.
                                Iterator divIte = divs.iterator();  //traverse through each select field .
                                while (divIte.hasNext()) {
                                    org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                    String placeholder = div.attr("attribute");    //get place holder of selected field
                                    String oldId = placeholder.substring(placeholder.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), placeholder.indexOf("}"));    //get old id of placeholder.
                                    placeholder = placeholder.replace(oldId, id);        //replace old id with new id .
                                    div.attr("attribute", placeholder);      //put placeholder with new id in labelhtml
                                }
                            }
                            //For Repeate row Table
                            List<org.jsoup.nodes.Element> globaltablerepeat = htmlDoc.getElementsByClass("globaltablerepeat");   //get all Repeat global tables from html.
                            Iterator repeat_ite = globaltablerepeat.iterator();   //traverse through each Repeat row table.
                            while (repeat_ite.hasNext()) { //  global repeate row table
                                mainGlobalTable = (org.jsoup.nodes.Element) repeat_ite.next();
//                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsByAttributeValue("columnname", fieldName);  //get all selected field from global table.
                                List<org.jsoup.nodes.Element> divs = mainGlobalTable.getElementsContainingOwnText("#" + fieldName + "#");  //get all selected field from global table.
                                Iterator divIte = divs.iterator();  //traverse through each select field .
                                while (divIte.hasNext()) {
                                    org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                                    String placeholder = div.attr("attribute");    //get place holder of selected field
                                    String oldId = placeholder.substring(placeholder.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), placeholder.indexOf("}"));    //get old id of placeholder.
                                    placeholder = placeholder.replace(oldId, id);        //replace old id with new id .
                                    div.attr("attribute", placeholder);      //put placeholder with new id in labelhtml
                                }
                            }
                           
                            String modifiedHtml = " ";
                            if (mainGlobalTable != null) {
                                modifiedHtml = mainGlobalTable.outerHtml();
                            }
                            field.put("labelhtml", modifiedHtml);
                            newFields.put(field);
                        } else {
                            newFields.put(field);
                        }
                    }
                    rows.getJSONObject(rowIndex).getJSONArray("data").getJSONObject(columnIndex).put("data", newFields);
                }
            }
            retjobj.put("JSON", rows);
        } catch (Exception ex) {
            retjobj = jobj;      //if exception occurs then return JSON without changing placeholder.
            Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return retjobj;
    }

    //change id assigned to placeholder of custom select field in HTML
    public String changePlaceholderInHTML(String HTML, String fieldName, String id) {
        String retHtml = "";
        try {
            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(HTML);
            List<org.jsoup.nodes.Element> globalTables = htmlDoc.getElementsByClass("globaltable");  //get all global tables present in template.
            Iterator ite = globalTables.iterator();    //traverse through each global table.
            while (ite.hasNext()) { //  global table
                org.jsoup.nodes.Element globalTable = (org.jsoup.nodes.Element) ite.next();
                List<org.jsoup.nodes.Element> divs = globalTable.getElementsContainingOwnText("#" + fieldName + "#");  //get all divs of selected field in global table.
                Iterator divIte = divs.iterator();
                while (divIte.hasNext()) {      // change place holder for custom select field in global table in html.
                    org.jsoup.nodes.Element div = (org.jsoup.nodes.Element) divIte.next();
                    String placeholder = div.attr("attribute");   //get placeholder of selected custom field.
                    String oldId = placeholder.substring(placeholder.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), placeholder.indexOf("}"));  //get old id of placeholder.
                    placeholder = placeholder.replace(oldId, id);       //replace old id with new id of custom field in placeholder
                    div.attr("attribute", placeholder);   //put this placeholder wih new id in custom field.
                }
            }
            List<org.jsoup.nodes.Element> selectFields = htmlDoc.getElementsContainingOwnText("#" + fieldName + "#");  // get all column level selected custom fields present in template.
            ite = selectFields.iterator();  //traverse through each custom select field.
            while (ite.hasNext()) {   
                org.jsoup.nodes.Element selectField = (org.jsoup.nodes.Element) ite.next();
                String placeholder = selectField.attr("attribute");     //get placeholder of selected custom field.
                String oldId = placeholder.substring(placeholder.indexOf("PLACEHOLDER:") + ("PLACEHOLDER:").length(), placeholder.indexOf("}"));    //get old id of placeholder.
                placeholder = placeholder.replace(oldId, id);    //replace old id with new id of custom field in placeholder
                selectField.attr("attribute", placeholder);      //put this placeholder wih new id in custom field.
            }

            retHtml = htmlDoc.outerHtml();
        } catch (Exception ex) {
            retHtml = HTML;    //if exception occurs then return HTML without changing placeholder.
            Logger.getLogger(DocumentDesignController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return retHtml;
    }
 
  @Override  
    public String getModuleName(int moduleid) {
        String moduleName = "";
        switch (moduleid) {
            case (Constants.Acc_Invoice_ModuleId):
                moduleName = "Invoice/Cash Sales";
                break;
            case (Constants.Acc_Purchase_Order_ModuleId):
                moduleName = "Purchase Order";
                break;
            case (Constants.Acc_Sales_Order_ModuleId):
                moduleName = "Sales Order";
                break;
            case (Constants.Acc_Credit_Note_ModuleId):
                moduleName = "Credit Note";
                break;
            case (Constants.Acc_Debit_Note_ModuleId):
                moduleName = "Debit Note";
                break;
            case (Constants.Acc_Delivery_Order_ModuleId):
                moduleName = "Delivery Order";
                break;
            case (Constants.Acc_Goods_Receipt_ModuleId):
                moduleName = "Goods Receipt";
                break;
            case (Constants.Acc_Make_Payment_ModuleId):
                moduleName = "Make Payment";
                break;
            case (Constants.Acc_Receive_Payment_ModuleId):
                moduleName = "Receive Payment";
                break;
            case (Constants.Acc_Vendor_Quotation_ModuleId):
                moduleName = "Vendor Quotation";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Vendor_Invoice_ModuleId):
                moduleName = "Purchase Invoice/Cash Purchase";
                break;
            case (Constants.Acc_Sales_Return_ModuleId):
                moduleName = "Sales Return";
                break;
            case (Constants.Acc_Purchase_Return_ModuleId):
                moduleName = "Purchase Return";
                break;
            case (Constants.Acc_RFQ_ModuleId):
                moduleName = "Request For Quotation";
                break;
            case (Constants.Acc_Stock_Adjustment_ModuleId):
                moduleName = "Stock Adjustment";
                break;
            case (Constants.Acc_Stock_Request_ModuleId):
                moduleName = "Stock Request";
                break;
            case (Constants.Inventory_ModuleId):
                moduleName = "Stock Issue";
                break;
            case (Constants.Acc_InterStore_ModuleId):
                moduleName = "Inter Store Stock Transfer";
                break;
            case (Constants.Acc_InterLocation_ModuleId):
                moduleName = "Inter Location Stock Transfer";
                break;
            case (Constants.Acc_Purchase_Requisition_ModuleId):
                moduleName = "Purchase Requisition";
                break;
            case (Constants.Build_Assembly_Module_Id): //ERM-26 Added Build Assembly Module in document designer
                moduleName = "Build Assembly Report";
                break;
            case (Constants.Acc_Customer_AccStatement_ModuleId): 
                moduleName = "Statement of Account(Customer)";
                break;
            case (Constants.Acc_Vendor_AccStatement_ModuleId): 
                moduleName = "Statement of Account(Vendor)";
                break;
        }
        return moduleName;
    }
   
  
 @Override 
     public JSONObject getCustomTemplatesJsonForExport(Map requestParams) {
        JSONObject jobj = new JSONObject();
        JSONObject retJobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            int moduleid = Integer.parseInt(requestParams.get("moduleid").toString());
            String templateid = requestParams.get("templateid").toString();
            String json = "[]";
            String html = "";
            String sqlQuery;
            String templateSubtype;
            JSONObject custJson = new JSONObject();
            KwlReturnObject result = customDesignDAOObj.getDesignTemplate(templateid);
            List list = result.getEntityList(); 
            Object[] designtemplaterows = (Object[]) list.get(0);
            jobj.put("templatename", designtemplaterows[15]);
            jobj.put("moduleid", moduleid);
            custJson.put("moduleid", moduleid);
            if (designtemplaterows[1] != null) {
                json = designtemplaterows[1].toString();
            }
            custJson.put("JSON", json);
            jobj.put("json", json);
            jobj.put("pagelayoutproperty", designtemplaterows[4]);
            if (designtemplaterows[6] != null) {
                json = designtemplaterows[6].toString();
            }
            custJson.put("footerjson", json);
            jobj.put("footerjson", json);
            if (designtemplaterows[10] != null) {
                json = designtemplaterows[10].toString();
            }
            custJson.put("footerjson", json);
            jobj.put("headerjson", json);
            if (designtemplaterows[0] != null) {
                html = designtemplaterows[0].toString();
            } else {
                html = "";
            }
            jobj.put("html", html);
            if (designtemplaterows[9] != null) {
                html = designtemplaterows[9].toString();
            } else {
                html = "";
            }
            jobj.put("headerhtml", html);
            if (designtemplaterows[5] != null) {
                html = designtemplaterows[5].toString();
            } else {
                html = "";
            }
            jobj.put("footerhtml", html);
            if (designtemplaterows[3] != null) {
                sqlQuery = designtemplaterows[3].toString();
            } else {
                sqlQuery = "";
            }
            jobj.put("sqlquery", sqlQuery);
            if (designtemplaterows[11] != null) {
                sqlQuery = designtemplaterows[11].toString();
            } else {
                sqlQuery = "";
            }
            jobj.put("headersqlquery", sqlQuery);
            if (designtemplaterows[7] != null) {
                sqlQuery = designtemplaterows[7].toString();
            } else {
                sqlQuery = "";
            }
            jobj.put("footersqlquery", sqlQuery);
            if (designtemplaterows[8] != null) {
                templateSubtype = designtemplaterows[8].toString();
            } else {
                templateSubtype = "";
            }
            jobj.put("templatesubtype", templateSubtype);
            JSONObject customFieldJson = getCustomColumnFromTemplateIfAny(custJson,requestParams);
            JSONObject customFieldJson1 = new JSONObject();
            customFieldJson1.put("data", customFieldJson.optJSONArray("data") != null ? customFieldJson.optJSONArray("data") : "");
            jobj.put("customColJson", customFieldJson1.toString());
            jobj.put("image",customFieldJson.optString("imgData",""));
            jarr.put(jobj);
            retJobj.put("data", jarr);
        } catch(Exception ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return  retJobj;
    }
    
    @Override
    public void ValidateHeadersCustomTemplates(JSONObject paramJobj, JSONArray validateJArray) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add(messageSource.getMessage("acc.designerTemplateName", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerModuleID", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add("JSON");
            list.add(messageSource.getMessage("acc.designerPageLayoutProperty", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerFooterJSON", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerHeaderJSON", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerHTML", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerHeaderHTML", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerFooterHTML", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerSQLQuery", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerHeaderSQLQuery", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
            list.add(messageSource.getMessage("acc.designerFooterSQLQuery", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));

            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " " + messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccDocumentDesignServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
   
    
     
}
