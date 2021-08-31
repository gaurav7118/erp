<%-- 
    Document   : DatesMigrationForVendorAndCustomerModule
    Created on : Sep 23, 2015, 10:25:32 AM
    Author     : krawler
--%>

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
        //SCRIPT URL : http://<app-url>/DatesMigrationForVendor_Customer_AccountModule.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String newcreationDate = "", newModifiedDate = "",serverTZID="", newMailonDate = "", newSelfbilledfromdate = "", newSelfbilledtodate = "";
        String subQ1 = "", subQ2 = "", defaultTZ="+08:00" , subQ3 = "", subQ4 = "", subQ5 = "", updateDate = "", id = "", name = "", subdomainQuery = "";
        String serverTZQuery = "", compCreator="", ctzone="", utzone="", serverTZDiff = "", userTZDiff = "", query1 = "", applyDateQuery = "", companyid = "", modifyPIDate = "";

        Date createdon = null, modifiedon = null, mailon = null, selfbilledfromdate = null, selfbilledtodate = null;

        PreparedStatement serverpst = null, pst1 = null, pst2 = null, modifypst = null, cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, cprst=null;
        PreparedStatement pstinner, tzpst;
        ResultSet tzrst;
        int updatecount = 0, companycount=0;

        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        //Get Server's timezone from which we ll convert date to Company Creator's timezone.
        TimeZone tzone = Calendar.getInstance().getTimeZone();
        System.out.println("Server Timezone Name : " + tzone.getDisplayName() + "\n");   //O/p E.g.: Indian Standard Time
//        out.println("Server Timezone Name : " + tzone.getDisplayName() + "<br/>");
        //serverTZID = tzone.getID();
       // serverTZID = "America/Phoenix";
        System.out.println("Server Timezone ID : " + serverTZID + "\n");  //O/p E.g. Asia/Calcutta
//        out.println("Server Timezone ID : " + serverTZID + "<br/>");

        //Define Timezone object
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        conn = DriverManager.getConnection(connectString, username, password);
        
            query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                subdomain = rst1.getString("subdomain");
                compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if(compCreator==null){
                    continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='" + utzone + "'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference") != null ? cprst.getString("difference") : "+08:00";
                }
                System.out.println(companycount +" : "+subdomain);
                out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
                // To Fetch company creator's timezone.
                String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid "
                        + "inner join company c on ur.userid = c.creator WHERE ur.company= '" + companyid + "'";
                tzpst = conn.prepareStatement(userTZQuery);
                tzrst = tzpst.executeQuery();
                if (tzrst.next()) {
                    userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ?tzrst.getString("difference"): defaultTZ;
                    df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                }
                pst2 = null;
                rst2 = null;

                //######################################     Vendor Module    ######################################
                System.out.println("Vendor Module Started...\n");
//                out.println("<b>Vendor Module Started...</b><br>");

                applyDateQuery = "SELECT v.id,v.name,v.createdon, v.modifiedon, v.mailon, v.selfbilledfromdate, v.selfbilledtodate"
                        + " FROM vendor v where v.company='" + companyid + "' ORDER BY v.createdon";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {

                    id = rst2.getString("id");
                    name = rst2.getString("name");
                    createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                    modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;
                    mailon = rst2.getObject("mailon") != null ? (java.util.Date) rst2.getObject("mailon") : null;
                    selfbilledfromdate = rst2.getObject("selfbilledfromdate") != null ? (java.util.Date) rst2.getObject("selfbilledfromdate") : null;
                    selfbilledtodate = rst2.getObject("selfbilledtodate") != null ? (java.util.Date) rst2.getObject("selfbilledtodate") : null;
                    pstinner = null;

                    //Convert Date from Server's timezone to Admin's timezone
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

                    if (mailon != null) {
                        newMailonDate = df.format(mailon);
                        if (!StringUtil.isNullOrEmpty(subQ1)) {
                            subQ1 += ", ";
                        }
                        subQ1 += " mailon='" + newMailonDate + "'";
                    }
                    if (selfbilledfromdate != null) {
                        newSelfbilledfromdate = df.format(selfbilledfromdate);
                        if (!StringUtil.isNullOrEmpty(subQ1)) {
                            subQ1 += ", ";
                        }
                        subQ1 += " selfbilledfromdate='" + newSelfbilledfromdate + "'";
                    }
                    if (selfbilledtodate != null) {
                        newSelfbilledtodate = df.format(selfbilledtodate);
                        if (!StringUtil.isNullOrEmpty(subQ1)) {
                            subQ1 += ", ";
                        }
                        subQ1 += " selfbilledtodate='" + newSelfbilledtodate + "' ";
                    }

                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        updateDate = "UPDATE vendor SET " + subQ1 + " WHERE id='" + id + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();

                    } else {
                        updatecount = 0;
                    }
                    if (updatecount > 0) {
                        //System.out.println("\n***************************************");
                        //System.out.println("Vendor (" + name + ") Updated Dates: \n Created On - " + createdon + "\n Modified On - " + modifiedon + "\n");
                        //System.out.println("Mail On - " + mailon + "\n Selfbilledfromdate - " + selfbilledfromdate + "\n Selfbilledtodate - " + selfbilledtodate + "\n");

                        //out.println("***************************************<br>");
                        //out.println("Vendor (" + name + ") Updated Dates: <br> Created On - " + createdon + "<br> Modified On - " + modifiedon + "<br>");
                        //out.println("Mail On - " + mailon + "<br> Selfbilledfromdate - " + selfbilledfromdate + "<br> Selfbilledtodate - " + selfbilledtodate + "<br>");
                    } else {
                        //System.out.println("Vendor Dates are not updated \n");
                        //out.println("Vendor Dates are not updated.<br>");
                    }
                    subQ1 = "";
                }
                System.out.println("Vendor Module Ended...\n");
