

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    String message = "updated records are =";
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        PreparedStatement updatestmt;
        String query = "";
        ResultSet rs;
        ResultSet custrs;
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query = "select id,paymentnumber,paymentwindowtype,customer,vendor,paidto,payee from payment where company in (select companyid from company where subdomain='" + subdomain + "')";
        } else {
            query = "select id,paymentnumber,paymentwindowtype,customer,vendor,paidto,payee from payment";
        }
        stmtquery = conn.prepareStatement(query);
        rs = stmtquery.executeQuery();
        while (rs.next()) {
            int paymentwindowtype = rs.getInt("paymentwindowtype");
            String paymentid = rs.getString("id");
            String paymentnumber = rs.getString("paymentnumber");
            String customer = rs.getString("customer");
            String vendor = rs.getString("vendor");
            String paidto = rs.getString("paidto");
            String payee = rs.getString("payee");
            String updatequery = "";
            if (StringUtil.isNullOrEmpty(payee) && !StringUtil.isNullOrEmpty(paymentid)) {
                if (paymentwindowtype == 1) {
                    String dataquery = "select name from vendor where id=?";
                    PreparedStatement datastmt;
                    datastmt = conn.prepareStatement(dataquery);
                    datastmt.setString(1, vendor);
                    ResultSet rs1 = datastmt.executeQuery();
                    String name = "";
                    while (rs1.next()) {
                        name = rs1.getString("name");
                    }
                    updatequery = "update payment set payee=? where paymentwindowtype=? and id=?";
                    updatestmt = conn.prepareStatement(updatequery);
                    updatestmt.setString(1, name);
                    updatestmt.setInt(2, 1);
                    updatestmt.setString(3, paymentid);
                    int no = updatestmt.executeUpdate();
                } else if (paymentwindowtype == 2) {
                    String dataquery = "select name from customer where id=?";
                    PreparedStatement datastmt;
                    datastmt = conn.prepareStatement(dataquery);
                    datastmt.setString(1, customer);
                    ResultSet rs1 = datastmt.executeQuery();
                    String name = "";
                    while (rs1.next()) {
                        name = rs1.getString("name");
                    }
                    updatequery = "update payment set payee=? where paymentwindowtype=? and id=?";
                    updatestmt = conn.prepareStatement(updatequery);
                    updatestmt.setString(1, name);
                    updatestmt.setInt(2, 2);
                    updatestmt.setString(3, paymentid);
                    int no = updatestmt.executeUpdate();
                } else {
                    String dataquery = "select value from masteritem where id=?";
                    PreparedStatement datastmt;
                    datastmt = conn.prepareStatement(dataquery);
                    datastmt.setString(1, paidto);
                    ResultSet rs1 = datastmt.executeQuery();
                    String name = "";
                    while (rs1.next()) {
                        name = rs1.getString("value");
                    }
                    updatequery = "update payment set payee=? where paymentwindowtype=? and id=?";
                    updatestmt = conn.prepareStatement(updatequery);
                    updatestmt.setString(1, name);
                    updatestmt.setInt(2, 3);
                    updatestmt.setString(3, paymentid);
                    int no = updatestmt.executeUpdate();
                }
                message += paymentnumber + " ,";
            }
        }
        System.out.println("data updated for following records");

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print(message);
    }
%>