
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        //SCRIPT URL : http://<app-url>/DateMigrationForBankRecon.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = !StringUtil.isNullOrEmpty(request.getParameter("subdomain"))?request.getParameter("subdomain"):"";
        String serverTZDiff = "";
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", applyDateQuery = "", companyid = "", compCreator = "";
        String id = "", ctzone="", utzone="";
        int totalCount=0, updateCount=0, companycount=0; 
        
        String prevCreatedBy = "", createdby = "";
        PreparedStatement pst1 = null, pst2 = null, modifypst = null;
        ResultSet rst1 = null, rst2 = null, cprst=null;
        PreparedStatement pstinner, tzpst, cppst=null;
        ResultSet tzrst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
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
                compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if(compCreator==null){
                    continue;
                }
                subdomain = rst1.getString("subdomain");
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
                
                System.out.println(companycount +" : "+subdomain);
                out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
                System.out.println("Bank Reconciliation Module Started...\n");
                out.println("<b>Bank Reconciliation Module Started...</b>");
                prevCreatedBy = "";
                applyDateQuery = "SELECT br.id AS brid, br.startdate, br.enddate, br.clearancedate,  br.createdby FROM bankreconciliation br WHERE br.company='" + companyid + "' ORDER BY br.clearancedate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    createdby = rst2.getObject("createdby") != null ? rst2.getString("createdby") : compCreator;
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
                    id = rst2.getString("brid");
                    Date startDate = rst2.getObject("startdate") != null ? (java.util.Date) rst2.getObject("startdate") : null;
                    Date endDate = rst2.getObject("enddate") != null ? (java.util.Date) rst2.getObject("enddate") : null;
                    Date clearanceDate = rst2.getObject("clearancedate") != null ? (java.util.Date) rst2.getObject("clearancedate") : null;
                    String newStartDate=null, newEndDate=null, newClearanceDate=null, subquery="";
                    pstinner=null;
                    if (startDate != null) {
                        newStartDate = df.format(startDate);
                        subquery += " startdate='" + newStartDate + "' ";
                    }
                    if (endDate != null) {
                        if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                        newEndDate = df.format(endDate);
                        subquery += " enddate='" + newEndDate + "' ";
                    }
                    if (clearanceDate != null) {
                        if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                        newClearanceDate = df.format(clearanceDate);
                        subquery += " clearancedate='" + newClearanceDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                    String updateStartDate = "UPDATE bankreconciliation SET " + subquery + " WHERE id='" + id + "'";
                        pstinner = conn.prepareStatement(updateStartDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            //System.out.println("Bank Recon Dates are not updated for ID :"+id+"\n");
                            //out.println("Bank Recon Dates are not updated for ID :"+id+"<br>");
                        }
                   } else {
                        //System.out.println("Bank Recon Dates are not updated for ID :"+id+"\n");
                        //out.println("Bank Recon Dates are not updated for ID :"+id+"<br>");
                   }
                }//createdBy
                out.println("<center><b>=================================================================================</b></center><br><br>");
            }
            out.println("Total No. of Records :"+totalCount+" <br>");
            out.println("Updated No. of Records :"+updateCount+" <br>");
            System.out.println("Bank Reconciliation Module Ended...\n");
            out.println("<b>Bank Reconciliation Module Ended...</b>");      
        System.out.println("Bank Reconciliation Module Ended...\n");
        out.println("<b>Bank Reconciliation Module Ended...<b><br>");
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