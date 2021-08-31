
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
        String dbName =request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;

        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String jeid = "", description= "";
        int success = 0, updatecount = 0, failedcount=0;
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String querycash = "SELECT id, description FROM receiptdetailotherwise WHERE description IS NOT NULL";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            jeid = rscash.getString("id");
            description = rscash.getString("description");
            try {
                description = URLDecoder.decode(description, "UTF-8");
                String updatepayment = "UPDATE receiptdetailotherwise SET description=?  where id=?";
                PreparedStatement statement1 = conn.prepareStatement(updatepayment);
                statement1.setString(1, description);
                statement1.setString(2, jeid);
                success = statement1.executeUpdate();
                if(success>0)
                    updatecount++;
            } catch (Exception ex) {
                failedcount++;
//                System.out.println("FAILED TO DECODE : "+description);
//                ex.getMessage();
            }
        }//while
        out.println("<center><br>No. of receiptdetailotherwise records are updated : "+updatecount+"</b></center>");
        out.println("<center><br>No. of receiptdetailotherwise records are failed to update : "+failedcount+"</b></center>");
        
        updatecount=0;
        failedcount=0;
        
        querycash = "SELECT id, description FROM paymentdetailotherwise WHERE description IS NOT NULL";
        stmtcash = conn.prepareStatement(querycash);
        
        rscash = stmtcash.executeQuery();
        
        while (rscash.next()) {
            jeid = rscash.getString("id");
            description = rscash.getString("description");
            try {
                description = URLDecoder.decode(description, "UTF-8");
                String updatepayment = "UPDATE paymentdetailotherwise SET description=?  where id=?";
                PreparedStatement statement1 = conn.prepareStatement(updatepayment);
                statement1.setString(1, description);
                statement1.setString(2, jeid);
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
        out.println("<center><br>No. of paymentdetailotherwise records are updated : "+updatecount+"</b></center>");
        out.println("<center><br>No. of paymentdetailotherwise records are failed to update : "+failedcount+"</b></center>");
        
        rscash.close();
        stmtcash.close();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>