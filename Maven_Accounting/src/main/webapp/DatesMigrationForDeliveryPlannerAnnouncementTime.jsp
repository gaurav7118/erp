
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForDeliveryPlanner.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

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
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", companyid = "", compcreator = "", subdom = "", dpaquery = "";
        String id = "", ctzone="", utzone="";
        int totalCount = 0, updateCount = 0, companycount=0;

        PreparedStatement pst1 = null, pst2 = null,cppst=null;
        ResultSet rst1 = null, rst2 = null, cprst=null;
        PreparedStatement modifypst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        Class.forName(driver).newInstance();
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        conn = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            companycount++;
            companyid = rst1.getString("companyid");
            subdom = rst1.getString("subdomain");
            compcreator = rst1.getString("creator") != null ? rst1.getString("creator") : null;
                if (StringUtil.isNullOrEmpty(compcreator)) {
                    compcreator = "";
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
                
            System.out.println(companycount +" : "+subdom);
            out.println("<center><b>"+companycount+" : "+subdom+"</b></center><br><br>");
                
            System.out.println("Delivery Planner-Announcement Module Started...\n");
            out.println("<b>Delivery Planner-Announcement Module Started...</b>");

            df2.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
            dpaquery = "SELECT id AS ida, announcementtime FROM deliveryplannerannouncement WHERE company='" + companyid + "'";
            pst2 = null;
            rst2 = null;
            try {
                modifypst = null;
                pst2 = conn.prepareStatement(dpaquery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    String ida = rst2.getString("ida");
                    Date announcementtime = rst2.getObject("announcementtime") != null ? (java.util.Date) rst2.getObject("announcementtime") : null;
                    String newAnnouncementTime = "";
                    if (announcementtime != null) {
                        newAnnouncementTime = df2.format(announcementtime);
                        String updateqry = "UPDATE deliveryplannerannouncement SET announcementtime='" + newAnnouncementTime + "' WHERE id='" + ida + "' AND company='" + companyid + "'";
                        modifypst = conn.prepareStatement(updateqry);
                        int updatecount = modifypst.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                        } else {
                            //System.out.println("Delivery Planner Announcement Date is not updated for ID :" + id + "\n");
                            //out.println("Delivery Planner Announcement Date is not updated for ID :" + id + "<br>");
                        }
                    }
                }
            } catch (SQLException se) {
                out.println("Delivery Planner Announcement Date Exception is not updated for ID :" + id + "<br>");
                se.printStackTrace();
            }
            out.println("<center><b>=================================================================================</b></center><br><br>");
        }//company
        out.println("<br><br>Total No. of Records :" + totalCount + " <br>");
        out.println("Updated No. of Records :" + updateCount + " <br><br>");

        System.out.println("Delivery Planner-Announcement Module Ended...\n");
        out.println("<b>Delivery Planner-Announcement Module Ended...<b><br>");
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