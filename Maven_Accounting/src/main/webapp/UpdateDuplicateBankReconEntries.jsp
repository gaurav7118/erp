<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.sql.Date"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        String driver = "com.mysql.jdbc.Driver";

        //http://localhost:8084/Accounting/UpdateDuplicateBankReconEntries.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
        String companyId = "";
        String companyName = "";
        int recordsno = 0;
        String query = "";
        ResultSet rs;

        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query = "SELECT companyid,subdomain FROM company where subdomain= ? ORDER BY companyid ASC ";
            PreparedStatement stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subdomain);
            rs = stmtquery.executeQuery();
        } else {
            query = "SELECT companyid,subdomain FROM company ORDER BY companyid ASC ";
            PreparedStatement stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        while (rs.next()) {
            companyId = rs.getString("companyid");
            companyName = rs.getString("subdomain");
            int updateCount = 0;

            String brdID = "", isDebit = "", name = "", journalEntry = "", bankReconID = "", brDetailQuery = "";
            java.sql.Date reconciledate = null;
            double amount = 0.0;
            int rowcount = 0;

            brDetailQuery = " SELECT brd.debit,sum(brd.amount) AS amount, brd.name, brd.journalentry, brd.bankReconciliation, brd.reconciledate FROM bankreconciliationdetail brd INNER JOIN journalentry je ON je.id=brd.journalentry WHERE brd.company='" + companyId + "' GROUP BY brd.bankReconciliation, brd.journalentry HAVING COUNT(*) > 1 ORDER BY brd.journalentry ";
            PreparedStatement stmt2 = conn.prepareStatement(brDetailQuery);
            ResultSet resultset2 = stmt2.executeQuery();
            while (resultset2.next()) {
                isDebit = resultset2.getString("debit");
                amount = resultset2.getDouble("amount");
                name = resultset2.getString("name") != null ? resultset2.getString("name") : "";
                journalEntry = resultset2.getString("journalEntry") != null ? resultset2.getString("journalEntry") : null;
                bankReconID = resultset2.getString("bankReconciliation");
                reconciledate = resultset2.getDate("reconciledate") != null ? resultset2.getDate("reconciledate") : null;

                String deleteRowQuery = "DELETE FROM bankreconciliationdetail WHERE journalentry='" + journalEntry + "' AND bankReconciliation='" + bankReconID + "' AND company='" + companyId + "'";
                PreparedStatement stmt3 = conn.prepareStatement(deleteRowQuery);
                stmt3.executeUpdate();

                try {
                    //Insert New Record for deleted data with new UUID
                    String newRecordQuery = "INSERT INTO  bankreconciliationdetail (id ,debit, amount, name, journalEntry, bankReconciliation, company,reconciledate) "
                            + " VALUES (UUID(),?,?,?,?,?,?,?)";
                    PreparedStatement stmt4 = conn.prepareStatement(newRecordQuery);
                    stmt4.setString(1, isDebit);
                    stmt4.setDouble(2, amount);
                    stmt4.setString(3, name);
                    stmt4.setString(4, journalEntry);
                    stmt4.setString(5, bankReconID);
                    stmt4.setString(6, companyId);
                    stmt4.setDate(7, reconciledate);
                    stmt4.executeUpdate();
                    updateCount++;
                } catch (SQLException se) {
                    System.out.println("\nException Occured in INSERT Query.\n");
                    se.printStackTrace();
                }
            }
            out.println(updateCount + " Records updated for " + companyName);
        }//outer while

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>