<%-- 
    Document   : ImportDistributedOpeningBalance
    Created on : Mar 3, 2017, 3:33:24 PM
    Author     : krawler
--%>

<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.hql.accounting.Account"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="com.krawler.spring.accounting.handler.AccountingHandlerDAO"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.krawler.spring.accounting.account.accAccountDAO"%>
<%@page import="com.krawler.spring.importFunctionality.ImportHandler"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFCell"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFRow"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFSheet"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFWorkbook"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="org.apache.poi.poifs.filesystem.POIFSFileSystem"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
        String filePath = request.getParameter("filepath") != null ? request.getParameter("filepath") : "";
        int type = request.getParameter("type") != null ? Integer.parseInt(request.getParameter("type")) : 0;
        
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain) || StringUtil.isNullOrEmpty(filePath)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password,subdomain,filepath] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);
            
            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
            ImportHandler importHandler = (ImportHandler) context.getBean("importHandler");
            accAccountDAO accAccountDAOobj = (accAccountDAO) context.getBean("accAccountDao");
            AccountingHandlerDAO accountingHandlerDAOobj = (AccountingHandlerDAO) context.getBean("accountHandlerDao");
            
            int sheetNo = 0;
            int maxCol = 0;
            String CHKLFieldComboID = "07535e3d-53e1-4756-bf87-24e2f338e783";
            String CHAFieldComboID = "31097d19-6046-49d7-ab4e-f14e42e9c72d";
            String CHGFieldComboID = "ead601ba-d99e-4c21-9991-4ee4ac39889f";
            String CHKGFieldComboID = "9c6340bb-18bf-4041-b6e0-10fd60edbfc9";
            String CHKLTFieldComboID = "b07ec072-9812-4cb7-9dee-ebf42a895336";
            
            if(type==1){
                //type = 1 : Import from Trial Balance shared by client
                int accountCodeIndex = -1;
                int CHKL_Debit_Index = -1, CHKL_Credit_Index = -1,
                    CHA_Debit_Index = -1, CHA_Credit_Index = -1, 
                    CHG_Debit_Index = -1, CHG_Credit_Index = -1, 
                    CHKG_Debit_Index = -1, CHKG_Credit_Index = -1, 
                    CHKLT_Debit_Index = -1, CHKLT_Credit_Index = -1, 
                        openingBalanceTypeIndex = -1;
                String accountCode = "", openingBalanceType = "";
                
                double CHKL_Debit = 0, CHKL_Credit = 0, 
                       CHA_Debit = 0, CHA_Credit = 0, 
                       CHG_Debit = 0, CHG_Credit = 0, 
                       CHKG_Debit = 0, CHKG_Credit = 0, 
                       CHKLT_Debit = 0, CHKLT_Credit = 0;
                
                out.println("Distributed Opening Balance from Trial Balance for following Account Codes - <br/><br/>");
                
                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                String cellHeaderName = importHandler.getCellValue(cell);
                                if (cellHeaderName.contains("Account Code") && accountCodeIndex == -1) {
                                    accountCodeIndex = cellcount;
                                } else if (cellHeaderName.contains("CHKL YTD Ending Amount Debit (Malaysian Ringgit (MYR))") && CHKL_Debit_Index == -1) {
                                    CHKL_Debit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHKL YTD Ending Amount Credit (Malaysian Ringgit (MYR))") && CHKL_Credit_Index == -1) {
                                    CHKL_Credit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHA YTD Ending Amount Debit (Malaysian Ringgit (MYR))") && CHA_Debit_Index == -1) {
                                    CHA_Debit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHA YTD Ending Amount Credit (Malaysian Ringgit (MYR))") && CHA_Credit_Index == -1) {
                                    CHA_Credit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHG YTD Ending Amount Debit (Malaysian Ringgit (MYR))") && CHG_Debit_Index == -1) {
                                    CHG_Debit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHG YTD Ending Amount Credit (Malaysian Ringgit (MYR))") && CHG_Credit_Index == -1) {
                                    CHG_Credit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHKG YTD Ending Amount Debit (Malaysian Ringgit (MYR))") && CHKG_Debit_Index == -1) {
                                    CHKG_Debit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHKG YTD Ending Amount Credit (Malaysian Ringgit (MYR))") && CHKG_Credit_Index == -1) {
                                    CHKG_Credit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHKLT YTD Ending Amount Debit (Malaysian Ringgit (MYR))") && CHKLT_Debit_Index == -1) {
                                    CHKLT_Debit_Index = cellcount;
                                } else if (cellHeaderName.contains("CHKLT YTD Ending Amount Credit (Malaysian Ringgit (MYR))") && CHKLT_Credit_Index == -1) {
                                    CHKLT_Credit_Index = cellcount;
                                }
                            }
                        }
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cellcount == accountCodeIndex) {
                                accountCode = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? importHandler.getCellValue(cell) : "";
                            } else if (cellcount == CHKL_Debit_Index) {
                                CHKL_Debit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKL_Credit_Index) {
                                CHKL_Credit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHA_Debit_Index) {
                                CHA_Debit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHA_Credit_Index) {
                                CHA_Credit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHG_Debit_Index) {
                                CHG_Debit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHG_Credit_Index) {
                                CHG_Credit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKG_Debit_Index) {
                                CHKG_Debit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKG_Credit_Index) {
                                CHKG_Credit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKLT_Debit_Index) {
                                CHKLT_Debit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKLT_Credit_Index) {
                                CHKLT_Credit = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            }
                        }
                        
                        if(!StringUtil.isNullOrEmpty(accountCode)){
                            boolean debitType = true;//Handle depending upon summation
                            
                            String query = "select id from account where acccode=? and company=(select companyid from company where subdomain=?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setString(1, accountCode);
                            pstmt.setString(2, subdomain);
                            ResultSet rs = pstmt.executeQuery();
                            if(rs.next()){
                                String accountID = rs.getString("id");
                                KwlReturnObject result = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                                Account account= (Account) result.getEntityList().get(0);

                                HashMap<String, Object> requestParams = new HashMap();
                                requestParams.put("companyid", "95d79cb9-3efa-49e6-b00e-9df20164cdbd");
                                requestParams.put("account", account);
                                requestParams.put("debitType", debitType);
                                
                                boolean debitTypeCHKL = true;
                                JSONArray distributedopeningbalancearray = new JSONArray();
                                double CHKLOpeningBalance = 0;
                                if(CHKL_Debit > 0){
                                    CHKLOpeningBalance = CHKL_Debit;
                                    debitTypeCHKL = true;
                                } 
                                if(CHKL_Credit > 0) {
                                    CHKLOpeningBalance = CHKL_Credit;
                                    debitTypeCHKL = false;
                                }
                                JSONObject jobj = new JSONObject();
                                jobj.put("comboid", CHKLFieldComboID);
                                jobj.put("distributedopeningbalanace", CHKLOpeningBalance);
                                jobj.put("debitType", debitTypeCHKL);
                                distributedopeningbalancearray.put(jobj);

                                boolean debitTypeCHA = true;
                                double CHAOpeningBalance = 0;
                                if(CHA_Debit > 0){
                                    CHAOpeningBalance = CHA_Debit;
                                    debitTypeCHA = true;
                                } 
                                if(CHA_Credit > 0) {
                                    CHAOpeningBalance = CHA_Credit;
                                    debitTypeCHA = false;
                                }
                                jobj = new JSONObject();
                                jobj.put("comboid", CHAFieldComboID);
                                jobj.put("distributedopeningbalanace", CHAOpeningBalance);
                                jobj.put("debitType", debitTypeCHA);
                                distributedopeningbalancearray.put(jobj);

                                boolean debitTypeCHG = true;
                                double CHGOpeningBalance = 0;
                                if(CHG_Debit > 0){
                                    CHGOpeningBalance = CHG_Debit;
                                    debitTypeCHG = true;
                                } 
                                if(CHG_Credit > 0) {
                                    CHGOpeningBalance = CHG_Credit;
                                    debitTypeCHG = false;
                                }
                                jobj = new JSONObject();
                                jobj.put("comboid", CHGFieldComboID);
                                jobj.put("distributedopeningbalanace", CHGOpeningBalance);
                                jobj.put("debitType",  debitTypeCHG);
                                distributedopeningbalancearray.put(jobj);

                                boolean debitTypeCHKG = true;
                                double CHKGOpeningBalance = 0;
                                if(CHKG_Debit > 0){
                                    CHKGOpeningBalance = CHKG_Debit;
                                    debitTypeCHKG = true;
                                } 
                                if(CHKG_Credit > 0) {
                                    CHKGOpeningBalance = CHKG_Credit;
                                    debitTypeCHKG = false;
                                }
                                jobj = new JSONObject();
                                jobj.put("comboid", CHKGFieldComboID);
                                jobj.put("distributedopeningbalanace", CHKGOpeningBalance);
                                jobj.put("debitType", debitTypeCHKG);
                                distributedopeningbalancearray.put(jobj);

                                boolean debitTypeCHKLT = true;
                                double CHKLTOpeningBalance = 0;
                                if(CHKLT_Debit > 0){
                                    CHKLTOpeningBalance = CHKLT_Debit;
                                    debitTypeCHKLT = true;
                                } 
                                if(CHKLT_Credit > 0) {
                                    CHKLTOpeningBalance = CHKLT_Credit;
                                    debitTypeCHKLT = false;
                                }
                                jobj = new JSONObject();
                                jobj.put("comboid", CHKLTFieldComboID);
                                jobj.put("distributedopeningbalanace", CHKLTOpeningBalance);
                                jobj.put("debitType", debitTypeCHKLT);
                                distributedopeningbalancearray.put(jobj);


                                requestParams.put("distributedopeningbalancearray", distributedopeningbalancearray);
                                
                                
                                /*if(!StringUtil.isNullOrEmpty(distributeddeletefield)){
                                    JSONArray distributeddeletefieldarray = new JSONArray(distributeddeletefield);
                                    requestParams.put("distributeddeletefieldarray", distributeddeletefieldarray);
                                }*/
                                accAccountDAOobj.distributeOpeningBalance(requestParams);


                                double openingBalance = (debitTypeCHKL ? CHKLOpeningBalance : -CHKLOpeningBalance) 
                                                        + (debitTypeCHA ? CHAOpeningBalance : -CHAOpeningBalance) 
                                                        + (debitTypeCHG ? CHGOpeningBalance : -CHGOpeningBalance)  
                                                        + (debitTypeCHKG ? CHKGOpeningBalance : -CHKGOpeningBalance) 
                                                        + (debitTypeCHKLT ? CHKLTOpeningBalance : -CHKLTOpeningBalance);

                                out.println("<br/>"+accountCode+", <b>"+openingBalance+"</b>, "
                                        +(debitTypeCHKL ? CHKLOpeningBalance : -CHKLOpeningBalance)+", "
                                        +(debitTypeCHA ? CHAOpeningBalance : -CHAOpeningBalance)+", "
                                        +(debitTypeCHG ? CHGOpeningBalance : -CHGOpeningBalance)+", "
                                        +(debitTypeCHKG ? CHKGOpeningBalance : -CHKGOpeningBalance)+", "
                                        +(debitTypeCHKLT ? CHKLTOpeningBalance : -CHKLTOpeningBalance));
                                
                                if(openingBalance!=0){
                                    String updatequery = "update account set openingbalance=? where id=?";
                                    PreparedStatement pstmtUpdate = conn.prepareStatement(updatequery);
                                    pstmtUpdate.setDouble(1, openingBalance);
                                    pstmtUpdate.setString(2, account.getID());
                                    int noofrows = pstmtUpdate.executeUpdate();
                                }
                                
                            }
                        }
                    }
                }
                
                
            }else{
                //type = 0 : Normal COA import as sample file shared to client
                out.println("Updated Distributed Opening Balance for following Account Codes - <br/><br/>");

                //String filePath = "/home/krawler/Desktop/SDP-7913/Group acc. Opening bal. 30.12.16 for Deskera (1).xls";
                
                int accountCodeIndex = -1;
                int totalOpeningBalanceIndex = -1;
                int CHKLOpeningBalanceIndex = -1, CHAOpeningBalanceIndex = -1, CHGOpeningBalanceIndex = -1, CHKGOpeningBalanceIndex = -1, 
                        CHKLTOpeningBalanceIndex = -1, openingBalanceTypeIndex = -1;
                String accountCode = "", openingBalanceType = "";
                double totalOpeningBalance = 0, CHKLOpeningBalance = 0, CHAOpeningBalance = 0, CHGOpeningBalance = 0, CHKGOpeningBalance = 0, CHKLTOpeningBalance = 0;
                

                POIFSFileSystem fs;
                fs = new POIFSFileSystem(new FileInputStream(filePath));
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(sheetNo);
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (i == 0) {
                        maxCol = row.getLastCellNum();
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);
                            if (cell != null) {
                                String cellHeaderName = importHandler.getCellValue(cell);
                                if (cellHeaderName.contains("Account Code") && accountCodeIndex == -1) {
                                    accountCodeIndex = cellcount;
                                } else if (cellHeaderName.contains("Opening Balance Type") && openingBalanceTypeIndex == -1) {
                                    openingBalanceTypeIndex = cellcount;
                                } else if (cellHeaderName.contains("Total Opening Balance") && totalOpeningBalanceIndex == -1) {
                                    totalOpeningBalanceIndex = cellcount;
                                } else if (cellHeaderName.contains("CHKL Opening Balance") && CHKLOpeningBalanceIndex == -1) {
                                    CHKLOpeningBalanceIndex = cellcount;
                                } else if (cellHeaderName.contains("CHA Opening Balance") && CHAOpeningBalanceIndex == -1) {
                                    CHAOpeningBalanceIndex = cellcount;
                                } else if (cellHeaderName.contains("CHG Opening Balance") && CHGOpeningBalanceIndex == -1) {
                                    CHGOpeningBalanceIndex = cellcount;
                                } else if (cellHeaderName.contains("CHKG Opening Balance") && CHKGOpeningBalanceIndex == -1) {
                                    CHKGOpeningBalanceIndex = cellcount;
                                } else if (cellHeaderName.contains("CHKLT Opening Balance") && CHKLTOpeningBalanceIndex == -1) {
                                    CHKLTOpeningBalanceIndex = cellcount;
                                }
                            }
                        }
                        out.println("<br/>Account Code, <b>Sum</b>, Total Opening Balance, CHKL Opening Balance, CHA Opening Balance, CHG Opening Balance, CHKG Opening Balance, CHKLT Opening Balance<br/>");
                    } else {
                        for (int cellcount = 0; cellcount < maxCol; cellcount++) {
                            HSSFCell cell = row.getCell(cellcount);

                            if (cellcount == accountCodeIndex) {
                                accountCode = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? importHandler.getCellValue(cell) : "";
                            } else if (cellcount == openingBalanceTypeIndex) {
                                openingBalanceType = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? importHandler.getCellValue(cell) : "";
                            } else if (cellcount == totalOpeningBalanceIndex) {
                                totalOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKLOpeningBalanceIndex) {
                                CHKLOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHAOpeningBalanceIndex) {
                                CHAOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHGOpeningBalanceIndex) {
                                CHGOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKGOpeningBalanceIndex) {
                                CHKGOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            } else if (cellcount == CHKLTOpeningBalanceIndex) {
                                CHKLTOpeningBalance = (cell != null && !StringUtil.isNullOrEmpty(importHandler.getCellValue(cell))) ? Double.parseDouble(importHandler.getCellValue(cell)) : 0;
                            }
                        }

                        if(!StringUtil.isNullOrEmpty(accountCode)){
                            boolean debitType = true;
                            if(openingBalanceType.equals("Credit")){
                                debitType = false;
                            }

                            String query = "select id from account where acccode=? and company=(select companyid from company where subdomain=?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setString(1, accountCode);
                            pstmt.setString(2, subdomain);
                            ResultSet rs = pstmt.executeQuery();
                            if(rs.next()){
                                String accountID = rs.getString("id");
                                KwlReturnObject result = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                                Account account= (Account) result.getEntityList().get(0);

                                HashMap<String, Object> requestParams = new HashMap();
                                requestParams.put("companyid", "95d79cb9-3efa-49e6-b00e-9df20164cdbd");
                                requestParams.put("account", account);
                                requestParams.put("debitType", debitType);
                                //if (!StringUtil.isNullOrEmpty(distributedopeningbalance)) {
                                    JSONArray distributedopeningbalancearray = new JSONArray();

                                    JSONObject jobj = new JSONObject();
                                    jobj.put("comboid", CHKLFieldComboID);
                                    jobj.put("distributedopeningbalanace", CHKLOpeningBalance);
                                    jobj.put("debitType", (CHKLOpeningBalance>=0) ? true : false);
                                    distributedopeningbalancearray.put(jobj);

                                    jobj = new JSONObject();
                                    jobj.put("comboid", CHAFieldComboID);
                                    jobj.put("distributedopeningbalanace", CHAOpeningBalance);
                                    jobj.put("debitType", (CHAOpeningBalance>=0) ? true : false);
                                    distributedopeningbalancearray.put(jobj);

                                    jobj = new JSONObject();
                                    jobj.put("comboid", CHGFieldComboID);
                                    jobj.put("distributedopeningbalanace", CHGOpeningBalance);
                                    jobj.put("debitType", (CHGOpeningBalance>=0) ? true : false);
                                    distributedopeningbalancearray.put(jobj);

                                    jobj = new JSONObject();
                                    jobj.put("comboid", CHKGFieldComboID);
                                    jobj.put("distributedopeningbalanace", CHKGOpeningBalance);
                                    jobj.put("debitType", (CHKGOpeningBalance>=0) ? true : false);
                                    distributedopeningbalancearray.put(jobj);

                                    jobj = new JSONObject();
                                    jobj.put("comboid", CHKLTFieldComboID);
                                    jobj.put("distributedopeningbalanace", CHKLTOpeningBalance);
                                    jobj.put("debitType", (CHKLTOpeningBalance>=0) ? true : false);
                                    distributedopeningbalancearray.put(jobj);


                                    requestParams.put("distributedopeningbalancearray", distributedopeningbalancearray);
                                //}
                                /*if(!StringUtil.isNullOrEmpty(distributeddeletefield)){
                                    JSONArray distributeddeletefieldarray = new JSONArray(distributeddeletefield);
                                    requestParams.put("distributeddeletefieldarray", distributeddeletefieldarray);
                                }*/
                                accAccountDAOobj.distributeOpeningBalance(requestParams);


                                double openingBalance = CHKLOpeningBalance + CHAOpeningBalance + CHGOpeningBalance + CHKGOpeningBalance + CHKLTOpeningBalance;

                                out.println("<br/>"+accountCode+", <b>"+openingBalance+"</b>, "+totalOpeningBalance+", "+CHKLOpeningBalance+", "+CHAOpeningBalance+", "+CHGOpeningBalance+", "+CHKGOpeningBalance+", "+CHKLTOpeningBalance);

                                if(openingBalance!=0){
                                    String updatequery = "update account set openingbalance=? where id=?";
                                    PreparedStatement pstmtUpdate = conn.prepareStatement(updatequery);
                                    pstmtUpdate.setDouble(1, openingBalance);
                                    pstmtUpdate.setString(2, account.getID());
                                    int noofrows = pstmtUpdate.executeUpdate();
                                }
                            }

                        }
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
