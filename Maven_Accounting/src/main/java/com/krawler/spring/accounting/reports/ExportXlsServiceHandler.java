/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  0in 2110-1301, USA.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import javax.mail.MessagingException;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.SessionFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class ExportXlsServiceHandler implements MessageSourceAware{
    
    private HibernateTemplate hibernateTemplate;
    private authHandlerDAO authHandlerDAOObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccCostCenterDAO accCostCenterObj;
    private accProductDAO accProductObj;

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterObj) {
        this.accCostCenterObj = accCostCenterObj;
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    
  public void GenerateXLSFile(JSONObject paramJobj, JSONObject jobj, ProductExportDetail productExportDetail,String filename,String fileType ) throws ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        JSONObject grid = null;
        int totalProducts = 0;
        boolean isSummaryXls = false;
        if (paramJobj.optString("isSummaryXls",null) != null) {
            isSummaryXls = Boolean.parseBoolean(paramJobj.optString("isSummaryXls"));
        }
        try {
            fileType = paramJobj.optString("filetype");
            if (paramJobj.optString("gridconfig",null) != null && !"undefined".equals(paramJobj.optString("gridconfig"))) {
                String gridconfig = paramJobj.optString("gridconfig");
                String get = paramJobj.optString("get",null) == null ? "" : paramJobj.optString("get");
                if (get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) { //Aged Receivable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridCustomer/AccName", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridIno", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.3", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22")) { //Aged Payable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridVendor/AccName", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridVIno", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.4", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("914")) {
                    gridconfig = "{"
                            + "data:[{'header':'productname','title':'" + messageSource.getMessage("acc.saleByItem.gridProduct", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'150','align':''},{'header':'productdescription','title':'Product Description','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.saleByItem.gridInvoice", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.saleByItem.gridDate", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'date'},{'header':'memo','title':'" + messageSource.getMessage("acc.saleByItem.gridMemo", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'personname','title':'" + messageSource.getMessage("acc.saleByItem.gridCustName", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'promisedDate','title':'Promised Date','width':'100','align':'date'},{'header':'quantity','title':'" + messageSource.getMessage("acc.saleByItem.gridQty", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':''},{'header':'rateinbase','title':'" + messageSource.getMessage("acc.saleByItem.gridSalesPrice", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'rowcurrency'},{'header':'amount','title':'" + messageSource.getMessage("acc.saleByItem.gridAmount", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'rowcurrency'},{'header':'totalsales','title':'" + messageSource.getMessage("acc.saleByItem.gridBalance", null, Locale.forLanguageTag(paramJobj.getString("language"))) + "','width':'100','align':'rowcurrency'},{'header':'status','title':'Status','width':'100','align':''}],"
                            + "groupdata:{'groupBy':'productname','groupSummaryField':'amount','groupSummaryText':'" + messageSource.getMessage("acc.nee.5", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " ','reportSummaryField':'amount','reportSummaryText':'" + messageSource.getMessage("acc.nee.6", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " '}"
                            + "}";
                }
                if (paramJobj.optString("gridconfig",null) != null) {
                    grid = (JSONObject) paramJobj.getJSONObject("gridconfig");
                } else {
                    grid = new JSONObject(gridconfig);
                }
            } else if (paramJobj.optString("gridconfig",null) != null) {
                grid = (JSONObject) paramJobj.getJSONObject("gridconfig");
            }

            int report = !StringUtil.isNullOrEmpty(paramJobj.optString("get",null)) ? Integer.parseInt(paramJobj.getString("get")) : 0;
            JSONArray gridmap = null;
            String colHeader = "";
            if (report == 772 || report == 773 || report == 775) {
                gridmap = grid.has("data") ? grid.getJSONArray("data") : null;
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    colHeader += StringUtil.serverHTMLStripper(temp.getString("header"));
                    if (colHeader.indexOf("*") != -1) {
                        colHeader = colHeader.substring(0, colHeader.indexOf("*") - 1) + ",";
                    } else {
                        colHeader += ",";
                    }
                }
            }
            
             
             String dataIndex="";
             String aligns="";
             if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport ) {
                gridmap =grid.has("headers") ?  grid.getJSONArray("headers") : null;
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    dataIndex += StringUtil.serverHTMLStripper(temp.getString("name"));
                    if (dataIndex.indexOf("*") != -1) {
                        dataIndex = dataIndex.substring(0, dataIndex.indexOf("*") - 1) + ",";
                    } else {
                        dataIndex += ",";
                    }
                    aligns+="none" + ",";
                }
                jobj.put("dataIndexes",dataIndex);
                jobj.put("aligns",aligns);
            }
             
            if (StringUtil.equal(fileType, "xls") || StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet       
                byte[] bytes = null;
                HSSFWorkbook workBook = null;
                workBook = createExcelFile(paramJobj, jobj, colHeader, filename, fileType,productExportDetail);
//                if (totalProducts < 1000) {
////                    writeXLSDataToFile(filename, fileType, workBook, response);
////                    writeXLSDataToFile(filename, fileType, workBook);
//                } else {
//                    createExcelExportFile(workBook, fileType, productExportFileName);
//                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
   
    public void createExcelExportFile(Workbook workbook, String ext, String fileName) {
        String destinationDirectory;
        ext = "." + ext;
        FileOutputStream fileOut = null;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath();
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = fileName.substring(fileName.lastIndexOf("."));
            }
            fileOut = new FileOutputStream(destinationDirectory + "/" + fileName + ext);
            workbook.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception ex) {
            System.out.println("\nExport file write [success/failed] " + ex);
        }
    }
    
    public HSSFWorkbook createExcelFile(JSONObject paramJObj, JSONObject obj, String colHeader,String filename,String fileType,ProductExportDetail productExportDetail) throws ServiceException, SessionExpiredException, JSONException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(paramJObj.getString(Constants.dateformatid), paramJObj.getString(Constants.timezonedifference), true);
        DateFormat frmt = authHandler.getDateOnlyFormat();
        DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj);   //date formatter for Sample file download
        DateFormat dbFormat = authHandler.getDateWithTimeFormat();                          //Database date format
        HSSFWorkbook wb = null;
        HSSFSheet sheet = null;
        boolean isCurrencyCode = false;
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        boolean isFromDocumentDesigner = false;
        try {
            String storePath = StorageHandler.GetDocStorePath();
            String companyid = paramJObj.optString(Constants.companyKey);
            String filePath = storePath + filename + "." + fileType;
            File destDir = new File(filePath);
            FileOutputStream oss = new FileOutputStream(destDir, true);

            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJObj.getString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            int report = !StringUtil.isNullOrEmpty(paramJObj.optString("get", null)) ? Integer.parseInt(paramJObj.getString("get")) : -1;
            boolean isSampleFile = obj.optString("isSampleFile").equals("T");
            String module = paramJObj.optString("moduleId", null) != null ? paramJObj.optString("moduleId") : "0000";
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String tit="";
            String head = "";
            String algn = "";
            isFromDocumentDesigner = obj.optBoolean("isFromDocumentDesigner", false);
            if (paramJObj.optString("header", null) != null) {
                 tit = paramJObj.optString("title");
                head = paramJObj.optString("header");
                  algn = paramJObj.optString("align");
                if (report == 772 || report == 773 || report == 775) {
                    /*
                        772 - Dimensional pnl report
                    773 - Dimensional BL
                    775 - Dimensional trial balance
                    */
                    tit = colHeader;
                    if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport) {
                        head = obj.getString("dataIndexes");
                        algn = obj.getString("aligns");
                }
                }else{
                
                 tit = paramJObj.optString("title");
                 head = paramJObj.optString("header");
                 algn = paramJObj.optString("align");
                }
                try {
                    tit = StringUtil.DecodeText(tit);
                } catch (IllegalArgumentException e) {    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) algn.split(",");
                if (report == 251) {
                    ArrayList<String> headerList = new ArrayList<String>();
                    Collections.addAll(headerList, headers);
                    ArrayList<String> titleList = new ArrayList<String>();
                    Collections.addAll(titleList, titles);

                    titleList.remove("View Details");
                    headerList.remove("type");

                    headers = headerList.toArray(new String[headerList.size()]);
                    titles = titleList.toArray(new String[titleList.size()]);
                }
            } else {
                headers = (String[]) obj.getString("header").split(",");
                titles = (String[]) obj.getString("title").split(",");
                align = (String[]) obj.getString("align").split(",");
            }
            String currencyid = paramJObj.getString(Constants.globalCurrencyKey);
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            cell = writeFilterDetailsInExcel(paramJObj, sheet, cell, rownum, cellnum);
            rownum += cell.getRow().getRowNum() + 1;
            HSSFRow headerRow = sheet.createRow(rownum++);
            JSONArray repArr = obj.getJSONArray("data");
            //companyid = paramJObj.optString("companyids", null);

            //Insert Headers
            cellnum = 0;
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (h < headers.length - 1) {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                    } else {
                        cell.setCellValue(headerStr);
                    }
                } else {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                    } else {
                        cell.setCellValue(headerStr);
                    }
                }
            }//headers loop

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row = sheet.createRow(rownum++);
                JSONObject temp = repArr.getJSONObject(t);
                String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
                String transactionCurrency = temp.has("transactioncurrencyid") ? temp.getString("transactioncurrencyid") : currencyid;
                for (int h = 0; h < headers.length; h++) {
                    if (h < headers.length - 1) {
                        cell = row.createCell(cellnum++);  //Create new cell
                        if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } /*
                         * Apply currency render to Amount in transaction
                         * currency in gst form 5
                         */ else if (align[h].equals("rowcurrencyGstForm") && !temp.optString(headers[h], "").equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), transactionCurrency, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("transactioncurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                            if (isSampleFile) {// sample file
                                try {
                                    String d1 = userFormat.format(dbFormat.parse(temp.getString(headers[h])));
                                    cell.setCellValue(d1);
                                } catch (JSONException | ParseException ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                try {
                                    String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                    cell.setCellValue(d1);
                                } catch (JSONException | ParseException ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no") ? temp.getString("no") : "");
                            } else if (headers[h].equals("invoicedate")) {
                                try {
                                    cell.setCellValue(formatter.format(frmt.parse(temp.getString("date"))));
                                } catch (Exception ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if (headers[h].equals("c_date")) {
                                cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))));
                            } else if (headers[h].equals("c_accountname")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")));
                            } else if (headers[h].equals("c_acccode")) {
                                String accCode = "";
                                if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                    accCode = temp.getString("c_acccode");
                                } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                    accCode = temp.getString("d_acccode");
                                }
                                cell.setCellValue(accCode);
                            } else if (headers[h].equals("c_transactionID")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_transactionID")) ? temp.getString("d_transactionID") : temp.getString("c_transactionID")));
                            } else if (headers[h].equals("c_checkno")) {
                                cell.setCellValue((temp.has("c_checkno") && StringUtil.isNullOrEmpty(temp.optString("c_checkno", "")) ? temp.optString("d_checkno", "") : temp.optString("c_checkno", "")));
                            } else if (headers[h].equals("c_amountAccountCurrency")) {
                                cell.setCellValue((temp.has("c_amountAccountCurrency") ? (StringUtil.isNullOrEmpty(temp.getString("c_amountAccountCurrency")) ? "" : temp.getString("c_amountAccountCurrency")) : ""));
                            } else if (headers[h].equals("c_transactionDetails")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_transactionDetails")) ? temp.getString("d_transactionDetails") : temp.getString("c_transactionDetails")));
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h])) && report != 117) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")));
                            } else if (headers[h].equals("c_transactionDetailsBankBook")) {
                                if (!temp.getString("d_transactionDetailsBankBook").equalsIgnoreCase("Transfer")) {
                                    cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsBankBook")) ? temp.getString("c_transactionDetailsBankBook") : temp.getString("d_transactionDetailsBankBook")));
                                } else {
                                    cell.setCellValue(temp.getString("c_transactionDetailsBankBook"));
                                }
                            } else if (headers[h].equals("d_date")) {
                                if (!temp.optString("d_date").equals("") || !temp.optString("c_date").equals("")) {
                                    cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                } else {
                                    cell.setCellValue("");
                                }

                            } else if (headers[h].equals("d_accountname")) {
                                String d_accountname = "";
                                if (temp.has("d_accountname") && !StringUtil.isNullOrEmpty(temp.getString("d_accountname"))) {
                                    d_accountname = temp.getString("d_accountname");
                                } else if (temp.has("c_accountname") && !StringUtil.isNullOrEmpty(temp.getString("c_accountname"))) {
                                    d_accountname = temp.getString("c_accountname");
                                }
                                cell.setCellValue(d_accountname);
                            } else if (headers[h].equals("d_reconciledate")) {
                                String d_reconciledate = "";
                                if (temp.has("d_reconciledate") && !StringUtil.isNullOrEmpty(temp.getString("d_reconciledate"))) {
                                    d_reconciledate = temp.getString("d_reconciledate");
                                } else if (temp.has("c_reconciledate") && !StringUtil.isNullOrEmpty(temp.getString("c_reconciledate"))) {
                                    d_reconciledate = temp.getString("c_reconciledate");
                                }
                                try {
                                    cell.setCellValue(formatter.format(frmt.parse(d_reconciledate)));
                                } catch (ParseException pe) {
                                    cell.setCellValue("");
                                }
                            } else if (headers[h].equals("d_acccode")) {
                                String accCode = "";
                                if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                    accCode = temp.getString("d_acccode");
                                } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                    accCode = temp.getString("c_acccode");
                                }
                                cell.setCellValue(accCode);
                            } else if (headers[h].equals("d_transactionID")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_transactionID")) ? temp.getString("c_transactionID") : temp.getString("d_transactionID")));
                            } else if (headers[h].equals("d_amount")) {
                                String d_amount = "";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                }
                                cell.setCellValue(d_amount);
                            } else if (headers[h].equals("d_amountinacc")) {
                                String amountinacc = "";
                                if (temp.has("d_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("d_amountinacc"))) {
                                    amountinacc = temp.getString("d_amountinacc");
                                } else if (temp.has("c_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("c_amountinacc"))) {
                                    amountinacc = temp.getString("c_amountinacc");
                                }
                                cell.setCellValue(amountinacc);
                            } else if (headers[h].equals("d_amountintransactioncurrency")) {
                                String amountintransactioncurrency = "";
                                if (temp.has("d_amountintransactioncurrency") && !StringUtil.isNullOrEmpty(temp.getString("d_amountintransactioncurrency"))) {
                                    amountintransactioncurrency = temp.getString("d_amountintransactioncurrency");
                                } else if (temp.has("c_amountintransactioncurrency") && !StringUtil.isNullOrEmpty(temp.getString("c_amountintransactioncurrency"))) {
                                    amountintransactioncurrency = temp.getString("c_amountintransactioncurrency");
                                }
                                cell.setCellValue(amountintransactioncurrency);
                            } else if (headers[h].equals("d_transactionDetails")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_transactionDetails")) ? temp.getString("c_transactionDetails") : temp.getString("d_transactionDetails")));
                            } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")));
                            } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period"))) {
                                String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                                if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                    tempString = "0.0";
                                }
                                tempString = withoutCurrencyRender(tempString, companyid);
                                tempString = tempString.replaceAll(",", "");
                                if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                    tempString = tempString.replaceAll("\\(", "");
                                    tempString = tempString.replaceAll("\\)", "");
                                    tempString = "-" + tempString;
                                }
                                try {
                                    cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                    double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                    cell.setCellValue(curr);
                                } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(tempString);
                                }
                            } else if (report == 1123 && (headers[h].equals("costprice") || headers[h].equals("unitprice") || headers[h].equals("amountcost") || headers[h].equals("amountsales"))) {
                                String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                                if (tempString == null || tempString.equals("")) {
                                    tempString = "0.0";
                                }
                                tempString = withoutCurrencyRender(tempString, companyid);
                                tempString = tempString.replaceAll(",", "");
                                if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                    tempString = tempString.replaceAll("\\(", "");
                                    tempString = tempString.replaceAll("\\)", "");
                                    tempString = "-" + tempString;
                                }
                                try {
                                    cell.setCellType(0);
                                    double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                    cell.setCellValue(curr);
                                } catch (Exception e) {
                                    cell.setCellValue(tempString);
                                }
                            } else if (report == 116 && (headers[h].equals("ytd_c_amount_open") || headers[h].equals("ytd_d_amount_open") || headers[h].equals("c_amount") || headers[h].equals("d_amount"))) {
                                String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                                if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                    tempString = "0.0";
                                }
                                tempString = withoutCurrencyRender(tempString, companyid);
                                tempString = tempString.replaceAll(",", "");
                                if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                    tempString = tempString.replaceAll("\\(", "");
                                    tempString = tempString.replaceAll("\\)", "");
                                    tempString = "-" + tempString;
                                }
                                try {
                                    cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                    double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                    cell.setCellValue(curr);
                                } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(tempString);
                                }
                            } else if (report == 116 && (headers[h].equals("ytd_d_amount_period") || headers[h].equals("ytd_c_amount_period") || headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount"))) {
                                String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                                if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                    tempString = "0.0";
                                }
                                tempString = withoutCurrencyRender(tempString, companyid);
                                tempString = tempString.replaceAll(",", "");
                                if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                    tempString = tempString.replaceAll("\\(", "");
                                    tempString = tempString.replaceAll("\\)", "");
                                    tempString = "-" + tempString;
                                }
                                try {
                                    cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                    double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                    cell.setCellValue(curr);
                                } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(tempString);
                                }
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                cell.setCellValue("");
                            } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                                double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                                if (adj < 0.0001) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(currency);
                                }
                            } else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                                if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                    cell.setCellValue(temp.getString("openbalance"));
                                } else {
                                    String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                    cell.setCellValue(currency);
                                }
                            } // SDP-1239 Fields made numeric in excel file
                            else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") || headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") || headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") || headers[h].equals("c_transactionAmount")) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                        cell.setCellValue(quantity);
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if (report == 1117 && (headers[h].equals("openingstock") || headers[h].equals("purchasereturenQuantity") || headers[h].equals("closingstock"))) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        if (!StringUtil.isNullOrEmpty(tempStr)) {
                                            double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                            cell.setCellValue(quantity);
                                        } else {
                                            cell.setCellValue("");
                                        }
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                if (titles[h].equals("Opening Balance Type")) {
                                    String str1 = "";
                                    if (temp.getString(headers[h]).equalsIgnoreCase("-")) { //chart of accounts export 
                                        str1 = "N/A";
                                    } else {
                                        double bal = Double.parseDouble(temp.getString(headers[h]));
                                        str1 = bal == 0 ? "N/A" : (bal < 0 ? "Credit" : "Debit");
                                    }
                                    cell.setCellValue(str1);
                                } else {
                                    try {
                                        if (isFromDocumentDesigner) {
                                            cell.setCellValue(temp.getString(headers[h]));
                                        } else {
                                            String tempStr = temp.getString(headers[h]);
                                            tempStr = tempStr.replaceAll("%", "%25").replaceAll("\\+", "%2B");
                                            cell.setCellValue(StringUtil.DecodeText(tempStr));
                                        }
                                    } catch (Exception e) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    }

                                }
                            }
                        }
                    } else {    //end of if(header.length)
                        cell = row.createCell(cellnum++);  //Create new cell
                        if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",", "");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                            if (isSampleFile) {// sample file
                                try {
                                    String d1 = userFormat.format(dbFormat.parse(temp.getString(headers[h])));
                                    cell.setCellValue(d1);
                                } catch (JSONException | ParseException ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                try {
                                    String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                    cell.setCellValue(d1);
                                } catch (JSONException | ParseException ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else if (headers[h].equals("d_amount")) {
                            String d_amount = "";
                            if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                d_amount = temp.getString("d_amount");
                            } else if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                d_amount = temp.getString("c_amount");
                            }
                            cell.setCellValue(d_amount);
                        } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period"))) {
                            String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            tempString = tempString.replaceAll(",", "");
                            if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                tempString = tempString.replaceAll("\\(", "");
                                tempString = tempString.replaceAll("\\)", "");
                                tempString = "-" + tempString;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else if (report == 116 && (headers[h].equals("ytd_c_amount_open") || headers[h].equals("ytd_d_amount_open") || headers[h].equals("c_amount") || headers[h].equals("d_amount"))) {
                            String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            tempString = tempString.replaceAll(",", "");
                            if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                tempString = tempString.replaceAll("\\(", "");
                                tempString = tempString.replaceAll("\\)", "");
                                tempString = "-" + tempString;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else if (report == 116 && (headers[h].equals("ytd_d_amount_period") || headers[h].equals("ytd_c_amount_period") || headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount"))) {
                            String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            tempString = tempString.replaceAll(",", "");
                            if (tempString.contains("(") && tempString.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                tempString = tempString.replaceAll("\\(", "");
                                tempString = tempString.replaceAll("\\)", "");
                                tempString = "-" + tempString;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no") ? temp.getString("no") : "");
                            } else if (headers[h].equals("invoicedate")) {
                                cell.setCellValue(formatter.format(frmt.parse(temp.getString("date"))));
                            } else if (headers[h].equals("c_date")) {
                                cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))));
                            } else if (headers[h].equals("c_accountname")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")));
                            } else if (headers[h].equals("c_acccode")) {
                                String accCode = "";
                                if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                    accCode = temp.getString("c_acccode");
                                } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                    accCode = temp.getString("d_acccode");
                                }
                                cell.setCellValue(accCode);
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h]))) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")));
                            } else if (headers[h].equals("d_date")) {
                                cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("c_date")))));
                            } else if (headers[h].equals("d_accountname")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname")));
                            } else if (headers[h].equals("d_acccode")) {
                                String accCode = "";
                                if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                    accCode = temp.getString("d_acccode");
                                } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                    accCode = temp.getString("c_acccode");
                                }
                                cell.setCellValue(accCode);
                            } else if (headers[h].equals("c_checkno")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_checkno")) ? temp.getString("d_checkno") : temp.getString("c_checkno")));
                            } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")));
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                //row = sheet.createRow(rownum++);//WRITER POINT TO NEXT ROW (i.e. New Line)
                            } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                                double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                                if (adj < 0.0001) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(currency);
                                }
                            } else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                cell.setCellValue(currency);
                            } else if (titles[h].equals("Opening Balance Type")) {
                                double bal = Double.parseDouble(temp.getString(headers[h]));
                                String str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                cell.setCellValue(str1);
                            } else if (headers[h].equalsIgnoreCase("otherwise")) {
                                if (temp.optString(headers[h]).equalsIgnoreCase("true")) {
                                    cell.setCellValue("No");
                                } else {
                                    cell.setCellValue("Yes");
                                }
                            } // SDP-1239 Fields made numeric in excel file
                            else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") || headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") || headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") || headers[h].equals("c_transactionAmount")) {
                                try {
                                    String tempStr = temp.getString(headers[h]);
                                    cell.setCellType(0);      // SDP-1239 Fields made numeric in excel file
                                    double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                    cell.setCellValue(quantity);
                                } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                cell.setCellValue(StringUtil.DecodeText(temp.optString(headers[h])));
                            }
                        }
                    }
                }//header for loop
            }//Data for loop
            
            wb.write(oss);
            oss.flush();
            if (oss != null) {
                oss.close();
            }

            HashMap<String, Object> exportDetails = new HashMap<String, Object>();

            exportDetails.put("id", productExportDetail.getId());
            exportDetails.put("status", 2);
            exportDetails.put("fileType", fileType);
            accProductObj.saveProductExportDetails(exportDetails);

            SendMail(paramJObj, filePath, fileType,filename);

        } catch (ParseException ex) {
            Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
            Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }

    public void SendMail(JSONObject paramJObj, String path, String fileType,String filename) throws ServiceException {

        String loginUserId = paramJObj.optString("userid");

        KwlReturnObject KWLUser = accountingHandlerDAOobj.getObject(User.class.getName(), loginUserId);
        User user = (User) KWLUser.getEntityList().get(0);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = filename;
        fileName = fileName + "." + fileType;

        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        if (!StringUtil.isNullOrEmpty(cEmail)) {
            try {
                String subject = "Dimension Report Profit and Loss Export Status";
                String reportName = "General Ledger Report ";
                if (paramJObj.has("mailSubject")) {
                    subject = paramJObj.optString("mailSubject");
                }
                if(paramJObj.has("reportName")){
                     reportName = paramJObj.optString("reportName");
                }
                //String sendorInfo = "admin@deskera.com";
                String htmlTextC = "";
                htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                htmlTextC += "<br/>"+reportName+"<b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";

                htmlTextC += "<br/>Regards,<br/>";
                htmlTextC += "<br/>ERP System<br/>";

                String plainMsgC = "";
                plainMsgC += "\nHello " + user.getFirstName() + "\n";
                plainMsgC += "\n"+reportName+"<b>\"" + fileName + "\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                plainMsgC += "\nRegards,\n";
                plainMsgC += "\nERP System\n";


                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                try {
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, new String[]{path}, smtpConfigMap);
                } catch (MessagingException ex1) {
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, "ExportLedger.SendMail :" + ex1.getMessage(), ex1);
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                } finally {
                    System.out.println("Mail Catch-1 Completed: " + new Date());
                }

            } catch (Exception ex) {
                Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                System.out.println("Mail Catch-2 Completed: " + new Date());
            }
        }
    }
    
    
    public HSSFCell writeFilterDetailsInExcel(JSONObject paramJObj, HSSFSheet sheet, HSSFCell cell, int rownum, int cellnum) throws SessionExpiredException, ServiceException, JSONException, UnsupportedEncodingException {
        String companyID = paramJObj.getString(Constants.companyKey);
        String moduleNo = "", reportIdNo = "", getNo = "", isBankBook = "", bankBookSumarryReport = "";
        int module1 = 0, reportId = 0, get = 0;
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("moduleId", null))) {
            moduleNo = paramJObj.optString("moduleId");
            module1 = Integer.parseInt(moduleNo);
        }
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("reportId", null))) {
            reportIdNo = paramJObj.optString("reportId");
            reportId = Integer.parseInt(reportIdNo);
        }
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("get", null))) {
            getNo = paramJObj.optString("get");
            get = Integer.parseInt(getNo);
        }
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("isBankBook", null))) {
            isBankBook = paramJObj.optString("isBankBook");
        }
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("bankBookSumarryReport", null))) {
            bankBookSumarryReport = paramJObj.optString("bankBookSumarryReport");
        }

        //Create new cell for Company Name
        String companyName = paramJObj.getString(Constants.companyname);
        HSSFRow compnayHeaderRow = sheet.createRow(rownum++);
        cell = compnayHeaderRow.createCell(cellnum++);
        cell.setCellValue("Company Name");
        cell = compnayHeaderRow.createCell(cellnum++);
        
