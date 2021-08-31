
<%@page import="com.krawler.spring.accounting.handler.AccountingManager"%>
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
        String dbName = "accounting_dev";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        int cnt = 0;
        String record = "";
        String companyId="";
        String query = "SELECT companyid FROM company";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery(); 
        while (rs.next()) {
                companyId = rs.getString("companyid");
                //companySubDomain = rs.getString("subdomain");
                //out.println("<br><br>Making Entry For Company subdomain <b>: "+companySubDomain+"</b>");
                // Filling Data into podetails table for rowtaxamount column.
                query = "select GROUP_CONCAT(id SEPARATOR ',') as moduleIds from fieldparams WHERE companyid=? and  fieldtype ='4' group by fieldlabel, fieldtype, customfield, customcolumn";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                ResultSet poidrs = stmt.executeQuery();
                while (poidrs.next()) {
                    String fieldParamId = poidrs.getString("moduleIds");
                    String IdsArray[] = fieldParamId.split(",");
                    String tempIds[] = IdsArray;
                    int rowUpdationCntForAPO = 0;

                    //out.println("<br><br>Updating Purchase Order Rows For PO <b>: "+ponumber+"</b>");

                    // Selecting purchase order rows for each PO.
                    for (int i = 0; i < IdsArray.length; i++) {
                        String id = IdsArray[i];
                        String sql = "select * from fieldcombodata where fieldid =?";
                        PreparedStatement stmt1 = conn.prepareStatement(sql);
                        stmt1.setObject(1, IdsArray[i]);
                        ResultSet rs1 = stmt1.executeQuery();
                        //HashSet set = new HashSet();
                        //HashMap<String, String> parentMap = new HashMap<String, String>();
                          // rs1 = stmt1.executeQuery();
                            while (rs1.next()) {
                                String value = rs1.getString("value");
                                String parentId = rs1.getString("parent");
                                String fieldId = rs1.getString("fieldid");
                                //set.add(value);
                                //parentMap.put(value, rs.getString("parent"));

                                for (int j = 0; j < tempIds.length; j++) {
                                    // Iterator it = set.iterator();
                                    // while (it.hasNext()) {
                                    String value1 = value;//((Object) it.next()).toString();
                                    String parent = parentId;//parentMap.get(value1);
                                    String sql1 = "select value, parent from fieldcombodata where fieldid =? and value=? ";
                                    PreparedStatement stmt2 = conn.prepareStatement(sql1);
                                    stmt2.setObject(1, tempIds[j]);
                                    stmt2.setObject(2, value1);
                                    //stmt2.setObject(3, parent);
                                    ResultSet rs2 = stmt2.executeQuery();
                                    rs2.last();
                                    if (rs2.getRow() == 0) {
                                        String sqlUpdate = "insert into fieldcombodata (id, fieldid, value) values(?,?,?)";
                                        PreparedStatement stmt3 = conn.prepareStatement(sqlUpdate);
                                         stmt3.setObject(1, UUID.randomUUID().toString().replace("-", ""));
                                        stmt3.setObject(2, tempIds[j]);
                                        stmt3.setObject(3, value1);
                                         int  updateValue = stmt3.executeUpdate();
                                        //out.println(tempIds[j]+" Records added successfully. with value =" + value1 + " and field Id =" + fieldId);
                                        cnt++;
                                    }
                                }
                                //}/
                            }                 
                    }
                }
            }
            out.println(cnt + " Records added successfully.");
    } catch (Exception e) {
        e.printStackTrace();
       

    } finally {
        //conn.close();
    }

    
%>
