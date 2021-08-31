
<%--
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
--%>
<%@page import="javax.mail.Session"%>
<%@page import="org.hibernate.Query"%>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.DriverManager"%>
<%@ page import="com.krawler.common.util.URLUtil" %>
<%@ page import="com.krawler.esp.utils.ConfigReader" %>
<%@page import="com.krawler.esp.handlers.SendMailHandler"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.spring.sessionHandler.sessionHandlerImpl" %>
<%@page import="com.krawler.common.session.SessionExpiredException" %>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFCell"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFRow"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFSheet"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFWorkbook"%>
<%
    Connection conn = null;
    String serverip = request.getParameter("serverip");
    String port = "3306";
    String dbName = request.getParameter("dbname");
    String userName = request.getParameter("username");
    String password = request.getParameter("password");
    String subdomain = request.getParameter("subdomain");
    if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
        throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
    }
    String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
    String driver = "com.mysql.jdbc.Driver";

    Class.forName(driver).newInstance();
    conn = DriverManager.getConnection(connectString, userName, password);

    ResultSet resultset = null;
    ResultSet rscompany = null;
    String companyid = "";
    PreparedStatement pstmt = null;
    pstmt = conn.prepareStatement("select companyid from company where subdomain = ?");
    pstmt.setString(1, subdomain);
    rscompany = pstmt.executeQuery();
    if (rscompany.next()) {
        companyid = rscompany.getString("companyid");
    }

    ArrayList<String> queryList = new ArrayList<String>();

    queryList.add("SELECT *,COUNT(*) as count FROM productbranddiscountdetails");
    queryList.add("SELECT *,COUNT(*) as count FROM croneschedule");
    queryList.add("SELECT *,COUNT(*) as count FROM taxadjustment");
    queryList.add("SELECT *,COUNT(*) as count FROM baddebtinvoicemapping");
    queryList.add("SELECT *,COUNT(*) as count FROM baddebtpurchaseinvoicemapping");
    queryList.add("SELECT *,COUNT(*) as count FROM timezone");
    queryList.add("SELECT *,COUNT(*) as count FROM templatefieldinfo");
    queryList.add("SELECT *,COUNT(*) as count FROM reportrolemap");
    queryList.add("SELECT *,COUNT(*) as count FROM reportmaster");
    queryList.add("SELECT *,COUNT(*) as count FROM producttype");
    queryList.add("SELECT *,COUNT(*) as count FROM modules");
    queryList.add("SELECT *,COUNT(*) as count FROM samplefiledata");
    queryList.add("SELECT *,COUNT(*) as count FROM reportschema");
    queryList.add("SELECT *,COUNT(*) as count FROM moduleCategory");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcompanypreferences");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractdoc");
    queryList.add("SELECT *,COUNT(*) as count FROM mastergroup");
    queryList.add("SELECT *,COUNT(*) as count FROM language");
    queryList.add("SELECT *,COUNT(*) as count FROM helpedit");
    queryList.add("SELECT *,COUNT(*) as count FROM rolepermission where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM gstformgenerationhistory where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM taxperiod where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customreports where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accounitng_period_lock_info where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM featurelist");
    queryList.add("SELECT *,COUNT(*) as count FROM exportcompanypref");
    queryList.add("SELECT *,COUNT(*) as count FROM exchangerate");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultuom");
    queryList.add("SELECT *,COUNT(*) as count FROM defaulttax1099category");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultpaymentmethod");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultmasteritem");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultgst");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultexchangeratedetails");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultexchangerate");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultcreditterm");
    queryList.add("SELECT *,COUNT(*) as count FROM default_header");
    queryList.add("SELECT *,COUNT(*) as count FROM dateformat");
    queryList.add("SELECT *,COUNT(*) as count FROM currency");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultgroupaccmap where defaultlayoutgroup in (select id from defaultlayoutgroup where template in (select id from defaulttemplatepnl where country in (select id from country where id in (select id from company where companyid = '" + companyid + "'))))");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultgroupmapfortotal where groupid in (select id from defaultlayoutgroup where template in (select id from defaulttemplatepnl where country in (select id from country where id in (select id from company where companyid = '" + companyid + "'))))");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultlayoutgroup where template in (select id from defaulttemplatepnl where country in (select id from country where id in (select id from company where companyid = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM defaulttemplatepnl where country in (select id from country where id in (select id from company where companyid = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM country");
    queryList.add("SELECT *,COUNT(*) as count FROM companytype");
    queryList.add("SELECT *,COUNT(*) as count FROM card");
    queryList.add("SELECT *,COUNT(*) as count FROM auditgroup");
    queryList.add("SELECT *,COUNT(*) as count FROM auditaction");
    queryList.add("SELECT *,COUNT(*) as count FROM audit_group");
    queryList.add("SELECT *,COUNT(*) as count FROM audit_action");
    queryList.add("SELECT *,COUNT(*) as count FROM activitylist");
    queryList.add("SELECT *,COUNT(*) as count FROM locationlevel");

    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailreceipttodebitnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailpaymenttocreditnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailreceipttoadvancepayment where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailpaymenttoadvancepayment where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptdetails where receipt in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailreceipt where receipt in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptadvancedetail where receipt in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptdetailotherwise where receipt in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptdetailsloan where receipt in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptwriteoff where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptlinking where docid in (select id from receipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receipt where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM cndiscount where creditnote in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM cndetails where creditNote in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM cntaxentry where creditnote in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM creditnotetermmap where creditnote in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM creditnotpayment where cnid in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM openingbalancecreditnotecustomdata where openingbalancecreditnoteid in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM creditnotelinking where docid in (select id from creditnote where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM creditterm where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM creditnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM loanrules where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM repaymentdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM disbursement where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicedetails where invoice in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM excludedoutstandingrecords  where invoice in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM openingbalanceinvoicecustomdata  where openingbalanceinvoiceid in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicelinking  where docid in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoiceinused where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicecontractmapping where invoice in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicedoccompmap where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicedocuments where id in (select documentid from invoicedoccompmap where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicetermsmap where invoice in (select id from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM repeatedinvoices where id in (select repeateinvoice from invoice where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryplannerannouncement where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryplanner where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicewriteoff where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoice where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM dndiscount where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM dndetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM debitnotepayment where dnid in (select id from debitnote  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM debitnotetermmap where debitnote in (select id from debitnote  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM dntaxentry where debitnote in (select id from debitnote  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM openingbalancedebitnotecustomdata where openingbalancedebitnoteid in (select id from debitnote  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM debitnotelinking where docid in (select id from debitnote  where company = '" + companyid + "')");

    queryList.add("SELECT *,COUNT(*) as count FROM docs where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM comment where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM debitnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM paymentdetailotherwise where payment in (select id from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM paymentdetail where payment in (select id from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM linkdetailpayment where payment in (select id from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM advancedetail where payment in (select id from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM repeatedpaymentchequedetail where repeatedpaymentid in (select repeatpayment from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM repeatedpayment where id in (select repeatpayment from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count  FROM paymentlinking where docid in (select id from payment where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM payment where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM expenseggrdetails where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM grdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count  FROM openingbalancevendorinvoicecustomdata where openingbalancevendorinvoiceid in (select id from goodsreceipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count  FROM goodsreceiptlinking where docid in (select id from goodsreceipt where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM goodsreceipt where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM prdetailscustomdata where prdetailsid in (select id from prdetails  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM prdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM prflowtargets where flowid in (select id from prflow where companyid = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM prflow where companyid= '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM purchasereturncustomdata where purchasereturnid in (select id from purchasereturn where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM purchasereturnlinking where docid in (select id from purchasereturn where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM purchasereturn where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM srdetailscustomdata where srdetailsid in (select id from srdetails where salesreturn in (select id from salesreturn where  company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM srdetails where salesreturn in (select id from salesreturn where  company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesreturncustomdata where salesreturnid in (select id from salesreturn where  company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesreturnlinking where docid in (select id from salesreturn where  company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesreturn where  company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM grodetailscustomdata where grodetailsid in (select id from grodetails where grorder in (select id from grorder where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM grodetails where grorder in (select id from grorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM goodsreceiptorderlinking where docid in (select id from grorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM grordercustomdata where goodsreceiptorderid in (select id from grorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM goodsreceiptordertermmap where goodsreceiptorder in (select id from grorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM grorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingcndiscount where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingcndetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingcreditnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingreceiptdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingreceiptdetailotherwise where billingreceipt in (select id from billingreceipt  where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM billingreceipt where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billinginvoicedetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billinginvoice where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingdndiscount where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingdndetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingdebitnote where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingpaymentdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingpaymentdetailotherwise where billingpayment in (select id from billingpayment  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM billingpayment where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billinggrdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billinggr where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM depriciationdetail where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM assetdepreciationdetail where assetdetail in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assetdetailsinvdetailmapping where assetdetails in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM fixedassetopeningmapping where assetdetails in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assetmaintenancescheduler where assetdetails in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assetmaintenanceschedulerobject where assetdetails in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assetdetailcustomdata where assetDetailsId in (select id from assetdetail where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assetdetail where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM asset where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM pbdetails where build in (select id from productbuild where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM jedetail where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM jedetail_optimized where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accjedetailcustomdata  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accjecustomdata  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM repeatedjechequedetail where repeatedjeid in (select repeateje from journalentry where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM revenuejeinvoicemapping where jeid in (select id from journalentry where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM journalentry where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM journalentryupdatehistory where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM salesorderdetailcustomdata where soDetailID in (select id from sodetails where salesorder in (select id from salesorder where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM sodetailsapprovermapping where sodetails in (select id from sodetails where salesorder in (select id from salesorder where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM sodetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM sodetails where salesorder in (select id from salesorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesordercustomdata where soID in (select id from salesorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesordertermmap where salesorder in (select id from salesorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM solinking where docid in (select id from salesorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingshippingaddresses  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM podetailproductcustomdata where poDetailID in (select id from podetails where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM pootherdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaseorderdetailcustomdata where poDetailID in (select id from podetails where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM podetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaseordercustomdata where poID in (select id from purchaseorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM polinking where docid in (select id from purchaseorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaseordertermmap where purchaseorder in (select id from purchaseorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaseorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM requestforquotationlinking where docid in (select id from requestforquotation where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM requestforquotationdetail where requestforquotation in (select id from requestforquotation where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM requestforquotation where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM rfqcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM requestforquotationdetailcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaserequisitiondetail where purchaserequisition in (select id from purchaserequisition where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaserequisitioncustomdata where purchaserequisitionid in (select id from purchaserequisition where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaserequisitiondetailcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaserequisitionlinking where docid in (select id from purchaserequisition where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaserequisition where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingsodetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingsalesorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingpodetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM billingpurchaseorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM quotationdetailscustomdata where quotationdetailsid in (select id from quotationdetails where quotation in (select id from quotation where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM cqdetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM vqdetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM prdetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM srdetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM grodetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM quotationdetails where quotation in (select id from quotation where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM quotationcustomdata where quotationid in (select id from quotation where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM quotationtermmap where quotation in (select id from quotation where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM cqlinking where docid in (select id from quotation where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM quotation where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM discount where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM serialcustomdata where serialdocumentmappingid in (select id from serialdocumentmapping where serialid in (select id from newbatchserial where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM locationbatchdocumentapprovermapping where locationmapping in (select id from locationbatchdocumentmapping where batchmapid in (select id from newproductbatch where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM locationbatchrejectormapping where locationdocumentmapping in (select id from locationbatchdocumentmapping where batchmapid in (select id from newproductbatch where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM locationbatchdocumentmapping where batchmapid in (select id from newproductbatch where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM serialdocumentapprovermapping where serialdocumentmapping in (select id from serialdocumentmapping where serialid in (select id from newbatchserial where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM serialdocumentmapping where serialid in (select id from newbatchserial where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM newproductbatch where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM newbatchserial where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM consignreqapprovermapping where consignmentrequest in (select id from consignreqapprovalrules where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM consignreqapprovalrules where warehouse in (select id from inventorywarehouse where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM inventorywarehouse where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM consignreqlocmapping where inventorylocation in (select id from inventorylocation where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM inventorylocation where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM inventory where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM productcyclecount where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM pricelist where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM cyclecount where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpqualitycontrol where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accountopeningtransaction where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM bomdetail where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productassembly where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assemblysubproductbatchserialmapping where mainproductserial in (select id from batchserial where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM assemblysubproductbatchserialmapping where subproductbatch in (select id from productbatch where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM batchserialmapping where purchaseSerial in (select id from batchserial where batch in (select id from productbatch where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM replacementproductbatchdetailsmapping where batchserial in (select id from batchserial where batch in (select id from productbatch where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM returnserialmapping where mapserial in (select id from batchserial where batch in (select id from productbatch where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM batchserial where batch in (select id from productbatch where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM returnbatchmapping where batchmap in (select id from productbatch where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salespurchasebatchmapping where salesBatch in (select id from productbatch where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productbatch where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accjedetailproductcustomdata  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accproductcustomdata  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM fixedassetopening where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM lockassemblyquantitymapping where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productbuild where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productcategorymapping where productid in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productcustomfieldhistory where product in (select id from product where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM productserial where product in (select id from product where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM productstockpricelist  where product in (select id from product where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM pricingbandmasterdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM pricingbandmaster where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM product where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM amendingprice  where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM approvalrules where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM approvalhistory where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultcap");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultcap where vendoraccount in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM ibgreceivingbankdetails where vendor in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM cimbreceivingdetails where vendor in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendoraddresses where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorcategorymapping where vendorid in (select id from vendor where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorcustomdata where vendorId in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorproductmapping where vendorid in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationversiondetailscustomdata where quotationdetailsid in (select id from vendorquotationversiondetails  where quotationversion in (select id from vendorquotationversion where vendor in (select id from vendor where company = '" + companyid + "') ))");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationversioncustomdata where quotationid in (select id from vendorquotationversion where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationversiondetails where quotationversion in (select id from vendorquotationversion where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationversion where vendor in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationcustomdata where vendorquotationid in (select id from vendorquotation where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationdetailscustomdata where vendorquotationdetailsid in (select id from vendorquotationdetails  where vendorquotation in (select id from vendorquotation where vendor in (select id from vendor where company = '" + companyid + "') ))");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationtermmap where vendorquotation in (select id from vendorquotation where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationdetails where vendorquotation in (select id from vendorquotation where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vqlinking where docid in (select id from vendorquotation where vendor in (select id from vendor where company = '" + companyid + "') )");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotation where vendor in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendor where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM vendoragentmapping where vendorid in (select id from vendor where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendoraddressdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM contractdetailcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM contractcustomdata where contractid in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM contractdetails where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM contractfile where contractid  in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM docontractmapping where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM maintenance where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM productreplacement where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM productreplacementdetail where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM servicedetails where contract in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM contractdates where contract  in (select id from contract where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM contract where customer in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customeraddresses where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customercategorymapping where customerid in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customercustomdata where customerId  in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customerproductmapping where  customerid in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customervendormapping where customeraccountid in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultcap where customeraccount in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryordercustomdata where deliveryOrderId in (select id from deliveryorder where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM dolinking where docid in (select id from deliveryorder where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM dodetailscustomdata where dodetailsid in (select id from dodetails where deliveryorder in (select id from deliveryorder where customer in (select id from customer where company = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM dodetailproductcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM wastagedetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM dodetails where deliveryorder in (select id from deliveryorder where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryordertermmap where deliveryorder in (select id from deliveryorder where customer in (select id from customer where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryorder where customer in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customerwarehousemap where customer in (select id from customer where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM customeraddressdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM companyaddressdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customer where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customizedagedduration  where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customizereportmapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customizereportheader");
    queryList.add("SELECT *,COUNT(*) as count FROM productfieldsandmodulesmapping where  productfieldsandmodulesmapping.fieldid in (select customizereportmapping.id from customizereportmapping where  customizereportmapping.company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM taxlist where tax in (select id from tax where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM tax where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM tax1099accounts where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM tax1099category where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM paydetail where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM chequelayout where paymentmethod in (select id from paymentmethod where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM labourcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM labour where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM paymentmethod where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM compaccpreferences where id = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM companyholiday  where id = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM companymapping where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accountbudget where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM accountforecast  where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM accountmapping  where childaccountid in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM accountcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM assethistory where assetid in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM bankreconciliationdetail where bankReconciliation in (select id from bankreconciliation where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM bankreconciliation where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM depriciationdetail where account  in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM closingaccountbalance where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM pnlaccountmap where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM groupaccmap where layoutgroup in (select id from layoutgroup where template in (select id from templatepnl where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM groupmapfortotal where groupid in (select id from layoutgroup where template in (select id from templatepnl where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM layoutgroup where template in (select id from templatepnl where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM templatepnl where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM ibgbankdetails where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM cimbbankdetails where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicetermssales where account in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM distributebalance where accountid in (select id from account where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM account where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM costcenter where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultaccount where groupname in (select id from accgroup where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM accgroup where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultaccount where parent is null and companytype is null");
    queryList.add("SELECT *,COUNT(*) as count FROM accgroup where company is null");
    queryList.add("SELECT *,COUNT(*) as count FROM userlogin where userid in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM userpermission where roleusermapping in (select id from role_user_mapping where userid in (select userid from users where company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM userpermission where roleusermapping is null");
    queryList.add("SELECT *,COUNT(*) as count FROM role_user_mapping where userid in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM role where company is null");
    queryList.add("SELECT *,COUNT(*) as count FROM role where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM rolelist where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM rolelist where company is null");
    queryList.add("SELECT *,COUNT(*) as count FROM salescomissionscehma where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM salescommission where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM audittrail where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM pdfreporttemplate where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM SAVED_SEARCH_QUERY where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM audit_trail  where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM revaltime where userid  in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM revaluationhistory where userid in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM column_header where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM department where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customdesigntemplate where createdby in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM emailtemplatefiles  where creator in  (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM importlog where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM projreport_template where userid  in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM projreport_template where tempid = 'ff80808140a555fe0140a588bcee0007'");
    queryList.add("SELECT *,COUNT(*) as count FROM widgetmanagement where user in (select userid from users where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM useractivedaysdetails where userid in (select userid from users where company = '" + companyid + "')");

    queryList.add("SELECT *,COUNT(*) as count FROM loanrules where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM users where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM uomschematype where stockuom in (select id from uom where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM uomschema where purchaseuom in (select id from uom where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM uomschema where salesuom in (select id from uom where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM uom where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM fieldcombodatamapping where childid in (select id from fieldcombodata where fieldid in (select id from fieldparams where companyid = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM fieldcombodata where fieldid in (select id from fieldparams where companyid = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM fieldparams where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM apiresponse where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM chequesequenceformat where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM sequenceformat where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM cheque where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM masteritemprice where type in (select id from pricetype where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM masteritempriceformula where type collate utf8_general_ci in (select id from pricetype where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM shelflocation where  company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM pdftemplateconfig where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM pricetype where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM assetmaintenanceworkorderdetail where assetmaintenanceworkorder in (select id from assetmaintenanceworkorder where assignedto in (select id from masteritem where company = '" + companyid + "' ))");
    queryList.add("SELECT *,COUNT(*) as count FROM assetmaintenanceworkorder where assignedto in (select id from masteritem where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM itempackingdetails where packingdolistdetails in (select id from packingdolistdetails  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM shipingdodetails where packingdolistdetails in (select id from packingdolistdetails  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM packingdolistdetails where packingdolist in (select id from packingdolist  where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM packingdolist where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM packages where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM productpricerule where category in (select id from masteritem where company = '" + companyid + "' )");
    queryList.add("SELECT *,COUNT(*) as count FROM masteritem where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM moduletemplate where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM multilevelapprovalruletargetusers where ruleid in (select id from multilevelapprovalrule where companyid = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM multilevelapprovalrule where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mailnotification where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM notifictionrulesrecurringdetail where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM notetype where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM extracompanypreferences where id = (select companyid from company where companyid = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM exchangeratedetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM girofileinfo where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM girofilelog where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM yearlock where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpaddressdetails where mrpcontract in (select id from mrpcontract where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM workcentrecustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM machinecustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM workordercustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractdetailscustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM routingtemplatecustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractdetails where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractdocmappingtemp where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractdocmapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM product_workcentre_mapping where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM material_workcentre_mapping where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM machinemanratio where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM machine_process_mapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM substitute_machine_mapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM machine_work_center_mapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrp_job_order where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM routing_template where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM workorder where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontractmapping where mrpcontract in (select id from mrpcontract where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM workorder_labour_mapping where workorderid in (select id from workorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM workorder_workcentre_mapping where workorderid in (select id from workorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM workorder_machine_mapping where workorderid in (select id from workorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM routingtemplate_labour_mapping where labourid in (select id from labour where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM routingtemplate_machine_mapping where machineid in (select id from machine where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM workordercomponentdetail where workorder in (select id from workorder where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM creditterm where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM taxtermsmapping where tax in (select id from tax where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM locationlevelmapping where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM showcustomcolumninreport where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM globalheaderfooter where companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM gldescriptionconfig where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM stockcustomdata where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicedoccompmaptemporary where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM accounitng_period_lock_info where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM resourcecost where labour in (select id from labour where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM labourcustomdata where labourId in (select id from labour where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM labourworkcentremapping where labour in (select id from labour where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM labour_skill_mapping where labour in (select id from labour where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM labour where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM machine_asset_mapping where machine in (select id from machine where company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM machine where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM mrpcontract where company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM workcenter where company = '" + companyid + "'");

    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_criteria_detail WHERE inspection_detail IN(SELECT id FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "'))))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_criteria_detail WHERE inspection_detail IN(SELECT id FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "'))))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_criteria_detail WHERE inspection_detail IN(SELECT id FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_sa_detail_approval WHERE sa_approval IN(SELECT id FROM in_sa_approval WHERE stock_adjustment IN (SELECT id FROM in_stockadjustment WHERE company = '" + companyid + "'))))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_sa_detail_approval WHERE sa_approval IN(SELECT id FROM in_sa_approval WHERE stock_adjustment IN (SELECT id FROM in_stockadjustment WHERE company = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stocktransfer_detail_approval WHERE stocktransfer_approval IN(SELECT id FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sa_detail_approval WHERE sa_approval IN(SELECT id FROM in_sa_approval WHERE stock_adjustment IN (SELECT id FROM in_stockadjustment WHERE company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stocktransfer_approval WHERE stocktransferid IN (SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sa_approval WHERE stock_adjustment IN (SELECT id FROM in_stockadjustment WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_criteria_detail WHERE inspection_detail IN(SELECT id FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_consignmentdetails WHERE consignment IN (SELECT id FROM in_consignment WHERE company = '" + companyid + "')))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_detail WHERE id IN(SELECT inspection_detail FROM in_consignmentdetails WHERE consignment IN (SELECT id FROM in_consignment WHERE company = '" + companyid + "'))");
    queryList.add("SELECT *,COUNT(*) as count FROM in_consignmentdetails WHERE consignment IN (SELECT id FROM in_consignment WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_consignment WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockbooking_detail WHERE stockbooking IN (SELECT id FROM in_stockbooking WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockbooking WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_documents WHERE id IN (SELECT documentid FROM in_documentcompmap WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_documentcompmap WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_store_executive WHERE storeid IN (SELECT id FROM in_storemaster WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_store_movementtype WHERE storeid IN (SELECT id FROM in_storemaster WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_temp_stockadjustmentdetail WHERE store IN (SELECT id FROM in_storemaster WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_seqnumber WHERE seqformat IN ( SELECT id FROM in_seqformat WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_store_location WHERE storeid IN (SELECT id FROM in_storemaster WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_store_user WHERE storeid IN (SELECT id FROM in_storemaster WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_attachedserial WHERE attachedbatch IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sr_attachedbatch WHERE id IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sa_attachedbatch WHERE id IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_ist_attachedbatch WHERE id IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sm_attachedbatch WHERE id IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cc_attachedbatch WHERE id IN ( SELECT id FROM in_attachedbatch WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sr_detail WHERE stockrequest IN ( SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sa_detail WHERE stockadjustment IN ( SELECT id FROM in_stockadjustment WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_ist_detail WHERE istrequest IN ( SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sm_detail WHERE stockmovement IN ( SELECT id FROM in_stockmovement WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cc_detail WHERE cyclecount IN ( SELECT id FROM in_cyclecount WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_sr_stockbuffer WHERE stockrequest IN ( SELECT id FROM in_goodsrequest WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_ist_stockbuffer WHERE istrequest IN ( SELECT id FROM in_interstoretransfer WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cccalendar_frequency WHERE cc_calendarid IN ( SELECT id FROM in_cyclecount_calendar WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cyclecount_calendar WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cyclecount WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_cyclecountdraft WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stock WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_goodsrequest WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_interstoretransfer WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockadjustment WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockadjustmentdraft WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockmovement WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_packaging WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_storemaster WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_location WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_seqformat WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inventoryconfig WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM inventory_thread_running_status  WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM notifictionrulesrecurringdetail  WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM documentrecurringmailrecord  WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_attachedbatch  WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_area  WHERE inspection_template IN ( SELECT id FROM in_inspection_template WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM in_inspection_template WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_interstoretransfer_customdata WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM in_stockadjustment_customdata WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM state WHERE id IN ( SELECT state FROM company WHERE companyid = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM tds_rate WHERE id IN ( SELECT ruleid FROM tdsdetails WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM producttermsmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptdetailtermsmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM receiptorderdetailtermsmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM deliveryorderdetailtermsmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM vendorquotationdetailstermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchasereturndetailtermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesreturndetailtermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesorderdetailtermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM quotationdetailtermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM invoicedetailtermsmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM purchaseorderdetailstermmap WHERE term IN ( SELECT id FROM linelevelterms WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM exciseInvoicedetails WHERE goodsreceipt IN ( SELECT id FROM goodsreceipt WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM salesinvoiceexcisedetailsmap WHERE invoice IN ( SELECT id FROM invoice WHERE company = '" + companyid + "')");
    queryList.add("SELECT *,COUNT(*) as count FROM tdsdetails WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM excisedetailstemplatemap WHERE companyid = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM linelevelterms WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM customdesigntemplate13 WHERE company = '" + companyid + "'");
    queryList.add("SELECT *,COUNT(*) as count FROM reportschema");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultchequelayout");
    queryList.add("SELECT *,COUNT(*) as count FROM defaultterms");
    queryList.add("SELECT *,COUNT(*) as count FROM company where companyid = '" + companyid + "'");

    try {
        Iterator QueryListItr = queryList.iterator();
        /*
         Xls file code
         */
        String excelFileName = dbName + "_" + subdomain + "_dataCount.xls";//name of excel file

        String sheetName = "Sheet1";//name of sheet

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName);
        int rowCount = 0;
        /**
         * xls file Declaration Code End
         */
        //out.println("Table Row Count for company: " + subdomain);
        //out.println("<ol>");
        Map<Integer, String> NoTableExists = new HashMap<Integer, String>();
        int noTableCount = 0;
        while (QueryListItr.hasNext()) {
            String Query = QueryListItr.next().toString();
            pstmt = conn.prepareStatement(Query);
            try {
                resultset = pstmt.executeQuery();
                int cnt = 0;
                String tableName = "";
                ResultSetMetaData resMetaData = resultset.getMetaData();
                tableName = resMetaData.getTableName(1);
                if (resultset.next()) {
                    cnt = resultset.getInt("count");
                }
                HSSFRow row = sheet.createRow(rowCount++);
                HSSFCell tableNameCell = row.createCell(0);
                tableNameCell.setCellValue(tableName);
                HSSFCell tableRowCountCell = row.createCell(1);
                tableRowCountCell.setCellValue(cnt);

                //out.println("<li>" + tableName + " : " + cnt + "</li><br>");
                resultset.close();
                pstmt.close();
            } catch (Exception e) {
                NoTableExists.put(noTableCount++, e.getMessage());
            }
        }
        /**
         * Add Exception details if tables not present in current DB
         */
        HSSFRow Emptyrow1 = sheet.createRow(rowCount++);
        HSSFRow Emptyrow2 = sheet.createRow(rowCount++);
        HSSFCell tableNotExistsCell = Emptyrow2.createCell(0);
        tableNotExistsCell.setCellValue("Tables Not Exists In DB: " + dbName);
        Iterator it = NoTableExists.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            HSSFRow NoTablerow = sheet.createRow(rowCount++);
            HSSFCell NoTableData = NoTablerow.createCell(0);
            NoTableData.setCellValue(pair.getValue().toString());
        }
        /**
         * ***************************************************
         */
        response.setHeader("Content-Disposition", "attachment;filename=" + excelFileName);
        response.setContentType("application/vnd.ms-excel");
        wb.write(response.getOutputStream());

    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        conn.close();
    }
%>