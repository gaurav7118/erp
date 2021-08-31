

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
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String query = "";
        String insertquery = "";
        ResultSet rs;
        String paymentid = "";
        String memo = "";
        String company = "";
        String advancedetailid = "";
        Double advanceamount = 0.0;
        int count=0;
        String currentSubDomain="";
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            // Getting data from 'payment' table
//          query = "SELECT id,memo,company,advanceamount FROM payment where company in (SELECT companyid FROM company WHERE subdomain= ?) and isadvancepayment='T' ";
            query = "SELECT r.id,r.memo,r.company,r.cndnandinvoiceid,r.currency,r.depositamount,r.journalentry,r.receiptnumber,r.company,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where c.subdomain =? and r.cndnandinvoiceid IS NOT NULL and r.invoiceadvcndntype=1";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            rs = stmtquery.executeQuery();
        } else {
            // Getting data from 'payment' table
            query = "SELECT r.id,r.memo,r.company,r.cndnandinvoiceid,r.currency,r.depositamount,r.journalentry,r.receiptnumber,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where r.cndnandinvoiceid IS NOT NULL and r.invoiceadvcndntype=1 order by c.subdomain";
            //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
            stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        while (rs.next()) {
            String receiptnumber = rs.getString("receiptnumber");
            paymentid = rs.getString("id");
            memo = rs.getString("memo");
            company = rs.getString("company");
            String cndnandinvoiceid = rs.getString("cndnandinvoiceid");
            String currency = rs.getString("currency");
            double depositamount = rs.getDouble("depositamount");
            String mainJE = rs.getString("journalentry");
            String companySubdomain=rs.getString("subdomain");
            query = " select * from debitnotepayment where receiptid='"+cndnandinvoiceid+"'";
            Statement statement = conn.createStatement(
                           ResultSet.TYPE_SCROLL_INSENSITIVE,
                           ResultSet.CONCUR_UPDATABLE);
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                String detailId = resultSet.getString("id");
                String cndnamount = resultSet.getString("amountdue");
                String cndnamountpaid = resultSet.getString("amountpaid");
                double cndnAmount=Double.parseDouble(cndnamount);
                double cndnAmountPaid=Double.parseDouble(cndnamountpaid);
                resultSet.updateString("receiptid", paymentid);
                resultSet.updateString("fromcurrency", currency);
                resultSet.updateString("tocurrency", currency);
                resultSet.updateDouble("amountinreceiptcurrency", cndnAmount);
                resultSet.updateDouble("paidamountinreceiptcurrency", cndnAmountPaid);
                resultSet.updateRow();
            }
            
            String updatequery = "update receipt set cndnandinvoiceid=null,paymentwindowtype=1,depositamount=? where id=?";
            stmtquery = conn.prepareStatement(updatequery);
            stmtquery.setDouble(1, depositamount);
            stmtquery.setString(2, paymentid);
            stmtquery.executeUpdate();
            

            
            String selectQuery = "select depositamount,journalentry from receipt where id=?";
            stmtquery = conn.prepareStatement(selectQuery);
            stmtquery.setString(1, cndnandinvoiceid);
            ResultSet rsCN=stmtquery.executeQuery();

            String deletequery = "delete from receipt where id=?";
            PreparedStatement preparedStmnt = conn.prepareStatement(deletequery);
            preparedStmnt.setString(1, cndnandinvoiceid);
            preparedStmnt.executeUpdate();
            
            while(rsCN.next()){
                depositamount+=rsCN.getDouble("depositamount");
                String journalEntry=rsCN.getString("journalentry");
                query = " select * from jedetail where journalentry='"+journalEntry+"'";
                Statement statementJE = conn.createStatement(
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_UPDATABLE);
                ResultSet resultSetJE = statementJE.executeQuery(query);
                while(resultSetJE.next()){
                    resultSetJE.updateString("journalentry", mainJE);
                    resultSetJE.updateRow();
                }
                String deletequeryJE = "delete from journalentry where id=?";
                PreparedStatement preparedStmntJE = conn.prepareStatement(deletequeryJE);
                preparedStmntJE.setString(1, journalEntry);
                preparedStmntJE.executeUpdate();
            }
            
            if (!currentSubDomain.equals(companySubdomain)) {
                 out.println("===================================Company Subdomain -" + companySubdomain + " =====================================================");
                 currentSubDomain = companySubdomain;
            }            
            out.println("Receipt Number -"+receiptnumber);
            
            count++;
        }
       out.println("Receipt CN/DN Details - " +String.valueOf(count)+" Records has been copied ");
       out.println("==============================================================================================================");
       out.println();
       
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>