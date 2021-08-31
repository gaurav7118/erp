<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
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
                    out.println("</br></br><b>Updated company Name: " + companyname + "</b><br><b> Subdomain: " + compSubDomain + "</b>");
                    Set<String> dnSet = new HashSet();
                    Set<String> cnSet = new HashSet();
                    //Debit Note
                    query = "select dndetails.id,if(debitnote.isopeningbalencedn='1',debitnote.creationdate,dnje.entrydate) as dndate,if(goodsreceipt.isopeningbalenceinvoice='1',goodsreceipt.creationdate,grje.entrydate) grdate, dndetails.linkedgainlossje, dndetails.grlinkdate as presentLinkingDate, debitnote.dnnumber , goodsreceipt.grnumber "
                            + " from dndetails "
                            + " inner join goodsreceipt on goodsreceipt.id=dndetails.goodsreceipt "
                            + " left join journalentry as grje on grje.id=goodsreceipt.journalentry "
                            + " inner join debitnote on debitnote.id=dndetails.debitNote "
                            + " left join journalentry as dnje on dnje.id=debitnote.journalentry "
                            + " where dndetails.company='" + companyid + "'";
                    

                    PreparedStatement dnstmt = conn.prepareStatement(query);
                    ResultSet dnrs = dnstmt.executeQuery();
                    while (dnrs.next()) {
                        String dnNumber = dnrs.getString("dnnumber");
                        String grnumber = dnrs.getString("grnumber");
                        long foexjedate=0;
                        long dndate = dnrs.getDate("dndate").getTime();
                        long grdate = dnrs.getDate("grdate").getTime();
                        long presentLinkingDate = dnrs.getLong("presentLinkingDate");
                        String gainLossJEID = (String) dnrs.getString("linkedgainlossje");
                        String dnDetailID = (String) dnrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {//if gainlossjeid present means at the time of linking payment and invoice forex came into the picture.
                            // in this case updating linking date with this je entry date 
                            query = "select journalentry.entrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt1 = conn.prepareStatement(query);
                            ResultSet result1 = stmt1.executeQuery();
                            while (result1.next()) {
                                foexjedate = result1.getDate("entrydate").getTime();
                                break;
                            }
                        } 
//                        long Linkdate=Math.max(Math.max(dndate, grdate), foexjedate);
                        long Linkdate=Math.max(dndate, grdate);
                        if(presentLinkingDate < Linkdate){
                            query = "update dndetails set grlinkdate=" + Linkdate + " where id='" + dnDetailID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate = FROM_UNIXTIME(" + Linkdate + "/1000,'%Y-%m-%d') where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                            }
                            dnSet.add(dnNumber+", Linked with PI "+grnumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(Linkdate));
                        }
                            
                    }
                    
                    //Credit Note
                    query = "select cndetails.id,if(creditnote.isopeningbalencecn='1',creditnote.creationdate,cnje.entrydate) as cndate, if(invoice.isopeningbalenceinvoice='1',invoice.creationdate,invje.entrydate) invdate, cndetails.linkedgainlossje, cndetails.invoicelinkdate as presentLinkingDate, creditnote.cnnumber, invoice.invoicenumber  "
                            + " from cndetails "
                            + " inner join invoice on invoice.id=cndetails.invoice "
                            + " left join journalentry as invje on invje.id=invoice.journalentry "
                            + " inner join creditnote on creditnote.id=cndetails.creditNote "
                            + " left join journalentry as cnje on cnje.id=creditnote.journalentry "
                            + " where cndetails.company='" + companyid + "'";
                    
                    PreparedStatement cnstmt = conn.prepareStatement(query);
                    ResultSet cnrs = cnstmt.executeQuery();
                    while (cnrs.next()) {
                        String cnNumber = cnrs.getString("cnnumber");
                        String invoicenumber = cnrs.getString("invoicenumber");
                        long foexjedate=0;
                        long cndate = cnrs.getDate("cndate").getTime();
                        long invdate = cnrs.getDate("invdate").getTime();
                        long presentLinkingDate = cnrs.getLong("presentLinkingDate");
                        String gainLossJEID = (String) cnrs.getString("linkedgainlossje");
                        String cnDetailID = (String) cnrs.getString("id");
                        if (!StringUtil.isNullOrEmpty(gainLossJEID)) {//if gainlossjeid present means at the time of linking payment and invoice forex came into the picture.
                            // in this case updating linking date with this je entry date 
                            query = "select journalentry.entrydate from journalentry where id='" + gainLossJEID + "'";
                            PreparedStatement stmt1 = conn.prepareStatement(query);
                            ResultSet result1 = stmt1.executeQuery();
                            while (result1.next()) {
                                foexjedate = result1.getDate("entrydate").getTime();
                                break;
                            }
                        } 
//                        long Linkdate=Math.max(Math.max(cndate, invdate), foexjedate);
                        long Linkdate=Math.max(cndate, invdate);
                        if(presentLinkingDate < Linkdate){
                            query = "update cndetails set invoicelinkdate=" + Linkdate + " where id='" + cnDetailID + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(query);
                            updatestmt.execute();
                            if (!StringUtil.isNullOrEmpty(gainLossJEID)) {
                                query = "update journalentry set entrydate =" + new Date(Linkdate) + " where id='" + gainLossJEID + "'";
                                updatestmt = conn.prepareStatement(query);
                                updatestmt.execute();
                            }
                            cnSet.add(cnNumber);
                            cnSet.add(cnNumber+", Linked with SI "+invoicenumber+", Old linking date : "+new Date(presentLinkingDate)+", New date set : "+new Date(Linkdate));
                        }
                    }
                    if(!dnSet.isEmpty()){
                        out.println("</br>Following Debit Notes are successfully updated.</br>");
                        Iterator dnItr = dnSet.iterator();
                        while(dnItr.hasNext()){
                            out.println(dnItr.next().toString()+"</br>");
                        }
                    }
                    if(!cnSet.isEmpty()){
                        out.println("</br>Following Credit Notes are successfully updated.</br>");
                        Iterator cnItr = cnSet.iterator();
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
