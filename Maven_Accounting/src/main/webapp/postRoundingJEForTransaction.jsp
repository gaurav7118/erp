<%-- 
    Document   : AdjustRoundingDifference
    Created on : 5 Jan, 2016, 1:45:50 PM
    Author     : krawler
--%>

<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.io.IOException"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String moduleNumber = request.getParameter("moduleNumber");
        String checkRoundingDifferenceBeforePost = request.getParameter("checkRoundingDifference");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        int module = 0;
        if (!StringUtil.isNullOrEmpty(moduleNumber)) {
            try {
                module = Integer.parseInt(moduleNumber);
            } catch (Exception ex) {
                String msg = "</br>Invalid Value given for module number " + moduleNumber + "."
                        + "</br>You can give only following values for modules Number."
                        + "</br>" + Constants.Acc_Invoice_ModuleId + " for Sales Invoice"
                        + "</br>" + Constants.Acc_Goods_Receipt_ModuleId + " for Purchase Invoice"
                        + "</br> 0 for All Transactions";
                throw new Exception(msg);
            }
        }
        boolean checkRounding = false;
        if (!StringUtil.isNullOrEmpty(checkRoundingDifferenceBeforePost)) {
            try {
                checkRounding = Boolean.parseBoolean(checkRoundingDifferenceBeforePost);
            } catch (Exception ex) {
                String msg = "</br>Invalid Value given for checkRoundingDifference " + checkRoundingDifferenceBeforePost + "."
                        + "</br>You can give only true or false values for checkRoundingDifference."
                        + "</br>true for checking rounding difference"
                        + "</br>false for posting rounding JE into system.";
                throw new Exception(msg);
            }
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/postRoundingJEForTransactions.do";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement ps = null;
        ResultSet rs = null;
        String queryCompany = "";
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            queryCompany = "select companyid, currency,subdomain from company where subdomain=?";
            ps = conn.prepareStatement(queryCompany);
            ps.setString(1, subDomain);
            rs = ps.executeQuery();
        } else {
            queryCompany = "select companyid,currency, subdomain from company";
            ps = conn.prepareStatement(queryCompany);
            rs = ps.executeQuery();
        }
        String companyid, subdomain, currencyid;
        if (rs != null) {
            while (rs.next()) {
                companyid = rs.getString("companyid");
                subdomain = rs.getString("subdomain");
                currencyid = rs.getString("currency");
                InputStream iStream = null;
                try {
                    URL u = new URL(requestedURL);
                    URLConnection uc = u.openConnection();
                    uc.setDoOutput(true);
                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                    pw.print("companyid=" + companyid + "&gcurrencyid=" + currencyid + "&module=" + module + "&checkRounding=" + checkRounding);
                    pw.close();
                    iStream = uc.getInputStream();
                    java.io.BufferedReader in = null;
                    in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                    String res = in.readLine();
                    out.println("<br>********************************** Company || " + subdomain + " ******************************* " + res + "</br>");
                    in.close();
                    iStream.close();
                } catch (IOException iex) {
                    iex.printStackTrace();
                } finally {
                    if (iStream != null) {
                        try {
                            iStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    } catch (Exception ex) {
        out.println("Exception occuring while executing script - " + ex.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
