<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

<%@page import="java.util.Collections"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


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
            while (comprs.next()) {
                String companyid = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubDomain = comprs.getString("subdomain");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    Set<String> paymentSet = new HashSet();
                    Set<String> receiptSet = new HashSet();
                    out.println("</br></br><b>Updated company Name: " + companyname + "</b><br><b> Subdomain: " + compSubDomain + "</b>");

                    //For Payment Linked To Purchase Invoice
                    query = "select if(pmt.isopeningbalencepayment='1',pmt.creationdate,je.entrydate) as paymentDate, linkdetailpayment.linkedgainlossje as gainlossjeid,linkdetailpayment.id as id, "
                            + " paymentlinkdate as presentLinkDate, pmt.paymentnumber,gainLossJERecord.entryno as gainLossJENumber,gainLossJERecord.entrydate as gainLossJEDate from linkdetailpayment "
                            + " inner join payment as pmt on pmt.id=linkdetailpayment.payment "
                            + " left join journalentry as je on je.id=pmt.journalentry "
                            + " inner join journalentry gainLossJERecord on linkdetailpayment.linkedgainlossje = gainLossJERecord.id "
                            + " where linkdetailpayment.company='" + companyid + "'";
                    PreparedStatement paymentstmt = conn.prepareStatement(query);
                    ResultSet paymentrs = paymentstmt.executeQuery();
                    while (paymentrs.next()) {
                        long gainlossJEdate = 0;//Foreign gain loss JE 
                        long presentLinkingDate = paymentrs.getLong("presentLinkDate"); //Current linking date
                        long paymentDate = paymentrs.getDate("paymentDate").getTime(); //Payment creation date
                        Date gainLossJEDate = paymentrs.getDate("gainLossJEDate");
                        String gainLossJENumber = paymentrs.getString("gainLossJENumber");
                        String paymentnumber = paymentrs.getString("paymentnumber");
                        String gainLossJEID = (String) paymentrs.getString("gainlossjeid");
                        String paymentLinkDetaillID = (String) paymentrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                            query = "select journalentry.entrydate as jeentrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt1 = conn.prepareStatement(query);
                            ResultSet result1 = stmt1.executeQuery();
                            while (result1.next()) {
                                gainlossJEdate = result1.getDate("jeentrydate").getTime();
                                break;
                            }
                        }

                        long invoiceDate = 0;
                        String InvoiceNumber = "";
                        query = "select if(goodsreceipt.isopeningbalenceinvoice='1',goodsreceipt.creationdate,grje.entrydate) as grdate, grnumber from linkdetailpayment "
                                + "inner join goodsreceipt on goodsreceipt.id=linkdetailpayment.goodsreceipt "
                                + "left join journalentry grje on grje.id=goodsreceipt.journalentry "
                                + "where linkdetailpayment.id='" + paymentLinkDetaillID + "'";
                        PreparedStatement stmt1 = conn.prepareStatement(query);
                        ResultSet result1 = stmt1.executeQuery();
                        while (result1.next()) {
                            invoiceDate = result1.getDate("grdate").getTime();
                            InvoiceNumber = result1.getString("grnumber");
                            break;
                        }
                        long NewLinkingDateToSet = Math.max(paymentDate, invoiceDate);
                        if (presentLinkingDate < NewLinkingDateToSet) {
                            query = "update linkdetailpayment set paymentlinkdate = " + NewLinkingDateToSet + " where id = '" + paymentLinkDetaillID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            paymentSet.add(paymentnumber+", Linked with PI "+InvoiceNumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(NewLinkingDateToSet));
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate = FROM_UNIXTIME(" + NewLinkingDateToSet + "/1000,'%Y-%m-%d') where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                                paymentSet.add(paymentnumber+", Linked with PI "+InvoiceNumber+", Forex Gain/Loss JE : "+gainLossJENumber+", Old Date : "+gainLossJEDate+", New date set : "+new Date(NewLinkingDateToSet));
                            }
                        }
                    }







                    //For Payment Linked To Credit Note
                    query = "select if(pmt.isopeningbalencepayment='1',pmt.creationdate,je.entrydate) as paymentDate, linkdetailpaymenttocreditnote.linkedgainlossje as gainlossjeid, "
                            + " linkdetailpaymenttocreditnote.id as id, paymentlinkdate as presentLinkDate, pmt.paymentnumber, gainLossJERecord.entryno as gainLossJENumber,gainLossJERecord.entrydate as gainLossJEDate from linkdetailpaymenttocreditnote "
                            + " inner join payment as pmt on pmt.id=linkdetailpaymenttocreditnote.payment "
                            + " left join journalentry as je on je.id=pmt.journalentry "
                            + " inner join journalentry gainLossJERecord on linkdetailpaymenttocreditnote.linkedgainlossje = gainLossJERecord.id "
                            + " where linkdetailpaymenttocreditnote.company='" + companyid + "'";
                    paymentstmt = conn.prepareStatement(query);
                    paymentrs = paymentstmt.executeQuery();
                    while (paymentrs.next()) {
                        long gainlossJEdate = 0;//Foreign gain loss JE 
                        long presentLinkingDate = paymentrs.getLong("presentLinkDate"); //Current linking date
                        long paymentDate = paymentrs.getDate("paymentDate").getTime(); //Payment creation date
                        Date gainLossJEDate = paymentrs.getDate("gainLossJEDate");
                        String gainLossJENumber = paymentrs.getString("gainLossJENumber");
                        String paymentnumber = paymentrs.getString("paymentnumber");
                        String gainLossJEID = (String) paymentrs.getString("gainlossjeid");
                        String paymentLinkDetaillID = (String) paymentrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                            query = "select journalentry.entrydate as jeentrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt1 = conn.prepareStatement(query);
                            ResultSet result1 = stmt1.executeQuery();
                            while (result1.next()) {
                                gainlossJEdate = result1.getDate("jeentrydate").getTime();                                
                                break;
                            }
                        }

                        long cnDate = 0;
                        String CNNumber="";
                        query = "select if(creditnote.isopeningbalencecn='1',creditnote.creationdate,cnje.entrydate) as cndate, cnnumber from linkdetailpaymenttocreditnote "
                                + "inner join creditnote on creditnote.id=linkdetailpaymenttocreditnote.creditnote "
                                + "left join journalentry cnje on cnje.id=creditnote.journalentry "
                                + "where linkdetailpaymenttocreditnote.id='" + paymentLinkDetaillID + "'";
                        PreparedStatement stmt1 = conn.prepareStatement(query);
                        ResultSet result1 = stmt1.executeQuery();
                        while (result1.next()) {
                            cnDate = result1.getDate("cndate").getTime();
                            CNNumber = result1.getString("cnnumber");
                            break;
                        }
                        long NewLinkingDateToSet = Math.max(paymentDate, cnDate);
                        if (presentLinkingDate < NewLinkingDateToSet) {
                            query = "update linkdetailpaymenttocreditnote set paymentlinkdate = " + NewLinkingDateToSet + " where id = '" + paymentLinkDetaillID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            paymentSet.add(paymentnumber+", Linked with CN "+CNNumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(NewLinkingDateToSet));
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate = FROM_UNIXTIME(" + NewLinkingDateToSet + "/1000,'%Y-%m-%d') where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                                paymentSet.add(paymentnumber+", Linked with CN "+CNNumber+", Forex Gain/Loss JE: "+gainLossJENumber+", Old Date : "+gainLossJEDate+", New date set : "+new Date(NewLinkingDateToSet));
                            }
                        }
                    }









                    //For Receipt Linked To Sales Invoice
                    query = "select if(rcp.isopeningbalencereceipt='1',rcp.creationdate,je.entrydate) as receiptDate, rcp.receiptnumber, linkdetailreceipt.linkedgainlossje as gainlossjeid, "
                            + " linkdetailreceipt.id as id, receiptlinkdate as presentLinkDate, gainLossJERecord.entryno as gainLossJENumber,gainLossJERecord.entrydate as gainLossJEDate from linkdetailreceipt "
                            + " inner join receipt as rcp on rcp.id=linkdetailreceipt.receipt "
                            + " left join journalentry as je on je.id=rcp.journalentry "
                            + " inner join journalentry gainLossJERecord on linkdetailreceipt.linkedgainlossje = gainLossJERecord.id "
                            + " where linkdetailreceipt.company='" + companyid + "'";
                    PreparedStatement receiptstmt = conn.prepareStatement(query);
                    ResultSet receiptrs = receiptstmt.executeQuery();
                    while (receiptrs.next()) {
                        long gainlossJEdate = 0;
                        long presentLinkingDate = paymentrs.getLong("presentLinkDate"); //Current linking date
                        long receiptDate = receiptrs.getDate("receiptDate").getTime(); // receipt creation Date
                        String receiptNumber = receiptrs.getString("receiptnumber");
                        Date gainLossJEDate = paymentrs.getDate("gainLossJEDate");
                        String gainLossJENumber = paymentrs.getString("gainLossJENumber");
                        String gainLossJEID = (String) receiptrs.getString("gainlossjeid");
                        String linkReceiptDetailID = (String) receiptrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                            query = "select journalentry.entrydate as jeentrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt2 = conn.prepareStatement(query);
                            ResultSet result2 = stmt2.executeQuery();
                            while (result2.next()) {
                                gainlossJEdate = result2.getDate("jeentrydate").getTime();
                                break;
                            }
                        }

                        long invoiceDate = 0;
                        String InvoiceNumber="";
                        query = "select if(invoice.isopeningbalenceinvoice='1',invoice.creationdate,invje.entrydate) invoicedate, invoicenumber from linkdetailreceipt "
                                + " inner join invoice on invoice.id=linkdetailreceipt.invoice "
                                + " inner join journalentry invje on invje.id=invoice.journalentry "
                                + " where linkdetailreceipt.id='" + linkReceiptDetailID + "'";
                        PreparedStatement stmt1 = conn.prepareStatement(query);
                        ResultSet result1 = stmt1.executeQuery();
                        while (result1.next()) {
                            invoiceDate = result1.getDate("invoicedate").getTime();
                            InvoiceNumber = result1.getString("invoicenumber");
                            break;
                        }
                        long NewLinkingDateToSet = Math.max(receiptDate, invoiceDate);
                        if (presentLinkingDate < NewLinkingDateToSet) {
                            query = "update linkdetailreceipt set receiptlinkdate = " + NewLinkingDateToSet + " where id = '" + linkReceiptDetailID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            receiptSet.add(receiptNumber+", Linked with SI "+InvoiceNumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(NewLinkingDateToSet));
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate = FROM_UNIXTIME(" + NewLinkingDateToSet + "/1000,'%Y-%m-%d') where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                                receiptSet.add(receiptNumber+", Linked with SI "+InvoiceNumber+", Forex Gain/Loss JE : "+gainLossJENumber+", Old Date : "+gainLossJEDate+", New date set : "+new Date(NewLinkingDateToSet));
                            }
                        }
                    }





                    //For Receipts Linked To Debit Note
                    query = "select if(receipt.isopeningbalencereceipt='1',receipt.creationdate,je.entrydate) as paymentDate, receipt.receiptnumber, linkdetailreceipttodebitnote.linkedgainlossje as gainlossjeid, "
                            + " linkdetailreceipttodebitnote.id as id, receiptlinkdate as presentLinkDate, gainLossJERecord.entryno as gainLossJENumber,gainLossJERecord.entrydate as gainLossJEDate from linkdetailreceipttodebitnote "
                            + " inner join receipt as receipt on receipt.id=linkdetailreceipttodebitnote.receipt "
                            + " left join journalentry as je on je.id=receipt.journalentry "
                            + " inner join journalentry gainLossJERecord on linkdetailreceipttodebitnote.linkedgainlossje = gainLossJERecord.id "
                            + " where linkdetailreceipttodebitnote.company='" + companyid + "'";
                    paymentstmt = conn.prepareStatement(query);
                    paymentrs = paymentstmt.executeQuery();
                    while (paymentrs.next()) {
                        long gainlossJEdate = 0;//Foreign gain loss JE 
                        long presentLinkingDate = paymentrs.getLong("presentLinkDate"); //Current linking date
                        long paymentDate = paymentrs.getDate("paymentDate").getTime(); //Payment creation date
                        String receiptNumber = paymentrs.getString("receiptNumber");
                        Date gainLossJEDate = paymentrs.getDate("gainLossJEDate");
                        String gainLossJENumber = paymentrs.getString("gainLossJENumber");
                        String gainLossJEID = (String) paymentrs.getString("gainlossjeid");
                        String paymentLinkDetaillID = (String) paymentrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                            query = "select journalentry.entrydate as jeentrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt1 = conn.prepareStatement(query);
                            ResultSet result1 = stmt1.executeQuery();
                            while (result1.next()) {
                                gainlossJEdate = result1.getDate("jeentrydate").getTime();
                                break;
                            }
                        }

                        long cnDate = 0;
                        String DNNumber= "";
                        query = "select if(debitnote.isopeningbalencedn='1',debitnote.creationdate,dnje.entrydate) as dndate, dnnumber from linkdetailreceipttodebitnote "
                                + "inner join debitnote on debitnote.id=linkdetailreceipttodebitnote.debitnote "
                                + "left join journalentry dnje on dnje.id=debitnote.journalentry "
                                + "where linkdetailreceipttodebitnote.id='" + paymentLinkDetaillID + "'";
                        PreparedStatement stmt1 = conn.prepareStatement(query);
                        ResultSet result1 = stmt1.executeQuery();
                        while (result1.next()) {
                            cnDate = result1.getDate("dndate").getTime();
                            DNNumber = result1.getString("dnnumber");
                            break;
                        }
                        long NewLinkingDateToSet = Math.max(paymentDate, cnDate);
                        if (presentLinkingDate < NewLinkingDateToSet) {
                            query = "update linkdetailreceipttodebitnote set paymentlinkdate = " + NewLinkingDateToSet + " where id = '" + paymentLinkDetaillID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            paymentSet.add(receiptNumber+", Linked with DN "+DNNumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(NewLinkingDateToSet));
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate = FROM_UNIXTIME(" + NewLinkingDateToSet + "/1000,'%Y-%m-%d') where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                                receiptSet.add(receiptNumber+", Linked with DN "+DNNumber+", Forex Gain/Loss JE : "+gainLossJENumber+", Old Date : "+gainLossJEDate+", New date set : "+new Date(NewLinkingDateToSet));
                            }
                        }
                    }
                    if(!paymentSet.isEmpty()){
                        out.println("</br></br><b>Following Payments are successfully updated.</b></br>");
                        Iterator dnItr = paymentSet.iterator();
                        while(dnItr.hasNext()){
                            out.println(dnItr.next().toString()+"</br>");
                        }
                    }
                    if(!receiptSet.isEmpty()){
                        out.println("</br></br><b>Following Receipts are successfully updated.</b></br>");
                        Iterator cnItr = receiptSet.iterator();
                        while(cnItr.hasNext()){
                            out.println(cnItr.next().toString()+"</br>");
                        }
                    }

                }
            }
        }
        if (conn != null) {
            conn.close();//finally release connection   
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
