
<%@page import="com.krawler.spring.accounting.handler.AccountingManager"%>
<%@page import="com.krawler.spring.accounting.currency.accCurrencyDAO"%>
<%@page import="com.krawler.hql.accounting.JournalEntry"%>
<%@page import="com.krawler.spring.accounting.handler.AccountingHandlerDAO"%>
<%@page import="com.krawler.spring.authHandler.authHandlerDAO"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="com.krawler.hql.accounting.Payment"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.net.URLDecoder"%>

 <% 
 Connection conn=null;
 try{

     String serverip = request.getParameter("serverip");
     String port = "3306";
     String dbName = request.getParameter("dbname");
     String userName = request.getParameter("username");
     String password = request.getParameter("password");
     String subDomain = request.getParameter("subdomain");
     if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
         throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
     }
     String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
     String driver = "com.mysql.jdbc.Driver";

     Class.forName(driver).newInstance();
     conn = DriverManager.getConnection(connectString, userName, password);
     String selectQuery="";
     ResultSet rs;
     String uri = request.getRequestURL().toString();
     String servletBase = request.getServletPath();
     
     String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updatePaymentCurrencyToPaymentMethodCurrencyRate.do";     
     
     String companyId="";
     String subdomain="";
     String queryCompany = "";
     PreparedStatement ps=null;
     if(!StringUtil.isNullOrEmpty(subDomain)){
         queryCompany="select companyid, subdomain from company where subdomain=?";
         ps = conn.prepareStatement(queryCompany);
         ps.setString(1, subDomain);
         rs=ps.executeQuery();
     } else {
         queryCompany = "select companyid, subdomain from company";
         ps = conn.prepareStatement(queryCompany);
         rs=ps.executeQuery();
     }
     
     while(rs.next()){
         companyId = rs.getString("companyid");
         subdomain=rs.getString("subdomain");
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
                System.out.println("Completed for Company - "+rs.getString("subdomain"));
                out.println("Completed for Company - "+rs.getString("subdomain"));
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
     ps.close();
     rs.close();
 } catch(Exception e){
     e.printStackTrace();
     out.print(e.toString());
 } finally {
     if (conn != null) {
            conn.close();
        }
 }

 %>
