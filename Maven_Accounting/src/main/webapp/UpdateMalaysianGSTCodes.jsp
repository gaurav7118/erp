<%@page import="com.krawler.common.util.StringUtil"%>

<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = "3306";//request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company where country='137'";
        //if (!StringUtil.isNullOrEmpty(subdomain)) {
        //      query += " where subdomain= ?";
        //  }
        PreparedStatement stmt = conn.prepareStatement(query);
        //if (!StringUtil.isNullOrEmpty(subdomain)) {
        //   stmt.setString(1, subdomain);
        //}
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");

            out.println("<br><br> Updating For Company <b>" + companyname + "</b>");

            List<String> taxList = new ArrayList<String>();
            taxList.add("TX");
            taxList.add("IM");
            taxList.add("IS");
            taxList.add("BL");
            taxList.add("NR");
            taxList.add("ZP");
            taxList.add("EP");
            taxList.add("OP");
            taxList.add("TX-E43");
            taxList.add("TX-N43");
            taxList.add("TX-RE");
            taxList.add("GP");
            taxList.add("AJP");
            taxList.add("SR");
            taxList.add("ZRL");
            taxList.add("ZRE");
            taxList.add("ES43");
            taxList.add("DS");
            taxList.add("OS");
            taxList.add("ES");
            taxList.add("AJS");
            taxList.add("GS");
            taxList.add("RS");

            for (String taxCode : taxList) {

                query = "SELECT id FROM tax tx WHERE tx.taxcode=? AND company=?";
                PreparedStatement stmt1 = conn.prepareStatement(query);
                stmt1.setString(1, taxCode);
                stmt1.setString(2, companyId);
                ResultSet rs1 = stmt1.executeQuery();
                String taxId = "";
                while (rs1.next()) {
                    taxId += rs1.getString("id") + ",";
                }

                if (!StringUtil.isNullOrEmpty(taxId)) {
                    taxId = taxId.substring(0, taxId.length() - 1);

                    String updateQuery = "UPDATE tax SET taxcode=?, name=? WHERE id IN (?)";
                    PreparedStatement stmt2 = conn.prepareStatement(updateQuery);
                    stmt2.setString(1, "GST(" + taxCode + ")");
                    stmt2.setString(2, "GST(" + taxCode + ")");
                    stmt2.setString(3, taxId);
                    int updatedRow = stmt2.executeUpdate();
                    out.println("<br><br>"+updatedRow+" Tax rows Updated for Company "+companyname);
                }

                // Updating Account Name

                query = "SELECT id FROM account WHERE `name`=? AND company=?";
                stmt1 = conn.prepareStatement(query);
                stmt1.setString(1, taxCode);
                stmt1.setString(2, companyId);
                rs1 = stmt1.executeQuery();
                String accountId = "";
                while (rs1.next()) {
                    accountId += rs1.getString("id") + ",";
                }

                if (!StringUtil.isNullOrEmpty(accountId)) {
                    accountId = accountId.substring(0, accountId.length() - 1);

                    String updateQuery = "UPDATE account SET `name`=? WHERE id IN (?)";
                    PreparedStatement stmt2 = conn.prepareStatement(updateQuery);
                    stmt2.setString(1, "GST(" + taxCode + ")");
                    stmt2.setString(2, accountId);
                    int updatedRow = stmt2.executeUpdate();

                    out.println("<br><br>"+updatedRow+" Account rows Updated for Company "+companyname);
                }
            }

            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>