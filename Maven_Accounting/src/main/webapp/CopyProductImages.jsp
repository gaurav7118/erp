<%-- 
    Document   : OlympusImportMaster
    Created on : 23 Sep, 2015, 11:42:06 AM
    Author     : krawler
--%>

<%@page import="java.io.IOException"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.common.admin.ImportLog"%>
<%@page import="com.krawler.spring.storageHandler.storageHandlerImpl"%>
<%--<%@page import="java.nio.file.DirectoryNotEmptyException"%>--%>
<%--<%@page import="java.nio.file.NoSuchFileException"%>--%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="java.io.FileFilter"%>
<%@page import="java.io.File"%>
<%@page import="com.krawler.spring.accounting.handler.OlympusImportDataController"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
        try {
                String sourcepath = null;
                String destinationpath = null;
                Boolean copycheck = true;
                sourcepath = storageHandlerImpl.GetDocStorePath() + "ProductImages/";
                destinationpath = storageHandlerImpl.GetProfileImgStorePath() + "ProductImages/";

                //ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
                OlympusImportDataController olympusDataImportcontroller = new OlympusImportDataController(); //(OlympusImportDataController) context.getBean("olympusDataImportcontroller");

                File sourcefolder = new File(sourcepath);
                File[] listOfFiles = sourcefolder.listFiles();
                File destinationfolder = new File(destinationpath);
                if (!destinationfolder.exists()) { //Create file's folder if not present
                        destinationfolder.mkdirs();
                    }
                
                String filename = "";
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        filename = listOfFiles[i].getName();
                        File sourcefilename = null;
                        File destinationfilename = null;
                        sourcefilename = new File(sourcepath + filename);
                        destinationfilename = new File(destinationpath + filename);
                        olympusDataImportcontroller.copyFile(sourcefilename, destinationfilename);
                        System.out.println("File " + listOfFiles[i].getName());
                        out.println("<br>File " + listOfFiles[i].getName()+"</br>");
                    } else {
                        System.out.println("File " + listOfFiles[i].getName() + "is not copied");
                        out.println("File " + listOfFiles[i].getName() + "is not copied");
                        copycheck = false;
                        break;
                    }
                }

                if (copycheck) {
                    out.println("<br>Number of Images copied to Destination : "+listOfFiles.length );
                }else{
                    out.println("<br>Failed to copy the images");
                }
            
        } catch (IOException e) {
            e.printStackTrace();
            out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            out.println(e.getMessage());
        }
%>
