
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
        //SCRIPT URL : http://<app-url>/DateMigrationForPriceList.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        String userTZDiff = "", query1 = "", applyDateQuery = "", companyid = "";
        String pbmdId = "",compCreator = "", ctzone="", utzone="",defaultTZ="+08:00";

        Date creationdate = null;

        PreparedStatement serverpst = null, pst1 = null, pst2 = null,cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, rst3 = null,cprst=null;
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
        //Define Timezone object
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
        
            query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                String cmpSubdomain = rst1.getString("subdomain");
                compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if (StringUtil.isNullOrEmpty(compCreator)) {
                    compCreator = "";
                    continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                    df.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
                }
                //String compcreator = rst1.getString("creator");
                System.out.println(companycount +" : "+cmpSubdomain);
                out.println("<center><b>"+companycount+" : "+cmpSubdomain+"</b></center><br><br>");

                System.out.println("Pricing List Module Started...\n");
                out.println("<b>Subdomain '" + cmpSubdomain + "' Pricing List Module Started...</b><br>");
                applyDateQuery = "SELECT pricingbandmasterdetails.id, pricingbandmasterdetails.applicabledate, product.name FROM pricingbandmasterdetails INNER JOIN product ON pricingbandmasterdetails.product= product.id WHERE pricingbandmasterdetails.company='"
                        + companyid + "' ORDER BY product.name";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    pbmdId = rst2.getObject("id") != null ? rst2.getString("id") : "";
                    String productName = rst2.getObject("name") != null ? rst2.getString("name") : "";
                    out.println("<br><b>Product " + productName + " details are updating : </b><br>");
                    creationdate = rst2.getObject("applicabledate") != null ? (java.util.Date) rst2.getObject("applicabledate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    String newcreationDate = null;
                    if (creationdate != null) {
                        newcreationDate = "'" + df.format(creationdate) + "'";
                        String updatePReqDate = "UPDATE pricingbandmasterdetails SET applicabledate=" + newcreationDate + " WHERE id='" + pbmdId + "'";
                        pstinner = null;
                        pstinner = conn.prepareStatement(updatePReqDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //System.out.println("Updated Applicable Date : " + newcreationDate + "\n");
                            //out.println("Updated Applicable Date : " + newcreationDate + "<br>");
                        } else {
                            //System.out.println("Applicable Date is not updated \n");
                            //out.println("Applicable Date is not updated.<br>");
                        }
                    }
                }//pricingbandmasterdetails loop end
                out.println("<center><b>=================================================================================</b></center><br><br>");
            }//company id loop end
        /*}//server timezone loop end*/
        System.out.println("Products Module Ended...\n");
        out.println("<b>Products Module Ended...<b><br>");
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