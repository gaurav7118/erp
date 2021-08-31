<%@page import="java.util.Calendar"%>
<%@page import="java.util.TimeZone"%>
<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    Connection conn = null;
    int updatecnt = 0;
    try {

        String serverip = request.getParameter("serverip");//"192.168.0.225";                            
        String port = "3306";
        String dbName = request.getParameter("dbname");//"staginginvaccnew";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = "fasten";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        DateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy");
        originalFormat.setLenient(false);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        targetFormat.setLenient(false);
        String companyId = "9226a574-3777-4d26-ab1c-e4c4a44f3ee5";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream invoice = new FileInputStream("/home/krawler/ERP_FASTEN_OPENING_ CUSTOMER _INVOICES.csv");
        BufferedReader in = new BufferedReader(new InputStreamReader(invoice));
        int cnt = 0;
        Calendar dueDate = null;
        boolean flag;
        String record = "";%>
<table width="99%" border="1">
    <TR>
        <TD>SR Number</TD>          
        <TD>Transaction Number</TD>
        <TD>Current Transaction Date(yyyy/mm/dd)</TD>
        <TD>Current Due Date(yyyy/mm/dd)</TD>
        <TD>Updated Current Due Date(yyyy/mm/dd)</TD>
        <TD>Updated Transaction Date(yyyy/mm/dd)</TD>
        <TD>Trem</TD>
        <TD>Status</TD>

    </TR>
    <%
        while ((record = in.readLine()) != null) {
            if (cnt == 0 && record != "") {
                //there is nothing to take this are headers                
            }
            if (cnt != 0 && !StringUtil.isNullOrEmpty(record)) {
                //System.out.println(record);
                String[] rec = record.split(",");
                String transactionno = rec[0].trim();
                String transactiondate = rec[1].trim();
                String query3 = " select invoicenumber,creationdate,duedate,termid from  invoice where invoicenumber=? and creationdate=? and company=?";
                PreparedStatement stmt3 = conn.prepareStatement(query3);
                stmt3.setObject(1, transactionno.replace("\"", ""));
                java.util.Date tranDate = originalFormat.parse(transactiondate.replace("\"", ""));
                stmt3.setObject(2, targetFormat.format(tranDate));
                stmt3.setObject(3, companyId);
                ResultSet rs = stmt3.executeQuery();
                if (rs.next()) {
    %>
    <TR>
        <TD><%=cnt%></TD>   
        <TD><%=rs.getString("invoicenumber")%></TD>
        <TD><%=rs.getDate("creationdate")%></TD>
        <TD><%=rs.getDate("duedate")%></TD>
        <TD><%=rs.getDate("duedate")%></TD>
        <TD><%=targetFormat1.format(tranDate)%></TD>
        <TD><%=rs.getDate("termid")%></TD>
        <TD>Already Set</TD>

    </TR><%
        //out.println("update record"+transactionno);
    } else {
        int termdays = 0;
        String query4 = " select invoicenumber,creationdate,duedate,termid from  invoice where invoicenumber=? and company=?";
        PreparedStatement stmt2 = conn.prepareStatement(query4);
        stmt2.setObject(1, transactionno.replace("\"", ""));
        stmt2.setObject(2, companyId);
        ResultSet rs1 = stmt2.executeQuery();
        String query2 = "select termdays from creditterm ct,customer cust where cust.creditTerm=ct.termid and cust.name=? and cust.company=?";
        PreparedStatement stmt5 = conn.prepareStatement(query2);
        stmt5.setObject(1, (rec[8].trim()).replace("\"", ""));
        stmt5.setObject(2, companyId);
        ResultSet rs5 = stmt5.executeQuery();
        if (rs5.next()) {
            termdays = rs5.getInt("termdays");
        }
        if (rs1.next()) {
            dueDate = dueDate.getInstance();  
            dueDate.setTime(tranDate);
            dueDate.add(Calendar.DATE, termdays);

    %>

    <TR>
        <TD><%=cnt%></TD>
        <TD><%=rs1.getString("invoicenumber")%></TD>
        <TD><%=rs1.getDate("creationdate")%></TD>
        <TD><%=targetFormat1.format(rs1.getDate("duedate"))%></TD>
        <TD><%=targetFormat1.format(dueDate.getTime())%></TD>
        <TD><%=targetFormat1.format(tranDate)%></TD>
        <TD><%=termdays%></TD>
        <TD>Need to Update</TD>

    </TR>
    <%
                        String query5 = " update invoice set creationdate=? ,duedate=? ,porefdate=? where invoicenumber=? and company=?";
                        PreparedStatement stmt4 = conn.prepareStatement(query5);
                        stmt4.setObject(1, targetFormat1.format(tranDate));
                        stmt4.setObject(2, targetFormat1.format(dueDate.getTime()));
                        stmt4.setObject(3, targetFormat1.format(tranDate));
                        stmt4.setObject(4, transactionno.replace("\"", ""));
                        stmt4.setObject(5, companyId);
                        int count1 = stmt4.executeUpdate();
                        updatecnt++;
                    }

                }

            }
            cnt++;
        }
//out.println("no of update record"+updatecnt);
    %>
</table><%
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
        out.println("no of records updated successfully =" + updatecnt);
    }

%>
