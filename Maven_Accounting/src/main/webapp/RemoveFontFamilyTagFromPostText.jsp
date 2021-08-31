
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
    Connection conn = null;
    try {

               String serverip = request.getParameter("serverip");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":3306/"; 
        String driver = "com.mysql.jdbc.Driver";
        
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query1 = "select id,posttext from quotation where company=(select companyid from company where subdomain=?)";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        stmt1.setString(1, subdomain);
        ResultSet rs1 = stmt1.executeQuery();
        int count1=0;
        while (rs1.next()) {
            count1++;
            String id="";
            String posttext="";
            String newposttext="";
            id = rs1.getString("id");
            posttext = rs1.getString("posttext");
            String expression="style=\".+?\"";
            Pattern p = Pattern.compile(expression);
            String[] items = p.split(posttext);
            for (String s : items) {
                newposttext+=s;
            }
            String updatequery1 = "update quotation set posttext = ? where id = ?";
            PreparedStatement updatestmt1 = conn.prepareStatement(updatequery1);
            updatestmt1.setString(1, newposttext);
            updatestmt1.setString(2, id);
            updatestmt1.executeUpdate();           
        }
        out.println("<center><br>No. of Quotation Records are updated :<b>"+count1+"</b></center>");
        stmt1.close();
        rs1.close();
        
        String query2 = "select id,posttext from purchaseorder where company=(select companyid from company where subdomain=?)";
        PreparedStatement stmt2 = conn.prepareStatement(query2);
        stmt2.setString(1, subdomain);
        ResultSet rs2 = stmt2.executeQuery();
        int count2=0;
        while (rs2.next()) {
            count2++;
            String id="";
            String posttext="";
            String newposttext="";
            id = rs2.getString("id");
            posttext = rs2.getString("posttext");
            String expression="style=\".+?\"";
            Pattern p = Pattern.compile(expression);
            
            String[] items = p.split(posttext);
            for (String s : items) {
                newposttext+=s;
            }
            
            String updatequery2 = "update purchaseorder set posttext = ? where id = ?";
            PreparedStatement updatestmt2 = conn.prepareStatement(updatequery2);
            updatestmt2.setString(1, newposttext);
            updatestmt2.setString(2, id);
            updatestmt2.executeUpdate();           
        }
        out.println("<center><br>No. of Puchase Order Records are updated :<b>"+count2+"</b></center>");
        stmt2.close();
        rs2.close();
        
        
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
