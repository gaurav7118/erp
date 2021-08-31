
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForJournalEntry.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is a optional field

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
        String userTZQuery = "", userTZDiff = "", defaultTZ = "+08:00", query1 = "", applyDateQuery = "", companyid = "", compCreator = "";
        String compName = "", prevCreatedBy = "", createdby = "", id = "", chid = "", ctzone="", utzone="";
        PreparedStatement pst1 = null, pst2 = null, tzpst = null, cppst=null, pstinner;
        ResultSet rst1 = null, rst2 = null, tzrst = null, cprst=null;
        int totalCount=0, updateCount=0, companycount=0;
                
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
            compCreator = rst1.getObject("creator") != null ? rst1.getString("creator") : "";
            if (StringUtil.isNullOrEmpty(compCreator)) {
                compCreator = "";
                continue;
            }
            compName = rst1.getObject("subdomain") != null ? rst1.getString("subdomain") : ""; 
            ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
            utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                
            // To Fetch company creator's timezone.
            String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
            cppst = conn.prepareStatement(defaultTimezone);
            cprst = cppst.executeQuery();
            if (cprst.next()) {
                defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"):"+08:00";
            }
                                      
            System.out.println(companycount +" : "+compName);
            out.println("<center><b>"+companycount+" : "+compName+"</b></center><br><br>");
                
//======================================================= JOURNAL ENTRY MODULE - START =========================================================

            System.out.println("Journal Entry Module Started...\n");
//            out.println("<b>Journal Entry Module Started...</b>");
            prevCreatedBy = "";
            applyDateQuery = "SELECT je.id, je.entryno, je.createdby, je.entrydate, ch.id as chid, ch.duedate "
                    + " FROM journalentry je LEFT JOIN cheque ch ON je.cheque=ch.id WHERE je.company='" + companyid + "' ORDER BY je.entrydate";
            pst2 = conn.prepareStatement(applyDateQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                    totalCount++;
                    createdby = rst2.getObject("createdby") != null ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            userTZDiff = tzrst.getString("difference")!=null?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    chid = rst2.getObject("chid") != null ? rst2.getString("chid") : null;
                    Date entrydate = rst2.getObject("entrydate") != null ? (java.util.Date)rst2.getObject("entrydate") : null;
                    Date duedate = rst2.getObject("duedate") != null ? (java.util.Date)rst2.getObject("duedate") : null;
                    String jeentryno = rst2.getObject("entryno") != null ? rst2.getString("entryno") : "Unknown JE";
                    String newEntryDate = null, newDueDate = null, subquery = "";
                    if (entrydate != null) {
                        newEntryDate = df.format(entrydate);
                        pstinner = null;
                        String updateJEDates = "UPDATE journalentry SET entrydate='" + newEntryDate + "' WHERE id='" + id + "' AND company='"+companyid+"'";
                        pstinner = conn.prepareStatement(updateJEDates);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            //System.out.println("JE Dates are not updated.\n");
                            //out.println("JE Dates are not updated.<br>");
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(chid) && duedate != null) {
                        newDueDate = df.format(duedate);                        
                        pstinner = null;
                        String updateChkDates = "UPDATE cheque SET duedate='" + newDueDate + "' WHERE id='" + chid + "' AND company='"+companyid+"'";
                        pstinner = conn.prepareStatement(updateChkDates);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //Nothing to do
                        } else {
                            //System.out.println("Cheque Due Dates are not updated.\n");
                            //out.println("Cheque Due Dates are not updated.<br>");
                        }
                }            
                }            
//======================================================= JOURNAL ENTRY MODULE - END =========================================================             
//            out.println("<center><b>=================================================================================</b></center><br><br>");
        }//companyid
//        out.println("Total No. of Records :"+totalCount+" <br>");
//        out.println("Updated No. of Records :"+updateCount+" <br>");
//        out.println("<b>Journal Entry Module Ended...</b>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
        out.print("Dates are not updated.");
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("Connection Closed....<br>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>