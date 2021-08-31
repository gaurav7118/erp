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

package com.krawler.esp.handlers;

import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONException;
import java.util.ArrayList;

public class DashboardHandler {
    public static String getContentDiv(String typeStr) {
        String div = "<div  class=\""+typeStr +" statusitemimg\"></div>";
        return div;
    }

    public static String getContentSpan(String textStr,Boolean isDashboard){
        String upperspacing="";
        if(isDashboard)
            upperspacing="dashboardupdate";
        String span = "<span class=\"statusitemcontent "+upperspacing+"\">" +textStr + "</span><div class=\"statusclr\"></div>";
        return span;
    }

    public static String getLink(String message, String functionName) {
        return "<a href=# onclick='"+functionName+"'>"+message+"</a>";
    }

//    public static String getLink(String message, String functionName, String toolTip) {
//        return "<a href=# onclick='"+functionName+"' wtf:qtip='"+toolTip+"'>"+message+"</a>";
//    }

    public static String getFormatedAlert(String message, String cssClass,Boolean isDashboard) {
        String fmtMsg="";
        if(isDashboard)
            fmtMsg=getContentDiv(cssClass);
        fmtMsg+=message;
        return getContentSpan(fmtMsg,isDashboard);
    }

//    public static StringBuilder getSectionHeader(String headerText) {
//        StringBuilder sb=new StringBuilder();
//        sb.append("<div class=\"statuspanelheader\"><span class=\"statuspanelheadertext\">");
//        sb.append(headerText);
//        sb.append("</span></div>");
//        return sb;
//    }

//    private static StringBuilder createNewLink(String text, String functionName, String cssClass) {
//        StringBuilder newLink=new StringBuilder();
//        newLink.append("<li>");
//        newLink.append(getLink(text, functionName));
//        newLink.append("<ul class='").append(cssClass).append("'>");
//        newLink.append("</ul>");
//        newLink.append("</li>");
//        return newLink;
//    }

//    private static StringBuilder createNewLink(String text, String functionName, String cssClass, String toolTip) {
//        StringBuilder newLink=new StringBuilder();
//        newLink.append("<li>");
//        newLink.append(getLink(text, functionName, toolTip));
//        newLink.append("<ul class='").append(cssClass).append("'>");
//        newLink.append("</ul>");
//        newLink.append("</li>");
//        return newLink;
//    }
//
//    private static StringBuilder createNewLink(String text, String functionName) {
//        return createNewLink(text, functionName, "leadlist");
//    }

//    private static StringBuilder createSection(String title, String sectionid, StringBuilder innerData) {
//        StringBuilder data=new StringBuilder();
//        data.append(getSectionHeader(title));
//        data.append("<ul id='").append(sectionid).append("'>");
//        data.append(innerData);
//        data.append("</ul>");
//        data.append("<div>&nbsp;</div>");
//        return data;
//    }

//    private static StringBuilder createLeftPane(String title, StringBuilder innerData,StringBuilder outerData) {
//        StringBuilder buffer=new StringBuilder();
//        buffer.append(outerData);
//        buffer.append("<div class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
//        buffer.append(getSectionHeader("<span style='float: left;'>"+title+"</span><span style='float: right;font-weight:normal'></span>"));
//        buffer.append(innerData);        
//        buffer.append("</div></div>");
//        return buffer;
//    }
//    
//    public static String getDashboardData(Session session,HttpServletRequest request) throws ServiceException, HibernateException {
//		StringBuilder data=new StringBuilder();
//        try {
//            data.append("<div id=\"DashboardContent\" class=\"dashboardcontent\">");
//            User user = (User) session.get(User.class, sessionHandlerImpl.getUserid(request));
//            JSONObject perms = PermissionHandler.getPermissions(session, user.getUserID());
//            if (AccountingManager.isCompanyAdmin(user)) {
//                getCompanyAdminDashboardData(session, request, data, perms);
//            } else {
//                getUserDashboardData(session, request, data, perms);
//            }
//            data.append("</div>");
//        } catch (SessionExpiredException ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return data.toString();
//    }

//    private static void getCompanyAdminDashboardData(Session session, HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder temp=getCompanyAdminDashboardUpdateList(session, request, perms);
//        CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//        StringBuilder temp2;
//        if(pref!=null){
//            if(!pref.isWithoutInventory())
//                temp2=getDashBoardDataFlow(session, request, perms);
//            else
//                temp2=getDashBoardDataFlowWithoutInv(session, request, perms);
//            }
//        else
//            temp2=getDashBoardDataFlow(session, request, perms);
//        if(temp.length()>0)
//            data.append(createLeftPane("Updates", temp,temp2));
//        else
//            data.append(createLeftPane("Welcome to Krawler Accounting", getSetupWizard(),temp2));
//
////        data.append("<div class=\"linkspanel\">");
////
////        StringBuilder links;
////        links=getCompanyLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Company Preferences", "accCompanyPane",links));
////        if(pref!=null){
////            if(!pref.isWithoutInventory()){
////                links=getPurchaseManagementLinks(session, request, perms);
////                if(links.length()>0)
////                    data.append(createSection("Purchase Management", "accPurchaseManagementPane",links));
////            }
////        }
////        else{
////            links=getPurchaseManagementLinks(session, request, perms);
////            if(links.length()>0)
////                data.append(createSection("Purchase Management", "accPurchaseManagementPane",links));
////        }
////        links=getSalesManagementLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Sales/Billing Management", "accSalesManagementPane",links));
////
////        links=getJournalEntryLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Journal Entry", "accJournalEntryPane",links));
////
////        if(pref!=null){
////            if(pref.isWithoutInventory()){
////        links=getPaymentLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Payment", "accPaymentPane",links));
////            }
////        }
////         if(pref!=null){
////            if(!pref.isWithoutInventory()){
////                links=getProductLinks(session, request, perms);
////                if(links.length()>0)
////                    data.append(createSection("Product Preferences", "accProductPane",links));
////            }
////         }
////         else{
////            links=getProductLinks(session, request, perms);
////                if(links.length()>0)
////                    data.append(createSection("Product Preferences", "accProductPane",links));
////        }
////        links=getReportLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Reports", "accReportPane",links));
//
////        links=getMasterSettingLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Master Settings", "accMasterSettingPane",links));
//
////        links=getAdministrationLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Administration", "accAdministrationPane",links));
////        data.append("</div>");
//    }
//
//    private static void getUserDashboardData(Session session, HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder temp=getUserDashboardUpdateList(session, request, perms);
//        CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//        StringBuilder temp2;
//        if(pref!=null){
//            if(!pref.isWithoutInventory())
//                temp2=getDashBoardDataFlow(session, request, perms);
//            else
//                temp2=getDashBoardDataFlowWithoutInv(session, request, perms);
//            }
//        else
//            temp2=getDashBoardDataFlow(session, request, perms);
//        if(temp.length()>0)
//            data.append(createLeftPane("Updates", temp,temp2));
//        else
//            data.append(createLeftPane("Welcome to Krawler Accounting", getSetupWizard(),temp2));
//
////        data.append("<div class=\"linkspanel\">");
////
////        StringBuilder links;
////         links=getCompanyLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Company Preferences", "accCompanyPane",links));
////         if(pref!=null){
////            if(!pref.isWithoutInventory()){
////            links=getPurchaseManagementLinks(session, request, perms);
////            if(links.length()>0)
////                data.append(createSection("Purchase Management", "accPurchaseManagementPane",links));
////            }
////         }
////         else{
////            links=getPurchaseManagementLinks(session, request, perms);
////            if(links.length()>0)
////                data.append(createSection("Purchase Management", "accPurchaseManagementPane",links));
////         }
////        links=getSalesManagementLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Sales/Billing Management", "accSalesManagementPane",links));
//
////        links=getJournalEntryLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Journal Entry", "accJournalEntryPane",links));
////        if(pref!=null){
////            if(pref.isWithoutInventory()){
////        links=getPaymentLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Payment", "accPaymentPane",links));
////            }
////        }
////         if(pref!=null){
////            if(!pref.isWithoutInventory()){
////            links=getProductLinks(session, request, perms);
////            if(links.length()>0)
////                data.append(createSection("Product Preferences", "accProductPane",links));
////            }
////         }
////         else{
////            links=getProductLinks(session, request, perms);
////            if(links.length()>0)
////                data.append(createSection("Product Preferences", "accProductPane",links));
////         }
// //       links=getReportLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Reports", "accReportPane",links));
////        links=getMasterSettingLinks(session, request, perms);
//
////        if(links.length()>0)
////            data.append(createSection("Master Settings", "accMasterSettingPane",links));
////        links=getAdministrationLinks(session, request, perms);
////        if(links.length()>0)
////            data.append(createSection("Administration", "accAdministrationPane",links));
////        data.append("</div>");
//    }
//
//    private static StringBuilder getCompanyAdminDashboardUpdateList(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalStr=new StringBuilder();
//        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//            String companyID = sessionHandlerImpl.getCompanyid(request);
//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms,true), ""));
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms,true), ""));
//            if(pref!=null){
//              if(!pref.isWithoutInventory())
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
//            }else
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
//        } catch (SessionExpiredException ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return finalStr;
//    }

