
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%--<%@page import="java.sql.Date"%>--%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.apache.commons.dbcp.BasicDataSource" %>
<body>
    <h3>Check Data Base Connection </h3>    
    <%!
    %>
    <%
        Connection con = null;
        try {
            
            String serverip = request.getParameter("serverip");
            String dbname = request.getParameter("dbname");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String tablename = request.getParameter("tablename");
            String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)|| StringUtil.isNullOrEmpty(tablename)) {//|| StringUtil.isNullOrEmpty(password)
                throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password,tablename) in url. so please provide all these parameters correctly. ");
            }
        
        try {
            //SCRIPT URL : http://<app-url>/DBConnectionTest.jsp?serverip=?&dbname=?&username=?&password=?&tablename=?

            

            String driver = "com.mysql.jdbc.Driver";
            String query1 = "";
            int companyCount = 0;

            PreparedStatement pst1 = null;
            ResultSet rst1 = null;
            Class.forName(driver).newInstance();
            //Execution Started :
            System.out.println("Execution Started @ " + new java.util.Date());
            out.println("<b>Execution Started @ " + new java.util.Date() + "<br></b>");
            System.out.println("Executing Test Scenario 1 : Establishing Simple JDBC connection from Data Base");
            out.println("<br><b>Executing Test Scenario 1 : Establishing Simple JDBC connection from Data Base" + "</b><br>");
            con = DriverManager.getConnection(connectString, username, password);
            if (con != null) {
                System.out.println("Successfully Established Simple JDBC connection from Data Base");
                out.println("Successfully Established Simple JDBC connection from Data Base" + "<br>");
            }
            query1 = "SELECT count(*) as count FROM "+ tablename;
            pst1 = con.prepareStatement(query1);
            System.out.println("Execute Query through Simple JDBC connection from Data Base");
            out.println("Execute Query through Simple JDBC connection from Data Base" + "<br>");
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companyCount = rst1.getInt("count");
                String comp = "<font color=\"green\">Total "+ tablename  + " Record Count through Simple JDBC connection from Data Base = " + companyCount + "</font>";
                System.out.println(comp + "\n");
                out.println(comp + "\n");
            }

            System.out.println("\n Executing Test Scenario 1 : Executed Successfully\n");
            out.println("<br>Executing Test Scenario 1 : Executed Successfully");
            out.println("<br/>");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.print("Executing Test Scenario 1 : Exception occured : ");
            out.print(ex.toString());
        } finally {
            if (con != null) {
                try {
                    con.close();
                    System.out.println("Successfully Closed connection through Simple JDBC connection from Data Base");
                    out.println("Successfully Closed connection through Simple JDBC connection from Data Base" + "<br>");
                    out.println("<b>Executing Test Scenario 1 : Ended @ " + new java.util.Date() + "</b><br><br>");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }//finally
    %>


    <%
        try {
            System.out.println("Executing Test Scenario 2 : Establishing connection from Application Resource Base DAO");
            out.println("<b><br>Executing Test Scenario 2 : Establishing connection from Application Resource Base DAO" + "<br></b>");
            System.out.println("Loading Application Context");
            out.println("Loading Application Context");
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
            if (context != null) {
                System.out.println("Application Context Loaded Successfully");
                out.println("<br>Application Context Loaded Successfully");
                System.out.println("Loading BaseDAO Bean from Application Context");
                out.println("<br>Loading BaseDAO Bean from Application Context");
                com.krawler.common.dao.BaseDAO baseDao = (com.krawler.common.dao.BaseDAO) context.getBean("baseDAO");
                if (baseDao != null) {
                    System.out.println("BaseDAO Bean from Application Context Loaded Successfully");
                    out.println("<br>BaseDAO Bean from Application Context Loaded Successfully");
                }
                tablename = request.getParameter("tablename");
                String query1= "SELECT count(*) as count FROM "+ tablename;
                System.out.println("Execute Query through BaseDAO Bean ");
                out.println("<br>Execute Query through BaseDAO Bean" + "<br>");
                List countList = baseDao.executeSQLQuery(query1);
                if (countList != null) {
                    System.out.println("Query Executed Successfully through BaseDAO Bean");
                    out.println("Query Executed Successfully through BaseDAO Bean" + "<br>");
                    String comp = "<font color=\"green\">Total "+ tablename +" Record Count through BaseDAO = " + countList.get(0) + "</font>";
                    out.print(comp + "<br>");

                }
                out.println("<b>Executing Test Scenario 2 : Ended @ " + new java.util.Date() + "<br><br></b>");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            out.print("Executing Test Scenario 2 : Exception occured : ");
            out.print(ex.toString());
        } finally {            
        }
        System.out.println("Execution Ended @ " + new java.util.Date());
        out.println("<b>Execution Ended @ " + new java.util.Date() + "<br></b>");
    } catch (Exception ex) {
            ex.printStackTrace();            
            out.print(ex.toString());
        }
    %>

</body>