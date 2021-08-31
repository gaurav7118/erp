
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForWriteOffTable.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
            String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String driver = "com.mysql.jdbc.Driver";
        String query1 = "", companyid = "";
        int totalCount=0, updateCount=0, companycount=0; 
        
        PreparedStatement pst1 = null, pst2 = null, modifypst = null, pstinner, cppst=null;
        ResultSet rst1 = null, rst2 = null, cprst=null;

        String subdomainQuery = "", defaultTZ = "", ctzone="", utzone="";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
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
            ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
            utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
            subdomain = rst1.getObject("subdomain") != null ? rst1.getString("subdomain") : "";   
            // To Fetch company creator's timezone.
            String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
            cppst = conn.prepareStatement(defaultTimezone);
            cprst = cppst.executeQuery();
            if (cprst.next()) {
                defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
            }
            df1.setTimeZone(TimeZone.getTimeZone("GMT"+ defaultTZ));
            System.out.println(companycount +" : "+subdomain);
            out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
            //============================================== Invoice WriteOff Dates =====================================
            String query = "SELECT id, writeoffdate FROM invoicewriteoff WHERE company='"+companyid+"' ORDER BY id";
            pst2 = conn.prepareStatement(query);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                Date writeoffdate = rst2.getObject("writeoffdate") != null ? (java.util.Date) rst2.getObject("writeoffdate") : null;
                String id = !StringUtil.isNullOrEmpty(rst2.getString("id"))?rst2.getString("id"):"";
                String newwriteoffdate=null;
                if (writeoffdate != null) {
                    newwriteoffdate = df1.format(writeoffdate);
                    pstinner = null;
                    String updateDate = "UPDATE invoicewriteoff SET writeoffdate = '" + newwriteoffdate + "' WHERE id ='" + id + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //out.println("Creation/Modification Date updated for Subdomain :"+subdom+"<br>");
                    } else {
                        //System.out.println("Creation/Modification Date is not updated for Subdomain :"+subdom+"\n");
                    }
                }
            }//createdBy
            //============================================== Invoice WriteOff Dates ==================================
            out.println("<br><br>");

            //============================================ Receipt WriteOff Dates ================================================           
            totalCount = 0; updateCount = 0;
            pst2 = null;
            rst2 = null;
            String query2 = "SELECT id, writeoffdate FROM receiptwriteoff WHERE company='"+companyid+"' ORDER BY id";
            pst2 = conn.prepareStatement(query2);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                String id = rst2.getString("id");
                Date rwriteoffdate = rst2.getObject("writeoffdate") != null ? (java.util.Date) rst2.getObject("writeoffdate") : null;
                String newrwriteoffdate=null;
                pstinner = null;
                if (rwriteoffdate != null) {
                    newrwriteoffdate = df1.format(rwriteoffdate);
                    String updatecreatedon = "UPDATE receiptwriteoff SET writeoffdate = '" + newrwriteoffdate + "' WHERE id='" + id + "'";
                    pstinner = conn.prepareStatement(updatecreatedon);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //Nothing to do
                    } else {
                        //System.out.println("System Controls : Store Dates are not updated \n");
                    }
                }
            }//createdBy
            //========================================== Receipt WriteOff Dates ================================================
            out.println("<br><br>");
        }//companyid
        System.out.println("Script Executed Successfully...\n");
        out.println("<b><center>Script Executed Successfully...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
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