//     private static StringBuilder getDashBoardDataFlow(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalStr=new StringBuilder();
//        try {
//            finalStr.append("<div class='firstflowlink'><IMG class='thickBlackBorder'  SRC='../../images/purchasemanagement.jpg' usemap='#AlienAreas1'><IMG class='thickBlackBorderInside1' SRC='../../images/customervendorinventory.jpg' usemap='#AlienAreas4'></div>");
//            finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' SRC='../../images/salesmanagement1.jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' SRC='../../images/accountmanagement.jpg' usemap='#AlienAreas5'></div>");
//            finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' SRC='../../images/financialreport.jpg' usemap='#AlienAreas3'></div>");
//
//            finalStr.append("<map name='AlienAreas1'>" );
//            finalStr.append("<area shape='rect' coords='50,60,150,160' href=# onclick='callBusinessContactWindow(false, null, null, false)'  wtf:qtip='Maintain all information about your vendors including contact information, account details, preferred delivery mode and payment term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
//            finalStr.append("<area shape='rect' coords='160,60,230,140' href=# onclick='callProductDetails(null,true)' wtf:qtip='Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
//            finalStr.append("<area shape='rect' coords='23,209,81,273' href=# onclick='callPurchaseReceipt(false,null)' wtf:qtip='Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
//            finalStr.append("<area shape='rect' coords='183,213,233,277' href=# onclick='callPurchaseOrder(false, null)' wtf:qtip='Easily create purchase order for your vendors. Include payment term and complete purchase information.'> ");
//            finalStr.append("<area shape='rect' coords='19,369,71,430' href=# onclick='callAgedPayable(true)' wtf:qtip='Keep a track record of all amount payables.'> ");
//            finalStr.append("<area shape='rect' coords='182,317,237,382' href=# onclick='callPurchaseInvoiceType()' wtf:qtip='Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
//            finalStr.append("<area shape='rect' coords='321,318,394,399' href=# onclick='callCreditNote(false)' wtf:qtip='Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
//            finalStr.append("<area shape='rect' coords='182,422,254,504' href=# onclick='callPayment()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("</map>");
//            
//            finalStr.append("<map name='AlienAreas2'>" );
//            finalStr.append("<area shape='rect' coords='53,76,119,139' href=# onclick='callBusinessContactWindow(false, null, null, true)' wtf:qtip='Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
//            finalStr.append("<area shape='rect' coords='153,73,220,149' href=# onclick='callProductDetails(null,true)' wtf:qtip='Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'>");
//            finalStr.append("<area shape='rect' coords='29,215,104,291' href=# onclick='callSalesReceipt(false,null)' wtf:qtip='Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
//            finalStr.append("<area shape='rect' coords='183,213,249,294' href=# onclick='callSalesOrder(false, null)' wtf:qtip='Record all details related to a customer purchase order by generating an associated sales order.'>");
//            finalStr.append("<area shape='rect' coords='19,369,83,453' href=# onclick='callAgedRecievable(true)' wtf:qtip='Keep a track record of all amount receivables'>");
//            finalStr.append("<area shape='rect' coords='182,320,253,395' href=# onclick='callInvoice(false,null)' wtf:qtip='Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
//            finalStr.append("<area shape='rect' coords='321,318,394,399' href=# onclick='callCreditNote(true)' wtf:qtip='If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
//            finalStr.append("<area shape='rect' coords='184,425,252,501' href=# onclick='callReceipt()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("</map>");
//
//            finalStr.append("<map name='AlienAreas3'>" );
//
//            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
//            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='callFinalStatement(0)' wtf:qtip='Track all major financial statements such as trial balance.'> ");
//            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callFinalStatement(1)' wtf:qtip='Track all major financial statements such as ledger.'> ");
//            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='callFinalStatement(2)' wtf:qtip='Track all major financial statements such as  trading and profit/loss statement '> ");
//            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='callFinalStatement(3)' wtf:qtip='Track all major financial statements such as balance sheet.'> ");
//
//
//            finalStr.append("<area shape='rect' coords='18,517,146,583' href=# onclick='callAgedPayable(true)' wtf:qtip='Keep a track record of all amount payables'> ");
//            finalStr.append("<area shape='rect' coords='22,598,142,683' href=# onclick='callAgedRecievable(true)' wtf:qtip='Keep a track record of all amount receivables'>");
//
//            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='Track all journal entries transactions entered into the system.'>");
//            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callInvoiceList()'  wtf:qtip='Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
//            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='callPurchaseOrderList()' wtf:qtip='View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
//            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callSalesOrderList()' wtf:qtip='View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
//            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callCreditNoteDetails()' wtf:qtip='View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
//            finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='ReceiptReport()' wtf:qtip='View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
//
//            finalStr.append("<area shape='rect' coords='294,147,394,254' href=# onclick='callGoodsReceiptList()' wtf:qtip='View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.'>");
//            finalStr.append("<area shape='rect' coords='291,260,383,354' href=# onclick='callDebitNoteDetails()' wtf:qtip='View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.'>");
//            finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callPaymentReport()' wtf:qtip='View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
//            finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callFrequentLedger(true,\"23\",\"Cash Book\",\"accountingbase cashbook\")' wtf:qtip='Monitor all cash transactions entered into the system for any time duration.'>");
//            finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(false,\"9\",\"Bank Book\",\"accountingbase bankbook\")' wtf:qtip='Monitor all transactions for a bank account for any time duration.'>");
//            finalStr.append("</map>");
//
//            finalStr.append("<map name='AlienAreas4'>" );
//            finalStr.append("<area shape='rect' coords='9,27,100,137' href=# onclick='callCustomerDetails(true)' wtf:qtip='Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
//            finalStr.append("<area shape='rect' coords='103,35,181,136' href=# onclick='callVendorDetails(true)'  wtf:qtip='Maintain all information about your vendors including contact information, account details, preferred delivery mode and payment term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
//            finalStr.append("<area shape='rect' coords='5,31,289,132' href=# onclick='callProductDetails()' wtf:qtip='Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
//            finalStr.append("</map>");
//
// 
//            finalStr.append("<map name='AlienAreas5'>" );
//            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.'>");
//            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.'>");
//            finalStr.append("</map>");
//        }
//
//        catch (Exception ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return finalStr;
//    }
//      
//     private static StringBuilder getDashBoardDataFlowWithoutInv(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//            StringBuilder finalStr=new StringBuilder();
//            try {
//            finalStr.append("<div class='firstwithoutinvflowlink'><IMG class='thickBlackBorder' SRC='../../images/salesmanagement2.jpg' usemap='#AlienAreas1'></div>");
//            finalStr.append("<div class='secondwithoutinvflowlink'><IMG class='thickBlackBorderInside4' SRC='../../images/customervendorwithoutinventory.jpg' usemap='#AlienAreas3'><IMG class='thickBlackBorderInside5' SRC='../../images/paymentmanagement.jpg' usemap='#AlienAreas4'><IMG class='thickBlackBorderInside5' SRC='../../images/accountmanagement.jpg' usemap='#AlienAreas5'></div>");
//            finalStr.append("<div class='thirdwithoutinvflowlink'><IMG class='thickBlackBorder' SRC='../../images/financialreport2.jpg' usemap='#AlienAreas2'></div>");
//            finalStr.append("<map name='AlienAreas1'>" );
//            finalStr.append("<area shape='rect' coords='23,36,139,169' href=# onclick='callCustomerDetails(null,true,false)' wtf:qtip='Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
//            finalStr.append("<area shape='rect' coords='29,193,100,291' href=# onclick='callBillingSalesReceipt(false,null)' wtf:qtip='Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
//            finalStr.append("<area shape='rect' coords='144,195,218,295' href=# onclick='callBillingInvoice(false,null)' wtf:qtip='Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
//            finalStr.append("<area shape='rect' coords='302,189,377,347' href=# onclick='callAgedRecievable(false)' wtf:qtip='Keep a track record of all amount receivables'>");
//            finalStr.append("<area shape='rect' coords='145,296,222,400' href=# onclick='callBillingReceipt()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("</map>");
//
//            finalStr.append("<map name='AlienAreas2'>" );
//
//            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
//            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='callFinalStatement(0)' wtf:qtip='Track all major financial statements such as trial balance.'> ");
//            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callFinalStatement(1)' wtf:qtip='Track all major financial statements such as ledger.'> ");
//            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='callFinalStatement(2)' wtf:qtip='Track all major financial statements such as  trading and profit/loss statement '> ");
//            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='callFinalStatement(3)' wtf:qtip='Track all major financial statements such as balance sheet.'> ");
//            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='Track all journal entries transactions entered into the system.'>");
//            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callBillingInvoiceList()' wtf:qtip='Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
//            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='BillingReceiptReport()' wtf:qtip='View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
//            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callPaymentReport()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callFrequentLedger(true,\"23\",\"Cash Book\",\"accountingbase cashbook\")' wtf:qtip='Monitor all cash transactions entered into the system for any time duration.'>");
//            finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='callFrequentLedger(false,\"9\",\"Bank Book\",\"accountingbase bankbook\")' wtf:qtip='Monitor all transactions for a bank account for any time duration.'>");
//            finalStr.append("</map>");
//  
//            finalStr.append("<map name='AlienAreas3'>" );
//            finalStr.append("<area shape='rect' coords='9,27,100,137' href=# onclick='callCustomerDetails(false)' wtf:qtip='Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
//            finalStr.append("<area shape='rect' coords='103,35,181,136' href=# onclick='callVendorDetails(false)'  wtf:qtip='Maintain all information about your vendors including contact information, account details, preferred delivery mode and payment term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
//            finalStr.append("</map>");
//
//
//            finalStr.append("<map name='AlienAreas4'>" );
//            finalStr.append("<area shape='rect' coords='14,30,93,133' href=# onclick='callBillingReceipt()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("<area shape='rect' coords='107,34,196,128' href=# onclick='callPayment()' wtf:qtip='Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
//            finalStr.append("</map>");
//
//            finalStr.append("<map name='AlienAreas5'>" );
//            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.'>");
//            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.'>");
//            finalStr.append("</map>");
//        }
//        catch (Exception ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return finalStr;
//    }

