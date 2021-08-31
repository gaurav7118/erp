

<%@page import="com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%

    Connection conn = null;
    try {
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
        PreparedStatement stmtquery;
        PreparedStatement stmtqueryCom;

        String customQuery = "";
        ResultSet entrySet = null;
        String query = "";
        /*
         * QUERY TO GET THE TAX ENTRY WITHOUT CUSTOM FIELDS
         */
        if (StringUtil.isNullOrEmpty(subDomain)) {
            query = "select companyid from company";
            stmtqueryCom = conn.prepareStatement(query);
        } else {
            query = "select companyname,subdomain,companyid from company where subdomain=?";
            stmtqueryCom = conn.prepareStatement(query);
            stmtqueryCom.setString(1, subDomain);
        }
        ResultSet comprs = stmtqueryCom.executeQuery();
        int count = 0;
        while (comprs.next()) {
            String companyId = comprs.getString("companyid");
            customQuery = "select invoicedetails.salesjedid,invoicedetails.gstjedid,invoicedetails.company "
                    + "from invoicedetails inner join accjedetailcustomdata on invoicedetails.salesjedid = accjedetailcustomdata.jedetailId and (invoicedetails.gstjedid  is not null "
                    + "and invoicedetails.gstjedid <> '' and (invoicedetails.gstjedid not in (select jedetailId from accjedetailcustomdata ))) "
                    + "where invoicedetails.company =?";

            stmtquery = conn.prepareStatement(customQuery);
            stmtquery.setString(1, companyId);

            entrySet = stmtquery.executeQuery();
            while (entrySet.next()) {
                /*
                 * INSERT CUSTOM FIELDS FOR EACH TAX ENTRY
                 */
                String salesjedid = entrySet.getString("salesjedid");
                String gstjedid = entrySet.getString("gstjedid");
                String companyid = entrySet.getString("company");
                PreparedStatement stmtquery0;
                String insertQuery = "insert into accjedetailcustomdata (jedetailId , col1 , col2 , col3 , col4 , col5 , col6 , col7 , "
                        + "col8 , col9 , col10 , col101 , col102 , col103 , col104 , col105 , col106 , col107 , col108 , col109 , col110 , col1001 , "
                        + "col1002 , col1003 , col1004 , col1005 , col1006 , col1007 , col1008 , col1009 , col1010 , col1011 , col1012 , col1013 , "
                        + "col1014 , col1015 , col1016 , col1017 , col1018 , col1019 , col1020 , col1021 , col1022 , col1023 , col1024 , col1025 , col1026 , "
                        + "col1027 , col1028 , col1029 , col1030 , col1031 , col1032 , col1033 , col1034 , col1035 , company, deleted , moduleId , col111 , col112 , "
                        + "col113 , col114 , col115 , col11 , col12 , col13 , col14 , col15 , col1036 , col1037 , col1038 , col1039 , col1040 , col1041 , col1042 , "
                        + "col1043 , col1044 , col1045 , recdetailId, col1046 , col1047 , col1048 , col1049 , col1050 , col1051 , col1052 , col1053 , col1054 , col1055 , "
                        + "col1056 , col1057 , col1058 , col1059 , col1060 , col2001 , col2002 , col2003 , col2004 , col2005 , col2006 , col2007 , col2008 , col2009 , col2010 , "
                        + "col16 , col17 , col18 , col19 , col20 , col21 , col22 , col23 , col24 , col25 , col26 , col27 , col28 , col29 , col30 , col31 , col32 , col33 , col34 , "
                        + "col35 , col36 , col37 , col38 , col39 , col40 , col41 , col42 , col43 , col44 , col45 , col46 , col47 , col48 , col49 , col50 ) "
                        + "select  ? as jedetailId, "
                        + "col1 , col2 , col3 , col4 , col5 , col6 , col7 , col8 , col9 , col10 , col101 , col102 , col103 , col104 , col105 , col106 , col107 , col108 , col109 , "
                        + "col110 , col1001 , col1002 , col1003 , col1004 , col1005 , col1006 , col1007 , col1008 , col1009 , col1010 , col1011 , col1012 , col1013 , col1014 , "
                        + "col1015 , col1016 , col1017 , col1018 , col1019 , col1020 , col1021 , col1022 , col1023 , col1024 , col1025 , col1026 , col1027 , "
                        + "col1028 , col1029 , col1030 , col1031 , col1032 , col1033 , col1034 , col1035 , company, deleted , moduleId , col111 , col112 , col113 , "
                        + "col114 , col115 , col11 , col12 , col13 , col14 , col15 , col1036 , col1037 , col1038 , col1039 , col1040 , col1041 , col1042 , col1043 , "
                        + "col1044 , col1045 , recdetailId, col1046 , col1047 , col1048 , col1049 , col1050 , col1051 , col1052 , col1053 , col1054 , col1055 , col1056 , "
                        + "col1057 , col1058 , col1059 , col1060 , col2001 , col2002 , col2003 , col2004 , col2005 , col2006 , col2007 , col2008 , col2009 , col2010 , col16 , "
                        + "col17 , col18 , col19 , col20 , col21 , col22 , col23 , col24 , col25 , col26 , col27 , col28 , col29 , col30 , col31 , col32 , col33 , col34 , col35 , "
                        + "col36 , col37 , col38 , col39 , col40 , col41 , col42 , col43 , col44 , col45 , col46 , col47 , col48 , col49  , col50 "
                        + "from accjedetailcustomdata where jedetailId = ?";
                stmtquery0 = conn.prepareStatement(insertQuery);
                stmtquery0.setString(1, gstjedid);
                stmtquery0.setString(2, salesjedid);
                count += stmtquery0.executeUpdate();
                // Insert The reverse JE id ref  
                PreparedStatement stmtquery1;
                String updateQuery = "update jedetail set accjedetailcustomdataref=? where id =? and company = ?";
                stmtquery1 = conn.prepareStatement(updateQuery);
                stmtquery1.setString(1, gstjedid);
                stmtquery1.setString(2, gstjedid);
                stmtquery1.setString(3, companyid);
                stmtquery1.executeUpdate();

            }
        }
        out.print("<b>Updated " + count + " Rows.<br>Exceuted the script sucessfully</b>");
    } catch (MySQLSyntaxErrorException e) {
        e.printStackTrace();
        out.print("<br>" + e.toString() + "<br><br> Please execute proper DB changes first");
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>