
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
                    System.out.println("Database Connection Failed. Check Database Parameters");
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
        //SCRIPT URL : http://<app-url>/PurchaseInvoiceDescUpdate.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        String id1 = "", id2 = "", desc1 = "", desc2 = "", grnumber1 = "", grnumber2 = "", compsubdomain = "", companyid = "", compName = "", querycashInv = "", querycashExpense = "";
        String companyQry = "";
        int updatecount = 0, failedcount = 0;
        PreparedStatement compstmt = null;
        ResultSet compresult = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        
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
                
            
            //Query ll fetch data based on provided URL
            querycashInv = "SELECT gd.id, gd.description, gr.grnumber from grdetails gd INNER JOIN goodsreceipt gr ON gd.goodsreceipt=gr.id WHERE gd.company='" + companyid + "' AND gd.description IS NOT NULL ORDER BY gr.grnumber";
            querycashExpense = "SELECT gd.id, gd.description, gr.grnumber from expenseggrdetails gd INNER JOIN goodsreceipt gr ON gd.goodsreceipt=gr.id WHERE gd.company='" + companyid + "' AND gd.description IS NOT NULL ORDER BY gr.grnumber";
            out.println("<center><b>Company  : " + compName + "</b></center>\n\n\n");
            ps1 = conn.prepareStatement(querycashInv);
            rs1 = ps1.executeQuery();
            ps2 = conn.prepareStatement(querycashExpense);
            rs2 = ps2.executeQuery();
%>
<div align="center">
    <table border="1" cellpadding="5">
        <caption><h2>List of Updated Records</h2></caption>
        <tr><th>Subdomain</th><th>PI Number</th><th>Description</th></tr>
        <%
            while (rs1.next()) {    //For Inventory Type Transaction
                id1 = rs1.getString("id");
                desc1 = rs1.getString("description");
                if (!StringUtil.isNullOrEmpty(subdomain)) {
                    compsubdomain = subdomain;
                }
                grnumber1 = rs1.getString("grnumber");
                try {
                    decodeDescription("grdetails", id1, desc1, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(grnumber1);%></td>
            <td><% out.println(desc1);%></td>
        </tr>
        <%
                    //out.println("<center><b>" + compsubdomain + " \t\t\t\t -- " + grnumber1 + " -- \t\t\t\t " + desc1 + "</b></center>");
                    updatecount++;

                } catch (Exception ex) {
                    failedcount++;
        //                System.out.println("FAILED TO DECODE : "+description);
        //                ex.getMessage();
                }
            }//Inventory while
            while (rs2.next()) {    //For Expense Type Transaction
                id2 = rs2.getString("id");
                desc2 = rs2.getString("description");
                if (!StringUtil.isNullOrEmpty(subdomain)) {
                    compsubdomain = subdomain;
                }
                grnumber2 = rs2.getString("grnumber");
                try {
                    decodeDescription("expenseggrdetails", id2, desc2, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(grnumber2);%></td>
            <td><% out.println(desc2);%></td>
        </tr>
        <%
                    //out.println("<center><b>" + compsubdomain + " \t\t\t\t -- " + grnumber2 + " -- \t\t\t\t " + desc2 + "</b></center>");
                    updatecount++;

                } catch (Exception ex) {
                    failedcount++;
//                System.out.println("FAILED TO DECODE : "+description);
//                ex.getMessage();
                }
            }//Expense while

        %>            
    </table>
</div> 
<%
            }
        } else {    //If subdomain is not provided
            companyQry = "SELECT companyid, companyname, subdomain FROM company ORDER BY subdomain";
            compstmt = conn.prepareStatement(companyQry);
            compresult = compstmt.executeQuery();
            while (compresult.next()) {
                companyid = compresult.getString("companyid");
                compName = compresult.getString("companyname");
                subdomain= compresult.getString("subdomain");
            querycashInv = "SELECT gd.id, gd.description, gr.grnumber from grdetails gd INNER JOIN goodsreceipt gr ON gd.goodsreceipt=gr.id WHERE gd.description IS NOT NULL ORDER BY gr.grnumber";
            querycashExpense = "SELECT gd.id, gd.description, gr.grnumber from expenseggrdetails gd INNER JOIN goodsreceipt gr ON gd.goodsreceipt=gr.id WHERE gd.description IS NOT NULL ORDER BY gr.grnumber";
            ps1 = conn.prepareStatement(querycashInv);
            rs1 = ps1.executeQuery();
            ps2 = conn.prepareStatement(querycashExpense);
            rs2 = ps2.executeQuery();
%>
<div align="center">
    <table border="1" cellpadding="5">
        <caption><h2>List of Updated Records</h2></caption>
        <tr><th>Subdomain</th><th>PI Number</th><th>Description</th></tr>
        <%
            while (rs1.next()) {    //For Inventory Type Transaction
                id1 = rs1.getString("id");
                desc1 = rs1.getString("description");
                if (!StringUtil.isNullOrEmpty(subdomain)) {
                    compsubdomain = subdomain;
                }
                grnumber1 = rs1.getString("grnumber");
                try {
                    decodeDescription("grdetails", id1, desc1, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(grnumber1);%></td>
            <td><% out.println(desc1);%></td>
        </tr>
        <%
                    updatecount++;
                } catch (Exception ex) {
                    failedcount++;  //ex.getMessage();
                }
            }//Inventory while
            while (rs2.next()) {    //For Expense Type Transaction
                id2 = rs2.getString("id");
                desc2 = rs2.getString("description");
                if (!StringUtil.isNullOrEmpty(subdomain)) {
                    compsubdomain = subdomain;
                }
                grnumber2 = rs2.getString("grnumber");
                try {
                    decodeDescription("expenseggrdetails", id2, desc2, conn);
        %>
        <tr>
            <td><% out.println(compsubdomain);%></td>
            <td><% out.println(grnumber2);%></td>
            <td><% out.println(desc2);%></td>
        </tr>
        <%
                    updatecount++;
                } catch (Exception ex) {
                    failedcount++; //ex.getMessage();
                }
            }//Expense while
        %>            
    </table>
</div>  
<%          }//while
        }//else
        out.println("\n\n\n\n\n\n");
        out.println("<center><b>No. of records are updated : " + updatecount + "</b></center>");
        out.println("<center><b>No. of records are failed to update : " + failedcount + "</b></center>");

        rs1.close();
        rs2.close();
        ps1.close();
        ps2.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>