
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
        //SCRIPT URL : http://<app-url>/DateMigrationForSystemControls.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?&serverTZ=?

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
        String query1 = "", companyid = "", compCreator="";
        int totalCount=0, updateCount=0, companycount=0; 
        
        PreparedStatement pst1 = null, pst2 = null, modifypst = null, pstinner, cppst=null;
        ResultSet rst1 = null, rst2 = null, cprst=null;

        String subdomainQuery = "", userTZDiff = "", defaultTZ = "+08:00", ctzone="", utzone="";
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
            compCreator = rst1.getString("creator") != null ? rst1.getString("creator") : null;
            if (StringUtil.isNullOrEmpty(compCreator)) {
                    compCreator = "";
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
            df1.setTimeZone(TimeZone.getTimeZone("GMT"+ defaultTZ));
            System.out.println(companycount +" : "+subdomain);
            out.println("<center><b>"+companycount+" : "+subdomain+"</b></center><br><br>");
                
            //============================================== Company Creation date / Modified Date =====================================
            System.out.println("Company Module Started...\n");
            out.println("<br><b>Company Module Started...</b>");
            String compQuery = "SELECT createdon, modifiedon, subdomain FROM company WHERE companyid='"+companyid+"' ORDER BY createdon";
            pst2 = conn.prepareStatement(compQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                Date createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                Date modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;
                String subdom = !StringUtil.isNullOrEmpty(rst2.getString("subdomain"))?rst2.getString("subdomain"):"";
                String newcreatedon=null, newmodifiedon = null, subquery="";;
                if (createdon != null) {
                    newcreatedon = df1.format(createdon);
                    subquery += " createdon='" + newcreatedon + "' ";
                }
                if (modifiedon != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newmodifiedon = df1.format(modifiedon);
                    subquery += " modifiedon='" + newmodifiedon + "' ";
                }
                if (!StringUtil.isNullOrEmpty(subquery)) {
                    pstinner = null;
                    String updateDate = "UPDATE company SET " + subquery + " WHERE companyid='" + companyid + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //out.println("Creation/Modification Date updated for Subdomain :"+subdom+"<br>");
                    } else {
                        //System.out.println("Creation/Modification Date is not updated for Subdomain :"+subdom+"\n");
                        //out.println("Creation/Modification Date is not updated for Subdomain :"+subdom+"<br>");
                    }
                }
            }//createdBy
            //============================================== Company Creation date / Modified Date ==================================
            out.println("<b>=====================================================================================================</b><br><br>");

            //============================================== USER ADMINISTRATIONS ======================================================================
