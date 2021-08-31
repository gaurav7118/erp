<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

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
                String serverip = "localhost";
                String port = "3306";
                String driver = "com.mysql.jdbc.Driver";

                String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
                String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
                String password = request.getParameter("password") != null ? request.getParameter("password") : "";
                String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";

                if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
                    out.println("Parameter missing from parameters=> [dbname,username,password] ");
                } else {
                    String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
                    Class.forName(driver).newInstance();
                    conn = DriverManager.getConnection(connectDB, userName, password);

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
                        String companyname = comprs.getString("companyname");
                        String compSubDomain = comprs.getString("subdomain");
                        if (!StringUtil.isNullOrEmpty(companyid)) {
                            int CSCount = 0;
                            int CPCount = 0;
                            String CPNumbers = "";
                            String CSNumbers = "";

                            out.println("</br></br><b>Updated company Name: " + companyname + "</b><br><b> Subdomain: " + compSubDomain + "</b>");

                            //For getting Cash account from account compaccpreferences
                            query = "select cashAccount from compaccpreferences where id='" + companyid + "'";
                            PreparedStatement cashstmt = conn.prepareStatement(query);
                            ResultSet cashrs = cashstmt.executeQuery();
                            String cashAccount = "";
                            if (cashrs.next()) {
                                cashAccount = cashrs.getString("cashAccount");
                            }
                            if (!StringUtil.isNullOrEmpty(cashAccount)) {
                                //For getting payment methodid from paymentmethod                                              
                                query = "select id from paymentmethod where detailtype=0 and account=?";//detail type 0 means cash type
                                PreparedStatement stmt = conn.prepareStatement(query);
                                stmt.setString(1, cashAccount);
                                ResultSet rs = stmt.executeQuery();
                                String methodid = "";
                                if (rs.next()) {
                                    methodid = rs.getString("id");
                                }

                                //when there is no cash payment method found for cashAccount create cash payment method with name "cash"                
                                if (StringUtil.isNullOrEmpty(methodid)) {
                                    methodid = UUID.randomUUID().toString().replace("-", "");
                                    query = "insert into paymentmethod (id,methodname,account,detailtype,company) values (?,?,?,?,?) ";
                                    PreparedStatement stmtpmt = conn.prepareStatement(query);
                                    stmtpmt.setString(1, methodid);
                                    stmtpmt.setString(2, "cash");
                                    stmtpmt.setString(3, cashAccount);
                                    stmtpmt.setInt(4, 0);
                                    stmtpmt.setString(5, companyid);
                                    stmtpmt.execute();
                                    out.println("A cash payment method created with name: <b>cash</b> ");
                                }
                                //updating CP with paydetail
                                query = "SELECT goodsreceipt.id,goodsreceipt.grnumber from goodsreceipt "
                                        + "LEFT JOIN jedetail ON jedetail.id=goodsreceipt.centry "
                                        + "where goodsreceipt.cashtransaction=1 and goodsreceipt.deleteflag='F' and goodsreceipt.paydetail is NULL "
                                        + "and goodsreceipt.company =? and jedetail.account=?";
                                PreparedStatement grstmt = conn.prepareStatement(query);
                                grstmt.setString(1, companyid);
                                grstmt.setString(2, cashAccount);

                                ResultSet grrs = grstmt.executeQuery();
                                while (grrs.next()) {
                                    String cashPurchaseID = grrs.getString("id");
                                    String cashNumber = grrs.getString("grnumber");
                                    String uuid = UUID.randomUUID().toString().replace("-", "");
                                    query = "insert into paydetail (id,paymentMethod,company) values (?,?,?) ";
                                    PreparedStatement stmt2 = conn.prepareStatement(query);
                                    stmt2.setString(1, uuid);
                                    stmt2.setString(2, methodid);
                                    stmt2.setString(3, companyid);
                                    stmt2.execute();

                                    query = "update goodsreceipt set paydetail=? where id=? and company=?";
                                    PreparedStatement stmt3 = conn.prepareStatement(query);
                                    stmt3.setString(1, uuid);
                                    stmt3.setString(2, cashPurchaseID);
                                    stmt3.setString(3, companyid);
                                    stmt3.execute();
                                    CPNumbers += (CPCount == 0) ? (cashNumber) : (CPCount % 10 == 0) ? (",</br>" + cashNumber) : ("," + cashNumber);
                                    CPCount++;
                                }

                                //updating CS with paydetail
                                query = "SELECT invoice.id,invoice.invoicenumber from invoice "
                                        + "LEFT JOIN jedetail  ON jedetail.id=invoice.centry "
                                        + "where invoice.cashtransaction=1 and invoice.deleteflag='F' and invoice.paydetail is NULL "
                                        + "and invoice.company =? and jedetail.account=?";
                                PreparedStatement invstmt = conn.prepareStatement(query);
                                invstmt.setString(1, companyid);
                                invstmt.setString(2, cashAccount);

                                ResultSet invrs = invstmt.executeQuery();
                                while (invrs.next()) {
                                    String salesPurchaseID = invrs.getString("id");
                                    String salesNumber = invrs.getString("invoicenumber");
                                    String uuid = UUID.randomUUID().toString().replace("-", "");
                                    query = "insert into paydetail (id,paymentMethod,company) values (?,?,?) ";
                                    PreparedStatement stmt4 = conn.prepareStatement(query);
                                    stmt4.setString(1, uuid);
                                    stmt4.setString(2, methodid);
                                    stmt4.setString(3, companyid);
                                    stmt4.execute();

                                    query = "update invoice set paydetail=? where id=? and company=?";
                                    PreparedStatement stmt5 = conn.prepareStatement(query);
                                    stmt5.setString(1, uuid);
                                    stmt5.setString(2, salesPurchaseID);
                                    stmt5.setString(3, companyid);
                                    stmt5.execute();
                                    CSNumbers += (CSCount == 0) ? salesNumber : (CSCount % 10 == 0) ? (",<br>" + salesNumber) : ("," + salesNumber);
                                    CSCount++;
                                }
                                out.println("</br>No. of Cash Sales updated: " + CSCount);
                                out.println("</br>Updated cash Sales are: " + CSNumbers);
                                out.println("</br></br> No. of Cash purchase updated :" + CPCount);
                                out.println("</br>Updated cash purchase are: " + CPNumbers);
                            } else {
                                out.println("</br>Cash Account is not available in company preferences ");
                            }
                        }
                    }
                }
                if(conn!=null){
                  conn.close();//finally release connection   
                }                
            } catch (Exception e) {
                e.printStackTrace();
                out.println(e.getMessage());

            } finally {
                //conn.close();
            }

%>
