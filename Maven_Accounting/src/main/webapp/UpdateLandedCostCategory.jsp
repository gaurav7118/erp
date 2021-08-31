<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
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

<%!
%>
<%
    Connection conn = null, conn1 = null;
    try {
        //SCRIPT URL : http://<app-url>/UpdateLandedCostCategory.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?&landcostcategory=?&productcode=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String landcostCategory = request.getParameter("landcostcategory");
        String productcode = request.getParameter("productcode");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password, subdomain) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String query1 = "", companyid = "", condition="", prodcondition="";

        PreparedStatement pst1 = null, pst2 = null, pst3 = null, pst4 = null, pst = null;
        ResultSet rst1 = null, rst2 = null, rst3 = null, rst4 = null, rst = null;
        ArrayList alist = new ArrayList();
        //Execution Started :
        out.println("<br><br><center>Execution Started @ " + new java.util.Date() + "<br><br>");
        Class.forName(driver).newInstance();  
        
        //Conditional Query for Landed Cost Category (If Category is sent through URL).
        List<String> list = null;
        StringBuilder builder = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(landcostCategory)) {
            list = Arrays.asList(landcostCategory.split("\\s*,\\s*"));
            condition = "AND lccName IN (";
            for (int i = 0; i < list.size(); i++) {
                builder.append("?,");                
            }
            condition += builder.deleteCharAt( builder.length() -1 ).toString()+")";
        }
        
        //Conditional Query for Product (If Product ID is sent through URL).
        List<String> productlist = null;
        StringBuilder prodbuilder = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(productcode)) {
            productlist = Arrays.asList(productcode.split("\\s*,\\s*"));
            prodcondition = "AND productid IN (";
            for (int i = 0; i < productlist.size(); i++) {
                prodbuilder.append("?,");                
            }
            prodcondition += prodbuilder.deleteCharAt( prodbuilder.length() -1 ).toString()+")";
        }
        
        conn = DriverManager.getConnection(connectString, username, password);
        conn1 = DriverManager.getConnection(connectString, username, password);
        query1 = "SELECT companyid FROM company WHERE subdomain=?";
        pst1 = conn.prepareStatement(query1);
        pst1.setString(1, subdomain);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            try {
                companyid = rst1.getString("companyid");
                String landCostQuery = "SELECT id, lccName FROM t_landingcostcategory WHERE company=? "+condition;
                pst2 = conn.prepareStatement(landCostQuery);
                pst2.setString(1, companyid);
                int index = 2;
                if (list!=null && !list.isEmpty()) {
                    for (Object o : list) {
                        pst2.setObject(index++, o); 
                    }
                }
                rst2 = pst2.executeQuery();
                int count = 0;
                while (rst2.next()) {
                    //Prepare INSERT query for Batch Update Operation
                    String updateProduct = "INSERT INTO productid_landingcostcategoryid(productid,lccategoryid) VALUES (?, ?)";
                    pst = conn.prepareStatement(updateProduct);
                    conn.setAutoCommit(false);

                    //Prepare Query to get the Product
                    String landcategoryid = rst2.getString("id");
                    String lccName = rst2.getString("lccName");
                    String getProduct = "SELECT id, productid FROM product WHERE company=? "+prodcondition;
                    pst3 = conn.prepareStatement(getProduct);
                    pst3.setString(1, companyid);
                    int indexpos = 2;
                    if (productlist!=null && !productlist.isEmpty()) {
                        for (Object o : productlist) {
                            pst3.setObject(indexpos++, o);
                        }
                    } 
                    rst3 = pst3.executeQuery();                    
                    while (rst3.next()) {
                        String prodid = rst3.getString("id");  
                        String productCode = rst3.getString("productid");  
                        
                        if(alist.contains(prodid)){
                            continue;   //If Product already mapped with any one of the Landed Cost Category then skip this.
                        }
                        if (count == 0) {
                            String getMappedProduct = "SELECT productid FROM productid_landingcostcategoryid WHERE productid=?";
                            pst4 = conn1.prepareStatement(getMappedProduct);
                            pst4.setString(1, prodid);
                            if (pst4.executeQuery().next()) {
                                alist.add(prodid);
                                out.println("<br><br><center><b>Product : "+productCode+" did not update. It is already mapped with one of the Landed Cost Category. Please update it manually.<b/>");
                                continue;   //If Product already mapped with any one of the Landed Cost Category then skip this.
                            }
                        }
                        //Set Params for INSERT batch query. There is no Limit for INSER batch query.
                        pst.setString(1, prodid);
                        pst.setString(2, landcategoryid);
                        pst.addBatch();
                    }
                    //Perform Batch Update Operation
                    pst.executeBatch();
                    conn.commit();
                    count++;
                    out.println("<br><br><center><b>Landed Cost Category : " + lccName + " updated for all products of " + subdomain+"<b/>");
                }
            } catch (SQLException se) {
                out.println("Error Inside the Land Cost Category Loop. Please check & try again later.");
                se.printStackTrace();
            }
        }//company        
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("<br><br><center><b>Exception occured in outer loop. Please check & try again.<center/>");
        out.print(ex.toString());
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("<br><br><center>Connection Closed....<br/>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn1 != null) {
            try {
                conn1.close();
                out.println("<center>Connection-1 Closed....<br/>");
                out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>