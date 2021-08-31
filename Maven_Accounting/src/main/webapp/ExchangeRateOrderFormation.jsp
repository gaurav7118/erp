
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

<%!
%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/UpdateExchangeRateOrderForOldData.jsp?serverip=?&dbname=?&username=?&password=?
        // This script ll be execute for all domain & ll set exchange order for all 

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        //subdomain = "fastenhardware"; //Applicable only 'FastenHardware
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String companyid = "", appDate = "";
        String addColumn = "", query1 = "", query3 = "", query4 = "", query5 = "";
        int ordercounter = 1, updatecount = 0, resetcount = 0;
        PreparedStatement pst = null, pst1 = null, pst3 = null, pst4 = null, pst5 = null, modifypst=null;
        ResultSet rst1 = null, rst4 = null;
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
        //Query to add Exchange order column :
        try {
            addColumn = "ALTER TABLE exchangeratedetails ADD exchangeorder BIGINT NOT NULL AUTO_INCREMENT UNIQUE";
            pst = conn.prepareStatement(addColumn);
            pst.execute();
        } catch (SQLException se) {
            System.out.println("Exchangeorder column is not added to exchangeratedetails. Either column is exist or something is missing.\n");
            out.println("Exchangeorder column is not added to exchangeratedetails. Either column is exist or something is missing.<br>");
            se.getMessage();
        }
//        if (result) {
        System.out.println("Exchangeorder column added to exchangeratedetails.\n");
        out.println("Exchangeorder column added to exchangeratedetails.<br>");
        //Get Max Order number
        query1 = "SELECT max(exchangeorder) AS MaxExchangeOrder FROM exchangeratedetails";
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        long maxOrder = 0L;
        if (rst1.next()) {
            maxOrder = rst1.getInt("MaxExchangeOrder");
            System.out.println("Max Exchange Order : " + maxOrder + "\n");
            out.println("Max Exchange Order : " + maxOrder + "<br>");
        }

        //query2 = "SELECT id, exchangeorder FROM exchangeratedetails ORDER BY exchangeorder";
        //pst2 = conn.prepareStatement(query2);
        //rst2 = pst2.executeQuery();
        out.println("Reset Exchange Order Started......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");
        // while(rst2.next()){
        try {   //Reset Exchange Order Count Loop
            //String id = rst2.getString("id");
            //int exchord = rst2.getInt("exchangeorder");
            query3 = "UPDATE exchangeratedetails SET exchangeorder=(exchangeorder + ?)";
            pst3 = conn.prepareStatement(query3);
            pst3.setLong(1, maxOrder);
            resetcount = pst3.executeUpdate();
            if (resetcount > 0) {
                System.out.println("Exchange Order updated.\n");
                out.println("Exchange Order updated.<br>");
            } else {
                System.out.println("Exchange Order not updated.\n");
                out.println("Exchange Order not updated.<br>");
            }
        } catch (Exception e) {
            System.out.println("Exception occurred in Reset Exchange Order Count Loop.\n");
        }
//            }//while rst2
        out.println("Reset Exchange Order Ended......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");

        query4 = "SELECT id FROM exchangeratedetails ORDER BY applydate";
        pst4 = conn.prepareStatement(query4);
        rst4 = pst4.executeQuery();
        out.println("Update Exchange Order While Loop Started......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");
        while (rst4.next()) {
            try {    //Update Exchange Order Count Loop
                String id = rst4.getString("id");
                query5 = "UPDATE exchangeratedetails SET exchangeorder='" + ordercounter + "' WHERE id='" + id + "'";
                pst5 = conn.prepareStatement(query5);
                updatecount = pst5.executeUpdate();
                if (updatecount > 0) {
                    System.out.println("New Exchange Order :" + ordercounter + "\n");
                } else {
                    System.out.println("New Exchange Order is not applied.\n");
                }
                ordercounter++;
            } catch (Exception ex) {
                System.out.println("Exception occurred in Update Exchange Order Count Loop");
            }
        }//while rst4
        out.println("Update Exchange Order While Loop Ended......<br>");
        out.println("-----------------------------------------------------------------------------------------------------------------<br>");
        /*
         * } else { System.out.println("Exchangeorder column is not added to
         * exchangeratedetails. Either column is exist or something is
         * missing.\n"); out.println("Exchangeorder column is not added to
         * exchangeratedetails. Either column is exist or something is
         * missing.<br>"); }//if
         */
        //Add isupdated column to Inventory table to identify which record is updated. This is to avoide multiple time updation of update date.
        modifypst = null;
        String addInventoryUpdateCol = "ALTER TABLE inventory ADD isupdated char(1) DEFAULT '0'";
        try {
            modifypst = conn.prepareStatement(addInventoryUpdateCol);
            modifypst.execute();
            System.out.println("'isupdated' column added to Inventory Table.\n");
            out.println("'isupdated' column added to Inventory Table.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("'isupdated' column is not added to Inventory Table.<br>");
        }
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