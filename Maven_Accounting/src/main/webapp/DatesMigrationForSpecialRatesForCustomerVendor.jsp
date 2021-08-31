
<%@page import="com.krawler.spring.authHandler.authHandler"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%!
%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/DateMigrationForSpecialRatesForCustomerVendor.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        String serverTZQuery = "", serverTZDiff = "", userTZDiff = "", query1 = "", applyDateQuery = "", companyid = "";
        String prLst = "";

        Date creationdate = null;

        PreparedStatement serverpst = null, pst1 = null, pst2 = null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, rst3 = null;
        PreparedStatement pstinner, tzpst, modifypst;
        ResultSet tzrst;
        int companycount=0;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        //Get Server's timezone from which we ll convert date to Company Creator's timezone.
        TimeZone tzone = Calendar.getInstance().getTimeZone();
        System.out.println("Server Timezone Name : " + tzone.getDisplayName() + "\n");   //O/p E.g.: Indian Standard Time
        out.println("Server Timezone Name : " + tzone.getDisplayName() + "<br/>");
//        String serverTZID = tzone.getID();
//        System.out.println("Server Timezone ID : " + serverTZID + "\n");  //O/p E.g. Asia/Calcutta
//        out.println("Server Timezone ID : " + serverTZID + "<br/>");

        //Define Timezone object
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat constantDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        constantDf.setTimeZone(TimeZone.getTimeZone("GMT"));

        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
//        serverTZQuery = "SELECT difference FROM timezone WHERE tzid like '%" + serverTZID + "%' OR name like '%" + tzone.getDisplayName() + "%'";
//        serverpst = conn.prepareStatement(serverTZQuery);
//        serverrst = serverpst.executeQuery();
//        while (serverrst.next()) {
//            serverTZDiff = serverrst.getString("difference");
//            System.out.println("Server Timezone Difference : " + serverTZDiff + "\n");
            query1 = "SELECT companyid, subdomain FROM company " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                String cmpSubdomain = rst1.getString("subdomain");
                System.out.println(companycount +" : "+cmpSubdomain);
                out.println("<center><b>"+companycount+" : "+cmpSubdomain+"</b></center><br><br>");
                
                // To Fetch company creator's timezone.
                String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid "
                        + "inner join company c on ur.userid = c.creator WHERE ur.company= '" + companyid + "'";
                tzpst = conn.prepareStatement(userTZQuery);
                tzrst = tzpst.executeQuery();
                if (tzrst.next()) {
                    userTZDiff = tzrst.getString("difference");
                    df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                }

                System.out.println("Special Rates Module Started...\n");
                out.println("<b>Subdomain '" + cmpSubdomain + "' Special Rates Module Started...</b><br>");
                applyDateQuery = "SELECT pricelist.id, pricelist.applydate, product.name FROM pricelist INNER JOIN product ON pricelist.product= product.id WHERE pricelist.company='"
                        + companyid + "' ORDER BY product.name";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    prLst = rst2.getObject("id") != null ? rst2.getString("id") : "";
                    String productName = rst2.getObject("name") != null ? rst2.getString("name") : "";
                    out.println("<br><b>Product " + productName + " details are updating : </b><br>");
                    creationdate = rst2.getObject("applydate") != null ? (java.util.Date) rst2.getObject("applydate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    String newcreationDate = null;
                    if (creationdate != null) {
                        newcreationDate = "'" + df.format(creationdate) + "'";
                    }

                    String updatePReqDate = "UPDATE pricelist SET applydate=" + newcreationDate + " WHERE id='" + prLst + "'";
                    pstinner = null;
                    pstinner = conn.prepareStatement(updatePReqDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        //System.out.println("Updated Apply Date : " + newcreationDate + "\n");
                        //out.println("Updated Apply Date : " + newcreationDate + "<br>");
                    } else {
                        //System.out.println("Apply Date is not updated \n");
                       // out.println("Apply Date is not updated.<br>");
                    }
                }//pricingbandmasterdetails loop end
                out.println("<center><b>=================================================================================</b></center><br><br>");
            }//company id loop end
//        }//server timezone loop end
        System.out.println("Special Rates Ended...\n");
        out.println("<b>Special Rates Module Ended...<b><br>");
/*        
        //Modify data type of applydate column as Date instead of timestamp
        String modifyDate = "ALTER TABLE pricelist MODIFY applydate DATE";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of applydate column is altered to 'Date'.\n");
            out.println("Data type of applydate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Apply Date column data type not modified.<br/>");
        }       */
    } catch (Exception ex) {
        conn.rollback();
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
        out.print("Dates are not updated.");
    } finally {
        if (conn != null) {
            try {
                conn.commit();
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