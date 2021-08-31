<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {
        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";

        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            PreparedStatement compstmt = null;
            String[] tableNameArray = {"accjecustomdata", "accjedetailcustomdata", "accjedetailproductcustomdata", "accountcustomdata", "vendorcustomdata", "customercustomdata", "purchaseordercustomdata", "salesordercustomdata", "contractcustomdata", "purchasereturncustomdata", "purchaserequisitioncustomdata", "rfqcustomdata", "salesreturncustomdata", "deliveryordercustomdata", "grordercustomdata", "quotationcustomdata", "vendorquotationcustomdata", "openingbalanceinvoicecustomdata", "openingbalancevendorinvoicecustomdata", "openingbalancedebitnotecustomdata", "openingbalancecreditnotecustomdata", "openingbalancemakepaymentcustomdata", "openingbalancereceiptcustomdata"};
            String[] tableColumnNameArray = {"col1", "col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9", "col10", "col101", "col102", "col103", "col104", "col105", "col106", "col107", "col108", "col109", "col110", "col1001", "col1002", "col1003", "col1004", "col1005", "col1006", "col1007", "col1008", "col1009", "col1010", "col1011", "col1012", "col1013", "col1014", "col1015", "col1016", "col1017", "col1018", "col1019", "col1020", "col1021", "col1022", "col1023", "col1024", "col1025", "col1026", "col1027", "col1028", "col1029", "col1030", "col1031", "col1032", "col1033", "col1034", "col1035", "col111", "col112", "col113", "col114", "col115", "col11", "col12", "col13", "col14", "col15", "col1036", "col1037", "col1038", "col1039", "col1040", "col1041", "col1042", "col1043", "col1044", "col1045"};
            for (int i = 0; i < tableNameArray.length; i++) {
                for (int j = 0; j < tableColumnNameArray.length; j++) {
                    String query = "update " + tableNameArray[i] + " set " + tableColumnNameArray[j] + "=? where " + tableColumnNameArray[j]+" =?";
                    PreparedStatement stmt5 = conn.prepareStatement(query);
                    stmt5.setString(1, null);
                    stmt5.setString(2, "defval");
                    stmt5.execute();
                }
                out.println("Completed " + tableNameArray[i] + " table");
                out.println("\n");
            }



        }
        if (conn != null) {
            conn.close();//finally release connection   
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
