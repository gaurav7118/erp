
%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
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
        //SCRIPT URL : http://<app-url>/DeleteDuplicatePriceListRecordFoSameDate.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

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
        int updateCount = 0, companycount = 0;

        PreparedStatement pst = null, pst1 = null, pst2 = null, pst3 = null;
        ResultSet rst1 = null, rst2 = null, rst3 = null;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        //Execution Started :
        out.println("<br><br><b><center>Execution Started @ " + new java.util.Date() + "</center><b><br><br>");

        conn = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT c.companyid, c.subdomain FROM company c " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            companycount++;
            companyid = !StringUtil.isNullOrEmpty(rst1.getString("companyid")) ? rst1.getString("companyid") : null;
            subdomain = !StringUtil.isNullOrEmpty(rst1.getString("subdomain")) ? rst1.getString("subdomain") : null;
            out.println("<center><b>" + companycount + " : " + subdomain + "</b></center><br><br>");
            String queryt = "SELECT id, DATE_FORMAT(applydate,'%Y-%m-%d') AS applydate, product FROM pricelist WHERE company='" + companyid + "' AND carryin='T' ORDER BY product, applydate DESC";
            pst2 = conn.prepareStatement(queryt);
            rst2 = pst2.executeQuery();
            String prevProdid = "";
            String preAppDate = "";
            while (rst2.next()) {
                String plid = !StringUtil.isNullOrEmpty(rst2.getString("id")) ? rst2.getString("id") : null;
                String applyDate = rst2.getObject("applydate") != null ? rst2.getString("applydate") : null;
                String productid = !StringUtil.isNullOrEmpty(rst2.getString("product")) ? rst2.getString("product") : null;
                if (!productid.equalsIgnoreCase(prevProdid)) {
                    prevProdid = productid;
                    preAppDate = applyDate;
                } else if (!applyDate.equals(preAppDate)) {
                    preAppDate = applyDate;
                } else {
                    try {
                        String delQuery = "DELETE FROM pricelist WHERE id='" + plid + "' AND company='" + companyid + "' AND product='" + productid + "'";
                        pst = conn.prepareStatement(delQuery);
                        pst.executeUpdate();
                        updateCount++;
                        pst = null;
                    } catch (Exception se) {
                        System.out.println("Exception occurred while deleting the record !!");
                        out.println("<center><b>Exception occurred while deleting the record !!</b></center><br><br>");
                        se.printStackTrace();
                    }
                }
            }//CARRY-IN : True
            pst2 = null;
            rst2 = null;
            
            String queryf = "SELECT id, DATE_FORMAT(applydate,'%Y-%m-%d') AS applydate, product FROM pricelist WHERE company='" + companyid + "' AND carryin='F' ORDER BY product, applydate DESC";
            pst2 = conn.prepareStatement(queryf);
            rst2 = pst2.executeQuery();
            String prevProdidf = "";
            String preAppDatef = "";
            while (rst2.next()) {
                String plidf = !StringUtil.isNullOrEmpty(rst2.getString("id")) ? rst2.getString("id") : null;
                String applyDatef = rst2.getObject("applydate") != null ? rst2.getString("applydate") : null;
                String productidf = !StringUtil.isNullOrEmpty(rst2.getString("product")) ? rst2.getString("product") : null;
                if (!productidf.equalsIgnoreCase(prevProdidf)) {
                    prevProdidf = productidf;
                    preAppDatef = applyDatef;
                } else if (!applyDatef.equals(preAppDatef)) {
                    preAppDatef = applyDatef;
                } else {
                    try {
                        String delQueryf = "DELETE FROM pricelist WHERE id='" + plidf + "' AND company='" + companyid + "' AND product='" + productidf + "'";
                        pst = conn.prepareStatement(delQueryf);
                        pst.executeUpdate();
                        updateCount++;
                        pst = null;
                    } catch (Exception se) {
                        System.out.println("Exception occurred while deleting the record !!");
                        out.println("<center><b>Exception occurred while deleting the record !!</b></center><br><br>");
                        se.printStackTrace();
                    }
                }
            }//CARRY-IN : True
            out.println("<center><b>"+updateCount+" Duplicate Records are deleted for "+subdomain+"</b></center><br><br>");
            out.println("<center><b>=======================================================================</b></center><br><br>");
            pst2 = null;
            rst2 = null;
        }//companyid
        System.out.println("Script Ended...\n");
        out.println("<b><center>Script Ended...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("<center>Connection Closed....</center><br/>");
                //Execution Ended :
                out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "</center><br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>