//        if (get == Constants.DimensionBasedProfitLossReport || get == Constants.DimensionBasedBalanceSheetReport||get==Constants.GstTapReturnDetailedView ||get==Constants.GstReport) {
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJObj.getString(Constants.companyKey));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity()) {//SDP-4889
                    String searchjson = paramJObj.optString(Constants.Acc_Search_Json,"{}");
                    boolean ismultientity=false;
                    boolean ismultientityfieldpresent=false;
                    StringBuilder appendimensionString = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
                        searchjson=StringUtil.DecodeText(searchjson);
                        JSONObject json = new JSONObject(searchjson);
                        if (json.has("root")) {
                            JSONArray advSearch = json.getJSONArray("root");
//                            if(advSearch.length()==1){
                            for (int i = 0; i < advSearch.length(); i++) {
                                JSONObject dimensionjson = advSearch.getJSONObject(i);
                                ismultientity= dimensionjson.optBoolean(Constants.isMultiEntity, false);
                                    String searchTextArray[] = dimensionjson.optString("searchText").split(",");
                                    if (searchTextArray.length > 0 && dimensionjson.optInt(Constants.fieldtype,0) == 4 && dimensionjson.optBoolean("iscustomfield",true)==false && ismultientity) {
                                        ismultientityfieldpresent=true;
                                        for (String searchTextvalue : searchTextArray) {
                                            FieldComboData fieldcombodata=null;
                                            KwlReturnObject fieldcombodataObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(),searchTextvalue);
                                             if (fieldcombodataObj != null && !fieldcombodataObj.getEntityList().isEmpty() && fieldcombodataObj.getEntityList().get(0) != null) {
                                                  fieldcombodata = (FieldComboData) fieldcombodataObj.getEntityList().get(0);
                                                  if(!StringUtil.isNullOrEmpty(fieldcombodata.getItemdescription())){
                                                      appendimensionString.append(fieldcombodata.getItemdescription().concat(","));
                                                   }else{
                                                      appendimensionString.append(fieldcombodata.getValue().concat(","));  
                                                  }
                                             }
                                        }//end of for of SearchTextValue
                                        break;
                                    }//end of searchTextArray
                            }//end of for loop of advSearch
//                        }
                        }//end of root
                    }//end of empty check of undefined
                    if (ismultientityfieldpresent) {//this will only be applicable for multientity
                            String dimensionvalue = appendimensionString.toString();
                            companyName = dimensionvalue.substring(0, dimensionvalue.length() - 1);
                    }
                }
            }// end of extrapreferences
