
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
        // For Local Testing Purpose
//        String serverip = "localhost";
//        String dbName = "accounting_15072014";
//        String userName = "krawler";
//        String password = "krawler";
                      
        String serverip = request.getParameter("serverip");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;

        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String receiptid = "", description= "";
        int success = 0, updatecount = 0, failedcount = 0;
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String querycash = "SELECT id, description FROM receiptdetails WHERE description IS NOT NULL";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            receiptid = rscash.getString("id");
            description = rscash.getString("description");
            try {
                description = URLDecoder.decode(description, "UTF-8");
                String updatepayment = "UPDATE receiptdetails SET description=?  where id=?";
                PreparedStatement statement1 = conn.prepareStatement(updatepayment);
                statement1.setString(1, description);
                statement1.setString(2, receiptid);
                success = statement1.executeUpdate();
                if(success>0)
                    updatecount++;
            } catch (Exception ex) {
                failedcount++;
//                System.out.println("FAILED TO DECODE : "+description);
//                ex.getMessage();
            }
        }//while
        out.println("\n\n\n");
        out.println("<center><b>No. of records are updated : "+updatecount+"</b></center>");
        out.println("<center><b>No. of records are failed to update : "+failedcount+"</b></center>");
        
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
