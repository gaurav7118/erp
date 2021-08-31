<%-- 
    Document   : DatesMigrationForStockAdjustmentModule
    Created on : Sep 29, 2015 
    Author     : krawler
--%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/DateMigrationForInventoryModules.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is a mandatory field

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ="+08:00", query1 = "", applyDateQuery = "", companyid = "", newModifiedDate = "", serverTZID = "";
        String id = "", smid = "", compCreator="", ctzone="", utzone="", seqno = "", prevCreatedBy = "", createdby = "", subQ1 = "", subQ2 = "", updateDate = "", updateDate3 = "";
        String newcreationDate = "", newtransaction_date3 = null, newcreatedon = "", modifyPIDate = "", new_inv_updatedate = "", new_sm_createdon = "", newbussinessdate = "";
        String newissuedonDate = "", modifiedby = "", newcollectedonDate = "";
        Date bussinessdate = null, createdon = null, transaction_date = null, modifiedon = null, sm_createdon = null, newtransaction_date = null, inv_updatedate = null, transaction_date3 = null;
        Date collectedon = null, issuedon = null;
        int updatecount = 0, updatecount3 = 0, companycount=0;

        PreparedStatement serverpst = null, pst1 = null, pst2 = null, pst3 = null, modifypst = null, cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, rst3 = null, modifyrst = null, cprst=null;
        PreparedStatement pst, pstinner, tzpst, pstinner3;
        ResultSet rst, rstinner, tzrst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        //Get Server's timezone from which we ll convert date to Company Creator's timezone.
        TimeZone tzone = Calendar.getInstance().getTimeZone();
        //System.out.println("Server Timezone Name : " + tzone.getDisplayName() + "\n");   //O/p E.g.: Indian Standard Time
        //out.println("Server Timezone Name : " + tzone.getDisplayName() + "<br/>");

        //System.out.println("Server Timezone ID : " + serverTZID + "\n");  //O/p E.g. Asia/Calcutta
        //out.println("Server Timezone ID : " + serverTZID + "<br/>");

        //Define Timezone object
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat df3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        conn = DriverManager.getConnection(connectString, username, password);

        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            companycount++;
            companyid = rst1.getString("companyid");
            compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
            if (StringUtil.isNullOrEmpty(compCreator)) {
                    compCreator = "";
                    continue;
                }
            subdomain = rst1.getString("subdomain");
            ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
            utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;

            // To Fetch company creator's timezone.
            String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='" + utzone + "'";
            cppst = conn.prepareStatement(defaultTimezone);
            cprst = cppst.executeQuery();
            if (cprst.next()) {
                defaultTZ = !StringUtil.isNullOrEmpty(cprst.getString("difference")) ? cprst.getString("difference") : "+08:00";
            }
                
            System.out.println(companycount +" : "+subdomain);
            out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
            //######################################     Stock Adjustment Module    ######################################
            System.out.println("Stock Adjustment Module Started...\n");
            out.println("<b>Stock Adjustment Module Started...</b><br>");

            prevCreatedBy = "";
            pst2 = null;
            rst2 = null;
            applyDateQuery = "SELECT sa.id,sa.seqno,sa.modifiedby,sa.createdon,sa.modifiedon "
                    + "FROM in_stockadjustment sa where sa.company='" + companyid + "' ORDER BY sa.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            while (rst2.next()) {
                id = rst2.getString("id");
                //smid = rst2.getString("smid");
                // invid = rst2.getString("invid");
                seqno = rst2.getString("seqno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                //Convert Date from Server's timezone to Admin's timezone
                if (createdon != null) {
                    newcreationDate = df.format(createdon);
                    subQ1 = " createdon ='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (modifiedon != null) {
                    newModifiedDate = df.format(modifiedon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " modifiedon='" + newModifiedDate + "'";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_stockadjustment SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();

                } else {
                    updatecount = 0;
                }

                if (updatecount > 0) {

                    //System.out.println("\n***************************************");
                    //System.out.println("Stock Adjustment No. (" + seqno + ") \n Updated Dates: \n new creation Date - " + newcreationDate + "\n" + "new Modified Date - " + newModifiedDate + "\n");

                    //out.println("***************************************<br>");
                    //out.println("Stock Adjustment No. (" + seqno + ") <br> Updated Dates: <br> new creation Date - " + newcreationDate + "<br>" + "new Modified Date - " + newModifiedDate + "<br>");

                } else {
                    //System.out.println("Stock Adjustment Dates are not updated \n");
                    //out.println("Stock Adjustment Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //System.out.println("Stock Adjustment Module Ended...\n");
            out.println("<b>Stock Adjustment Module Ended...</b><br>");
            //System.out.println("\n#######################################\n");
            out.println("<br>#######################################<br>");


            //######################################     Stock Request Module    ######################################
            System.out.println("Stock Request Module Started...\n");
            out.println("<b>Stock Request Module Started...</b><br>");

            prevCreatedBy = "";
            pst2 = null;
            rst2 = null;
            applyDateQuery = "SELECT req.id,req.transactionno,req.requestedon,req.modifiedon,req.issuedon,req.collectedon,req.requestedby "
                    + "FROM in_goodsrequest req where req.company='" + companyid + "' ORDER BY req.requestedon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                seqno = rst2.getString("transactionno");
                createdon = rst2.getObject("requestedon") != null ? (java.util.Date) rst2.getObject("requestedon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;
                issuedon = rst2.getObject("issuedon") != null ? (java.util.Date) rst2.getObject("issuedon") : null;
                collectedon = rst2.getObject("collectedon") != null ? (java.util.Date) rst2.getObject("collectedon") : null;

                //Convert Date from Server's timezone to Admin's timezone
                if (createdon != null) {
                    newcreationDate = df.format(createdon);
                    subQ1 = " requestedon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (modifiedon != null) {
                    newModifiedDate = df.format(modifiedon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " modifiedon='" + newModifiedDate + "'";
                }
                if (issuedon != null) {
                    newissuedonDate = df.format(issuedon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " issuedon='" + newissuedonDate + "'";
                }
                if (collectedon != null) {
                    newcollectedonDate = df.format(collectedon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " collectedon='" + newcollectedonDate + "'";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_goodsrequest SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();

                } else {
                    updatecount = 0;
                }

                if (updatecount > 0) {
/*                    System.out.println("\n***************************************");
                    System.out.println("Stock Request No. (" + seqno + ") \n Updated Dates: ");
                    System.out.println("requestedon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");
                    System.out.println("issuedon - " + newissuedonDate + "\n" + "collectedon - " + newcollectedonDate + "\n");

                    out.println("***************************************<br>");
                    out.println("Stock Request No. (" + seqno + ") <br> Updated Dates: <br>");
                    out.println("requestedon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");
                    out.println("issuedon - " + newissuedonDate + "<br>" + "collectedon - " + newcollectedonDate + "<br>");     */
                } else {
                    //System.out.println("Stock Request Dates are not updated \n");
                    //out.println("Stock Request Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //System.out.println("Stock Request Module Ended...\n");
            out.println("<b>Stock Request Module Ended...</b><br>");
            //System.out.println("\n#######################################\n");
            out.println("<br>#######################################<br>");


            //######################################     Inter Store Stock Transfer Module    ######################################
            System.out.println("Inter Store Stock Transfer Module Started...\n");
            out.println("<b>Inter Store Stock Transfer Module Started...</b><br>");

            prevCreatedBy = "";
            pst2 = null;
            rst2 = null;
            applyDateQuery = "SELECT req.id,req.transactionno,req.modifiedby, req.createdon,req.modifiedon "
                    + "FROM in_interstoretransfer req where req.company='" + companyid + "' ORDER BY req.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                seqno = rst2.getString("transactionno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                if (createdon != null) {
                    newcreationDate = df.format(createdon);
                    subQ1 = " createdon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (modifiedon != null) {
                    newModifiedDate = df.format(modifiedon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " modifiedon='" + newModifiedDate + "'";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_interstoretransfer SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();

                } else {
                    updatecount = 0;
                }

                if (updatecount > 0) {
/*                    System.out.println("\n***************************************");
                    System.out.println("Inter Store Stock Transfer No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("createdon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");

                    out.println("***************************************<br>");
                    out.println("Inter Store Stock Transfer No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("createdon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");        */
                } else {
                    //System.out.println("Inter Store Stock Transfer Dates are not updated \n");
                    //out.println("Inter Store Stock Transfer Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //System.out.println("Inter Store Stock Transfer Module Ended...\n");
            out.println("<b>Inter Store Stock Transfer Module Ended...</b><br>");
            //System.out.println("\n#######################################\n");
            out.println("<br>#######################################<br>");


            //######################################     QA Approval Module    ######################################
            System.out.println("QA Approval Module Started...\n");
            out.println("<b>QA Approval Module Started...</b><br>");

            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df1.setTimeZone(TimeZone.getTimeZone("GMT"));
            prevCreatedBy = "";
            pst2 = null;
            rst2 = null;

            //@@@@@     in_consignment Table Started    @@@@@

            applyDateQuery = "SELECT req.id,reqd.id as smid,req.transactionno, req.createdon,reqd.modifiedon "
                    + "FROM in_consignment req INNER JOIN in_consignmentdetails reqd ON req.id=reqd.consignment"
                    + " where req.company='" + companyid + "' ORDER BY req.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                smid = rst2.getString("smid");
                seqno = rst2.getString("transactionno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                if (createdon != null) {
                    newcreationDate = df1.format(createdon);
                    subQ1 = " createdon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_consignment SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();
                } else {
                    updatecount = 0;
                }

                //Convert Date from Server's timezone to Admin's timezone
                if (updatecount > 0) {
                    //Updating Column modified on from Consignment Detail table also.
                    if (modifiedon != null) {
                        newModifiedDate = df1.format(modifiedon);
                        updateDate = "UPDATE in_consignmentdetails SET modifiedon ='" + newModifiedDate + "'" + " WHERE id='" + smid + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    }
/*                    System.out.println("\n***************************************");
                    System.out.println("in_consignment Transaction No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("createdon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");

                    out.println("***************************************<br>");
                    out.println("in_consignment Transaction No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("createdon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");        */
                } else {
                    //System.out.println("in_consignment Dates are not updated \n");
                    //out.println("in_consignment Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //@@@@@     in_consignment Table End    @@@@@
            //@@@@@     in_sa_approval Table Started    @@@@@

            applyDateQuery = "SELECT req.id,reqd.id as smid,sa.seqno, req.createdon,reqd.modifiedon "
                    + "FROM in_sa_approval req Inner Join in_stockadjustment sa ON req.stock_adjustment = sa.id"
                    + " INNER JOIN in_sa_detail_approval reqd ON req.id = reqd.sa_approval"
                    + " where sa.company='" + companyid + "' ORDER BY req.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                smid = rst2.getString("smid");
                seqno = rst2.getString("seqno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                if (createdon != null) {
                    newcreationDate = df1.format(createdon);
                    subQ1 = " createdon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_sa_approval SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();
                } else {
                    updatecount = 0;
                }

                //Convert Date from Server's timezone to Admin's timezone
                if (updatecount > 0) {
                    if (modifiedon != null) {
                        newModifiedDate = df1.format(modifiedon);
                        updateDate = "UPDATE in_sa_detail_approval SET modifiedon ='" + newModifiedDate + "'" + " WHERE id='" + smid + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    }
/*                    System.out.println("\n***************************************");
                    System.out.println("in_sa_approval(Stock Adjustment) Transaction No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("createdon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");

                    out.println("***************************************<br>");
                    out.println("in_sa_approval(Stock Adjustment) Transaction No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("createdon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");        */
                } else {
                    //System.out.println("in_sa_approval Dates are not updated \n");
                    //out.println("in_sa_approval Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //@@@@@     in_sa_approval Table End    @@@@@

            //@@@@@     in_stocktransfer_approval Table Started    @@@@@

            // In this table, data comes from two tables: 1.in_goodsrequest 2.in_interstoretransfer
            applyDateQuery = "SELECT req.id,reqd.id as smid,gr.transactionno, req.createdon ,reqd.modifiedon "
                    + "FROM in_stocktransfer_approval req INNER JOIN in_goodsrequest gr ON req.stocktransferid = gr.id"
                    + " INNER JOIN in_stocktransfer_detail_approval reqd ON req.id= reqd.stocktransfer_approval "
                    + "where gr.company='" + companyid + "' ORDER BY req.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                smid = rst2.getString("smid");
                seqno = rst2.getString("transactionno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                if (createdon != null) {
                    newcreationDate = df1.format(createdon);
                    subQ1 = " createdon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_stocktransfer_approval SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();
                } else {
                    updatecount = 0;
                }

                //Convert Date from Server's timezone to Admin's timezone
                if (updatecount > 0) {
                    if (modifiedon != null) {
                        newModifiedDate = df1.format(modifiedon);
                        updateDate = "UPDATE in_stocktransfer_detail_approval SET modifiedon ='" + newModifiedDate + "'" + " WHERE id='" + smid + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    }
/*                    System.out.println("\n***************************************");
                    System.out.println("in_stocktransfer_approval(Stock Request) Transaction No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("createdon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");

                    out.println("***************************************<br>");
                    out.println("in_stocktransfer_approval(Stock Request) Transaction No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("createdon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");    */
                } else {
                    //System.out.println("in_stocktransfer_approval Dates are not updated \n");
                    //out.println("in_stocktransfer_approval Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //2.in_interstoretransfer
            applyDateQuery = "SELECT req.id,reqd.id as smid,gr.transactionno, req.createdon,reqd.modifiedon "
                    + "FROM in_stocktransfer_approval req INNER JOIN in_interstoretransfer gr ON req.stocktransferid = gr.id"
                    + " INNER JOIN in_stocktransfer_detail_approval reqd ON req.id= reqd.stocktransfer_approval "
                    + "where gr.company='" + companyid + "' ORDER BY req.createdon";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getString("id");
                smid = rst2.getString("smid");
                seqno = rst2.getString("transactionno");
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;

                if (createdon != null) {
                    newcreationDate = df1.format(createdon);
                    subQ1 = " createdon='" + newcreationDate + "'";
                } else {
                    subQ1 = "";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_stocktransfer_approval SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();
                } else {
                    updatecount = 0;
                }

                //Convert Date from Server's timezone to Admin's timezone
                if (updatecount > 0) {
                    if (modifiedon != null) {
                        newModifiedDate = df1.format(modifiedon);
                        updateDate = "UPDATE in_stocktransfer_detail_approval SET modifiedon ='" + newModifiedDate + "'" + " WHERE id='" + smid + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    }
/*                    System.out.println("\n***************************************");
                    System.out.println("in_stocktransfer_approval(Stock Request) Transaction No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("createdon - " + newcreationDate + "\n" + "modifiedon - " + newModifiedDate + "\n");

                    out.println("***************************************<br>");
                    out.println("in_stocktransfer_approval(Stock Request) Transaction No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("createdon - " + newcreationDate + "<br>" + "modifiedon - " + newModifiedDate + "<br>");    */
                } else {
                    //System.out.println("in_stocktransfer_approval Dates are not updated \n");
                    //out.println("in_stocktransfer_approval Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            //@@@@@     in_stocktransfer_approval Table End    @@@@@



            //System.out.println("QA Approval Module End...\n");
            out.println("<b>QA Approval Module End...</b><br>");
            //System.out.println("\n#######################################\n");
            out.println("<br>#######################################<br>");




            //######################################     Stock Movement Module    ######################################
            System.out.println("Stock Movement Module Started...\n");
            out.println("<b>Stock Movement Module Started...</b><br>");

            prevCreatedBy = "";
            pst2 = null;
            rst2 = null;

            //To Update in_stockmovement table for those which has modulerefid has in_sa_detail ( Stock Adjustment Detail ) id.
            applyDateQuery = "select sm.id,sm.transactionno,sm.createdon,sm.transaction_date,sa.modifiedby from in_stockmovement sm "
                    + "INNER JOIN in_sa_detail sad ON sm.modulerefid = sad.id "
                    + "INNER JOIN in_stockadjustment sa ON sad.stockadjustment = sa.id where sm.company='" + companyid + "'";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            tzpst = null;
            tzrst = null;
            while (rst2.next()) {
                id = rst2.getObject("id") != null ? rst2.getObject("id").toString() : "";
                seqno = rst2.getObject("transactionno") != null ? rst2.getObject("transactionno").toString() : "";
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                transaction_date = rst2.getObject("transaction_date") != null ? (java.util.Date) rst2.getObject("transaction_date") : null;
                createdby = rst2.getObject("modifiedby") != null ? rst2.getObject("modifiedby").toString() : compCreator;

                if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                    prevCreatedBy = createdby;
                    String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                    tzpst = conn.prepareStatement(userTZQuery);
                    tzrst = tzpst.executeQuery();
                    if (tzrst.next()) {
                        //Formatter to convert server's date to UTC date.
                        df2.setTimeZone(TimeZone.getTimeZone("GMT"));
                        // Formatter to convert date to User's (Client) date.
                        userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"): defaultTZ;
                        df3.setTimeZone(TimeZone.getTimeZone(userTZDiff));
                    }
                }

                if (transaction_date != null) {
                    //First, To convert transaction_date to UTC date.
                    newtransaction_date = df2.parse(df2.format(transaction_date));
                    //Then, To convert UTC date into User's TimeZone Difference date.
                    newtransaction_date = df3.parse(df3.format(newtransaction_date));
                    subQ1 = " transaction_date ='" + transaction_date + "'";
                } else {
                    subQ1 = "";
                }
                if (createdon != null) {
                    //To convert server's new date to UTC date.
                    newcreationDate = df2.format(createdon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " createdon='" + newcreationDate + "'";
                }
                if (!StringUtil.isNullOrEmpty(subQ1)) {
                    updateDate = "UPDATE in_stockmovement SET " + subQ1 + " WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    updatecount = pstinner.executeUpdate();

                } else {
                    updatecount = 0;
                }
                if (updatecount > 0) {
/*                    System.out.println("\n***************************************");
                    System.out.println("Stock Movement No. (" + seqno + ") \n Updated Dates:");
                    System.out.println("transaction_date - " + newtransaction_date + "\n");

                    out.println("***************************************<br>");
                    out.println("Stock Movement No. (" + seqno + ") <br> Updated Dates: ");
                    out.println("transaction_date - " + newtransaction_date + "<br>");          */
                } else {
                    //System.out.println("Stock Movement Dates are not updated \n");
                    //out.println("Stock Movement Dates are not updated.<br>");
                }
                subQ1 = "";
            }

            tzpst = null;
            tzrst = null;
            //To Update in_stockmovement table for those which has modulerefid has in_interstoretransfer.id
            applyDateQuery = "select sm.id,sm.transactionno,ist.modifiedby,sm.createdon,sm.transaction_date from in_stockmovement sm INNER JOIN in_interstoretransfer ist "
                    + "ON sm.modulerefid = ist.id where sm.company ='" + companyid + "'";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                smid = rst2.getString("id");
                seqno = rst2.getString("transactionno");
                transaction_date = rst2.getObject("transaction_date") != null ? (java.util.Date) rst2.getObject("transaction_date") : null;
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                createdby = rst2.getObject("modifiedby") != null ? rst2.getObject("modifiedby").toString() : compCreator;
                if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                    prevCreatedBy = createdby;
                    String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                    tzpst = conn.prepareStatement(userTZQuery);
                    tzrst = tzpst.executeQuery();
                    if (tzrst.next()) {
                        //Formatter to convert server's date to UTC date.
                        df2.setTimeZone(TimeZone.getTimeZone("GMT"));
                        // Formatter to convert date to User's (Client) date.
                        userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                        df3.setTimeZone(TimeZone.getTimeZone(userTZDiff));
                    }
                }

                if (transaction_date != null) {
                    //First, To convert transaction_date to UTC date.
                    newtransaction_date = df2.parse(df2.format(transaction_date));
                    //Then, To convert UTC date into User's TimeZone Difference date.
                    newtransaction_date = df3.parse(df3.format(newtransaction_date));
                    subQ1 = " transaction_date ='" + transaction_date + "'";
                } else {
                    subQ1 = "";
                }
                if (createdon != null) {
                    //To convert server's new date to UTC date.
                    newcreationDate = df2.format(createdon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " createdon='" + newcreationDate + "'";
                }
                if (transaction_date3 != null) {
                    newtransaction_date3 = df.format(transaction_date3);
                    updateDate = "UPDATE in_stockmovement SET " + subQ1 + " WHERE id='" + id + "'";;
                    pstinner = conn.prepareStatement(updateDate3);
                    updatecount = pstinner.executeUpdate();
                }
                if (updatecount > 0) {
/*                    System.out.println("\n***************************************");
                    System.out.println("Stock Movement Record of SEQ NO. (" + seqno + ") \n Updated Dates:");
                    System.out.println("transaction_date - " + newtransaction_date + "\n");

                    out.println("***************************************<br>");
                    out.println("Stock Movement Record of SEQ NO. (" + seqno + ") <br> Updated Dates:");
                    out.println("transaction_date - " + newtransaction_date + "<br>");  */
                }
            }

            tzpst = null;
            tzrst = null;
            //To Update in_stockmovement table for those which has modulerefid has in_consignment.modulerefid
            applyDateQuery = "select distinct sm.id,cnd.inspector,sm.transactionno,sm.createdon,sm.transaction_date "
                    + "from in_stockmovement sm INNER JOIN in_consignment cn INNER JOIN in_consignmentdetails cnd ON cn.id = cnd.consignment"
                    + " where sm.modulerefid = cn.modulerefid and sm.company='" + companyid + "' and cnd.inspector IS NOT NULL";

            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                smid = rst2.getString("id");
                seqno = rst2.getString("transactionno");
                transaction_date = rst2.getObject("transaction_date") != null ? (java.util.Date) rst2.getObject("transaction_date") : null;
                createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                createdby = rst2.getObject("modifiedby") != null ? rst2.getObject("modifiedby").toString() : compCreator;
                if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                    prevCreatedBy = createdby;
                    String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                    tzpst = conn.prepareStatement(userTZQuery);
                    tzrst = tzpst.executeQuery();
                    if (tzrst.next()) {
                        //Formatter to convert server's date to UTC date.
                        df2.setTimeZone(TimeZone.getTimeZone("GMT"));
                        // Formatter to convert date to User's (Client) date.
                        userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ?tzrst.getString("difference"):defaultTZ;
                        df3.setTimeZone(TimeZone.getTimeZone(userTZDiff));
                    }
                }

                if (transaction_date != null) {
                    //First, To convert transaction_date to UTC date.
                    newtransaction_date = df2.parse(df2.format(transaction_date));
                    //Then, To convert UTC date into User's TimeZone Difference date.
                    newtransaction_date = df3.parse(df3.format(newtransaction_date));
                    subQ1 = " transaction_date ='" + transaction_date + "'";
                } else {
                    subQ1 = "";
                }
                if (createdon != null) {
                    //To convert server's new date to UTC date.
                    newcreationDate = df2.format(createdon);
                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        subQ1 += ", ";
                    }
                    subQ1 += " createdon='" + newcreationDate + "'";
                }
                if (transaction_date3 != null) {
                    newtransaction_date3 = df.format(transaction_date3);
                    updateDate = "UPDATE in_stockmovement SET " + subQ1 + " WHERE id='" + id + "'";;
                    pstinner = conn.prepareStatement(updateDate3);
                    updatecount = pstinner.executeUpdate();
                }
                if (updatecount > 0) {
/*                    System.out.println("\n***************************************");
                    System.out.println("Stock Movement Record of SEQ NO. (" + seqno + ") \n Updated Dates:");
                    System.out.println("transaction_date - " + newtransaction_date + "\n");

                    out.println("***************************************<br>");
                    out.println("Stock Movement Record of SEQ NO. (" + seqno + ") <br> Updated Dates:");
                    out.println("transaction_date - " + newtransaction_date + "<br>");      */
                }
            }




            System.out.println("Stock Movement Module Ended...\n");
            out.println("<b>Stock Movement Module Ended...</b><br>");
            System.out.println("\n#######################################\n");
            out.println("<br>#######################################<br>");
            out.println("<center><b>=================================================================================</b></center><br><br>");
        }
        //}//Server query while
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
        out.print("Dates are not updated.");
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>