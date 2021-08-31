
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
        String serverip = "localhost";
        String dbName = "swt_05022015";
        String userName = "krawler";
        String password = "krawler";
        String subdomain = "swt";
        boolean sendmail = false;
        int module = 0;// 0 - CI, 1 - VI
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":3306/";

        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        // To recalculate Gainloss for Invoice Payments
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/findForeignGainLossForOldPayments.do";
        
        // to recalculate gainloss for CN/DN
//         String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/findForeignGainLossForCreditNote.do";
        String driver = "com.mysql.jdbc.Driver";
        String companyId = "";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String querycash = "select companyid, subdomain from company where subdomain='"+subdomain+"'";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            companyId = rscash.getString("companyid");
            InputStream iStream = null;
            try {
                URL u = new URL(requestedURL);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.print("cid=" + companyId + "&module="+module+ "&cdomain="+subdomain);
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                String res = URLDecoder.decode(in.readLine());
                System.out.println("Completed for Company - "+rscash.getString("subdomain"));
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

        stmtcash.close();
        rscash.close();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
