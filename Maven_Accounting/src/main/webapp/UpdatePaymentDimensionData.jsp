

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%

    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldlabel = request.getParameter("fieldlabel");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String ModuleIdQuery = "";
        ResultSet moduleidrs = null;

        String refid = "";
        /**
         * Get Dimension of Masters
         */
        if (StringUtil.isNullOrEmpty(subDomain)) {
            ModuleIdQuery = "SELECT id,companyid,fieldlabel FROM fieldparams WHERE fieldtype=4 and customcolumn=0 and moduleid IN('25','26')";
            stmtquery = conn.prepareStatement(ModuleIdQuery);
            moduleidrs = stmtquery.executeQuery();
        } else {
            ModuleIdQuery = "SELECT id,companyid,fieldlabel FROM fieldparams WHERE fieldtype=4 and customcolumn=0 and moduleid IN('25','26') and companyid IN(SELECT companyid from company WHERE subdomain=?)";
            stmtquery = conn.prepareStatement(ModuleIdQuery);
            stmtquery.setString(1, subDomain);
            moduleidrs = stmtquery.executeQuery();
        }
        while (moduleidrs.next()) {
            String masterFieldId = moduleidrs.getString("id");
            String companyId = moduleidrs.getString("companyid");
            fieldlabel = moduleidrs.getString("fieldlabel");
            /*
             Get Dimension Record for Payment Modules
             */
            String paymentQuery = "select id,moduleid,colnum from fieldparams where fieldlabel=? and companyid=? and moduleid in ('14','16')";
            PreparedStatement preparedStatement = conn.prepareStatement(paymentQuery);
            preparedStatement.setString(1, fieldlabel);
            preparedStatement.setString(2, companyId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String paymentFieldId = resultSet.getString("id");
                int module = Integer.parseInt(resultSet.getString("moduleid"));
                int colnum = Integer.parseInt(resultSet.getString("colnum"));
                /*
                 Get FieldCombodata info for payment
                 */
                String fieldcombodatapaymentQ = "select id,value from fieldcombodata where fieldid=?";
                PreparedStatement preparedStatement1 = conn.prepareStatement(fieldcombodatapaymentQ);
                preparedStatement1.setString(1, paymentFieldId);
                ResultSet resultSet1 = preparedStatement1.executeQuery();
                while (resultSet1.next()) {
                    String fcdPaymentId = resultSet1.getString("id");
                    String fcdPaymentValue = resultSet1.getString("value");
                    /*
                     Get FieldComboDate info Master using Payment values
                     */
                    String customerFieldComboQ = "select id from fieldcombodata where value=? and fieldid=?";
                    PreparedStatement preparedStatement2 = conn.prepareStatement(customerFieldComboQ);
                    preparedStatement2.setString(1, fcdPaymentValue);
                    preparedStatement2.setString(2, masterFieldId);
                    ResultSet resultSet2 = preparedStatement2.executeQuery();
                    while (resultSet2.next()) {
                        String masterFieldComboId = resultSet2.getString("id");
                        /*
                         Update such columns from Table i.e. Replace Master values with Module Specific values
                         */
                        String updateQuery = "update accjecustomdata set col" + colnum + "=? where col" + colnum + "=? and moduleid=? and company=?";
                        PreparedStatement preparedStatement3 = conn.prepareStatement(updateQuery);
                        preparedStatement3.setString(1, fcdPaymentId);
                        preparedStatement3.setString(2, masterFieldComboId);
                        preparedStatement3.setString(3, "" + module);
                        preparedStatement3.setString(4, companyId);
                        int update = preparedStatement3.executeUpdate();
                        if (update > 0) {
                            out.print("Update " + update + " Records from Accjecustomdata for Module: " + module + " Dimension=" + fieldlabel + " Value =" + fcdPaymentValue + "<br> ColNumber: " + colnum + " Old Value: " + masterFieldComboId + " Replaced with: " + fcdPaymentId + " <br> CompanyId: " + companyId + "<br>");
                        }
                    }
                }
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>