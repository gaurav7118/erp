<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

<%@page import="java.util.UUID"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

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

            int migratedSeqformat = 0;
            String query = "";
            PreparedStatement compstmt = null;
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
                String companyid = comprs.getString("companyid");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    String selectSeqFormatQuery = "select DISTINCT(prd.seqformat),sf.name,sf.prefix,sf.suffix,sf.numberofdigit,sf.startfrom,sf.showleadingzero,sf.isdatebeforeprefix,sf.dateformatinprefix,sf.showdateformataftersuffix,sf.dateformataftersuffix "
                            + "from product prd "
                            + "inner join sequenceformat sf on sf.id=prd.seqformat "
                            + "where prd.company=? and prd.isasset='1' and prd.seqformat is not null and sf.moduleid=?";

                    PreparedStatement seqstmt = conn.prepareStatement(selectSeqFormatQuery);
                    seqstmt.setString(1, companyid);
                    seqstmt.setInt(2, 30);
                    ResultSet seqrs = seqstmt.executeQuery();
                    while (seqrs.next()) {
                        String formatID = seqrs.getString("seqformat");
                        if (!StringUtil.isNullOrEmpty(formatID)) {
                            String name = seqrs.getString("name");
                            String prefix = seqrs.getString("prefix");
                            String suffix = seqrs.getString("suffix");
                            int numberofdigit = seqrs.getInt("numberofdigit");
                            int startfrom = seqrs.getInt("startfrom");
                            String showleadingzero = seqrs.getString("showleadingzero");
                            String isdatebeforeprefix = seqrs.getString("isdatebeforeprefix");
                            String dateformatinprefix = seqrs.getString("dateformatinprefix");
                            String showdateformataftersuffix = seqrs.getString("showdateformataftersuffix");
                            String dateformataftersuffix = seqrs.getString("dateformataftersuffix");

                            //Creating same Sequence Format for Asset Group    
                            String uuid = UUID.randomUUID().toString().replace("-", "");
                            String insertSeqFormatQuery = "insert into sequenceformat "
                                    + "(id,name,prefix,suffix,numberofdigit,startfrom,showleadingzero,isdatebeforeprefix,dateformatinprefix,showdateformataftersuffix,dateformataftersuffix,modulename,moduleid,company) "
                                    + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                            PreparedStatement insertstmt = conn.prepareStatement(insertSeqFormatQuery);
                            insertstmt.setString(1, uuid);
                            insertstmt.setString(2, name);
                            insertstmt.setString(3, prefix);
                            insertstmt.setString(4, suffix);
                            insertstmt.setInt(5, numberofdigit);
                            insertstmt.setInt(6, startfrom);
                            insertstmt.setString(7, showleadingzero);
                            insertstmt.setString(8, isdatebeforeprefix);
                            insertstmt.setString(9, dateformatinprefix);
                            insertstmt.setString(10, showdateformataftersuffix);
                            insertstmt.setString(11, dateformataftersuffix);
                            insertstmt.setString(12, "autoassetgroup");
                            insertstmt.setInt(13, 42);// 42 is moduleid foe asset group. It is defined in Constants.java
                            insertstmt.setString(14, companyid);
                            insertstmt.execute();

                            //Updating Asset Group seqformat with created formaid   
                            String updateQuery = "update product set seqformat=? where seqformat=? and isasset='1'";
                            PreparedStatement updatestmt = conn.prepareStatement(updateQuery);
                            updatestmt.setString(1, uuid);
                            updatestmt.setString(2, formatID);
                            updatestmt.execute();
                            migratedSeqformat++;
                        }
                    }
                }
            }
            if (migratedSeqformat > 0) {
                out.println("</br>Script executed successfully.</br> Total " + migratedSeqformat + " Sequence format added into Asset group from Product.");
            } else {
                out.println("</br>Script executed successfully.There is no record for updation.");
            }

        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        if (conn != null) {
            conn.close();//finally release connection   
        }
    }

%>
