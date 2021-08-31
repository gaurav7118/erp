/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customDesign;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import static com.krawler.spring.accounting.customDesign.CustomDesignerConstants.CustomDesignExtraFieldsForRequestForQuotationIndia;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class CustomDesignServiceImpl implements CustomDesignServiceDao{
    private CustomDesignDAO customDesignDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private HibernateTransactionManager txnManager;
    public ImportHandler importHandler;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;

    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    @Override
    public JSONObject getDesignTemplateList(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String[] moduleidStr = new String[100];
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap,false) == true) {
                moduleidStr = paramJobj.optString(Constants.moduleArray).split(",");
            } else {
                moduleidStr = (String[]) paramJobj.opt(Constants.moduleArray);
            }
            JSONArray templateArr = new JSONArray();
            String start = paramJobj.optString("start");
            String limit = paramJobj.optString("limit");
            String isActive = (paramJobj.optString("isActive") == null) ? "" : paramJobj.optString("isActive");
            int tcount = 0;
            String countryid = new String();
            String stateid = new String();
            KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyObj = (Company) companyresult.getEntityList().get(0);
            if (companyObj.getCountry() != null) {
                countryid = companyObj.getCountry().getID();
            }
            if (companyObj.getState() != null) {
                stateid = companyObj.getState().getID();
            }
            
            String sort = paramJobj.optString("sort","");
            String dir = paramJobj.optString("dir","");
            for (int i = 0; i < moduleidStr.length; i++) {
                int moduleid = Integer.parseInt(moduleidStr[i]);
                String ss = "";
                ss = StringUtil.isNullOrEmpty(paramJobj.optString("ss", null)) ? "" : paramJobj.optString("ss");
                KwlReturnObject result = customDesignDAOObj.getDesignTemplateList(companyid, moduleid, ss, start, limit, isActive, countryid, stateid,sort,dir);
                List list = result.getEntityList();
                tcount = result.getRecordTotalCount();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("templateid", row[0]);
                    tempObj.put("templatename", row[1]);
                    tempObj.put("createdby", row[2]);
                    tempObj.put("createdon", row[3]);    //SDP-4325 : Do not send long value to UI Side.
                    tempObj.put("isdefault", row[4]);
                    tempObj.put("moduleid", row[5]);
                    String moduleName = "";
                    try {
                        moduleName = Constants.moduleID_NameMap.get(Integer.parseInt(row[5].toString()));
                        if (StringUtil.isNullOrEmpty(moduleName)) {
                            moduleName = "None";
                        }
                    } catch (Exception ex) {
                        moduleName = "None";
                    }
                    tempObj.put("moduleName", moduleName);
                    tempObj.put("templatesubtype", row[6]);
                    tempObj.put("isnewdesign", row[7]);
                    tempObj.put("isdefaulttemplate", row[8]);
                    tempObj.put("updatedon", row[9]);
                    templateArr.put(tempObj);
                }
            }

            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap,false) == true) {
                jobj.put("totalCount", templateArr.length());
            } else {
                jobj.put("totalCount", tcount);
            }

            jobj.put(Constants.RES_data, templateArr);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject getDesignTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        String postText=""; 
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
            String templateid = paramJobj.optString("templateid", null);
            String bandID = paramJobj.optString("bandID", null);
            String json = "[]", summarytable = "";
            String html = "";
            JSONObject LineItemSummaryTableInfo = new JSONObject();
            String pagefooterjson = "[]", pageheaderjson = "[]";
            String templatesubtype = "";
            boolean isLineItemConfigured = false;
            int quantitydigitafterdecimal = 0;
            int amountdigitafterdecimal = 0;
            int unitpricedigitafterdecimal = 0;
            int isnewdesign = 0;
            int countryid = 0;
            KwlReturnObject companyobj = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyid);
            if (companyobj != null) {
                Company companydata = (Company) companyobj.getEntityList().get(0);
                if (companydata != null && companydata.getCountry() != null && !StringUtil.isNullOrEmpty(companydata.getCountry().getID())) {
                    countryid = Integer.parseInt(companydata.getCountry().getID());
                }
            }

            KwlReturnObject cpresult = customDesignDAOObj.getCompanyDetails(companyid);
            List cplist = cpresult.getEntityList();
            Object[] decimalValuesobj = (Object[]) cplist.get(0);
            if (decimalValuesobj != null) {
                if (decimalValuesobj[0] != null) {
                    quantitydigitafterdecimal = Integer.parseInt(decimalValuesobj[0].toString());
                }
                if (decimalValuesobj[1] != null) {
                    amountdigitafterdecimal = Integer.parseInt(decimalValuesobj[1].toString());
                }
                if (decimalValuesobj[2] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalValuesobj[2].toString());
                }
            }
            KwlReturnObject result = customDesignDAOObj.getDesignTemplate(templateid);
            List list = result.getEntityList();
            Object[] designtemplaterows = (Object[]) list.get(0);
            int count = result.getRecordTotalCount();
            JSONObject returnObj = new JSONObject();
            TreeMap<String, String> lineCols = new TreeMap<String, String>();
            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_body)) {
                returnObj.put("html", designtemplaterows[0]);
                html = (String) designtemplaterows[0];
                if (designtemplaterows[1] != null) {
                    json = designtemplaterows[1].toString();
                }
                returnObj.put("json", json);
            }
            returnObj.put("pagelayoutproperty", designtemplaterows[4]);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_footer)) {
                returnObj.put("pagefooterhtml", designtemplaterows[5]);
                if (designtemplaterows[6] != null) {
                    pagefooterjson = designtemplaterows[6].toString();
                }
                returnObj.put("pagefooterjson", pagefooterjson);
                json = pagefooterjson;
            }
            templatesubtype = designtemplaterows[8] != null ? designtemplaterows[8].toString() : "0";
            returnObj.put("templatesubtype", templatesubtype);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_header)) {
                returnObj.put("pageheaderhtml", designtemplaterows[9]);
                if (designtemplaterows[10] != null) {
                    pageheaderjson = designtemplaterows[10].toString();
                }
                returnObj.put("pageheaderjson", pageheaderjson);
                json = pageheaderjson;
            }

            if (designtemplaterows[13] != null) {
                isnewdesign = Integer.parseInt(designtemplaterows[13].toString());
            }
            /*
             * get default header
             */
            JSONArray defaulhHeaderArr = new JSONArray();
            JSONArray customfieldArr = new JSONArray();//json array for custom fields
            defaulhHeaderArr = fetchFieldsWithModule(defaulhHeaderArr, moduleid, companyid);

            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("companyid", companyid);
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                hm.put("salesOrPurchaseFlag", true);
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                hm.put("salesOrPurchaseFlag", false);
            }
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId
                    || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId
                    || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                defaulhHeaderArr = fetchSummaryTermFields(defaulhHeaderArr, hm);
            }


            //For total Amount,Subtotal,Total Tax,Total Discount,Amount in words entry in Default Header
            HashMap<String, String> amountcols = null;
            HashMap<String, String> cndnAmountcols = null;
            List<JSONObject> amountcolList = new ArrayList();
            if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) { //27 - Delivery order, 28- Goods receipts 
                amountcols = LineItemColumnModuleMapping.GROReportDOReportProductSummaryItems;
            } else if (moduleid != Constants.Acc_Debit_Note_ModuleId && (moduleid != Constants.Acc_Credit_Note_ModuleId && !templatesubtype.contains("1")) && moduleid != Constants.Acc_Make_Payment_ModuleId
                    && moduleid != Constants.Acc_Receive_Payment_ModuleId && moduleid != Constants.Acc_Stock_Request_ModuleId && moduleid != Constants.Inventory_ModuleId && moduleid != Constants.Acc_Stock_Adjustment_ModuleId
                    && moduleid != Constants.Acc_InterStore_ModuleId && moduleid != Constants.Acc_RFQ_ModuleId && moduleid != Constants.Acc_InterLocation_ModuleId && moduleid != Constants.Acc_Purchase_Requisition_ModuleId
                    && moduleid != Constants.Acc_Customer_AccStatement_ModuleId && moduleid != Constants.Acc_Vendor_AccStatement_ModuleId && moduleid != Constants.Build_Assembly_Module_Id && moduleid != Constants.MRP_WORK_ORDER_MODULEID
                    && moduleid != Constants.Bank_Reconciliation_ModuleId) {
                amountcols = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
            }
            if (amountcols != null) {
                for (Map.Entry<String, String> amountentry : amountcols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    staticamountInfo.put("id", amountentry.getKey());
                    staticamountInfo.put("fieldid", amountentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    staticamountInfo.put("isNumeric", staticamountInfo.optBoolean("isNumeric",false));
                    amountcolList.add(staticamountInfo);
                    defaulhHeaderArr.put(staticamountInfo);
                }
            }
            // Total of columns only for CN and DN fields
            if (cndnAmountcols != null) {
                for (Map.Entry<String, String> amountentry : cndnAmountcols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    staticamountInfo.put("fieldid", amountentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    staticamountInfo.put("isNumeric", staticamountInfo.optBoolean("isNumeric",false));
                    staticamountInfo.put("id", amountentry.getKey());
                    amountcolList.add(staticamountInfo);
                    defaulhHeaderArr.put(staticamountInfo);
                }
            }

            HashMap<String, String> extraCols = null;
            HashMap<String, String> dbcustomCols = new HashMap<String, String>();

            /*
             * get line item columns configuration
             */

            TreeMap<String, String> defaultlineCols = null;
            TreeMap<String, String> baseModuletoOtherMap = null;
            TreeMap<String, String> ageingColsMap = null;
            TreeMap<String, String> ageingCols = new TreeMap<String, String>();
            TreeMap<String, String> productitemsplaceholder = null;

            HashMap<String, TreeMap<String, String>> lineItemFieldsMap = getLineItemFieldsMap(moduleid, templatesubtype, countryid);

            defaultlineCols = lineItemFieldsMap.containsKey("defaultlineCols") ? lineItemFieldsMap.get("defaultlineCols") : null;
            baseModuletoOtherMap = lineItemFieldsMap.containsKey("baseModuletoOtherMap") ? lineItemFieldsMap.get("baseModuletoOtherMap") : null;
            ageingColsMap = lineItemFieldsMap.containsKey("ageingColsMap") ? lineItemFieldsMap.get("ageingColsMap") : null;

            if (defaultlineCols != null) {
                lineCols = (TreeMap<String, String>) defaultlineCols.clone();
            }
            if (ageingColsMap != null) {
                ageingCols = (TreeMap<String, String>) ageingColsMap.clone();
            }
            if (baseModuletoOtherMap != null) {
                TreeMap<String, String> moduletoOtherMap = (TreeMap<String, String>) baseModuletoOtherMap.clone();
                if (moduletoOtherMap != null) {
                    for (Map.Entry<String, String> mapModule : moduletoOtherMap.entrySet()) {
                        int mapModuleId = StringUtil.getInteger(mapModule.getValue());
                        defaulhHeaderArr = fetchFieldsWithModule(defaulhHeaderArr, mapModuleId, companyid);
                        customfieldArr = fetchCustomFieldsWithModule(customfieldArr, mapModuleId, companyid,postText);
                    }
                }
            }
            //inserting product items 
            if (productitemsplaceholder != null) {
                for (Map.Entry<String, String> amountentry : productitemsplaceholder.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    staticamountInfo.put("fieldid", amountentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    staticamountInfo.put("isNumeric", staticamountInfo.optBoolean("isNumeric",false));
                    amountcolList.add(staticamountInfo);
                    defaulhHeaderArr.put(staticamountInfo);
                }
            }

            if (templatesubtype.equals("1")) {      //for consignment case
                if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    moduleid = Constants.Acc_ConsignmentRequest_ModuleId;
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    moduleid = Constants.Acc_ConsignmentDeliveryOrder_ModuleId;
                    TreeMap<String, String> diffdbCols = null;
                    diffdbCols = CustomDesignerConstants.ConsignmentDOtoOtherMap;
                    if (diffdbCols != null) {
                        TreeMap<String, String> moduletoOtherMap = (TreeMap<String, String>) diffdbCols.clone();
                        if (moduletoOtherMap != null) {
                            for (Map.Entry<String, String> mapModule : moduletoOtherMap.entrySet()) {
                                int mapModuleId = StringUtil.getInteger(mapModule.getValue());
                                customfieldArr = fetchCustomFieldsWithModule(customfieldArr, mapModuleId, companyid,postText);
                            }
                        }
                    }
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    moduleid = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    moduleid = Constants.Acc_ConsignmentInvoice_ModuleId;
                } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                    /*
                     *Get Custom Fields for Credit note first.   
                     */
                    customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyid, CustomDesignerConstants.CUSTOM_POST_TEXT_CN);
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                } else if (moduleid == Constants.Acc_Debit_Note_ModuleId) {
                    /*
                     *Get Custom Fields for Debit note first.   
                     */
                    customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyid, CustomDesignerConstants.CUSTOM_POST_TEXT_DN);
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                } else if(moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                    moduleid = Constants.Acc_Consignment_GoodsReceipt_ModuleId;
                } else if(moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    moduleid = Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
                }
            } else if(templatesubtype.equals("2")){
                if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    moduleid = Constants.LEASE_INVOICE_MODULEID;
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    moduleid = Constants.Acc_Lease_DO;
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    moduleid = Constants.Acc_Lease_Return;
                } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    moduleid = Constants.Acc_Lease_Order_ModuleId;
                } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                    moduleid = Constants.Acc_Lease_Quotation;
                }
            } else if(templatesubtype.equals(CustomDesignerConstants.ASSET)){
                //If template is of asset type then update module id to asset module id
                if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_Purchase_Order_ModuleId;
                } else if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId;
                } else if (moduleid == Constants.Acc_RFQ_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_RFQ_ModuleId; 
                } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId;
                } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_DisposalInvoice_ModuleId;
                } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_GoodsReceipt_ModuleId;
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_DeliveryOrder_ModuleId;
                } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_Purchase_Return_ModuleId;
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    moduleid = Constants.Acc_FixedAssets_Sales_Return_ModuleId;
                } 
            }

            extraCols = getExtraFieldsForModule(moduleid, countryid,templatesubtype);
            /**
             * Get extra fields related template subtype
             */
            HashMap<String, String> subtypExtraFieldsMap = null;
            subtypExtraFieldsMap = CustomDesignHandler.getExtraFieldsForSubtype(moduleid, templatesubtype,  countryid);
            if(extraCols != null && subtypExtraFieldsMap != null){
                extraCols.putAll(subtypExtraFieldsMap);
                extraCols = (HashMap<String, String>) extraCols.clone();
            }
            
            if (moduleid == Constants.Acc_Credit_Note_ModuleId || moduleid == Constants.Acc_Debit_Note_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId) {//Inserting line level customfields or dimensions in global section 
                extraCols = getdbCustomFieldColumns(moduleid, companyid, extraCols);
            }

            if (extraCols != null) {
                for (Map.Entry<String, String> extraColsEntry : extraCols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                    staticamountInfo.put("fieldid", extraColsEntry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    staticamountInfo.put("id", extraColsEntry.getKey());
                    amountcolList.add(staticamountInfo);
                    defaulhHeaderArr.put(staticamountInfo);
                }
            }

            defaulhHeaderArr = CustomDesignHandler.sortJsonArrayOnFieldNames(defaulhHeaderArr);//Sorting fields by fiels name
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId
                    || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId
                    || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                JSONObject jSONObject = new JSONObject();//For createing approval section
                jSONObject.put("fieldid", "");
                jSONObject.put("id", "NA");
                jSONObject.put("label", "<font color=gray>--------[Approver levels]--------</font>");
                jSONObject.put("xtype", "NAN");
                defaulhHeaderArr.put(jSONObject);
                for (int level = 1; level <= 10; level++) {
                    JSONObject staticamountInfo = new JSONObject();
                    staticamountInfo.put("fieldid", Constants.ApproverLevel + level);
                    staticamountInfo.put("label", Constants.ApproverLevel + level);
                    staticamountInfo.put("xtype", 1);
                    staticamountInfo.put("id", Constants.ApproverLevel + level);
                    defaulhHeaderArr.put(staticamountInfo);
                    // If module is make payment then put approved date field for all level
                    if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                        JSONObject approvedDateObj = new JSONObject();
                        approvedDateObj.put("fieldid", Constants.APPROVED_DATE_LEVEL + level);
                        approvedDateObj.put("label", Constants.APPROVED_DATE_LEVEL + level);
                        approvedDateObj.put("xtype", 3);
                        approvedDateObj.put("id", Constants.APPROVED_DATE_LEVEL + level);
                        defaulhHeaderArr.put(approvedDateObj);
                    }
                }
            }

            customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyid,postText);//fetching customfields for original moduleid
            if (customfieldArr.length() > 1) {//putting all custom fields in defaultheader array
                for (int i = 0; i < customfieldArr.length(); i++) {
                    JSONObject customfieldobject = new JSONObject();
                    customfieldobject = customfieldArr.getJSONObject(i);
                    defaulhHeaderArr.put(customfieldobject);
                }

            }
            /*
             * find out configured line items and store it in map to configure
             * json
             */
            TreeMap<String, JSONObject> customisedLineItems = new TreeMap<String, JSONObject>();
            TreeMap<String, JSONObject> columndata = new TreeMap<String, JSONObject>();
            if (!StringUtil.isNullOrEmpty(json)) {
                JSONArray jUserLineConfArr = new JSONArray();
                JSONArray columndataArr = new JSONArray();
                String summaryTable = "";
                JSONArray jUserConfArr = new JSONArray(json);
                if (isnewdesign == 1) { //new design
                    HashMap<String, Object> lineitemmap = CustomDesignHandler.getLineItemsDetails(jUserConfArr);
                    if (lineitemmap.containsKey(Constants.Customedlineitems) && lineitemmap.get(Constants.Customedlineitems) != null) {
                        jUserLineConfArr = (JSONArray) lineitemmap.get(Constants.Customedlineitems);
                    }
                    if (lineitemmap.containsKey(Constants.isLineItemPresent) && lineitemmap.get(Constants.isLineItemPresent) != null) {
                        isLineItemConfigured = (Boolean) lineitemmap.get(Constants.isLineItemPresent);
                    }
                    if (lineitemmap.containsKey("columndata") && lineitemmap.get("columndata") != null) {
                        columndataArr = (JSONArray) lineitemmap.get("columndata");
                    }
                    if (lineitemmap.containsKey("LineItemSummaryTableInfo") && lineitemmap.get("LineItemSummaryTableInfo") != null) {
                        LineItemSummaryTableInfo = new JSONObject(lineitemmap.get("LineItemSummaryTableInfo").toString());
                        if (LineItemSummaryTableInfo.has("html") && !StringUtil.isNullOrEmpty(LineItemSummaryTableInfo.getString("html"))) {
                            Document jsoupDoc = Jsoup.parse(html);
                            org.jsoup.nodes.Element summaryTableElement = jsoupDoc.getElementById("summaryTableID");
                            if (summaryTableElement != null) {
                                LineItemSummaryTableInfo.put("html", summaryTableElement.outerHtml());
                            }
                            returnObj.put("summaryTable", LineItemSummaryTableInfo);
                        }
                    }
                } else { //old design
                    for (int cnt = 0; cnt < jUserConfArr.length(); cnt++) {
                        JSONObject tempjObj = jUserConfArr.getJSONObject(cnt);
                        if (!StringUtil.isNullOrEmpty(tempjObj.optString("lineitems", ""))) {
                            jUserLineConfArr = new JSONArray(tempjObj.optString("lineitems", "[]"));
                            isLineItemConfigured = true;
                            break;
                        }
                    }
                }
                for (int cnt = 0; cnt < jUserLineConfArr.length(); cnt++) {
                    JSONObject lineObj = jUserLineConfArr.getJSONObject(cnt);
                    customisedLineItems.put(lineObj.optString("fieldid", ""), lineObj);
                }
                for (int cnt = 0; cnt < columndataArr.length(); cnt++) {
                    JSONObject lineObj = columndataArr.getJSONObject(cnt);
                    columndata.put(lineObj.optString("fieldid", ""), lineObj);
                }
                if (columndata.isEmpty()) {
                    columndata = customisedLineItems;
                }
            }

            /*
             * Put custom columns in lineCols map
             */
            int totalstaticCLineols = lineCols.size();
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            // select id, fieldtype, fieldlabel, colnum from fieldparams
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject temp = new JSONObject();
                temp.put("id", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                temp.put("label", row[2]);
                temp.put("xtype", row[1]);
                temp.put("defwidth", 15);
                temp.put("seq", totalstaticCLineols + cnt);
                temp.put("custom", true);
                lineCols.put("col" + row[3], temp.toString());
            }

            //Product custom field
            int totalstaticCLineols1 = lineCols.size() + 1;
            result = customDesignDAOObj.getProductCustomLineFields(companyid, Constants.Acc_Product_Master_ModuleId);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                if (row[4] != null) {
                    String relatedmoduleids[] = row[4].toString().split(",");
                    for (int ct = 0; ct < relatedmoduleids.length; ct++) {
                        if (moduleid == Integer.parseInt(relatedmoduleids[ct])) {
                            JSONObject temp = new JSONObject();
                            temp.put("id", "productcol" + row[3]);// use <productcol>colnum as id for product custom line. We used id=1 as Product Name as so on.
                            temp.put("label", row[2]);
                            temp.put("xtype", row[1]);
                            temp.put("defwidth", 15);
                            temp.put("seq", totalstaticCLineols1 + cnt);
                            temp.put("custom", true);
                            lineCols.put("productcol" + row[3], temp.toString());
                        }
                    }
                }

            }

            if (templatesubtype.equals("1")) {
                if (moduleid == Constants.Acc_ConsignmentRequest_ModuleId) {    //reassigning again moduleid to salesorder & delivery order
                    moduleid = Constants.Acc_Sales_Order_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                    moduleid = Constants.Acc_Delivery_Order_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentInvoice_ModuleId) {
                    moduleid = Constants.Acc_Invoice_ModuleId;
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    moduleid = Constants.Acc_Credit_Note_ModuleId;
                } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                    moduleid = Constants.Acc_Debit_Note_ModuleId;
                }
            }
            JSONArray arraydefaulhHeaderArr = defaulhHeaderArr;
            returnObj.put("defaultfield", arraydefaulhHeaderArr);

            if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId || moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                int totalstaticCLineols2 = lineCols.size() + 1;
                JSONObject resultObj = getCustomFieldsForSOA(companyid, moduleid);
                JSONArray resultJarr = resultObj.getJSONArray("data");
                for (int cnt = 0; cnt < resultJarr.length(); cnt++) {
                    JSONObject temp = new JSONObject();
                    temp.put("id", "custom_" + resultJarr.getJSONObject(cnt).getString("label"));
                    temp.put("label", resultJarr.getJSONObject(cnt).getString("label"));
                    temp.put("xtype", resultJarr.getJSONObject(cnt).getString("xtype"));
                    temp.put("defwidth", 15);
                    temp.put("seq", totalstaticCLineols2 + cnt);
                    temp.put("custom", true);
                    lineCols.put("custom_" + resultJarr.getJSONObject(cnt).getString("label"), temp.toString());
                }
            }

            /*
             * prepare line item json
             */
            JSONArray linecolArr;
            List<JSONObject> linecolList = new ArrayList();
            linecolArr = new JSONArray();
            int defcolno = 1;
            if (lineCols != null) {
                for (Map.Entry<String, String> entry : lineCols.entrySet()) {
                    JSONObject staticColInfo = new JSONObject(entry.getValue());
                    JSONObject customColInfo = null;
                    boolean isLineCustomized = false;
                    if (columndata.containsKey(entry.getKey())) {
                        isLineCustomized = true;
                        customColInfo = columndata.get(entry.getKey());
                    }
                    staticColInfo.put("fieldid", entry.getKey());
                    if (staticColInfo.has("custom") && staticColInfo.getBoolean("custom")) {
                        staticColInfo.put("fieldid", "Custom_" + staticColInfo.get("label"));
                    }
                    if (isnewdesign == 1) {// For new design
                        staticColInfo.put("hidecol", isLineCustomized ? (customColInfo.has("hidecol") && !StringUtil.isNullOrEmpty(customColInfo.getString("hidecol"))) ? customColInfo.getBoolean("hidecol") : false : (isLineItemConfigured ? true : staticColInfo.optBoolean("defaulthiddenfield", true)));// we stored columns which are not hide in db json
                    } else {
                        staticColInfo.put("hidecol", isLineCustomized ? (customColInfo.has("hidecol") && !StringUtil.isNullOrEmpty(customColInfo.getString("hidecol"))) ? customColInfo.getBoolean("hidecol") : false : (isLineItemConfigured ? true : false));// we stored columns which are not hide in db json
                    }
                    staticColInfo.put("coltotal", isLineCustomized ? customColInfo.get("coltotal") : staticColInfo.get("xtype").toString().equals("2") ? true : false);
                    if(staticColInfo.optString("label","").equalsIgnoreCase("Quantity With UOM") || staticColInfo.optString("label","").equalsIgnoreCase("UOM")){
                        staticColInfo.put("showtotal", false);
                    } else {
                        staticColInfo.put("showtotal", isLineCustomized ? (customColInfo.optString("showtotal", "false")) : (isLineItemConfigured ? true : false));
                    }
                    staticColInfo.put("headercurrency", isLineCustomized ? (customColInfo.optString("headercurrency", "true")) : customColInfo != null ? customColInfo.optString("headercurrency", "false") : "");
                    staticColInfo.put("recordcurrency", isLineCustomized ? (customColInfo.optString("recordcurrency", "true")) : customColInfo != null ? customColInfo.optString("recordcurrency", "false") : "");
                    staticColInfo.put("commaamount", isLineCustomized ? (customColInfo.optString("commaamount", "true")) : customColInfo != null ? customColInfo.optString("commaamount", "false") : "");
                    staticColInfo.put("colwidth", isLineCustomized ? customColInfo.opt("colwidth") : staticColInfo.opt("defwidth"));

                    if (!staticColInfo.getBoolean("hidecol")) {
                        staticColInfo.put("colno", isLineCustomized ? customColInfo.optInt("colno", defcolno) : defcolno);
                        defcolno++;
                    }
//                    if (staticColInfo.get("xtype").toString().equals("2")) {
                    if (staticColInfo.has("basequantity") || staticColInfo.has("baserate") || staticColInfo.has("baseamount") || staticColInfo.has("basequantitywithuom")) {
                        if (customColInfo != null && customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("undefined")) {//FOR CUSTOM field check and decimal undefined check.
                            if ((staticColInfo.has("basequantity") && staticColInfo.get("basequantity").equals(true))) {
                                staticColInfo.put("decimalpoint", (isLineCustomized) ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : quantitydigitafterdecimal) : quantitydigitafterdecimal);
                            }
                            if (staticColInfo.has("baserate") && staticColInfo.get("baserate").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : unitpricedigitafterdecimal) : unitpricedigitafterdecimal);
                                staticColInfo.put("baserate", true);
                            }
                            if (staticColInfo.has("baseamount") && staticColInfo.get("baseamount").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : amountdigitafterdecimal) : amountdigitafterdecimal);
                            }
                            if (staticColInfo.has("basequantitywithuom") && staticColInfo.get("basequantitywithuom").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : quantitydigitafterdecimal) : quantitydigitafterdecimal);
                                staticColInfo.put("basequantitywithuom", true);
                            }
                        } else {
                            staticColInfo.put("decimalpoint", "");
                        }
                    } else {
                        staticColInfo.put("decimalpoint", "");
                    }
                    staticColInfo.put("seq", isLineCustomized ? customColInfo.optInt("seq", 0) : staticColInfo.optInt("seq", 0));
                    staticColInfo.put("columnname", staticColInfo.opt("label"));
                    staticColInfo.put("xtype", staticColInfo.opt("xtype"));
                    staticColInfo.put("displayfield", isLineCustomized ? customColInfo.has("displayfield") ? customColInfo.opt("displayfield") : customColInfo.opt("label") : staticColInfo.opt("label"));
                    staticColInfo.put("headerproperty", customColInfo != null ? customColInfo.optString("headerproperty", "") : "");
                    //                    customisedSortedLineItems.p
                    linecolList.add(staticColInfo);
                }
                jsonComp comp = new jsonComp();
                Collections.sort(linecolList, comp);
            }
            if (linecolList.size() > 0) {
                for (JSONObject customColInfo : linecolList) {
                    linecolArr.put(customColInfo);
                }
            }
            returnObj.put("linecolumns", linecolArr);

            JSONArray ageingcolArr = new JSONArray();
            List<JSONObject> ageingcolList = new ArrayList();



            if (ageingCols != null) {
                for (Map.Entry<String, String> entry : ageingCols.entrySet()) {
                    JSONObject staticColInfo = new JSONObject(entry.getValue());
                    staticColInfo.put("fieldid", entry.getKey());
                    staticColInfo.put("colwidth", (staticColInfo.optString("label", "")).equalsIgnoreCase("Interval") ? 60 : 20);
                    staticColInfo.put("columnname", staticColInfo.opt("label"));
                    staticColInfo.put("xtype", staticColInfo.opt("xtype"));
                    staticColInfo.put("displayfield", staticColInfo.opt("label"));
                    ageingcolList.add(staticColInfo);
                }
                jsonComp comp = new jsonComp();
                Collections.sort(ageingcolList, comp);
            }
            if (ageingcolList.size() > 0) {
                for (JSONObject ageingColInfo : ageingcolList) {
                    ageingcolArr.put(ageingColInfo);
                }
            }

            returnObj.put("ageingcolumns", ageingcolArr);
            JSONArray dimensionAndCustomColumnArr = new JSONArray();
            HashMap<String, Object> otherrequestParams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);//User Date Formatter
            otherrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            otherrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid));
            KwlReturnObject rs = customDesignDAOObj.getFieldParams(otherrequestParams);
            List lst = rs.getEntityList();
            if (lst.size() > 0) {
                for (Iterator it = lst.iterator(); it.hasNext();) {
                    FieldParams dimAndCustom = (FieldParams) it.next();
                    JSONObject dimjson = new JSONObject();
                    dimjson.put("fieldname", dimAndCustom.getFieldname());
                    dimjson.put("fieldlabel", dimAndCustom.getFieldlabel());
                    dimjson.put("iscustom", dimAndCustom.getCustomfield() == 0 ? false : true);
                    dimjson.put("isline", dimAndCustom.getCustomcolumn() == 0 ? false : true);
                    dimjson.put("customlabel", dimAndCustom.getFieldlabel());
                    dimensionAndCustomColumnArr.put(dimjson);
                }
            }
            returnObj.put("dimensionandcustom", dimensionAndCustomColumnArr);
            /*
             * End
             */

            jobj.put(Constants.RES_data, new JSONArray().put(returnObj));
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    @Override
    public JSONObject getJobOrderDesignTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        String postText="";
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
            String templateid = paramJobj.optString("templateid", null);
            String bandID = paramJobj.optString("bandID", null);
            String json = "[]";
            String pagefooterjson = "[]", pageheaderjson = "[]";
            String templatesubtype = "";
            KwlReturnObject result = customDesignDAOObj.getDesignTemplate(templateid);
            List list = result.getEntityList();
            Object[] designtemplaterows = (Object[]) list.get(0);
            JSONObject returnObj = new JSONObject();
            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_body)) {
                returnObj.put("html", designtemplaterows[0]);
                if (designtemplaterows[1] != null) {
                    json = designtemplaterows[1].toString();
                }
                returnObj.put("json", json);
            }
            returnObj.put("pagelayoutproperty", designtemplaterows[4]);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_footer)) {
                returnObj.put("pagefooterhtml", designtemplaterows[5]);
                if (designtemplaterows[6] != null) {
                    pagefooterjson = designtemplaterows[6].toString();
                }
                returnObj.put("pagefooterjson", pagefooterjson);
                json = pagefooterjson;
            }
            templatesubtype = designtemplaterows[8] != null ? designtemplaterows[8].toString() : "0";
            returnObj.put("templatesubtype", templatesubtype);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_header)) {
                returnObj.put("pageheaderhtml", designtemplaterows[9]);
                if (designtemplaterows[10] != null) {
                    pageheaderjson = designtemplaterows[10].toString();
                }
                returnObj.put("pageheaderjson", pageheaderjson);
                json = pageheaderjson;
            }

            /*
             * get default header
             */
            JSONArray defaulhHeaderArr = new JSONArray();//json array for default fields
            JSONArray customfieldArr = new JSONArray();//json array for custom fields
            HashMap<String, String> fieldMap = new HashMap<String, String>();
            
            // Add Specific fields to map
            if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                fieldMap = CustomDesignerConstants.CustomDesignSO_JobOrderFlowExtraFieldsMap;
            } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                fieldMap = CustomDesignerConstants.CustomDesignSI_JobOrderFlowExtraFieldsMap;
            }
            
            // Add common fields to map
            fieldMap.putAll(CustomDesignerConstants.CustomDesignCommon_JobOrderFlowExtraFieldsMap);
            
            // Put all fields to default header array
            for (Map.Entry<String, String> fieldentry : fieldMap.entrySet()) {
                JSONObject staticamountInfo = new JSONObject(fieldentry.getValue());
                staticamountInfo.put("fieldid", fieldentry.getKey());
                staticamountInfo.put("id", fieldentry.getKey());
                staticamountInfo.put("label", staticamountInfo.get("label"));
                staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                defaulhHeaderArr.put(staticamountInfo);
            }

            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("companyid", companyid);
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                hm.put("salesOrPurchaseFlag", true);
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                hm.put("salesOrPurchaseFlag", false);
            }
            
            /*
             * Put global custom fields
             */
            customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyid,postText);//fetching customfields for original moduleid
            if (customfieldArr.length() > 1) {//putting all custom fields in defaultheader array
                for (int i = 0; i < customfieldArr.length(); i++) {
                    JSONObject customfieldobject = new JSONObject();
                    customfieldobject = customfieldArr.getJSONObject(i);
                    defaulhHeaderArr.put(customfieldobject);
                }
            }
            
            /*
             * Put line custom columns in lineCols map
             */
            TreeMap<String, String> lineCols = new TreeMap<String, String>();
            JSONArray linecolArr;
            linecolArr = new JSONArray();
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            // select id, fieldtype, fieldlabel, colnum from fieldparams
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject staticamountInfo = new JSONObject();
                staticamountInfo.put("fieldid", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                staticamountInfo.put("id", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                staticamountInfo.put("label", row[2]);
                staticamountInfo.put("xtype", row[1]);
                staticamountInfo.put("custom", true);
                linecolArr.put(staticamountInfo);
                defaulhHeaderArr.put(staticamountInfo);
            }
            
            returnObj.put("linecolumns", linecolArr);
            returnObj.put("defaultfield", defaulhHeaderArr);
            
            jobj.put(Constants.RES_data, new JSONArray().put(returnObj));
            issuccess = true;     
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    /**
     * Get QA Approval template details with all global and line level default and custom fields
     * @param paramJobj
     * @return 
     */
    @Override
    public JSONObject getQAApprovalDesignTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String postText="";
        String msg = null;
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
            String templateid = paramJobj.optString("templateid", null);
            String bandID = paramJobj.optString("bandID", null);
            String json = "[]";
            String html = "";
            String pagefooterjson = "[]", pageheaderjson = "[]";
            String templatesubtype = "";
            boolean isLineItemConfigured = false;
            int isnewdesign = 0;
            int quantitydigitafterdecimal = 0;
            int amountdigitafterdecimal = 0;
            int unitpricedigitafterdecimal = 0;
            JSONObject LineItemSummaryTableInfo = new JSONObject();
            
            KwlReturnObject cpresult = customDesignDAOObj.getCompanyDetails(companyid);
            List cplist = cpresult.getEntityList();
            Object[] decimalValuesobj = (Object[]) cplist.get(0);
            if (decimalValuesobj != null) {
                if (decimalValuesobj[0] != null) {
                    quantitydigitafterdecimal = Integer.parseInt(decimalValuesobj[0].toString());
                }
                if (decimalValuesobj[1] != null) {
                    amountdigitafterdecimal = Integer.parseInt(decimalValuesobj[1].toString());
                }
                if (decimalValuesobj[2] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalValuesobj[2].toString());
                }
            }
            
            KwlReturnObject result = customDesignDAOObj.getDesignTemplate(templateid);
            List list = result.getEntityList();
            Object[] designtemplaterows = (Object[]) list.get(0);
            JSONObject returnObj = new JSONObject();
            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_body)) {
                returnObj.put("html", designtemplaterows[0]);
                html = (String) designtemplaterows[0];
                if (designtemplaterows[1] != null) {
                    json = designtemplaterows[1].toString();
                }
                returnObj.put("json", json);
            }
            returnObj.put("pagelayoutproperty", designtemplaterows[4]);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_footer)) {
                returnObj.put("pagefooterhtml", designtemplaterows[5]);
                if (designtemplaterows[6] != null) {
                    pagefooterjson = designtemplaterows[6].toString();
                }
                returnObj.put("pagefooterjson", pagefooterjson);
                json = pagefooterjson;
            }
            templatesubtype = designtemplaterows[8] != null ? designtemplaterows[8].toString() : "0";
            returnObj.put("templatesubtype", templatesubtype);

            if (bandID.equals(CustomDesignerConstants.CustomDesignBandID_header)) {
                returnObj.put("pageheaderhtml", designtemplaterows[9]);
                if (designtemplaterows[10] != null) {
                    pageheaderjson = designtemplaterows[10].toString();
                }
                returnObj.put("pageheaderjson", pageheaderjson);
                json = pageheaderjson;
            }
            if (designtemplaterows[13] != null) {
                isnewdesign = Integer.parseInt(designtemplaterows[13].toString());
            }
            /*
             * get default header
             */
            JSONArray defaulhHeaderArr = new JSONArray();//json array for default fields
            JSONArray customfieldArr = new JSONArray();//json array for custom fields
            HashMap<String, String> fieldMap = new HashMap<String, String>();
            
            if(moduleid == Constants.Acc_QA_APPROVAL_MODULE_ID){
                switch(templatesubtype){
                    case "0":
                        moduleid = Constants.Acc_Delivery_Order_ModuleId;
                        fieldMap = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                        fieldMap.putAll(CustomDesignerConstants.CustomDesignDO_QA_Approval_ExtraFieldsMap);
                        break;
                    case "1":
                        moduleid = Constants.Acc_Stock_Adjustment_ModuleId;
                        fieldMap = CustomDesignerConstants.CustomDesignStockAdjustmentExtraFieldsMap;
                        fieldMap.putAll(CustomDesignerConstants.CustomDesignStockAdjustment_QA_Approval_ExtraFieldsMap);
                        break;
                }
                fieldMap.putAll(CustomDesignerConstants.CustomDesign_QA_Inspecation_Form_ExtraFieldsMap);
            }
            
            defaulhHeaderArr = fetchFieldsWithModule(defaulhHeaderArr, moduleid, companyid);
            // Put all fields to default header array
            for (Map.Entry<String, String> fieldentry : fieldMap.entrySet()) {
                JSONObject staticamountInfo = new JSONObject(fieldentry.getValue());
                staticamountInfo.put("fieldid", fieldentry.getKey());
                staticamountInfo.put("id", fieldentry.getKey());
                staticamountInfo.put("label", staticamountInfo.get("label"));
                staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                defaulhHeaderArr.put(staticamountInfo);
            }
            /*
             * Put global custom fields
             */
            customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyid,postText);//fetching customfields for original moduleid
            if (customfieldArr.length() > 1) {//putting all custom fields in defaultheader array
                for (int i = 0; i < customfieldArr.length(); i++) {
                    JSONObject customfieldobject = new JSONObject();
                    customfieldobject = customfieldArr.getJSONObject(i);
                    defaulhHeaderArr.put(customfieldobject);
                }
            }
            /*
             * find out configured line items and store it in map to configure
             * json
             */
            TreeMap<String, JSONObject> customisedLineItems = new TreeMap<String, JSONObject>();
            TreeMap<String, JSONObject> columndata = new TreeMap<String, JSONObject>();
            if (!StringUtil.isNullOrEmpty(json)) {
                JSONArray jUserLineConfArr = new JSONArray();
                JSONArray columndataArr = new JSONArray();
                String summaryTable = "";
                JSONArray jUserConfArr = new JSONArray(json);
                if (isnewdesign == 1) { //new design
                    HashMap<String, Object> lineitemmap = CustomDesignHandler.getLineItemsDetails(jUserConfArr);
                    if (lineitemmap.containsKey(Constants.Customedlineitems) && lineitemmap.get(Constants.Customedlineitems) != null) {
                        jUserLineConfArr = (JSONArray) lineitemmap.get(Constants.Customedlineitems);
                    }
                    if (lineitemmap.containsKey(Constants.isLineItemPresent) && lineitemmap.get(Constants.isLineItemPresent) != null) {
                        isLineItemConfigured = (Boolean) lineitemmap.get(Constants.isLineItemPresent);
                    }
                    if (lineitemmap.containsKey("columndata") && lineitemmap.get("columndata") != null) {
                        columndataArr = (JSONArray) lineitemmap.get("columndata");
                    }
                    if (lineitemmap.containsKey("LineItemSummaryTableInfo") && lineitemmap.get("LineItemSummaryTableInfo") != null) {
                        LineItemSummaryTableInfo = new JSONObject(lineitemmap.get("LineItemSummaryTableInfo").toString());
                        if (LineItemSummaryTableInfo.has("html") && !StringUtil.isNullOrEmpty(LineItemSummaryTableInfo.getString("html"))) {
                            Document jsoupDoc = Jsoup.parse(html);
                            org.jsoup.nodes.Element summaryTableElement = jsoupDoc.getElementById("summaryTableID");
                            if (summaryTableElement != null) {
                                LineItemSummaryTableInfo.put("html", summaryTableElement.outerHtml());
                            }
                            returnObj.put("summaryTable", LineItemSummaryTableInfo);
                        }
                    }
                } else { //old design
                    for (int cnt = 0; cnt < jUserConfArr.length(); cnt++) {
                        JSONObject tempjObj = jUserConfArr.getJSONObject(cnt);
                        if (!StringUtil.isNullOrEmpty(tempjObj.optString("lineitems", ""))) {
                            jUserLineConfArr = new JSONArray(tempjObj.optString("lineitems", "[]"));
                            isLineItemConfigured = true;
                            break;
                        }
                    }
                }
                for (int cnt = 0; cnt < jUserLineConfArr.length(); cnt++) {
                    JSONObject lineObj = jUserLineConfArr.getJSONObject(cnt);
                    customisedLineItems.put(lineObj.optString("fieldid", ""), lineObj);
                }
                for (int cnt = 0; cnt < columndataArr.length(); cnt++) {
                    JSONObject lineObj = columndataArr.getJSONObject(cnt);
                    columndata.put(lineObj.optString("fieldid", ""), lineObj);
                }
                if (columndata.isEmpty()) {
                    columndata = customisedLineItems;
                }
            }
            /*
             * Put line custom columns in lineCols map
             */
            TreeMap<String, String> lineCols = new TreeMap<String, String>();
            JSONArray linecolArr;
            linecolArr = new JSONArray();
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            // select id, fieldtype, fieldlabel, colnum from fieldparams
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject staticamountInfo = new JSONObject();
                staticamountInfo.put("fieldid", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                staticamountInfo.put("id", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                staticamountInfo.put("label", row[2]);
                staticamountInfo.put("xtype", row[1]);
                staticamountInfo.put("custom", true);
                defaulhHeaderArr.put(staticamountInfo);
            }
            //Product custom field
            result = customDesignDAOObj.getProductCustomLineFields(companyid, Constants.Acc_Product_Master_ModuleId);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                if (row[4] != null) {
                    String relatedmoduleids[] = row[4].toString().split(",");
                    for (int ct = 0; ct < relatedmoduleids.length; ct++) {
                        if (moduleid == Integer.parseInt(relatedmoduleids[ct])) {
                            JSONObject staticamountInfo = new JSONObject();
                            staticamountInfo.put("fieldid", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                            staticamountInfo.put("id", "col" + row[3]);// use <productcol>colnum as id for product custom line. We used id=1 as Product Name as so on.
                            staticamountInfo.put("label", row[2]);
                            staticamountInfo.put("xtype", row[1]);
                            staticamountInfo.put("custom", true);
                            defaulhHeaderArr.put(staticamountInfo);
                        }
                    }
                }

            }
                        
            lineCols.putAll(LineItemColumnModuleMapping.CustomDesign_QA_Approval_LineMap);
            /*
             * prepare line item json
             */
            List<JSONObject> linecolList = new ArrayList();
            linecolArr = new JSONArray();
            int defcolno = 1;
            if (lineCols != null) {
                for (Map.Entry<String, String> entry : lineCols.entrySet()) {
                    JSONObject staticColInfo = new JSONObject(entry.getValue());
                    JSONObject customColInfo = null;
                    boolean isLineCustomized = false;
                    if (columndata.containsKey(entry.getKey())) {
                        isLineCustomized = true;
                        customColInfo = columndata.get(entry.getKey());
                    }
                    staticColInfo.put("fieldid", entry.getKey());
                    if (staticColInfo.has("custom") && staticColInfo.getBoolean("custom")) {
                        staticColInfo.put("fieldid", "Custom_" + staticColInfo.get("label"));
                    }
                    staticColInfo.put("hidecol", isLineCustomized ? (customColInfo.has("hidecol") && !StringUtil.isNullOrEmpty(customColInfo.getString("hidecol"))) ? customColInfo.getBoolean("hidecol") : false : (isLineItemConfigured ? true : staticColInfo.optBoolean("defaulthiddenfield", true)));// we stored columns which are not hide in db json
                    staticColInfo.put("coltotal", isLineCustomized ? customColInfo.get("coltotal") : staticColInfo.get("xtype").toString().equals("2") ? true : false);
                    staticColInfo.put("showtotal", isLineCustomized ? (customColInfo.optString("showtotal", "false")) : (isLineItemConfigured ? true : false));
                    staticColInfo.put("headercurrency", isLineCustomized ? (customColInfo.optString("headercurrency", "true")) : customColInfo != null ? customColInfo.optString("headercurrency", "false") : "");
                    staticColInfo.put("recordcurrency", isLineCustomized ? (customColInfo.optString("recordcurrency", "true")) : customColInfo != null ? customColInfo.optString("recordcurrency", "false") : "");
                    staticColInfo.put("commaamount", isLineCustomized ? (customColInfo.optString("commaamount", "true")) : customColInfo != null ? customColInfo.optString("commaamount", "false") : "");
                    staticColInfo.put("colwidth", isLineCustomized ? customColInfo.opt("colwidth") : staticColInfo.opt("defwidth"));

                    if (!staticColInfo.getBoolean("hidecol")) {
                        staticColInfo.put("colno", isLineCustomized ? customColInfo.optInt("colno", defcolno) : defcolno);
                        defcolno++;
                    }
                    if (staticColInfo.has("basequantity") || staticColInfo.has("baserate") || staticColInfo.has("baseamount") || staticColInfo.has("basequantitywithuom")) {
                        if (customColInfo != null && customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("undefined")) {//FOR CUSTOM field check and decimal undefined check.
                            if ((staticColInfo.has("basequantity") && staticColInfo.get("basequantity").equals(true))) {
                                staticColInfo.put("decimalpoint", (isLineCustomized) ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : quantitydigitafterdecimal) : quantitydigitafterdecimal);
                            }
                            if (staticColInfo.has("baserate") && staticColInfo.get("baserate").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : unitpricedigitafterdecimal) : unitpricedigitafterdecimal);
                                staticColInfo.put("baserate", true);
                            }
                            if (staticColInfo.has("baseamount") && staticColInfo.get("baseamount").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : amountdigitafterdecimal) : amountdigitafterdecimal);
                            }
                            if (staticColInfo.has("basequantitywithuom") && staticColInfo.get("basequantitywithuom").equals(true)) {
                                staticColInfo.put("decimalpoint", isLineCustomized ? ((customColInfo.has("decimalpoint") && !customColInfo.getString("decimalpoint").toString().equals("null") && !customColInfo.getString("decimalpoint").toString().equals("")) ? customColInfo.get("decimalpoint") : quantitydigitafterdecimal) : quantitydigitafterdecimal);
                                staticColInfo.put("basequantitywithuom", true);
                            }
                        } else {
                            staticColInfo.put("decimalpoint", "");
                        }
                    } else {
                        staticColInfo.put("decimalpoint", "");
                    }
                    staticColInfo.put("seq", isLineCustomized ? customColInfo.optInt("seq", 0) : staticColInfo.optInt("seq", 0));
                    staticColInfo.put("columnname", staticColInfo.opt("label"));
                    staticColInfo.put("xtype", staticColInfo.opt("xtype"));
                    staticColInfo.put("displayfield", isLineCustomized ? customColInfo.has("displayfield") ? customColInfo.opt("displayfield") : customColInfo.opt("label") : staticColInfo.opt("label"));
                    staticColInfo.put("headerproperty", customColInfo != null ? customColInfo.optString("headerproperty", "") : "");
                    linecolList.add(staticColInfo);
                }
                jsonComp comp = new jsonComp();
                Collections.sort(linecolList, comp);
            }
            if (linecolList.size() > 0) {
                for (JSONObject customColInfo : linecolList) {
                    linecolArr.put(customColInfo);
                }
            }
            returnObj.put("linecolumns", linecolArr);
            
            returnObj.put("defaultfield", defaulhHeaderArr);
            
            jobj.put(Constants.RES_data, new JSONArray().put(returnObj));
            issuccess = true;     
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject getCustomFieldsForSOA(String companyid, int moduleid) {
        JSONObject retJobj = new JSONObject();
        KwlReturnObject result = null;
        List list = null;
        JSONArray jArr = new JSONArray();
        try {

            result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Credit_Note_ModuleId);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject tempObj = new JSONObject();
                tempObj.put("id", row[0]);
                tempObj.put("label", row[1]);
                tempObj.put("xtype", row[2]);
                tempObj.put("customfield", true);
                jArr.put(tempObj);
            }
            result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Debit_Note_ModuleId);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject tempObj = new JSONObject();
                tempObj.put("id", row[0]);
                tempObj.put("label", row[1]);
                tempObj.put("xtype", row[2]);
                tempObj.put("customfield", true);
                jArr.put(tempObj);
            }
            if (moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Vendor_Invoice_ModuleId);
                list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", row[0]);
                    tempObj.put("label", row[1]);
                    tempObj.put("xtype", row[2]);
                    tempObj.put("customfield", true);
                    jArr.put(tempObj);
                }
                result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Make_Payment_ModuleId);
                list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", row[0]);
                    tempObj.put("label", row[1]);
                    tempObj.put("xtype", row[2]);
                    tempObj.put("customfield", true);
                    jArr.put(tempObj);
                }
            } else {
                result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Invoice_ModuleId);
                list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", row[0]);
                    tempObj.put("label", row[1]);
                    tempObj.put("xtype", row[2]);
                    tempObj.put("customfield", true);
                    jArr.put(tempObj);
                }
                result = customDesignDAOObj.getGlobalCustomFields(companyid, Constants.Acc_Receive_Payment_ModuleId);
                list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("id", row[0]);
                    tempObj.put("label", row[1]);
                    tempObj.put("xtype", row[2]);
                    tempObj.put("customfield", true);
                    jArr.put(tempObj);
                }
            }

            retJobj.put("data", jArr);
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retJobj;
    }

    @Override
    public HashMap<String, String> getExtraFieldsForModule(int moduleid, int countryid, String templatesubtype) {
        HashMap<String, String> map = null;

        switch (moduleid) {
            case Constants.LEASE_INVOICE_MODULEID://Lease Invoice
            case Constants.Acc_Invoice_ModuleId: // Invoice
                map = CustomDesignerConstants.CustomDesignInvoiceExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForInvoiceIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForInvoiceIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId: // Vendor Invoice 
                map = CustomDesignerConstants.CustomDesignVendorInvoiceExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForVendorInvoiceIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForVendorInvoiceIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Debit_Note_ModuleId://DebitNote
                map = CustomDesignerConstants.CustomDesignDebitNoteExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForCN_DN_India); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForCN_DN_India);
                }
                
                // Add extra summary fields in case of Undercharged and Overcharged CN
                if(templatesubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE) || templatesubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE) ){
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForOverChargedCN); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForOverChargedCN);
                }
                
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Credit_Note_ModuleId://CreditNOte
                map = CustomDesignerConstants.CustomDesignCreditNoteExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForCN_DN_India); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForCN_DN_India);
                }
                // Add extra summary fields in case of Undercharged and Overcharged CN
                if(templatesubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE) || templatesubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE) ){
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForOverChargedCN); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForOverChargedCN);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Make_Payment_ModuleId://14 - Make Payment;
                map = CustomDesignerConstants.CustomDesignMakePaymentNewExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                    //put Make Payment extra field map for india
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForMakeAndReceivePaymentIndia); 
                    map.putAll(CustomDesignerConstants.TDS_Field_Map);
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                    //remove Make Payment extra field map for india
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForMakeAndReceivePaymentIndia);
                    map = removeIndianExtraFields(map, CustomDesignerConstants.TDS_Field_Map);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Receive_Payment_ModuleId://16 - Receive Payment
                map = CustomDesignerConstants.CustomDesignReceivePaymentNewExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                    //put Receive Payment extra field map for india
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForMakeAndReceivePaymentIndia);
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                    //remove Receive Payment extra field map for india
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignExtraFieldsForMakeAndReceivePaymentIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Purchase_Order_ModuleId: // Purchase Order
                map = CustomDesignerConstants.CustomDesignPurchaseOrderExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForPurchaseOrderIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForPurchaseOrderIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Lease_Order_ModuleId://Lease Order
            case Constants.Acc_Sales_Order_ModuleId: // Sales Order
                map = CustomDesignerConstants.CustomDesignSalesOrderExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForSalesOrderIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForSalesOrderIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Lease_Quotation://Lease Quotation
            case Constants.Acc_Customer_Quotation_ModuleId: // Custom Quotation
                map = CustomDesignerConstants.CustomDesignCustomerQuotationExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForCustomerQuotationIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForCustomerQuotationIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId: // Vendor Quotation
                map = CustomDesignerConstants.CustomDesignVendorQuotationExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Lease_DO://Lease DO
            case Constants.Acc_Delivery_Order_ModuleId: //Delivery Order
                map = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForDeliveryOrderIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForDeliveryOrderIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Goods_Receipt_ModuleId: //Good Receipt Order
                map = CustomDesignerConstants.CustomDesignGROExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Lease_Return:// Lease Sales Return
            case Constants.Acc_Sales_Return_ModuleId://Sales Return
                map = CustomDesignerConstants.CustomDesignSalesReturnExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForSalesReturnIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForSalesReturnIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_ConsignmentRequest_ModuleId: // Sales Order
                map = CustomDesignerConstants.CustomDesignSalesOrderExtraFieldsMap;
                map.putAll(CustomDesignerConstants.CustomDesignConsignmentRequestExtraFieldsMap);
                break;
            case Constants.Acc_ConsignmentDeliveryOrder_ModuleId: // Delivery Order
                map = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentInvoice_ModuleId: // Consignment Invoice
                map = CustomDesignerConstants.CustomDesignInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentSalesReturn_ModuleId: // Consignment Sales Return
                map = CustomDesignerConstants.CustomDesignSalesReturnExtraFieldsMap;
                break;
            case Constants.Acc_Consignment_GoodsReceipt_ModuleId: // Consignment Sales Return
                map = CustomDesignerConstants.CustomDesignVendorInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_ConsignmentPurchaseReturn_ModuleId: // Purchase Return
                map = CustomDesignerConstants.CustomDesignPurchaseReturnExtraFieldsMap;
                break;
            case Constants.Acc_Purchase_Return_ModuleId: // Purchase Return
                map = CustomDesignerConstants.CustomDesignPurchaseReturnExtraFieldsMap;
                // add or remove fields related to Indian
                if(countryid == Constants.indian_country_id){
                    map.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia); 
                } else{
                    map = removeIndianExtraFields(map,CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndia);
                }
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Stock_Request_ModuleId: // Consignment Stock Request
                map = CustomDesignerConstants.CustomDesignStockRequestExtraFieldsMap;
                break;
            case Constants.Inventory_ModuleId: // Consignment Stock Issue
                map = CustomDesignerConstants.CustomDesignStockRequestExtraFieldsMap;
                break;
            case Constants.Acc_Stock_Adjustment_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignStockAdjustmentExtraFieldsMap;
                break;
            case Constants.Acc_InterStore_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignInterStoreTransferExtraFieldsMap;
                break;
            case Constants.Acc_InterLocation_ModuleId: // Consignment Stock Adjustment
                map = CustomDesignerConstants.CustomDesignInterLocationstockTransferExtraFieldsMap;
                break;
            case Constants.Acc_RFQ_ModuleId: // Request For Quotation
                map = CustomDesignerConstants.CustomDesignRequestForQuotationExtraFieldsMap;
                if (countryid == Constants.indian_country_id) {
                    map.putAll(CustomDesignerConstants.CustomDesignExtraFieldsForRequestForQuotationIndia);
                } else {
                    map = removeIndianExtraFields(map, CustomDesignerConstants.CustomDesignExtraFieldsForRequestForQuotationIndia);
                }
                break;
            case Constants.Acc_Purchase_Requisition_ModuleId: // Purchase Requisition //ERP-19851
                map = CustomDesignerConstants.CustomDesignPurchaseRequisitionExtraFieldsMap;
                map = CustomDesignHandler.addRemoveSpecificExtraFields(map,countryid);
                break;
            case Constants.Acc_Stock_Repair_Report_ModuleId: // Inventory Stock Repair Module
                map = InventoryCustomDesignerConstants.CustomDesignStockRepairExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_Purchase_Order_ModuleId: // Asset Pucrhase Order Module
                map = CustomDesignerConstants.CustomDesignPurchaseOrderExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId: // Asset Purchase Requisition Module
                map = CustomDesignerConstants.CustomDesignPurchaseRequisitionExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_RFQ_ModuleId: // Asset RFQ Module
                map = CustomDesignerConstants.CustomDesignRequestForQuotationExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId: // Asset Vendor Quotation Module
                map = CustomDesignerConstants.CustomDesignVendorQuotationExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId: // Asset Acquired Invoice Module
                map = CustomDesignerConstants.CustomDesignVendorInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_DisposalInvoice_ModuleId: // Asset Disposal Invoice Module
                map = CustomDesignerConstants.CustomDesignInvoiceExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_GoodsReceipt_ModuleId: // Asset Goods Receipt Module
                map = CustomDesignerConstants.CustomDesignGROExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_DeliveryOrder_ModuleId: // Asset Delivery Order Module
                map = CustomDesignerConstants.CustomDesignDOExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_Purchase_Return_ModuleId: // Asset Purchase Return Module
                map = CustomDesignerConstants.CustomDesignPurchaseReturnExtraFieldsMap;
                break;
            case Constants.Acc_FixedAssets_Sales_Return_ModuleId: // Asset Sales Return Module
                map = CustomDesignerConstants.CustomDesignSalesReturnExtraFieldsMap;
                break;
            case Constants.MRP_WORK_ORDER_MODULEID: // MRP Work Order Module
                map = CustomDesignerConstants.CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap;
                break;
            case Constants.Bank_Reconciliation_ModuleId: // Bank Reconciliation Module
                map = CustomDesignerConstants.CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap;
                break;
            case Constants.Build_Assembly_Module_Id: // Build Assembly Module
                map = CustomDesignerConstants.CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap;
                break;

        }

        return map;
    }
    
    public static HashMap<String, String> removeIndianExtraFields(HashMap<String, String> map, HashMap<String, String> removeMap) {
        Iterator it = removeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (map.containsKey(pair.getKey())) {
                map.remove(pair.getKey());
            }
        }
        return map;
    }

    @Override
    public HashMap<String, String> getdbCustomFieldColumns(int moduleid, String companyid, HashMap<String, String> extraCols) throws ServiceException {
        KwlReturnObject result;
        List list = null;

        result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
        list = result.getEntityList();
        for (int cnt = 0; cnt < list.size(); cnt++) {
            Object[] row = (Object[]) list.get(cnt);
//            extraCols.put("Custom_" + row[2], "{label:'" + row[2] + "',xtype:'" + row[1].toString() + "'}");
            extraCols.put("Custom_" + row[2], "{label:\"" + row[2] + "\",xtype:\"" + row[1].toString() + "\"}");    //SDP-12845
        }
        return extraCols;
    }

    @Override
    public JSONArray sortJsonArrayOnTransaction(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString("defaultHeader");
                        rid = rhs.getString("defaultHeader");
                    } catch (JSONException ex) {
                        Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }

    //fetching customfields for particular moduleid
    private JSONArray fetchCustomFieldsWithModule(JSONArray defaultHeaderArr, int moduleid, String companyid,String postText) {
        try {
            if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId) {
                moduleid = Constants.Acc_Customer_ModuleId;
            } else if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId) {
                moduleid = Constants.Acc_Vendor_ModuleId;
            }
            KwlReturnObject result = customDesignDAOObj.getGlobalCustomFields(companyid, moduleid);
            List list = result.getEntityList();
            if (list.size() > 0) {//creating custom fields section module wise
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("fieldid", "");
                jSONObject.put("id", "NA" + moduleid);
                jSONObject.put("xtype", "NAN");
                jSONObject.put("label", !StringUtil.isNullOrEmpty(getModuleName(moduleid)) ? "<font color=gray>--------[" + getModuleName(moduleid) + " ]Custom Fields--------</font>" : "");
                defaultHeaderArr.put(jSONObject);
            }
            for (int cnt = 0; cnt < list.size(); cnt++) {
                // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue
                Object[] row = (Object[]) list.get(cnt);
                JSONObject tempObj = new JSONObject();
                tempObj.put("id", row[0]);
                tempObj.put("label", row[1]+postText);
                tempObj.put("xtype", row[2]);
                tempObj.put("customfield", true);
                tempObj.put("defaultHeader", row[1]);
//                tempObj.put("dataindex", row[3].toString().concat(".").concat(html));
                defaultHeaderArr.put(tempObj);
            }
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return defaultHeaderArr;
        }
    }

    private void setGlobalTableParentDivAttr(Attributes styleAtt, String width) throws JSONException {
        List<Attribute> attList = styleAtt.asList();
        for (Attribute a : attList) {
            if (a.getKey().equals("style")) {
                String newValue = "";
                String[] items = a.getValue().trim().split(";");
                for (String item : items) {
                    String[] itemValues = item.split(":");
                    if (!itemValues[0].trim().equals("width") && !itemValues[0].trim().equals("cursor")) {
                        newValue = newValue.concat(item).concat(";");
                    }
                }
                newValue = newValue.concat("width:" + width).concat(";");
                a.setValue(newValue);
            }
        }
    }

    private String getGlobalTableParentDivWidth(String json, String compId) throws JSONException {
        String width = "";
        JSONArray jArr = new JSONArray(json);
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONObject jObj = jArr.getJSONObject(cnt);
            if (!StringUtil.isNullOrEmpty(jObj.optString("id", "")) && jObj.getString("id").equals(compId)) {
                width = jObj.getString("width");
                break;
            }
        }
        return width + "px";
    }

    private JSONArray fetchFieldsWithModule(JSONArray defaultHeaderArr, int moduleid, String companyid) {
        try {
            if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId || moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                defaultHeaderArr = getGlobalFieldsForSOA(moduleid);
            } else {
                KwlReturnObject result = customDesignDAOObj.getDefaultHeaders(String.valueOf(moduleid), companyid);
                List list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue,xtype,allowincustomtemplate
                    Object[] row = (Object[]) list.get(cnt);
                    if (row[8].equals('1')) { // value at index - 8 - allowincustomtemplate
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("id", row[0]);
                        tempObj.put("label", row[1]);
                        tempObj.put("dbcolumnname", row[2]);
                        tempObj.put("reftablename", row[3]);
                        tempObj.put("reftablefk", row[4]);
                        tempObj.put("reftabledatacolumn", row[5]);
                        tempObj.put("dummyvalue", row[6]);
                        tempObj.put("xtype", row[7]);
                        tempObj.put("isNumeric", false);
//                    tempObj.put("allowincustomtemplate", row[8]);
                        tempObj.put("customfield", false);
                        tempObj.put("defaultHeader", row[1]);
                        defaultHeaderArr.put(tempObj);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return defaultHeaderArr;
        }
    }
    
    private JSONArray getGlobalFieldsForSOA(int moduleid) {
        JSONArray retJarr = new JSONArray();
        try {
            if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId) {
                HashMap<String, String> fieldMap = CustomDesignerConstants.CustomDesignSOACustomerExtraFieldsMap;
                fieldMap.putAll(CustomDesignerConstants.CustomDesignSOACurrencyExtraFieldsMap);
                for (Map.Entry<String, String> fieldentry : fieldMap.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(fieldentry.getValue());
                    staticamountInfo.put("fieldid", fieldentry.getKey());
                    staticamountInfo.put("id", fieldentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    retJarr.put(staticamountInfo);
                }
            } else if (moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                HashMap<String, String> fieldMap = CustomDesignerConstants.CustomDesignSOAVendorExtraFieldsMap;
                fieldMap.putAll(CustomDesignerConstants.CustomDesignSOACurrencyExtraFieldsMap);
                for (Map.Entry<String, String> fieldentry : fieldMap.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(fieldentry.getValue());
                    staticamountInfo.put("fieldid", fieldentry.getKey());
                    staticamountInfo.put("id", fieldentry.getKey());
                    staticamountInfo.put("label", staticamountInfo.get("label"));
                    staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                    retJarr.put(staticamountInfo);
                }
            }
        } catch (Exception ex) {
        }
        return retJarr;
    }
    //fetching customfields for particular moduleid

    private class jsonComp implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            int returnVal = 0;
            try {
                int seq1 = Integer.parseInt(o1.getString("seq"));
                int seq2 = Integer.parseInt(o2.getString("seq"));
                if (seq1 == seq2) {
                    returnVal = 0;
                } else {
                    if (seq1 < seq2) {
                        returnVal = -1;
                    } else {
                        returnVal = 1;
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            return returnVal;
        }
    }

    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.    
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private String getModuleName(int moduleid) {
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
            case (Constants.Acc_Customer_ModuleId):
                moduleName = "Customer Master";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor Master";
                break;
            case (Constants.Acc_Purchase_Requisition_ModuleId):
                moduleName = "Purchase Requisition";
                break;
            case (Constants.Acc_FixedAssets_Purchase_Order_ModuleId):
                moduleName = "Asset Purchase Order";
                break;
            case (Constants.Acc_Customer_AccStatement_ModuleId):
                moduleName = "Statement of Account(Customer)";
                break;
            case (Constants.Acc_Vendor_AccStatement_ModuleId):
                moduleName = "Statement of Account(Vendor)";
                break;
            case (Constants.Acc_Stock_Repair_Report_ModuleId):
                moduleName = "Stock Repair";
                break;
            case (Constants.Acc_QA_APPROVAL_MODULE_ID):
                moduleName = "QA Approval";
                break;
            case (Constants.Build_Assembly_Module_Id):
                moduleName = "Build Assembly";
                break;
            case (Constants.Bank_Reconciliation_ModuleId):
                moduleName = "Bank Reconciliation";
                break;
            case (Constants.MRP_WORK_ORDER_MODULEID):
                moduleName = "Work Order";
                break;

        }
        return moduleName;
    }

    /*
     * Fetching Terms
     */
    //Get Summary Terms and insert into Default Header-ERP-13451
    private JSONArray fetchSummaryTermFields(JSONArray defaultHeaderArr, HashMap<String, Object> hm) {
        try {
            KwlReturnObject result = customDesignDAOObj.getSummaryTerms(hm);
            List list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                JSONObject tempObj = new JSONObject();
                tempObj.put("id", row[1]);
                tempObj.put("label", row[1]);
                tempObj.put("dbcolumnname", "");
                tempObj.put("reftablename", "");
                tempObj.put("reftablefk", "");
                tempObj.put("reftabledatacolumn", "");
                tempObj.put("dummyvalue", row[1]);
                tempObj.put("xtype", "2");
                tempObj.put("isnumeric", false);
                tempObj.put("customfield", false);
                defaultHeaderArr.put(tempObj);
                /*
                 * Exchange Rate of Terms
                 */
                tempObj = new JSONObject();
                tempObj.put("id", CustomDesignerConstants.BaseCurrency + row[1]);
                tempObj.put("label", CustomDesignerConstants.BaseCurrency + row[1]);
                tempObj.put("dbcolumnname", "");
                tempObj.put("reftablename", "");
                tempObj.put("reftablefk", "");
                tempObj.put("reftabledatacolumn", "");
                tempObj.put("dummyvalue", CustomDesignerConstants.BaseCurrency + row[1]);
                tempObj.put("xtype", "2");
                tempObj.put("isNumeric", false);
                tempObj.put("customfield", false);
                defaultHeaderArr.put(tempObj);
            }
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return defaultHeaderArr;
        }
    }

    public void deleteCustomTemplate(JSONObject paramJObj) throws ServiceException, JSONException {
        String companyid = paramJObj.getString(Constants.companyKey);
        String templateids[] = paramJObj.optString("templateid").split(",");

        StringBuffer templateidlist = new StringBuffer();
        for (int index = 0; index < templateids.length; index++) {
            templateidlist.append("'");
            templateidlist.append(templateids[index]);
            templateidlist.append("'");
        }
        String ids = templateidlist.toString().trim().replaceAll("''", "','");
        int moduleid = Integer.parseInt(paramJObj.optString(Constants.moduleid));
        customDesignDAOObj.deleteCustomTemplate(ids, moduleid, companyid);
    }
    
  @Override  
    public JSONObject deleteCustomTemplatemodule(JSONObject paramJObj) throws SecurityException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean isCommitEx = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Custom_Design");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String templateids[] = paramJObj.optString("templateid").split(",");

            StringBuffer templateidlist = new StringBuffer();
            for (int index = 0; index < templateids.length; index++) {
                templateidlist.append("'");
                templateidlist.append(templateids[index]);
                templateidlist.append("'");
            }
            String ids = templateidlist.toString().trim().replaceAll("''", "','");

            String companyid = paramJObj.getString(Constants.companyKey);
            int moduleid = Integer.parseInt(paramJObj.optString(Constants.moduleid));
            KwlReturnObject result = customDesignDAOObj.getAllDesignTemplateList(companyid, moduleid, ids);
            List list = result.getEntityList();
            String moduleName = getModuleName(moduleid);
            JSONArray templatenames = new JSONArray();
            for (Object tmpname : list) {
                templatenames.put(tmpname.toString());
            }

            deleteCustomTemplate(paramJObj);
            msg = "Template deleted successfully.";
            txnManager.commit(status);
            int cnt = 0;

            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));

            while (cnt < templatenames.length()) {
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_DELETED, "User " + paramJObj.optString(Constants.userfullname) + " has deleted Template " + templatenames.getString(cnt) + " from " + moduleName, auditRequestParams, templateids[cnt++]);
            }
            isCommitEx = true;
            issuccess = true;
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject getActiveDesignTemplateList(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        String templatesubtype = "0";
        String countryid = "";
        String stateid = "";
        try {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String companyid = paramJobj.getString(Constants.companyKey);
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            if (extrapref != null && extrapref.getCompany() != null) {
                if (extrapref.getCompany().getCountry().getID() != null) {
                    countryid = extrapref.getCompany().getCountry().getID();
                }
                if (extrapref.getCompany().getState() != null) {
                    stateid = extrapref.getCompany().getState().getID();
                }
            }
            String[] moduleidStr = (String[]) paramJobj.opt(Constants.moduleArray);
            JSONArray templateArr = new JSONArray();
            for (int i = 0; i < moduleidStr.length; i++) {
                int moduleid = Integer.parseInt(moduleidStr[i]);
                if (moduleid == Constants.Acc_ConsignmentRequest_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Sales_Order_ModuleId;
                } else if (moduleid == Constants.Acc_Lease_Order_ModuleId) {
                    templatesubtype = "2";
                    moduleid = Constants.Acc_Sales_Order_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentDeliveryOrder_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Delivery_Order_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentInvoice_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Invoice_ModuleId;
                } else if (moduleid == Constants.LEASE_INVOICE_MODULEID) {
                    templatesubtype = "2";
                    moduleid = Constants.Acc_Invoice_ModuleId;
                }  else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                    templatesubtype = "";
                    moduleid = Constants.Acc_Credit_Note_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentVendorRequest_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                } else if (moduleid == Constants.Acc_Consignment_GoodsReceipt_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
                } else if (moduleid == Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                } else if (moduleid == Constants.Acc_ConsignmentPurchaseReturn_ModuleId) {
                    templatesubtype = "1";
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                } else if (moduleid == Constants.Acc_Debit_Note_ModuleId) {
                    templatesubtype = "";
                    moduleid = Constants.Acc_Debit_Note_ModuleId;
                } else if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId) {
                    templatesubtype = "";
                    moduleid = Constants.Acc_Customer_AccStatement_ModuleId;
                } else if (moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                    templatesubtype = "";
                    moduleid = Constants.Acc_Vendor_AccStatement_ModuleId;
                } else if (moduleid == Constants.Acc_Lease_DO) {
                    templatesubtype = "2";
                    moduleid = Constants.Acc_Delivery_Order_ModuleId;
                } else if (moduleid == Constants.Acc_Lease_Return) {
                    templatesubtype = "2";
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                } else if (moduleid == Constants.Acc_Lease_Quotation) {
                    templatesubtype = "2";
                    moduleid = Constants.Acc_Customer_Quotation_ModuleId;
                } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    templatesubtype = "0,3,4";
                } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    templatesubtype = "0,3,4,5";
                } else if (moduleid == Constants.Acc_FixedAssets_Purchase_Order_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Purchase_Requisition_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_RFQ_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_RFQ_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Vendor_Quotation_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Invoice_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Delivery_Order_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_Purchase_Return_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                } else if (moduleid == Constants.Acc_FixedAssets_Sales_Return_ModuleId) {
                    templatesubtype = "6";
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                } else if(moduleid == Constants.Acc_QA_APPROVAL_MODULE_ID){
                    templatesubtype = "0";
                } else if(moduleid == Constants.Build_Assembly_Module_Id){ //ERM-26 templatesubtype="0" --> default
                    templatesubtype = "0";
                } else if(moduleid == Constants.VENDOR_JOB_WORKORDER_MODULEID){
                    templatesubtype = "9";
                    moduleid = Constants.Acc_Sales_Order_ModuleId;
                } else if(moduleid == Constants.JOB_WORK_OUT_ORDER_MODULEID){
                    templatesubtype = "9";
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                } else if(moduleid == Constants.JOB_WORK_STOCK_IN_MODULEID){
                    templatesubtype = "9";
                    moduleid = Constants.Acc_Stock_Adjustment_ModuleId;
                } else if(moduleid == Constants.JOB_WORK_OUT_STOCK_TRANSFER_MODULEID){
                    templatesubtype = "9";
                    moduleid = Constants.Acc_InterStore_ModuleId;
                }

                KwlReturnObject result = customDesignDAOObj.getActiveDesignTemplateList(companyid, moduleid, templatesubtype, countryid, stateid);
                List list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    // fetch columns - id, defaultHeader, dbcolumnname,reftablename, reftablefk,reftabledatacolumn,dummyvalue
                    Object[] row = (Object[]) list.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("templateid", row[0]);
                    tempObj.put("templatename", row[1]);
                    tempObj.put("createdby", row[2]);
                    tempObj.put("createdon", formatter.parse(row[3].toString()).getTime());
                    tempObj.put("isdefault", row[4]);
                    tempObj.put("templatesubtype", row[6]);
                    if (templatesubtype.equals("1")) {
                        if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentRequest_ModuleId;
                        } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentDeliveryOrder_ModuleId;
                        } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentSalesReturn_ModuleId;
                        } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentInvoice_ModuleId;
                        } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentVendorRequest_ModuleId;
                        } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                            moduleid = Constants.Acc_Consignment_GoodsReceipt_ModuleId;
                        } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                            moduleid = Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId;
                        } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                            moduleid = Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
                        }

                    } else if (templatesubtype.equals("2")) {
                        if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                            moduleid = Constants.Acc_Lease_DO;
                        } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                            moduleid = Constants.Acc_Lease_Return;
                        } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                            moduleid = Constants.LEASE_INVOICE_MODULEID;
                        } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                            moduleid = Constants.Acc_Lease_Order_ModuleId;
                        } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) {
                            moduleid = Constants.Acc_Lease_Quotation;
                        }
                    } else if (templatesubtype.equals(CustomDesignerConstants.ASSET)) {
                        //If template is of asset type then update module id to asset module id
                        if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_Purchase_Order_ModuleId;
                        } else if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId; 
                        } else if (moduleid == Constants.Acc_RFQ_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_RFQ_ModuleId;
                        } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId;
                        } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                        } else if (moduleid == Constants.Acc_Invoice_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_DisposalInvoice_ModuleId;
                        } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_GoodsReceipt_ModuleId;
                        } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_DeliveryOrder_ModuleId;
                        } else if (moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_Purchase_Return_ModuleId;
                        } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                            moduleid = Constants.Acc_FixedAssets_Sales_Return_ModuleId;
                        } 
                    } else if (templatesubtype.equals(CustomDesignerConstants.JOB_WORK)) {
                        if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                            moduleid = Constants.VENDOR_JOB_WORKORDER_MODULEID;
                        } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                            moduleid = Constants.JOB_WORK_OUT_ORDER_MODULEID;
                        } else if(moduleid == Constants.Acc_Stock_Adjustment_ModuleId){
                            moduleid = Constants.JOB_WORK_STOCK_IN_MODULEID;
                        } else if(moduleid == Constants.Acc_InterStore_ModuleId){
                            moduleid = Constants.JOB_WORK_OUT_STOCK_TRANSFER_MODULEID;
                        }
                    } else {
                        moduleid = (Integer) row[5];
                    }

                    tempObj.put("moduleid", moduleid);
                    templateArr.put(tempObj);
                }
                templatesubtype = "0";
            }

            jobj.put("count", templateArr.length());
            jobj.put(Constants.RES_data, templateArr);
            issuccess = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject saveActiveModeTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isCommitEx = false;
        String msg = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Custom_Design");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String templateid = paramJobj.optString("templateid", null);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid, "0"));
            int isactive = Integer.parseInt(paramJobj.optString("isactive", "0"));
            KwlReturnObject result = customDesignDAOObj.saveActiveModeTemplate(templateid, moduleid, isactive);

            jobj.put(Constants.RES_success, issuccess);
            jobj.put(Constants.RES_msg, "Active Mode changed successfully.");
            txnManager.commit(status);
            issuccess = true;
            isCommitEx = true;
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    public JSONObject copyTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isdefault = false;
        String msg = null, msgdisplay = "", templatesubtype = null, templateid = null, html = "", json = "[]";
        String pagelayoutproperty = "", pagefooterhtml = "", pagefooterJSON = "", pageheaderhtml = "";
        String pageheaderJSON = "", sqlquery = "", pagefootersqlquery = "", pageheadersqlquery = "", footerheader = "", isnewdesign = "";
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String userid = paramJobj.optString(Constants.useridKey, null);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
            String templatename = paramJobj.optString("templatename", null);
            String moduleName = getModuleName(moduleid);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatesubtype", null))) {
                templatesubtype = paramJobj.optString("templatesubtype");
            } else {
                templatesubtype = "0";
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templateid", null))) {
                templateid = paramJobj.optString("templateid");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isdefault", null))) {
                isdefault = Boolean.parseBoolean(paramJobj.optString("isdefault"));
            }
            boolean isDuplicate = customDesignDAOObj.isDuplicateTemplate(companyid, moduleid, templatename);
            if (!isDuplicate) {
                if (templateid != null) {
                    // Get the template
                    KwlReturnObject getDataResult = null;
                    if (isdefault) {
                        getDataResult = customDesignDAOObj.getNewDesignTemplate(Constants.defaultTemplateCompanyid, templateid, false);
                    } else {
                        getDataResult = customDesignDAOObj.getNewDesignTemplate(companyid, templateid, false);
                    }
                    if (getDataResult != null) {
                        List list = getDataResult.getEntityList();
                        if (list.size() > 0) {
                            Object[] rows = (Object[]) list.get(0);
                            html = rows[0] != null ? rows[0].toString() : "";
                            json = rows[1] != null ? rows[1].toString() : "";
                            sqlquery = rows[2] != null ? rows[2].toString() : "";
                            pagelayoutproperty = rows[3] != null ? rows[3].toString() : "";
                            pagefooterhtml = rows[4] != null ? rows[4].toString() : "";
                            pagefooterJSON = rows[5] != null ? rows[5].toString() : "";
                            pagefootersqlquery = rows[6] != null ? rows[6].toString() : "";
                            pageheaderhtml = rows[8] != null ? rows[8].toString() : "";
                            pageheaderJSON = rows[9] != null ? rows[9].toString() : "";
                            pageheadersqlquery = rows[10] != null ? rows[10].toString() : "";
                            footerheader = rows[11] != null ? rows[11].toString() : "";
                            isnewdesign = rows[12] != null ? rows[12].toString() : "";

                            KwlReturnObject result = customDesignDAOObj.copyTemplate(companyid, userid, moduleid, templatename, templatesubtype, html, json, pagelayoutproperty, pagefooterhtml, pagefooterJSON, pageheaderhtml, pageheaderJSON, sqlquery, pagefootersqlquery, pageheadersqlquery, footerheader, isnewdesign);
                            issuccess = result.isSuccessFlag();
                            jobj.put(Constants.RES_success, issuccess);

                            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));

                            if (issuccess == true) {
                                msgdisplay = messageSource.getMessage("acc.customedesigner.Templatecopiedsuccessfully", null, Locale.forLanguageTag(paramJobj.getString("language")));
                                jobj.put(Constants.RES_msg, msgdisplay);
                                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.getString(Constants.userfullname) + " has copied Template " + templatename + " for " + moduleName, auditRequestParams, companyid);
                            }
                        }
                    }
                }
            } else {
                msgdisplay = "Duplicate Template Name";
                jobj.put(Constants.RES_msg, msgdisplay);
            }
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? msgdisplay : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

  @Override  
    public JSONArray getGroupingFields(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        boolean issuccess = false;
        String msg = null;
        try {
            int moduleId = Integer.parseInt(paramJobj.optString("moduleid"));
            String companyId = paramJobj.optString("companyid");

            JSONObject tempObj = new JSONObject();

            KwlReturnObject result = customDesignDAOObj.getComboFieldParams(companyId, moduleId);
            List list = result.getEntityList();

            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                tempObj = new JSONObject();
                tempObj.put("id", row[0]);
                tempObj.put("value", row[1]);
                tempObj.put("key", row[2]);
                jarr.put(tempObj);
            }
            jobj.put("data", jarr);

        } catch (Exception e) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jarr;
    }

    @Override
    public JSONObject createTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null, msgdisplay = "";
        String templatesubtype = null;
        try {
            String companyid = paramJobj.getString(Constants.companyKey);
            String userid = paramJobj.optString(Constants.useridKey);
            int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid, "0"));
            String templatename = paramJobj.optString("templatename", null);
            String moduleName = getModuleName(moduleid);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatesubtype", null))) {
                templatesubtype = paramJobj.optString("templatesubtype");
            } else {
                templatesubtype = "0";
            }
            boolean isDuplicate = customDesignDAOObj.isDuplicateTemplate(companyid, moduleid, templatename);
            if (!isDuplicate) {
                KwlReturnObject result = customDesignDAOObj.createTemplate(companyid, userid, moduleid, templatename, templatesubtype);
                issuccess = result.isSuccessFlag();
                jobj.put(Constants.RES_success, issuccess);
                if (issuccess == true) {
                    msgdisplay = "Template saved successfully.";
                    jobj.put(Constants.RES_msg, msgdisplay);
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_CREATED, "User " + paramJobj.optString(Constants.userfullname) + " has created custom designer Template " + templatename + " for " + moduleName, auditRequestParams, companyid);
                }
            } else {
                msgdisplay = "Duplicate Template Name";
                jobj.put(Constants.RES_msg, msgdisplay);
            }
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? msgdisplay : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    
    /**
     * This method returns the fields for the selected module category
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
  @Override
    public JSONObject getFields(Map<String, Object> requestMap) throws ServiceException {
        String moduleID = null, companyID = null, userid = null;
        int xtype = 0;
        int moduleid = 0;
        Boolean isforformulabuilder = false;
        String postText ="";
        if (requestMap.containsKey(Constants.isforformulabuilder) && requestMap.get(Constants.isforformulabuilder) != null) {
            isforformulabuilder = Boolean.parseBoolean((String) requestMap.get(Constants.isforformulabuilder));
        }
        if (requestMap.containsKey(Constants.xtype) && requestMap.get(Constants.xtype) != null) {
            xtype = Integer.parseInt((String) requestMap.get(Constants.xtype));
        }
        if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey) != null) {
            companyID = (String) requestMap.get(Constants.companyKey);
        }
        if (requestMap.containsKey(Constants.useridKey) && requestMap.get(Constants.useridKey) != null) {
            userid = (String) requestMap.get(Constants.useridKey);
        }
        if (requestMap.containsKey(Constants.moduleid) && requestMap.get(Constants.moduleid) != null) {
            moduleID = (String) requestMap.get(Constants.moduleid);
        }

        moduleid = Integer.parseInt(moduleID);

        JSONObject jresult = new JSONObject();
        int countryid = 0;
        KwlReturnObject companyobj = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyID);
        if (companyobj != null) {
            Company companydata = (Company) companyobj.getEntityList().get(0);
            if (companydata != null && companydata.getCountry() != null && !StringUtil.isNullOrEmpty(companydata.getCountry().getID())) {
                countryid = Integer.parseInt(companydata.getCountry().getID());
            }
        }

        JSONArray defaultHeaderArr = new JSONArray();
        JSONArray customfieldArr = new JSONArray();
        try {
            customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyID,postText);//fetching customfields for original moduleid
            if (customfieldArr.length() > 1) {//putting all custom fields in defaultheader array
                for (int i = 0; i < customfieldArr.length(); i++) {
                    JSONObject customfieldobject = new JSONObject();
                    customfieldobject = customfieldArr.getJSONObject(i);
                    if (i > 0 && customfieldobject.getString("xtype").equals("2")) {
                        customfieldobject.put("columntype", "Custom Fields");
                        jresult.append("data", customfieldobject);
                    }
                }
            }
            HashMap<String, String> extraCols = getExtraFieldsForModule(moduleid, countryid,"-1");
            if (extraCols != null) {
                for (Map.Entry<String, String> extraColsEntry : extraCols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                    if (staticamountInfo.get("xtype").equals("2")) {
                        staticamountInfo.put("fieldid", extraColsEntry.getKey());
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("id", extraColsEntry.getKey());
                        staticamountInfo.put("defaultHeader", staticamountInfo.get("label"));
                        staticamountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticamountInfo);
                    }
                }
            }
            //For total Amount,Subtotal,Total Tax,Total Discount,Amount in words entry in Default Header
            HashMap<String, String> amountcols = null;
            if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) { //27 - Delivery order, 28- Goods receipts 
                amountcols = LineItemColumnModuleMapping.GROReportDOReportProductSummaryItems;
            } else if (moduleid != Constants.Acc_Debit_Note_ModuleId && moduleid != Constants.Acc_Credit_Note_ModuleId && moduleid != Constants.Acc_Make_Payment_ModuleId
                    && moduleid != Constants.Acc_Receive_Payment_ModuleId && moduleid != Constants.Acc_Stock_Request_ModuleId && moduleid != Constants.Inventory_ModuleId && moduleid != Constants.Acc_Stock_Adjustment_ModuleId
                    && moduleid != Constants.Acc_InterStore_ModuleId && moduleid != Constants.Acc_RFQ_ModuleId && moduleid != Constants.Acc_InterLocation_ModuleId && moduleid != Constants.Acc_Purchase_Requisition_ModuleId
                    && moduleid != Constants.Acc_Customer_AccStatement_ModuleId && moduleid != Constants.Acc_Vendor_AccStatement_ModuleId  && moduleid != Constants.Bank_Reconciliation_ModuleId) {
                amountcols = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
            }
            if (amountcols != null) {
                for (Map.Entry<String, String> amountentry : amountcols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    if (staticamountInfo.get("xtype").equals("2")) {
                        staticamountInfo.put("id", amountentry.getKey());
                        staticamountInfo.put("fieldid", amountentry.getKey());
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("defaultHeader", staticamountInfo.get("label"));
                        staticamountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticamountInfo);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return jresult;
    }
    
  @Override  
    public JSONObject saveDesignTemplate(JSONObject paramJObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = null, msgdisplay = "", templatesubtype = "";
        try {
            boolean saveasflag = false;
            KwlReturnObject result = null;
            String templateid = paramJObj.optString("templateid", null);
            String templatename = paramJObj.optString("templatename", null);
            String pagelayoutproperty = paramJObj.optString("pagelayoutproperty", null);
            String companyid = paramJObj.getString(Constants.companyKey);
            String userid = paramJObj.optString(Constants.useridKey);
            int moduleid = Integer.parseInt(paramJObj.optString(Constants.moduleid, "0"));
            String bandID = paramJObj.optString("bandid");
            String sqlquery = "";
            String html = paramJObj.optString("html");
            String json = paramJObj.optString("json");
            saveasflag = !StringUtil.isNullOrEmpty(paramJObj.optString("saveasflag", null)) ? Boolean.parseBoolean(paramJObj.optString("saveasflag")) : true;

            if (!saveasflag) {                           //Deepak Pingale SAVE AS
                json = "[]";
                String pagefooterjson = "[]", pageheaderjson = "[]", pagefooterhtml = "", pagefootersqlquery = "", pageheaderhtml = "", pageheadersqlquery = "";
                int footerheader = 0;
                result = customDesignDAOObj.getDesignTemplate(templateid);
                List list = result.getEntityList();
                templateid = "";
                Object[] designtemplaterows = (Object[]) list.get(0);
                html = (String) designtemplaterows[0];
                if (designtemplaterows[1] != null) {
                    json = designtemplaterows[1].toString();
                }
                if (designtemplaterows[3] != null) {
                    sqlquery = (String) designtemplaterows[3];
                }
                if (designtemplaterows[4] != null) {
                    pagelayoutproperty = (String) designtemplaterows[4];
                }
                if (designtemplaterows[5] != null) {
                    pagefooterhtml = (String) designtemplaterows[5];
                }
                if (designtemplaterows[6] != null) {
                    pagefooterjson = (String) designtemplaterows[6];
                }
                if (designtemplaterows[7] != null) {
                    pagefootersqlquery = (String) designtemplaterows[7];
                }
                if (designtemplaterows[8] != null) {
                    templatesubtype = (String) designtemplaterows[8];
                }

                if (designtemplaterows[9] != null) {
                    pageheaderhtml = (String) designtemplaterows[9];
                }
                if (designtemplaterows[10] != null) {
                    pageheaderjson = (String) designtemplaterows[10];
                }
                if (designtemplaterows[11] != null) {
                    pageheadersqlquery = (String) designtemplaterows[11];
                }
                if (designtemplaterows[12] != null) {
                    footerheader = (Integer) designtemplaterows[12];
                }

                boolean isDuplicate = customDesignDAOObj.isDuplicateTemplate(companyid, moduleid, templatename);
                if (!isDuplicate) {
                    result = customDesignDAOObj.saveAsDesignTemplate(companyid, userid, templateid, templatename, moduleid, json, html, sqlquery, pagelayoutproperty, pagefooterhtml, pagefooterjson, pagefootersqlquery, pageheaderhtml, pageheaderjson, pageheadersqlquery, footerheader, templatesubtype);
                    msgdisplay = "Template saved successfully.";
                } else {
                    msgdisplay = "Duplicate Template Name";
                }

            } else {//save template

                if (moduleid == Constants.Acc_Invoice_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "invoice");
                } else if (moduleid == Constants.Acc_Sales_Order_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "salesorder");
                } else if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "purchaseorder");
                } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) //Neeraj Change
                {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "goodsreceipt");
                } else if (moduleid == Constants.Acc_Vendor_Quotation_ModuleId) //Neeraj Change
                {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "vendorquotation");
                } else if (moduleid == Constants.Acc_Customer_Quotation_ModuleId) //Neeraj Change
                {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "quotation");
                } else if (moduleid == Constants.Acc_Credit_Note_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "creditnote");
                } else if (moduleid == Constants.Acc_Debit_Note_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "debitnote");
                } else if (moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "deliveryorder");
                } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "grorder");
                } else if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "payment");
                } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "receipt");
                } else if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "salesreturn");
                } else if (moduleid == Constants.Acc_RFQ_ModuleId) {
                    sqlquery = CustomDesignHandler.buildSqlQuery(json, "requestforquotation");
                }

                /*
                 * Remove resize handler divs from html
                 */
                Document jsoupDoc = Jsoup.parse(html);

                Elements elements = jsoupDoc.select("div.x-resizable-handle");
                elements.remove();

                // removed Pointer image 
                if (jsoupDoc.getElementById("contentImage") != null) {
                    jsoupDoc.getElementById("contentImage").remove();
                }


                /*
                 * Remove globaltable class's width=100% property
                 */
                org.jsoup.select.Elements itemListElement = jsoupDoc.getElementsByClass("globaltable");
                if (!itemListElement.isEmpty()) {
                    Element mainDiv = itemListElement.first();
                    Element parentDiv = mainDiv.parent();
                    String parentDivID = parentDiv.id();
                    String width = getGlobalTableParentDivWidth(json, parentDivID);
                    setGlobalTableParentDivAttr(parentDiv.attributes(), width);
                    setGlobalTableParentDivAttr(mainDiv.attributes(), width);
                }
                String html1 = "";
                html1 = jsoupDoc.outerHtml();
                result = customDesignDAOObj.saveDesignTemplate(companyid, userid, templateid, templatename, moduleid, json, html1, sqlquery, pagelayoutproperty, bandID);
                msgdisplay = "Template saved successfully.";
            }
            issuccess = true;
            jobj.put(Constants.RES_success, issuccess);
            jobj.put(Constants.RES_msg, msgdisplay);
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg == null ? msgdisplay : msg);
            } catch (JSONException ex) {
                Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
  
  /**
     * Method for getting line level fields from constants map
     * 
     * @param moduleid 
     * @param templatesubtype 
     * @return JSONObject
     */
    public HashMap<String, TreeMap<String, String>> getLineItemFieldsMap(int moduleid, String templatesubtype, int countryid) {

        HashMap<String, TreeMap<String, String>> returnMap = new HashMap<String, TreeMap<String, String>>();
        TreeMap<String, String> defaultlineCols = null;
        TreeMap<String, String> baseModuletoOtherMap = null;
        TreeMap<String, String> ageingColsMap = null;
        try {
            switch (moduleid) {
                case Constants.Acc_Invoice_ModuleId: // Invoice
                    defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignInvoicetoOtherMap;
                    break;
                case Constants.Acc_Vendor_Invoice_ModuleId: // Vendor Invoice 
                    defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    break;
                case Constants.Acc_Debit_Note_ModuleId:
                    if (templatesubtype.equals("1")) {
                        defaultlineCols = LineItemColumnModuleMapping.PurchaseReturnProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPurchaseReturntoOtherMap;
                    } else if (templatesubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE)) {  //Purchase invoice - subtype - 7
                        // Fetching sales invoice line item data for Overcharged CN
                        defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignInvoicetoOtherMap;
                    } else if (templatesubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE)) {  // Sales Invoice - subtype - 8
                        // Fetching purchase invoice line item data for undercharged CN
                        defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    } else {
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignOtherCommonMap;
                    }
                    break;
                case Constants.Acc_Credit_Note_ModuleId:
                    if (templatesubtype.equals("1")) {
                        defaultlineCols = LineItemColumnModuleMapping.SalesReturnProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignSalesReturntoOtherMap;
                    } else if (templatesubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE)) {  //Purchase invoice - subtype - 7
                        // Fetching purchase invoice line item data for undercharged CN
                        defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorInvoicetoOtherMap;
                    } else if (templatesubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE)) {  // Sales Invoice - subtype - 8
                        // Fetching sales invoice line item data for Overcharged CN
                        defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignInvoicetoOtherMap;
                    } else {
                        baseModuletoOtherMap = CustomDesignerConstants.CustomDesignOtherCommonMap;
                    }
                    break;
                case Constants.Acc_Make_Payment_ModuleId:
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPaymenttoOtherMap;
                    break;
                case Constants.Acc_Receive_Payment_ModuleId:
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPaymenttoOtherMap;
                    break;
                case Constants.Acc_Purchase_Order_ModuleId: // Purchase Order
                    defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap_PO;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPurchaseOrdertoOtherMap;
                    break;
                case Constants.Acc_Sales_Order_ModuleId: // Sales Order
                    defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                    /*
                     * putting new added fields in So
                     */
                    defaultlineCols.putAll(LineItemColumnModuleMapping.InvoiceProductLineMap_SO);
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignSalesOrdertoOtherMap;
                    break;
                case Constants.Acc_Customer_Quotation_ModuleId: // Custom Quotation
                    defaultlineCols = LineItemColumnModuleMapping.CustomerQuotationProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignQuotationtoOtherMap;
                    break;
                case Constants.Acc_Vendor_Quotation_ModuleId: // Vendor Quotation
                    defaultlineCols = LineItemColumnModuleMapping.InvoiceProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignVendorQuotationtoOtherMap;
                    break;
                case Constants.Acc_Delivery_Order_ModuleId://Delivery Order
                    defaultlineCols = LineItemColumnModuleMapping.DOGROLineMap_DO;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignDeliveryOrdertoOtherMap;
                    break;
                case Constants.Acc_Goods_Receipt_ModuleId://GoodsReceiptOrder
                    defaultlineCols = LineItemColumnModuleMapping.DOGROLineMap_GR;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignGoodsReceiptOrdertoOtherMap;
                    break;
                case Constants.Acc_Sales_Return_ModuleId: //Sales Return
                    defaultlineCols = LineItemColumnModuleMapping.SalesReturnProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignSalesReturntoOtherMap;
                    break;
                case Constants.Acc_Purchase_Return_ModuleId: //Sales Return
                    defaultlineCols = LineItemColumnModuleMapping.PurchaseReturnProductLineMap;
                    baseModuletoOtherMap = CustomDesignerConstants.CustomDesignPurchaseReturntoOtherMap;
                    break;
                case Constants.Acc_Stock_Request_ModuleId: //Stock Request
                    defaultlineCols = LineItemColumnModuleMapping.StockRequestLineMap;
                    break;
                case Constants.Inventory_ModuleId: //Stock Issue
                    defaultlineCols = LineItemColumnModuleMapping.StockIssueLineMap;
                    break;
                case Constants.Acc_Stock_Adjustment_ModuleId: //Stock Request
                    defaultlineCols = LineItemColumnModuleMapping.StockAdjustmentLineMap;
                    break;
                case Constants.Acc_InterStore_ModuleId: //Stock Request
                    defaultlineCols = LineItemColumnModuleMapping.InterStoreStockTransferLineMap;
                    break;
                case Constants.Acc_InterLocation_ModuleId: //Stock Request
                    defaultlineCols = LineItemColumnModuleMapping.InterLocationStoreTransferLineMap;
                    break;
                case Constants.Acc_RFQ_ModuleId: //Request For Quotation
                    defaultlineCols = LineItemColumnModuleMapping.RFQProductLineMap;
                    break;
                case Constants.Acc_Customer_AccStatement_ModuleId:
                    defaultlineCols = LineItemColumnModuleMapping.CustomDesignStatementOfAccountLineLevelExtraFieldsMap;
                    ageingColsMap = CustomDesignerConstants.CustomDesignCustomerAgeingMap;
                    break;
                case Constants.Acc_Vendor_AccStatement_ModuleId:
                    defaultlineCols = LineItemColumnModuleMapping.CustomDesignStatementOfAccountLineLevelVendorExtraFieldsMap;
                    ageingColsMap = CustomDesignerConstants.CustomDesignVendorAgeingMap;
                    break;
                case Constants.Acc_Purchase_Requisition_ModuleId: //Request For Purchase Requisition //ERP-19851
                    defaultlineCols = LineItemColumnModuleMapping.PurchaseRequisitionLineMap;
                    break;
                case Constants.Acc_Stock_Repair_Report_ModuleId: //Request For Purchase Requisition //ERP-19851
                    defaultlineCols = LineItemColumnModuleMapping.StockRepairLineMap;
                    break;
                case Constants.Build_Assembly_Module_Id: //ERM-26 line level items map
                    defaultlineCols = LineItemColumnModuleMapping.BuildAssemblyReportLineMap;
                    break;
                case Constants.MRP_WORK_ORDER_MODULEID: //ERM-646 Work Order
                    defaultlineCols = new TreeMap<>();
                    ageingColsMap = LineItemColumnModuleMapping.MRP_WORK_ORDER_MAP_Checklist;
                    break;
            }
            
            /*
             * Adding LineLevelTax Fields for Indian country
             */
            if(countryid == Constants.indian_country_id || countryid == Constants.USA_country_id) {
                if(moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Sales_Return_ModuleId || moduleid == Constants.Acc_Purchase_Return_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid ==  Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId) {
                   defaultlineCols.putAll(LineItemColumnModuleMapping.LineLevelTaxFields_India_USA); 
                }
            }
            
            if(countryid == Constants.indian_country_id && moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                defaultlineCols.putAll(CustomDesignerConstants.TDS_Field_Map);
            }
            
            if (defaultlineCols != null) {
                returnMap.put("defaultlineCols", defaultlineCols);
            }
            if (baseModuletoOtherMap != null) {
                returnMap.put("baseModuletoOtherMap", baseModuletoOtherMap);
            }
            if (ageingColsMap != null) {
                returnMap.put("ageingColsMap", ageingColsMap);
            }
        } catch (Exception e) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnMap;
    }
    
    /**
     * This method returns the fields for the selected module category
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    public JSONObject getLineLevelFields(Map<String, Object> requestMap) throws ServiceException {
        String moduleID = null, companyID = null, userid = null, templatesubtype = "0";
        int xtype = 0;
        int moduleid = 0;
        Boolean isforformulabuilder = false;
        if (requestMap.containsKey(Constants.isforformulabuilder) && requestMap.get(Constants.isforformulabuilder) != null) {
            isforformulabuilder = Boolean.parseBoolean((String) requestMap.get(Constants.isforformulabuilder));
        }
        if (requestMap.containsKey(Constants.xtype) && requestMap.get(Constants.xtype) != null) {
            xtype = Integer.parseInt((String) requestMap.get(Constants.xtype));
        }
        if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey) != null) {
            companyID = (String) requestMap.get(Constants.companyKey);
        }
        if (requestMap.containsKey(Constants.useridKey) && requestMap.get(Constants.useridKey) != null) {
            userid = (String) requestMap.get(Constants.useridKey);
        }
        if (requestMap.containsKey(Constants.moduleid) && requestMap.get(Constants.moduleid) != null) {
            moduleID = (String) requestMap.get(Constants.moduleid);
        }
        if (requestMap.containsKey(Constants.TEMPLATE_SUBTYPE) && requestMap.get(Constants.TEMPLATE_SUBTYPE) != null) {
            templatesubtype = (String) requestMap.get(Constants.TEMPLATE_SUBTYPE);
        }

        moduleid = Integer.parseInt(moduleID);

        JSONObject jresult = new JSONObject();
        int countryid = 0;
        KwlReturnObject companyobj = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyID);
        if (companyobj != null) {
            Company companydata = (Company) companyobj.getEntityList().get(0);
            if (companydata != null && companydata.getCountry() != null && !StringUtil.isNullOrEmpty(companydata.getCountry().getID())) {
                countryid = Integer.parseInt(companydata.getCountry().getID());
            }
        }

        try {
            /*
             * get line item columns configuration
             */
            TreeMap<String, String> lineCols = new TreeMap<String, String>();
            TreeMap<String, String> defaultlineCols = null;
            TreeMap<String, String> baseModuletoOtherMap = null;
            TreeMap<String, String> ageingColsMap = null;
            TreeMap<String, String> ageingCols = new TreeMap<String, String>();
            TreeMap<String, String> productitemsplaceholder = null;

            HashMap<String, TreeMap<String, String>> lineItemFieldsMap = getLineItemFieldsMap(moduleid, templatesubtype, countryid);

            defaultlineCols = lineItemFieldsMap.containsKey("defaultlineCols") ? lineItemFieldsMap.get("defaultlineCols") : null;
            baseModuletoOtherMap = lineItemFieldsMap.containsKey("baseModuletoOtherMap") ? lineItemFieldsMap.get("baseModuletoOtherMap") : null;
            ageingColsMap = lineItemFieldsMap.containsKey("ageingColsMap") ? lineItemFieldsMap.get("ageingColsMap") : null;

            if (defaultlineCols != null) {
                lineCols = (TreeMap<String, String>) defaultlineCols.clone();
            }
            for (Map.Entry<String, String> linefield : lineCols.entrySet()) {
                JSONObject fieldJson = new JSONObject(linefield.getValue());
                if (fieldJson.getString("xtype").equals("2")) {
                    fieldJson.put("defaultHeader", fieldJson.getString("label"));
                    fieldJson.put("columntype", "Default Fields");
                    fieldJson.put("customfield", false);
                    fieldJson.put("id", linefield.getKey());
                    jresult.append("data", fieldJson);
                }
            }
            /*
             * Put custom columns in lineCols map
             */
            int totalCount = lineCols.size();
            int totalstaticCLineols = lineCols.size();
            KwlReturnObject result = customDesignDAOObj.getCustomLineFields(companyID, moduleid);
            // select id, fieldtype, fieldlabel, colnum from fieldparams
            List list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                if (row[1].toString().equals("2")) { //If number field : xtype = 2
                    JSONObject temp = new JSONObject();
                    temp.put("id", "col" + row[3]);// use <col>colnum as id for custom line. We used id=1 as Product Name as so on.
                    temp.put("label", row[2]);
                    temp.put("defaultHeader", row[2]);
                    temp.put("xtype", row[1]);
                    temp.put("defwidth", 15);
                    temp.put("seq", totalstaticCLineols + cnt);
                    temp.put("custom", true);
                    temp.put("customfield", true);
                    temp.put("columntype", "Custom Fields");
                    jresult.append("data", temp);
                    
                    totalCount = totalstaticCLineols + cnt;
                }
            }
            /*
             * Put Product custom columns in lineCols map
             */
            KwlReturnObject productResult = customDesignDAOObj.getProductCustomLineFields(companyID, moduleid);
            // select id, fieldtype, fieldlabel, colnum from fieldparams
            List productCustomList = productResult.getEntityList();
            for (int cnt = 0; cnt < productCustomList.size(); cnt++) {
                Object[] row = (Object[]) productCustomList.get(cnt);
                if (row[1].toString().equals("2")) { //If number field : xtype = 2
                    JSONObject temp = new JSONObject();
                    temp.put("id", "col" + row[3]);// use <col>colnum as id for product custom line. We used id=1 as Product Name as so on.
                    temp.put("label", row[2]);
                    temp.put("defaultHeader", row[2]);
                    temp.put("xtype", row[1]);
                    temp.put("defwidth", 15);
                    temp.put("seq", totalCount + cnt);
                    temp.put("custom", true);
                    temp.put("customfield", true);
                    temp.put("columntype", "Custom Fields");
                    jresult.append("data", temp);
                }
            }

        } catch (Exception e) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, e);
        }
        return jresult;
    }
    
 @Override   
    public JSONObject getLineFieldsData(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        Map<String, Object> requestParams = new HashMap<String, Object>();
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("xtype", null))) {
            requestParams.put("xtype", paramJobj.optString("xtype"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isforformulabuilder, null))) {
            requestParams.put(Constants.isforformulabuilder, paramJobj.optString(Constants.isforformulabuilder));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("id", null))) {
            requestParams.put(Constants.moduleid, paramJobj.optString("id"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatesubtype", null))) {
            requestParams.put(Constants.TEMPLATE_SUBTYPE, paramJobj.optString("templatesubtype"));
        }
        requestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));

        JSONArray returnJarr = getLineLevelFields(requestParams).optJSONArray("data");
        returnJarr = sortJsonArrayOnTransaction(returnJarr);
        jobj.put("data", returnJarr != null ? returnJarr : new JSONArray());
        jobj.put("success", true);
        jobj.put("msg", "succes");
        return jobj;
    }
    
        /**
     * This method returns the fields for the selected module category
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    public JSONObject getGlobalLevelFields(Map<String, Object> requestMap) throws ServiceException {
        String moduleID = null, companyID = null, userid = null;
        int xtype = 0;
        int moduleid = 0;
        String postText ="";
        String templatesubtype = "";
        Boolean isforformulabuilder = false;
        if (requestMap.containsKey(Constants.isforformulabuilder) && requestMap.get(Constants.isforformulabuilder) != null) {
            isforformulabuilder = Boolean.parseBoolean((String) requestMap.get(Constants.isforformulabuilder));
        }
        if (requestMap.containsKey(Constants.xtype) && requestMap.get(Constants.xtype) != null) {
            xtype = Integer.parseInt((String) requestMap.get(Constants.xtype));
        }
        if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey) != null) {
            companyID = (String) requestMap.get(Constants.companyKey);
        }
        if (requestMap.containsKey(Constants.useridKey) && requestMap.get(Constants.useridKey) != null) {
            userid = (String) requestMap.get(Constants.useridKey);
        }
        if (requestMap.containsKey(Constants.moduleid) && requestMap.get(Constants.moduleid) != null) {
            moduleID = (String) requestMap.get(Constants.moduleid);
        }
        moduleid = Integer.parseInt(moduleID);
        
        if (requestMap.containsKey("templatesubtype") && requestMap.get("templatesubtype") != null) {
            templatesubtype = (String) requestMap.get("templatesubtype");
        }
        if (templatesubtype.equals("1")) { // for consignment case
            if (moduleid == Constants.Acc_Sales_Return_ModuleId) {
                moduleid = Constants.Acc_ConsignmentSalesReturn_ModuleId;
            } else if(moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {
                moduleid = Constants.Acc_Consignment_GoodsReceipt_ModuleId;
            } else if(moduleid == Constants.Acc_Purchase_Return_ModuleId) {
                moduleid = Constants.Acc_ConsignmentPurchaseReturn_ModuleId;
            }
        }
        JSONObject jresult = new JSONObject();
        int countryid = 0;
        KwlReturnObject companyobj = accountingHandlerDAOobj.getObject("com.krawler.common.admin.Company", companyID);
        if (companyobj != null) {
            Company companydata = (Company) companyobj.getEntityList().get(0);
            if (companydata != null && companydata.getCountry() != null && !StringUtil.isNullOrEmpty(companydata.getCountry().getID())) {
                countryid = Integer.parseInt(companydata.getCountry().getID());
            }
        }

        JSONArray defaultHeaderArr = new JSONArray();
        JSONArray customfieldArr = new JSONArray();
        try {
            customfieldArr = fetchCustomFieldsWithModule(customfieldArr, moduleid, companyID,postText);//fetching customfields for original moduleid
            if (customfieldArr.length() > 1) {//putting all custom fields in defaultheader array
                for (int i = 0; i < customfieldArr.length(); i++) {
                    JSONObject customfieldobject = new JSONObject();
                    customfieldobject = customfieldArr.getJSONObject(i);
                    if (i > 0 && customfieldobject.getString("xtype").equals("2")) {
                        customfieldobject.put("columntype", "Custom Fields");
                        jresult.append("data", customfieldobject);
                    }
                }
            }
            HashMap<String, String> extraCols = getExtraFieldsForModule(moduleid, countryid,"-1");
            if (extraCols != null) {
                for (Map.Entry<String, String> extraColsEntry : extraCols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                    if (staticamountInfo.get("xtype").equals("2") && (staticamountInfo.opt(Constants.ISNUMERIC) != null ? staticamountInfo.optBoolean(Constants.ISNUMERIC) : true)) {
                        staticamountInfo.put("fieldid", extraColsEntry.getKey());
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("id", extraColsEntry.getKey());
                        staticamountInfo.put("defaultHeader", staticamountInfo.get("label"));
                        staticamountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticamountInfo);
                    }
                }
            }
            //add global level extra fields for Customer and Vendor SOA
            if (moduleid == Constants.Acc_Customer_AccStatement_ModuleId || moduleid == Constants.Acc_Vendor_AccStatement_ModuleId) {
                JSONArray soaJArr= getGlobalFieldsForSOA(moduleid);
                for(int ind = 0; ind < soaJArr.length(); ind++){
                    JSONObject staticAmountInfo = soaJArr.getJSONObject(ind);
                    //If numeric fields then only add in map for formula fields
                    if (staticAmountInfo.get("xtype").equals("2") && (staticAmountInfo.opt(Constants.ISNUMERIC) != null ? staticAmountInfo.optBoolean(Constants.ISNUMERIC) : true)) {
                        staticAmountInfo.put("defaultHeader", staticAmountInfo.get("label"));
                        staticAmountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticAmountInfo);
                    }
                }
            }
            //For total Amount,Subtotal,Total Tax,Total Discount,Amount in words entry in Default Header
            HashMap<String, String> amountcols = null;
            if (moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) { //27 - Delivery order, 28- Goods receipts 
                amountcols = LineItemColumnModuleMapping.GROReportDOReportProductSummaryItems;
            } else if (moduleid != Constants.Acc_Debit_Note_ModuleId && moduleid != Constants.Acc_Credit_Note_ModuleId && moduleid != Constants.Acc_Make_Payment_ModuleId
                    && moduleid != Constants.Acc_Receive_Payment_ModuleId && moduleid != Constants.Acc_Stock_Request_ModuleId && moduleid != Constants.Inventory_ModuleId && moduleid != Constants.Acc_Stock_Adjustment_ModuleId
                    && moduleid != Constants.Acc_InterStore_ModuleId && moduleid != Constants.Acc_RFQ_ModuleId && moduleid != Constants.Acc_InterLocation_ModuleId && moduleid != Constants.Acc_Purchase_Requisition_ModuleId
                    && moduleid != Constants.Acc_Customer_AccStatement_ModuleId && moduleid != Constants.Acc_Vendor_AccStatement_ModuleId && moduleid != Constants.Bank_Reconciliation_ModuleId && moduleid != Constants.Acc_ConsignmentSalesReturn_ModuleId 
                    && moduleid != Constants.Acc_ConsignmentPurchaseReturn_ModuleId && moduleid != Constants.Acc_Consignment_GoodsReceipt_ModuleId && moduleid != Constants.Acc_ConsignmentPurchaseReturn_ModuleId
                    && moduleid != Constants.Build_Assembly_Module_Id && moduleid != Constants.MRP_WORK_ORDER_MODULEID) {
                amountcols = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
            }
            if (amountcols != null) {
                for (Map.Entry<String, String> amountentry : amountcols.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(amountentry.getValue());
                    if (staticamountInfo.get("xtype").equals("2") && (staticamountInfo.opt(Constants.ISNUMERIC) != null ? staticamountInfo.optBoolean(Constants.ISNUMERIC) : true)) {
                        staticamountInfo.put("id", amountentry.getKey());
                        staticamountInfo.put("fieldid", amountentry.getKey());
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("defaultHeader", staticamountInfo.get("label"));
                        staticamountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticamountInfo);
                    }
                }
            }
            
            /*
                code to get terms in formula fields
            */
            JSONArray defaulhHeaderArr = new JSONArray();
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("companyid", companyID);
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                hm.put("salesOrPurchaseFlag", true);
            } else if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                hm.put("salesOrPurchaseFlag", false);
            }
            if (moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Customer_Quotation_ModuleId
                    || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId
                    || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId) {
                defaulhHeaderArr = fetchSummaryTermFields(defaulhHeaderArr, hm);
                for (int i = 0; i < defaulhHeaderArr.length(); i++) {
                    JSONObject staticamountInfo = new JSONObject();
                    staticamountInfo = defaulhHeaderArr.getJSONObject(i);
                    //if (staticamountInfo.get("xtype").equals("2") && (staticamountInfo.opt(Constants.ISNUMERIC) != null ? staticamountInfo.optBoolean(Constants.ISNUMERIC) : true)) {
                        staticamountInfo.put("id", staticamountInfo.get("id"));
                        staticamountInfo.put("fieldid", staticamountInfo.get("id"));
                        staticamountInfo.put("label", staticamountInfo.get("label"));
                        staticamountInfo.put("xtype", staticamountInfo.get("xtype"));
                        staticamountInfo.put("defaultHeader", staticamountInfo.get("label"));
                        staticamountInfo.put("columntype", "Extra Fields");
                        jresult.append("data", staticamountInfo);
                    //}
                }
                
            }
        } catch (Exception e) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, e);
        }
        return jresult;
    }
  
    @Override
    public JSONObject getGlobalFieldsData(JSONObject paramJobj) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        Map<String, Object> requestParams = new HashMap<String, Object>();
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("xtype", null))) {
            requestParams.put("xtype", paramJobj.optString("xtype"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isforformulabuilder, null))) {
            requestParams.put(Constants.isforformulabuilder, paramJobj.optString(Constants.isforformulabuilder));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("id", null))) {
            requestParams.put(Constants.moduleid, paramJobj.optString("id"));
        }
        requestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
        requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
        
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("templatesubtype", null))) {
            requestParams.put("templatesubtype", paramJobj.optString("templatesubtype"));
        }

        JSONArray returnJarr = getGlobalLevelFields(requestParams).optJSONArray("data");
        returnJarr = sortJsonArrayOnTransaction(returnJarr);
        jobj.put("data", returnJarr != null ? returnJarr : new JSONArray());
        jobj.put("success", true);
        jobj.put("msg", "succes");

        return jobj;
    }
    /**
     * Get fields of Details Table based on subtype
     * @param paramJobj
     * @return JSONArray of fields
     */
    public JSONArray getDetailsTableFields(JSONObject paramJobj) {
        JSONArray jarr = new JSONArray();
        try {
            int moduleId = Integer.parseInt(paramJobj.optString("moduleId"));
            String companyId = paramJobj.optString("companyId");
            String detailsSubType_id = paramJobj.optString("detailsSubType_id");
            String detailsSubType_value = paramJobj.optString("detailsSubType_value");

            //get fields from constants map based on module and subtype
            TreeMap<String, String> detailsTableCols = new TreeMap<String, String>();
            //condition for moduleid
            switch(moduleId){
                case Constants.Bank_Reconciliation_ModuleId : {
                    //condition for subtype
                    switch(detailsSubType_id){
                        case "deposits_and_other_credits" : {
                            detailsTableCols = LineItemColumnModuleMapping.BankReconciliationTableMap_DetailsTableCommanFieldsMap;
                            detailsTableCols.putAll(LineItemColumnModuleMapping.BankReconciliationTableMap_Deposits_and_Other_Credits);
                            detailsTableCols = removeExtraFieldsFromMap(detailsTableCols, LineItemColumnModuleMapping.BankReconciliationTableMap_Checks_and_Payments);
                        }
                        break;
                        case "checks_and_payments" : {
                            detailsTableCols = LineItemColumnModuleMapping.BankReconciliationTableMap_DetailsTableCommanFieldsMap;
                            detailsTableCols.putAll(LineItemColumnModuleMapping.BankReconciliationTableMap_Checks_and_Payments);
                            detailsTableCols = removeExtraFieldsFromMap(detailsTableCols, LineItemColumnModuleMapping.BankReconciliationTableMap_Deposits_and_Other_Credits);
                        }
                        break;
                    }
                }
                break;
                case Constants.MRP_WORK_ORDER_MODULEID : {// MRP Work Order module
                    switch(detailsSubType_id){ // Work Order subtypes
                        case "component_availability" : {
                            detailsTableCols = LineItemColumnModuleMapping.MRP_WORK_ORDER_MAP_ComponentAvailability;
                        }
                        break;
                        case "tasks" : {
                            detailsTableCols = LineItemColumnModuleMapping.MRP_WORK_ORDER_MAP_Tasks;
                        }
                        break;
                        case "consumption" : {
                            detailsTableCols = LineItemColumnModuleMapping.MRP_WORK_ORDER_MAP_Consumption;
                        }
                        break;
                    }
                }
                break;
                case Constants.Acc_Invoice_ModuleId:
                case Constants.Acc_Purchase_Order_ModuleId:
                case Constants.Acc_Sales_Order_ModuleId:
                case Constants.Acc_Delivery_Order_ModuleId:
                case Constants.Acc_Goods_Receipt_ModuleId:
                case Constants.Acc_Receive_Payment_ModuleId:
                case Constants.Acc_Vendor_Quotation_ModuleId:
                case Constants.Acc_Customer_Quotation_ModuleId:
                case Constants.Acc_Vendor_Invoice_ModuleId: 
                case Constants.Acc_Sales_Return_ModuleId:
                case Constants.Acc_Purchase_Return_ModuleId: {// MRP Work Order module
                    switch (detailsSubType_id) { // Work Order subtypes
                        case "gsttaxsummary": {
                            detailsTableCols = LineItemColumnModuleMapping.DETAILSTABLE_GST_TAX_SUMMARY_FIELDS_MAP;
                        }
                        break;
                        
                    }
                }
                break;
            }
            //prepare details table fields json
            JSONArray detailsTableColArr = new JSONArray();
            //count for fields
            int defcolno = 1;
            //JSONObject list for fields
            List<JSONObject> detailsTableColList = new ArrayList<>();
            if(detailsTableCols != null){
                for (Map.Entry<String, String> entry : detailsTableCols.entrySet()) {
                    //set fields info in JSONObject
                    JSONObject staticColInfo = new JSONObject(entry.getValue());
                    staticColInfo.put("fieldid", entry.getKey());
                    staticColInfo.put("hidecol", staticColInfo.optBoolean("defaulthiddenfield", true));// we stored columns which are not hide in db json
                    staticColInfo.put("coltotal", false);
                    staticColInfo.put("showtotal", false);
                    staticColInfo.put("headercurrency", "");
                    staticColInfo.put("recordcurrency", "");
                    staticColInfo.put("commaamount", "");
                    staticColInfo.put("colwidth", staticColInfo.opt("defwidth"));
                    staticColInfo.put("colno", staticColInfo.optInt("colno", defcolno));
                    staticColInfo.put("decimalpoint", "");
                    staticColInfo.put("seq", staticColInfo.optInt("seq", 0));
                    staticColInfo.put("columnname", staticColInfo.opt("label"));
                    staticColInfo.put("xtype", staticColInfo.opt("xtype"));
                    staticColInfo.put("displayfield", staticColInfo.opt("label"));
                    staticColInfo.put("headerproperty", "");
                    //put JSONObject of field in fields JSON list
                    detailsTableColList.add(staticColInfo);
                    //increase count of fields
                    defcolno++;
                }
                //sort fields for proper presentation and sequence in UI
                jsonComp comp = new jsonComp();
                Collections.sort(detailsTableColList, comp);
            }
            if (detailsTableColList.size() > 0) {
                for (JSONObject customColInfo : detailsTableColList) {
                    detailsTableColArr.put(customColInfo);
                }
            }
            //put details table fields JSON in JSONArray
            jarr = detailsTableColArr;
        } catch (Exception e) {
            Logger.getLogger(CustomDesignServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return jarr;
    }
    /**
     * remove fields from map
     */
    public static TreeMap<String, String> removeExtraFieldsFromMap(TreeMap<String, String> map, TreeMap<String, String> removeMap) {
        Iterator it = removeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (map.containsKey(pair.getKey())) {
                map.remove(pair.getKey());
            }
        }
        return map;
    }
}
