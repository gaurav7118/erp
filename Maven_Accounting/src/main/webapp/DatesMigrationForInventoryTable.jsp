
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForInventoryTable.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

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
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "+08:00", query1 = "", applyDateQuery = "", companyid = "", subdom = "";
        String id = "",compCreator="", ctzone="", utzone="", defaultTZ="+08:00";
        int totalCount = 0, updateCount = 0, companycount=0;

        PreparedStatement pst1 = null, pst2 = null;
        ResultSet rst1 = null, rst2 = null, cprst=null;
        PreparedStatement pstinner, cppst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        Class.forName(driver).newInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        conn = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            companycount++;
            companyid = rst1.getString("companyid");
            subdom = rst1.getString("subdomain");
            compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if (StringUtil.isNullOrEmpty(compCreator)) {
                    compCreator = "";
                    continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='" + utzone + "'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = !StringUtil.isNullOrEmpty(cprst.getString("difference")) ? cprst.getString("difference") : "+08:00";
                }

            System.out.println(companycount +" : "+subdom);
            out.println("<center><b>"+companycount+" : "+subdom+"</b></center><br><br>");
                
            System.out.println("Inventory Table Started...\n");
            out.println("<b>Inventory Table Started...</b>");

            applyDateQuery = "SELECT dp.id AS dpid,dp.updatedate FROM inventory dp WHERE dp.company='" + companyid + "' AND isupdated=0";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            df.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
            while (rst2.next()) {
                totalCount++;
                id = rst2.getString("dpid");
                Date updatedate = rst2.getObject("updatedate") != null ? (java.util.Date) rst2.getObject("updatedate") : null;
                String newupdatedate = null;
                pstinner = null;
                if (updatedate != null) {
                    newupdatedate = df.format(updatedate);
                    String updateDP = "UPDATE inventory SET updatedate = '" + newupdatedate + "', isupdated= '1' WHERE id='" + id + "' AND company='" + companyid + "'";
                    pstinner = conn.prepareStatement(updateDP);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //Nothing to do
                    } else {
                        //System.out.println("Inventory Table are not updated for ID :" + id + "\n");
                        //out.println("Inventory Table are not updated for ID :" + id + "<br>");
                    }
                }

            }//createdBy 
            out.println("<center><b>=================================================================================</b></center><br><br>");
        }//company
        out.println("Total No. of Records :" + totalCount + " <br>");
        out.println("Updated No. of Records :" + updateCount + " <br>");
        System.out.println("Inventory Table Ended...\n");
        out.println("<b>DInventory Table Ended...</b>");

        System.out.println("Inventory Table Ended...\n");
        out.println("<b>Inventory Table Ended...<b><br>");
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
                out.println("<br><br>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>