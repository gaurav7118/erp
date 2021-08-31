<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%!
%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/DeleteDuplicatePriceListRowWhoseUOMisNULL.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String driver = "com.mysql.jdbc.Driver";
        String query1 = "", companyid = "";
        int updateCount = 0, companycount = 0,nullvalueUpdatedTostockuomcnt = 0;

        PreparedStatement pst = null, pst1 = null, pst2 = null, pst3 = null, innerPst = null;
        ResultSet rst1 = null, rst2 = null, rst3 = null, innerRs = null, innerRsF = null;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        //Execution Started :
        out.println("<br><br><b><center>Execution Started @ " + new java.util.Date() + "</center><b><br><br>");

        conn = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT c.companyid, c.subdomain FROM company c " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        int totalNullCountOfAllSubdomain = 0;
        while (rst1.next()) {
            companycount++;
            companyid = !StringUtil.isNullOrEmpty(rst1.getString("companyid")) ? rst1.getString("companyid") : null;
            subdomain = !StringUtil.isNullOrEmpty(rst1.getString("subdomain")) ? rst1.getString("subdomain") : null;
            out.println("<center><b>" + companycount + " : " + subdomain + "</b></center><br><br>");
            String queryt = "SELECT pr.id, DATE_FORMAT(pr.applydate,'%Y-%m-%d') AS applydate, pr.product,pr.price,pr.affecteduser,pr.currency,pr.company,pr.uomid FROM pricelist pr INNER JOIN (select pl.product from pricelist pl inner join product p on pl.product = p.id where pl.uomid = p.unitOfMeasure) as t ON t.product=pr.product WHERE carryin='T' AND company = '" + companyid + "'";
            pst2 = conn.prepareStatement(queryt);
            rst2 = pst2.executeQuery();
            while (rst2.next()) {
                String plid = !StringUtil.isNullOrEmpty(rst2.getString("id")) ? rst2.getString("id") : null;
                java.sql.Date applyDate = rst2.getObject("applydate") != null ? rst2.getDate("applydate") : null;
                String productid = !StringUtil.isNullOrEmpty(rst2.getString("product")) ? rst2.getString("product") : null;
                String price = !StringUtil.isNullOrEmpty(rst2.getString("price")) ? rst2.getString("price") : null;
                String affecteduser = !StringUtil.isNullOrEmpty(rst2.getString("affecteduser")) ? rst2.getString("affecteduser") : null;
                String currency = !StringUtil.isNullOrEmpty(rst2.getString("currency")) ? rst2.getString("currency") : null;
                String company = !StringUtil.isNullOrEmpty(rst2.getString("company")) ? rst2.getString("company") : null;
                String uomid = !StringUtil.isNullOrEmpty(rst2.getString("uomid")) ? rst2.getString("uomid") : null;
//                String innerQuery = "SELECT id, DATE_FORMAT(applydate,'%Y-%m-%d') AS applydate, product,price,affecteduser,curreny,company,uomid FROM pricelist WHERE carryin='T' AND applydate = ? and product = ? and affecteduser = ? and currency = ? and company = ? ";
                String innerQuery = "SELECT count(*) as CNT FROM pricelist pl inner join product p on pl.product = p.id WHERE pl.carryin='T' AND pl.applydate = ? and pl.product = ? and pl.affecteduser = ? and pl.currency = ? and pl.company = ? and pl.uomid = p.unitOfMeasure ";
                innerPst = conn.prepareStatement(innerQuery);
                innerPst.setDate(1, applyDate);
                innerPst.setString(2, productid);
                innerPst.setString(3, affecteduser);
                innerPst.setString(4, currency);
                innerPst.setString(5, company);
                innerRs = innerPst.executeQuery();
                if (innerRs.next()) {
                        int cnt = innerRs.getInt("CNT");
                        if (cnt > 0) {
                            try {
                                String delQueryIfMatch = "DELETE FROM pricelist WHERE id='" + plid + "' AND company='" + company + "' AND product='" + productid + "' and uomid is null";
                                pst = conn.prepareStatement(delQueryIfMatch);
                                int rowCnt = pst.executeUpdate();
                                if (rowCnt > 0) {
                                    updateCount++;
                                }
                            } catch (Exception se) {
                                System.out.println("Exception occurred while deleting the record for id " + plid + " productid " + productid + " companyid " + company + " uomid " + uomid);
                                out.println("<center><b>Exception occurred while deleting the record !!</b></center><br><br>");
                                se.printStackTrace();
                            }
                        }
                    }

            }//CARRY-IN : True
            pst2 = null;
            rst2 = null;

            String queryf = "SELECT pr.id, DATE_FORMAT(pr.applydate,'%Y-%m-%d') AS applydate, pr.product,pr.price,pr.affecteduser,pr.currency,pr.company,pr.uomid FROM pricelist pr INNER JOIN (select pl.product from pricelist pl inner join product p on pl.product = p.id where pl.uomid = p.unitOfMeasure) as t ON t.product=pr.product WHERE carryin='F' AND company = '" + companyid + "'";
            pst2 = conn.prepareStatement(queryf);
            rst2 = pst2.executeQuery();
            String prevProdidf = "";
            String preAppDatef = "";
            while (rst2.next()) {
                String plidf = !StringUtil.isNullOrEmpty(rst2.getString("id")) ? rst2.getString("id") : null;
                java.sql.Date applyDateF = rst2.getObject("applydate") != null ? rst2.getDate("applydate") : null;
                String productidF = !StringUtil.isNullOrEmpty(rst2.getString("product")) ? rst2.getString("product") : null;
                String priceF = !StringUtil.isNullOrEmpty(rst2.getString("price")) ? rst2.getString("price") : null;
                String affecteduserF = !StringUtil.isNullOrEmpty(rst2.getString("affecteduser")) ? rst2.getString("affecteduser") : null;
                String currencyF = !StringUtil.isNullOrEmpty(rst2.getString("currency")) ? rst2.getString("currency") : null;
                String companyF = !StringUtil.isNullOrEmpty(rst2.getString("company")) ? rst2.getString("company") : null;
                String uomidF = !StringUtil.isNullOrEmpty(rst2.getString("uomid")) ? rst2.getString("uomid") : null;
                String innerQueryF = "SELECT count(*) as CNT FROM pricelist pl inner join product p on pl.product = p.id WHERE pl.carryin='F' AND pl.applydate = ? and pl.product = ? and pl.affecteduser = ? and pl.currency = ? and pl.company = ? and pl.uomid = p.unitOfMeasure ";
                innerPst = conn.prepareStatement(innerQueryF);
                innerPst.setDate(1, applyDateF);
                innerPst.setString(2, productidF);
                innerPst.setString(3, affecteduserF);
                innerPst.setString(4, currencyF);
                innerPst.setString(5, companyF);
                innerRsF = innerPst.executeQuery();
                 if (innerRsF.next()) {
                         int cnt = innerRsF.getInt("CNT");
                         if (cnt > 0) {
                             try {
                                 String delQueryIfMatchF = "DELETE FROM pricelist WHERE id='" + plidf + "' AND company='" + companyF + "' AND product='" + productidF + "' and uomid is null";
                                 pst = conn.prepareStatement(delQueryIfMatchF);
                                 int rowCnt = pst.executeUpdate();
                                 if (rowCnt > 0) {
                                     updateCount++;
                                 }
                                 pst = null;
                             } catch (Exception se) {
                                 System.out.println("Exception occurred while deleting the record for id " + plidf + " productid " + productidF + " companyid " + companyF + " uomid " + uomidF);
                                 out.println("<center><b>Exception occurred while deleting the record !!</b></center><br><br>");
                                 se.printStackTrace();
                             }
                         }
                }
            }//CARRY-IN : True
             String queryToSetNullUOMidToStockUOM = "update pricelist pls INNER JOIN product prt on pls.product = prt.id set pls.uomid = prt.unitOfMeasure where pls.uomid is null and prt.unitOfMeasure is not null AND prt.company = '" + companyid + "'";
             pst3 = conn.prepareStatement(queryToSetNullUOMidToStockUOM);
             nullvalueUpdatedTostockuomcnt = pst3.executeUpdate();
             totalNullCountOfAllSubdomain+=nullvalueUpdatedTostockuomcnt;
             
            //insert query to update null values
            out.println("<center><b>" + updateCount + " Duplicate Records are deleted and null uomid updated with stock uomid count " + nullvalueUpdatedTostockuomcnt + " for " + subdomain + "</b></center><br><br>");
            out.println("<center><b>=======================================================================</b></center><br><br>");
            pst2 = null;
            rst2 = null;
        }//companyid
        
        System.out.println("TOTAL NULL COUNT " + totalNullCountOfAllSubdomain+"\n");
        out.println("<b><center>TOTAL NULL COUNT "+totalNullCountOfAllSubdomain+"</center><b><br>");
        System.out.println("Script Ended...\n");
        out.println("<b><center>Script Ended...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("<center>Connection Closed....</center><br/>");
                //Execution Ended :
                out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "</center><br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>