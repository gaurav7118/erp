<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
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
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");

            // For Sales Bad Debt Claim

            int bscUpdateCounter = 0;
            int bscAddCounter = 0;


            query = "select id from sequenceformat where moduleid=86 and deleted='F' and isdefaultformat='T' and company=? "; //Credit Note moduleid is 12
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            String sequnceformatid = "";
            while (rs1.next()) {
                sequnceformatid = rs1.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { 

                query = "select id from sequenceformat where moduleid=86 and deleted='F' and company=? ";

                PreparedStatement stmt2 = conn.prepareStatement(query);
                stmt2.setString(1, companyId);
                ResultSet rs2 = stmt2.executeQuery();
                sequnceformatid = "";
                if (rs2.next()) {
                    sequnceformatid = rs2.getString("id");

                    // Update Ist Sequence format and set it to default Sequence Format

                    query = "update sequenceformat set isdefaultformat='T' where id=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, sequnceformatid);
                    stmt3.executeUpdate();
                    bscUpdateCounter++;
                    out.println("<br><br> Bad Debt Sales Claim Record successfully <b>updated</b> for " + companyname + " : " + bscUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "BSC000000");
                    stmt4.setString(3, "BSC");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autosalesbaddebtclaimid");
                    stmt4.setInt(10, 86);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    bscAddCounter++;
                    out.println("<br><br> Bad Debt Sales Claim Record successfully <b>Added</b> for " + companyname + " : " + bscAddCounter);
                }

            }



            // For Sales Bad Debt Recover

            int bsrUpdateCounter = 0;
            int bsrAddCounter = 0;

            query = "select id from sequenceformat where moduleid=85 and deleted='F' and isdefaultformat='T' and company=? "; //Debit Note moduleid is 10
            PreparedStatement stmt5 = conn.prepareStatement(query);
            stmt5.setString(1, companyId);
            ResultSet rs5 = stmt5.executeQuery();
            sequnceformatid = "";
            while (rs5.next()) {
                sequnceformatid = rs5.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { 

                query = "select id from sequenceformat where moduleid=85 and deleted='F' and company=? ";

                PreparedStatement stmt2 = conn.prepareStatement(query);
                stmt2.setString(1, companyId);
                ResultSet rs2 = stmt2.executeQuery();
                sequnceformatid = "";
                if (rs2.next()) {
                    sequnceformatid = rs2.getString("id");

                    // Update Ist Sequence format and set it to default Sequence Format

                    query = "update sequenceformat set isdefaultformat='T' where id=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, sequnceformatid);
                    stmt3.executeUpdate();
                    bsrUpdateCounter++;
                    out.println("<br><br> Bad Debt Sales Recover Record successfully <b>updated</b> for " + companyname + " : " + bsrUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "BSR000000");
                    stmt4.setString(3, "BSR");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autosalesbaddebtrecoverid");
                    stmt4.setInt(10, 85);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    bsrAddCounter++;
                    out.println("<br><br> Bad Debt Sales Recover Record successfully <b>Added</b> for " + companyname + " : " + bsrAddCounter);
                }
            }
            
             // For Bad Debt Purchase Claim

            int BPCUpdateCounter = 0;
            int BPCAddCounter = 0;

            query = "select id from sequenceformat where moduleid=83 and deleted='F' and isdefaultformat='T' and company=? "; //Debit Note moduleid is 10
            PreparedStatement stmt6 = conn.prepareStatement(query);
            stmt6.setString(1, companyId);
            ResultSet rs6 = stmt6.executeQuery();
            sequnceformatid = "";
            while (rs6.next()) {
                sequnceformatid = rs6.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { //If JE000000 format not exists then create same format

                query = "select id from sequenceformat where moduleid=83 and deleted='F' and company=? ";

                PreparedStatement stmt2 = conn.prepareStatement(query);
                stmt2.setString(1, companyId);
                ResultSet rs2 = stmt2.executeQuery();
                sequnceformatid = "";
                if (rs2.next()) {
                    sequnceformatid = rs2.getString("id");

                    // Update Ist Sequence format and set it to default Sequence Format

                    query = "update sequenceformat set isdefaultformat='T' where id=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, sequnceformatid);
                    stmt3.executeUpdate();
                    BPCUpdateCounter++;
                    out.println("<br><br> Bad Debt Purchase Claim Record successfully <b>updated</b> for " + companyname + " : " + BPCUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "BPC000000");
                    stmt4.setString(3, "BPC");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autopurchasebaddebtclaimid");
                    stmt4.setInt(10, 83);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    BPCAddCounter++;
                    out.println("<br><br> Bad Debt Purchase Claim Record successfully <b>Added</b> for " + companyname + " : " + BPCAddCounter);
                }
            }
             // For Bad Debt Purchase Claim

            int BPRUpdateCounter = 0;
            int BPRAddCounter = 0;

            query = "select id from sequenceformat where moduleid=84 and deleted='F' and isdefaultformat='T' and company=? "; //Debit Note moduleid is 10
            PreparedStatement stmt7 = conn.prepareStatement(query);
            stmt7.setString(1, companyId);
            ResultSet rs7 = stmt7.executeQuery();
            sequnceformatid = "";
            while (rs7.next()) {
                sequnceformatid = rs7.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { 

                query = "select id from sequenceformat where moduleid=84 and deleted='F' and company=? ";

                PreparedStatement stmt2 = conn.prepareStatement(query);
                stmt2.setString(1, companyId);
                ResultSet rs2 = stmt2.executeQuery();
                sequnceformatid = "";
                if (rs2.next()) {
                    sequnceformatid = rs2.getString("id");

                    // Update Ist Sequence format and set it to default Sequence Format

                    query = "update sequenceformat set isdefaultformat='T' where id=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, sequnceformatid);
                    stmt3.executeUpdate();
                    BPRUpdateCounter++;
                    out.println("<br><br> Bad Debt Purchase Recover Record successfully <b>updated</b> for " + companyname + " : " + BPRUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "BPR000000");
                    stmt4.setString(3, "BPR");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autopurchasebaddebtrecoverid");
                    stmt4.setInt(10, 84);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    BPRAddCounter++;
                    out.println("<br><br> Bad Debt Purchase Recover Record successfully <b>Added</b> for " + companyname + " : " + BPRAddCounter);
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
