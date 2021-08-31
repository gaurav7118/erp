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
<%      Connection conn = null;
    int totalCompanyScan = 0;
    int foundMissingTDSCompany = 0;
    int UpdateTDSRateCompany = 0;
    String missingDeducteeType="";
    String updatedTDSRateSubdomain="";
    int missingDeducteeTypeCount=0;
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
        String defaultTDSQuery = " SELECT * from default_tds_rate ";
        JSONObject jobj = new JSONObject();
        PreparedStatement defaultTDSstmt = conn.prepareStatement(defaultTDSQuery);
        String companyQuery = "SELECT companyid,subdomain,tds_rate.natureofpayment from company  INNER JOIN extracompanypreferences on company.companyid = extracompanypreferences.id LEFT JOIN tds_rate on  tds_rate.company = company.companyid "
                            + "where country=105 AND company.deleteflag=0 AND activated='T' AND tds_rate.id IS NULL";
        PreparedStatement stmt = conn.prepareStatement(companyQuery);
        ResultSet companyResults = stmt.executeQuery();
        while (companyResults.next()) {
            totalCompanyScan++;
            String companyid = companyResults.getString("companyid");
            String subdomain = companyResults.getString("subdomain");
            String tdsQuery = "Select * from tds_rate where company=?";
            PreparedStatement tdsStmt = conn.prepareStatement(tdsQuery);
            tdsStmt.setString(1, companyid);
            ResultSet tdsResults = tdsStmt.executeQuery();
            if (!tdsResults.next()) {
                foundMissingTDSCompany++;
                jobj = new JSONObject();
                String masterItemDeducteeTyoe = "SELECT id, defaultmasteritem,company from masteritem where mastergroup = ? AND company=?";
                PreparedStatement masterItemDeducteeStmt = conn.prepareStatement(masterItemDeducteeTyoe);
                masterItemDeducteeStmt.setString(1, IndiaComplianceConstants.DEDUCTEE_MASTERGROUP);
                masterItemDeducteeStmt.setString(2, companyid);
                ResultSet masterItemDeducteeResults = masterItemDeducteeStmt.executeQuery();
                while (masterItemDeducteeResults.next()) {
                    if (!StringUtil.isNullOrEmpty(masterItemDeducteeResults.getString("defaultmasteritem"))) {
                        jobj.put(masterItemDeducteeResults.getString("defaultmasteritem"), masterItemDeducteeResults.getString("id"));
                    }
                }
                masterItemDeducteeStmt.close();
                ResultSet tdsMasterData = defaultTDSstmt.executeQuery();
                if (jobj.length() > 9) {
                    boolean update = false;
                    while (tdsMasterData.next()) {
                            if (jobj.has(tdsMasterData.getString("defaultMasterdeducteetype"))) {
                                String updateQuery = "INSERT INTO tds_rate (natureofpayment ,deducteetype,residentialstatus,fromdate,todate,"
                                        + "rate,basicexemptionpertransaction,basicexemptionperannum,company) values (?,?,?,?,?,?,?,?,?)";
                                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                                updateStmt.setString(1, tdsMasterData.getString("natureofpayment"));
                                updateStmt.setString(2, jobj.getString(tdsMasterData.getString("defaultMasterdeducteetype")));
                                updateStmt.setString(3, tdsMasterData.getString("residentialstatus"));
                                updateStmt.setDate(4, tdsMasterData.getDate("fromdate"));
                                updateStmt.setDate(5, tdsMasterData.getDate("todate"));
                                updateStmt.setDouble(6, tdsMasterData.getDouble("rate"));
                                updateStmt.setDouble(7, tdsMasterData.getDouble("basicexemptionpertransaction"));
                                updateStmt.setDouble(8, tdsMasterData.getDouble("basicexemptionperannum"));
                                updateStmt.setString(9, companyid);
                                updateStmt.executeUpdate();
                                updateStmt.close();
                                update = true;
                            }
                        }
                    conn.commit();
                    if (update) {
                        updatedTDSRateSubdomain += subdomain + ", ";
                        UpdateTDSRateCompany++;
                    }
                }else{
                    missingDeducteeTypeCount++;
                    missingDeducteeType += subdomain+", ";
                }
            }
            tdsStmt.close();
        }
        defaultTDSstmt.close();
        stmt.close();
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
        out.println("<b> Total Company Scaned : </b>" + totalCompanyScan + " <br><br>");
        out.println("<b> Total Company Found with Missing TDS : </b>" + foundMissingTDSCompany + " <br><br>");
        out.println("<b> Missing deductee Type : </b> Count :" +missingDeducteeTypeCount+" | Subdomains :"+ missingDeducteeType + " <br><br>");
        out.println("<b> Updated Subdomains : </b>:" +updatedTDSRateSubdomain + " <br><br>");
        out.println("<b> Total Company Updated : </b>" + UpdateTDSRateCompany);
    }

%>
