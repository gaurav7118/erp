<%-- 
    Document   : ScriptForAddingMissingTDSRate
    Created on : 17 Apr, 2018, 10:44:55 AM
    Author     : krawler
--%>

<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.IndiaComplianceConstants"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%  Connection conn = null;
    int accountNotFound = 0;
    int accountupdated = 0;
    int totalMasterItemFound = 0;
    try {
        String serverip = request.getParameter("serverip");//"IP";                            
        String port = request.getParameter("port");//"Port";
        String dbName = request.getParameter("dbname");//"Database name";
        String userName = request.getParameter("username");//"username";
        String password = request.getParameter("password"); //"password"

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" Please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        conn.setAutoCommit(false);

        // Update Default Master table masterGroup
        String changedefaultMasterTypeQuery = "UPDATE defaultmasteritem SET masterGroup=33 WHERE code='194J' and id='41' and  value='Person engaged only in the business of operation of call center'";
        PreparedStatement stmtdefaultChangeMasterType = conn.prepareStatement(changedefaultMasterTypeQuery);
        stmtdefaultChangeMasterType.executeUpdate();
        conn.commit();
        stmtdefaultChangeMasterType.close();

        // Update Master table masterGroup
        String changeMasterTypeQuery = "UPDATE masteritem SET masterGroup =33 WHERE code='194J' and defaultmasteritem='41' and  value='Person engaged only in the business of operation of call center' and masterGroup =34";
        PreparedStatement stmtChangeMasterType = conn.prepareStatement(changeMasterTypeQuery);
        stmtChangeMasterType.executeUpdate();
        conn.commit();
        stmtChangeMasterType.close();

        // Update Master table masterGroup
        String changeMasterTypeDefaultAccountQuery = "UPDATE defaultmasteritem set defaultaccid = '5795cdf1-df89-24e5-9b8a-24dda97935fc' WHERE code='194J' and  value='Person engaged only in the business of operation of call center' and masterGroup =33  and id='41'";
        PreparedStatement stmtdefaultAccountType = conn.prepareStatement(changeMasterTypeDefaultAccountQuery);
        stmtdefaultAccountType.executeUpdate();
        conn.commit();
        stmtdefaultAccountType.close();

        String masterItemQuery = "SELECT * from masteritem INNER JOIN company on company.companyid = masteritem.company  where masteritem.defaultmasteritem = '41'  and  value='Person engaged only in the business of operation of call center' and masterGroup =33 and accid IS NULL AND company.country = '105'";
        PreparedStatement masterItemStmt = conn.prepareStatement(masterItemQuery);
        ResultSet masterItemResults = masterItemStmt.executeQuery();
        while (masterItemResults.next()) {
            String companyid = masterItemResults.getString("company");
            String accountQuery = "SELECT id from account WHERE account.defaultaccountid  = '5795cdf1-df89-24e5-9b8a-24dda97935fc' AND name LIKE 'TDS Payable%' AND company='" + companyid + "'";
            PreparedStatement accountStmt = conn.prepareStatement(accountQuery);
            ResultSet accountResults = accountStmt.executeQuery();
            if (accountResults.next()) {
                String id = accountResults.getString("id");
                String updateDefaultAccountQuery = "UPDATE masteritem set accid = '" + id + "' WHERE code='194J' and  value='Person engaged only in the business of operation of call center' and masterGroup =33  and defaultmasteritem='41' AND company='" + companyid + "' and accid IS NULL";
                PreparedStatement stmtUpdateAccountType = conn.prepareStatement(updateDefaultAccountQuery);
                stmtUpdateAccountType.executeUpdate();
                conn.commit();
                stmtUpdateAccountType.close();
                accountupdated++;
            } else {
                accountNotFound++;
            }
            accountStmt.close();
            totalMasterItemFound++;
        }
        masterItemStmt.close();
        out.println("<b>Script Executed Successfully</b> </br></br>");
    } catch (Exception e) {
        if (conn != null) {
            conn.rollback();
        }
        e.printStackTrace();
        out.println("<b>Error while executing script</b> </br></br>");
        out.println("<b>Exception :</b> " + e.toString() + "</br></br>");
        out.println("<b>Exception Message:</b> " + e.getMessage() + "</br></br><br><br>");

    } finally {
        out.println("<b> Total Master Item for 194J : </b>" + totalMasterItemFound + " <br><br>");
        out.println("<b> Payable Account not Found for 194J : </b>" + accountNotFound + " <br><br>");
        out.println("<b> Update Payable Account : </b>" + accountupdated);
    }

%>