//    private static StringBuilder getUserDashboardUpdateList(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalStr=new StringBuilder();
//        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//            String companyID = sessionHandlerImpl.getCompanyid(request);
//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms,true), ""));
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms,true), ""));
//            if(pref!=null){
//              if(!pref.isWithoutInventory())
//                  finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
//            }else
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
//            } catch (SessionExpiredException ex) {
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        }
//        return finalStr;
//    }

//    public static StringBuilder getSysAdminLinks(Session session, HttpServletRequest request, JSONObject perms) {
//        StringBuilder newLink = new StringBuilder();
//
//        newLink.append(createNewLink("List of the companies","callSystemAdmin()"));
//
//        return newLink;
//    }

//    public static StringBuilder getCompanyLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//
//        try {
//            if(isPermitted(perms, "accpref", "view"))
//                newLink.append(createNewLink("Account Preferences","callAccountPref()","leadlist","Maintain general settings for your organization such as financial year settings, account settings, automatic number generation and email settings."));
//            if(isPermitted(perms, "coa", "view"))
//                newLink.append(createNewLink("Chart of Accounts","callCOA()","leadlist","Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts."));
//            if(isPermitted(perms, "customer", "view"))
//                newLink.append(createNewLink("Accounts Receivable/Customer(s)","callCustomerDetails()","leadlist","Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts."));
//            if(isPermitted(perms, "vendor", "view"))
//                newLink.append(createNewLink("Accounts Payable/Vendor(s)","callVendorDetails()","leadlist","Maintain all information about your vendors including contact information, account details, preferred delivery mode and payment term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts."));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//
//        return finalString;
//    }

