<%-- 
    Document   : DeleteTaxFromIndianCompany
    Created on : 18 Jan, 2018, 11:35:37 AM
    Author     : Suhas C
--%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%
    Connection conn = null;
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
    String fialResultStatus = "Final Result : <br/>";
    try {
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        String companyQuery = "select c.companyid, c.subdomain , c.country from company c inner join extracompanypreferences ecf on c.companyid=ecf.id where (isnewgst='T' or isnewgst = 1) and ecf.lineleveltermflag=1  and c.country=105 ";

        if (!StringUtil.isNullOrEmpty(subdomain)) {
            companyQuery += "  and c.subdomain= '" + subdomain + "' ";
        }
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement(companyQuery);
        ResultSet rscompany = pstmt.executeQuery();
        while (rscompany.next()) {
            String companyid = rscompany.getString("companyid");
            String companyDomain = rscompany.getString("Subdomain");
            String country = rscompany.getString("country");
            /**
             * update data From Transaction tables
             */
            fialResultStatus += "<br/>===============Started Execution for : " + companyDomain + "=====================<br/>";

            String foreignkeyresult = "SELECT TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME "
                    + "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME = 'tax'";
            PreparedStatement fstmt = null;
            fstmt = conn.prepareStatement(foreignkeyresult);
            ResultSet foreignkeyresultset = fstmt.executeQuery();
            while (foreignkeyresultset.next()) {
                String tablename = foreignkeyresultset.getString("TABLE_NAME");
                String columnname = foreignkeyresultset.getString("COLUMN_NAME");
                String updatetraquery = " Update " + tablename + " set " + columnname + "=NULL where " + columnname + " in "
                        + "( select id from tax where company= '" + companyid + "')";
                PreparedStatement updatestmt = conn.prepareStatement(updatetraquery);
                int deletedRecords = updatestmt.executeUpdate();
                if (deletedRecords > 0) {
                    fialResultStatus += " Execution Done for  : Updated " + columnname + " column of " + tablename + " ( " + deletedRecords + ") <br/>";
                }
            }
            int deletedRecords = 0;
            // delete taxList table Data
            try {
                pstmt = conn.prepareStatement(" delete from taxlist where company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();
                fialResultStatus += " Execution Done for  : taxlist ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : taxlist ( " + e.getMessage() + ") <br/>";
            }

            //****************************************** End Update Tax column **************************************
            // set taxid for vendor table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" update vendor set taxid=null where  company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();
                fialResultStatus += " Execution Done for  : vendor ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : vendor ( " + e.getMessage() + ") <br/>";
            }

            // set taxid for vendor table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" update customer set taxid=null where  company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();
                fialResultStatus += " Execution Done for  : customer ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : customer ( " + e.getMessage() + ") <br/>";
            }

            // delete taxtermsmapping table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from taxtermsmapping where tax in (select id from tax where company= '" + companyid + "' );");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : taxtermsmapping ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : taxtermsmapping ( " + e.getMessage() + ") <br/>";
            }

            // delete tax table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from tax where company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : tax ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : tax ( " + e.getMessage() + ") <br/>";
            }

            fialResultStatus += "<br/>===============End Execution for : " + companyDomain + "========================<br/>";
            //----------------------------------------
        }
        // delete defaultterms table Data
        int deletedRecords = 0;

        // delete defaultgst table Data
        deletedRecords = 0;
        try {
            pstmt = conn.prepareStatement(" delete from defaultgst where country=105  ; ");
            deletedRecords = pstmt.executeUpdate();

            fialResultStatus += " Execution Done for  : defaultgst ( " + deletedRecords + ") <br/>";
        } catch (Exception e) {
            fialResultStatus += " Falied Execution for  : defaultgst ( " + e.getMessage() + ") <br/>";
        }
    } catch (Exception ex) {
        fialResultStatus += ex.getMessage();
    } finally {
        out.println(fialResultStatus);
        conn.close();
    }
%>
