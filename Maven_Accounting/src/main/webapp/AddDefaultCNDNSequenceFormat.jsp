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

            // For Credit Note

            int cnUpdateCounter = 0;
            int cnAddCounter = 0;

            query = "select id from sequenceformat where moduleid=12 and deleted='F' and isdefaultformat='T' and company=? "; //Credit Note moduleid is 12
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            String sequnceformatid = "";
            while (rs1.next()) {
                sequnceformatid = rs1.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { //If JE000000 format not exists then create same format

                query = "select id from sequenceformat where moduleid=12 and deleted='F' and company=? ";

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
                    cnUpdateCounter++;
                    out.println("<br><br> Credit Note Record successfully <b>updated</b> for " + companyname + " : " + cnUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "CN000000");
                    stmt4.setString(3, "CN");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autocreditmemo");
                    stmt4.setInt(10, 12);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    cnAddCounter++;
                    out.println("<br><br> Credit Note Record successfully <b>Added</b> for " + companyname + " : " + cnAddCounter);
                }

            }



            // For Debit Note

            int dnUpdateCounter = 0;
            int dnAddCounter = 0;

            query = "select id from sequenceformat where moduleid=10 and deleted='F' and isdefaultformat='T' and company=? "; //Debit Note moduleid is 10
            PreparedStatement stmt5 = conn.prepareStatement(query);
            stmt5.setString(1, companyId);
            ResultSet rs5 = stmt5.executeQuery();
            sequnceformatid = "";
            while (rs5.next()) {
                sequnceformatid = rs5.getString("id");
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { //If JE000000 format not exists then create same format

                query = "select id from sequenceformat where moduleid=10 and deleted='F' and company=? ";

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
                    dnUpdateCounter++;
                    out.println("<br><br> Debit Note Record successfully <b>updated</b> for " + companyname + " : " + dnUpdateCounter);
                } else {
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt4 = conn.prepareStatement(query);
                    stmt4.setString(1, uuid);
                    stmt4.setString(2, "DN000000");
                    stmt4.setString(3, "DN");
                    stmt4.setString(4, "");
                    stmt4.setInt(5, 6);
                    stmt4.setInt(6, 1);
                    stmt4.setString(7, "F");
                    stmt4.setString(8, "T");
                    stmt4.setString(9, "autodebitnote");
                    stmt4.setInt(10, 10);
                    stmt4.setString(11, companyId);
                    stmt4.setString(12, "T");
                    stmt4.execute();
                    dnAddCounter++;
                    out.println("<br><br> Debit Note Record successfully <b>Added</b> for " + companyname + " : " + dnAddCounter);
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