//        }//end of balancesheet
        
        cell.setCellValue(companyName);

        //Create new cell for Report Title
        cellnum = 0;
        String reporttitle = paramJObj.optString("filename");
        String filename = paramJObj.optString("filename", null) != null ? paramJObj.optString("filename") : paramJObj.optString("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            try {
                reporttitle = URLDecoder.decode(filename, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                reporttitle = filename;
                Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String reporttype = paramJObj.optString("filetype");
        if (reporttype.equalsIgnoreCase("detailedXls")) {
            reporttype = "_Detail";
        } else if (reporttype.equalsIgnoreCase("xls")) {
            reporttype = "_Summary";
        }
        HSSFRow reportTitleHeaderRow = sheet.createRow(rownum++);
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue("Report Title");
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue(reporttitle);

        //Create new cell for Export Date
        cellnum = 0;
        String exportdate = authHandler.getDateOnlyFormat().format(new Date());
        HSSFRow exportDateHeaderRow = sheet.createRow(rownum++);
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue("Export Date");
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(exportdate);

        boolean isDailySalesReport = false;
        if (!StringUtil.isNullOrEmpty(paramJObj.optString("isDailySalesReport", null))) {
            isDailySalesReport = Boolean.parseBoolean(paramJObj.optString("isDailySalesReport"));
        }

        if (isDailySalesReport) {
            String dateRange = paramJObj.optString("dateRange", null);
            cellnum = 0;
            HSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue("For Month");
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue(dateRange);
        } else {
            //Create new cell for From Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get != 1119 && get != 1111) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band,1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String startdate = "";
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("startdate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("startDate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("stdate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("fromDate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("frmDate", null))) {
                    if (module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId && !bankBookSumarryReport.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false")) {
                        if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport) {
                            startdate = paramJObj.optString("startDate");
                        } else if (get == 27 || get == 66 || get == 67 || get == 116 || get == 125 || get == 126 || get == 215 || get == 807 || get == 555 || get == 911 || get == 916) { // 27 = Cost Center Report, 66/67=Customer/Vendor Legder Report,116=Trial Balance,125=Monthly Sales By Product Report,215=Inventory Movement Details Report,555=Stock Report,126=Stock Status report,911=Tax Report,916=GST Report
                            if (!StringUtil.isNullOrEmpty(paramJObj.optString("startdate", null)) && get == 27) {
                                startdate = paramJObj.optString("startdate");
                            } else {
                                startdate = paramJObj.optString("stdate");
                            }
                        } else if (get == 913 || get == 914 || get == 823) { // 913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report
                            startdate = paramJObj.optString("fromDate");
                        } else if (get == 236 || get == 805) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report
                            startdate = paramJObj.optString("frmDate");
                        } else if (get == 117 || get == 772 || get == 773 || get == 775) { // 772=Dimension Based Profit and Loss,773=Dimension Based Balance Sheet,775=Dimension Based Trial Balance
                            startdate = paramJObj.optString("stdate");
                        } else {
                            startdate = paramJObj.optString("startdate");
                        }
                    } else {
                        startdate = paramJObj.optString("stdate");
                    }
                }
                HSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
                cell = fromDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("From Date");
                cell = fromDateHeaderRow.createCell(cellnum++);
                if (!StringUtil.isNullOrEmpty(startdate)) {
                    cell.setCellValue(StringUtil.DecodeText(startdate));
                } else {
                    cell.setCellValue(startdate);
                }
            }

            //Create new cell for To Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get != 1119 && get != 1111) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band, 1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String enddate = "";
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("startdate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("startDate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("stdate", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("toDate", null))) {
                    if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport) {
                        enddate = paramJObj.optString("endDate");
                    } else if (get == 236 || get == 805 || get == 913 || get == 914 || get == 823) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report,913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report
                        enddate = paramJObj.optString("toDate");
                    } else {
                        enddate = paramJObj.optString("enddate");
                    }
                }
                HSSFRow toDateHeaderRow = sheet.createRow(rownum++);
                cell = toDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("To Date");
                cell = toDateHeaderRow.createCell(cellnum++);
                if (!StringUtil.isNullOrEmpty(enddate)) {
                    cell.setCellValue(StringUtil.DecodeText(enddate));
                } else {
                    cell.setCellValue(enddate);
                }
            }
        }
        //Create new cell for Cash/Bank Account Name
        if (isBankBook.equalsIgnoreCase("true") || isBankBook.equalsIgnoreCase("false")) {
            cellnum = 0;
            String accountid = paramJObj.optString("accountid", null);
            String[] accountsArr = accountid.split(",");
            String accountName = "";
            for (int accCount = 0; accCount < accountsArr.length; accCount++) {
                if (!StringUtil.isNullOrEmpty(accountsArr[accCount])) {
                    List accounts = accCostCenterObj.getAccountNameByID(companyID, accountsArr[accCount]);
                    accountName += (String) accounts.get(0) + ", ";
                }
            }
            if (accountName.length() > 0) {
                accountName = accountName.substring(0, accountName.length() - 2);
            }
            HSSFRow cndntypeHeaderRow = sheet.createRow(rownum++);
            cell = cndntypeHeaderRow.createCell(cellnum++);
            if (isBankBook.equalsIgnoreCase("true")) {
                cell.setCellValue("Bank Account Name");
            } else if (isBankBook.equalsIgnoreCase("false")) {
                cell.setCellValue("Cash Account Name");
            }
            cell = cndntypeHeaderRow.createCell(cellnum++);
            cell.setCellValue(accountName);
        }
        /*
         After Appending all rows for filter append search fields rows
         */
        String searchjson = paramJObj.optString(Constants.Acc_Search_Json,"");
        if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
            try {
                searchjson=StringUtil.DecodeText(searchjson);
                JSONObject json = new JSONObject(searchjson);
                if (json.has("root")) {
                    JSONArray advSearch = json.getJSONArray("root");
                    for (int i = 0; i < advSearch.length(); i++) {
                        JSONObject dimensionjson = advSearch.getJSONObject(i);
                        String value = null;
                        String header = "";
                        String fieldtype = "";
                        String ModuleName="";
                        fieldtype = StringUtil.DecodeText(dimensionjson.optString("fieldtype", ""));
                        header = StringUtil.DecodeText(dimensionjson.optString("columnheader", ""));
                        ModuleName = StringUtil.DecodeText(dimensionjson.optString("modulename", ""));
                        if (!StringUtil.isNullOrEmpty(ModuleName)) {
                            header += " [" + ModuleName + "]";
                        }
                        if (fieldtype.equalsIgnoreCase("2") || fieldtype.equalsIgnoreCase("3")) {
                            value = StringUtil.DecodeText(dimensionjson.optString("search", ""));
                        } else {
                            value = StringUtil.DecodeText(dimensionjson.optString("combosearch", ""));
                        }
                        if (!StringUtil.isNullOrEmpty(header)) {
                            cellnum = 0;
                            HSSFRow termHeaderRow = sheet.createRow(rownum++);
                            cell = termHeaderRow.createCell(cellnum++);
                            cell.setCellValue(header);
                            cell = termHeaderRow.createCell(cellnum++);
                            cell.setCellValue(value);
                        }
                    }

                }//end of root
            } catch (Exception ex) {
                Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //Create new cell for BlankRow
        cellnum = 0;
        HSSFRow BlankRow = sheet.createRow(rownum++);
        cell = BlankRow.createCell(cellnum++);
        cell.setCellValue("");

        return cell;
    }

