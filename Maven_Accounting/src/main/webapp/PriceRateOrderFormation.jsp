
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
<%@page import="java.math.BigInteger"%>

<%!
%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/PriceRateOrderFormation.jsp?serverip=?&dbname=?&username=?&password=?
        // This script ll be execute for all domain & ll set exchange order for all 

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String driver = "com.mysql.jdbc.Driver";
        String addColumn = "", query1 = "", query3 = "", query4 = "", query5 = "";
        int updatecount = 0, resetcount = 0;
        long ordercounter = 1;
        PreparedStatement pst = null, pst1 = null, pst3 = null, pst4 = null, pst5 = null, modifypst=null;
        ResultSet rst1 = null, rst4 = null;
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
        //Query to add Price order column :
        try {
            addColumn = "ALTER TABLE pricelist ADD priceorder BIGINT NOT NULL AUTO_INCREMENT UNIQUE";
            pst = conn.prepareStatement(addColumn);
            pst.execute();
        } catch (Exception se) {
            System.out.println("Priceorder column is not added to pricelist. Either column is exist or something is missing.\n");
            out.println("Priceorder column is not added to pricelist. Either column is exist or something is missing.<br>");
            se.getMessage();
        }
        System.out.println("Priceorder column added to pricelist.\n");
        out.println("Priceorder column added to pricelist.<br>");
        //Get Max Order number
        query1 = "SELECT max(priceorder) AS MaxPriceOrder FROM pricelist";
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        long maxOrder = 0L;
        if (rst1.next()) {
            maxOrder = rst1.getInt("MaxPriceOrder");
            System.out.println("Max Price Order : " + maxOrder + "\n");
            out.println("Max Price Order : " + maxOrder + "<br>");
        }
        out.println("<b><center>Reset Price Order Started......</center><b><br>");
        out.println("<b>------------------------------------------------------------------------</center><b><br>");
        try {   //Reset Exchange Order Count Loop
            query3 = "UPDATE pricelist SET priceorder=(priceorder + ?)";
            pst3 = conn.prepareStatement(query3);
            pst3.setLong(1, maxOrder);
            resetcount = pst3.executeUpdate();
            if (resetcount > 0) {
                //System.out.println("Price Order updated.\n");
                //out.println("Price Order updated.<br>");
            } else {
                //System.out.println("Price Order not updated.\n");
                //out.println("Price Order not updated.<br>");
            }
        } catch (Exception e) {
            System.out.println("Exception occurred in Reset Price Order Count Loop.\n");
        }
//            }//while rst2
        out.println("Reset Price Order Ended......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");

        query4 = "SELECT id FROM pricelist ORDER BY applydate";
        pst4 = conn.prepareStatement(query4);
        rst4 = pst4.executeQuery();
        out.println("Update Price Order While Loop Started......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");
        while (rst4.next()) {
            try {    //Update Exchange Order Count Loop
                String id = rst4.getString("id");
                query5 = "UPDATE pricelist SET priceorder='" + ordercounter + "' WHERE id='" + id + "'";
                pst5 = conn.prepareStatement(query5);
                updatecount = pst5.executeUpdate();
                if (updatecount > 0) {
                    System.out.println("New Exchange Order :" + ordercounter + "\n");
                    //out.println("<center>New Exchange Order :" + ordercounter +"</center><br>");
                } else {
                    System.out.println("New Exchange Order is not applied.\n");
                }
                ordercounter++;
            } catch (Exception ex) {
                System.out.println("Exception occurred in Update Price Order Count Loop");
                ex.printStackTrace();
            }
        }//while rst4
        out.println("Update Price Order While Loop Ended......<br>");
        out.println("<center>------------------------------------------------------------------------</center><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println("Exception occurred : " + ex.getMessage());
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>