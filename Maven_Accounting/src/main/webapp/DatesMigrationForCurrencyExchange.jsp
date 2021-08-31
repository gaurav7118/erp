
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Date"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/UpdateCurrencyExchangeDateWithoutTime.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (Parameter are: serverip, dbname, username, password, subdomain) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", applyDateQuery = "", companyid = "", compCreator="";
        String exchangeid = "", ctzone="", utzone="";
        int totalCount=0, updateCount=0, companycount=0;
        
        PreparedStatement pst1 = null, pst2 = null, modifypst = null, cppst=null;
        ResultSet rst1 = null, rst2 = null, cprst=null;
        PreparedStatement pst;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        conn = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            companycount++;
            companyid = rst1.getString("companyid");
            compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
            if (StringUtil.isNullOrEmpty(compCreator)){
               compCreator = "";
               continue;
            }
            subdomain = rst1.getString("subdomain");
            ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Arizona
            utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;             
            // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='" + utzone + "'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
            
            System.out.println(companycount +" : "+subdomain);
            out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
            df.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
            applyDateQuery = "SELECT id, applydate, todate FROM exchangeratedetails where company='" + companyid + "' ORDER BY applydate";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                exchangeid = rst2.getString("id") != null ? rst2.getString("id") : null;
                Date applyDate = rst2.getDate("applydate") != null ? (java.util.Date) rst2.getObject("applydate") : null;
                Date todate = rst2.getDate("todate") != null ? (java.util.Date) rst2.getObject("todate") : null;
                String newApplyDate = null, newToDate = null, subquery="";
                pst = null;
                if (exchangeid != null) {
                    if (applyDate != null) {
                        newApplyDate = df.format(applyDate);
                        subquery += " applydate='" + newApplyDate + "' ";
                    }
                    if (todate != null) {
                        if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                        newToDate = df.format(todate);
                        subquery += " todate='" + newToDate + "' ";
                    }
                }
                if (!StringUtil.isNullOrEmpty(subquery)) {
                pst = null;
                String updateAppDate = "UPDATE exchangeratedetails SET " + subquery + " WHERE id='" + exchangeid + "'";
                pst = conn.prepareStatement(updateAppDate);
                int updatecount = pst.executeUpdate();
                if (updatecount > 0) {
                    updateCount++;
                    //Nothing to do.
                } else {
                    //System.out.println("Apply Date / Todate is not updated for ID :" + exchangeid + " \n");
                    //out.println("Apply Date / Todate is not updated for ID :" + exchangeid + " <br>");
                }
              } else {
                    //System.out.println("Apply Date / Todate is not updated.\n");
                    //out.println("Apply Date / Todate is not updated.<br>");
              }
            }//applyDateQuery while
            out.println("<center><b>=================================================================================</b></center><br><br>");            
        }//query-1 while
        
            out.println("<br><b>Total No. of Records :"+totalCount+" <b><br>");
            out.println("<b>Updated No. of Records :"+updateCount+" <b><br>");
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