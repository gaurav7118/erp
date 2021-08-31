
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<!--Following steps need to be done on DB before executing this script
First execute missingsql.jsp then only execute sqlformulltermid.jsp-->


<%
    Connection conn = null;
        try {
            String serverip = request.getParameter("serverip");
            String dbname = request.getParameter("dbname");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String subdomain = request.getParameter("subdomain");
            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
                out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
                throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");                
            }
            String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
            String driver = "com.mysql.jdbc.Driver";

            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, username, password);

            String querycompany = "SELECT companyid FROM company WHERE  subdomain=?";
            String queryproduct = "SELECT id,name FROM product where company=?";
            String querylandedcost = "SELECT id,lccName FROM t_landingcostcategory where company=?";
            String queryinsertProductMap = "INSERT productid_landingcostcategoryid (productid,lccategoryid) values(?,?)";
            String queryCheckProductMap = "SELECT * FROM productid_landingcostcategoryid where productid =? AND lccategoryid = ? ";

            PreparedStatement stmtcash = conn.prepareStatement(querycompany);
            stmtcash.setString(1, subdomain);
            ResultSet rscompany = stmtcash.executeQuery();
            String companyid = "";
            if(rscompany.next()){
               companyid = rscompany.getString(1); 
            }
            //After using result set and Prepared Statement closing the connection
                if (rscompany != null) {
                    rscompany.close();

                }
                if (stmtcash != null) {
                    stmtcash.close();
                }
            
            if (!StringUtil.isNullOrEmpty(companyid)) {
                Map<String,String> landedidMap = new HashMap<String,String>();
                PreparedStatement stmt1 = conn.prepareStatement(querylandedcost);
                stmt1.setString(1, companyid);
                ResultSet rs1 = stmt1.executeQuery();
                while (rs1.next()) { // Add list landed item in Set .
                    landedidMap.put(rs1.getString(1),rs1.getString(2));
                }
                
                //After using result set and Prepared Statement closing the connection
                if (rs1 != null) {
                    rs1.close();

                }
                if (stmt1 != null) {
                    stmt1.close();
                }
                
                Map<String,String> productidMap = new HashMap<String,String>();
                PreparedStatement stmt = conn.prepareStatement(queryproduct);
                stmt.setString(1, companyid);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {   // Add List of products in Set.
                    productidMap.put(rs.getString(1),rs.getString(2));
                }
                
                //After using result set and Prepared Statement closing the connection
                if (rs != null) {
                    rs.close();

                }
                if (stmt != null) {
                    stmt.close();
                }
                
                
                for (String prodId : productidMap.keySet()) {
                    for (String landedId : landedidMap.keySet()) {
                        
                        PreparedStatement stmt3 = conn.prepareStatement(queryCheckProductMap);
                        stmt3.setString(1, prodId);
                        stmt3.setString(2, landedId);
                        ResultSet rs3=stmt3.executeQuery();
                        if(rs3.next()){  // Check record already present in db .
                            continue;
                        }
                        
                        //After using result set and Prepared Statement closing the connection
                            if (rs3 != null) {
                                rs3.close();

                            }
                            if (stmt3 != null) {
                                stmt3.close();
                            }
                        
                        PreparedStatement stmt2 = conn.prepareStatement(queryinsertProductMap);
                        stmt2.setString(1, prodId);
                        stmt2.setString(2, landedId);
                        boolean rs2 = stmt2.execute(); // Insert record .
                        out.println("<br><br> Product : <b>" + productidMap.get(prodId) + " </b> And Landed Cost : <b>"+ landedidMap.get(landedId)+"</b>");
                        //After using result set and Prepared Statement closing the connection
                            if (stmt2 != null) {
                                stmt2.close();
                            }
                    }
                     out.println("\n-----------------------------------------------------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();                                   
        }finally{
           if(conn!=null){
               conn.close();
           } 
        }

%>

  