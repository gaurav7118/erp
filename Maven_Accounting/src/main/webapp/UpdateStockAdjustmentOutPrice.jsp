<%-- 
    Document   : updateAmountDueAndDiscount
    Created on : Nov 6, 2015, 8:15:21 PM
    Author     : krawler
--%>

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
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%
    Connection conn = null;
    try {
        // String serverip = "localhost";
        //String port = "3306";
        //String dbName = "fasten17052016";
        //String userName = "krawlersqladmin";
        //String password = "Krawler[X]";
        String serverip = request.getParameter("serverip");
        String port = request.getParameter("port");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subDomain)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password,subdomain) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updateStockAdjustmentOutPrice.do";
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
        }



        String companyid, currencyid;
        if (rs != null) {
            while (rs.next()) {
                companyid = rs.getString("companyid");
                currencyid = rs.getString("currency");
                InputStream iStream = null;
                try {
                    // GET LOGIN INFORMATION FOR THE USER_ADMIN FOR PROVIDED SUBDOMAIN
                    String compQry = "SELECT  c.currency, ul.username, ul.password,ul.userid FROM company c INNER JOIN users u ON c.creator=u.userid  INNER JOIN userlogin ul ON u.userid=ul.userid where c.subdomain=? and ul.username='admin'";
                    out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");
                    PreparedStatement pst1 = conn.prepareStatement(compQry);
                    pst1.setString(1, subDomain);
                    ResultSet rst1 = pst1.executeQuery();
                    String username = "", pass = "", userid = "", currency = "";
                    if (rst1.next()) {
                        userid = rst1.getString("userid") != null ? rst1.getString("userid") : null;
                        username = rst1.getString("username") != null ? rst1.getString("username") : "";
                        pass = rst1.getString("password") != null ? rst1.getString("password") : "";
                    }

                    String requestParms = ("?u=" + username + "&p=" + pass + "&cdomain=" + subDomain + "&userid=" + userid + "&companyid=" + companyid + "&currencyid=" + currencyid);
                    URL u = new URL(requestedURL + requestParms);
                    URLConnection uc = u.openConnection();
                    uc.setDoOutput(true);
                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                    pw.print("cdomain=" + subDomain);

                    pw.close();
                    iStream = uc.getInputStream();
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                    String res = URLDecoder.decode(in.readLine());
                    out.println("<br> " + res + "</br>");
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