/*            totalCount = 0; updateCount = 0;
            pst2 = null;
            rst2 = null;
            //Convert User last login date to UTC From Server Timezone.
            System.out.println("User Administration Module Started...\n");
            out.println("<br><b>User Administration Module Started...</b>");
            String userAdminQuery = "SELECT userid, lastactivitydate FROM userlogin ORDER BY lastactivitydate";
            pst2 = conn.prepareStatement(userAdminQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                String userid = rst2.getString("userid");
                Date userlastactivitydate = rst2.getObject("lastactivitydate") != null ? (java.util.Date) rst2.getObject("lastactivitydate") : null;
                if (userlastactivitydate != null) {
                    pstinner = null;
                    String newLastActivityDate = df1.format(userlastactivitydate);
                    String updateLastActivityDate = "UPDATE userlogin SET lastactivitydate='" + newLastActivityDate + "' WHERE userid='" + userid + "'";
                    pstinner = conn.prepareStatement(updateLastActivityDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //Nothing to do
                    } else {
                        System.out.println("User Last Activity Date is not updated \n");
                        out.println("User Last Activity Date is not updated.<br>");
                    }
                }
            }//createdBy
            out.println("Total No. of Records :"+totalCount+" <br>");
            out.println("Updated No. of Records :"+updateCount+" <br>");
            System.out.println("User Administration Module Ended...\n");
            out.println("<b>User Administration Module Ended...</b>");
*/           //================================================ END - USER ADMINISTRATION ============================================================
            
            //============================================== Company Prefernces Date Start ======================================================================
            //out.println("<br><b>Company Preferences Module Started...</b>");
            String comQuery = "SELECT fyfrom, firstfyfrom, bbfrom FROM compaccpreferences WHERE id='"+companyid+"' ORDER BY fyfrom";
            pst2 = conn.prepareStatement(comQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                Date fyfrom = rst2.getObject("fyfrom") != null ? (java.util.Date) rst2.getObject("fyfrom") : null;
                Date firstfyfrom = rst2.getObject("firstfyfrom") != null ? (java.util.Date) rst2.getObject("firstfyfrom") : null;
                Date bbfrom = rst2.getObject("bbfrom") != null ? (java.util.Date) rst2.getObject("bbfrom") : null;
                String newfyfrom = null, newfirstfyfrom = null, newbbfrom = null, subquery="";;
                if (fyfrom != null) {
                    newfyfrom = df1.format(fyfrom);
                    subquery += " fyfrom='" + newfyfrom + "' ";
                }
                if (firstfyfrom != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newfirstfyfrom = df1.format(firstfyfrom);
                    subquery += " firstfyfrom='" + newfirstfyfrom + "' ";
                }
                if (bbfrom != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newbbfrom = df1.format(bbfrom);
                    subquery += " bbfrom='" + newbbfrom + "' ";
                }
                if (!StringUtil.isNullOrEmpty(subquery)) {
                    pstinner = null;
                    String updateDate = "UPDATE compaccpreferences SET " + subquery + " WHERE id='" + companyid + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        //updateCount++;
                    } else {
                        //Nothing to do
                    }
                }
            }//createdBy
            //============================================== Company Prefernces Date End ======================================================================
            out.println("<b>=====================================================================================================</b>");

            //========================================================= START- SYSTEM CONTROLS =======================================================           
            totalCount = 0; updateCount = 0;
            pst2 = null;
            rst2 = null;
            System.out.println("System Control Module Started...\n");
            out.println("<br><b>System Control Module Started...</b><br>");
            String systemControlsQuery = "SELECT id, createdon, modifiedon FROM in_storemaster WHERE company='" + companyid + "' ORDER BY createdon";
            pst2 = conn.prepareStatement(systemControlsQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                String sysid = rst2.getString("id");
                Date createdon = rst2.getObject("createdon") != null ? (java.util.Date) rst2.getObject("createdon") : null;
                Date modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date) rst2.getObject("modifiedon") : null;
                String newcreatedon=null, newmodifiedon=null, subquery="";
                pstinner = null;
                if (createdon != null) {
                    newcreatedon = df1.format(createdon);
                    subquery += " createdon='" + newcreatedon + "' ";
                }
                if (modifiedon != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newmodifiedon = df1.format(modifiedon);
                    subquery += " modifiedon='" + newmodifiedon + "' ";
                }
                if (!StringUtil.isNullOrEmpty(subquery)) {
                    String updatecreatedon = "UPDATE in_storemaster SET " + subquery + " WHERE id='" + sysid + "'";
                    pstinner = conn.prepareStatement(updatecreatedon);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //Nothing to do
                    } else {
                        //System.out.println("System Controls : Store Dates are not updated \n");
                        //out.println("System Controls : Store Dates are not updated.<br>");
                    }
                }
            }//createdBy
            out.println("Total No. of Records :"+totalCount+" <br>");
            out.println("Updated No. of Records :"+updateCount+" <br>");
            System.out.println("System Control Module Ended...\n");
            out.println("<br><b>System Control Module Ended...</b><br>");
            //========================================================= END - SYSTEM CONTROLS ===========================================================
            out.println("<b>=====================================================================================================</b>");

            //========================================================= START- ACTIVE DATE RANGE =======================================================
            totalCount = 0; updateCount = 0;
            pst2 = null;
            rst2 = null;
            System.out.println("Update Active Date Range Module Started...\n");
            out.println("<br><b>Update Active Date Range Module Started...</b><br>");
            String daterangeQuery = "SELECT ec.id, ec.activefromdate, ec.activetodate, ec.lastsyncwithpm, ec.mslastsyncwithpm, ec.gsteffectivedate FROM extracompanypreferences ec WHERE ec.id='" + companyid + "' ORDER BY ec.activefromdate";
            pst2 = conn.prepareStatement(daterangeQuery);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                totalCount++;
                String daterangeid = rst2.getString("id");
                Date activeFromDate = rst2.getObject("activefromdate") != null ? (java.util.Date) rst2.getObject("activefromdate") : null;
                Date activeToDate = rst2.getObject("activetodate") != null ? (java.util.Date) rst2.getObject("activetodate") : null;
                Date lastsyncwithpm = rst2.getObject("lastsyncwithpm") != null ? (java.util.Date) rst2.getObject("lastsyncwithpm") : null;
                Date mslastsyncwithpm = rst2.getObject("mslastsyncwithpm") != null ? (java.util.Date) rst2.getObject("mslastsyncwithpm") : null;
                Date gsteffectivedate = rst2.getObject("gsteffectivedate") != null ? (java.util.Date) rst2.getObject("gsteffectivedate") : null;
                
                String newactiveFromDate=null, newactiveToDate=null, newlastsyncwithpm=null, newmslastsyncwithpm=null, newgsteffectivedate=null, subquery="";
                pstinner = null;
                //df1.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
                if (activeFromDate != null) {
                    newactiveFromDate = df1.format(activeFromDate);
                    subquery += " activefromdate='" + newactiveFromDate + "' "; 
                }
                if (activeToDate != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newactiveToDate = df1.format(activeToDate);
                    subquery += " activetodate='" + newactiveToDate + "' "; 
                }
                if (lastsyncwithpm != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newlastsyncwithpm = df1.format(lastsyncwithpm);
                    subquery += " lastsyncwithpm='" + newlastsyncwithpm + "' "; 
                }
                if (mslastsyncwithpm != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newmslastsyncwithpm = df1.format(mslastsyncwithpm);
                    subquery += " mslastsyncwithpm='" + newmslastsyncwithpm + "' "; 
                }
                if (gsteffectivedate != null) {
                    if (!StringUtil.isNullOrEmpty(subquery)) {
                            subquery += ", ";
                        }
                    newgsteffectivedate = df1.format(gsteffectivedate);
                    subquery += " gsteffectivedate='" + newgsteffectivedate + "' "; 
                }
                if (!StringUtil.isNullOrEmpty(subquery)) {
                    pstinner = null;
                    String updateDate = "UPDATE extracompanypreferences SET " + subquery + " WHERE id='" + daterangeid + "'";
                    pstinner = conn.prepareStatement(updateDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        updateCount++;
                        //Nothing to do.
                    } else {
                        //System.out.println("Active From/To Date is not updated for ID :"+daterangeid+"\n");
                        //out.println("Active From/To Date is not updated for ID :"+daterangeid+"<br>");
                    }
                }
            }
            out.println("<br><b>Total No. of Records :"+totalCount+" </b><br>");
            out.println("<br><b>Updated No. of Records :"+updateCount+" </b><br>");
            System.out.println("Update Active Date Range Module Ended...\n");
            out.println("<br><b>Update Active Date Range Module Ended...</b>");
            //========================================================= END - ACTIVE DATE RANGE =======================================================
            out.println("<center><b>=================================================================================</b></center><br><br>");
        }//companyid
        System.out.println("Script Ended...\n");
        out.println("<b>Script Ended...<b><br>");
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