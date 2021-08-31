<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
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
        String port = "3306";//request.getParameter("port");//"3306";
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
        String query = "select companyid,companyname,currency FROM company where country='137'";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
              query += " and subdomain= ? ";
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
            String currency = rs.getString("currency");

            out.println("<br><br> Updating For Company <b>" + companyname + "</b>");

            DateFormat df = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

            java.util.Date creationDate = df.parse(df.format(new java.util.Date()));

            List<String> taxList = new ArrayList<String>();
            taxList.add("GST(AJS1)");
            taxList.add("GST(AJP1)");

            for (String taxCode : taxList) {

                query = "SELECT id FROM account WHERE `name`=? AND company=?";
                PreparedStatement stmt1 = conn.prepareStatement(query);
                stmt1.setString(1, taxCode);
                stmt1.setString(2, companyId);
                ResultSet rs1 = stmt1.executeQuery();

                String accountId = "";

                if (rs1.next()) {
                    accountId = rs1.getString("id");
                    out.println("<br><br> Account <b>" + taxCode + "</b> exist for company " + companyname);
                } else {
                    query = "SELECT id FROM accgroup WHERE company=? AND name=?";
                    stmt1 = conn.prepareStatement(query);
                    stmt1.setString(1, companyId);
                    stmt1.setString(2, "Other Current Liability");
                    rs1 = stmt1.executeQuery();
                    String groupId = "";
                    if (rs1.next()) {
                        groupId = rs1.getString("id");
                    } else {
                        out.println("<h1>Other Current Liability Group Not Found For Company - </h1><b>"+companyname+"</b>");
                        continue;
                    }

                    int accountType = 1;//Group.ACC_TYPE_BALANCESHEET

                    int masterType = 4;//Group.ACCOUNTTYPE_GST

                    String insertQuery = "INSERT INTO account(id, name,groupname,company,currency,creationdate,accounttype,mastertypeid,openingbalance,deleteflag,life,presentvalue,isdepreciable,budget) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    String accId = UUID.randomUUID().toString();
                    stmt1 = conn.prepareStatement(insertQuery);
                    stmt1.setString(1, accId);
                    stmt1.setString(2, taxCode);
                    stmt1.setString(3, groupId);
                    stmt1.setString(4, companyId);
                    stmt1.setString(5, currency);
                    stmt1.setObject(6, creationDate);
                    stmt1.setInt(7, accountType);
                    stmt1.setInt(8, masterType);
                    stmt1.setDouble(9, 0.0);
                    stmt1.setString(10, "F");
                    stmt1.setDouble(11, 10);
                    stmt1.setDouble(12, 0.0);
                    stmt1.setInt(13, 0);
                    stmt1.setInt(14, 0);

                    int updatedRow = stmt1.executeUpdate();
                    
                    out.println("<br><br> Account <b>" + taxCode + "</b> is Added for company " + companyname);

                    accountId = accId;

                }

                // Check Tax Code for this company exist or not

                query = "SELECT id FROM tax WHERE taxcode=? AND company=?";

                stmt1 = conn.prepareStatement(query);
                stmt1.setString(1, taxCode);
                stmt1.setString(2, companyId);
                rs1 = stmt1.executeQuery();
                
                if (rs1.next()) {
                    out.println("<br><br> Tax <b>" + taxCode + "</b> exist for company " + companyname);
                } else {
                    // Saving Tax 

                    int taxType = 2;
                    String taxDescription = "Adjustment Output Tax";
                    if (StringUtil.equal("GST(AJP1)", taxCode)) {// input tax(Purchase Tax)
                        taxType = 1;
                        taxDescription = "Adjustment Input Tax";
                    }

                    String taxId = UUID.randomUUID().toString();

                    String insertQuery = "INSERT INTO tax(id,name,taxtype,description,taxcode,deleteflag,account,company) VALUES(?,?,?,?,?,?,?,?)";

                    stmt1 = conn.prepareStatement(insertQuery);
                    stmt1.setString(1, taxId);
                    stmt1.setString(2, taxCode);
                    stmt1.setInt(3, taxType);
                    stmt1.setString(4, taxDescription);
                    stmt1.setString(5, taxCode);
                    stmt1.setString(6, "F");
                    stmt1.setString(7, accountId);
                    stmt1.setString(8, companyId);

                    int insertedRows = stmt1.executeUpdate();
                    out.println("<br><br>" + insertedRows + " rows inserted for Tax Table for Tax Code <b>" + taxCode + "</b> for Company " + companyname);


                    // Making Entries in Tax List

                    String taxListId = UUID.randomUUID().toString();

                    insertQuery = "INSERT INTO taxlist(id, applydate, percent, tax, company) VALUES(?,?,?,?,?)";

                    stmt1 = conn.prepareStatement(insertQuery);
                    stmt1.setString(1, taxListId);
                    stmt1.setObject(2, creationDate);
                    stmt1.setInt(3, 6);
                    stmt1.setString(4, taxId);
                    stmt1.setString(5, companyId);

                    insertedRows = stmt1.executeUpdate();
                    out.println("<br><br>" + insertedRows + " rows inserted for TaxList Table for Tax Code <b>" + taxCode + "</b> for Company " + companyname);
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
