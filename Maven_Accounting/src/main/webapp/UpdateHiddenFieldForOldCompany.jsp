<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {
        //URL : localhost:8084/Accounting/UpdateCustomizeReportMappingForOldCompany.jsp?serverip=localhost&dbname=staging_20012016&username=krawlersqladmin&password=Krawler[X]&subdomain=""
        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            String query = "";
            PreparedStatement compstmt = null;
            PreparedStatement headerpst = null;
            PreparedStatement mapperpst = null;
            Statement st = null;
            if (StringUtil.isNullOrEmpty(subdomain)) {
                query = "select companyname,subdomain,companyid from company";
                compstmt = conn.prepareStatement(query);
            } else {
                query = "select companyname,subdomain,companyid from company where subdomain=?";
                compstmt = conn.prepareStatement(query);
                compstmt.setString(1, subdomain);
            }
            ResultSet comprs = compstmt.executeQuery();
            while (comprs.next()) {
                String companyId = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubdomain = comprs.getString("subdomain");
                out.println("</br>Company Name : <b>" + companyname + "</b> and Subdomain: <b>" + compSubdomain + "</b>");

                String headerquery = "SELECT id, moduleid FROM customizereportheader WHERE dataIndex='permit' AND reportid=1 "
                        + "AND isformfield=1 AND islinefield=1 ORDER BY moduleid ASC";
                headerpst = conn.prepareStatement(headerquery);
                ResultSet headerrst = headerpst.executeQuery();
                int updatecount = 0;

                while (headerrst.next()) {
                    String headerid = headerrst.getString("id") != null ? headerrst.getString("id") : "";
                    int moduleid = headerrst.getInt("moduleid");
                    //To avoid duplicate enrty
                    String qry = "SELECT id FROM customizereportmapping WHERE dataIndex='permit' AND moduleid='" + moduleid + "' AND customizereportheader='" + headerid + "' AND company='" + companyId + "'";
                    st = conn.createStatement();
                    ResultSet rs = st.executeQuery(qry);
                    if (rs.next()) {
                        out.println("</br><b>" + companyname + "</b> is already updated.");
                    } else {
                        //Insert query for CustomizeReportMapping
                        if (headerid != null && !headerid.isEmpty()) {
                            String mapQuery = "INSERT INTO customizereportmapping (id,reportid,hidden,dataheader,dataIndex,isformfield,isreadonlyfield,islinefield, isusermanadatoryfield, customizereportheader, moduleid, company) VALUES(UUID(), 1, 1, 'Permit No.', 'permit', 1, 0, 1, 0, ?, ?, ?)";
                            mapperpst = conn.prepareStatement(mapQuery);
                            mapperpst.setString(1, headerid);
                            mapperpst.setInt(2, moduleid);
                            mapperpst.setString(3, companyId);
                            int count = mapperpst.executeUpdate();
                            updatecount = updatecount + count;
                        }
                    }
                }
            }//company-while
            out.println("</br><b>Script execution is completed. Thanks!");
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>