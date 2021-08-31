<%-- 
    Document   : UpdateIncoiceAmountDueDate
    Created on : Mar 1, 2017, 3:33:24 PM
    Author     : krawler
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.HashSet"%>
<%@page import="com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO"%>
<%@page import="com.krawler.spring.accounting.invoice.accInvoiceDAO"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%
    Connection conn = null;
    try {
        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";

        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);


            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
            accInvoiceDAO accInvoiceDAOObj = (accInvoiceDAO) context.getBean("accInvoiceDao");
            accGoodsReceiptDAO accGoodsReceiptDAOObj = (accGoodsReceiptDAO) context.getBean("accGoodsReceiptDao");


            String query = "";
            PreparedStatement compstmt = null;
            if (StringUtil.isNullOrEmpty(subdomain)) {
                query = "select companyname,subdomain,companyid from company";
                compstmt = conn.prepareStatement(query);
            } else {
                query = "select companyname,subdomain,companyid from company where subdomain=?";
                compstmt = conn.prepareStatement(query);
                compstmt.setString(1, subdomain);
            }
            ResultSet comprs = compstmt.executeQuery();
            DateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);
            while (comprs.next()) {
                String companyid = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubDomain = comprs.getString("subdomain");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    //Sales Invoice 
                    Set<String> updateInvoiceSet = new HashSet();
                    String salesInvQuery = "select id,invoicenumber,amountduedate from invoice where (invoiceamountdue=0 || (openingbalancebaseamountdue=0 and isopeningbalenceinvoice='1')) and company=? and deleteflag='F' order by invoicenumber";
                    PreparedStatement saleInvstmt = null;
                    saleInvstmt = conn.prepareStatement(salesInvQuery);
                    saleInvstmt.setString(1, companyid);
                    ResultSet invoicers = saleInvstmt.executeQuery();
                    while (invoicers.next()) {
                        String id = invoicers.getString("id");
                        String invoiceNumber = invoicers.getString("invoicenumber");
                        Date invoiceDueDate = invoicers.getDate("amountduedate");
                        if(invoiceDueDate!=null){
                            invoiceDueDate = sdf.parse(sdf.format(invoiceDueDate));//Removing time part
                        }
                        
                        String maxdateSelectQuery = "select max(resulttable.linkeddate)  as maxdate "
                                + "from "
                                + "( "
                                + "select invoicelinkdate as linkeddate from cndetails  "
                                + "where invoice=? and invoicelinkdate!=0 "
                                + "union "
                                + "select receiptlinkdate as linkeddate from linkdetailreceipt  "
                                + "where invoice=? and receiptlinkdate!=0 "
                                + "union "
                                + "select je.entrydate as linkeddate from receiptdetails  inner join receipt rp on rp.id=receiptdetails.receipt inner join journalentry as je on je.id=rp.journalentry "
                                + "where  receiptdetails.invoice=? "
                                + ") as resulttable ";
                        PreparedStatement maxdateSelectQueryStmt = null;
                        maxdateSelectQueryStmt = conn.prepareStatement(maxdateSelectQuery);
                        maxdateSelectQueryStmt.setString(1, id);
                        maxdateSelectQueryStmt.setString(2, id);
                        maxdateSelectQueryStmt.setString(3, id);
                        ResultSet maxdateResultSet = maxdateSelectQueryStmt.executeQuery();
                        Date maxDate = null;
                        if (maxdateResultSet.next()) {
                            maxDate = maxdateResultSet.getDate("maxdate");
                            if(maxDate!=null){
                               maxDate = sdf.parse(sdf.format(maxDate));//Removing time part 
                            }
                        } 
                        boolean needToUpdate =false;
                        if (invoiceDueDate != null && maxDate != null) {
                            if (maxDate.after(invoiceDueDate)) {
                                invoiceDueDate = maxDate;
                                updateInvoiceSet.add(invoiceNumber);
                                needToUpdate = true;
                            }
                        } else if (invoiceDueDate == null && maxDate != null) {
                            invoiceDueDate = maxDate;
                            updateInvoiceSet.add(invoiceNumber);
                            needToUpdate = true;
                        }
                        if (invoiceDueDate != null && needToUpdate) {
                            String updateInvoiceQuery = "update invoice set amountduedate = '" + sdf.format(invoiceDueDate) + "' where id = '" + id + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(updateInvoiceQuery);
                            updatestmt.execute();
                        }
                    }

                    //Purchase Invoice
                    Set<String> updatePISet = new HashSet();
                    String PISelectQuery = "select id,grnumber,amountduedate from goodsreceipt where (invoiceamountdue=0 || (openingbalanceamountdue=0 and isopeningbalenceinvoice='1')) and company=? and deleteflag='F' order by grnumber";
                    PreparedStatement pistmt = null;
                    pistmt = conn.prepareStatement(PISelectQuery);
                    pistmt.setString(1, companyid);
                    ResultSet pirs = pistmt.executeQuery();
                    while (pirs.next()) {
                        String id = pirs.getString("id");
                        String grnumber = pirs.getString("grnumber");
                        Date invoiceDueDate = pirs.getDate("amountduedate");
                        if(invoiceDueDate!=null){
                            invoiceDueDate = sdf.parse(sdf.format(invoiceDueDate));//Removing time part
                        }
                        
                        String maxdateSelectQuery = "select max(resulttable.linkeddate) as maxdate "
                                + "from "
                                + "( "
                                + "select grlinkdate as linkeddate from dndetails  "
                                + "where goodsreceipt=? and grlinkdate!=0 "
                                + "union "
                                + "select paymentlinkdate as linkeddate from linkdetailpayment  "
                                + "where goodsReceipt=? and paymentlinkdate!=0 "
                                + "union "
                                + "select je.entrydate as linkeddate from paymentdetail  "
                                + "inner join payment mp on mp.id=paymentdetail.payment inner join journalentry as je on je.id=mp.journalentry "
                                + "where  paymentdetail.goodsReceipt=? "
                                + ") as resulttable ";
                        PreparedStatement maxdateSelectQueryStmt = null;
                        maxdateSelectQueryStmt = conn.prepareStatement(maxdateSelectQuery);
                        maxdateSelectQueryStmt.setString(1, id);
                        maxdateSelectQueryStmt.setString(2, id);
                        maxdateSelectQueryStmt.setString(3, id);
                        ResultSet maxdateResultSet = maxdateSelectQueryStmt.executeQuery();
                        Date maxDate = null;
                        if (maxdateResultSet.next()) {
                            maxDate = maxdateResultSet.getDate("maxdate");
                            if(maxDate!=null){
                               maxDate = sdf.parse(sdf.format(maxDate));//Removing time part 
                            }
                        }
                        boolean needToUpdate =false;
                        if (invoiceDueDate != null && maxDate != null) {
                            if (maxDate.after(invoiceDueDate)) {
                                invoiceDueDate = maxDate;
                                updatePISet.add(grnumber);
                                needToUpdate = true;
                            }
                        } else if (invoiceDueDate == null && maxDate != null) {
                            invoiceDueDate = maxDate;
                            updatePISet.add(grnumber);
                            needToUpdate = true;
                        }
                        if (invoiceDueDate != null && needToUpdate) {
                            String updateInvoiceQuery = "update goodsreceipt set amountduedate = '" + sdf.format(invoiceDueDate) + "' where id = '" + id + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(updateInvoiceQuery);
                            updatestmt.execute();
                        }
                    }
                    if (updateInvoiceSet.size() > 0 || updatePISet.size() > 0) {
                        out.println("</br>===============Company:<b>" + companyname + "</b>, Subdomain=<b>" + compSubDomain + "</b>==============</br>");
                        if (updateInvoiceSet.size() > 0) {
                            out.println("Sales Invoice Updated:" + updateInvoiceSet.size() + "</br>");
                        }
                        if (updatePISet.size() > 0) {
                            out.println("Purchase Invoice Updated:" + updatePISet.size() + "</br>");
                        }
                    }
                }
            }
            out.println("</br></br>Script executed successfully.Thanks!!! ");
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
