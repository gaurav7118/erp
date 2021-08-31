
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
    public void decodeDescription(String tablename, String id, String desc, Connection conn) {  //Recursive Method
        String originalVal = "", decodedVal = "";
        int success = 0;
        try {
            originalVal = desc;
            decodedVal = URLDecoder.decode(originalVal, "UTF-8");
            if (originalVal.equalsIgnoreCase(decodedVal)) {
                //Show Transaction Number
                return;
            } else {
                try {
                    String updateQuery = "UPDATE " + tablename + " SET description=?  where id=?";
                    PreparedStatement statement1 = conn.prepareStatement(updateQuery);
                    statement1.setString(1, decodedVal);
                    statement1.setString(2, id);
                    success = statement1.executeUpdate();
                    decodeDescription(tablename, id, decodedVal, conn);
                } catch (SQLException se) {
                    //Database Connection Problem
                }
            }
        } catch (UnsupportedEncodingException ue) {
            System.out.println("OS do not support to UTF-8 format.");
        } catch (IllegalArgumentException ie) {
            System.out.println("IllegalArgumentException : Description cannot decode more !!");
            return;
        }
    }
%>
<%
    Connection conn = null;
    try {
        // VENDOR INVOICE & CASH PURCHASE
        //SCRIPT URL : http://<app-url>/PurchaseOrderDescUpdate.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is an optional param in URL 

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String id1 = "", id2 = "", desc1 = "", desc2 = "", txnnumber = "", compsubdomain = "", companyid = "", compName = "", querycashInv = "", querycashExpense = "";
        String companyQry = "";
        int updatecount = 0, failedcount = 0;
        PreparedStatement compstmt = null;
        ResultSet compresult = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            companyQry = "SELECT companyid, companyname FROM company where subdomain=?";
            compstmt = conn.prepareStatement(companyQry);
            compstmt.setString(1, subdomain);
            compresult = compstmt.executeQuery();
            while (compresult.next()) {
                companyid = compresult.getString("companyid");
                compName = compresult.getString("companyname");
                querycashInv = "SELECT pd.id, pd.description, po.ponumber from podetails pd INNER JOIN purchaseorder po ON pd.purchaseorder=po.id WHERE pd.company='" + companyid + "' AND pd.description IS NOT NULL ORDER BY po.ponumber";
                out.println("<center><b>Company  : " + compName + "</b></center>\n\n\n");
                ps1 = conn.prepareStatement(querycashInv);
                rs1 = ps1.executeQuery();
%>
<div align="center">
    <table border="1" cellpadding="5">
        <caption><h2>List of Updated Records</h2></caption>
        <tr><th>Subdomain</th><th>PO Number</th><th>Description</th></tr>
        <%
            while (rs1.next()) {
                id1 = rs1.getString("id");
                desc1 = rs1.getString("description");
                compsubdomain = subdomain;
                txnnumber = rs1.getString("ponumber");
                try {
                    decodeDescription("podetails", id1, desc1, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(txnnumber);%></td>
            <td><% out.println(desc1);%></td>
        </tr>
        <%
                    updatecount++;

                } catch (Exception ex) {
                    failedcount++;      //ex.getMessage();
                }
            }
        %>            
    </table>
</div> 
<%
    }
} else {
    companyQry = "SELECT companyid, companyname, subdomain FROM company ORDER BY subdomain";
    compstmt = conn.prepareStatement(companyQry);
    compresult = compstmt.executeQuery();
    while (compresult.next()) {
        companyid = compresult.getString("companyid");
        compName = compresult.getString("companyname");
        subdomain = compresult.getString("subdomain");
        querycashInv = "SELECT pd.id, pd.description, po.ponumber from podetails pd INNER JOIN purchaseorder po ON pd.purchaseorder=po.id WHERE pd.description IS NOT NULL ORDER BY po.ponumber";
        ps1 = conn.prepareStatement(querycashInv);
        rs1 = ps1.executeQuery();
%>
<div align="center">
    <table border="1" cellpadding="5">
        <caption><h2>List of Updated Records</h2></caption>
        <tr><th>Subdomain</th><th>PO Number</th><th>Description</th></tr>
        <%
            while (rs1.next()) {
                id1 = rs1.getString("id");
                desc1 = rs1.getString("description");
                if (!StringUtil.isNullOrEmpty(subdomain)) {
                    compsubdomain = subdomain;
                }
                txnnumber = rs1.getString("ponumber");
                try {
                    decodeDescription("podetails", id1, desc1, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(txnnumber);%></td>
            <td><% out.println(desc1);%></td>
        </tr>
        <%
                    updatecount++;
                } catch (Exception ex) {
                    failedcount++;  //ex.getMessage();
                }
            }
        %>            
    </table>
</div>  
<%          }//while
        }//else
        out.println("\n\n\n\n\n\n");
        out.println("<center><b>No. of records are updated : " + updatecount + "</b></center>");
        out.println("<center><b>No. of records are failed to update : " + failedcount + "</b></center>");

        rs1.close();
        ps1.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>