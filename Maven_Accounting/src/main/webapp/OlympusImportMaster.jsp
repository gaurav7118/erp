<%-- 
    Document   : OlympusImportMaster
    Created on : 23 Sep, 2015, 11:42:06 AM
    Author     : krawler
--%>

<%@page import="java.util.HashMap"%>
<%@page import="com.krawler.spring.accounting.product.accProductDAO"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="com.krawler.common.admin.User"%>
<%@page import="com.krawler.spring.auditTrailModule.auditTrailDAO"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.common.admin.ImportLog"%>
<%@page import="com.krawler.spring.storageHandler.storageHandlerImpl"%>
<%--<%@page import="java.nio.file.DirectoryNotEmptyException"%>--%>
<%--<%@page import="java.nio.file.NoSuchFileException"%>--%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.spring.accounting.handler.OlympusImportDataServiceDAO"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.filefilter.AgeFileFilter"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.io.File"%>
<%@page import="com.krawler.spring.accounting.handler.OlympusImportDataController"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
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
        String importfiledate = request.getParameter("date") != null ? request.getParameter("date") : "";
        
        out.println("<b>***** Olympus Import Master *****<b/><br/>");
        System.out.println("\n***** Olympus Import Master *****\n");
        
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain)) {
            out.println("Parameter missing from parameters => [serverip,dbname,username,password,subdomain] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            Date todayDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            if(!StringUtil.isNullOrEmpty(importfiledate)){
                try {
                    todayDate = sdf.parse(importfiledate);//To import files of particular date(last modilfied not file name date)
                } catch (ParseException pex) {
                    out.println("<br/><br/>Please provide date in format <b>\"yyyyMMdd\" e.g. 20151001</b><br/>");
                    System.out.println("\nPlease provide date in format \"yyyyMMdd\" e.g. 20151001\n");
                }                
            }
            
            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
            OlympusImportDataController olympusDataImportcontroller = (OlympusImportDataController) context.getBean("olympusDataImportcontroller");
            OlympusImportDataServiceDAO olympusImportDataServiceDAO = (OlympusImportDataServiceDAO) context.getBean("olympusImportDataServiceDAO");
            auditTrailDAO auditTrailObj = (auditTrailDAO) context.getBean("auditTraildao");
            accProductDAO accProductDAO = (accProductDAO) context.getBean("accProductDao");
            
            Calendar todayStart = Calendar.getInstance();
            todayStart.setTime(todayDate);
            todayStart.set(Calendar.HOUR_OF_DAY, 0);
            todayStart.set(Calendar.MINUTE, 0);
            todayStart.set(Calendar.SECOND, 0);
            todayStart.set(Calendar.MILLISECOND, 0);
            Date todayStartDate = todayStart.getTime();
            //If date is provided then it will import file(s) generated on that particular date otherwise it will import import file(s) generated yesterday instead of today.
            if(StringUtil.isNullOrEmpty(importfiledate)){
                todayStart.add(Calendar.DAY_OF_MONTH, -1);
                todayStartDate = todayStart.getTime();
            }
            
            out.println("<br/><br/>Import file for date(yyyyMMdd): <b>"+sdf.format(todayStartDate)+"<b/>");
            System.out.println("\nImport file for date(yyyyMMdd): "+sdf.format(todayStartDate)+"\n");
            
            Calendar todayEnd = Calendar.getInstance();
            todayEnd.setTime(todayDate);
            todayEnd.set(Calendar.HOUR_OF_DAY, 23);
            todayEnd.set(Calendar.MINUTE, 59);
            todayEnd.set(Calendar.SECOND, 59);
            todayEnd.set(Calendar.MILLISECOND, 999);
            Date todayEndDate = todayEnd.getTime();
            //If date is provided then it will import file(s) generated on that particular date otherwise it will import import file(s) generated yesterday instead of today.
            if(StringUtil.isNullOrEmpty(importfiledate)){
                todayEnd.add(Calendar.DAY_OF_MONTH, -1);
                todayEndDate = todayEnd.getTime();
            }
            
            String CUSTOMERBILLINGMASTER="CUSTOMERBILLINGMASTER";
            String customerBillingMaster="";
            boolean isCustomerBillingMasterDone = false;
            String CUSTOMERSHIPPINGMASTER="CUSTOMERSHIPPINGMASTER";
            String customerShippingMaster="";
            boolean isCustomerShippingMasterDone = false;
            String PRODUCTMASTER="PRODUCTMASTER";
            String productMaster="";//"wd.materialmasterdata";
            boolean isProductDone = false;
            String LICENSEMASTER="LICENSEMASTER";
            String licenseMaster="";//"wd.licensedata_ZH01";
            boolean isLicenseMasterDone = false;
            String SUPPLLICENSE1="SUPPLLICENSE1";
            String licenseMasterSupp1="";//"wd.licensedata_ZHS1";
            boolean isLicenseMasterSupp1Done = false;
            String SUPPLLICENSE2="SUPPLLICENSE2";
            String licenseMasterSupp2="";//"wd.licensedata_Z0N1";
            boolean isLicenseMasterSupp2Done = false;
            String STOCKMOVEMENT1 ="STOCKMOVEMENT1";
            String stockMovement1 ="";//"wd.a411stock";
            boolean isStockMovement1Done = false;
            String STOCKMOVEMENT2 ="STOCKMOVEMENT2";
            String stockMovement2 ="";//"wd.assetdata";
            boolean isStockMovement2Done = false;
            String STOCKMOVEMENT3 ="STOCKMOVEMENT3";
            String stockMovement3 ="";//"wd.EXPcost";
            boolean isStockMovement3Done = false;
            String STOCKMOVEMENT4 ="STOCKMOVEMENT4";
            String stockMovement4 ="";
            boolean isStockMovement4Done = false;
            String olympusfilepath = "";//"/home/krawler/store/Accounting/importfiles/olympusfilepath";//rs.getString("olympusfilepath");
            String filepath = "";//"/home/krawler/store/Accounting/importfiles";//rs.getString("filepath");
            String processedfilepath = "";
            
            String query = "";
            query = "select olympus_importinfo.*, company.creator from olympus_importinfo "
                    + "inner join company on company.subdomain=olympus_importinfo.subdomain "
                    + "where olympus_importinfo.subdomain = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, subdomain);
            ResultSet rs1 = pstmt.executeQuery();
            if(rs1.next()){
                customerBillingMaster = rs1.getString("customerbilling");
                customerShippingMaster = rs1.getString("customershipping");
                productMaster = rs1.getString("productmaster");
                licenseMaster = rs1.getString("licensemaster");
                licenseMasterSupp1 = rs1.getString("supp1");
                licenseMasterSupp2 = rs1.getString("supp2");
                stockMovement1 = rs1.getString("stockmovement1");
                stockMovement2 = rs1.getString("stockmovement2");
                stockMovement3 = rs1.getString("stockmovement3");
                stockMovement4 = rs1.getString("stockmovement4");
                olympusfilepath = rs1.getString("olympusfilepath");
                filepath = rs1.getString("filepath");
                processedfilepath = rs1.getString("processedfilepath");
                
                String creator = rs1.getString("creator");
                request.getSession().setAttribute("userid", creator);
                KwlReturnObject jeresult = accProductDAO.getObject(User.class.getName(), creator);
                User user = (User) jeresult.getEntityList().get(0);
                
                File destFile = new File(filepath);
                if (!destFile.exists()) { //Create dir. if not present
                    destFile.mkdirs();
                }
                
                File sourceFile = new File(olympusfilepath);
                FileFilter fileFilter = new AgeFileFilter(todayStartDate, false);
                File[] files = sourceFile.listFiles(fileFilter);
                if(files.length<=0){
                    out.println("<br/><br/>Sorry! no files to import.<br/>");
                    System.out.println("\n\nSorry! no files to import.\n");
                }else{
                    for (File file : files) {
                        Date lastMod = new Date(file.lastModified());
                        if(todayStartDate.getTime()<=lastMod.getTime() && todayEndDate.getTime()>=lastMod.getTime()){
                            String filename = file.getName();
                            File orgfile = null;
                            File cpfile = null;
                            orgfile = new File(olympusfilepath+"/"+filename);
                            String processfilepath = filepath+"/"+filename;
                            cpfile = new File(processfilepath);
                            //Copy file to particular dir.
                            if (orgfile != null && cpfile != null) {
                                olympusDataImportcontroller.copyFile(orgfile, cpfile);
                            }
/*
                            String scriptinfo = "update olympus_importscriptinfo set filepath = ? where id = ? and subdomain = ?";
                            pstmt = conn.prepareStatement(scriptinfo);
                            pstmt.setString(1, processfilepath);
                            pstmt.setString(3, subdomain);
*/                            //Copy Customer Billing Master File
                            if(filename.contains(customerBillingMaster)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, CUSTOMERBILLINGMASTER, isCustomerBillingMasterDone);
                                isCustomerBillingMasterDone = true;
                                //pstmt.setString(2, CUSTOMERBILLINGMASTER);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Customer Shipping Master File
                            if(filename.contains(customerShippingMaster)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, CUSTOMERSHIPPINGMASTER, !isCustomerShippingMasterDone);
                                isCustomerShippingMasterDone = true;
                                //pstmt.setString(2, CUSTOMERSHIPPINGMASTER);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Product Master File
                            if(filename.contains(productMaster)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, PRODUCTMASTER, !isProductDone);
                                isProductDone = true;
                                //pstmt.setString(2, PRODUCTMASTER);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy License Master File
                            if(filename.contains(licenseMaster)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, LICENSEMASTER, !isLicenseMasterDone);
                                isLicenseMasterDone = true;
                                //pstmt.setString(2, LICENSEMASTER);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy License Master Supp 1 File
                            if(filename.contains(licenseMasterSupp1)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, SUPPLLICENSE1, !isLicenseMasterSupp1Done);
                                isLicenseMasterSupp1Done = true;
                                //pstmt.setString(2, SUPPLLICENSE1);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy License Master Supp 2 File
                            if(filename.contains(licenseMasterSupp2)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, SUPPLLICENSE2, !isLicenseMasterSupp2Done);
                                isLicenseMasterSupp2Done = true;
                                //pstmt.setString(2, SUPPLLICENSE2);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Stock Master 1 File
                            if(filename.contains(stockMovement1)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, STOCKMOVEMENT1, !isStockMovement1Done);
                                isStockMovement1Done = true;
                                //pstmt.setString(2, STOCKMOVEMENT1);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Stock Master 2 File
                            if(filename.contains(stockMovement2)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, STOCKMOVEMENT2, !isStockMovement2Done);
                                isStockMovement2Done = true;
                                //pstmt.setString(2, STOCKMOVEMENT2);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Stock Master 3 File
                            if(filename.contains(stockMovement3)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, STOCKMOVEMENT3, !isStockMovement3Done);
                                isStockMovement3Done = true;
                                //pstmt.setString(2, STOCKMOVEMENT3);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            //Copy Stock Master 4 File
                            if(filename.contains(stockMovement4)){
                                olympusImportDataServiceDAO.addImportFilePath(processfilepath, subdomain, STOCKMOVEMENT4, !isStockMovement4Done);
                                isStockMovement4Done = true;
                                //pstmt.setString(2, STOCKMOVEMENT4);
                                //pstmt.execute();

                                //Copy file to particular dir.
                                //if(orgfile!=null && cpfile!=null){
                                //    olympusDataImportcontroller.copyFile(orgfile, cpfile);                                    
                                //}
                                }
                            }
                        }
                    }
                
                File processFile = null;
                File xlsfilepath = null;
                File processedFile = null;
                File failureFile = null;
                File processedfailureFile = null;
                
                String baseUrl="";
                if(!StringUtil.isNullOrEmpty(request.getParameter("cdomain"))){
                    baseUrl = URLUtil.getPageURL(request, com.krawler.esp.web.resource.Links.loginpageFull);
                }else{
                    baseUrl = olympusDataImportcontroller.getPageURLForCron(request, com.krawler.esp.web.resource.Links.loginpageFull);
                }
                
                String moduleName = "";
                query = "select paths.filepath AS filepath, scripts.id AS id from olympus_importscriptinfo scripts "
                        + " INNER JOIN olympus_import_filepath paths ON paths.recid = scripts.recid"
                        + " WHERE scripts.subdomain = ? order by scripts.sequence ";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, subdomain);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    JSONObject jobj = new JSONObject();
                    filepath = rs.getString("filepath");
                    String id = rs.getString("id");
                    if(id.equals(CUSTOMERBILLINGMASTER) && isCustomerBillingMasterDone){
                        jobj = olympusImportDataServiceDAO.importCustomer(filepath, baseUrl, true, subdomain);
                        out.println("<br/><br/>***** Customer Billing *****");
                        System.out.println("\n\n***** Customer Billing *****");
                        moduleName = Constants.CUSTOMERBILLINGMASTER_Module;
                    }else if(id.equals(CUSTOMERSHIPPINGMASTER) && isCustomerShippingMasterDone){
                        jobj = olympusImportDataServiceDAO.importCustomer(filepath, baseUrl, false, subdomain);
                        out.println("<br/><br/>***** Customer Shipping *****");
                        System.out.println("\n\n***** Customer Shipping *****");
                        moduleName = Constants.CUSTOMERSHIPPINGMASTER_Module;
                    }else if(id.equals(PRODUCTMASTER) && isProductDone){
                        jobj = olympusImportDataServiceDAO.importProducts(filepath, baseUrl, subdomain);
                        out.println("<br/><br/>***** Product Master *****");
                        System.out.println("\n\n***** Product Master *****");
                        moduleName = Constants.PRODUCTMASTER_Module;
                    }else if(id.equals(LICENSEMASTER) && isLicenseMasterDone){
                        jobj = olympusImportDataServiceDAO.importMasterLicense(filepath, baseUrl, subdomain);
                        out.println("<br/><br/>***** License Master *****");
                        System.out.println("\n\n***** License Master *****");
                        moduleName = Constants.LICENSEMASTER_Module;
                    }else if(id.equals(SUPPLLICENSE1) && isLicenseMasterSupp1Done){
                        jobj = olympusImportDataServiceDAO.importSupplimenteryLicense(filepath, baseUrl, subdomain, true);
                        out.println("<br/><br/>***** License Master Supp. 1 *****");
                        System.out.println("\n\n***** License Master Supp. 1 *****");
                        moduleName = Constants.SUPPLLICENSE1_Module;
                    }else if(id.equals(SUPPLLICENSE2) && isLicenseMasterSupp2Done){
                        jobj = olympusImportDataServiceDAO.importSupplimenteryLicense(filepath, baseUrl, subdomain, false);
                        out.println("<br/><br/>***** License Master Supp. 2 *****");
                        System.out.println("\n\n***** License Master Supp. 2 *****");
                        moduleName = Constants.SUPPLLICENSE2_Module;
                    }else if(id.equals(STOCKMOVEMENT1) && isStockMovement1Done){
                        jobj = olympusImportDataServiceDAO.importStockMovementInData(request, filepath, subdomain, "1", baseUrl);
                        out.println("<br/><br/>***** Stock Movement 1 *****");
                        System.out.println("\n\n***** Stock Movement 1 *****");
                        moduleName = Constants.STOCKMOVEMENT1_Module;
                    }else if(id.equals(STOCKMOVEMENT2) && isStockMovement2Done){
                        jobj = olympusImportDataServiceDAO.importStockMovementInData(request, filepath, subdomain, "2", baseUrl);
                        out.println("<br/><br/>***** Stock Movement 2 *****");
                        System.out.println("\n\n***** Stock Movement 2 *****");
                        moduleName = Constants.STOCKMOVEMENT2_Module;
                    }else if(id.equals(STOCKMOVEMENT3) && isStockMovement3Done){
                        jobj = olympusImportDataServiceDAO.importStockMovementInData(request, filepath, subdomain, "3", baseUrl);
                        out.println("<br/><br/>***** Stock Movement 3 *****");
                        System.out.println("\n\n***** Stock Movement 3 *****");
                        moduleName = Constants.STOCKMOVEMENT3_Module;
                    }else if(id.equals(STOCKMOVEMENT4) && isStockMovement4Done){
                        jobj = olympusImportDataServiceDAO.importStockMovementInData(request, filepath, subdomain, "4", baseUrl);
                        out.println("<br/><br/>***** Stock Movement 4 *****");
                        System.out.println("\n\n***** Stock Movement 4 *****");
                        moduleName = Constants.STOCKMOVEMENT4_Module;
                    }
                    if(jobj.has("failedCnt") && jobj.has("total") && jobj.has("processfilepath") && jobj.has("xlsfilepath") && jobj.has("failuefilepath")){
                        try {
                            processFile = new File(jobj.getString("processfilepath"));
                            xlsfilepath = new File(jobj.getString("xlsfilepath"));
                            String processFileName = processFile.getName();
                            String processedFileName = processedfilepath+"/"+processFileName;
                            processedFile = new File(processedFileName);
                            File processedFileDir = new File(processedfilepath);
                            if (!processedFileDir.exists()) { //Create xls file's folder if not present
                                processedFileDir.mkdirs();
                            }         
                            olympusDataImportcontroller.copyFile(processFile, processedFile); 

                            //Delete the file csv/txt & xls file from filepath dir.
                            processFile.delete();
                            xlsfilepath.delete();
                            
                            out.println("<br/>Total no. of records: "+jobj.getInt("total"));
                            System.out.println("\nTotal no. of records: "+jobj.getInt("total"));
                            out.println("<br/>Success: "+(jobj.getInt("total") - jobj.getInt("failedCnt")));
                            System.out.println("\nSuccess: "+(jobj.getInt("total") - jobj.getInt("failedCnt")));
                            out.println("<br/>Failure: "+jobj.getInt("failedCnt"));
                            System.out.println("\nFailure: "+jobj.getInt("failedCnt"));

                            if((jobj.getInt("failedCnt")>0)){
                                String failuefilepath = jobj.getString("failuefilepath");
                                failuefilepath = failuefilepath.substring(failuefilepath.lastIndexOf("/")+1);
                                String ext = failuefilepath.substring(failuefilepath.lastIndexOf("."));
                                String failureFileName= failuefilepath.substring(0, failuefilepath.lastIndexOf("."));
                                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                                String failureFileStr = destinationDirectory + File.separator + failureFileName + ImportLog.failureTag + ext;
                                failureFile = new File(failureFileStr);
                                String processedfailureFileStr = processedfilepath + File.separator + failureFileName + ImportLog.failureTag + ext;
                                processedfailureFile = new File(processedfailureFileStr);
                                olympusDataImportcontroller.copyFile(failureFile, processedfailureFile); 
                            } 
                            
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userid", creator);
                            requestParams.put("reqHeader", request.getHeader("x-real-ip"));
                            requestParams.put("remoteAddress", request.getRemoteAddr());
                            String auditMessage = "imported " + processFileName + " for " + moduleName + " through scheduled job.";
                            if (!StringUtil.isNullOrEmpty(auditMessage)) {
                                auditMessage = "User " + user.getFullName() + " has " + auditMessage;
//                                auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.IMPORT_MASTER, auditMessage, request, "");
                               auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.IMPORT_MASTER, auditMessage, requestParams, "");
                            }
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            out.println(ex.getMessage());
                        }
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if(pstmt!=null){
            pstmt.close();
        }
        if(conn!=null){
            conn.close();
        }
    }
%>
