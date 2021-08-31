<%-- 
    Document   : ReadUsersRecord
    Created on : Oct 28, 2013, 7:44:16 PM
    Author     : Sumit Jain
--%>


<%@page import="com.sun.org.apache.bcel.internal.generic.AALOAD"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.CsvReader"%>
<%@page import="javax.servlet.http.HttpServlet"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="javax.servlet.http.HttpServletResponse"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.Constants"%>
<%
        String failedRecords = "";
        CsvReader csvReader = null;
        FileInputStream fstream = null;
        String csvFile = "";
        System.out.println("<br>Starting the script....");
        int rejectedFile = 0;//For count rejected file
        String skipMsg = ""; //For error message
        String warningMsg = ""; //For warning message when employee id
        Connection conn = null;
    try {
        
            String serverip = request.getParameter("serverip");
            String port = "3306";
            String dbName = request.getParameter("dbname");
            String userName = request.getParameter("username");
            String password = request.getParameter("password");
            String subdomain = request.getParameter("subdomain");

            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
                throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
            }
            String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            String driver = "com.mysql.jdbc.Driver";

            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, userName, password);
            
            
        String filename = request.getParameter("filename");
        csvFile = filename + ".csv";
        String destinationDirectory = "/home/krawler/Downloads";//  Constants.CSVFileStoreLocation;    --------- Directory Path
        File csv = new File(destinationDirectory + "/" + csvFile);
        fstream = new FileInputStream(csv);
        csvReader = new CsvReader(new InputStreamReader(fstream));
        csvReader.readRecord();
        String header = csvReader.getRawRecord();   // ----------------this is for Header you must have header to read record
        failedRecords = header + ",\"Log description\"";
        int totalrecords = 0;
        int totalUserCount = 1;
        int counterTrue = 0;     // true            
        int counterFalse = 0;    // false
        int countererror = 0;    // other         
         while (csvReader.readRecord()) {

            String productid = getFieldFromCSV(csvReader,0, "Product ID");  // Suppose you stored true false
            String inputvat = getFieldFromCSV(csvReader,2, "INPUT VAT");  // other column           
            String inputvatadditional = getFieldFromCSV(csvReader,3, "Input Additional Tax");  // other column
 	    String outputvat = getFieldFromCSV(csvReader,4, "Output VAT");  // other column
            String outputvatadditional = getFieldFromCSV(csvReader,5, "Output Additional Tax");  // other column
            
            HashMap<String, String> hashMap = new HashMap<String, String>();
            
            if (!StringUtil.isNullOrEmpty(productid)) {
                hashMap.put("productid", productid);
            }
            if (!StringUtil.isNullOrEmpty(inputvat)) {
                hashMap.put("inputvat", inputvat);
            }
            if (!StringUtil.isNullOrEmpty(outputvat)) {
                hashMap.put("outputvat", outputvat);
            }            
            if (!StringUtil.isNullOrEmpty(inputvatadditional)) {
                hashMap.put("inputvatadditional", inputvatadditional);
            }
            if (!StringUtil.isNullOrEmpty(outputvatadditional)) {
                hashMap.put("outputvatadditional", outputvatadditional);
            }
            if (!hashMap.isEmpty()) {                   
                    saveRecordProductTax(request, response, hashMap,conn,subdomain,totalrecords);
            }
            totalrecords++;
        }
        out.println("<br><br><b><i>Data scanning sucessfully...!</i></b>");

    } catch (Exception ex) {
        out.println("Exception :- " + ex);
    } finally {
            if (conn != null) {
                conn.close();
            }
        }
%>
<%!
   private String getFieldFromCSV(CsvReader csvrdr, int index, String FieldName) throws IOException {
        String result = "";
           String date="";   
        try {
            csvrdr.getRawRecord().contains(FieldName);
            if (!(StringUtil.isNullOrEmpty(cleanHTML(csvrdr.get(index))))) {
                result = cleanHTML(csvrdr.get(index));
                if(FieldName.compareTo("Date Joined") == 0 || FieldName.compareTo("Date of Birth") == 0) {
                 result = cleanHTML(csvrdr.get(index));
                }

            } else {
                result = "";
            }
        } finally {
            return result;
        }
    }

    private String cleanHTML(String strText) throws IOException {
        return StringUtil.serverHTMLStripper(strText);
    }

    private Boolean saveRecordProductTax(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> hashMap,Connection conn,String subdomain,int totalrecords) throws IOException, SQLException, ServletException {
       
        PrintWriter out = response.getWriter();
        try {
            String query = "select companyid,companyname FROM company ";
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                query += " where subdomain= ?";
            }
            PreparedStatement stmt1 = conn.prepareStatement(query);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                stmt1.setString(1, subdomain);
            }
            ResultSet rs1 = stmt1.executeQuery();
            int totalCompanyUpdationCnt = 0;
            while (rs1.next()) {
                String companyID = rs1.getString("companyid");
                String companyName = rs1.getString("companyname");
                String productid = hashMap.get("productid");

                /*
                 * ------ Get UUID of a product From product ID-----
                 */

                query = "SELECT id FROM product WHERE productid=? and company = ?"; // productp id for Stock Adjustment is 31
                PreparedStatement stmtProduct = conn.prepareStatement(query);
                stmtProduct.setString(1, productid);
                stmtProduct.setString(2, companyID);
                ResultSet rsProduct = stmtProduct.executeQuery();
                while (rsProduct.next()) {
                    String pid = rsProduct.getString(1);
                    try{
                    /*
                     * ------ Set Default false to all Tax of a given product
                     * -----
                     */
                    out.println("<br><br> "+ totalrecords +" ) ----------------- "+hashMap.get("productid")+" -----------------------\n ");
                    query = "UPDATE producttermsmap  SET isdefault='F',taxtype=1  WHERE product=?"; // productp id for Stock Adjustment is 31
                    PreparedStatement stmt2 = conn.prepareStatement(query);
                    stmt2.setString(1, pid);
                    stmt2.execute();
                    stmt2.close();
                    int count=1;
                    for (String taxName : hashMap.keySet()) {
                        if(taxName.equals("productid")){
                            continue;
                        }
                        query = "UPDATE producttermsmap  SET isdefault='T' WHERE product=? AND term in (SELECT id FROM linelevelterms where company=? and term =?)"; // productp id for Stock Adjustment is 31
                        PreparedStatement stmt3 = conn.prepareStatement(query);
                        stmt3.setString(1, pid);
                        stmt3.setString(2, companyID);
                        stmt3.setString(3, hashMap.get(taxName));
                        stmt3.execute();
                        stmt3.close();
                        out.println("<br><br>  "+count+" ) <b> "+hashMap.get(taxName)+"</b> : Tax Set Is Default Successfully.");
                        count++;
                    }
                     totalCompanyUpdationCnt++;
                    }catch(Exception ex){
                        out.println("<br><br> "+hashMap.get("productid")+"</b> : Tax Set Is Not Mapped Successfully. Error :: "+ ex.getLocalizedMessage());
                    }
                }     
                
            }


        } catch (Exception e) {
            e.printStackTrace();
            out.print(e.toString());
        } 
        return true;
    }
%>