//    public static StringBuilder getMasterSettingLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//           newLink.append(createNewLink("Master Configuration","callMasterConfiguration()","leadlist","Define settings for payment methods, payment terms, unit of measure, bank names, preferred delivery mode and more."));
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }
//
//    public static StringBuilder getProductLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//
//        try {
//            if(isPermitted(perms, "product", "view"))
//                newLink.append(createNewLink("Product List","callProductDetails()","leadlist","Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product."));
////            if(isPermitted(perms, "uom", "view"))
////                newLink.append(createNewLink("Unit of measure","callUOM()"));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//
//        return finalString;
//    }
//
//     public static StringBuilder getAdministrationLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//            if(isPermitted(perms, "useradmin", "view"))
//                newLink.append(createNewLink("User Administration","loadAdminPage(1)","leadlist","Easily manage all users in the system. Assign roles and permission to individual users in accordance to their work functions."));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }

//    public static StringBuilder getPurchaseManagementLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//                newLink.append(createNewLink("Create Cash Purchase","callPurchaseReceipt(false,null)","leadlist","Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase."));
//                newLink.append(createNewLink("Create Purchase Order","callPurchaseOrder(false, null)","leadlist","Easily create purchase order for your vendors. Include payment term and complete purchase information."));
//                newLink.append(createNewLink("Create Vendor Invoice","callPurchaseInvoiceType()","leadlist","Provide your vendors with receipt on delivery of purchased goods. Record product and payment details."));
//                newLink.append(createNewLink("Create Debit Note","callCreditNote(false)","leadlist","Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc."));
//         } catch (Exception e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }
//
//    public static StringBuilder getSalesManagementLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//            if(pref!=null){
//                if(pref.isWithoutInventory()){
//                    newLink.append(createNewLink("Create Cash Sales"," callBillingSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
//                        newLink.append(createNewLink("Create Invoice","callBillingInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
//                }
//                else{
//                    newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
//                    newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
//                    if(isPermitted(perms, "invoice", "create"))
//                        newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
//                    if(isPermitted(perms, "invoice", "view"))
//                        newLink.append(createNewLink("Create Credit Note","callCreditNote(true)","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));
//
//                }
//            }else{
//                newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
//                newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
//                if(isPermitted(perms, "invoice", "create"))
//                    newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
//                if(isPermitted(perms, "invoice", "view"))
//                    newLink.append(createNewLink("Create Credit Note","callCreditNote(true)","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));
//            }
//            //   TODO         if(isPermitted(perms, "invoice", "view"))
////            if(isPermitted(perms, "invoice", "view"))
////                newLink.append(createNewLink("Credit Note","callCreditMemo()"));
////            if(isPermitted(perms, "invoice", "view"))
////                newLink.append(createNewLink("Credit Note/Receipt","callReceipt()"));
////               newLink.append(createNewLink("Debit Note/Payment","callDebitNote()"));
////                newLink.append(createNewLink("Payment","callPayment()"));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }

