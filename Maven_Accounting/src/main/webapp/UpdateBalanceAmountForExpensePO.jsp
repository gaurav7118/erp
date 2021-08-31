
<%@page import="com.krawler.hql.accounting.PurchaseOrder"%>
<%@page import="com.krawler.spring.common.AccCommonTablesDAO"%>
<%@page import="com.krawler.hql.accounting.ExpenseGRDetail"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="com.krawler.hql.accounting.ExpensePODetail"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
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
            String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
            String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {
                throw new Exception(" You have not privided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
            }
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, username, password);
            Double balanceAmount = 0.0;
            int count = 0;
            String epoid = "";
            String updatequery = "";
            String query = "";
            PreparedStatement pstmt = null;
            Set purchaseOrderIdSet = new HashSet();
            String queryForCompany = "select companyid,companyname FROM company  where subdomain= ? ";
            PreparedStatement stmt = conn.prepareStatement(queryForCompany);
            stmt.setString(1, subdomain);
            ResultSet cmprs = stmt.executeQuery();
            /**
             * get all Expense PO.
             */
            if (cmprs.next()) {
                String companyId = cmprs.getString("companyid");
                query = "SELECT	id, totalamount FROM purchaseorder WHERE isexpensetype = 'T' and company = '"+ companyId +"'";
            } else {
                query = "SELECT	id, totalamount FROM purchaseorder WHERE isexpensetype = 'T'";
            }
            pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String poid = rs.getString("id");
                /**
                 * get expensePODetails from poid.
                 */
                String epodquery = "SELECT id, amount FROM expensepodetails WHERE purchaseorder = '" + poid + "'";
                PreparedStatement epodpstmt = conn.prepareStatement(epodquery);
                ResultSet epodrs = epodpstmt.executeQuery();
                while (epodrs.next()) {
                    epoid = epodrs.getString("id");
                    Double epodamount = epodrs.getDouble("amount");
                    /**
                     * check ExpensePO link with ExpensePI.
                     */
                    String egrdquery = "SELECT id, amount FROM expenseggrdetails WHERE expensepodetails = '" + epoid + "'";
                    PreparedStatement egrdpstmt = conn.prepareStatement(egrdquery);
                    ResultSet egrdrs = egrdpstmt.executeQuery();
                    if (!egrdrs.next()) {
                        /**
                         * Expense PO not link Expense PI then add amount as
                         * balance amount.
                         */
                        balanceAmount = epodamount;
                        updatequery = "UPDATE expensepodetails SET balanceamount = " + balanceAmount + " WHERE id = '" + epoid + "'";
                        PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                        updatestmt.execute();
                        purchaseOrderIdSet.add(epoid);
                        count++;
                        continue;
                    } else {
                        do {
                            Double egrdamount = egrdrs.getDouble("amount");
                            balanceAmount = egrdamount;
                            /**
                             * Expense PO link Expense PI then minus expensePI
                             * amount from total amount for balance amount.
                             */
                            updatequery = "UPDATE expensepodetails SET balanceamount = (amount -" + balanceAmount + ") WHERE id ='" + epoid + "'";
                            PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                            updatestmt.execute();
                            purchaseOrderIdSet.add(epoid);
                            count++;
                        } while (egrdrs.next());
                    }
                }
            }
            out.println("<center><br/>"+ count +" Data Updated for subdomain : <b>" + subdomain + "</b> <br/>");
            out.println("<br/>Purchase Order Balance Amount updated for following ids : <br/><br/><br/>");
            out.println(purchaseOrderIdSet.toString() + "</center>");
        } catch (Exception e) {
            e.printStackTrace();
            out.print(e.toString());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

%>