//                out.println("<b>Vendor Module Ended...</b><br>");
                System.out.println("\n#######################################\n");
//                out.println("<br>#######################################<br>");

                //######################################     Customer Module    ######################################
                System.out.println("Customer Module Started...\n");
//                out.println("<b>Customer Module Started...</b><br>");

                applyDateQuery = "SELECT c.id,c.name,c.createdon, c.modifiedon FROM customer c where c.company='" + companyid + "' ORDER BY c.createdon";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {

                    id = rst2.getString("id");
                    name = rst2.getString("name");
                    createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                    modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;
                    pstinner = null;

                    //Convert Date from Server's timezone to Admin's timezone
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
                        updateDate = "UPDATE customer SET " + subQ1 + "WHERE id ='" + id + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    } else {
                        updatecount = 0;
                    }
                    if (updatecount > 0) {
                        //System.out.println("\n***************************************");
                        //System.out.println("Customer (" + name + ") Updated Dates: \n Created On - " + createdon + "\n Modified On - " + modifiedon + "\n");

                        //out.println("***************************************<br>");
                        //out.println("Customer (" + name + ") Updated Dates: <br> Created On - " + createdon + "<br> Modified On - " + modifiedon + "<br>");
                    } else {
                        //System.out.println("Customer Dates are not updated \n");
                        //out.println("Customer Dates are not updated.<br>");
                    }
                    subQ1 = "";
                }
                System.out.println("Customer Module Ended...\n");
//                out.println("<b>Customer Module Ended...</b><br>");
                System.out.println("\n#######################################\n");
//                out.println("<br>#######################################<br>");
                
                
                //######################################     Chart Of Account Module    ######################################
                System.out.println("Chart Of Account Module Started...\n");
//                out.println("<b>Chart Of Account Module Started...</b><br>");

                applyDateQuery = "SELECT ac.id, ac.name, ac.creationdate FROM account ac where ac.company='" + companyid + "' ORDER BY ac.creationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {

                    id = rst2.getString("id");
                    name = rst2.getString("name");
                    createdon = rst2.getObject("creationdate") != null ? (java.util.Date) rst2.getObject("creationdate") : null;
                    pstinner = null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (createdon != null) {
                        newcreationDate = df.format(createdon);
                        subQ1 = " creationdate='" + newcreationDate + "'";
                    } else {
                        subQ1 = "";
                    }

                    if (!StringUtil.isNullOrEmpty(subQ1)) {
                        updateDate = "UPDATE account SET " + subQ1 + "WHERE id ='" + id + "'";
                        pstinner = conn.prepareStatement(updateDate);
                        updatecount = pstinner.executeUpdate();
                    } else {
                        updatecount = 0;
                    }
                    if (updatecount > 0) {
                        //System.out.println("\n***************************************");
                        //System.out.println("Account (" + name + ") Updated Dates: \n Created On - " + createdon + "\n");

                        //out.println("***************************************<br>");
                        //out.println("Account (" + name + ") Updated Dates: <br> Created On - " + createdon + "<br>");
                    } else {
                        //System.out.println("Account Date is not updated \n");
                        //out.println("Account Date is not updated.<br>");
                    }
                    subQ1 = "";
                }
                //System.out.println("Chart Of Account Module Ended...\n");
//                out.println("<b>Chart Of Account Module Ended...</b><br>");
                //System.out.println("\n#######################################\n");
//                out.println("<center><b>=================================================================================</b></center><br><br>");
            }
      // }//Server query while
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