
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForProductsOldRecordsCreationDate.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        String prLst = "",compCreator = "", ctzone="", utzone="",defaultTZ="+08:00";

        PreparedStatement serverpst = null, pst1 = null, pst2 = null,cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, rst3 = null,cprst=null;
        PreparedStatement pstinner, tzpst;
        ResultSet tzrst;
        int companycount=0;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        //Define Timezone object
        DateFormat df = null;
        DateFormat constantDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        constantDf.setTimeZone(TimeZone.getTimeZone("GMT"));

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
                }
                
                df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
                System.out.println(companycount +" : "+cmpSubdomain);
                out.println("<center><b>"+companycount+" : "+cmpSubdomain+"</b></center><br><br>");

                String companyPreferencesQuery = "SELECT firstfyfrom from compaccpreferences where id='" + companyid + "'";
                PreparedStatement cmpPrefSt = conn.prepareStatement(companyPreferencesQuery);
                ResultSet cmpPrefRst = cmpPrefSt.executeQuery();
                if(cmpPrefRst.next()) {//moved cursor to first row
                    Date firstFYDate = (java.util.Date)cmpPrefRst.getObject("firstfyfrom");

                    System.out.println("Products Module Started...\n");
                    out.println("<b>Subdomain '" + cmpSubdomain + "' Products Module Started...</b><br>");
                    applyDateQuery = "SELECT p.id, p.name FROM product p WHERE p.company='" + companyid + "' and p.deleteflag='F' ORDER BY p.createdon";
                    pst2 = conn.prepareStatement(applyDateQuery);
                    rst2 = pst2.executeQuery();
                    while (rst2.next()) {
                        prLst = rst2.getObject("id") != null ? rst2.getString("id") : "";
                        String productName = rst2.getObject("name") != null ? rst2.getString("name") : "";
                        out.println("<br><b>Product " + productName + " details are updating : </b><br>");

                        Date productCreationDate = null;
                        String checkInventoryQuery = "select updatedate from inventory where product='" + prLst + "' and newInv='T' and deleteflag='F' and updatedate IS NOT NULL and updatedate<>''";
                        PreparedStatement checkInventorySt = conn.prepareStatement(checkInventoryQuery);
                        ResultSet checkInventoryRst = checkInventorySt.executeQuery();
                        if(checkInventoryRst.next()){
                            productCreationDate = (java.util.Date)checkInventoryRst.getObject("updatedate");
                        } else {
                            productCreationDate = firstFYDate;
                        }

                        //Convert Date from Server's timezone to Admin's timezone
                        String newcreationDate = null;
                        if (productCreationDate != null) {
                            newcreationDate = "'" + df.format(productCreationDate) + "'";
                        }

                        String updatePReqDate = "UPDATE product SET asofdate=" + newcreationDate + " WHERE id='" + prLst + "'";
                        pstinner = null;
                        pstinner = conn.prepareStatement(updatePReqDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //System.out.println("Updated Creation Date : " + newcreationDate + "\n");
                            //out.println("Updated Creation Date : " + newcreationDate + "<br>");
                        } else {
                            //System.out.println("Creation Date is not updated \n");
                            //out.println("Creation Date is not updated.<br>");
                        }
                    }//pricingbandmasterdetails loop end
                    out.println("<center><b>=================================================================================</b></center><br><br>");
                }//if block end First financial year
                df = null;
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
                //Execution Started :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>