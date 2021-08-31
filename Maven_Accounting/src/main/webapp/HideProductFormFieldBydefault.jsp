<%@page import="java.util.UUID"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%

    Connection conn = null;
    try {

        String serverip = "localhost";
        String port = "3306";
        String dbName = "invaccnew";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,creator from company";
        PreparedStatement stmtquery = conn.prepareStatement(query);
        ResultSet rs = stmtquery.executeQuery();
        String[] dataIndex = {"hscode", "reusabilitycount", "licensetype", "licensecode"};
        String[] dataHeader = {"HS Code", "Re-usability Count", "License Type", "License Code"};
        int updateCount = 0;
        while (rs.next()) {
            for (int i = 0; i < dataIndex.length; i++) {
                String companId = rs.getString("companyid");
                boolean exist = false;
                String checkForExist = "select * from customizereportmapping where moduleId=34 and reportid=1 and isformfield=1  and  dataIndex='" + dataIndex[i] + "' and company=? ";
                PreparedStatement checkForExiststmtq = conn.prepareStatement(checkForExist);
                checkForExiststmtq.setString(1, rs.getString("companyid"));
                ResultSet rs1 = checkForExiststmtq.executeQuery();
                while (rs1.next()) {

                    exist = true;
                    checkForExiststmtq.close();
                    break;
                }

                if (exist) {
                    String query1 = "update customizereportmapping set hidden=1 where moduleId=34 and reportid=1 and isformfield=1  and  dataIndex='" + dataIndex[i] + "' and company=?";
                    PreparedStatement stmtquery1 = conn.prepareStatement(query1);
                    stmtquery1.setString(1, rs.getString("companyid"));
                    updateCount += stmtquery1.executeUpdate();
                    stmtquery1.close();
                } else {
                    String query1 = "INSERT INTO customizereportmapping (moduleid,reportid,hidden,dataheader,dataindex,company,isformfield,ismanadatoryfield,id,users) VALUES (?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmtquery1 = conn.prepareStatement(query1);
                    stmtquery1.setString(1, "34");
                    stmtquery1.setInt(2, 1);
                    stmtquery1.setInt(3, 1);
                    stmtquery1.setString(4, dataHeader[i]);
                    stmtquery1.setString(5, dataIndex[i]);
                    stmtquery1.setString(6, rs.getString("companyid"));
                    stmtquery1.setInt(7, 1);
                    stmtquery1.setInt(8, 0);
                    stmtquery1.setString(9, UUID.randomUUID().toString());
                    stmtquery1.setString(10, rs.getString("creator"));
                    updateCount += stmtquery1.executeUpdate();
                    stmtquery1.close();
                }
            }
        }
        stmtquery.close();
        out.println(updateCount + " Records has been updated");
        out.println("==============================================================================================================");
        out.println();

    } catch (Exception e) {
        out.println(e);
        if (conn != null) {
            conn.rollback();
        }
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>