//    public void writeXLSDataToFile(String filename, String fileType, HSSFWorkbook wb, HttpServletResponse response) throws ServiceException {
//    public void writeXLSDataToFile(String filename, String fileType, HSSFWorkbook wb) throws ServiceException {
//        try {
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xls\"");
//            wb.write(response.getOutputStream());
//            response.getOutputStream().close();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
//        } catch (Exception e) {
//            try {
//                response.getOutputStream().println("{\"valid\": false}");
//            } catch (IOException ex) {
//                Logger.getLogger(ExportXlsServiceHandler.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }

    @Deprecated
    public String withoutCurrencyRender(String currency) {

        String fmt = "";
        try {

            DecimalFormat decimalFormat = null;
            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.");
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return "";
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = "(" + decimalFormat.format(amt) + ")";


            } else {
                fmt = decimalFormat.format(amt);

            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String withoutCurrencyRender(String currency, String companyid) {

        String fmt = "";
        try {

            DecimalFormat decimalFormat = null;
            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return "";
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = "(" + decimalFormat.format(amt) + ")";


            } else {
                fmt = decimalFormat.format(amt);

            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }

    @Deprecated
    public String currencyRender(String currency, String currencyid) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            DecimalFormat decimalFormat = null;

            try {
                symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }

            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.");
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(" + symbol + " " + fmt + ")";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String currencyRender(String currency, String currencyid, String companyid) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            DecimalFormat decimalFormat = null;

            try {
                symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }

            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(" + symbol + " " + fmt + ")";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }

    public String unitPriceCurrencyRender(String currency, String currencyid, boolean isCurrencyCode, String companyid) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            DecimalFormat decimalFormat = null;

            try {
                if (isCurrencyCode && currencyid.equals("1")) {
                    symbol = cur.getCurrencyCode();
                } else {
                    symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
                }
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }

            String formatstring = authHandler.getCompleteDFStringwithDigitNumber("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(" + symbol + " " + fmt + ")";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }

    @Deprecated
    public String currencyRender(String currency, String currencyid, boolean isCurrencyCode) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            DecimalFormat decimalFormat = null;

            try {
                if (isCurrencyCode && currencyid.equals("1")) {
                    symbol = cur.getCurrencyCode();
                } else {
                    symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
                }
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }

            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.");
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(" + symbol + " " + fmt + ")";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String currencyRender(String currency, String currencyid, boolean isCurrencyCode, String companyid) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            DecimalFormat decimalFormat = null;

            try {
                if (isCurrencyCode && currencyid.equals("1")) {
                    symbol = cur.getCurrencyCode();
                } else {
                    symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
                }
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }

            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(" + symbol + " " + fmt + ")";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }

    public String htmlPercentageRender(String data, boolean isPDF) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        Double value = Double.parseDouble(data);
        if (value < 0) {
            value = value * -1;
            if (isPDF) {
                data = "(" + decimalFormat.format(value) + ")%";
            } else {
                data = "<td align=\"right\"> (<label style='color:red;'>" + decimalFormat.format(value) + "</label>)&#37;</td>";
            }
        } else {
            if (isPDF) {
                data = decimalFormat.format(value) + "%";
            } else {
                data = "<td align=\"right\">" + data + "&#37;</td>";
            }
        }
        return data;
    }
    
}
