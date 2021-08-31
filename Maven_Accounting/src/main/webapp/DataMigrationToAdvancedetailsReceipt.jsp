

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
            query = "SELECT r.id,r.memo,r.company,r.advanceamount,r.advanceid,r.depositamount,r.journalentry,r.receiptnumber,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where c.subdomain = ? and r.advanceid IS NOT NULL ";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            rs = stmtquery.executeQuery();
        } else {
            // Getting data from 'payment' table
            query = "SELECT r.id,r.memo,r.company,r.advanceamount,r.advanceid,r.depositamount,r.journalentry,r.receiptnumber,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where r.advanceid IS NOT NULL order by c.subdomain";
            //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
            stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        out.println("=======================Converting Advance with Invoice according to new payment========================");
        while (rs.next()) {
            String receiptnumber = rs.getString("receiptnumber");
            paymentid = rs.getString("id");
            memo = rs.getString("memo");
            company = rs.getString("company");
            advanceamount = rs.getDouble("advanceamount");
            String advancedue = rs.getString("advanceamount");
            String advanceid = rs.getString("advanceid");
            double depositamount = rs.getDouble("depositamount");
            String mainJE = rs.getString("journalentry");
            String companySubdomain=rs.getString("subdomain");
            double advanceDue=Double.parseDouble(advancedue);
            query = " select id,company,amount,receipt,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency from receiptdetails where receipt=?";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, advanceid);
            ResultSet resultSet = stmtquery.executeQuery();
            PreparedStatement preparedStatement = null;
            while(resultSet.next()){
                String detailId = resultSet.getString("id");
                double amount = resultSet.getDouble("amount");
                advanceDue-=amount;
                String linkingQuery = "INSERT into linkdetailreceipt (id,company,amount,receipt,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency )"+
                " select id,company,amount,?,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency from receiptdetails where id=?";
                preparedStatement = conn.prepareStatement(linkingQuery);
                preparedStatement.setString(1, paymentid);
                preparedStatement.setString(2, detailId);
                try{
                preparedStatement.executeUpdate();
                }catch(Exception e){
                    query = "SELECT subdomain from company where companyid = ?";
                    //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                    stmtquery = conn.prepareStatement(query);
                    stmtquery.setString(1, company+"");
                    ResultSet rs1 = stmtquery.executeQuery();
                    while(rs1.next()){
                        System.out.println(rs1.getString("subdomain"));
                    }                
                }
                
                String deleteQuery = "delete from receiptdetails where id=?";
                preparedStatement = conn.prepareStatement(deleteQuery);
                preparedStatement.setString(1, detailId);
                preparedStatement.executeUpdate();
            }
            
            
            
            //Copying data to table 'advancedetail'
            advancedetailid = UUID.randomUUID().toString();
            insertquery = "INSERT into receiptadvancedetail(id,company,amount,receipt,amountdue) VALUES (?,?,?,?,?) ";
            stmtquery = conn.prepareStatement(insertquery);
            stmtquery.setString(1, advancedetailid);
            stmtquery.setString(2, company);
            stmtquery.setDouble(3, advanceamount);
            stmtquery.setString(4, paymentid);
            stmtquery.setString(5, advanceDue+"");
            try{
            stmtquery.executeUpdate();
            }catch(Exception e){
                query = "SELECT subdomain from company where companyid = ?";
                //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, company+"");
                ResultSet rs1 = stmtquery.executeQuery();
                while(rs1.next()){
                    System.out.println(rs1.getString("subdomain"));
                }                
            }
            String updatequery = "update receipt set advanceid=null,paymentwindowtype=1,depositamount=? where id=?";
            stmtquery = conn.prepareStatement(updatequery);
            stmtquery.setDouble(1, advanceamount+depositamount);
            stmtquery.setString(2, paymentid);
            try{
            stmtquery.executeUpdate();
            }catch(Exception e){
                query = "SELECT subdomain from company where companyid = ?";
                //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, company+"");
                ResultSet rs1 = stmtquery.executeQuery();
                while(rs1.next()){
                    System.out.println(rs1.getString("subdomain"));
                }                
            }
            
            String selectQuery = "select journalentry from receipt where id=?";
            stmtquery = conn.prepareStatement(selectQuery);
            stmtquery.setString(1, advanceid);
            ResultSet rsCN=stmtquery.executeQuery();
            
            String deletequery = "delete from receipt where id=?";
            PreparedStatement statement = conn.prepareStatement(deletequery);
            statement.setString(1, advanceid);
            statement.executeUpdate();
            
            //update JE
            
            while(rsCN.next()){
                String journalEntry=rsCN.getString(1);
                query = " select * from jedetail where journalentry='"+journalEntry+"'";
                Statement statementJE = conn.createStatement(
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_UPDATABLE);
                ResultSet resultSetJE = statementJE.executeQuery(query);
                while(resultSetJE.next()){
                    resultSetJE.updateString("journalentry", mainJE);
                    resultSetJE.updateRow();
                }
                
                query = " select * from bankreconciliationdetail where journalEntry='" + journalEntry + "'";
                    statementJE = conn.createStatement(
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_UPDATABLE);
                    resultSetJE = statementJE.executeQuery(query);
                    while (resultSetJE.next()) {
                        resultSetJE.updateString("journalEntry", mainJE);
                        resultSetJE.updateRow();
                    }
                
                String deletequeryJE = "delete from journalentry where id=?";
                PreparedStatement preparedStmntJE = conn.prepareStatement(deletequeryJE);
                preparedStmntJE.setString(1, journalEntry);
                try {
                preparedStmntJE.executeUpdate();
                    } catch (Exception e) {
                        query = "SELECT subdomain from company where companyid = ?";
                        //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                        stmtquery = conn.prepareStatement(query);
                        stmtquery.setString(1, company + "");
                        ResultSet rs1 = stmtquery.executeQuery();
                        while (rs1.next()) {
                            System.out.println(rs1.getString("subdomain"));
            }
                    }
            
            }
            
            if (!currentSubDomain.equals(companySubdomain)) {
                out.println("===================================Company Subdomain -" + companySubdomain + " =====================================================");
                currentSubDomain = companySubdomain;
            }
            out.println("Receipt Number -"+receiptnumber);
            
            count++;
        }
        
        currentSubDomain="";
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            // Getting data from 'payment' table
            query = "SELECT r.id,r.memo,r.company,r.depositamount,r.receiptnumber,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where c.subdomain= ? and r.isadvancepayment='T' ";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            rs = stmtquery.executeQuery();
        } else {
            // Getting data from 'payment' table
            query = "SELECT r.id,r.memo,r.company,r.depositamount,r.receiptnumber,c.subdomain FROM receipt r inner join company c on r.company=c.companyid where r.isadvancepayment='T' order by c.subdomain";
            stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        out.println("=======================Converting Only Advance according to new payment========================");
        while (rs.next()) {
            String receiptnumber = rs.getString("receiptnumber");
            paymentid = rs.getString("id");
            memo = rs.getString("memo");
            company = rs.getString("company");
            advanceamount = rs.getDouble("depositamount");
            String advancedue = rs.getString("depositamount");
            String companySubdomain=rs.getString("subdomain");
            double advanceDue=Double.parseDouble(advancedue);
            query = " select id,company,amount,receipt,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency from receiptdetails where receipt=?";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, paymentid);
            ResultSet resultSet = stmtquery.executeQuery();
            PreparedStatement preparedStatement = null;
            while(resultSet.next()){
                String detailId = resultSet.getString("id");
                double amount = resultSet.getDouble("amount");
                advanceDue-=amount;
                String linkingQuery = "INSERT into linkdetailreceipt (id,company,amount,receipt,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency )"+
                " select id,company,amount,receipt,invoice,srno,fromcurrency,tocurrency,exchangeratefortransaction,amountininvoicecurrency from receiptdetails where id=?";
                preparedStatement = conn.prepareStatement(linkingQuery);
                preparedStatement.setString(1, detailId);
                try{
                preparedStatement.executeUpdate();
                }catch(Exception e){
                    query = "SELECT subdomain from company where companyid = ?";
                    //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                    stmtquery = conn.prepareStatement(query);
                    stmtquery.setString(1, company+"");
                    ResultSet rs1 = stmtquery.executeQuery();
                    while(rs1.next()){
                        System.out.println(rs1.getString("subdomain"));
                    }                
                }
                
                String deleteQuery = "delete from receiptdetails where id=?";
                preparedStatement = conn.prepareStatement(deleteQuery);
                preparedStatement.setString(1, detailId);
                try{
                preparedStatement.executeUpdate();
                }catch(Exception e){
                    query = "SELECT subdomain from company where companyid = ?";
                    //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                    stmtquery = conn.prepareStatement(query);
                    stmtquery.setString(1, company+"");
                    ResultSet rs1 = stmtquery.executeQuery();
                    while(rs1.next()){
                        System.out.println(rs1.getString("subdomain"));
            }
                }
            
            }
            
            //Copying data to table 'advancedetail'
            advancedetailid = UUID.randomUUID().toString();
            insertquery = "INSERT into receiptadvancedetail(id,company,amount,receipt,amountdue) VALUES (?,?,?,?,?) ";
            stmtquery = conn.prepareStatement(insertquery);
            stmtquery.setString(1, advancedetailid);
            stmtquery.setString(2, company);
            stmtquery.setDouble(3, advanceamount);
            stmtquery.setString(4, paymentid);
            stmtquery.setDouble(5, advanceDue);
            //stmtquery.setString(5, advanceDue+"");
            try{
            stmtquery.executeUpdate();
            }catch(Exception e){
                query = "SELECT subdomain from company where companyid = ?";
                //query = "SELECT id,memo,company,advanceamount FROM payment where isadvancepayment='T'";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, company+"");
                ResultSet rs1 = stmtquery.executeQuery();
                while(rs1.next()){
                    System.out.println(rs1.getString("subdomain"));
                }                
            }            
            if (!currentSubDomain.equals(companySubdomain)) {
                out.println("===================================Company Subdomain -" + companySubdomain + " =====================================================");
                currentSubDomain = companySubdomain;
            }
            out.println("Receipt Number -"+receiptnumber);
            
            count++;
        }
       out.println("Receipt Advance Type - " + String.valueOf(count)+" Records has been copied ");
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