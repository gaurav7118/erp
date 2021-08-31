
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
        String serverTZDiff = "";
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", applyDateQuery = "", companyid = "", compCreatorTZ = "", subdom="", compcreator="";
        String id = "", ctzone="", utzone="";
        int totalCount=0, updateCount=0, companycount=0; 
        
        String prevCreatedBy = "", createdby = "";
        PreparedStatement pst1 = null, pst2 = null, cppst=null;
        ResultSet rst1 = null, rst2 = null, cprst=null;
        PreparedStatement pstinner, tzpst;
        ResultSet tzrst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        conn = DriverManager.getConnection(connectString, username, password);
            query1 =  "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                compcreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if (StringUtil.isNullOrEmpty(compcreator)) {
                        compcreator = "";
                        continue;
                }
                subdom = rst1.getString("subdomain");
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
                
                System.out.println(companycount +" : "+subdom);
                out.println("<center><b>"+companycount+" : "+subdom+"</b></center><br><br>");
                
                System.out.println("Delivery Planner Module Started...\n");
                out.println("<b>Delivery Planner Module Started...</b>");
                
                //df2.setTimeZone(TimeZone.getTimeZone("GMT" + compCreatorTZ));
                prevCreatedBy = "";
                applyDateQuery = "SELECT dp.id AS dpid, dp.pushtime, dp.deliverydate, dp.fromuser FROM deliveryplanner dp WHERE dp.company='" + companyid + "' ORDER BY dp.pushtime";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    createdby = rst2.getObject("fromuser") != null ? rst2.getString("fromuser") : compcreator;
                    if (createdby != null && (!prevCreatedBy.equalsIgnoreCase(createdby))) {
                        prevCreatedBy = createdby;
                        String userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }
                    id = rst2.getString("dpid");
                    Date pushtime = rst2.getObject("pushtime") != null ? (java.util.Date) rst2.getObject("pushtime") : null;
                    Date deliverydate = rst2.getObject("deliverydate") != null ? (java.util.Date) rst2.getObject("deliverydate") : null;
                    String newPushTime=null, newDeliveryDate=null, subquery="";
                    pstinner=null;
                    if (pushtime != null) {
                        newPushTime = df.format(pushtime);
                        subquery += " pushtime='" + newPushTime + "' ";
                    }
                    if (deliverydate != null) {
                        if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                        newDeliveryDate = df.format(deliverydate);
                        subquery += " deliverydate='" + newDeliveryDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                    String updateDP = "UPDATE deliveryplanner SET " + subquery + " WHERE id='" + id + "'";
                        pstinner = conn.prepareStatement(updateDP);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            //System.out.println("Delivery Planner Dates are not updated for ID :"+id+"\n");
                            //out.println("Delivery Planner Dates are not updated for ID :"+id+"<br>");
                        }
                   }
                }//createdBy 
                out.println("<center><b>=================================================================================</b></center><br><br>");
            }//company
            out.println("Total No. of Records :"+totalCount+" <br>");
            out.println("Updated No. of Records :"+updateCount+" <br>");
            System.out.println("Delivery Planner Module Ended...\n");
            out.println("<b>Delivery Planner Module Ended...</b>");
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