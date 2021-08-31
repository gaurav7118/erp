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
<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        boolean updateData = Boolean.parseBoolean(request.getParameter("updateData"));
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);        
        ResultSet rs;
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();

        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updateCNDNBaseAmountDue.do?updateData="+updateData;

        String companyId = "";
        String subdomain = "";
        String queryCompany = "";
        PreparedStatement ps = null;
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            queryCompany = "select companyid, subdomain from company where subdomain=? order by subdomain";
            ps = conn.prepareStatement(queryCompany);
            ps.setString(1, subDomain);
            rs = ps.executeQuery();
        } else {
            queryCompany = "select companyid, subdomain from company order by subdomain";
            ps = conn.prepareStatement(queryCompany);
            rs = ps.executeQuery();
        }

        while (rs.next()) {
            companyId = rs.getString("companyid");
            subdomain = rs.getString("subdomain");
            InputStream iStream = null;

            try {
                URL u = new URL(requestedURL);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.print("cid=" + companyId);
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                String res = URLDecoder.decode(in.readLine());
                System.out.println("***************************************************************");
                System.out.println("Completed for Company - " + subdomain+".");
                System.out.println("Status of "+subdomain+" : "+res);
                System.out.println("***************************************************************");
                
                out.println("***************************************************************");
                out.println("</br>");
                out.println("Completed for Company - " + subdomain+".");
                out.println("</br>");
                out.println("Status of "+subdomain+" : "+res);
                out.println("</br>");
                out.println("***************************************************************");
                
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



    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>    