//    public static StringBuilder getJournalEntryLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//                if(isPermitted(perms, "journalentry", "create"))
//                    newLink.append(createNewLink("Make a Journal Entry","callJournalEntry()","leadlist","Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions."));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }
//
//    public static StringBuilder getPaymentLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//            if(pref!=null){
//                if(pref.isWithoutInventory())
//                    newLink.append(createNewLink("Receive Payment(s)","callBillingReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
//                 else
//                     newLink.append(createNewLink("Receive Payment(s)","callReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
//            }
//            else
//                newLink.append(createNewLink("Receive Payment(s)","callReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
//            newLink.append(createNewLink("Make Payment(s)","callPayment()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
//            } catch (Exception e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//        return finalString;
//    }

//    public static StringBuilder getReportLinks(Session session, HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder finalString = new StringBuilder();
//        StringBuilder newLink = new StringBuilder();
//
//        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,sessionHandlerImpl.getCompanyid(request));
//
//                //newLink.append(createNewLink("Products List","callProductList()"));
//                //newLink.append(createNewLink("Customer List","callCustomerReport()"));
//                //newLink.append(createNewLink("Vendor List","callVendorReport()"));
////            if(isPermitted(perms, "invoice", "view"))
////                newLink.append(createNewLink("Sales Register","callInvoiceDetails()"));
////            if(isPermitted(perms, "creditnote", "view"))
////                newLink.append(createNewLink("Credit Note","callCreditMemoDetails()"));
//              newLink.append(createNewLink("<b>Financial Statements</b>","callFinalStatement()","leadlist","Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet."));
//               if(pref!=null){
//              if(pref.isWithoutInventory()){
//                        newLink.append(createNewLink("Invoice and Cash Sales Report","callBillingInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
//                        newLink.append(createNewLink("Received Payment","BillingReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
//                    }
//                    else{
//                        if(isPermitted(perms, "invoice", "view"))
//                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
//                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
//                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
//                        if(isPermitted(perms, "creditnote", "view"))
//                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
//                        if(isPermitted(perms, "receipt", "view"))
//                            newLink.append(createNewLink("Received Payment(s)","ReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
//
//                        newLink.append(createNewLink("Cash and Credit Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
//                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
//                   }
//               }
//               else{
//                  if(isPermitted(perms, "invoice", "view"))
//                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
//                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
//                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
//                        if(isPermitted(perms, "creditnote", "view"))
//                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
//                        if(isPermitted(perms, "receipt", "view"))
//                            newLink.append(createNewLink("Receive Payment","ReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
//
//                        newLink.append(createNewLink("vendor invoice and Cash Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
//                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
//              }               
////             newLink.append(createNewLink("Book Reports","callBookReport()"));
//               newLink.append(createNewLink("Payment Made","callPaymentReport()","leadlist","View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
//             if(isPermitted(perms, "journalentry", "view"))
//                newLink.append(createNewLink("Journal Entry","callJournalEntryDetails()","leadlist","Track all journal entries transactions entered into the system."));
////            if(isPermitted(perms, "ledger", "view"))
////                newLink.append(createNewLink("Ledger","callLedger()"));
////            if(isPermitted(perms, "trialbalance", "view"))
////                newLink.append(createNewLink("Trial Balance","TrialBalance()"));
////            if(isPermitted(perms, "trading", "view"))
////                newLink.append(createNewLink("Trading","Trading()"));
////            if(isPermitted(perms, "pl", "view"))
////                newLink.append(createNewLink("Profit and Loss","ProfitandLoss()"));
////            if(isPermitted(perms, "trading", "view")&&isPermitted(perms, "pl", "view"))
////                newLink.append(createNewLink("Trading, Profit and Loss","TradingProfitLoss()"));
////            if(isPermitted(perms, "bsheet", "view"))
////                newLink.append(createNewLink("Balance Sheet","BalanceSheet()"));
//            
//            if(isPermitted(perms, "cashbook", "view"))
//                newLink.append(createNewLink("Cash Book","callFrequentLedger(true,\"23\",\"Cash Book\",\"accountingbase cashbook\")","leadlist","Monitor all cash transactions entered into the system for any time duration."));
//            if(isPermitted(perms, "bankbook", "view"))
//                newLink.append(createNewLink("Bank Book","callFrequentLedger(false,\"9\",\"Bank Book\",\"accountingbase bankbook\")","leadlist","Monitor all transactions for a bank account for any time duration."));
//////            if(isPermitted(perms, "bankbook", "view"))
//                newLink.append(createNewLink("Aged Receivable", "callAgedRecievable()","leadlist","Keep a track record of all amount receivables"));
//////            if(isPermitted(perms, "bankbook", "view"))
//                newLink.append(createNewLink("Aged Payable", "callAgedPayable()","leadlist","Keep a track record of all amount payables"));
//
//            if(isPermitted(perms, "audittrail", "view"))
//                newLink.append(createNewLink("Audit Trail","callAuditTrail()","leadlist","Track all user activities through comprehensive Accounting system records"));
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        } finally {
//            finalString = newLink;
//        }
//
//        return finalString;
//    }
//    
    public static boolean isPermitted(JSONObject perms, String featureName, String activityName) throws JSONException {
        int perm = perms.getJSONObject("Perm").getJSONObject(featureName).optInt(activityName);
        int uperm = perms.getJSONObject("UPerm").optInt(featureName);
        if ((perm & uperm) == perm) {
            return true;
        }
        return false;
    }

//
//    public static StringBuilder getCompaniesUpdationInfo(Session session, HttpServletRequest request, JSONObject perms,Boolean isDashboard) throws ServiceException {
//        StringBuilder finalString = new StringBuilder();
//        try {
//            String query="from Company where modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Integer[]{0,3});
//            Iterator itr=list.iterator();
//            while(itr.hasNext()){
//                Company company=(Company)itr.next();
//                finalString.append(getFormatedAlert("New company "+getLink(company.getCompanyName(), "callSystemAdmin()")+" created","systemadmin",isDashboard));
//            }
//            query="from Company where modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Integer[]{0,3});
//            itr=list.iterator();
//            String link;
//            while(itr.hasNext()){
//                Company company=(Company)itr.next();
//                link=company.getCompanyName();
//                if(isPermitted(perms, "companyadmin", "view"))
//                    link=getLink(link, "callSystemAdmin()");
//                 finalString.append(getFormatedAlert("Company "+link+" modified","systemadmin",isDashboard));
//            }
//        } catch (JSONException e) {
//            throw ServiceException.FAILURE(e.getMessage(), e);
//        }
//        return finalString;
//    }

    public static String joinArrayList(ArrayList arr, String sep) {
        StringBuilder sb=new StringBuilder();
        if(!arr.isEmpty())sb.append(arr.get(0));
        for(int i=1;i<arr.size();i++){
            sb.append(sep+arr.get(i));
        }
        return sb.toString();
    }

//    public static StringBuilder getSetupWizard(){
//        String imgPath="../../images/welcome/";
//        StringBuilder buffer = new StringBuilder();
//        buffer.append(createHelpSection(getLink("Set up accounts","callCOA()"),
//                "You can add various types of income, expense, asset, liability accounts and their descriptions",
//                imgPath+"coa.gif",""));
//        buffer.append(createHelpSection("Add "+getLink("Customers","callCustomerDetails()")+" and "+getLink("Vendors","callVendorDetails()"),
//                "From here, you can add new customers and vendors and edit information about a customer/vendor.",
//                imgPath+"customer.png",""));
//        buffer.append(createHelpSection(getLink("Invoice","callInvoice(false,null)"),
//                "You can track the money that comes into your business by filling out invoices to give to your customers.",
//                imgPath+"invoice.png","Generate invoices quickly with instant access to product, pricing and customer information"));
//        buffer.append(createHelpSection(getLink("Make a Journal Entry","callJournalEntry()"),
//                "You can add multiple accounting transactions as they occur in the course of your business",
//                imgPath+"journalentry.png","Define components of a journal entry such as accounts, descriptions, and amounts"));
//        buffer.append(createHelpSection(getLink("Add Product & Services","callProductDetails()"),
//                "You can add products and services your business sells or purchases",
//                imgPath+"product.png","you can edit information about a product or service, such its description, or the rate you charge"));
//        buffer.append(createHelpSection(getLink("Receive Payment","callReceipt()"),
//                "Fill this form if you receive a payment from a customer in response to an invoice",
//                imgPath+"receipt.gif",""));
//        return buffer;
//    }
//
//    private static StringBuilder createHelpSection(String title, String message, String imgPath, String tipMsg) {
//        StringBuilder data=new StringBuilder();
//       data.append("<div>&nbsp;</div>");
//         data.append("<h2 class='bullet'>"+title+"</h2>");
//        data.append("<div style='padding:10px 20px'>"+message+"</div>");
//        data.append("<div class='centered'><img src='"+imgPath+"' width='300px' wtf:qtip='"+tipMsg+"' wtf:qtitle='Tip'></div>");
//        return data;
//    }

}
