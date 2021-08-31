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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.exportFunctionality;

import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.IndonesiaConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import java.util.List;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.web.servlet.support.RequestContextUtils;

public class exportMPXDAOImpl implements MessageSourceAware {

    private HibernateTemplate hibernateTemplate;
    private storageHandlerImpl storageHandlerImplObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private authHandlerDAO authHandlerDAOObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccCostCenterDAO accCostCenterObj;

    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterObj) {
        this.accCostCenterObj = accCostCenterObj;
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    private static FontFamilySelector fontFamilySelector = new FontFamilySelector();

    static {
        FontFamily fontFamily = new FontFamily();
        fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
        fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK));
        fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Times New Roman", 14, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.REPORT_TITLE, FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK));
        fontFamily.addFont(FontContext.SMALL_TEXT, FontFactory.getFont("Times New Roman", 12, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.TABLE_HEADER, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.TABLE_DATA, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
        fontFamilySelector.addFontFamily(fontFamily);

        File[] files;
        try {
            File f = new File(exportMPXDAOImpl.class.getClassLoader().getResource("fonts").toURI());
            files = f.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".ttf");
                }
            });
        } catch (Exception e1) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e1);
            files = new File[]{};
        }
        for (File file : files) {
            try {
                BaseFont bfnt = BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontFamily = new FontFamily();
                fontFamily.addFont(FontContext.HEADER_NOTE, new Font(bfnt, 10, Font.BOLD, Color.GRAY));
                fontFamily.addFont(FontContext.FOOTER_NOTE, new Font(bfnt, 12, Font.BOLD, Color.GRAY));
                fontFamily.addFont(FontContext.LOGO_TEXT, new Font(bfnt, 14, Font.NORMAL, Color.BLACK));
                fontFamily.addFont(FontContext.REPORT_TITLE, new Font(bfnt, 20, Font.BOLD, Color.BLACK));
                fontFamily.addFont(FontContext.SMALL_TEXT, new Font(bfnt, 12, Font.NORMAL, Color.BLACK));
                fontFamily.addFont(FontContext.TABLE_HEADER, new Font(bfnt, 14, Font.BOLD, Color.BLACK));
                fontFamily.addFont(FontContext.TABLE_DATA, new Font(bfnt, 12, Font.NORMAL, Color.BLACK));
                fontFamilySelector.addFontFamily(fontFamily);
            } catch (Exception e) {
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
            }
        }

    }
//    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
//    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
//    private static Font fontMediumRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
//    private static Font fontRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
//    private static Font fontBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
//    private static Font fontBig = FontFactory.getFont("Helvetica", 24, Font.NORMAL, Color.BLACK);
    private static String imgPath = "";
    private static String companyName = "";
    private static com.krawler.utils.json.base.JSONObject config = null;
    private PdfPTable header = null;
    private PdfPTable footer = null;
    private static final long serialVersionUID = -8401651817881523209L;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
    private static String errorMsg = "";

    public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                try {
                    getHeaderFooter(document);
                } catch (ServiceException ex) {
                    Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Add page header
                header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());

                // Add page footer
                footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());

                // Add page border
                if (config.getBoolean("pageBorder")) {
                    int bmargin = 8;  //border margin
                    PdfContentByte cb = writer.getDirectContent();
                    cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.stroke();
                }

            } catch (JSONException e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        String filename = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        String fileType = null;
        JSONObject grid = null;
        String productExportFileName = jobj.optString("productExportFileName");
        int totalProducts = 0;
        boolean isSummaryXls = false;
        if (request.getAttribute("isSummaryXls") != null) {
                    isSummaryXls = Boolean.parseBoolean(request.getAttribute("isSummaryXls").toString());
                }
        try {
            if (!StringUtil.isNullOrEmpty(jobj.optString("filename"))) {    //sample file ckeck
                filename = (String) jobj.optString("filename");
                filename = URLDecoder.decode(filename, "ISO-8859-1");
            }
            fileType = request.getParameter("filetype");
            if (!StringUtil.isNullOrEmpty(jobj.optString("totalProducts"))) {
                totalProducts = Integer.parseInt(jobj.optString("totalProducts"));
            }
            if (request.getParameter("gridconfig") != null && !"undefined".equals(request.getParameter("gridconfig"))) {
                String gridconfig = request.getParameter("gridconfig");
                String get = request.getParameter("get") == null ? "" : request.getParameter("get");
                if (get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) { //Aged Receivable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridCustomer/AccName", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request)) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.3", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22")) { //Aged Payable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridVendor/AccName", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridVIno", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request)) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.4", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("914")) {
                    gridconfig = "{"
                            + "data:[{'header':'productname','title':'" + messageSource.getMessage("acc.saleByItem.gridProduct", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'productdescription','title':'Product Description','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.saleByItem.gridInvoice", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.saleByItem.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'memo','title':'" + messageSource.getMessage("acc.saleByItem.gridMemo", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'personname','title':'" + messageSource.getMessage("acc.saleByItem.gridCustName", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'promisedDate','title':'Promised Date','width':'100','align':'date'},{'header':'quantity','title':'" + messageSource.getMessage("acc.saleByItem.gridQty", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'rateinbase','title':'" + messageSource.getMessage("acc.saleByItem.gridSalesPrice", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'amount','title':'" + messageSource.getMessage("acc.saleByItem.gridAmount", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'totalsales','title':'" + messageSource.getMessage("acc.saleByItem.gridBalance", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'status','title':'Status','width':'100','align':''}],"
                            + "groupdata:{'groupBy':'productname','groupSummaryField':'amount','groupSummaryText':'" + messageSource.getMessage("acc.nee.5", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amount','reportSummaryText':'" + messageSource.getMessage("acc.nee.6", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                }
                if (request.getAttribute("gridconfig") != null) {
                    grid = (JSONObject) request.getAttribute("gridconfig");
                } else {
                    grid = new JSONObject(gridconfig);
                }
            } else if (request.getAttribute("gridconfig") != null) {
                grid = (JSONObject) request.getAttribute("gridconfig");
            }

            int report =!StringUtil.isNullOrEmpty(request.getParameter("get")) ? Integer.parseInt(request.getParameter("get")) : 0;
            JSONArray gridmap=null;
            String colHeader="";
            if (report == 772 || report == 773 || report == 775) {
                gridmap =grid.has("data") ?  grid.getJSONArray("data") : null;
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
                 /*
                 772 - dimensional pnl
                 773 - dimensional BL
                 */
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
            
            if (StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType,"detailedCSV")) {
                if (totalProducts < 1000) {
                    createCsvFile(request, response, jobj,colHeader);
                } else {// For Product Export

                    DateFormat frmt = (DateFormat) jobj.get("dateFormatForProduct");
                    String userDateFormatId = jobj.getString("userDateFormatId");
                    String timeZoneDifferenceId = jobj.getString("timeZoneDifferenceId");
                    String currencyID = jobj.getString("currencyIDForProduct");

                    baos = getCreatedCSVFileOutputStream(request, response, jobj, frmt, userDateFormatId, timeZoneDifferenceId, currencyID, colHeader);

                    createExportFiles(baos, fileType, productExportFileName);   //If product count is more than 1000
                }
            } else if (StringUtil.equal(fileType, "pdf")) {

                baos = getPdfData(grid, request, jobj);
                if (totalProducts < 1000) {
                    writeDataToFile(filename, fileType, baos, response);
                } else {
                    createExportFiles(baos, fileType, productExportFileName);   //If product count is more than 1000
                }
            } else if (StringUtil.equal(fileType, "print")) {
                createPrinPriviewFile(request, response, jobj ,colHeader);
            } else if (StringUtil.equal(fileType, "xls")||StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet       
                    if (StringUtil.equal(fileType, "detailedXls")||isSummaryXls) {
                        String append = StringUtil.equal(fileType, "detailedXls") ? "(Detail)" : "(Summary)";
                        if (!StringUtil.isNullOrEmpty(filename)) {
                            if (filename.indexOf("_v") != -1) {
                                String version = filename.substring(filename.indexOf("_v"), filename.length());
                                filename = filename.substring(0, filename.indexOf("_v"));
                                filename = filename.concat(append).concat(version);
                            } else {
                                filename = filename + append;
                            }
                        }
                    }
                    byte[] bytes=null;
                    HSSFWorkbook workBook=null;
                    workBook=createExcelFile(request, response, jobj,colHeader);

                    if (totalProducts < 1000) {
                        writeXLSDataToFile(filename, fileType, workBook, response);
                    } else {
                        createExcelExportFile(workBook, fileType, productExportFileName);
                    }
            } else if (StringUtil.equal(fileType, "xlsx")) {      //Generate Excel Sheet
                byte[] bytes=null;
                XSSFWorkbook workBook=null;
                workBook=createExcelXFile(request, response, jobj,colHeader);
                writeXLSXDataToFile(filename, fileType, workBook, response);
            }
        } catch (ServiceException ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    public void processRequestNew(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        String filename = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        String fileType = null;
        JSONObject grid = null;
        String productExportFileName = jobj.optString("productExportFileName");
        int totalProducts = 0;
        boolean isSummaryXls = false;
        if (request.getAttribute("isSummaryXls") != null) {
                    isSummaryXls = Boolean.parseBoolean(request.getAttribute("isSummaryXls").toString());
                }
        try {
            if (!StringUtil.isNullOrEmpty(jobj.optString("filename"))) {    //sample file ckeck
                filename = (String) jobj.optString("filename");
                filename = URLDecoder.decode(filename, "ISO-8859-1");
            }
            fileType = request.getParameter("filetype");
            if (!StringUtil.isNullOrEmpty(jobj.optString("totalProducts"))) {
                totalProducts = Integer.parseInt(jobj.optString("totalProducts"));
            }
            if (request.getParameter("gridconfig") != null && !"undefined".equals(request.getParameter("gridconfig"))) {
                String gridconfig = request.getParameter("gridconfig");
                String get = request.getParameter("get") == null ? "" : request.getParameter("get");
                if (get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) { //Aged Receivable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridCustomer/AccName", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request)) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.3", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22")) { //Aged Payable
                    gridconfig = "{"
                            + "data:[{'header':'personname','title':'" + messageSource.getMessage("acc.agedPay.gridVendor/AccName", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.agedPay.gridVIno", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'duedate','title':'" + messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'termname','title':'Term Name','width':'150','align':''},{'header':'amountdue','title':'" + messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request)) + "','width':'75','align':'currency'},{'header':'amountdue1','title':'Current','width':'75','align':'currency'},{'header':'amountdue2','title':'1-30 days','width':'75','align':'currency'},{'header':'amountdue3','title':'31-60 days','width':'75','align':'currency'},{'header':'amountdue4','title':'61-90 days','width':'75','align':'currency'},{'header':'amountdue5','title':'91-120 days','width':'75','align':'currency'},{'header':'amountdue6','title':'121-150 days','width':'75','align':'currency'},{'header':'amountdue7','title':'151-180 days','width':'75','align':'currency'},{'header':'amountdue8','title':'>180 days','width':'75','align':'currency'},{'header':'amountdueinbase','title':'Amount Due (In Home Currency)','width':'150','align':'currency'}],"
                            + "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'" + messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amountdueinbase','reportSummaryText':'" + messageSource.getMessage("acc.nee.4", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                } else if (get.equalsIgnoreCase("914")) {
                    gridconfig = "{"
                            + "data:[{'header':'productname','title':'" + messageSource.getMessage("acc.saleByItem.gridProduct", null, RequestContextUtils.getLocale(request)) + "','width':'150','align':''},{'header':'productdescription','title':'Product Description','width':'150','align':''},{'header':'billno','title':'" + messageSource.getMessage("acc.saleByItem.gridInvoice", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'date','title':'" + messageSource.getMessage("acc.saleByItem.gridDate", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'date'},{'header':'memo','title':'" + messageSource.getMessage("acc.saleByItem.gridMemo", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'personname','title':'" + messageSource.getMessage("acc.saleByItem.gridCustName", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'promisedDate','title':'Promised Date','width':'100','align':'date'},{'header':'quantity','title':'" + messageSource.getMessage("acc.saleByItem.gridQty", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':''},{'header':'rateinbase','title':'" + messageSource.getMessage("acc.saleByItem.gridSalesPrice", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'amount','title':'" + messageSource.getMessage("acc.saleByItem.gridAmount", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'totalsales','title':'" + messageSource.getMessage("acc.saleByItem.gridBalance", null, RequestContextUtils.getLocale(request)) + "','width':'100','align':'rowcurrency'},{'header':'status','title':'Status','width':'100','align':''}],"
                            + "groupdata:{'groupBy':'productname','groupSummaryField':'amount','groupSummaryText':'" + messageSource.getMessage("acc.nee.5", null, RequestContextUtils.getLocale(request)) + " ','reportSummaryField':'amount','reportSummaryText':'" + messageSource.getMessage("acc.nee.6", null, RequestContextUtils.getLocale(request)) + " '}"
                            + "}";
                }
                if (request.getAttribute("gridconfig") != null) {
                    grid = (JSONObject) request.getAttribute("gridconfig");
                } else {
                    grid = new JSONObject(gridconfig);
                }
            } else if (request.getAttribute("gridconfig") != null) {
                grid = (JSONObject) request.getAttribute("gridconfig");
            }

            int report =!StringUtil.isNullOrEmpty(request.getParameter("get")) ? Integer.parseInt(request.getParameter("get")) : 0;
            JSONArray gridmap=null;
            String colHeader="";
            if (report == 772 || report == 773 || report == 775) {
                gridmap =grid.has("data") ?  grid.getJSONArray("data") : null;
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
                 /*
                 772 - dimensional pnl
                 773 - dimensional BL
                 */
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
            
            if (StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType,"detailedCSV")) {
                if (totalProducts < 1000) {
                    createCsvFile(request, response, jobj,colHeader);
                } else {// For Product Export

                    DateFormat frmt = (DateFormat) jobj.get("dateFormatForProduct");
                    String userDateFormatId = jobj.getString("userDateFormatId");
                    String timeZoneDifferenceId = jobj.getString("timeZoneDifferenceId");
                    String currencyID = jobj.getString("currencyIDForProduct");

                    baos = getCreatedCSVFileOutputStream(request, response, jobj, frmt, userDateFormatId, timeZoneDifferenceId, currencyID, colHeader);

                    createExportFiles(baos, fileType, productExportFileName);   //If product count is more than 1000
                }
            } else if (StringUtil.equal(fileType, "pdf")) {

                baos = getPdfData(grid, request, jobj);
                if (totalProducts < 1000) {
                    writeDataToFile(filename, fileType, baos, response);
                } else {
                    createExportFiles(baos, fileType, productExportFileName);   //If product count is more than 1000
                }
            } else if (StringUtil.equal(fileType, "print")) {
                createPrinPriviewFile(request, response, jobj ,colHeader);
            } else if (StringUtil.equal(fileType, "xls")||StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet       

                    if (StringUtil.equal(fileType, "detailedXls")||isSummaryXls) {
                        String append = StringUtil.equal(fileType, "detailedXls") ? "(Detail)" : "(Summary)";
                        if (!StringUtil.isNullOrEmpty(filename)) {
                            if (filename.indexOf("_v") != -1) {
                                String version = filename.substring(filename.indexOf("_v"), filename.length());
                                filename = filename.substring(0, filename.indexOf("_v"));
                                filename = filename.concat(append).concat(version);
                            } else {
                                filename = filename + append;
                            }
                        }
                    }
                    byte[] bytes=null;
                    XSSFWorkbook workBook=null;
                    workBook=createExcelXFileNew(request, response, jobj,colHeader);
                    if (totalProducts < 1000) {
                        writeXLSXDataToFile(filename, fileType, workBook, response);
                    } else {
                        createExcelExportFile(workBook, fileType, productExportFileName);
                    }
                
            } else if (StringUtil.equal(fileType, "xlsx")) {      //Generate Excel Sheet
                byte[] bytes=null;
                XSSFWorkbook workBook=null;
                workBook=createExcelXFile(request, response, jobj,colHeader);
                writeXLSXDataToFile(filename, fileType, workBook, response);
            }
        } catch (ServiceException ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    public String processRequest(JSONObject requestJobj, JSONObject jobj) throws ServiceException, IOException, SessionExpiredException, JSONException {
        String filename = requestJobj.optString("filename", null) != null ? requestJobj.optString("filename") : requestJobj.optString("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        String fileType = requestJobj.optString("filetype");
        String filePath = "";
        String ext = "";
        ByteArrayOutputStream baos = null;
        JSONObject grid = null;
        try {
            if (!StringUtil.isNullOrEmpty(jobj.optString("filename"))) {    //sample file ckeck
                filename = jobj.optString("filename");
                filename = URLDecoder.decode(filename, "ISO-8859-1");
            }

            String colHeader = "";

            if (StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType,"detailedCSV")) {
                DateFormat frmt = (DateFormat) jobj.get("dateFormatForProduct");
                String userDateFormatId = jobj.getString("userDateFormatId");
                String timeZoneDifferenceId = jobj.getString("timeZoneDifferenceId");
                String currencyID = jobj.getString("currencyIDForProduct");

                baos = getCreatedCSVFileOutputStream(requestJobj, jobj, userDateFormatId, timeZoneDifferenceId, currencyID, colHeader);

                createExportFiles(baos, fileType, filename);   //If product count is more than 1000
            } else if (StringUtil.equal(fileType, "pdf")) {

                baos = getPdfData(grid, requestJobj, jobj);
                createExportFiles(baos, fileType, filename);   //If product count is more than 1000
            } else if (StringUtil.equal(fileType, "xls") || StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet       
                HSSFWorkbook workBook = createExcelFile(requestJobj, jobj, colHeader);
                ext = "xls";

                filePath = createExcelExportFile(workBook, ext, filename);
            }
        } catch (UnsupportedEncodingException | ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return filePath;
    }
    public String processRequestNew(JSONObject requestJobj, JSONObject jobj) throws ServiceException, IOException, SessionExpiredException, JSONException {
        String filename = requestJobj.optString("filename", null) != null ? requestJobj.optString("filename") : requestJobj.optString("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        String fileType = requestJobj.optString("filetype");
        String filePath = "";
        String ext = "";
        ByteArrayOutputStream baos = null;
        JSONObject grid = null;
        try {
            if (!StringUtil.isNullOrEmpty(jobj.optString("filename"))) {    //sample file ckeck
                filename = jobj.optString("filename");
                filename = URLDecoder.decode(filename, "ISO-8859-1");
            }

            String colHeader = "";

            if (StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet      
                XSSFWorkbook workBook = createExcelFileNew(requestJobj, jobj, colHeader);
                ext = "xlsx";

                filePath = createExcelExportFile(workBook, ext, filename);
            }else if (StringUtil.equal(fileType, "xls")) {      //Generate Excel Sheet      
                HSSFWorkbook workBook = createExcelFile(requestJobj, jobj, colHeader);
                ext = "xls";
//                ext = "xlsx";

                filePath = createExcelExportFile(workBook, ext, filename);
            } else if (StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType,"detailedCSV")) {
//                DateFormat frmt = (DateFormat) requestJobj.get("dateFormatForProduct");
                String userDateFormatId = requestJobj.getString("userDateFormatId");
                String timeZoneDifferenceId = requestJobj.getString("timeZoneDifferenceId");
                String currencyID = requestJobj.getString("currencyIDForProduct");

                baos = getCreatedCSVFileOutputStream(requestJobj, jobj, userDateFormatId, timeZoneDifferenceId, currencyID, colHeader);

                createExportFiles(baos, fileType, filename);   //If product count is more than 1000
            } else if (StringUtil.equal(fileType, "pdf")) {

                baos = getPdfData(grid, requestJobj, jobj);
                createExportFiles(baos, fileType, filename);   //If product count is more than 1000
            }
        } catch (UnsupportedEncodingException | ParseException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return filePath;
    }

     public void createPrinPriviewFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj, String colHeader) throws ServiceException {

        try {
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(request);//ERP-36142
            KwlReturnObject compaccresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImplObj.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
//            DateFormat frmt = authHandler.getDateFormatter(request);  //refer ticket ERP-15117
            DateFormat frmt = authHandler.getDateOnlyFormat(request);
            SimpleDateFormat dateFormatForTapReturn = new SimpleDateFormat("yyyy-MM-dd");
            boolean excludeCustomHeaders = StringUtil.isNullOrEmpty(request.getParameter("excludeCustomHeaders"))?false:Boolean.parseBoolean(request.getParameter("excludeCustomHeaders"));
            String module =request.getAttribute("moduleId")!=null? (String) request.getAttribute("moduleId"):"0000";
            int moduleId = Integer.parseInt(module);
            String headers[] = null;
            KwlReturnObject result = null;
            String titles[] = null;
            String align[] = null;
            JSONArray repArr = new JSONArray();
            String searchjson = request.getParameter(Constants.Acc_Search_Json);
            JSONObject json = null;
            JSONArray advSearch = null;
            String htmlCode = "";
            String advStr = "<ol>";
//            User userid = (User) session.load(User.class, AuthHandler.getUserid(request));
//            String  startdate = remoteapi.getUserDateFormatter1(userid, session, KWLDateFormat.DATE_PART).format(new Date());
            int report = Integer.parseInt(request.getParameter("get"));      
            boolean isBasedOnProduct = request.getParameter("isBasedOnProduct") != null ? Boolean.parseBoolean(request.getParameter("isBasedOnProduct")) : false;
            boolean displayUnitPriceandAmtInSalesDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,false);
            Set<String> unitPriceAmountheaderName = new HashSet<>();
            if (!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) {
                unitPriceAmountheaderName = getUnitPriceAndAmountDataIndexSet(report,displayUnitPriceandAmtInSalesDocPerm,displayUnitPriceandAmtInPurchaseDocPerm);
            }
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0, grandTotalAmountWithTaxInBaseCurrency=0 ;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isCurrencyCode = false;
            String startdate = request.getParameter("generatedOnTime");//to show client time 
            if (StringUtil.isNullOrEmpty(startdate)) {
                startdate = obj.optString("GenerateDate");
            }
            String FromDate="",ToDate="";
            boolean isFromToDateRequired = false;
            if (!StringUtil.isNullOrEmpty(obj.optString("isFromToDateRequired"))) {
                isFromToDateRequired = obj.getBoolean("isFromToDateRequired");
                boolean monthlyBS = request.getParameter("monthlyBS") != null ? Boolean.parseBoolean(request.getParameter("monthlyBS")) : false;
                if (monthlyBS && (StringUtil.isNullOrEmpty(obj.getString("stdate")) && StringUtil.isNullOrEmpty(obj.getString("enddate")))) {
                    final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
                    LocalDate localStartDate = dtf.parseLocalDate(request.getParameter("stdate"));
                    LocalDate localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
                    FromDate = authHandler.getUserDateFormatterWithoutTimeZone(request).format(localStartDate.toDate());
                    ToDate = authHandler.getUserDateFormatterWithoutTimeZone(request).format(localEndDate.toDate());
                } else if(report==1153){// For Group Detail Report(GL) print case
                    FromDate = StringUtil.isNullOrEmpty(request.getParameter("startdate")) ? "" : authHandler.getUserDateFormatterWithoutTimeZone(request).format(authHandler.getGlobalDateFormat().parse(request.getParameter("startdate")));
                    ToDate = StringUtil.isNullOrEmpty(request.getParameter("enddate")) ? "" : authHandler.getUserDateFormatterWithoutTimeZone(request).format(authHandler.getGlobalDateFormat().parse(request.getParameter("enddate")));
                } else {
                    FromDate = authHandler.getUserDateFormatterWithoutTimeZone(request).format(authHandler.getDateOnlyFormat(request).parse(obj.getString("stdate")));  //ERP-20918
                    ToDate = authHandler.getUserDateFormatterWithoutTimeZone(request).format(authHandler.getDateOnlyFormat(request).parse(obj.getString("enddate")));
                }
            }
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            //companyid = request.getParameter("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }
            if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
                json = new JSONObject(request.getParameter(Constants.Acc_Search_Json));
                advSearch = json.getJSONArray("root");
                for (int i = 0; i < advSearch.length(); i++) {
                    JSONObject key = advSearch.getJSONObject(i);
                    String value = "";
                    String name = key.getString("columnheader");
                    name = StringUtil.DecodeText(name);
                    name.trim();
                    if (name.contains("*")) {
                        name = name.substring(0, name.indexOf("*") - 1);
                    }
                    if (name.contains("(") && name.charAt(name.indexOf("(") + 1) == '&') {
                        htmlCode = name.substring(name.indexOf("(") + 3, name.length() - 2);
                        char temp = (char) Integer.parseInt(htmlCode, 10);
                        htmlCode = Character.toString(temp);
                        if (htmlCode.equals("$")) {
                            String currency = currencyRender(key.getString("combosearch"), currencyid, isCurrencyCode, companyid);
                            name = name.substring(0, name.indexOf("(") - 1);
                            name = name + "(" + htmlCode + ")";
                            value = currency;
                        } else {
                            name = name.substring(0, name.indexOf("(") - 1);
                            value = name + " " + htmlCode;
                        }
                    } else {
                        value = StringUtil.DecodeText(key.optString("combosearch"));
                    }
                    advStr += "<li><font size=\"2\">" + name + " : " + value + "</font></li>";
                }
                advStr += "</ol>";
            }
            String filename = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
            if (!StringUtil.isNullOrEmpty(filename)) {
                filename = URLDecoder.decode(filename, "ISO-8859-1");
            }
            String ashtmlString = "<html> "
                    + "<head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                    + "<title>" + filename + "</title>"
                    + "<style type=\"text/css\">@media print {button#print {display: none;}}</style>"
                    + "</head>"
                    + //                                        "<body style = \"font-family: Arial, Helvetica, sans-sarif;\">" +
                    "<center><div style='padding-bottom: 5px; padding-right: 5px;'>"
                    + "<h3> " + filename + " </h3>"
                    + "</div></center>";
            if(isFromToDateRequired){
                ashtmlString += "<p style=\"text-align:left;\">"
                        + "<b><font size=\"2\">" + messageSource.getMessage("acc.nee.FromDate", null, RequestContextUtils.getLocale(request)) + " : </b>" + FromDate + "</font>&nbsp;&nbsp;"
                        + "<b><font size=\"2\">" + messageSource.getMessage("acc.nee.ToDate", null, RequestContextUtils.getLocale(request)) + " : </b>" + ToDate + "</font>"
                        + "";
                ashtmlString += "<span style=\"float:right;\">"
                        + "<b><font size=\"2\">" + messageSource.getMessage("acc.nee.1", null, RequestContextUtils.getLocale(request)) + " : </b>" + startdate + "</font>"
                        + "</span></p>";
            }else{
                ashtmlString += "<div>"
                        + "<b><font size=\"2\">" + messageSource.getMessage("acc.nee.1", null, RequestContextUtils.getLocale(request)) + " : </b>" + startdate + "</font>"
                        + "</div></br>";
            }
            if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
                ashtmlString += "<div>"
                        + "<b><font size=\"2\">Selection Criteria : </b></font>" + advStr
                        + "</div>";
            }

            String atempstr = "<DIV style='page-break-after:always'></DIV>";
            String tit="";
            String head="";
            String alignstr="";
            if (request.getParameter("header") != null) {
                tit = request.getParameter("title");
                 head = request.getParameter("header");
                alignstr = request.getParameter("align");
                if (report == 772 || report == 773 ||report == 775) {
                    tit = colHeader;
                    if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport){
                        head =  obj.getString("dataIndexes");
                        alignstr = obj.getString("aligns");
                    }
                } else {
                    tit = request.getParameter("title");
                    head = request.getParameter("header");
                    alignstr = request.getParameter("align");
                }
                boolean isMonthlyReport=StringUtil.isNullOrEmpty(request.getParameter("isMonthlyReport"))?false:Boolean.parseBoolean(request.getParameter("isMonthlyReport"));
                if(isMonthlyReport){
                    tit = request.getAttribute("title").toString();
                    head = request.getAttribute("header").toString();
                    alignstr = request.getAttribute("align").toString();
                }
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) alignstr.split(",");
            } else {
                headers = (String[]) obj.get("header");
                titles = (String[]) obj.get("title");
                align = (String[]) obj.get("align");
                    }
            /* 'excludeCustomHeaders' check is added to improve performance while exporting records. 
               Purpose: Insert Custom field headers on server side rather than sending all of them in URL. 
               It will reduce size of URL.*/
             if (excludeCustomHeaders) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(request.getAttribute("companyid"), moduleId));
                result = accCommonTablesDAO.getFieldParamsforSpecificFields(requestParams);

                ArrayList<String> headList = new ArrayList<String>();
                Collections.addAll(headList, headers);
                if (result.getRecordTotalCount() > 0) {
                    List<String> fields = result.getEntityList();
                    Iterator itr = fields.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String label = oj[2].toString();
                        String fieldname = oj[1].toString();
                        if (headList.contains(fieldname)) {
                            int index = headList.indexOf(fieldname);
                            titles[index] = label;
                        }
                    }
                }
                headers = headList.toArray(new String[headList.size()]);
            }
            
            StringBuilder reportSB = new StringBuilder();

            if (obj.isNull("coldata")) {
                if (obj.has("data")) {
                    repArr = obj.getJSONArray("data");
                }
            } else {
                repArr = obj.getJSONArray("coldata");
            }

            for (int t = 0; t < repArr.length(); t++) {
                grandTotalInBaseCurrency=0;
                grandTotalAmountWithTaxInBaseCurrency=0;
                if (t != 0) {
                    ashtmlString += "</br></br>";
                }
                ashtmlString += "<center>";
                ashtmlString += "<table cellspacing=0 border=1 cellpadding=2 width='100%' style='font-size:9pt'>";
                ashtmlString += "<tr>";
                for (int hCnt = -1; hCnt < titles.length; hCnt++) {
                    if (hCnt == -1) {
                        ashtmlString += "<th>" + messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)) + "</th>";
                    } else {
                        ashtmlString += "<th>" + titles[hCnt] + "</th>";
                    }
                }
                ashtmlString += "</tr>";
                for (int h = 0; h < repArr.length(); h++) {
                    if (repArr.length() - t != 0) {
                        String recordData = "<tr><td align=\"center\">" + (t + 1) + "</td>";
                        JSONObject temp = repArr.getJSONObject(t);
                    /*commenting this code for ERP-12840 ticket
                     * if (report == 116) { //116:Trial Balance
                            if (temp.has("c_amount")) {
                                totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                            }
                            if (temp.has("d_amount")) {
                                totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                            }
                            if (temp.has("c_amount_open")) {
                                totalOpenCre = totalOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_open")) ? temp.getString("c_amount_open") : "0");
                            }
                            if (temp.has("d_amount_open")) {
                                totalOpenDeb = totalOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_open")) ? temp.getString("d_amount_open") : "0");
                            }
                            if (temp.has("c_amount_period")) {
                                totalPeriodCre = totalPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_period")) ? temp.getString("c_amount_period") : "0");
                            }
                            if (temp.has("d_amount_period")) {
                                totalPeriodDeb = totalPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_period")) ? temp.getString("d_amount_period") : "0");
                            }
                            if (temp.has("ytd_c_amount_open")) {
                                totalYTDOpenCre = totalYTDOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_open")) ? temp.getString("ytd_c_amount_open") : "0");
                            }
                            if (temp.has("ytd_d_amount_open")) {
                                totalYTDOpenDeb = totalYTDOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_open")) ? temp.getString("ytd_d_amount_open") : "0");
                            }
                            if (temp.has("ytd_c_amount_period")) {
                                totalYTDPeriodCre = totalYTDPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_period")) ? temp.getString("ytd_c_amount_period") : "0");
                            }
                            if (temp.has("ytd_d_amount_period")) {
                                totalYTDPeriodDeb = totalYTDPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_period")) ? temp.getString("ytd_d_amount_period") : "0");
                            }
                            if (temp.has("ytd_c_amount")) {
                                totalYTDCre = totalYTDCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount")) ? temp.getString("ytd_c_amount") : "0");
                            }
                            if (temp.has("ytd_d_amount")) {
                                totalYTDDeb = totalYTDDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount")) ? temp.getString("ytd_d_amount") : "0");
                            }
                        } else */if (report == 4 || report == 8 || report==301  || report==827 || report == 810 ||report == 807 ||(report == 125 && isBasedOnProduct)) { //report==827 day end collection report   report==810 : "Sales by Product Category Detail Report" ,report == 807 :  Monthly Sales by Product subject to GST,  report == 125 : Monthly Sales by Product
                                grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.optString("amountinbase")) ? temp.optString("amountinbase") : "0");
                                if(report == 301){
                                    grandTotalAmountWithTaxInBaseCurrency = grandTotalAmountWithTaxInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbasewithtax")) ? temp.getString("amountinbasewithtax") : "0");
                                }
                              }
                        for (int hCnt = 0; hCnt < headers.length; hCnt++) {
                            if (temp.has(headers[hCnt].toString())) {
                                try {
                                    String cellData = temp.getString(headers[hCnt]);
                                    String unitName=temp.has("uomname")?temp.getString("uomname"):""; //ERP-9768 [SJ]
                                        cellData=cellData.replace("\n", "<br>");
                                        
                                    if ((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[hCnt])) {
                                        recordData += "<td align=\"right\">" + Constants.UPAndAmtDispalyValueNoPerm + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("currency") && !cellData.equals("")) {
                                        String currencyId = currencyid;
                                        cellData = (report==115||report==51||report==52||report==66||report==67)?htmlCurrencyRender(cellData, currencyId, isCurrencyCode, companyid):withoutHtmlCurrencyRender(cellData, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("withoutcurrency") && !cellData.equals("")) {
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("rowcurrency") && !cellData.equals("")) {
                                        String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
                                        cellData =(report==115||report==51||report==52||report==66||report==67)?htmlCurrencyRender(cellData, rowCurrencyId, isCurrencyCode, companyid): withoutHtmlCurrencyRender(cellData, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    }
                                    /*
                                     * Apply currency render to Amount in transaction currency in gst form 5
                                     */
                                            
                                    else if (align[hCnt].equals("rowcurrencyGstForm") && !cellData.equals("")) {
                                        String rowCurrencyId = temp.has("transactioncurrencyid") ? temp.getString("transactioncurrencyid") : currencyid;
                                        cellData = currencyRender(cellData, rowCurrencyId, isCurrencyCode, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    }else if (align[hCnt].equals("transactioncurrency") && !cellData.equals("")) {
                                        String rowCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
                                        cellData = htmlCurrencyRender(cellData, rowCurrencyId, isCurrencyCode, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("unitpricecurrency") && !cellData.equals("")) {
                                        String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
                                        cellData = (report==152)? withoutHtmlCurrencyRender(cellData, companyid):htmlCurrencyUnitPriceRender(cellData, rowCurrencyId, isCurrencyCode, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("withoutrowcurrency") && !cellData.equals("")) {
//                                    String rowCurrencyId = temp.has("currencyid")?temp.getString("currencyid"):currencyid;
                                        cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                        recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                    } else if (align[hCnt].equals("date") && !cellData.equals("")) {
                                        try {
                                            cellData = formatter.format(frmt.parse(cellData));
                                            recordData += "<td style=\"white-space:nowrap\">" + cellData + "&nbsp;</td>";
                                        } catch (Exception ex) {
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        }
                                    } else if (headers[hCnt].equals("taxrate") || headers[hCnt].equals("permargin") && !cellData.equals("")) {
                                        recordData += htmlPercentageRender(cellData, false);
                                    } else {
                                        if (headers[hCnt].equals("invoiceno")) {
                                            cellData = temp.getString("invoiceno");
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("supplierinvoiceno")) {
                                            cellData = temp.optString("supplierinvoiceno");
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("invoicedate")) {
                                            try {
                                                cellData = formatter.format(frmt.parse(temp.getString("date")));
                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            } catch (Exception ex) {
                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            }
                                        } else if (headers[hCnt].equals("c_date")) {
                                            cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date"))));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_checkdate")) {
                                            String checkdate = (StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate"));
                                            if (!StringUtil.isNullOrEmpty(checkdate)) {
                                                try {
                                                    cellData = formatter.format(frmt.parse(checkdate));
                                                } catch (Exception ex) {
                                                    checkdate = (StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate"));
                                                }
                                            }
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_checkno")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_checkno")) ? temp.getString("d_checkno") : temp.getString("c_checkno"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_transactionDetailsBankBook")) {
                                            if (!temp.getString("d_transactionDetailsBankBook").equalsIgnoreCase("Transfer")) {
                                                cellData = (StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsBankBook")) ? temp.getString("c_transactionDetailsBankBook") : temp.getString("d_transactionDetailsBankBook"));
                                            } else {
                                                cellData = temp.getString("c_transactionDetailsBankBook");
                                            }
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_accountname")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_acccode")) {
                                            cellData = "";
                                            if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                                cellData = temp.getString("c_acccode");
                                            } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                                cellData = temp.getString("d_acccode");
                                            }
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_transactionID")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_transactionID")) ? temp.getString("d_transactionID") : temp.getString("c_transactionID"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_transactionDetails")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_transactionDetails")) ? temp.getString("d_transactionDetails") : temp.getString("c_transactionDetails"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_entryno") && !(temp.isNull(headers[hCnt])) && report != 117) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (report == 116 && (headers[hCnt].equals("d_amount_open") || headers[hCnt].equals("c_amount_open") || headers[hCnt].equals("c_amount_period") || headers[hCnt].equals("d_amount_period") || headers[hCnt].equals("openingamount") || headers[hCnt].equals("endingamount"))) {
                                            if (cellData.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                                cellData = "0.0";
                                            }
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (report == 116 && (headers[hCnt].equals("ytd_d_amount") || headers[hCnt].equals("ytd_c_amount") || headers[hCnt].equals("ytd_c_amount_open") || headers[hCnt].equals("ytd_d_amount_open") || headers[hCnt].equals("c_amount") || headers[hCnt].equals("d_amount") || headers[hCnt].equals("c_period") || headers[hCnt].equals("d_period")  || headers[hCnt].equals("periodBalance"))) {
                                            if (cellData.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                                cellData = "0.0";
                                            }
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (report == 116 && (headers[hCnt].equals("ytd_d_amount_period") || headers[hCnt].equals("ytd_c_amount_period") || headers[hCnt].equals("ytd_d_amount") || headers[hCnt].equals("ytd_c_amount"))) {
                                            if (cellData.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                                cellData = "0.0";
                                            }
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        }else if (headers[hCnt].equals("d_date")) {
                                            if (report == 117) {//Ledger T report
                                                if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling case to show netbalnce in general ledger
                                                    double netbalnce = temp.optDouble("netbalance", 0.0);
                                                    cellData = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                                    recordData += "<td>" + cellData + "&nbsp;</td>";
                                                } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general ledger                                                                                           
                                                    recordData += "<td>" + cellData + "&nbsp;</td>";
                                                } else if(!temp.optString("d_date","").equals("") ||!temp.optString("c_date","").equals("")  ){
                                                    cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date"))));
                                                    recordData += "<td>" + cellData + "&nbsp;</td>";
                                                }else{
                                                    recordData += "<td>&nbsp;</td>";
                                                }
                                            } else {
                                                cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date"))));
                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            }
                                        } else if (headers[hCnt].equals("d_accountname")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_description")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.optString("c_description")) ? temp.optString("d_description") : temp.optString("c_description"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        }  else if (headers[hCnt].equals("d_acccode")) {
                                            cellData = "";
                                            if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                                cellData = temp.getString("d_acccode");
                                            } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                                cellData = temp.getString("c_acccode");
                                            }
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_reconciledate")) {
                                            cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_reconciledate")) ? temp.getString("c_reconciledate") : temp.getString("d_reconciledate"))));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_transactionID")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_transactionID")) ? temp.getString("c_transactionID") : temp.getString("d_transactionID"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_amount")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? ( report == 771 ? temp.getString("c_amount") : "&nbsp;"): temp.getString("d_amount"));
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("c_amount")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? ( report == 771 ? temp.getString("d_amount") : "&nbsp;") : temp.getString("c_amount"));
                                            cellData = withoutHtmlCurrencyRender(cellData, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_amountinacc")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_amountinacc")) ? temp.getString("c_amountinacc") : temp.getString("d_amountinacc"));
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_amountintransactioncurrency")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_amountintransactioncurrency")) ? temp.getString("c_amountintransactioncurrency") : temp.getString("d_amountintransactioncurrency"));
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_transactionDetails")) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_transactionDetails")) ? temp.getString("c_transactionDetails") : temp.getString("d_transactionDetails"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("d_entryno") && !(temp.isNull(headers[hCnt]))) {
                                            cellData = (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno"));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (report == 27 && headers[hCnt].equals("transactionDetails") && StringUtil.isNullOrEmpty(temp.optString("transactionDetails", ""))) {// for JE Export Report
                                            cellData = StringUtil.DecodeText(temp.optString("journalEntryDetailsDescription", ""));
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if ((temp.isNull(headers[hCnt])) && !(headers[hCnt].equals("invoiceno")) && !(headers[hCnt].equals("invoicedate"))) {
                                                recordData += "<td>&nbsp;</td>";
                                        } else if (!(temp.isNull(headers[hCnt])) && headers[hCnt].equals("perioddepreciation")) {
                                            double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                            String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                                            if (adj < 0.0001) {
                                                cellData = "";
                                            } else {
                                                cellData = currency;
                                            }
                                            recordData += "<td>" + cellData + "&nbsp;</td>";
                                        } else if (titles[hCnt].equals("Opening Balance") || titles[hCnt].equals("Asset Value")) {
                                            String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                            cellData = currency;
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("reorderquantity") || headers[hCnt].equals("reorderlevel") || headers[hCnt].equals("balancequantity") || headers[hCnt].equals("quantity") || headers[hCnt].equals("leasedQuantity") || headers[hCnt].equals("lockquantity") || headers[hCnt].equals("consignquantity") || headers[hCnt].equals("venconsignquantity") || headers[hCnt].equals("recycleQuantity") || headers[hCnt].equals("received") || headers[hCnt].equals("delivered") || headers[hCnt].equals("reservestock")) {
                                            double bal = 0f;
                                            if (!temp.getString(headers[hCnt]).equals("N/A") && !StringUtil.isNullOrEmpty(temp.getString(headers[hCnt]))) {    //ERP-9768 [SJ]
                                                bal = Double.parseDouble(temp.getString(headers[hCnt]));
                                                cellData = authHandler.formattedQuantity(bal, companyid) + " " + unitName;
                                            }
                                            else{
                                                cellData=temp.getString(headers[hCnt]);
                                             
                                            }
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("frommonth")) {
                                            cellData = htmlPeriodRender(cellData);
                                            recordData += "<td align=\"center\">" + cellData + "&nbsp;</td>";
                                        }
                                         else if (headers[hCnt].equals("limit")) {
                                             double creditlimit = Double.parseDouble((StringUtil.isNullOrEmpty(temp.getString(headers[hCnt])))?"0.00":temp.getString(headers[hCnt]));
                                            cellData = authHandler.formattedAmount(creditlimit, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("exceededamount")) {
                                            double exceededamount = Double.parseDouble((StringUtil.isNullOrEmpty(temp.getString(headers[hCnt])))?"0.00":temp.getString(headers[hCnt]));
                                            cellData = authHandler.formattedAmount(exceededamount, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("amountcost") || headers[hCnt].equals("amountsales") || headers[hCnt].equals("costprice") || headers[hCnt].equals("unitprice")) {
                                            double amount = Double.parseDouble((StringUtil.isNullOrEmpty(temp.getString(headers[hCnt]))) ? "0.00" : temp.getString(headers[hCnt]));
                                            cellData = authHandler.formattedAmount(amount, companyid);
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else if (headers[hCnt].equals("jedate") ) {
                                            cellData = temp.optString(headers[hCnt],"");
                                            try {
                                                cellData = userFormat.format(dateFormatForTapReturn.parse(cellData));//ERP-36142
                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            } catch (Exception e) {
                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            }
                                        } else if (headers[hCnt].equals("taxamount") || headers[hCnt].equals("transactionexchangerate")) {
                                            recordData += "<td align=\"right\">" + cellData + "&nbsp;</td>";
                                        } else {
                                            if (titles[hCnt].equals("Opening Balance Type")) {
                                                try {
                                                    String str1 = "";
                                                    if (temp.getString(headers[hCnt]).equalsIgnoreCase("-")) {
                                                        str1 = "N/A";
                                                    } else {
                                                        double bal = Double.parseDouble(temp.getString(headers[hCnt]));
                                                        str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                                        if (str1.equals("")) {
                                                            str1 = "N/A";
                                                        }
                                                    }
                                                    cellData = str1;
                                                } catch (Exception ex) {
                                                    System.out.print(ex.getMessage());
                                                }

                                                recordData += "<td>" + cellData + "&nbsp;</td>";
                                            } else if (headers[hCnt].equals("subtotal") 
                                                    || headers[hCnt].equals("discount") 
                                                    || headers[hCnt].equals("discountinbase") 
                                                    || headers[hCnt].equals("productTotalAmount") 
                                                    || headers[hCnt].equals("debitAmount") 
                                                    || headers[hCnt].equals("creditAmount") 
                                                    || headers[hCnt].equals("amount") 
                                                    || headers[hCnt].equals("amountinbase")
                                                    || headers[hCnt].equals("totalprofitmargin") 
                                                    || headers[hCnt].equals("discountamountinbase") 
                                                    || headers[hCnt].equals("amountwithouttax") 
                                                    || headers[hCnt].equals("totaltaxamount")
                                                    || headers[hCnt].equals("discountval")
                                                    || headers[hCnt].equals("rowprdiscount")
                                                    || headers[hCnt].equals("deductDiscount")
                                                    || headers[hCnt].equals("taxamount")
                                                    || headers[hCnt].equals("amountBeforeTax")
                                                    || headers[hCnt].equals("discounttotal")) {
                                                String str = "<td";
                                                try {
                                                    double amount = temp.optDouble(headers[hCnt], 0.00);
                                                    cellData = authHandler.formattedAmount(amount, companyid);
                                                    str = "<td align=\"right\">";
                                                    recordData += str + StringUtil.DecodeText(cellData) + "&nbsp;</td>";
                                                } catch (Exception e) {
                                                    recordData += str + StringUtil.DecodeText(cellData) + "&nbsp;</td>";
                                                }
                                            } else {
                                                String beforeReplaceCellData = "";
                                                try {
                                                    if (headers[hCnt].equalsIgnoreCase("otherwise")) {
                                                        if (cellData.equalsIgnoreCase("true")) {
                                                            cellData = "No";
                                                            recordData += "<td>" + StringUtil.DecodeText(cellData) + "&nbsp;</td>";
                                                        } else {
                                                            cellData = "Yes";
                                                            recordData += "<td>" + StringUtil.DecodeText(cellData) + "&nbsp;</td>";
                                                        }
                                                    } else {
                                                        beforeReplaceCellData = new String(cellData);
                                                        cellData = cellData.replaceAll("%", "%25").replaceAll("\\+", "%2B");   //ERP-27500
                                                        recordData += "<td>" + URLDecoder.decode(cellData, Constants.ENCODING) + "&nbsp;</td>";
                                                    }
                                                } catch (Exception e) {
                                                    recordData += "<td>" + beforeReplaceCellData + "&nbsp;</td>";
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (report == 116 && (headers[hCnt].equals("d_amount_open") || headers[hCnt].equals("c_amount_open") || headers[hCnt].equals("c_amount_period") || headers[hCnt].equals("d_amount_period") || headers[hCnt].equals("c_period") || headers[hCnt].equals("d_period")  || headers[hCnt].equals("periodBalance") || headers[hCnt].equals("openingamount") || headers[hCnt].equals("endingamount"))) {
                                    String tempString="&nbsp;";
                                    if (!extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                        tempString = "0.0";
                                    }
                                    tempString = withoutHtmlCurrencyRender(tempString, companyid);
                                    recordData += "<td align=\"right\">" + tempString + "&nbsp;</td>";
                                } else if (headers[hCnt].equals("c_description")) {
                                    String tempString = (StringUtil.isNullOrEmpty(temp.optString("c_description")) ? temp.optString("d_description") : temp.optString("c_description"));
                                    recordData += "<td>" + tempString + "&nbsp;</td>";
                                } else if (report == 116 && (headers[hCnt].equals("ytd_d_amount") || headers[hCnt].equals("ytd_c_amount") || headers[hCnt].equals("ytd_c_amount_open") || headers[hCnt].equals("ytd_d_amount_open") || headers[hCnt].equals("c_amount") || headers[hCnt].equals("d_amount"))) {
                                    String tempString = "&nbsp;";
                                    if (!extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                        tempString = "0.0";
                                    }
                                    tempString = withoutHtmlCurrencyRender(tempString, companyid);
                                    recordData += "<td align=\"right\">" + tempString + "&nbsp;</td>";
                                } else if (headers[hCnt].equals("c_checkdate")) {
                                    String checkdate = (StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate"));
                                    if (!StringUtil.isNullOrEmpty(checkdate)) {
                                        try {
                                            checkdate = formatter.format(frmt.parse(checkdate));
                                        } catch (Exception ex) {
                                            checkdate = (StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate"));
                                        }
                                    }
                                    recordData += "<td>" + checkdate + "&nbsp;</td>";
                                } else if (report == 116 && (headers[hCnt].equals("ytd_d_amount_period") || headers[hCnt].equals("ytd_c_amount_period") || headers[hCnt].equals("ytd_d_amount") || headers[hCnt].equals("ytd_c_amount"))) {
                                     String tempString="&nbsp;";
                                    if (!extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                        tempString = "0.0";
                                    }
                                    tempString = withoutHtmlCurrencyRender(tempString, companyid);
                                    recordData += "<td align=\"right\">" + tempString + "&nbsp;</td>";
                                } else {
                                    recordData += "<td>&nbsp;</td>";
                                }
                                
                            }
                        }
                        ashtmlString += recordData + "</tr>";
                        t++;
                    } else {
                        atempstr = "";
                    }
                }//inner for
            /* commenting this code for ERP-12840 ticket 
             * if (report == 116) { //116:Trial Balance
                    String recordData = "<tr><td align=\"center\">&nbsp;</td><td>Total</td>";
                    for (int h = 1; h < headers.length; h++) {
                        if (headers[h].equals("c_amount")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("d_amount")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("c_amount_open")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalOpenCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("d_amount_open")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalOpenDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("c_amount_period")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalPeriodCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("d_amount_period")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalPeriodDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_c_amount_open")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDOpenCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_d_amount_open")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDOpenDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_c_amount_period")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDPeriodCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_d_amount_period")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDPeriodDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_c_amount")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDCre), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else if (headers[h].equals("ytd_d_amount")) {
                            recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(totalYTDDeb), currencyid, isCurrencyCode) + "&nbsp;</td>";
                        } else {
                            recordData += "<td>&nbsp;</td>";
                        }                        
                    }
                    ashtmlString += recordData + "</tr>";
                }else */if(report == 4 || report==8 || report==301  || report==827){ //4: Payment Received;  8: Make Payment ,827day end collection report
                        String gridHeader = (Arrays.toString(titles));
                        if (gridHeader.contains("Amount Paid (In Base Currency)")
                                || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                                || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                                || gridHeader.contains("Total Amount (In Base Currency)(SG Dollar(SGD)) ")
                                ){
                        String recordData = "<tr><td align=\"center\">&nbsp;</td><td><b>" + messageSource.getMessage("acc.common.total", null, RequestContextUtils.getLocale(request)) + "</b></td>";

                        for (int h = 1; h < titles.length; h++) {
                            if(titles[h].equals("Amount Paid (In Base Currency)")){
                                recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid) + "&nbsp;</td>";
                            }else if(titles[h].equals("Amount Received (In Base Currency)")){
                                recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid) + "&nbsp;</td>";
                            }else if(titles[h].equals("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")){
                                recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(grandTotalAmountWithTaxInBaseCurrency), currencyid, isCurrencyCode, companyid) + "&nbsp;</td>";
                            }else if(titles[h].equals("Total Amount (In Base Currency)(SG Dollar(SGD)) ")){
                                recordData += "<td align=\"right\">" + htmlCurrencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid) + "&nbsp;</td>";
                            }
                            else{
                                recordData +="<td></td>";
                            }
                        }
                        ashtmlString += recordData + "</tr>";
                    }
                }else if(report == 810 || report == 807 ||(report == 125 && isBasedOnProduct)){    //report == 807 :  Monthly Sales by Product subject to GST , report==810 : "Sales by Product Category Detail Report" , report == 125 : Monthly Sales by Product
                    String recordData = "<tr><td align=\"center\">&nbsp;</td><td><b>" + messageSource.getMessage("acc.common.total", null, RequestContextUtils.getLocale(request)) + "</b></td>";
                    
                    for (int h = 1; h < headers.length; h++) {
                        if(headers[h].equals("amountinbase")){
                            recordData += "<td align=\"right\"><b>" + withoutHtmlCurrencyRender(String.valueOf(grandTotalInBaseCurrency), companyid) + "<b>&nbsp;</td>";
                        }else{
                            recordData +="<td></td>";
                        }
                    }
                    ashtmlString += recordData + "</tr>";
                }
                ashtmlString += "</table>";
                ashtmlString += "</center>";
                t--;
                if (t != repArr.length() - 1) {
                    ashtmlString += atempstr;
                }
            }//outer for
            ashtmlString += "<div style='float: left; padding-top: 3px; padding-right: 5px;'>"
                    + "<button id = 'print' title='Print Invoice' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>" + messageSource.getMessage("acc.common.print", null, RequestContextUtils.getLocale(request)) + "</button>"
                    + "</div>";
            ashtmlString += "</body>"
                    + "</html>";
            response.getOutputStream().write(ashtmlString.getBytes());
            response.getOutputStream().flush();
        } catch (SessionExpiredException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (IOException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            String str;
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        }
    }

    private Set<String> getUnitPriceAndAmountDataIndexSet(int report, boolean displayUnitPriceandAmtInSalesDocPerm,boolean displayUnitPriceandAmtInPurchaseDocPerm) {
        Set<String> unitPriceAmountheaderName = new HashSet<>();
        Set<Integer> reportSet = new HashSet<>();
        reportSet.add(198);  //Product
        reportSet.add(199);  //Product List by category
        reportSet.add(1214); //Price List
        reportSet.add(1215); //Customer/Vendor Price List
        if (reportSet.contains(report)) {
            if(!displayUnitPriceandAmtInSalesDocPerm){
                unitPriceAmountheaderName.add("initialsalesprice");
                unitPriceAmountheaderName.add("saleprice");
                unitPriceAmountheaderName.add("price");
            }
            if(!displayUnitPriceandAmtInPurchaseDocPerm){
                unitPriceAmountheaderName.add("initialprice");
                unitPriceAmountheaderName.add("mrprate");
                unitPriceAmountheaderName.add("purchaseprice");
                unitPriceAmountheaderName.add("price");
            }
        } else {
            unitPriceAmountheaderName.add("taxamount");
            unitPriceAmountheaderName.add("rowTaxAmount");
            unitPriceAmountheaderName.add("termamount");
            unitPriceAmountheaderName.add("amountbeforegst");
            unitPriceAmountheaderName.add("orderamountwithTax");
            unitPriceAmountheaderName.add("amount");
            unitPriceAmountheaderName.add("amountinbase");
            unitPriceAmountheaderName.add("amountdue");
            unitPriceAmountheaderName.add("rate");
            unitPriceAmountheaderName.add("rateIncludingGst");
            unitPriceAmountheaderName.add("orderrate");
            unitPriceAmountheaderName.add("amountForExcelFile");
            unitPriceAmountheaderName.add("amountwithouttax");
            unitPriceAmountheaderName.add("totaltaxamount");
            unitPriceAmountheaderName.add("subtotal");
            unitPriceAmountheaderName.add("productTotalAmount");
            unitPriceAmountheaderName.add("discount");
            unitPriceAmountheaderName.add("discountinbase");
            unitPriceAmountheaderName.add("discountamountinbase");
        }
        return unitPriceAmountheaderName;
    }

    public boolean getDisplayUnitPriceandAmtInDocPerm(HttpServletRequest request, int report,boolean isSales) throws SessionExpiredException {
        boolean displayUnitPriceandAmtInDocPerm = true;
        if (isSales) {// For sales Document
            Set<Integer> salesReportSet = new HashSet<>();
            salesReportSet.add(1);  //SO
            salesReportSet.add(2);  //SI
            salesReportSet.add(50); //CQ
            salesReportSet.add(53); //DO
            salesReportSet.add(61); //Sales Return
            salesReportSet.add(198);//Product
            salesReportSet.add(199);//Product List by category
            salesReportSet.add(835);//Product summary
            salesReportSet.add(1214);//Price List
            salesReportSet.add(1215);//Customer/Vendor Price List
            salesReportSet.add(12);//Customer/Vendor Price List for PDF Only
            
            if (salesReportSet.contains(report)) {
                int unitPriceAndAmountPermCode = sessionHandlerImpl.getPerms(request, Constants.UNITPRICE_AMOUNT_PERMCODE);
                if ((unitPriceAndAmountPermCode & Constants.DISPLAY_UP_AMT_SLAES_DOUCUMENT_PERMCODE) != Constants.DISPLAY_UP_AMT_SLAES_DOUCUMENT_PERMCODE) { //If this condition is true means user does not have permission to display unit price and related amounts
                    displayUnitPriceandAmtInDocPerm = false;
                }
            }
        } else {//For purchase Documents
            Set<Integer> purchaseReportSet = new HashSet<>();
            purchaseReportSet.add(5);//PO
            purchaseReportSet.add(6);//GR
            purchaseReportSet.add(54);//GRO
            purchaseReportSet.add(57);//VQ
            purchaseReportSet.add(58);//Purchase Requisition
            purchaseReportSet.add(63);//Purchase Return
            purchaseReportSet.add(198);//Product
            purchaseReportSet.add(199);//Product List by category
            purchaseReportSet.add(835);//Product summary
            purchaseReportSet.add(1214);//Price List
            purchaseReportSet.add(1215);//Customer/Vendor Price List
            purchaseReportSet.add(12);//Customer/Vendor Price List for PDF Only

            if (purchaseReportSet.contains(report)) {
                int unitPriceAndAmountPermCode = sessionHandlerImpl.getPerms(request, Constants.UNITPRICE_AMOUNT_PERMCODE);
                if ((unitPriceAndAmountPermCode & Constants.DISPLAY_UP_AMT_PURCHASE_DOUCUMENT_PERMCODE) != Constants.DISPLAY_UP_AMT_PURCHASE_DOUCUMENT_PERMCODE) { //If this condition is true means user does not have permission to display unit price and related amounts
                    displayUnitPriceandAmtInDocPerm = false;
                }
            }
        }
        return displayUnitPriceandAmtInDocPerm;
    }
    
    public void writeDataToFile(String filename, String fileType, ByteArrayOutputStream baos, HttpServletResponse response) throws ServiceException {
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "." + fileType + "\"");
                response.setContentType("application/octet-stream");
            response.setContentLength(baos.size());
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            try {
                response.getOutputStream().println("{\"valid\": false}");
            } catch (IOException ex) {
                Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeXLSDataToFile(String filename, String fileType, HSSFWorkbook wb, HttpServletResponse response) throws ServiceException {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xls\"");
            wb.write(response.getOutputStream());
            response.getOutputStream().close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            try {
                response.getOutputStream().println("{\"valid\": false}");
            } catch (IOException ex) {
                Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void writeXLSXDataToFile(String filename, String fileType, XSSFWorkbook wb, HttpServletResponse response) throws ServiceException {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xlsx\"");
            response.setCharacterEncoding("UTF-8");
            wb.write(response.getOutputStream());
            response.getOutputStream().close();
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            try {
                response.getOutputStream().println("{\"valid\": false}");
            } catch (IOException ex) {
                Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createExportFiles(ByteArrayOutputStream baos, String ext, String filename) {
        String destinationDirectory;
        ext = "." + ext;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath();
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ext);
            fileOut.write(baos.toByteArray());
            fileOut.flush();
            fileOut.close();
        } catch (Exception ex) {
            System.out.println("\nExport file write [success/failed] " + ex);
        }
    }
    public String createExcelExportFile(Workbook workbook, String ext, String fileName){
        String destinationDirectory;
        String filepath = "";
        ext = "." + ext;
        FileOutputStream fileOut = null;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath();
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = fileName.substring(fileName.lastIndexOf("."));
            }
            filepath = destinationDirectory + "/" + fileName + ext;
            fileOut = new FileOutputStream(filepath);
            workbook.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (Exception ex) {
            System.out.println("\nExport file write [success/failed] " + ex);
        }
        return filepath;
    }

    public void addComponyLogo(Document d, JSONObject requestJobj, Boolean logoFlag) throws ServiceException {
        try {
            String get = requestJobj.optString("get");
            PdfPTable table = new PdfPTable(3);
            imgPath = ProfileImageServlet.getProfileImagePath(requestJobj, true, null);//getImgPath(request);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{33, 33, 34});
            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);

            //Cell for Logo
            if (logoFlag) {
                PdfPCell cell = null;
                try {
                    Image img = Image.getInstance(imgPath);
                    cell = new PdfPCell(img);
                } catch (Exception e) {
                    //companyName = sessionHandlerImplObj.getCompanyName(request);
                    cell = new PdfPCell();
                }
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);

                table.addCell(cell);
            } else {
                table.addCell(blankCell);
            }
            //For bold company name
//            PdfPTable tableHead = new PdfPTable(1);
//            tableHead.setHorizontalAlignment(Element.ALIGN_CENTER);
//            tableHead.setWidthPercentage(100);

            /*Removed _version from filename if its appearing in filename in PDF print */
            String companyname = "";//ERP-12220
            String filename = requestJobj.optString("filename");
            int lastIndex = filename.lastIndexOf("_v");
            if (lastIndex != -1) {
                filename = filename.substring(0, lastIndex);
            }
            if(get.equalsIgnoreCase("125")){
                filename = filename.replaceAll("_", " ");
            }          
            if ("true".equals(requestJobj.optString("isRFQ"))) {
                companyname = filename;
            } else {
                companyname = requestJobj.optString(Constants.companyname) + "\n";
                companyname += authHandlerDAOObj.getCompanyAddress(requestJobj.optString(Constants.companyKey)) + "\n\n";
                companyname += filename;
            }
//            PdfPCell companyHead = new PdfPCell(new Paragraph(fontFamilySelector.process(companyname, FontContext.TABLE_HEADER)));
//            companyHead.setHorizontalAlignment(Element.ALIGN_CENTER);
//            companyHead.setBorder(0);
//            tableHead.addCell(companyHead);

            //Cell For Company Addrs

            companyname = StringUtil.serverHTMLStripper(companyname);
            PdfPCell companyCell = new PdfPCell(new Paragraph(fontFamilySelector.process(companyname, FontContext.TABLE_DATA)));
            companyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            companyCell.setBorder(0);
//            tableHead.addCell(companyCell);
            table.addCell(companyCell);

            table.addCell(blankCell);
            d.add(table);
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addComponyLogo", e);
        }
    }

    public String getImgPath(HttpServletRequest req) throws SessionExpiredException {
        String requestedFileName = "";
        String companyId = null;
        try {
            companyId = sessionHandlerImplObj.getCompanyid(req);
        } catch (Exception ee) {
        }
        if (StringUtil.isNullOrEmpty(companyId)) {
            String domain = URLUtil.getDomainName(req);
            if (!StringUtil.isNullOrEmpty(domain)) {
                companyId = sessionHandlerImplObj.getCompanyid(req);
                requestedFileName = "/original_" + companyId + ".png";
            } else {
                requestedFileName = "logo.gif";
            }
        } else {
            requestedFileName = companyId + ".png";
        }
        String fileName = storageHandlerImplObj.GetProfileImgStorePath() + requestedFileName;
        return fileName;
    }

    public void addTitleSubtitle(Document d) throws ServiceException {
        try {
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontBold.setColor(tColor);
//            fontRegular.setColor(tColor);
            PdfPTable table = new PdfPTable(1);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.setWidthPercentage(100);
            table.setSpacingBefore(6);

            //Report Title
            PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(config.getString("title"), FontContext.REPORT_TITLE, tColor)));
            cell.setBorder(0);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            //Report Subtitle(s)
            String[] SubTitles = config.getString("subtitles").split("~");// '~' as separator
            for (int i = 0; i < SubTitles.length; i++) {
                cell = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(SubTitles[i], FontContext.FOOTER_NOTE)))));
                cell.setBorder(0);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            table.setSpacingAfter(6);
            d.add(table);

        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTitleSubtitle", e);
        }
    }

    public int addTable(int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] colwidth2, String[] colHeader, String[] widths, String[] align, Document document, JSONObject requestJobj) throws ServiceException {
        try {
            KwlReturnObject compaccresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), requestJobj.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(requestJobj.optString(Constants.dateformatid), requestJobj.optString(Constants.timezonedifference), true);
            DateFormat frmt = authHandler.getDateOnlyFormat();
            String currencyid = requestJobj.optString(Constants.globalCurrencyKey);
            String companyid = "";
            int mode = requestJobj.optInt("get");
            
            boolean displayUnitPriceandAmtInSalesDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInSalesDocPerm", true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInPurchaseDocPerm", true);
            Set<String> unitPriceAmountheaderName = new HashSet<>();
            if (!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) {
                unitPriceAmountheaderName = getUnitPriceAndAmountDataIndexSet(mode,displayUnitPriceandAmtInSalesDocPerm,displayUnitPriceandAmtInPurchaseDocPerm);
            }
            boolean isBasedOnProduct=requestJobj.optBoolean("isBasedOnProduct",false);
            double totalCre = 0,grandTotalInBaseCurrency=0;
            double totalDeb = 0;
            boolean isCurrencyCode = false;
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontSmallRegular.setColor(tColor);
            PdfPTable table;
            float[] tcol;
            tcol = new float[colHeader.length + 1];
            tcol[0] = 40;
            for (int i = 1; i < colHeader.length + 1; i++) {
                tcol[i] = Float.parseFloat(widths[i - 1]);
            }
            table = new PdfPTable(colHeader.length + 1);
            table.setWidthPercentage(tcol, document.getPageSize());
            table.setSpacingBefore(15);
            table.setSplitLate(false);              // setSplitLate property of PDFTable set to false as it allows immediate splitting. ERP-26688
            PdfPCell h2 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process("No.", FontContext.FOOTER_NOTE, tColor)))));
            if (config.getBoolean("gridBorder")) {
                h2.setBorder(PdfPCell.BOX);
            } else {
                h2.setBorder(0);
            }
            h2.setPadding(4);
            h2.setBorderColor(Color.GRAY);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h2);
            PdfPCell h1 = null;
            for (int hcol = stcol; hcol < colwidth2.length; hcol++) {
                String headerStr = StringUtil.serverHTMLStripper(colHeader[hcol]);
                if (align[hcol].equals("currency") && !colHeader[hcol].equals("")) {
                    String currency = currencyRender("", currencyid, companyid);
                    h1 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(headerStr + "(" + currency + ")", FontContext.FOOTER_NOTE, tColor)))));
                } else {
                    h1 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(headerStr, FontContext.FOOTER_NOTE, tColor)))));
                }
                h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (config.getBoolean("gridBorder")) {
                    h1.setBorder(PdfPCell.BOX);
                } else {
                    h1.setBorder(0);
                }
                h1.setBorderColor(Color.GRAY);
                h1.setPadding(4);
                table.addCell(h1);
            }
            table.setHeaderRows(1);

            String fixedAccId = "";
            int count = 0;
            double grandCreTotal = 0;
            double grandDebTotal = 0;
            String accountId = requestJobj.optString("accountid");
            companyid = requestJobj.optString("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }

            for (int row = strow; row < stprow; row++) {
                JSONObject temp = store.getJSONObject(row);

                if (mode == 116 || mode == 117) {

                    if (mode == 117 && (accountId.equalsIgnoreCase("All") || accountId.split(",").length > 1))//for all account leadger
                    {
                        String traverseAccId = temp.getString("accountid");
                        if (!fixedAccId.equals(traverseAccId)) {
                            if (row != 0) {
                                table = addTotalRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "Total", companyid);
                                grandCreTotal += totalCre;
                                grandDebTotal += totalDeb;
                                totalCre = 0;
                                totalDeb = 0;
                                count = 0;
                            }
                            addRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "", true);
                            if(!StringUtil.isNullOrEmpty(temp.getString("accCode"))){
                                addRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, temp.getString("accCode"), false);
                            }else if(temp.has("accCodeName")){
                                addRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, temp.getString("accCodeName"), false);
                            }
                            fixedAccId = traverseAccId;
                        }
                        if (temp.has("c_amount")) {
                            totalCre = totalCre + Double.parseDouble(temp.getString("c_amount") != "" ? temp.getString("c_amount") : "0");
                        }
                        if (temp.has("d_amount")) {
                            totalDeb = totalDeb + Double.parseDouble(temp.getString("d_amount") != "" ? temp.getString("d_amount") : "0");
                        }
                    } else {
                        if (temp.has("c_amount")) {
                            totalCre = totalCre + Double.parseDouble(temp.getString("c_amount") != "" ? temp.getString("c_amount") : "0");
                        }
                        if (temp.has("d_amount")) {
                            totalDeb = totalDeb + Double.parseDouble(temp.getString("d_amount") != "" ? temp.getString("d_amount") : "0");
                        }
                        if (row == 0) {
                            addRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "", true);
                        }
                    }
                }

                if (mode == 152) {
                    grandCreTotal = grandCreTotal + Double.parseDouble(temp.getString("amountinbase") != "" ? temp.getString("amountinbase") : "0");
                    grandDebTotal = grandDebTotal + Double.parseDouble(temp.getString("amountinbasewithtax") != "" ? temp.getString("amountinbasewithtax") : "0");
                } else if (mode == 4 || mode == 8 || mode==827 || mode == 810 || mode == 807 || mode == 125) { //4: Payment Received;  8: Make Payment ,report==810 : "Sales by Product Category Detail Report"
                    grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbase")) ? temp.getString("amountinbase") : "0");
                }

                if (mode == 117 && (temp.optBoolean("isnetbalance", false) || temp.optBoolean("isgroupname", false))) { //In case of For Ledger T Report
                    h2 = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA)));
                    count = count - 1;//for adjusting serial number while printing 
                } else {
                    h2 = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(count + 1), FontContext.TABLE_DATA)));
                }
                if (config.getBoolean("gridBorder")) {
                    h2.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h2.setBorder(0);
                }
                h2.setPadding(4);
                h2.setBorderColor(Color.GRAY);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);
                boolean isBoldRow = false;
                for (int col = 0; col < colwidth2.length; col++) {
                    Paragraph para = null;
                    String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
                    String transactionCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
                    
                    if ((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(colwidth2[col])) {
                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(Constants.UPAndAmtDispalyValueNoPerm), FontContext.TABLE_DATA));
                    } else if (align[col].equals("currency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String currency =(mode==115||mode==67||mode==66)?currencyRender(temp.getString(colwidth2[col]), currencyid, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(colwidth2[col]), companyid);
                        para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("withoutcurrency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String currency = withoutCurrencyRender(temp.getString(colwidth2[col]), companyid);
                        para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("rowcurrency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String withCurrency = (mode==115||mode==67||mode==66)?currencyRender(temp.getString(colwidth2[col]), rowCurrencyId, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(colwidth2[col]), companyid);
                        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("unitpricecurrency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String withCurrency = (mode==152)? withoutCurrencyRender(temp.getString(colwidth2[col]), companyid):unitPriceCurrencyRender(temp.getString(colwidth2[col]), rowCurrencyId, isCurrencyCode, companyid);
                        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("withoutrowcurrency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String withCurrency = withoutCurrencyRender(temp.getString(colwidth2[col]), companyid);
                        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("transactioncurrency") && !temp.optString(colwidth2[col], "").equals("")) {
                        String withCurrency = currencyRender(temp.getString(colwidth2[col]), transactionCurrencyId, isCurrencyCode, companyid);
                        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("date") && !temp.optString(colwidth2[col], "").equals("")) {
                        try {
                            String d1 = formatter.format(frmt.parse(temp.getString(colwidth2[col])));
                            para = new Paragraph(fontFamilySelector.process(d1, FontContext.TABLE_DATA));
                        } catch (Exception ex) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]), FontContext.TABLE_DATA));
                        }
                    } else if (colwidth2[col].equals("taxrate") || colwidth2[col].equals("permargin") && !colHeader[col].equals("")) {
                        para = new Paragraph(fontFamilySelector.process(htmlPercentageRender(temp.getString(colwidth2[col]), true), FontContext.TABLE_DATA));
                    } else if (mode == 116 && (colwidth2[col].equals("d_amount_open") || colwidth2[col].equals("c_amount_open") || colwidth2[col].equals("c_amount_period") || colwidth2[col].equals("d_amount_period") || colwidth2[col].equals("c_period") || colwidth2[col].equals("d_period") || colwidth2[col].equals("periodBalance") || colwidth2[col].equals("openingamount") || colwidth2[col].equals("endingamount"))) {
                        String tempString = !StringUtil.isNullOrEmpty(temp.optString((colwidth2[col]), "")) ? temp.optString((colwidth2[col]), "") : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(tempString), FontContext.TABLE_DATA));
                    } else if (mode == 116 && (colwidth2[col].equals("ytd_d_amount") || colwidth2[col].equals("ytd_c_amount") || colwidth2[col].equals("ytd_c_amount_open") || colwidth2[col].equals("ytd_d_amount_open") || colwidth2[col].equals("c_amount") || colwidth2[col].equals("d_amount"))) {
                        String tempString = !StringUtil.isNullOrEmpty(temp.optString((colwidth2[col]), "")) ? temp.optString((colwidth2[col]), "") : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(tempString), FontContext.TABLE_DATA));
                    } else if (mode == 116 && (colwidth2[col].equals("ytd_d_amount_period") || colwidth2[col].equals("ytd_c_amount_period") || colwidth2[col].equals("ytd_d_amount") || colwidth2[col].equals("ytd_c_amount"))) {
                        String tempString = !StringUtil.isNullOrEmpty(temp.optString((colwidth2[col]), "")) ? temp.optString((colwidth2[col]), "") : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(tempString), FontContext.TABLE_DATA));
                    } else if (mode == 1123 && (colwidth2[col].equals("costprice") || colwidth2[col].equals("unitprice") || colwidth2[col].equals("amountcost") || colwidth2[col].equals("amountsales"))) {
                        String tempString = !StringUtil.isNullOrEmpty(temp.optString((colwidth2[col]), "")) ? temp.optString((colwidth2[col]), "") : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(tempString), FontContext.TABLE_DATA));
                    } else {
                        if (colwidth2[col].equals("invoiceno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("invoicedate")) {
                            try {
                                para = new Paragraph(fontFamilySelector.process(temp.getString("date").toString(), FontContext.TABLE_DATA));
                            } catch (Exception ex) {
                                para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]), FontContext.TABLE_DATA));
                            }
                        } else if (align[col].equals("quantity")) {
                            String str;
                            try {
                                double bal = Double.parseDouble(temp.optString(colwidth2[col],"0.00"));
                                str = authHandler.formattedQuantity(bal, companyid);
                            } catch (NumberFormatException e) {
                                str = temp.optString(colwidth2[col],"0.00");
                            }
                            para = new Paragraph(fontFamilySelector.process(str, FontContext.TABLE_DATA));
                        } else if ((temp.isNull(colwidth2[col])) && !(colwidth2[col].equals("invoiceno")) && !(colwidth2[col].equals("invoicedate"))) {
                            para = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_date")) {
                            para = new Paragraph(fontFamilySelector.process(formatter.format(frmt.parse(temp.getString("c_date").toString() == "" ? temp.getString("d_date") : temp.getString("c_date"))), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_accountname")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_accountname").toString() == "" ? temp.getString("d_accountname").toString() : temp.getString("c_accountname").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_acccode")) {
                            String accCode = "";
                            if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            }
                            para = new Paragraph(fontFamilySelector.process(accCode, FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_entryno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_entryno").toString() == "" ? temp.getString("d_entryno").toString() : temp.getString("c_entryno").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_entryno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_entryno").toString() == "" ? temp.getString("c_entryno").toString() : temp.getString("d_entryno").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_transactionID")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_transactionID").toString() == "" ? temp.getString("d_transactionID").toString() : temp.getString("c_transactionID").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_transactionDetails")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_transactionDetails").toString() == "" ? temp.getString("d_transactionDetails").toString() : temp.getString("c_transactionDetails").toString(), FontContext.TABLE_DATA));
                        }  else if (colwidth2[col].equals("c_transactionDetailsForExpander")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_transactionDetailsForExpander").toString() == "" ? (temp.has("d_transactionDetailsForExpander") ? temp.getString("d_transactionDetailsForExpander").toString() :"") : temp.getString("c_transactionDetailsForExpander").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_date")) {
                            if (mode == 117) {//ledger T report
                                if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling to dispaly Net Balance in general T leadger   
                                    double netbalnce = temp.optDouble("netbalance", 0.0);
                                    String currency = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                    para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                                } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general T leadger                                                                                          
                                    para = new Paragraph(fontFamilySelector.process(temp.getString("d_date"), FontContext.TABLE_DATA));
                                } else {
                                    if(!temp.optString("d_date").equals("") || !temp.getString("c_date").equals("")){
                                        para = new Paragraph(fontFamilySelector.process(formatter.format(frmt.parse(temp.getString("d_date").toString() == "" ? temp.getString("c_date") : temp.getString("d_date"))), FontContext.TABLE_DATA));
                                    }else{
                                        para = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                                    }
                                }
                            } else {
                                para = new Paragraph(fontFamilySelector.process(formatter.format(frmt.parse(temp.getString("d_date").toString() == "" ? temp.getString("c_date") : temp.getString("d_date"))), FontContext.TABLE_DATA));
                            }

                        } else if (colwidth2[col].equals("d_accountname")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_accountname").toString() == "" ? temp.getString("c_accountname").toString() : temp.getString("d_accountname").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_acccode")) {
                            String accCode = "";
                            if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            }
                            para = new Paragraph(fontFamilySelector.process(accCode, FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_transactionID")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_transactionID").toString() == "" ? temp.getString("c_transactionID").toString() : temp.getString("d_transactionID").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_transactionDetails")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_transactionDetails").toString() == "" ? temp.getString("c_transactionDetails").toString() : temp.getString("d_transactionDetails").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_transactionDetailsForExpander")) {
                            if (temp.has("d_transactionDetailsForExpander") || temp.has("c_transactionDetailsForExpander")) {
                                para = new Paragraph(fontFamilySelector.process(temp.getString("d_transactionDetailsForExpander").toString() == "" ? (temp.has("c_transactionDetailsForExpander") ? temp.getString("c_transactionDetailsForExpander").toString():"") : temp.getString("d_transactionDetailsForExpander").toString(), FontContext.TABLE_DATA));
                            }
                        } else if (colwidth2[col].equals("d_transactionDetails")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_transactionDetails").toString() == "" ? temp.getString("c_transactionDetails").toString() : temp.getString("d_transactionDetails").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_transactionDetailsBankBook")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_transactionDetailsBankBook").toString() == "" ? temp.getString("d_transactionDetailsBankBook").toString() : temp.getString("c_transactionDetailsBankBook").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("perioddepreciation")) {
                            double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                            String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                            if (adj < 0.0001) {
                                para = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                            } else {
                                para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                            }
                        } else if (colHeader[col].equals("Opening Balance") || colHeader[col].equals("Asset Value")) {
                            if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                para = new Paragraph(fontFamilySelector.process(temp.getString("openbalance"), FontContext.TABLE_DATA));
                            } else {
                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                            }
                        } else if (colwidth2[col].equals("c_checkno")) {
                            para = new Paragraph(fontFamilySelector.process((StringUtil.isNullOrEmpty(temp.getString("c_checkno")) ? temp.getString("d_checkno") : temp.getString("c_checkno")), FontContext.TABLE_DATA));
                        } 
                        else if (colwidth2[col].equals("limit")) {
                            double creditlimit = Double.parseDouble((StringUtil.isNullOrEmpty(temp.getString("limit")))?"0.00":temp.getString(colwidth2[col]));
                             String creditpurlimit = authHandler.formattedAmount(creditlimit, companyid);
                            para = new Paragraph(fontFamilySelector.process(creditpurlimit, FontContext.TABLE_DATA));
                        }
                        else if (colwidth2[col].equals("reorderquantity") || colwidth2[col].equals("reorderlevel") ||  colwidth2[col].equals("balancequantity") || colwidth2[col].equals("quantity") || colwidth2[col].equals("leasedQuantity") || colwidth2[col].equals("lockquantity") || colwidth2[col].equals("consignquantity") || colwidth2[col].equals("venconsignquantity") || colwidth2[col].equals("recycleQuantity") || colwidth2[col].equals("reservestock") || colwidth2[col].equals("blockquantity")) {
                            String str;
                            try {
                                double bal = Double.parseDouble(temp.getString(colwidth2[col]));
                                str = authHandler.formattedQuantity(bal, companyid);
                            } catch (NumberFormatException e) {
                                str = temp.getString(colwidth2[col]);
                            }
                            para = new Paragraph(fontFamilySelector.process(str, FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("frommonth")) {                            
                            para = new Paragraph(fontFamilySelector.process(htmlPeriodRender(temp.getString("frommonth")), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("subtotal") 
                                || colwidth2[col].equals("discount") 
                                || colwidth2[col].equals("discountinbase") 
                                || colwidth2[col].equals("productTotalAmount") 
                                || colwidth2[col].equals("debitAmount") 
                                || colwidth2[col].equals("creditAmount") 
                                || colwidth2[col].equals("amount") 
                                || colwidth2[col].equals("amountinbase")
                                || colwidth2[col].equals("totalprofitmargin") 
                                || colwidth2[col].equals("discountamountinbase") 
                                || colwidth2[col].equals("amountwithouttax") 
                                || colwidth2[col].equals("totaltaxamount")
                                || colwidth2[col].equals("discountval")
                                || colwidth2[col].equals("rowprdiscount")
                                || colwidth2[col].equals("deductDiscount")
                                || colwidth2[col].equals("taxamount")
                                || colwidth2[col].equals("discounttotal")) {
                            String str;
                            try {
                                double bal = Double.parseDouble(temp.getString(colwidth2[col]));
                                str = authHandler.formattedAmount(bal, companyid);
                            } catch (NumberFormatException e) {
                                str = temp.getString(colwidth2[col]);
                            }
                            para = new Paragraph(fontFamilySelector.process(str, FontContext.TABLE_DATA));
                        } else {
                            String str = "";
                            if (colHeader[col].equals("Opening Balance Type")) {
                                if (temp.getString(colwidth2[col]).equalsIgnoreCase("-")) {
                                    str = "N/A";
                                } else {
                                    double bal = Double.parseDouble(temp.getString(colwidth2[col]));
                                    str = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                    if (str.equals("")) {
                                        str = "N/A";
                                    }
                                }
                                para = new Paragraph(fontFamilySelector.process(str, FontContext.TABLE_DATA));
                            } else {
                                try {
                                    if (mode == 125 && (((temp.getString(colwidth2[col]).toString()).contains("Total")) || isBoldRow)) {
                                        isBoldRow = true;
                                        para = new Paragraph();
                                        para.add(new Chunk(temp.getString(colwidth2[col]), FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK)));
                                    } else if ((mode == 4 || mode == 8) && (colwidth2[col].equalsIgnoreCase("otherwise"))) {   //mode=4 for RP & Mode=8 for MP
                                        if (StringUtil.DecodeText(temp.optString(colwidth2[col])).equalsIgnoreCase("true")) {
                                            para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText("No"), FontContext.TABLE_DATA));
                                        } else {
                                            para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText("Yes"), FontContext.TABLE_DATA));
                                        }
                                    } else {
                                        String tempStr = temp.getString(colwidth2[col]).toString();
                                        tempStr = tempStr.replaceAll("%", "%25").replaceAll("\\+", "%2B");   //ERP-27500
                                        tempStr = StringUtil.replaceFullHTML(tempStr.replaceAll("<br>", "\n").replaceAll("</div>", "\n"));
                                        para = new Paragraph(fontFamilySelector.process(StringUtil.DecodeText(tempStr), FontContext.TABLE_DATA));
                                    }
                                } catch (Exception e) {
                                    para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]).toString(), FontContext.TABLE_DATA));
                                }
                            }
                        }
                    }
                    h1 = new PdfPCell(para);
                    if (config.getBoolean("gridBorder")) {
                        h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(4);
                    h1.setBorderColor(Color.GRAY);
                    if (align[col].equals("currency") || align[col].equals("rowcurrency") || colwidth2[col].equals("taxrate") || colwidth2[col].equals("permargin") || align[col].equals("withoutcurrency") || align[col].equals("withoutcurrencywithtax") ||align[col].equals("quantity")) {
                        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    } else if (colwidth2[col].equals("subtotal") 
                            || colwidth2[col].equals("discount") 
                            || colwidth2[col].equals("discountinbase") 
                            || colwidth2[col].equals("productTotalAmount") 
                            || colwidth2[col].equals("debitAmount") 
                            || colwidth2[col].equals("creditAmount") 
                            || colwidth2[col].equals("amount") 
                            || colwidth2[col].equals("amountinbase")
                            || colwidth2[col].equals("totalprofitmargin") 
                            || colwidth2[col].equals("discountamountinbase") 
                            || colwidth2[col].equals("amountwithouttax") 
                            || colwidth2[col].equals("totaltaxamount")
                            || colwidth2[col].equals("discountval")
                            || colwidth2[col].equals("rowprdiscount")
                            || colwidth2[col].equals("deductDiscount")
                            || colwidth2[col].equals("taxamount")                            
                            || colwidth2[col].equals("discounttotal")
                            || colwidth2[col].equals("quantity")
                            || colwidth2[col].equals("blockquantity")
                            || colwidth2[col].equals("rate")) {
                        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    } else if (align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    } else {
                        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        h1.setVerticalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(h1);
                }
                count++;
            }
            if (mode == 117 && accountId.equalsIgnoreCase("All"))//for all account leadger
            {
                table = addTotalRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "Total", companyid);
                addRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "", true);
                table = addTotalRow(mode, colwidth2, table, grandCreTotal + totalCre, grandDebTotal + totalDeb, currencyid, "Grand Total", companyid);
            } else if (mode == 112) { //chart of accounts export and fixed asset export include total row
                double endingBalanceSummary = requestJobj.optDouble("endingBalanceSummary");
                double openbalanceSummary = requestJobj.optDouble("openbalanceSummary");
                double presentbalanceSummary = requestJobj.optDouble("presentbalanceSummary");
                if (requestJobj.optString("filename").equalsIgnoreCase("Fixed Assets.")) {
                    table = addTotalRow(mode, colwidth2, table, openbalanceSummary, presentbalanceSummary, currencyid, "Total", companyid);
                } else {
                    table = addTotalRow(mode, colwidth2, table, openbalanceSummary, endingBalanceSummary, currencyid, "Total", companyid);
                }
            } else if (mode == 117) {//for perticular account leadger
                table = addTotalRow(mode, colwidth2, table, totalCre, totalDeb, currencyid, "Total", companyid);
            
            } else if (mode == 4 || mode == 8 || mode==827) {//4: Payment Received;  8: Make Payment
                String gridHeader = (Arrays.toString(colwidth2));
                if(gridHeader.contains("amountinbase")){
                    table = addTotalRow(mode, colwidth2, table, grandTotalInBaseCurrency, 0, currencyid, "Grand Total", companyid);
                }
            } else if (mode == 810 || mode == 807 || (mode == 125 && isBasedOnProduct)) { //report == 807 :  Monthly Sales by Product subject to GST , report==810 : "Sales by Product Category Detail Report" , report == 125 : Monthly Sales by Product
                table = addTotalRow(mode, colwidth2, table, grandTotalInBaseCurrency, 0, currencyid, "Total", companyid);
            }
            if (mode == 152)//for Sales by Customer, Product and Agent Report
            {
                table = addTotalRow(mode, colwidth2, table, grandCreTotal, grandDebTotal, currencyid, "Total", companyid);
                grandCreTotal = 0;
                grandDebTotal = 0;
                count = 0;
            }
//            else{ 
//                if(mode != 115){ //for bank book export,do not include total row
//                    table=addTotalRow(mode,colwidth2,table,totalCre,totalDeb,currencyid,"Total");
//                }                
//            }
            document.add(table);
            document.newPage();
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTable", e);
        }
        return stpcol;
    }

    @Deprecated
    public PdfPTable addTotalRow(int mode, String[] colwidth2, PdfPTable table, double totalCre, double totalDeb, String currencyid, String total) throws ServiceException {//add totle row and grand totle row in report

        try {
            Paragraph para1 = null;
            PdfPCell h3 = null;
            String totCr = "";
            String totDb = "";
            h3 = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA)));
            if (config.getBoolean("gridBorder")) {
                h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.GRAY);
            h3.setBackgroundColor(Color.lightGray);
            h3.setHorizontalAlignment(Element.ALIGN_CENTER);
            h3.setVerticalAlignment(Element.ALIGN_CENTER);
            table.addCell(h3);
            para1 = new Paragraph(fontFamilySelector.process(total, FontContext.REPORT_TITLE));
            h3 = new PdfPCell(para1);
            if (config.getBoolean("gridBorder")) {
                if (total.equalsIgnoreCase("Grand Total")) {
                    h3.setBorder(PdfPCell.BOTTOM);
                } else {
                    h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                }
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.GRAY);
            h3.setBackgroundColor(Color.LIGHT_GRAY);
            h3.setHorizontalAlignment(Element.ALIGN_LEFT);
            h3.setVerticalAlignment(Element.ALIGN_LEFT);
            table.addCell(h3);

            for (int col = 1; col < colwidth2.length; col++) {
                if (colwidth2[col].equals("c_amount")) {
                    totCr = currencyRender(String.valueOf(totalCre), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totCr, FontContext.TABLE_DATA));
                } else if (colwidth2[col].equals("d_amount")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("openbalanceinbase")) {
                    totCr = currencyRender(String.valueOf(totalCre), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totCr, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("endingBalance")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("presentbalanceInBase")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if ( (mode == 152 || mode ==8 || mode==4 |mode==827)  && colwidth2[col].equals("amountinbase")) {//4: Payment Received;  8: Make Payment
                    totDb = currencyRender(String.valueOf(totalCre), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 152 && colwidth2[col].equals("amountinbasewithtax")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if ((mode==810 || mode==807 || mode==125)  && colwidth2[col].equals("amountinbase")) { //report==810 : "Sales by Product Category Detail Report" , report == 807 :  Monthly Sales by Product subject to GST ,report == 125 : Monthly Sales by Product
                    totDb = withoutCurrencyRender(String.valueOf(totalCre));
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else {
                    para1 = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                }

                h3 = new PdfPCell(para1);
                if (config.getBoolean("gridBorder")) {
                    if (total.equalsIgnoreCase("Grand Total")) {
                        h3.setBorder(PdfPCell.BOTTOM);
                    } else {
                        h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    }
                } else {
                    h3.setBorder(0);
                }
                h3.setPadding(4);
                h3.setBorderColor(Color.GRAY);
                h3.setBackgroundColor(Color.LIGHT_GRAY);
                h3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                h3.setVerticalAlignment(Element.ALIGN_RIGHT);
                table.addCell(h3);

            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTotalRow", e);
        }
        return table;
    }

    public PdfPTable addTotalRow(int mode, String[] colwidth2, PdfPTable table, double totalCre, double totalDeb, String currencyid, String total, String companyid) throws ServiceException {//add totle row and grand totle row in report

        try {
            Paragraph para1 = null;
            PdfPCell h3 = null;
            String totCr = "";
            String totDb = "";
            h3 = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA)));
            if (config.getBoolean("gridBorder")) {
                h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.GRAY);
            h3.setBackgroundColor(Color.lightGray);
            h3.setHorizontalAlignment(Element.ALIGN_CENTER);
            h3.setVerticalAlignment(Element.ALIGN_CENTER);
            table.addCell(h3);
            para1 = new Paragraph(fontFamilySelector.process(total, FontContext.REPORT_TITLE));
            h3 = new PdfPCell(para1);
            if (config.getBoolean("gridBorder")) {
                if (total.equalsIgnoreCase("Grand Total")) {
                    h3.setBorder(PdfPCell.BOTTOM);
                } else {
                    h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                }
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.GRAY);
            h3.setBackgroundColor(Color.LIGHT_GRAY);
            h3.setHorizontalAlignment(Element.ALIGN_LEFT);
            h3.setVerticalAlignment(Element.ALIGN_LEFT);
            table.addCell(h3);

            for (int col = 1; col < colwidth2.length; col++) {
                if (colwidth2[col].equals("c_amount")) {
                    totCr = currencyRender(String.valueOf(totalCre), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totCr, FontContext.TABLE_DATA));
                } else if (colwidth2[col].equals("d_amount")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("openbalanceinbase")) {
                    totCr = currencyRender(String.valueOf(totalCre), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totCr, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("endingBalance")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 112 && colwidth2[col].equals("presentbalanceInBase")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if ( (mode == 152 || mode ==8 || mode==4 |mode==827)  && colwidth2[col].equals("amountinbase")) {//4: Payment Received;  8: Make Payment
                    totDb = currencyRender(String.valueOf(totalCre), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if (mode == 152 && colwidth2[col].equals("amountinbasewithtax")) {
                    totDb = currencyRender(String.valueOf(totalDeb), currencyid, companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else if ((mode==810 || mode==807 || mode==125)  && colwidth2[col].equals("amountinbase")) { //report==810 : "Sales by Product Category Detail Report" , report == 807 :  Monthly Sales by Product subject to GST ,report == 125 : Monthly Sales by Product
                    totDb = withoutCurrencyRender(String.valueOf(totalCre), companyid);
                    para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                } else {
                    para1 = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                }

                h3 = new PdfPCell(para1);
                if (config.getBoolean("gridBorder")) {
                    if (total.equalsIgnoreCase("Grand Total")) {
                        h3.setBorder(PdfPCell.BOTTOM);
                    } else {
                        h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    }
                } else {
                    h3.setBorder(0);
                }
                h3.setPadding(4);
                h3.setBorderColor(Color.GRAY);
                h3.setBackgroundColor(Color.LIGHT_GRAY);
                h3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                h3.setVerticalAlignment(Element.ALIGN_RIGHT);
                table.addCell(h3);

            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTotalRow", e);
        }
        return table;
    }
    
    public PdfPTable addRow(int mode, String[] colwidth2, PdfPTable table, double totalCre, double totalDeb, String currencyid, String title, Boolean isBlank) throws ServiceException {//add blank or title row in report

        try {
            Paragraph para1 = null;
            PdfPCell h3 = null;
            h3 = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA)));
            if (config.getBoolean("gridBorder")) {
                h3.setBorder(PdfPCell.BOTTOM);
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.LIGHT_GRAY);
            if (isBlank) {
                h3.setBackgroundColor(Color.WHITE);
                h3.setFixedHeight(17);
            } else {
                h3.setBackgroundColor(Color.LIGHT_GRAY);
            }
            h3.setHorizontalAlignment(Element.ALIGN_CENTER);
            h3.setVerticalAlignment(Element.ALIGN_CENTER);
            table.addCell(h3);
            if (isBlank) {
                para1 = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
            } else {
                para1 = new Paragraph(fontFamilySelector.process(title, FontContext.REPORT_TITLE));
            }
            h3 = new PdfPCell(para1);
            if (config.getBoolean("gridBorder")) {
                h3.setBorder(PdfPCell.BOTTOM);
            } else {
                h3.setBorder(0);
            }
            h3.setPadding(4);
            h3.setBorderColor(Color.LIGHT_GRAY);
            if (isBlank) {
                h3.setBackgroundColor(Color.WHITE);
                h3.setFixedHeight(17);
            } else {
                h3.setBackgroundColor(Color.LIGHT_GRAY);
            }
            h3.setColspan(colwidth2.length);
            h3.setHorizontalAlignment(Element.ALIGN_LEFT);
            h3.setVerticalAlignment(Element.ALIGN_LEFT);
            table.addCell(h3);

        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTotalRow", e);
        }
        return table;
    }

    public int addGroupableTable(JSONObject groupingConfig, int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] dataIndexArr, String[] colHeader, String[] widths, String[] align, Document document, JSONObject requestJobj) throws ServiceException {
        try {
            String groupByField = groupingConfig.getString("groupBy");
            String groupHeaderText = "";
            boolean showGroupByColumn = false;

            if (!showGroupByColumn) {
                ArrayList<String> newdataIndexs = new ArrayList<String>();
                ArrayList<String> newColHeaders = new ArrayList<String>();
                ArrayList<String> newColWidths = new ArrayList<String>();
                ArrayList<String> newColAlign = new ArrayList<String>();
                for (int i = 0; i < dataIndexArr.length; i++) {
                    if (dataIndexArr[i].equalsIgnoreCase(groupByField)) {
                        groupHeaderText = colHeader[i];
                        continue;   //Remove all groupByField column's config to hide groupByField column in table
                    }
                    newdataIndexs.add(dataIndexArr[i]);
                    newColHeaders.add(colHeader[i]);
                    newColWidths.add(widths[i]);
                    newColAlign.add(align[i]);
                }
                dataIndexArr = new String[newdataIndexs.size()];
                colHeader = new String[newdataIndexs.size()];
                widths = new String[newdataIndexs.size()];
                align = new String[newdataIndexs.size()];
                for (int i = 0; i < newdataIndexs.size(); i++) {
                    dataIndexArr[i] = newdataIndexs.get(i);
                    colHeader[i] = newColHeaders.get(i);
                    widths[i] = newColWidths.get(i);
                    align[i] = newColAlign.get(i);
                }
            }
            return addGroupableTable(groupingConfig, groupByField, groupHeaderText, stcol, stpcol, strow, stprow, store, dataIndexArr, colHeader, widths, align, document, requestJobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addGroupableTable", e);
        }
    }

    public int addGroupableTable(JSONObject groupingConfig, String groupByField, String groupHeaderText, int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] dataIndexArr, String[] colHeader, String[] widths, String[] align, Document document, JSONObject requestJobj) throws ServiceException {
        try {
            String companyid = requestJobj.optString(Constants.companyKey);
            String groupSummaryField = groupingConfig.getString("groupSummaryField");
            String groupSummaryText = groupingConfig.getString("groupSummaryText");
            String reportSummaryField = groupingConfig.getString("reportSummaryField");
            String reportSummaryText = groupingConfig.getString("reportSummaryText");
            String get = requestJobj.optString("get");
            double amtDueSubTotal = 0.0;
            double amtDue1SubTotal = 0.0;
            double amtDue2SubTotal = 0.0;
            double amtDue3SubTotal = 0.0;
            double amtDue4SubTotal = 0.0;
            double amtDueHSubTotal = 0.0;
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(requestJobj.optString(Constants.dateformatid), requestJobj.optString(Constants.timezonedifference), true);
            DateFormat frmt = authHandler.getDateOnlyFormat();
            String currencyid = requestJobj.optString(Constants.globalCurrencyKey);
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
            java.awt.Color gsBGColor = new Color(Integer.parseInt("E5E5E5", 16));
            java.awt.Color rsBGColor = new Color(Integer.parseInt("808080", 16));
//            fontRegular.setColor(tColor);
            PdfPTable table;

            float[] tcol;
            tcol = new float[colHeader.length + 1];
            tcol[0] = 40;
            for (int i = 1; i < colHeader.length + 1; i++) {
                tcol[i] = Float.parseFloat(widths[i - 1]);
            }
            table = new PdfPTable(colHeader.length + 1);
            table.setWidthPercentage(tcol, document.getPageSize());
            table.setSpacingBefore(15);
            PdfPCell h2 = new PdfPCell(new Paragraph(fontFamilySelector.process("No.", FontContext.TABLE_HEADER, tColor)));
            if (config.getBoolean("gridBorder")) {
                h2.setBorder(PdfPCell.BOX);
            } else {
                h2.setBorder(0);
            }
            h2.setPadding(4);
            h2.setBorderColor(Color.GRAY);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h2);
            PdfPCell h1 = null;
            for (int hcol = stcol; hcol < dataIndexArr.length; hcol++) {
                if (align[hcol].equals("currency") && !colHeader[hcol].equals("")) {
                    String currency = currencyRender("", currencyid, companyid);
                    if ((get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22") || get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25") || get.equalsIgnoreCase("55"))) {   //Aged Receivable or Payable
                        if (dataIndexArr[hcol].equalsIgnoreCase("amountdueinbase")) {//for amount due (in home currency) column
                            h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol] + "(" + currency + ")", FontContext.TABLE_HEADER, tColor)));
                        } else {
                            h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol], FontContext.TABLE_HEADER, tColor)));
                        }
                    } else {
                        h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol] + "(" + currency + ")", FontContext.TABLE_HEADER, tColor)));
                    }
                } else {
                    h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol], FontContext.TABLE_HEADER, tColor)));
                }
                h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (config.getBoolean("gridBorder")) {
                    h1.setBorder(PdfPCell.BOX);
                } else {
                    h1.setBorder(0);
                }
                h1.setBorderColor(Color.GRAY);
                h1.setPadding(4);
                table.addCell(h1);
            }
            table.setHeaderRows(1);
            String groupName = "", rowCurrency = "";
            Double subTotal = 0.0;
            Double grandTotal = 0.0;
            int rowSpan = 0;
            for (int row = strow; row < stprow; row++) {
                rowSpan++;
                JSONObject rowData = store.getJSONObject(row);
                if (row == 0) {
                    groupName = rowData.getString(groupByField);
                    rowCurrency = rowData.has("currencyid") ? rowData.getString("currencyid") : currencyid;
                    subTotal = 0.0;
                    addGroupRow(groupHeaderText + ": " + groupName, currencyid, table, dataIndexArr);
                }
                if (!groupName.equalsIgnoreCase(rowData.getString(groupByField))) {
                    if ((get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22") || get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25"))) //Aged Receivable or Payable
                    {
                        addAmountDueTotalRow("Total ", amtDueSubTotal, amtDue1SubTotal, amtDue2SubTotal, amtDue3SubTotal, amtDue4SubTotal, amtDueHSubTotal, rowCurrency, table, dataIndexArr, gsBGColor, true, requestJobj);
                        amtDueSubTotal = 0.0;
                        amtDue1SubTotal = 0.0;
                        amtDue2SubTotal = 0.0;
                        amtDue3SubTotal = 0.0;
                        amtDue4SubTotal = 0.0;
                        amtDueHSubTotal = 0.0;
                    } else {
                        addSummaryRow(groupSummaryText + groupName + " ", subTotal, rowCurrency, table, dataIndexArr, false, gsBGColor, companyid);
                    }
                    groupName = rowData.getString(groupByField);
                    rowCurrency = rowData.has("currencyid") ? rowData.getString("currencyid") : currencyid;
                    addGroupRow(groupHeaderText + ": " + groupName, currencyid, table, dataIndexArr);
                    subTotal = 0.0;
                    rowSpan = 1;
                }
                subTotal += Double.parseDouble(rowData.getString(groupSummaryField));
                grandTotal += Double.parseDouble(rowData.getString(reportSummaryField));
                rowCurrency = rowData.has("currencyid") ? rowData.getString("currencyid") : currencyid;

                h2 = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(row + 1), FontContext.TABLE_HEADER, tColor)));
                if (config.getBoolean("gridBorder")) {
                    h2.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h2.setBorder(0);
                }
                h2.setPadding(4);
                h2.setBorderColor(Color.GRAY);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);

                for (int col = 0; col < dataIndexArr.length; col++) {
                    String cellData = null;
                    if (align[col].equals("currency") && !rowData.getString(dataIndexArr[col]).equals("")) {
                        String currencyID = null;
                        if ((get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22") || get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) && dataIndexArr[col].equalsIgnoreCase("amountdueinbase")) {  //Aged Receivable or Payable  and for amount due in home currency column
                            currencyID = currencyid;
                        } else {
                            currencyID = rowData.getString("currencyid");
                        }
                        cellData = currencyRender(rowData.getString(dataIndexArr[col]), currencyID, companyid);
                    } else if (align[col].equals("date") && !rowData.getString(dataIndexArr[col]).equals("")) {
                        try {
                            cellData = formatter.format(frmt.parse(rowData.getString(dataIndexArr[col])));
                        } catch (Exception ex) {
                            cellData = rowData.getString(dataIndexArr[col]);
                        }
                    } else {
                        cellData = rowData.getString(dataIndexArr[col]);
                    }

                    if (get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22") || get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) //Aged Receivable or Payable
                    {
                        if (dataIndexArr[col].equalsIgnoreCase("amountDue")) {
                            amtDueSubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                        if (dataIndexArr[col].equalsIgnoreCase("amountDue1")) {
                            amtDue1SubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                        if (dataIndexArr[col].equalsIgnoreCase("amountDue2")) {
                            amtDue2SubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                        if (dataIndexArr[col].equalsIgnoreCase("amountDue3")) {
                            amtDue3SubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                        if (dataIndexArr[col].equalsIgnoreCase("amountDue4")) {
                            amtDue4SubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                        if (dataIndexArr[col].equalsIgnoreCase("amountdueinbase")) {
                            amtDueHSubTotal += Double.parseDouble(rowData.getString(dataIndexArr[col]));
                        }
                    }
                    Paragraph para = new Paragraph(fontFamilySelector.process(cellData, FontContext.TABLE_HEADER, tColor));
                    h1 = new PdfPCell(para);
                    if (config.getBoolean("gridBorder")) {
                        h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(4);
                    h1.setBorderColor(Color.GRAY);

                    if (!align[col].equals("currency") && !align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        h1.setVerticalAlignment(Element.ALIGN_LEFT);
                    } else if (align[col].equals("currency")) {
                        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    } else if (align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    }
                    table.addCell(h1);
                }
            }
            if (rowSpan > 0) {
                if (get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22") || get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) //Aged Receivable or Payable
                {
                    addAmountDueTotalRow("Total ", amtDueSubTotal, amtDue1SubTotal, amtDue2SubTotal, amtDue3SubTotal, amtDue4SubTotal, amtDueHSubTotal, rowCurrency, table, dataIndexArr, gsBGColor, true, requestJobj);
                } else {
                    addSummaryRow(groupSummaryText + groupName + " ", subTotal, rowCurrency, table, dataIndexArr, false, gsBGColor, companyid);
                }
            }
            addSummaryRow(reportSummaryText, grandTotal, currencyid, table, dataIndexArr, false, gsBGColor, companyid);

            document.add(table);
            document.newPage();
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTable", e);
        }
        return stpcol;
    }

    public void addSummaryRow(String summeryText, double subTotal, String currencyid, PdfPTable table, String[] dataIndexArr, boolean addBlankRow, java.awt.Color bgColor, String companyid) throws JSONException, SessionExpiredException {
        Paragraph para = new Paragraph(fontFamilySelector.process(summeryText, FontContext.TABLE_HEADER));
        PdfPCell h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.TOP);
        }
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
        h1.setColspan(dataIndexArr.length);
        h1.setBackgroundColor(bgColor);
        table.addCell(h1);

        String withCurrency = currencyRender(Double.toString(subTotal), currencyid, companyid);
        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_HEADER));
        h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.TOP);
        }
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
        h1.setBackgroundColor(bgColor);
        table.addCell(h1);

        if (addBlankRow) {
            para = new Paragraph(fontFamilySelector.process(" ", FontContext.TABLE_HEADER));
            h1 = new PdfPCell(para);
            if (config.getBoolean("gridBorder")) {
                h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
            } else {
                h1.setBorder(0);
            }
            h1.setPadding(4);
            h1.setBorderColor(Color.GRAY);
            h1.setHorizontalAlignment(Element.ALIGN_LEFT);
            h1.setVerticalAlignment(Element.ALIGN_LEFT);
            h1.setColspan(dataIndexArr.length + 1);
            table.addCell(h1);
        }
    }

    public void addAmountDueTotalRow(String summeryText, double amtDueSubTotal, double amtDue1SubTotal, double amtDue2SubTotal, double amtDue3SubTotal, double amtDue4SubTotal, double amtDueHSubTotal, String currencyid, PdfPTable table, String[] dataIndexArr, java.awt.Color bgColor, boolean addBlankRow, JSONObject requestJobj) throws JSONException, SessionExpiredException {
        Paragraph para = new Paragraph(fontFamilySelector.process(summeryText, FontContext.TABLE_HEADER));
        PdfPCell h1 = new PdfPCell(para);
        String companyid = requestJobj.optString(Constants.companyKey);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.TOP);
        }
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
        boolean multiRecordMailFlag = requestJobj.optBoolean("multiRecordMailFlag", false);
        int getcol = 3;
        if (multiRecordMailFlag) {
            h1.setColspan(6);
            getcol = 5;
        } else {
            getcol = 4;
            h1.setColspan(5);
        }
        h1.setBackgroundColor(bgColor);
        table.addCell(h1);

        for (int col = getcol; col < dataIndexArr.length; col++) {
            double subTotal = 0.0;
            if (dataIndexArr[col].equalsIgnoreCase("amountdue")) {
                subTotal = amtDueSubTotal;
            }
            if (dataIndexArr[col].equalsIgnoreCase("amountdue1")) {
                subTotal = amtDue1SubTotal;
            }
            if (dataIndexArr[col].equalsIgnoreCase("amountdue2")) {
                subTotal = amtDue2SubTotal;
            }
            if (dataIndexArr[col].equalsIgnoreCase("amountdue3")) {
                subTotal = amtDue3SubTotal;
            }
            if (dataIndexArr[col].equalsIgnoreCase("amountdue4")) {
                subTotal = amtDue4SubTotal;
            }
            if (dataIndexArr[col].equalsIgnoreCase("amountdueinbase")) {
                subTotal = amtDueHSubTotal;
                currencyid = requestJobj.optString(Constants.globalCurrencyKey);
            }
            String withCurrency = currencyRender(Double.toString(subTotal), currencyid, companyid);
            para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_HEADER));
            h1 = new PdfPCell(para);
            if (config.getBoolean("gridBorder")) {
                h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
            } else {
                h1.setBorder(PdfPCell.TOP);
            }
            h1.setPadding(4);
            h1.setBorderColor(Color.GRAY);
            h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            h1.setVerticalAlignment(Element.ALIGN_RIGHT);
            h1.setBackgroundColor(bgColor);
            table.addCell(h1);
        }

        if (addBlankRow) {
            para = new Paragraph(fontFamilySelector.process(" ", FontContext.TABLE_HEADER));
            h1 = new PdfPCell(para);
            if (config.getBoolean("gridBorder")) {
                h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
            } else {
                h1.setBorder(0);
            }
            h1.setPadding(4);
            h1.setBorderColor(Color.GRAY);
            h1.setHorizontalAlignment(Element.ALIGN_LEFT);
            h1.setVerticalAlignment(Element.ALIGN_LEFT);
            h1.setColspan(dataIndexArr.length + 1);
            table.addCell(h1);
        }
    }

    public void addGroupRow(String groupText, String currencyid, PdfPTable table, String[] dataIndexArr) throws JSONException, SessionExpiredException {
        Paragraph para = new Paragraph(fontFamilySelector.process(groupText, FontContext.REPORT_TITLE));
        PdfPCell h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.BOTTOM);
        }
        h1.setBorderWidthBottom(1);
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setBorderColorBottom(Color.DARK_GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
        h1.setVerticalAlignment(Element.ALIGN_LEFT);
        h1.setColspan(dataIndexArr.length + 1);
        table.addCell(h1);
    }

    public void getHeaderFooter(Document document) throws ServiceException {
        try {
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontSmallRegular.setColor(tColor);
            java.util.Date dt = new java.util.Date();
            String date = "yyyy-MM-dd";
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
            String DateStr = dtf.format(dt);

            // -------- header ----------------
            header = new PdfPTable(3);
            String HeadDate = "";
            if (config.getBoolean("headDate")) {
                HeadDate = DateStr;
            }
            PdfPCell headerDateCell = new PdfPCell(new Phrase(fontFamilySelector.process(HeadDate, FontContext.FOOTER_NOTE, tColor)));
            headerDateCell.setBorder(0);
            headerDateCell.setPaddingBottom(4);
            header.addCell(headerDateCell);

            PdfPCell headerNotecell = new PdfPCell(new Phrase(fontFamilySelector.process(config.getString("headNote"), FontContext.FOOTER_NOTE, tColor)));
            headerNotecell.setBorder(0);
            headerNotecell.setPaddingBottom(4);
            headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            header.addCell(headerNotecell);

            String HeadPager = "";
            if (config.getBoolean("headPager")) {
                HeadPager = String.valueOf(document.getPageNumber());//current page no
            }
            PdfPCell headerPageNocell = new PdfPCell(new Phrase(fontFamilySelector.process(HeadPager, FontContext.FOOTER_NOTE, tColor)));
            headerPageNocell.setBorder(0);
            headerPageNocell.setPaddingBottom(4);
            headerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            header.addCell(headerPageNocell);

            PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
            headerSeparator.setBorder(PdfPCell.BOX);
            headerSeparator.setPadding(0);
            headerSeparator.setColspan(3);
            header.addCell(headerSeparator);
            // -------- header end ----------------

            // -------- footer  -------------------
            footer = new PdfPTable(3);
            PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
            footerSeparator.setBorder(PdfPCell.BOX);
            footerSeparator.setPadding(0);
            footerSeparator.setColspan(3);
            footer.addCell(footerSeparator);

            String PageDate = "";
            if (config.getBoolean("footDate")) {
                PageDate = DateStr;
            }
            PdfPCell pagerDateCell = new PdfPCell(new Phrase(fontFamilySelector.process(PageDate, FontContext.FOOTER_NOTE, tColor)));
            pagerDateCell.setBorder(0);
            footer.addCell(pagerDateCell);

            PdfPCell footerNotecell = new PdfPCell(new Phrase(fontFamilySelector.process(config.getString("footNote"), FontContext.FOOTER_NOTE, tColor)));
            footerNotecell.setBorder(0);
            footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            footer.addCell(footerNotecell);

            String FootPager = "";
            if (config.getBoolean("footPager")) {
                FootPager = String.valueOf(document.getPageNumber());//current page no
            }
            PdfPCell footerPageNocell = new PdfPCell(new Phrase(fontFamilySelector.process(FootPager, FontContext.FOOTER_NOTE, tColor)));
            footerPageNocell.setBorder(0);
            footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            footer.addCell(footerPageNocell);
            // -------- footer end   -----------
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.getHeaderFooter", e);
        }
    }

    public void addDateRange(Document d, String From, String To,boolean isAccountForeCastReport) throws ServiceException {
        try {
            String DateFromStr = "";
            String DateToStr = "";
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
            if (isAccountForeCastReport) {
                DateFromStr = From;
                DateToStr = To;
            } else {
                try {
                    java.util.Date fromDate = new java.util.Date(java.util.Date.parse(From));
                    java.util.Date toDate = new java.util.Date(java.util.Date.parse(To));
                    String date = "yyyy-MM-dd";
                    java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
                    DateFromStr = dtf.format(fromDate);
                    DateToStr = dtf.format(toDate);
                } catch (Exception e) {
                    DateFromStr = From;
                    DateToStr = To;
                }
            }

            PdfPTable table = new PdfPTable(1);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidthPercentage(100);
            String DateFromRange = "";
            if (config.getBoolean("dateRange")) {
                DateFromRange = DateFromStr;
            }
            String DateToRange = "";
            if (config.getBoolean("dateRange")) {
                DateToRange = DateToStr;
            }
            PdfPCell DateFromRangeCell = new PdfPCell(new Phrase(fontFamilySelector.process("From:" + DateFromRange + "    To:" + DateToRange, FontContext.FOOTER_NOTE, tColor)));
            DateFromRangeCell.setBorder(0);
            DateFromRangeCell.setBorderWidth(0);
            DateFromRangeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            DateFromRangeCell.setPaddingBottom(4);
            table.addCell(DateFromRangeCell);

            d.add(table);

        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addDateRange", e);
        }
    }

    public ByteArrayOutputStream getPdfData(JSONObject grid, HttpServletRequest request, JSONObject obj) throws ServiceException {
        ByteArrayOutputStream baos = null;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            int mode = requestJobj.optInt("get");
            boolean displayUnitPriceandAmtInSalesDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,mode,true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,mode,false);
            requestJobj.put("displayUnitPriceandAmtInSalesDocPerm", displayUnitPriceandAmtInSalesDocPerm);
            requestJobj.put("displayUnitPriceandAmtInPurchaseDocPerm", displayUnitPriceandAmtInPurchaseDocPerm);
            baos = getPdfData(grid, requestJobj, obj);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return baos;
    }
    
    public ByteArrayOutputStream getPdfData(JSONObject grid, JSONObject requestJobj, JSONObject obj) throws ServiceException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        Document document = null;
        try {
            JSONArray gridmap = grid == null ? null : grid.getJSONArray("data");
            String colHeader = "";
            String colHeaderFinal = "";
            String fieldListFinal = "";
            String fieldList = "";
            String width = "";
            String align = "";
            String alignFinal = "";
            String widthFinal = "";
            String colHeaderArrStr[] = null;
            String dataIndexArrStr[] = null;
            String widthArrStr[] = null;
            String alignArrStr[] = null;
            int strLength = 0;
            float totalWidth = 0;

            config = new com.krawler.utils.json.base.JSONObject(requestJobj.optString("config"));
            document = null;
            Rectangle rec = null;
            if (config.getBoolean("landscape")) {
                Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
                rec = document.getPageSize();
                totalWidth = rec.getWidth();
            } else {
                Rectangle recPage = new Rectangle(PageSize.A4);
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
                rec = document.getPageSize();
                totalWidth = rec.getWidth();
            }

            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new EndPage());
            document.open();

            // if (config.getBoolean("showLogo")) {
            addComponyLogo(document, requestJobj, config.getBoolean("showLogo"));
            // }

            addTitleSubtitle(document);
            if (config.has("dateRange")) {
                if (config.getBoolean("dateRange")) {
                    String From = requestJobj.optString("startdate");
                    if (StringUtil.isNullOrEmpty(From)) {
                        From = requestJobj.optString("stdate");
                    }
                    String To = requestJobj.optString("enddate");

                    if (!StringUtil.isNullOrEmpty(From) && !StringUtil.isNullOrEmpty(To)) {
                        From = StringUtil.DecodeText(From);
                        To = StringUtil.DecodeText(To);
                        String report = requestJobj.optString("get");
                        boolean isAccountForeCastReport = false;
                        /* Check for AccountForeCast Report*/
                        if (!StringUtil.isNullOrEmpty(report) && report.equals("197")) {
                            isAccountForeCastReport = true;
                            final DateTimeFormatter dtf1 = DateTimeFormat.forPattern("MMMM, yyyy");
                            Date startDate = dtf1.parseLocalDate(From).toDate();
                            Date endDate = dtf1.parseLocalDate(To).toDate();

                            LocalDate localStartDate = new LocalDate(startDate);
                            LocalDate localEndDate = new LocalDate(endDate);

                            startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
                            endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();

                            From = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj).format(startDate);
                            To = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj).format(endDate);
                        }
                        addDateRange(document, From, To, isAccountForeCastReport);
                    }
                }
            }
            //Separator line after addTitleSubtitle() or addDateRange()
            PdfPTable line = new PdfPTable(1);
            line.setWidthPercentage(100);
            PdfPCell cell1 = null;
            cell1 = new PdfPCell(new Paragraph(""));
            cell1.setBorder(PdfPCell.BOTTOM);
            line.addCell(cell1);
            document.add(line);

            if (gridmap != null) {
                int givenTotalWidth = 0;
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    givenTotalWidth += Integer.parseInt(temp.getString("width"));
                }
                double widthRatio = 1;
                if (givenTotalWidth > (totalWidth - 40.00)) {
                    widthRatio = (totalWidth - 40.00) / givenTotalWidth; // 40.00 is left + right + table margin [15+15+10] margins of documents
                }
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    colHeader += StringUtil.serverHTMLStripper(temp.getString("title"));
                    if (colHeader.indexOf("*") != -1) {
                        colHeader = colHeader.substring(0, colHeader.indexOf("*") - 1) + ",";
                    } else {
                        colHeader += ",";
                    }
                    fieldList += temp.getString("header") + ",";
                    if (!config.getBoolean("landscape")) {
                        int totalWidth1 = (int) ((totalWidth / gridmap.length()) - 5.00);
                        width += "" + totalWidth1 + ",";  //resize according to page view[potrait]
                    } else {
                        double adjustedWidth = (Integer.parseInt(temp.getString("width")) * widthRatio);
                        width += ((int) Math.floor(adjustedWidth)) + ",";
                    }
                    if (temp.getString("align").equals("")) {
                        align += "none" + ",";
                    } else {
                        align += temp.getString("align") + ",";
                    }
                }
                strLength = colHeader.length() - 1;
                colHeaderFinal = colHeader.substring(0, strLength);
                strLength = fieldList.length() - 1;
                fieldListFinal = fieldList.substring(0, strLength);
                strLength = width.length() - 1;
                widthFinal = width.substring(0, strLength);
                strLength = align.length() - 1;
                alignFinal = align.substring(0, strLength);
                colHeaderArrStr = colHeaderFinal.split(",");
                dataIndexArrStr = fieldListFinal.split(",");
                widthArrStr = widthFinal.split(",");
                alignArrStr = alignFinal.split(",");
            } else {
                fieldList = requestJobj.optString("header");
                colHeader = requestJobj.optString("title");
                width = requestJobj.optString("width");
                align = requestJobj.optString("align");
                
                colHeader=StringUtil.DecodeText(colHeader);
                colHeaderArrStr = colHeader.split(",");
                dataIndexArrStr = fieldList.split(",");
                widthArrStr = width.split(",");
                alignArrStr = align.split(",");

                int givenTotalWidth = 0;
                for (int i = 0; i < widthArrStr.length; i++) {
                    String temp = widthArrStr[i];
                    givenTotalWidth += Integer.parseInt(temp);
                }
                double widthRatio = 1;
                if (givenTotalWidth > (totalWidth - 40.00)) {
                    widthRatio = (totalWidth - 40.00) / givenTotalWidth; // 40.00 is left + right + table margin [15+15+10] margins of documents
                }
                String widthCalculated="";
                for (int i = 0; i < widthArrStr.length; i++) {
                    if (!config.getBoolean("landscape")) {
                        int totalWidth1 = (int) ((totalWidth / dataIndexArrStr.length) - 5.00);
                        widthCalculated += "" + totalWidth1 + ",";  //resize according to page view[potrait]
                    } else {
                        double adjustedWidth = (Integer.parseInt(widthArrStr[i]) * widthRatio);
                        widthCalculated += ((int) Math.floor(adjustedWidth)) + ",";
                    }
                }
                widthFinal = widthCalculated.substring(0, widthCalculated.length() - 1);
                widthArrStr = widthFinal.split(",");
            }

            JSONArray store = obj.getJSONArray("data");
            int mode = requestJobj.optInt("get");
            if (mode == 112) {
                requestJobj.put("endingBalanceSummary", obj.getDouble("endingBalanceSummary"));
                requestJobj.put("openbalanceSummary", obj.getDouble("openbalanceSummary"));
                requestJobj.put("presentbalanceSummary", obj.getDouble("presentbalanceSummary"));
            }

            if (grid != null && grid.has("groupdata")) {
                JSONObject groupingConfig = grid.getJSONObject("groupdata");
                addGroupableTable(groupingConfig, 0, colHeaderArrStr.length, 0, store.length(), store, dataIndexArrStr, colHeaderArrStr, widthArrStr, alignArrStr, document, requestJobj);
            } else {
                addTable(0, colHeaderArrStr.length, 0, store.length(), store, dataIndexArrStr, colHeaderArrStr, widthArrStr, alignArrStr, document, requestJobj);
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("exportMPXDAOImpl.getPdfData", e);
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return baos;
    }

    public ByteArrayOutputStream getCreatedCSVFileOutputStream(HttpServletRequest request, HttpServletResponse response, JSONObject obj, DateFormat dateFormat, String dateFormatID, String timeZoneDifference, String currencyId, String colHeader) throws UnsupportedEncodingException, IOException, JSONException, ParseException, SessionExpiredException {
        JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
        int report =requestJobj.optInt("get",-1);
        boolean isMonthlyReport=requestJobj.optBoolean("isMonthlyReport",false);
        if(isMonthlyReport || (request.getAttribute("isexportledgerflag") != null && (Boolean)request.getAttribute("isexportledgerflag"))){
            requestJobj.put("title",request.getAttribute("title").toString());
            requestJobj.put("header",request.getAttribute("header").toString());
            requestJobj.put("align",request.getAttribute("align").toString());
        }
        boolean displayUnitPriceandAmtInSalesDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,true);
        boolean displayUnitPriceandAmtInPurchaseDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,false);
        requestJobj.put("displayUnitPriceandAmtInSalesDocPerm", displayUnitPriceandAmtInSalesDocPerm);
        requestJobj.put("displayUnitPriceandAmtInPurchaseDocPerm", displayUnitPriceandAmtInPurchaseDocPerm);
        return getCreatedCSVFileOutputStream(requestJobj, obj, dateFormatID, timeZoneDifference, currencyId, colHeader);
    }
    
    public ByteArrayOutputStream getCreatedCSVFileOutputStream(JSONObject requestJobj, JSONObject obj, String dateFormatID, String timeZoneDifference, String currencyId, String colHeader) throws UnsupportedEncodingException, IOException, JSONException, ParseException, SessionExpiredException {
        ByteArrayOutputStream os = null;
        KwlReturnObject result = null;
        try {
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(dateFormatID, timeZoneDifference, true);
            DateFormat frmt = authHandler.getDateOnlyFormat();
            
            DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj.optString(Constants.userdateformat));   //Date formatter for Sample file 
            DateFormat dbFormat = authHandler.getDateWithTimeFormat();                         //This Date format is fixed in sample file record in DB
            SimpleDateFormat dateFormatForTapReturn = new SimpleDateFormat("yyyy-MM-dd");
            boolean isSampleFile =obj.optString("isSampleFile").equals("T");
            int report = requestJobj.optInt("get",-1);
            boolean displayUnitPriceandAmtInSalesDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInSalesDocPerm", true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInPurchaseDocPerm", true);
            Set<String> unitPriceAmountheaderName = new HashSet<>();
            if (!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) {
                unitPriceAmountheaderName = getUnitPriceAndAmountDataIndexSet(report,displayUnitPriceandAmtInSalesDocPerm,displayUnitPriceandAmtInPurchaseDocPerm);
            }
            
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0, grandTotalAmountWithTaxInBaseCurrency=0;
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), requestJobj.getString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            boolean excludeCustomHeaders = requestJobj.optBoolean("excludeCustomHeaders",false);
            int moduleId = requestJobj.optInt("moduleId",0000);
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String str = null;
            String nm = null, companyid = "";
            boolean isCurrencyCode = false;
            String tit = "";
            String head = "";
            String aligns = "";
           
            if (requestJobj.optString("header",null) != null) {
                tit = requestJobj.optString("title");
                head = requestJobj.optString("header");
                aligns = requestJobj.optString("align");
                if (report == 772 || report == 773 || report == 775) {
                     /*
                     772 - dimensional pnl
                     773 - dimensional BL
                     */
                    tit = colHeader;

                     if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport) {
                        head = obj.getString("dataIndexes");
                        aligns = obj.getString("aligns");
                     }
                }
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) aligns.split(",");
            }
            else {                
                headers = (String[]) obj.getString("header").split(",");
                titles = (String[]) obj.getString("title").split(",");
                align = (String[]) obj.getString("align").split(",");
        }
        companyid = requestJobj.optString("companyids");
        if (requestJobj.optBoolean("isCompareGlobal", false) && (requestJobj.optBoolean("isForTradingAndProfitLoss", false) || requestJobj.optBoolean("isFromBalanceSheet", false))) {
            String startDate = requestJobj.optString("stdate", "");
            String endDate = requestJobj.optString("enddate", "");
            String startPreDate = requestJobj.optString("stpredate", "");
            String endPreDate = requestJobj.optString("endpredate", "");
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                startDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(startDate));
                endDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(endDate));
                if (!StringUtil.isNullOrEmpty(startPreDate) && !StringUtil.isNullOrEmpty(endPreDate)) {
                    startPreDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(startPreDate));
                    endPreDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(endPreDate));
                }
                for (int cnt = 1; cnt < headers.length; cnt++) {
                    if (headers[cnt].equals("amount") || headers[cnt].equals("ramount") || headers[cnt].equals("lamount")) {
                        titles[cnt] += "\n" + startDate + " to " + endDate;
                    } else if (headers[cnt].equals("preamount") || headers[cnt].equals("rpreamount") || headers[cnt].equals("lpreamount")) {
                        titles[cnt] += "\n" + startPreDate + " to " + endPreDate;
                        titles[cnt] += "";
                    }
                }
            }
        }
        if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
            isCurrencyCode = true;
        }
        String currencyid = currencyId;//sessionHandlerImpl.getCurrencyID(request);
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
            /* 'excludeCustomHeaders' check is added to improve performance while exporting records. 
               Purpose: Insert Custom field headers on server side rather than sending all of them in URL. 
               It will reduce size of URL.*/
          if (excludeCustomHeaders) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.optString("companyid"), moduleId));
                result = accCommonTablesDAO.getFieldParamsforSpecificFields(requestParams);

                ArrayList<String> headList = new ArrayList<String>();
                Collections.addAll(headList, headers);
                if (result.getRecordTotalCount() > 0) {
                    List<String> fields = result.getEntityList();
                    Iterator itr = fields.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String label = oj[2].toString();
                        String fieldname = oj[1].toString();
                        if (headList.contains(fieldname)) {
                            int index = headList.indexOf(fieldname);
                            titles[index] = label;
                        }
                    }
                }
                headers = headList.toArray(new String[headList.size()]);
            }
        
        StringBuilder reportSB = new StringBuilder();
            JSONArray repArr = new JSONArray();
            if (report == Constants.GSTR2A_Match_And_Reconcile_Report) {
                repArr = obj.getJSONArray("coldata");
            } else {
                repArr = obj.getJSONArray("data");
            }
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (h < headers.length - 1) {
                    if (align[h].equals("currency") && !headers[h].equals("") && report!=Constants.GROUP_DETAIL_REPORT_ID) { // Dont' add currency symbol in GL header
                        String currency = currencyRender("", currencyid, companyid);
                        reportSB.append("\"" + headerStr + "(" + currency + ")" + "\",");
                    } else {
                        reportSB.append("\"" + headerStr + "\",");
                    }
                } else {
                    if (align[h].equals("currency") && !headers[h].equals("") && report!=Constants.GROUP_DETAIL_REPORT_ID) { // Dont' add currency symbol in GL header
                        String currency = currencyRender("", currencyid, companyid);
                        reportSB.append("\"" + headerStr + "(" + currency + ")" + "\"\n");
                    } else {
                        reportSB.append("\"" + headerStr + "\"\n");
                    }
                }
        }
           
        for (int t = 0; t < repArr.length(); t++) {
            JSONObject temp = repArr.getJSONObject(t);
            /*commenting this code for ERP-12840 ticket
            * if (report == 116) { //116:Trial Balance
            if(temp.has("c_amount")){
            totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
            }
            if(temp.has("d_amount")){
            totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
            }
            if (temp.has("c_amount_open")) {
            totalOpenCre = totalOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_open")) ? temp.getString("c_amount_open") : "0");
            }
            if (temp.has("d_amount_open")) {
            totalOpenDeb = totalOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_open")) ? temp.getString("d_amount_open") : "0");
            }
            if (temp.has("c_amount_period")) {
            totalPeriodCre = totalPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_period")) ? temp.getString("c_amount_period") : "0");
            }
            if (temp.has("d_amount_period")) {
            totalPeriodDeb = totalPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_period")) ? temp.getString("d_amount_period") : "0");
            }
            if (temp.has("ytd_c_amount_open")) {
            totalYTDOpenCre = totalYTDOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_open")) ? temp.getString("ytd_c_amount_open") : "0");
            }
            if (temp.has("ytd_d_amount_open")) {
            totalYTDOpenDeb = totalYTDOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_open")) ? temp.getString("ytd_d_amount_open") : "0");
            }
            if (temp.has("ytd_c_amount_period")) {
            totalYTDPeriodCre = totalYTDPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_period")) ? temp.getString("ytd_c_amount_period") : "0");
            }
            if (temp.has("ytd_d_amount_period")) {
            totalYTDPeriodDeb = totalYTDPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_period")) ? temp.getString("ytd_d_amount_period") : "0");
            }
            if (temp.has("ytd_c_amount")) {
            totalYTDCre = totalYTDCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount")) ? temp.getString("ytd_c_amount") : "0");
            }
            if (temp.has("ytd_d_amount")) {
            totalYTDDeb = totalYTDDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount")) ? temp.getString("ytd_d_amount") : "0");
            }
            } else */if (report == 4 || report == 8 || report == 301 || report==827 || report == 810 || report == 807 || report == 125) { //4: Payment Received;  8: Make Payment 827:day end collection report  report==810 : "Sales by Product Category Detail Report" 
                grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbase")) ? temp.getString("amountinbase") : "0");
                if(report == 301){
                    grandTotalAmountWithTaxInBaseCurrency = grandTotalAmountWithTaxInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbasewithtax")) ? temp.getString("amountinbasewithtax") : "0");
                }
            }
            String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
            String transactionCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
            String transactionCurrency = temp.has("transactioncurrencyid") ? temp.getString("transactioncurrencyid") : currencyid;
            for (int h = 0; h < headers.length; h++) {
                if (h < headers.length - 1) {
                    if((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[h])){
                        reportSB.append("\" " + Constants.UPAndAmtDispalyValueNoPerm + "\",");
                    } else if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = (report==115||report==51||report==52||report==66||report==67)?currencyRender(temp.getString(headers[h]), currencyid, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = (report==115||report==51||report==52||report==66||report==67)?currencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\",");
                    }
                    /*
                     * Apply currency render to Amount in transaction currency
                     * in gst form 5
                     */
                    else if (align[h].equals("rowcurrencyGstForm") && !temp.optString(headers[h], "").equals("")) {
                        String currency = currencyRender(temp.getString(headers[h]), transactionCurrency, isCurrencyCode, companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency =(report==152)?withoutCurrencyRender(temp.getString(headers[h]), companyid):unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("transactioncurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = currencyRender(temp.getString(headers[h]), transactionCurrencyId, isCurrencyCode, companyid);
                        reportSB.append("\" " + currency + "\",");
                    } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                        if (isSampleFile) {//Sample file check
                            try {
                                String d1 = userFormat.format(dbFormat.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\",");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                            }
                        } else {
                            try {
                                String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\",");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                            }
                        }
                    } else if(headers[h].equals("jedate")){
                        String d1 = "";
                        if (temp.has("jedate") && !StringUtil.isNullOrEmpty(temp.getString("jedate"))) {
                            try {   //For GST Form 5 Detail CSV
                              d1 = userFormat.format(dateFormatForTapReturn.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\",");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                            }
                        } else {
                            reportSB.append(",");
                        }
                        //reportSB.append("\" " + d1 + "\",");                        
                    } else if(headers[h].equals("creationDate")){
                        String d1 = "";
                        if (temp.has("creationDate") && !StringUtil.isNullOrEmpty(temp.getString("creationDate"))) {
                            try {   //For GST Form 5 Detail CSV
                              d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                              reportSB.append("\" " + d1 + "\",");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                            }
                        } else {
                            reportSB.append(",");
                        }                     
                    }
                    else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                        reportSB.append("\" " + htmlPercentageRender(temp.getString(headers[h]), true) + "\",");
                    } else if (report == Constants.GSTR2A_Match_And_Reconcile_Report && ((headers[h]).equals("taxableAmt") || (headers[h]).equals("totalTax") || (headers[h]).equals("totalAmt") || (headers[h]).equals("cess"))) {
                        String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\",");
                    } else {
                        if (headers[h].equals("invoiceno")) {
                            reportSB.append("\" " + temp.getString("invoiceno") + "\",");
                        } else if (headers[h].equals("supplierinvoiceno")) {
                            reportSB.append("\" " + temp.optString("supplierinvoiceno") + "\",");
                        } else if(headers[h].equals("invoicedate") && report == 246 ){
                            reportSB.append("\" " + temp.getString("invoicedate") + "\",");
                        }else if (headers[h].equals("invoicedate")) {
                            reportSB.append("\" " + formatter.format(frmt.parse(temp.getString("date"))) + "\",");
                        } else if (headers[h].equals("c_date")) {
                            reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))) + "\",");
                        } else if (headers[h].equals("c_checkdate")) {
                            String checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                            if (!StringUtil.isNullOrEmpty(checkdate)) {
                                try {
                                    checkdate = formatter.format(frmt.parse((checkdate)));
                                } catch (Exception ex) {
                                    checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                                }
                            }
                            reportSB.append("\" " + checkdate + "\",");
                        } else if (headers[h].equals("c_accountname")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")) + "\",");
                        } else if (headers[h].equals("c_acccode")) {
                            String accCode = "";
                            if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            }
                            reportSB.append("\" " + accCode + "\",");
                        } else if (headers[h].equals("c_transactionID")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_transactionID")) ? temp.getString("d_transactionID") : temp.getString("c_transactionID")) + "\",");
                        } else if (headers[h].equals("c_checkno")) {
                            try{
                                reportSB.append("\" " + (temp.has("c_checkno") && StringUtil.isNullOrEmpty(temp.optString("c_checkno", "")) ? temp.optString("d_checkno", "") : temp.optString("c_checkno", "")) + "\",");
                            }catch(Exception e){
                                reportSB.append("\" " + temp.optString("c_checkno","") + "\",");
                            }
                        } else if (headers[h].equals("c_amountAccountCurrency")) {
                            reportSB.append("\" " + (temp.has("c_amountAccountCurrency") ? (StringUtil.isNullOrEmpty(temp.getString("c_amountAccountCurrency")) ? "" : temp.getString("c_amountAccountCurrency")) : "") + "\",");
                        } else if (headers[h].equals("c_transactionDetails")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.optString("c_transactionDetails")) ? StringUtil.serverHTMLStripper(temp.optString("d_transactionDetails")) : StringUtil.serverHTMLStripper(temp.optString("c_transactionDetails"))) + "\",");
                        } else if (headers[h].equals("c_transactionDetailsForExpander")) {
                            if (temp.has("d_transactionDetailsForExpander") || temp.has("c_transactionDetailsForExpander")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_transactionDetailsForExpander")) ? (temp.has("d_transactionDetailsForExpander") ? temp.getString("d_transactionDetailsForExpander"):"") : temp.getString("c_transactionDetailsForExpander")) + "\",");
                            }
                        } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h])) && report != 117) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")) + "\",");
                        } else if (headers[h].equals("c_transactionDetailsBankBook")) {
                            if (!temp.getString("d_transactionDetailsBankBook").equalsIgnoreCase("Transfer")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsBankBook")) ? temp.getString("c_transactionDetailsBankBook") : temp.getString("d_transactionDetailsBankBook")) + "\",");
                            } else {
                                reportSB.append("\" " + temp.getString("c_transactionDetailsBankBook") + "\",");
                            }
                        } else if (headers[h].equals("d_date")) {
                            if (report == 117) {//Ledger T report
                                if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling case to show netbalnce in general ledger
                                    double netbalnce = temp.optDouble("netbalance", 0.0);
                                    String currency = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                    reportSB.append("\" " + currency + "\",");
                                } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general ledger                                                                                           
                                    reportSB.append("\" " + temp.getString("d_date") + "\",");
                                } else if (temp.has("isTotal") && temp.optBoolean("isTotal", false)) {//Handling to dispaly Total in general ledger                                                                                           
                                    reportSB.append("\" " + "" + "\","); //In total row blank date field required
                                } else {
                                    if(!temp.optString("d_date").equals("") || !temp.getString("c_date").equals("")){
                                        reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))) + "\",");
                                    }else{
                                        reportSB.append("\" " + "" + "\","); //In total row blank date field required
                                    }
                                    
                                }
                            } else if (report == 771) {
                                if (!(temp.has("d_date") && temp.has("c_date"))) {
                                    reportSB.append("\" " + "" + "\",");
                                } else {
                                    reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))) + "\",");
                                }
                            } else if(!StringUtil.isNullOrEmpty(temp.optString("d_date")) || !StringUtil.isNullOrEmpty(temp.optString("c_date"))) {
                                reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.optString("d_date")) ? temp.optString("c_date") : temp.optString("d_date")))) + "\",");
                            }
                            else{
                                reportSB.append("\" " + "" + "\",");
                            }

                        } else if (headers[h].equals("d_accountname")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.optString("d_accountname")) ? temp.optString("c_accountname") : temp.optString("d_accountname")) + "\",");
                        } else if (headers[h].equals("c_description")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.optString("c_description")) ? temp.optString("d_description") : temp.optString("c_description")) + "\",");
                        } else if (headers[h].equals("descinforeign")) {
                            reportSB.append("\" " + temp.optString("foreigndescription") + "\",");
                        } else if (headers[h].equals("d_acccode")) {
                            String accCode = "";
                            if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            }
                            reportSB.append("\" " + accCode + "\",");
                        } else if (headers[h].equals("d_transactionID")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_transactionID")) ? temp.getString("c_transactionID") : temp.getString("d_transactionID")) + "\",");
                        } else if (headers[h].equals("d_transactionDetails")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_transactionDetails")) ? temp.getString("c_transactionDetails") : temp.getString("d_transactionDetails")) + "\",");
                        } else if (headers[h].equals("d_transactionDetailsForExpander")) {
                            if (temp.has("d_transactionDetailsForExpander") || temp.has("c_transactionDetailsForExpander")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsForExpander")) ? (temp.has("c_transactionDetailsForExpander") ? temp.getString("c_transactionDetailsForExpander"):"") : temp.getString("d_transactionDetailsForExpander")) + "\",");
                            }
                        } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")) + "\",");
                        } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period") || headers[h].equals("d_period") || headers[h].equals("c_period") || headers[h].equals("periodBalance"))) {
                            String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\",");
                        } else if (report == 1123 && (headers[h].equals("costprice") || headers[h].equals("unitprice") || headers[h].equals("amountcost") || headers[h].equals("amountsales"))) {
                            String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\",");
                        } else if (report == 116 && (headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount") || headers[h].equals("ytd_c_amount_open") || headers[h].equals("ytd_d_amount_open") || headers[h].equals("c_amount") || headers[h].equals("d_amount"))) {
                            String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }
                            tempString = withoutCurrencyRender(tempString, companyid);
                            reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\",");
                        } else if (report == 116 && (headers[h].equals("ytd_d_amount_period") || headers[h].equals("ytd_c_amount_period") || headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount"))) {
                            String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                            if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                                tempString = "0.0";
                            }

                            tempString = withoutCurrencyRender(tempString, companyid);
                            reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\",");
                        } else if (align[h].equals("quantity")) {
                            reportSB.append("\" " + authHandler.formattedQuantity(temp.optDouble(headers[h],0.00),companyid) + "\",");
                        } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                            reportSB.append(",");
                        } else if (report == 27 && headers[h].equals("transactionDetails") && StringUtil.isNullOrEmpty(temp.optString("transactionDetails", ""))) {// for JE Export Report
                            try {
                                reportSB.append("\" " + StringUtil.DecodeText(temp.optString("journalEntryDetailsDescription", "")) + "\",");
                            } catch (IllegalArgumentException e) {   //To handle "%" & "+" in String, I took String value as it.
                                reportSB.append("\" " + temp.optString("journalEntryDetailsDescription", "") + "\",");
                            }
                        } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                            double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                            String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                            if (adj < 0.0001) {
                                reportSB.append(",");
                            } else {
                                reportSB.append("\" " + currency + "\",");
                            }
                        } else if (isSampleFile && titles[h].equals("Opening Balance")) {
                            if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                reportSB.append("\" " + temp.getString("openbalance") + "\",");
                            } else {
//                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                reportSB.append("\" " + temp.getString("openbalance") + "\",");
                            }
                        }else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                            if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                reportSB.append("\" " + temp.getString("openbalance") + "\",");
                            } else {
                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                reportSB.append("\" " + currency + "\",");
                            }
                        } else if (headers[h].equals("transactionno") || headers[h].equals("donumber")) {
                            String refNo = " \t" + temp.getString(headers[h]);
                            reportSB.append("\" " + refNo + "\",");
                        } else if (headers[h].equalsIgnoreCase("reorderquantity") || headers[h].equalsIgnoreCase("reorderlevel") || headers[h].equalsIgnoreCase("quantity") || headers[h].equalsIgnoreCase("leasedQuantity") || headers[h].equalsIgnoreCase("lockquantity") || headers[h].equalsIgnoreCase("consignquantity") || headers[h].equalsIgnoreCase("venconsignquantity") || headers[h].equalsIgnoreCase("recycleQuantity")|| headers[h].equalsIgnoreCase("received") || headers[h].equalsIgnoreCase("delivered") || 
                                   headers[h].equalsIgnoreCase("blockquantity") || headers[h].equalsIgnoreCase("repairquantity") || headers[h].equalsIgnoreCase("qaquantity") || headers[h].equalsIgnoreCase("reservestock")||headers[h].equalsIgnoreCase("closedqty")) {//blockquantity,repairquantity,qaquantity should be in number format not in text format
                            String str1;
                            try {
                                double bal = Double.parseDouble(temp.getString(headers[h]));
                                str1 = authHandler.formattedQuantity(bal,companyid);
                            } catch (NumberFormatException e) {
                                str1 = temp.getString(headers[h]);
                            }
                            reportSB.append("\" " + str1 + "\",");
                        } else if (headers[h].equals("frommonth")) {
                            String frommonth = htmlPeriodRender(temp.getString("frommonth"));
                            reportSB.append("\" " + frommonth + "\",");
                        } else if (headers[h].equals("transactionexchangerate")) {
                            double exRate = temp.optDouble("transactionexchangerate", 0.0);//ERP-36142
                            reportSB.append("\" " + exRate + "\",");
                        } else {
                            if (titles[h].equals("Opening Balance Type")) {
                                String str1 = "";
                                try {
                                    if (temp.getString(headers[h]).equalsIgnoreCase("-")) { //chart of accounts export 
                                        str1 = "N/A";
                                    } else {
                                        double bal = Double.parseDouble(temp.getString(headers[h]));
                                        str1 = bal == 0 ? "N/A" : (bal < 0 ? "Credit" : "Debit");
                                    }
                                } catch (NumberFormatException ex) {
                                    str1 = temp.getString(headers[h]);
                                }
                                reportSB.append("\" " + str1 + "\",");
                            } else {
                                try {
                                    if (headers[h].equalsIgnoreCase("otherwise")) {
                                        boolean isLinked = !(Boolean.parseBoolean(StringUtil.DecodeText(temp.optString(headers[h]))));
                                        reportSB.append("\" " + isLinked + "\",");
                                    } else {
                                        /* Replacing " with ' ,if present in the field Memo & Sales person
                                        
                                         During Export csv file if memo & sales person present " then it breaks the record in two lines. 
                                         */
                                        if (headers[h].equalsIgnoreCase("memo") || headers[h].equalsIgnoreCase("salesPerson") || headers[h].equalsIgnoreCase("salespersonname")) {
                                            String memo = temp.getString(headers[h]).trim();
                                            memo = memo.replaceAll("%", "%25").replaceAll("\"", "\'").replaceAll("\\+", "%2B");
                                            reportSB.append("\" " + StringUtil.DecodeText(memo) + "\",");
                                        } else if (headers[h].equals("documentReferenceNo")) {
                                            String documentReferenceNo = " \t" + temp.getString("documentReferenceNo");
                                            reportSB.append("\" " + documentReferenceNo + "\",");
                                        } else if (headers[h].equals("itemasset")) {
                                            String itemasset = " \t" + temp.getString("itemasset");
                                            reportSB.append("\" " + itemasset + "\",");
                                        } else if (headers[h].equals("returnNo")) {
                                            String returnNo = " \t" + temp.getString("returnNo");
                                            reportSB.append("\" " + returnNo + "\",");
                                        } else if (headers[h].equals("quantity")) {
                                            double quantity = Double.parseDouble(temp.getString(headers[h]));
                                            reportSB.append("\" " + quantity + "\",");
                                        }
                                        else if (headers[h].equals("serials")) {
                                            String serials = " \t" + temp.getString("serials");
                                            reportSB.append("\" " + serials + "\",");
                                        } else if (headers[h].equals("subtotal") 
                                                || headers[h].equals("discount")
                                                || headers[h].equals("discountinbase")
                                                || headers[h].equals("productTotalAmount")
                                                || headers[h].equals("debitAmount")
                                                || headers[h].equals("creditAmount")
                                                || headers[h].equals("amount")
                                                || headers[h].equals("amountinbase")
                                                || headers[h].equals("totalprofitmargin")
                                                || headers[h].equals("discountamountinbase")
                                                || headers[h].equals("amountwithouttax")
                                                || headers[h].equals("totaltaxamount")
                                                || headers[h].equals("discountval")
                                                || headers[h].equals("rowprdiscount")
                                                || headers[h].equals("deductDiscount")
                                                || headers[h].equals("taxamount")
                                                || headers[h].equals("discounttotal")){
                                            try {
                                                if (report == Constants.GstTapReturnDetailedView && headers[h] != null && temp.optString(headers[h], "").trim().equals("")) {
                                                    reportSB.append("\" " + temp.optString(headers[h], "") + "\",");
                                                } else {
                                                    double amount = temp.optDouble(headers[h], 0.00);
                                                    reportSB.append("\" " + authHandler.formattingDecimalForAmount(amount, companyid) + "\",");
                                                }
                                            } catch (Exception e) {
                                                reportSB.append("\" " + temp.optString(headers[h], "") + "\",");
                                            }
                                        } else {
                                            String tempStr = temp.getString(headers[h]);
                                            tempStr = tempStr.replaceAll("%", "%25").replaceAll("\\+", "%2B");   //ERP-27500
                                            tempStr = tempStr.replaceAll("\"", "''");
                                            tempStr = StringUtil.replaceFullHTML(tempStr.replaceAll("<br>", "\n").replaceAll("</div>", "\n"));
					    //tempStr = StringEscapeUtils.unescapeCsv(tempStr);   //SDP-11789
                                            reportSB.append("\" " +  URLDecoder.decode(tempStr,Constants.ENCODING) + "\",");  //ERP-27500
                                        }

                                    }
                                } catch (Exception e) {
                                    reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                                }
                            }
                        }
                    }
                } else {
                    // SON - QUICK FIX FOR MONTHLY REPORTS LIKE MONTHLY TRADING & PROFIT/LOSS
//                        if (align[h].equals("currency") && !temp.getString(headers[h]).equals("")) {
                    if((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[h])){
                        reportSB.append("\" " + Constants.UPAndAmtDispalyValueNoPerm + "\"\n");
                    } else if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = (report==115||report==51||report==52||report==66||report==67)?currencyRender(temp.getString(headers[h]), currencyid, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\"\n");
                    } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\"\n");
                    } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = (report==115||report==51||report==52||report==66||report==67)?currencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid):withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\"\n");
                    } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = (report==152)?withoutCurrencyRender(temp.getString(headers[h]), companyid):unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                        reportSB.append("\" " + currency + "\"\n");
                    } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                        String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                        reportSB.append("\" " + currency + "\"\n");
                    } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                        if (isSampleFile) {//Sample file check
                            try {
                                String d1 = userFormat.format(dbFormat.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\"\n");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\"\n");
                            }
                        } else {
                            try {
                                String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\"\n");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\"\n");
                            }
                        }
                    } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                        reportSB.append("\" " + htmlPercentageRender(temp.getString(headers[h]), true) + "\"\n");
                    } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period"))) {
                        String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\"\n");
                    } else if (report == 116 && (headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount") || headers[h].equals("ytd_c_amount_open") || headers[h].equals("ytd_d_amount_open") || headers[h].equals("c_amount") || headers[h].equals("d_amount") || headers[h].equals("d_period") || headers[h].equals("c_period") || headers[h].equals("periodBalance"))) {
                        String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }
                        tempString = withoutCurrencyRender(tempString, companyid);
                        reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\"\n");
                    } else if (report == 116 && (headers[h].equals("ytd_d_amount_period") || headers[h].equals("ytd_c_amount_period") || headers[h].equals("ytd_d_amount") || headers[h].equals("ytd_c_amount"))) {
                        String tempString = !temp.optString(headers[h], "").equals("") ? temp.getString(headers[h]) : "";
                        if (tempString.equals("") && !extraCompanyPreferences.isShowZeroAmountAsBlank()) {
                            tempString = "0.0";
                        }

                        tempString = withoutCurrencyRender(tempString, companyid);
                        reportSB.append("\"" + StringUtil.DecodeText(tempString) + "\"\n");
                    } else {
                        if (headers[h].equals("invoiceno")) {
                            reportSB.append("\" " + temp.getString("invoiceno") + "\"\n");
                        } else if (headers[h].equals("invoicedate")) {
                            reportSB.append("\" " + formatter.format(frmt.parse(temp.getString("date"))) + "\"\n");
                        } else if (headers[h].equals("c_date")) {
                            reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))) + "\"\n");
                        } else if (headers[h].equals("c_accountname")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")) + "\"\n");
                        } else if (headers[h].equals("c_acccode")) {
                            String accCode = "";
                            if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            } else if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            }
                            reportSB.append("\" " + accCode + "\",");
                        } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h]))) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")) + "\"\n");
                        } else if (headers[h].equals("d_date")) {
                            reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("c_date")))) + "\"\n");
                        } else if (headers[h].equals("c_checkdate")) {
                            String checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                            if (!StringUtil.isNullOrEmpty(checkdate)) {
                                try {
                                    checkdate = formatter.format(frmt.parse((checkdate)));
                                } catch (Exception ex) {
                                    checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                                }
                            }
                            reportSB.append("\" " + checkdate + "\",");
                        } else if (headers[h].equals("d_accountname")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname")) + "\"\n");
                        } else if (headers[h].equals("d_acccode")) {
                            String accCode = "";
                            if (temp.has("d_acccode") && !StringUtil.isNullOrEmpty(temp.getString("d_acccode"))) {
                                accCode = temp.getString("d_acccode");
                            } else if (temp.has("c_acccode") && !StringUtil.isNullOrEmpty(temp.getString("c_acccode"))) {
                                accCode = temp.getString("c_acccode");
                            }
                            reportSB.append("\" " + accCode + "\",");
                        } else if (align[h].equals("quantity")) {
                            reportSB.append("\" " + authHandler.formattedQuantity(temp.optDouble(headers[h],0.00),companyid) + "\"\n");
                        } else if (headers[h].equals("c_checkno")) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_checkno")) ? temp.getString("d_checkno") : temp.getString("c_checkno")) + "\",");
                        } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                            reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")) + "\"\n");
                        } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                reportSB.append("\n");
                        } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                            double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                            String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                            if (adj < 0.0001) {
                                reportSB.append(",");
                            } else {
                                reportSB.append("\" " + currency + "\",");
                            }
                        } else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                            String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                            reportSB.append("\" " + currency + "\",");
                        } else if (titles[h].equals("Opening Balance Type")) {
                            double bal = Double.parseDouble(temp.getString(headers[h]));
                            String str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                            reportSB.append("\" " + str1 + "\"\n");
                        } else if (headers[h].equalsIgnoreCase("otherwise")) {
                            if (temp.optString(headers[h]).equalsIgnoreCase("true")) {
                                reportSB.append("\"" + "No" + "\"\n");
                            } else {
                                reportSB.append("\"" + "Yes" + "\"\n");
                            }
                        } else if (headers[h].equals("frommonth")) {
                            String frommonth = htmlPeriodRender(temp.getString("frommonth"));
                            reportSB.append("\" " + frommonth + "\"\n");
                        }
                         else if (headers[h].equals("documentReferenceNo")) {
                            String documentReferenceNo = " \t" +temp.getString("documentReferenceNo");
                            reportSB.append("\" " + documentReferenceNo + "\",");
                        }
                        else if (headers[h].equals("itemasset")) {
                            String itemasset = " \t" +temp.getString("itemasset");
                             reportSB.append("\" " + itemasset + "\",");
                        }
                        
                        else if (headers[h].equals("returnNo")) {
                            String returnNo = " \t" +temp.getString("returnNo");
                             reportSB.append("\" " + returnNo + "\",");
                        }
                        else if (headers[h].equals("quantity")) {
                            double quantity = Double.parseDouble(temp.getString(headers[h]));
                             reportSB.append("\" " + quantity + "\",");
                        }
                        else {
                             reportSB.append("\"" + StringUtil.replaceFullHTML(temp.getString(headers[h]).replaceAll("<br>", "\n").replaceAll("</div>", "\n")) + "\"\n");
                        }
                    }
                }
            }
        }
       /*commenting this code for ERP-12840 ticket
             * if (report == 116) { //116:Trial Balance
            String sep = ""; //Data separator for CSV
            reportSB.append("\"Total\",");
            reportSB.append("\" \",");
            reportSB.append("\" \",");
            reportSB.append("\" \",");
            for (int h = 1; h < headers.length; h++) {
            sep = (h < headers.length - 1) ? "," : "\n";
            if (headers[h].equals("c_amount")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("d_amount")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalDeb), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("c_amount_open")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalOpenCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("d_amount_open")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalOpenDeb), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("c_amount_period")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalPeriodCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("d_amount_period")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalPeriodDeb), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_c_amount_open")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDOpenCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_d_amount_open")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDOpenDeb), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_c_amount_period")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDPeriodCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_d_amount_period")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDPeriodDeb), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_c_amount")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDCre), currencyid, isCurrencyCode) + "\"" + sep);
            } else if (headers[h].equals("ytd_d_amount")) {
            reportSB.append("\"" + currencyRender(String.valueOf(totalYTDDeb), currencyid, isCurrencyCode) + "\"" + sep);
            }
            }
        } else */if (report == 4 || report == 8 || report == 301 || report==827) {//4: Payment Received; 8: Make Payment
            if(report == 301){
                reportSB.append("\n\n" + "\"Total\",");
            }else{
                String gridHeader = (Arrays.toString(titles));
                if (gridHeader.contains("Amount Paid (In Base Currency)")
                        || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                        || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                        || gridHeader.contains("Total Amount (In Base Currency)(SG Dollar(SGD)) ")) {
                    reportSB.append("\n\n" + "\"Grand Total (In Base Currency)\",");
                }
            }
                
                for (int h = 1;h< headers.length; h++) {
                    if (headers[h].equals("amountinbase")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        reportSB.append("\"" + totalAmount + "\",");
                    }else if(headers[h].equals("amountinbasewithtax")){
                        String totalAmount = currencyRender(String.valueOf(grandTotalAmountWithTaxInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        reportSB.append("\"" + totalAmount + "\",");
                    }else{
                        reportSB.append(",");
                    }
                }
            } else if (report == 112 && (head.contains("openbalanceinbase") || head.contains("endingBalanceSummary"))) { //112:COA - added Total at the end
                reportSB.append("\n\n" + "\"Total\",");
                for (int h = 1; h < headers.length; h++) {
                    if (headers[h].equals("openbalanceinbase")) {
                        String totalAmount = currencyRender(String.valueOf(obj.getDouble("openbalanceSummary")), currencyid, isCurrencyCode, companyid);
                        reportSB.append("\"" + totalAmount + "\",");
                    } else if (headers[h].equals("endingBalance")) {
                        String totalAmount = currencyRender(String.valueOf(obj.optDouble("endingBalanceSummary")), currencyid, isCurrencyCode, companyid);
                        reportSB.append("\"" + totalAmount + "\",");
                    } else {
                        reportSB.append(",");
                    }
                }
            }else if (report == 810 || report == 807 || report == 125) {    //report == 807 :  Monthly Sales by Product subject to GST , report==810 : "Sales by Product Category Detail Report" , report == 125 : Monthly Sales by Product
                boolean isBasedOnProduct = requestJobj.optBoolean("isBasedOnProduct",true);
                if (isBasedOnProduct) {
                    reportSB.append("\n\n" + "\"Total\",");
                    for (int h = 1; h < headers.length; h++) {
                        if (headers[h].equals("amountinbase")) {
                            reportSB.append("\"" + grandTotalInBaseCurrency + "\",");
                        } else {
                            reportSB.append(",");
                        }
                    }
                }
            }
        String fname = requestJobj.optString("filename",requestJobj.optString("name"));
        if (!StringUtil.isNullOrEmpty(fname)) {
            fname = URLDecoder.decode(fname, "ISO-8859-1");
        }   
        os = new ByteArrayOutputStream();
        os.write(reportSB.toString().getBytes());
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fname + ".csv\"");
//            response.setContentType("application/octet-stream");
//            response.setContentLength(os.size());
//            response.getOutputStream().write(os.toByteArray());
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
//            if (os != null) {
//                os.close();
//            }

        } catch (ServiceException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
            return os;

    }

    public void createCsvFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        ByteArrayOutputStream os = null;
//        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
        DateFormat frmt = authHandler.getDateOnlyFormat();
        try {

            String fname = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
            String fileType = request.getParameter("filetype");
            if (!StringUtil.isNullOrEmpty(obj.optString("filename"))) {
                fname = (String) obj.optString("filename");//Filename overide only when sample file is downloading
            }
            if (!StringUtil.isNullOrEmpty(fname)) {
                fname = URLDecoder.decode(fname, "ISO-8859-1");
            }
            
            if (StringUtil.equal(fileType, "detailedCSV")) {
                String append = StringUtil.equal(fileType, "detailedCSV") ? "(Detail)" : "(Summary)";
                if (!StringUtil.isNullOrEmpty(fname)) {
                    if (fname.indexOf("_v") != -1) {
                        String version = fname.substring(fname.indexOf("_v"), fname.length());
                        fname = fname.substring(0, fname.indexOf("_v"));
                        fname = fname.concat(append).concat(version);
                    } else {
                        fname = fname + append;
                    }
                }
            }

                os = getCreatedCSVFileOutputStream(request, response, obj, frmt, sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), sessionHandlerImpl.getCurrencyID(request), colHeader);

            response.setHeader("Content-Disposition", "attachment; filename=\"" + fname + ".csv\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(os.size());
            response.getOutputStream().write(os.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            if (os != null) {
                os.close();
            }
        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ServiceException ex) {
//            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public HSSFWorkbook createExcelFile(JSONObject requestJobj, JSONObject obj, String colHeader) throws ServiceException, SessionExpiredException, JSONException {
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook();
            String sheetName = "Sheet-1";
            createSheet(requestJobj, obj, colHeader, wb, sheetName);
        } catch (ServiceException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }
    private XSSFWorkbook createExcelFileNew(JSONObject requestJobj, JSONObject obj, String colHeader) throws ServiceException, SessionExpiredException {
        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook();
            String sheetName = "Sheet-1";
            createSheet(requestJobj, obj, colHeader, wb, sheetName);
        } catch (ServiceException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }

    public HSSFWorkbook createExcelFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        boolean dimensionBasedMonthlyPL=StringUtil.isNullOrEmpty(request.getParameter("dimensionBasedMonthlyPL")) ? false : Boolean.parseBoolean(request.getParameter("dimensionBasedMonthlyPL"));
        boolean dimensionBasedMonthlyBS=StringUtil.isNullOrEmpty(request.getParameter("dimensionBasedMonthlyBS")) ? false : Boolean.parseBoolean(request.getParameter("dimensionBasedMonthlyBS"));
        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook();
            String sheetName="Sheet-1";
            if(dimensionBasedMonthlyPL || dimensionBasedMonthlyBS){
                JSONArray dimMonthlyData=obj.getJSONArray("dimMonthlyData");
                for (int cnt = 0; cnt < dimMonthlyData.length(); cnt++) {
                    JSONObject jObj = dimMonthlyData.getJSONObject(cnt);
                    sheetName="Sheet-"+(cnt+1);
                    createSheet(request, response, jObj,colHeader, wb, sheetName); 
                }
            } else {
                createSheet(request, response, obj,colHeader, wb, sheetName);
            }
        } catch (Exception e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }
    public HSSFSheet createSheet(HttpServletRequest request, HttpServletResponse response, JSONObject obj, String colHeader, HSSFWorkbook wb, String sheetName) throws ServiceException, SessionExpiredException {
        HSSFSheet sheet = null;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            int report =requestJobj.optInt("get",-1);
            boolean isMonthlyReport=requestJobj.optBoolean("isMonthlyReport",false);
            if(isMonthlyReport){
                requestJobj.put("title",request.getAttribute("title").toString());
                requestJobj.put("header",request.getAttribute("header").toString());
                requestJobj.put("align",request.getAttribute("align").toString());
            }
            boolean displayUnitPriceandAmtInSalesDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,false);
            requestJobj.put("displayUnitPriceandAmtInSalesDocPerm", displayUnitPriceandAmtInSalesDocPerm);
            requestJobj.put("displayUnitPriceandAmtInPurchaseDocPerm", displayUnitPriceandAmtInPurchaseDocPerm);
            sheet = createSheet(requestJobj, obj, colHeader, wb, sheetName);
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return sheet;
    }
        
    public HSSFSheet createSheet(JSONObject requestJobj, JSONObject obj,String colHeader, HSSFWorkbook wb, String sheetName) throws ServiceException, SessionExpiredException, JSONException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(requestJobj.optString(Constants.dateformatid), requestJobj.optString(Constants.timezonedifference), true);
        DateFormat frmt = authHandler.getDateOnlyFormat();
        DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj.optString(Constants.userdateformat));   //date formatter for Sample file download
        SimpleDateFormat dateFormatForTapReturn = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dbFormat = authHandler.getDateWithTimeFormat();                          //Database date format
        HSSFSheet sheet = null;
        boolean isCurrencyCode = false;
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = requestJobj.optString(Constants.companyKey);
        KwlReturnObject result = null;
        String fileType = requestJobj.getString("filetype");
        boolean isFromDocumentDesigner =false;
        boolean dimensionBasedMonthlyPL=requestJobj.optBoolean("dimensionBasedMonthlyPL",false);
        boolean dimensionBasedMonthlyBS=requestJobj.optBoolean("dimensionBasedMonthlyBS",false);
        try {
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            int report =requestJobj.optInt("get",-1);
            boolean displayUnitPriceandAmtInSalesDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInSalesDocPerm", true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = requestJobj.optBoolean("displayUnitPriceandAmtInPurchaseDocPerm", true);
            Set<String> unitPriceAmountheaderName = new HashSet<>();
            if (!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) {
                unitPriceAmountheaderName = getUnitPriceAndAmountDataIndexSet(report,displayUnitPriceandAmtInSalesDocPerm,displayUnitPriceandAmtInPurchaseDocPerm);
            }
            boolean isSampleFile =obj.optString("isSampleFile").equals("T");
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0, grandTotalAmountWithTaxInBaseCurrency=0;
            double totalPeriodCredit = 0, totalPeriodDebit = 0, totalPeriodBalance = 0,totalOpeningAmount=0,totalEndingAmount=0;
            boolean excludeCustomHeaders = requestJobj.optBoolean("excludeCustomHeaders",false);
            String module = requestJobj.optString("moduleId","0000");
//            int moduleId = Integer.parseInt(module);
            int moduleId = requestJobj.optInt("moduleId",0000);
            boolean isBasedOnProduct=requestJobj.optBoolean("isBasedOnProduct",false);
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String tit="";
            String head = "";
            String algn = "";
            isFromDocumentDesigner = obj.optBoolean("isFromDocumentDesigner",false);
            if (requestJobj.optString("header",null) != null) {
                 tit = requestJobj.optString("title");
                head = requestJobj.optString("header");
                algn = requestJobj.optString("align");

                if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport || report == 775) {
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
                }
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                if(dimensionBasedMonthlyPL || dimensionBasedMonthlyBS){
                    String customDimensionName = (obj.has("customDimensionName") && obj.get("customDimensionName")!=null) ? obj.get("customDimensionName").toString():"";
                    if(!StringUtil.isNullOrEmpty(customDimensionName)){
                        for (int cnt = 1; cnt < titles.length-1; cnt++) {
                            titles[cnt] += " (" + customDimensionName + ")";
                        }
                    }
                }
                if (requestJobj.optBoolean("isCompareGlobal", false) && (requestJobj.optBoolean("isForTradingAndProfitLoss", false) || requestJobj.optBoolean("isFromBalanceSheet", false))) {
                    String startDate = requestJobj.optString("stdate", "");
                    String endDate = requestJobj.optString("enddate", "");
                    String startPreDate = requestJobj.optString("stpredate", "");
                    String endPreDate = requestJobj.optString("endpredate", "");
                    if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                        startDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(startDate));
                        endDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(endDate));
                        if (!StringUtil.isNullOrEmpty(startPreDate) && !StringUtil.isNullOrEmpty(endPreDate)) {
                            startPreDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(startPreDate));
                            endPreDate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(endPreDate));
                        }
                        for (int cnt = 1; cnt < headers.length; cnt++) {
                            if (headers[cnt].equals("amount") || headers[cnt].equals("ramount") || headers[cnt].equals("lamount")) {
                                titles[cnt] += "\n" + startDate + " to " + endDate;
                            } else if (headers[cnt].equals("preamount") || headers[cnt].equals("rpreamount") || headers[cnt].equals("lpreamount")) {
                                titles[cnt] += "\n" + startPreDate + " to " + endPreDate;
                                titles[cnt] += "";
                            }
                        }
                    }
                }
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
            String currencyid = requestJobj.optString(Constants.globalCurrencyKey);
            sheetName=StringUtil.isNullOrEmpty(sheetName)?"Sheet-1":sheetName;
            if (report == Constants.GSTR3B_Summary_Report || report == Constants.GSTR3B_DETAIL_REPORT) {
                sheetName = "GSTR3B";
            }
            /**
             * GST Computation Detail section view Export Sheet Name
             */
            if (report == Constants.GSTRComputationDetailReport) {
                sheetName = "GST Computation";
            }
            sheet = wb.createSheet(sheetName);
            cell = writeFilterDetailsInExcel(requestJobj,sheet,cell,rownum,cellnum,obj,wb);
            
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontHeightInPoints((short) 10);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);

            
            rownum+=cell.getRow().getRowNum()+1;
            HSSFRow headerRow = sheet.createRow(rownum++);
            JSONArray repArr = new JSONArray();
            /**
             * Get GST Computation Section date to export
             */
            if (report == Constants.GSTR2A_Match_And_Reconcile_Report || report == Constants.GSTR3B_Summary_Report || report == Constants.GSTR3B_DETAIL_REPORT || report == Constants.GSTRComputationDetailReport ) {
                repArr = obj.getJSONArray("coldata");
            } else {
                repArr = obj.getJSONArray("data");
            }
            companyid = requestJobj.optString("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }
            
            /* 
            Bold and Italic Style created to apply on Sub-totals as per reports 
            */ 
            HSSFCellStyle boldAndItalicStyle = wb.createCellStyle();
            HSSFFont boldAndItalicFont = wb.createFont();
            boldAndItalicFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            boldAndItalicFont.setItalic(true);
            boldAndItalicStyle.setFont(boldAndItalicFont);
            
            /* 'excludeCustomHeaders' check is added to improve performance while exporting records. 
               Purpose: Insert Custom field headers on server side rather than sending all of them in URL. 
               It will reduce size of URL.*/
            
            HSSFCellStyle boldStyle = wb.createCellStyle();
            HSSFFont boldFont = wb.createFont();
            boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            DataFormat format = wb.createDataFormat();
            CellStyle amountStyle = wb.createCellStyle();
            amountStyle.setDataFormat(format.getFormat(authHandler.formattingDecimalForAmount(0.0, companyid)));
            CellStyle quantityStyle = wb.createCellStyle();
            quantityStyle.setDataFormat(format.getFormat(authHandler.formattingDecimalForQuantity(0.0, companyid)));
            boldStyle.setFont(boldFont);
            
            // Amount Formate Cell Style - Decimal based on company setting - Ex. 123,456,78.90 (This formate use system language setting and format amount based on it.)
            HSSFCellStyle cellStyleAmount = getCommaSepratedAmountStyle(wb, requestJobj.optString("companyid"));
            // End - Amount Formate Cell Style
            
            if (excludeCustomHeaders) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.optString("companyid"), moduleId));
                result = accCommonTablesDAO.getFieldParamsforSpecificFields(requestParams);

                ArrayList<String> headList = new ArrayList<String>();
                Collections.addAll(headList, headers);
                if (result.getRecordTotalCount() > 0) {
                    List<String> fields = result.getEntityList();
                    Iterator itr = fields.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String label = oj[2].toString();
                        String fieldname = oj[1].toString();
                        if (headList.contains(fieldname)) {
                            int index = headList.indexOf(fieldname);
                            titles[index] = label;
                        }
                    }
                }
                headers = headList.toArray(new String[headList.size()]);
            }
            //Insert Headers
            cellnum = 0;
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (h < headers.length - 1) {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                        if (requestJobj.optInt("get", 0) == Constants.GstTapReturnDetailedView) {
                            cell.setCellStyle(style);
                        }
                    } else {
                        cell.setCellValue(headerStr);
                        if (requestJobj.optInt("get",0) == Constants.GstTapReturnDetailedView) {
                            cell.setCellStyle(style);
                            /**
                             * Set Reports Header Name Bold 
                             */
                        } else if (report == Constants.INDONESIA_VAT_OUT_REPORT || report == Constants.GSTR3B_Summary_Report || report == Constants.GSTRComputationDetailReport) {
                            cell.setCellStyle(boldStyle);
                        }
                    }
                } else {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                    } else {
                        cell.setCellValue(headerStr);
                    }
                    /**
                     * Set Reports Header Name Bold 
                     */
                    if (report == Constants.INDONESIA_VAT_OUT_REPORT || report == Constants.GSTR3B_Summary_Report || report == Constants.GSTRComputationDetailReport) {
                        cell.setCellStyle(boldStyle);
                    }
                }
            }//headers loop
            /**
             * For VAT Out Report Add multiple header 
             */
            if (report == Constants.INDONESIA_VAT_OUT_REPORT) {
                if (obj.optBoolean(IndonesiaConstants.isCreateHeaderAndFilterRow, false) && obj != null && obj.has("OtherHeaders")) {
                    JSONArray OtherHeadersArray = obj.getJSONArray("OtherHeaders");
                    for (int i = 0; i < OtherHeadersArray.length(); i++) {
                        String headerString = OtherHeadersArray.getJSONObject(i).optString("header", "");
                        if (!StringUtil.isNullOrEmpty(headerString)) {
                            cellnum = 0;
                            headerRow = sheet.createRow(rownum++);
                            String[] headerStringArray = (String[]) headerString.split(",");
                            for (int h = 0; h < headerStringArray.length; h++) {
                                String headerStr = StringUtil.serverHTMLStripper(headerStringArray[h]);
                                cell = headerRow.createCell(cellnum++);  //Create new cell
                                cell.setCellValue(headerStr);
                                cell.setCellStyle(boldStyle);
                            }
                        }
                    }
                }
                /**
                 * Remove Header and Filter for VAT Out Report
                 */
                if (!obj.optBoolean(IndonesiaConstants.isCreateHeaderAndFilterRow, false)) {
                    rownum = 0;
                }
            }

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row = sheet.createRow(rownum++);
                JSONObject temp = repArr.getJSONObject(t);
                if (report == 116) { //116:Trial Balance
                if(temp.has("c_amount")){
                        totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                    }
                if(temp.has("d_amount")){
                        totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                    }
                    if (temp.has("c_amount_open")) {
                        totalOpenCre = totalOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_open")) ? temp.getString("c_amount_open") : "0");
                    }
                    if (temp.has("d_amount_open")) {
                        totalOpenDeb = totalOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_open")) ? temp.getString("d_amount_open") : "0");
                    }
                    if (temp.has("c_amount_period")) {
                        totalPeriodCre = totalPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_period")) ? temp.getString("c_amount_period") : "0");
                    }
                    if (temp.has("d_amount_period")) {
                        totalPeriodDeb = totalPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_period")) ? temp.getString("d_amount_period") : "0");
                    }
                    if (temp.has("ytd_c_amount_open")) {
                        totalYTDOpenCre = totalYTDOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_open")) ? temp.getString("ytd_c_amount_open") : "0");
                    }
                    if (temp.has("ytd_d_amount_open")) {
                        totalYTDOpenDeb = totalYTDOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_open")) ? temp.getString("ytd_d_amount_open") : "0");
                    }
                    if (temp.has("ytd_c_amount_period")) {
                        totalYTDPeriodCre = totalYTDPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_period")) ? temp.getString("ytd_c_amount_period") : "0");
                    }
                    if (temp.has("ytd_d_amount_period")) {
                        totalYTDPeriodDeb = totalYTDPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_period")) ? temp.getString("ytd_d_amount_period") : "0");
                    }
                    if (temp.has("ytd_c_amount")) {
                        totalYTDCre = totalYTDCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount")) ? temp.getString("ytd_c_amount") : "0");
                    }
                    if (temp.has("ytd_d_amount")) {
                        totalYTDDeb = totalYTDDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount")) ? temp.getString("ytd_d_amount") : "0");
                    }
                    if (temp.has("c_period")) {
                        totalPeriodCredit = totalPeriodCredit + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_period")) ? temp.getString("c_period") : "0");
                    }
                    if (temp.has("d_period")) {
                        totalPeriodDebit = totalPeriodDebit + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_period")) ? temp.getString("d_period") : "0");
                    }
                    if (temp.has("periodBalance")) {
                        totalPeriodBalance = totalPeriodBalance + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("periodBalance")) ? temp.getString("periodBalance") : "0");
                    }
                    if (temp.has("openingamount")) {
                        totalOpeningAmount = totalOpeningAmount + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("openingamount")) ? temp.getString("openingamount") : "0");
                    }
                    if (temp.has("endingamount")) {
                        totalEndingAmount = totalEndingAmount + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("endingamount")) ? temp.getString("endingamount") : "0");
                    }
                } else if (report == 4 || report == 8 || report == 301 || report == 827 || report == 810 || report == 807 || report == 125) { //4: Payment Received;  8: Make Payment   report==810 : "Sales by Product Category Detail Report" 
                grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.optString("amountinbase","")) ? temp.optString("amountinbase","") : "0");
                if(report == 301){
                    grandTotalAmountWithTaxInBaseCurrency = grandTotalAmountWithTaxInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbasewithtax")) ? temp.getString("amountinbasewithtax") : "0");
                }
            }
            String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
            String transactionCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
            String transactionCurrency = temp.has("transactioncurrencyid") ? temp.getString("transactioncurrencyid") : currencyid;
            for (int h = 0; h < headers.length; h++) {
                    if (h < headers.length - 1) {
                        cell = row.createCell(cellnum++);  //Create new cell
                        if (headers[h].equals("subtotal")
                                || headers[h].equals("discount")
                                || headers[h].equals("prdiscount")
                                || headers[h].equals("discountinbase")
                                || headers[h].equals("productTotalAmount")
                                || headers[h].equals("debitAmount")
                                || headers[h].equals("creditAmount")
                                || headers[h].equals("amount")
                                || headers[h].equals("amountinbase")
                                || headers[h].equals("totalprofitmargin")
                                || headers[h].equals("discountamountinbase")
                                || headers[h].equals("amountwithouttax")
                                || headers[h].equals("totaltaxamount")
                                || headers[h].equals("cost")
                                || headers[h].equals("margin")
                                || headers[h].equals("balanceamount")
                                || headers[h].equals("discountval")
                                || headers[h].equals("rowprdiscount")
                                || headers[h].equals("deductDiscount")
                                || headers[h].equals("taxamount")
                                || headers[h].equals("rowTaxAmount")
                                || headers[h].equals("discounttotal")
                                || headers[h].equals("vendorunitcost")
                                || headers[h].equals("amountForExcelFile")
                                || align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            cell.setCellStyle(amountStyle);
                        }
                                                
                        /* Set Cell Style - Bold and Italic if key - boldAndItalicFont true passed in json - specific row*/
                        if(temp.has(IndiaComplianceConstants.boldAndItalicFontStyleFlag) && !StringUtil.isNullOrEmpty(temp.getString(IndiaComplianceConstants.boldAndItalicFontStyleFlag)) && temp.getBoolean(IndiaComplianceConstants.boldAndItalicFontStyleFlag) ){
                            cell.setCellStyle(boldAndItalicStyle);
                        } else if (report == Constants.GSTR3B_Summary_Report && temp.optBoolean("showBold", false)) {
                            cell.setCellStyle(boldStyle);
                        }
                        if ((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[h])) {
                            if(StringUtil.equal(fileType, "detailedXls") && StringUtil.isNullOrEmpty(temp.optString(headers[h],""))){// Indetail XLS case at global level row data empty and at row level global data is empty, So showing as it is
                                cell.setCellValue("");//set empty data for this case
                            } else{
                                cell.setCellValue(Constants.UPAndAmtDispalyValueNoPerm);
                            }
                        } else if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
                            if(currency.contains("(") && currency.contains(")")){  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(","");
                                currency = currency.replaceAll("\\)","");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellStyle(cellStyleAmount); 
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file 
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
//                            cell.setCellStyle(amountStyle);
                            try {           
                                cell.setCellStyle(cellStyleAmount); 
                                cell.setCellType(0);
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } 
                        /*
                         * Apply currency render to Amount in transaction
                         * currency in gst form 5
                         */
                        else if (align[h].equals("rowcurrencyGstForm") && !temp.optString(headers[h], "").equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), transactionCurrency, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        }else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
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
                        }
                        else  if (report==771&&align[h].equals("date")&&temp.has("c_reconciledate")&&headers[h].equals("d_reconciledate")) {
                            if(!StringUtil.isNullOrEmpty(temp.optString("c_reconciledate",null))){
                              cell.setCellValue(formatter.format(frmt.parse(temp.getString("c_reconciledate"))));   
                            }else{
                                cell.setCellValue("");   
                            }
                            
                        } else if (headers[h].equals("jedate")) {
                            if (temp.has("jedate") && !StringUtil.isNullOrEmpty(temp.getString("jedate"))) {
                                try{ //For GST Form 5 Detail Excel
                                cell.setCellValue(userFormat.format(dateFormatForTapReturn.parse(temp.getString(headers[h]))));
                                } catch(ParseException pe){
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        } else if (headers[h].equals("creationDate")) {
                            if (temp.has("creationDate") && !StringUtil.isNullOrEmpty(temp.getString("creationDate"))) {
                                try{
                                    formatter.format(frmt.parse(temp.getString(headers[h])));
                                    cell.setCellValue(formatter.format(frmt.parse(temp.getString(headers[h]))));
                                } catch(ParseException pe){
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        }
                        else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else if (report == Constants.GSTR2A_Match_And_Reconcile_Report && ((headers[h]).equals("taxableAmt") || (headers[h]).equals("totalTax") || (headers[h]).equals("totalAmt") || (headers[h]).equals("cess"))) {
                            cell.setCellStyle(cellStyleAmount);
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
                        } else if ((report == Constants.GSTR3B_Summary_Report || report == Constants.GSTR3B_DETAIL_REPORT || report == Constants.GSTRComputationDetailReport) && ((headers[h]).equals("taxableamt") || (headers[h]).equals("totaltax") || (headers[h]).equals("totalamount") || (headers[h]).equals("igst") || (headers[h]).equals("sgst") || (headers[h]).equals("cgst") || (headers[h]).equals("csgst"))) {
                            cell.setCellStyle(cellStyleAmount);
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
                                if (temp.optBoolean("isLabel", false)) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(curr);
                                }
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else {
                            if (extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry()!= null && extraCompanyPreferences.getCompany().getCountry().getID().equals(String.valueOf(Constants.indian_country_id)) && report == 1138 && headers[h].equals("invoiceno")) {// For Indian company
                                cell.setCellValue(temp.has("invoiceno") ? temp.getString("invoiceno") : "");
                            } else if ((report == 1123 || report == 1231) && headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno") ? temp.getString("invoiceno") : "");
                            } else if ((report == 53 || report == 54) && headers[h].equals("invoiceno") && temp.has("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno")?temp.getString("invoiceno"):"");//Delivery Order export call
                            } else if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no")?temp.getString("no"):"");
                            } else if (headers[h].equals("invoicedate")) {
                                 try {
                                    cell.setCellValue(formatter.format(frmt.parse(temp.getString("date"))));
                                } catch (Exception ex) {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if (headers[h].equals("c_date")) {
                                cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))));
                            } else if (headers[h].equals("c_checkdate")) {
                                String checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                                if (!StringUtil.isNullOrEmpty(checkdate)) {
                                    try {
                                        checkdate = formatter.format(frmt.parse((checkdate)));
                                    } catch (Exception ex) {
                                        checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                                    }
                                }
                                cell.setCellValue(checkdate);
                            } else if (headers[h].equals("c_accountname")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")));
                            } else if (headers[h].equals("c_description")) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.optString("c_description")) ? temp.optString("d_description") : temp.optString("c_description")));
                            } else if (headers[h].equals("descinforeign")) {
                                cell.setCellValue(temp.optString("foreigndescription"));
                            }else if (headers[h].equals("c_acccode")) {
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
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.optString("c_transactionDetails")) ? StringUtil.serverHTMLStripper(temp.optString("d_transactionDetails")) : StringUtil.serverHTMLStripper(temp.optString("c_transactionDetails"))));
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h])) && report != 117) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")));
                            } else if (headers[h].equals("c_transactionDetailsBankBook")) {
                                if (!temp.getString("d_transactionDetailsBankBook").equalsIgnoreCase("Transfer")) {
                                    cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsBankBook")) ? temp.getString("c_transactionDetailsBankBook") : temp.getString("d_transactionDetailsBankBook")));
                                } else {
                                    cell.setCellValue(temp.getString("c_transactionDetailsBankBook"));
                                }
                            } else if (headers[h].equals("d_date")) {
                                if (report == 117) {//Ledger T report
                                    if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling case to show netbalnce in general ledger
                                        double netbalnce = temp.optDouble("netbalance", 0.0);
                                        String currency = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                        cell.setCellValue(currency);
                                    } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general ledger                                                                                           
                                        if (!temp.optString("d_date").equals("")) {
                                            cell.setCellValue(temp.getString("d_date"));
                                        } else {
                                            cell.setCellValue("");
                                        }
                                        
                                    } else if (temp.has("isTotal") && temp.optBoolean("isTotal", false)) {//Handling to dispaly Total in general ledger                                                                                           
                                        cell.setCellValue("");//In total row blank date field required
                                    } else {
                                        if (!temp.optString("d_date").equals("") || !temp.optString("c_date").equals("")) {
                                            cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                        } else {
                                            cell.setCellValue("");
                                        }
                                    }
                                } else if (report == 771) {
                                    if (!(temp.has("d_date") && temp.has("c_date"))) {
                                        cell.setCellValue("");//In
                                    } else {
                                        cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                    }
                                } else {
                                     if (!temp.optString("d_date").equals("") || !temp.optString("c_date").equals("")) {
                                        cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                    } else {
                                        cell.setCellValue("");
                                    }
                                }

                            } else if (headers[h].equals("d_accountname")) {
                                String  d_accountname="";
                                if (temp.has("d_accountname") && !StringUtil.isNullOrEmpty(temp.getString("d_accountname"))) {
                                    d_accountname = temp.getString("d_accountname");
                                } else if (temp.has("c_accountname") && !StringUtil.isNullOrEmpty(temp.getString("c_accountname"))) {
                                    d_accountname = temp.getString("c_accountname");
                                }
                                 cell.setCellValue(d_accountname);
                            } else if (headers[h].equals("d_reconciledate")) {
                                String  d_reconciledate="";
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
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                }
                                d_amount = d_amount.replaceAll(",", "");
                                try {
                                    cell.setCellType(0);
                                    double curr = (!StringUtil.isNullOrEmpty(d_amount)) ? Double.parseDouble(d_amount) : 0;
                                    
                                    if (report == Constants.GROUP_DETAIL_REPORT_ID && temp.optBoolean("mainRow") && StringUtil.isNullOrEmpty(d_amount)) {
                                        cell.setCellValue("");
                                    } else {
                                        cell.setCellValue(curr);
                                    }
                                    
                                } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(d_amount);
                                }
                            } else if (headers[h].equals("d_amountinacc")) {
                                String  amountinacc="";
                                if (temp.has("d_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("d_amountinacc"))) {
                                    amountinacc = temp.getString("d_amountinacc");
                                } else if (temp.has("c_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("c_amountinacc"))) {
                                    amountinacc = temp.getString("c_amountinacc");
                                }
                                 cell.setCellValue(amountinacc);
                            } else if (headers[h].equals("d_amountintransactioncurrency")) {
                                String amountintransactioncurrency="";
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
                            } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period") || headers[h].equals("c_period")  || headers[h].equals("d_period") || headers[h].equals("periodBalance") || headers[h].equals("openingamount") || headers[h].equals("endingamount"))) {
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
                            } else if (align[h].equals("quantity")) {
                                cell.setCellStyle(quantityStyle);
                                String tempStr = temp.optString(headers[h],"0.00");
                                cell.setCellType(0);   // 0 for numeric type
                                double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                cell.setCellValue(quantity);
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))&& !(headers[h].equals("closedqty"))) {
                                    cell.setCellValue("");
                            } else if (report == 27 && headers[h].equals("transactionDetails") && StringUtil.isNullOrEmpty(temp.optString("transactionDetails", ""))) {// for JE Export Report
                                try {
                                    cell.setCellValue(StringUtil.DecodeText(temp.optString("journalEntryDetailsDescription", "")));
                                } catch (IllegalArgumentException e) {   //To handle "%" & "+" in String, I took String value as it.
                                    cell.setCellValue(temp.optString("journalEntryDetailsDescription", ""));
                                }
                            } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                                double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                String currency = currencyRender("" + adj, currencyid, isCurrencyCode, companyid);
                                if (adj < 0.0001) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(currency);
                                }
                            } else if (isSampleFile && titles[h].equals("Opening Balance")) {
                                if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                    cell.setCellValue(temp.getString("openbalance"));
                                } else {
//                                    String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                    cell.setCellValue(temp.getString("openbalance"));
                                }
                            }else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                                if (temp.getString("openbalance").equalsIgnoreCase("-")) {
                                    cell.setCellValue(temp.getString("openbalance"));
                                } else {
                                    String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid, isCurrencyCode, companyid);
                                    cell.setCellValue(currency);
                                }
                            } // SDP-1239 Fields made numeric in excel file
                             else if (headers[h].equals("transactionno") || headers[h].equals("donumber")) {
                                String refNo = "" + temp.getString(headers[h]);
                                cell.setCellValue(refNo);
                            }
                             else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || 
                                     headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") ||
                                     headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || 
                                     headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || 
                                     headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") ||
                                     headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") ||
                                     headers[h].equals("c_transactionAmount")||headers[h].equals("doquantity")||headers[h].equals("requestqunatity") ||
                                     headers[h].equals("stockquantity")  || headers[h].equals("returnqty")||headers[h].equals("assessablevalue")||
                                     headers[h].equals("taxamount")||headers[h].equals("vatamt")||headers[h].equals("cstamt") || 
                                     headers[h].equals("blockquantity") || headers[h].equals("repairquantity") || headers[h].equals("qaquantity") || headers[h].equals("reservestock")|| headers[h].equals("closedqty")) {//blockquantity,repairquantity,qaquantity should be in number format not in text format
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        cell.setCellStyle(cellStyleAmount);
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        double quantity=(!StringUtil.isNullOrEmpty(tempStr))?Double.parseDouble(tempStr):0;
                                        cell.setCellValue(quantity);
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if ( report == 1117 && (headers[h].equals("openingstock") || headers[h].equals("purchasereturenQuantity") || headers[h].equals("closingstock"))) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        if(!StringUtil.isNullOrEmpty(tempStr)){
                                            double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                            cell.setCellValue(quantity);
                                        }else{
                                            cell.setCellValue("");
                                        }
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if (headers[h].equals("transactionexchangerate")) {
                                double exRate = temp.optDouble("transactionexchangerate", 0.0);//ERP-36142
                                cell.setCellValue(exRate);
                            } else if (headers[h].equals("subtotal") 
                                    || headers[h].equals("discount") 
                                    || headers[h].equals("discountinbase") 
                                    || headers[h].equals("productTotalAmount") 
                                    || headers[h].equals("debitAmount") 
                                    || headers[h].equals("creditAmount") 
                                    || headers[h].equals("amount") 
                                    || headers[h].equals("amountinbase")
                                    || headers[h].equals("totalprofitmargin") 
                                    || headers[h].equals("discountamountinbase") 
                                    || headers[h].equals("amountwithouttax") 
                                    || headers[h].equals("totaltaxamount") 
                                    || headers[h].equals("cost") 
                                    || headers[h].equals("margin") 
                                    || headers[h].equals("balanceamount")
                                    || headers[h].equals("vendorunitcost")
                                    || headers[h].equals("discountval")
                                    || headers[h].equals("rowprdiscount")
                                    || headers[h].equals("deductDiscount")
                                    || headers[h].equals("taxamount")
                                    || headers[h].equals("discounttotal")) {
                                try {
                                    double amount = temp.optDouble(headers[h],0.00);
                                    cell.setCellStyle(amountStyle);
                                    cell.setCellValue(amount);
                                } catch (Exception e){
                                    cell.setCellValue(temp.optString(headers[h],"0.00"));
                                }
                            } else {
                                if (titles[h].equals("Opening Balance Type")) {
                                    String str1 = "";
                                    String val = temp.optString(headers[h], "-").trim();
                                    if (val.equalsIgnoreCase("N/A") || val.equalsIgnoreCase("Credit") || val.equalsIgnoreCase("Debit")) {
                                        str1 = val;
                                    } else {
                                        if (val.equalsIgnoreCase("-")) { //chart of accounts export 
                                            str1 = "N/A";
                                        } else {
                                            double bal = Double.parseDouble(StringUtil.isNullOrEmpty(val) ? "0" : val);
                                            str1 = bal == 0 ? "N/A" : (bal < 0 ? "Credit" : "Debit");
                                        }
                                    }                                                                    
                                    cell.setCellValue(str1);
                                } else {
                                    try {
                                        JSONObject jsonObj = new JSONObject();
                                        if(report == 23 || report == 26){
                                            jsonObj.put("amountdue1_line", "amountdue1_line");
                                            jsonObj.put("amountdue2_line", "amountdue2_line");
                                            jsonObj.put("amountdue3_line", "amountdue3_line");
                                            jsonObj.put("amountdue4_line", "amountdue4_line");
                                            jsonObj.put("amountdue5_line", "amountdue5_line");
                                            jsonObj.put("amountdue6_line", "amountdue6_line");
                                            jsonObj.put("amountdue7_line", "amountdue7_line");
                                            jsonObj.put("amountdue8_line", "amountdue8_line");
                                            jsonObj.put("total_line", "total_line");
                                        }
                                        
                                        if (isFromDocumentDesigner) {
                                            cell.setCellValue(temp.getString(headers[h]));
                                        }else if(jsonObj.has(headers[h]) && (report == 23 || report == 26)) {
                                           Double tempValue = temp.optDouble(headers[h],0.0);
                                           cell.setCellValue(tempValue); 
                                        } else {
                                            String tempStr = temp.getString(headers[h]);
                                            if(temp.has("isGroupHeader") && temp.optBoolean("isGroupHeader") && requestJobj.optInt("get", 0) == Constants.GstTapReturnDetailedView){
                                                cell.setCellStyle(style);
                                            }
                                            tempStr = tempStr.replaceAll("</div>", "\n");
                                            tempStr=tempStr.replaceAll("<br>", "\n");
                                            /**
                                             * While Export if contains special characters then only decode otherwise not. SDP-14482
                                             */
                                            if(requestJobj.has("filename")&&requestJobj.getString("filename").equals("Forecasting Report_v1") && (tempStr.equals("Change Order Status - External PCIs : ") || tempStr.equals("Billings : ") || 
                                                     tempStr.equals("Receivables and Inventory : ") || tempStr.equals("Fee Summary : ") || 
                                                     tempStr.equals("Fee Summary : ") || tempStr.equals("Profit & Loss and Backlog : "))){
                                                cell.setCellStyle(boldStyle);
                                            }
                                            if (tempStr.contains("%25") || tempStr.contains("%2B")) {
                                                cell.setCellValue(URLDecoder.decode(StringUtil.replaceFullHTML(tempStr),Constants.ENCODING));
                                            } else {
                                                cell.setCellValue(StringUtil.replaceFullHTML(tempStr));
                                            }
                                        }
                                    } catch (Exception e) {
                                        cell.setCellValue(Jsoup.parse(temp.getString(headers[h])).text());
                                    }
                                    
                                }
                            }
                        }
                    } else {    //end of if(header.length)
                        cell = row.createCell(cellnum++);  //Create new cell
                        if (headers[h].equals("subtotal")
                                || headers[h].equals("discount")
                                || headers[h].equals("prdiscount")
                                || headers[h].equals("discountinbase")
                                || headers[h].equals("productTotalAmount")
                                || headers[h].equals("debitAmount")
                                || headers[h].equals("creditAmount")
                                || headers[h].equals("amount")
                                || headers[h].equals("amountinbase")
                                || headers[h].equals("totalprofitmargin")
                                || headers[h].equals("discountamountinbase")
                                || headers[h].equals("amountwithouttax")
                                || headers[h].equals("totaltaxamount")
                                || headers[h].equals("cost")
                                || headers[h].equals("margin")
                                || headers[h].equals("balanceamount")
                                || headers[h].equals("discountval")
                                || headers[h].equals("rowprdiscount")
                                || headers[h].equals("deductDiscount")
                                || headers[h].equals("taxamount")
                                || headers[h].equals("rowTaxAmount")
                                || headers[h].equals("discounttotal")
                                || headers[h].equals("vendorunitcost")
                                || headers[h].equals("amountForExcelFile")
                                || align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            cell.setCellStyle(amountStyle);
                        }
                        /* Set Cell Style - Bold and Italic if key - boldAndItalicFont true passed in json - specific row*/
                        if(temp.has(IndiaComplianceConstants.boldAndItalicFontStyleFlag) && !StringUtil.isNullOrEmpty(temp.getString(IndiaComplianceConstants.boldAndItalicFontStyleFlag)) && temp.getBoolean(IndiaComplianceConstants.boldAndItalicFontStyleFlag) ){
                            cell.setCellStyle(boldAndItalicStyle);
                        } else if (report == Constants.GSTR3B_Summary_Report && temp.optBoolean("showBold", false)) {
                            cell.setCellStyle(boldStyle);
                        }
                        
                        if ((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[h])) {
                            if(StringUtil.equal(fileType, "detailedXls") && StringUtil.isNullOrEmpty(temp.optString(headers[h],""))){// Indetail XLS case at global level row data empty and at row level global data is empty, So showing as it is
                                cell.setCellValue("");//set empty data for this case
                            } else{
                                cell.setCellValue(Constants.UPAndAmtDispalyValueNoPerm);
                            }
                        }else if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellType(0);
                                if(report == 125){
                                String curr = (!StringUtil.isNullOrEmpty(currency)) ? currency : "0";
                                 cell.setCellValue(curr);
                                }else{
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                 cell.setCellValue(curr);
                                }
                                
                               
                            } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
                            try {
                                cell.setCellStyle(cellStyleAmount); 
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(currency)) ? Double.parseDouble(currency) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(currency);
                            }
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("") && !headers[h].equals("stockRate") && !headers[h].equals("value")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
                            if (currency.contains("(") && currency.contains(")")) {  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(", "");
                                currency = currency.replaceAll("\\)", "");
                                currency = "-" + currency;
                            }
//                            cell.setCellStyle(amountStyle);
                            try {
                                cell.setCellStyle(cellStyleAmount);
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
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                } else if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                    d_amount = temp.getString("c_amount");
                                }
                            d_amount = d_amount.replaceAll(",", "");
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(d_amount)) ? Double.parseDouble(d_amount) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(d_amount);
                            }
                        } else if (headers[h].equals("c_checkdate")) {
                            String checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                            if (!StringUtil.isNullOrEmpty(checkdate)) {
                                try {
                                    checkdate = formatter.format(frmt.parse((checkdate)));
                                } catch (Exception ex) {
                                    checkdate = StringUtil.isNullOrEmpty(temp.optString("c_checkdate")) ? temp.optString("d_checkdate") : temp.optString("c_checkdate");
                                }
                            }
                            cell.setCellValue(checkdate);
                        } else if(report == Constants.GROUP_DETAIL_REPORT_ID && headers[h].equals("c_amount")) {
                            String c_amount = "";
                            if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                c_amount = temp.getString("c_amount");
                            }
                            c_amount = c_amount.replaceAll(",", "");
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file

                                double curr = (!StringUtil.isNullOrEmpty(c_amount)) ? Double.parseDouble(c_amount) : 0;

                                if (report == 1153 && temp.optBoolean("mainRow") && StringUtil.isNullOrEmpty(c_amount)) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(curr);
                                }
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(c_amount);
                            }
                        } else if (report == 116 && (headers[h].equals("d_amount_open") || headers[h].equals("c_amount_open") || headers[h].equals("c_amount_period") || headers[h].equals("d_amount_period") || headers[h].equals("c_period")  || headers[h].equals("d_period") || headers[h].equals("periodBalance") || headers[h].equals("openingamount") || headers[h].equals("endingamount"))) {
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
                        } else if (report == 204 && align[h].equals("rowcurrency") && headers[h].equals("value")) {
                            String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                            if (temp.has("delivered")) {
                                tempString = "-" + tempString;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else if ( report == 1117 && headers[h].equals("closingstock")) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        if(!StringUtil.isNullOrEmpty(tempStr)){
                                            double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                            cell.setCellValue(quantity);
                                        }else{
                                            cell.setCellValue("");
                                        }
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                         } else if ((report == Constants.GSTR3B_Summary_Report || report == Constants.GSTR3B_DETAIL_REPORT || report == Constants.GSTRComputationDetailReport) && ((headers[h]).equals("taxableamt") || (headers[h]).equals("totaltax") || (headers[h]).equals("totalamount") || (headers[h]).equals("igst") || (headers[h]).equals("sgst") || (headers[h]).equals("cgst") || (headers[h]).equals("csgst"))) {
                             cell.setCellStyle(cellStyleAmount);
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
                                if (temp.optBoolean("isLabel", false)) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(curr);
                                }
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        }else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno")?temp.getString("invoiceno"):temp.optString("no",""));
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
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.optString("d_accountname",null)) ? temp.optString("c_accountname") : temp.optString("d_accountname")));
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
                            } else if (align[h].equals("quantity")) {
                                cell.setCellStyle(quantityStyle);
                                String tempStr = temp.optString(headers[h],"0.00");
                                cell.setCellType(0);   // 0 for numeric type
                                double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                cell.setCellValue(quantity);
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
                             else if (headers[h].equals("transactionno") || headers[h].equals("donumber")) {
                                String refNo = "" + temp.getString(headers[h]);
                                cell.setCellValue(refNo);
                            }
                              else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || 
                                      headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") || 
                                      headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || 
                                      headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || 
                                      headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") || 
                                      headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") || 
                                      headers[h].equals("c_transactionAmount")||headers[h].equals("doquantity")  || headers[h].equals("returnqty")||
                                      headers[h].equals("assessablevalue")  || headers[h].equals("taxamount")||headers[h].equals("vatamt")||headers[h].equals("cstamt") || headers[h].equals("reservestock")|| headers[h].equals("closedqty")) {
                                try {
                                    String tempStr = temp.getString(headers[h]);
                                    cell.setCellType(0);      // SDP-1239 Fields made numeric in excel file
                                    double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                    cell.setCellValue(quantity);
                                } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if (headers[h].equals("subtotal") 
                                    || headers[h].equals("discount") 
                                    || headers[h].equals("discountinbase") 
                                    || headers[h].equals("productTotalAmount") 
                                    || headers[h].equals("debitAmount") 
                                    || headers[h].equals("creditAmount") 
                                    || headers[h].equals("amount") 
                                    || headers[h].equals("amountinbase")
                                    || headers[h].equals("totalprofitmargin") 
                                    || headers[h].equals("discountamountinbase") 
                                    || headers[h].equals("amountwithouttax") 
                                    || headers[h].equals("totaltaxamount") 
                                    || headers[h].equals("cost") 
                                    || headers[h].equals("margin") 
                                    || headers[h].equals("balanceamount")
                                    || headers[h].equals("discountval")
                                    || headers[h].equals("rowprdiscount")
                                    || headers[h].equals("deductDiscount")
                                    || headers[h].equals("taxamount")
                                    || headers[h].equals("vendorunitcost")
                                    || headers[h].equals("discounttotal")){
                                try {
                                    double amount = temp.optDouble(headers[h],0.00);
                                    cell.setCellStyle(amountStyle);
                                    cell.setCellValue(amount);
                                } catch (Exception e){
                                    cell.setCellValue(temp.optString(headers[h],"0.00"));
                                }
                            } else {
                                  if(report == 23 || report == 26){
                                    cell.setCellValue(temp.optDouble(headers[h],0.0));
                                  }else{
                                    try{
                                        String tempStr=temp.getString(headers[h]).replaceAll("</div>", "\n");
                                        tempStr=tempStr.replaceAll("<br>", "\n");
                                        /**
                                         * While Export if contains special characters then only decode otherwise not. SDP-14482
                                         */  
                                        if (temp.getString(headers[h]).contains("%25") || temp.getString(headers[h]).contains("%2B")) {
                                            cell.setCellValue(URLDecoder.decode((StringUtil.replaceFullHTML(tempStr)), Constants.ENCODING));
                                        } else {
                                            cell.setCellValue(StringUtil.replaceFullHTML(tempStr));
                                        }
                                    } catch (Exception e) {
                                        cell.setCellValue(Jsoup.parse(temp.getString(headers[h])).text());
                                    }
                                } 
                            }
                        }
                    }
                    /**
                     * Set VAT Out Report column styles
                     * ERP-41891 (ERM-1147)
                     */
                    if(report == Constants.INDONESIA_VAT_OUT_REPORT){
                        if(temp.optString(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), "").equalsIgnoreCase(IndonesiaConstants.COLUMNTYPE_FK)
                                && IndonesiaConstants.Amount_COLUMN_FOR_FK_ROW.contains(headers[h])){
                            cell.setCellValue(temp.optDouble(headers[h],0.0));
                            cell.setCellStyle(cellStyleAmount);
                        }else if(temp.optString(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), "").equalsIgnoreCase(IndonesiaConstants.COLUMNTYPE_LT)
                                && IndonesiaConstants.Amount_COLUMN_FOR_LT_ROW.contains(headers[h])){
                            cell.setCellValue(temp.optDouble(headers[h],0.0));
                            cell.setCellStyle(cellStyleAmount);
                        }else if(temp.optString(IndonesiaConstants.VATOUT_REPORT_DATAINDEX.COLUMN1.name(), "").equalsIgnoreCase(IndonesiaConstants.COLUMNTYPE_OF)
                                && IndonesiaConstants.Amount_COLUMN_FOR_OF_ROW.contains(headers[h])){
                            cell.setCellValue(temp.optDouble(headers[h],0.0));
                            cell.setCellStyle(cellStyleAmount);
                        }
                    }
                }//header for loop
            }//Data for loop
            if (report == 116) { //116:Trial Balance
                cellnum = 0;
                HSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodCredit), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodDebit), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("periodBalance")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodBalance), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("openingamount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpeningAmount), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("endingamount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalEndingAmount), currencyid, isCurrencyCode, companyid));
                    }
                }
            } else if (report == 4 || report == 8 || report==301 || report==827) {//4: Payment Received; 8: Make Payment
                cellnum = 0;
                HSSFRow row1 = sheet.createRow(rownum++);
                HSSFRow row2 = sheet.createRow(rownum++);
                HSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                if(report==301){
                    cell.setCellValue("Total");
                }else{
                    String gridHeader = (Arrays.toString(titles));
                    if (gridHeader.contains("Amount Paid (In Base Currency)")
                            || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                            || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                            || gridHeader.contains("Total Amount (In Base Currency)(SG Dollar(SGD)) ")) {
                        cell.setCellValue("Grand Total (In Base Currency)");
                    }
                }
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("amountinbase")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        cell.setCellValue(totalAmount);
                    }else if(headers[h].equals("amountinbasewithtax")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalAmountWithTaxInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        cell.setCellValue(totalAmount);
                    }
                }
            } else if (report == 112 && (head.contains("openbalanceinbase") || head.contains("endingBalanceSummary"))) { //112:COA - added Total at the end
                cellnum = 0;
                HSSFRow row2 = sheet.createRow(rownum++);
                HSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("openbalanceinbase")) {
                        cell.setCellValue(currencyRender(String.valueOf(obj.getDouble("openbalanceSummary")), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("endingBalance")) {
                        cell.setCellValue(currencyRender(String.valueOf(obj.optDouble("endingBalanceSummary")), currencyid, isCurrencyCode, companyid));
                    }
                }
            } else if (report == 810 || report == 807 || (report == 125 && isBasedOnProduct)) {     //report == 807 :  Monthly Sales by Product subject to GST , report==810 : "Sales by Product Category Detail Report" , report == 125 : Monthly Sales by Product
                cellnum = 0;
                HSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("amountinbase")) {
                        cell.setCellValue(withoutCurrencyRender(String.valueOf(grandTotalInBaseCurrency), companyid));
                    }
                }
            }  
            for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
                wb.getSheetAt(0).autoSizeColumn(columnIndex);
                int columnsize = wb.getSheetAt(0).getColumnWidth(columnIndex);
                if (columnsize > 10000) {
                    wb.getSheetAt(0).setColumnWidth(columnIndex, 10000);
                }
            }
            
            
//            String fileName = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
//            if (!StringUtil.isNullOrEmpty(fileName)) {
//                fileName = URLDecoder.decode(fileName, "ISO-8859-1");
//            }
//            System.out.println(fileName + ".xls written successfully.");
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xls\"");
  //          wb.write(response.getOutputStream());//To write Excel sheet data into Response object
//            response.getOutputStream().close();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
           
        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } /*catch (IOException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return sheet;
    }
    
    public XSSFWorkbook createExcelXFileNew(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        boolean dimensionBasedMonthlyPL=StringUtil.isNullOrEmpty(request.getParameter("dimensionBasedMonthlyPL")) ? false : Boolean.parseBoolean(request.getParameter("dimensionBasedMonthlyPL"));
        boolean dimensionBasedMonthlyBS=StringUtil.isNullOrEmpty(request.getParameter("dimensionBasedMonthlyBS")) ? false : Boolean.parseBoolean(request.getParameter("dimensionBasedMonthlyBS"));
        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook();
            String sheetName="Sheet-1";
            if(dimensionBasedMonthlyPL || dimensionBasedMonthlyBS){
                JSONArray dimMonthlyData=obj.getJSONArray("dimMonthlyData");
                for (int cnt = 0; cnt < dimMonthlyData.length(); cnt++) {
                    JSONObject jObj = dimMonthlyData.getJSONObject(cnt);
                    sheetName="Sheet-"+(cnt+1);
                    createSheet(request, response, jObj,colHeader, wb, sheetName); 
                }
            } else {
                createSheet(request, response, obj,colHeader, wb, sheetName);
            }
        } catch (Exception e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }
    
    public XSSFSheet createSheet(HttpServletRequest request, HttpServletResponse response, JSONObject obj, String colHeader, XSSFWorkbook wb, String sheetName) throws ServiceException, SessionExpiredException {
        XSSFSheet sheet = null;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            boolean isMonthlyReport=requestJobj.optBoolean("isMonthlyReport",false);
            if(isMonthlyReport){
                requestJobj.put("title",request.getAttribute("title").toString());
                requestJobj.put("header",request.getAttribute("header").toString());
                requestJobj.put("align",request.getAttribute("align").toString());
            }
            else if(request.getAttribute("isexportledgerflag") != null && (Boolean)request.getAttribute("isexportledgerflag")){
                requestJobj.put("title",request.getAttribute("title").toString());
                requestJobj.put("header",request.getAttribute("header").toString());
                requestJobj.put("align",request.getAttribute("align").toString());
            }
            sheet = createSheet(requestJobj, obj, colHeader, wb, sheetName);
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return sheet;
    }
    
        
    public XSSFSheet createSheet(JSONObject requestJobj, JSONObject obj,String colHeader, XSSFWorkbook wb, String sheetName) throws ServiceException, SessionExpiredException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(requestJobj.optString(Constants.dateformatid), requestJobj.optString(Constants.timezonedifference), true);
        DateFormat frmt = authHandler.getDateOnlyFormat();
        DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj.optString(Constants.userdateformat));   //date formatter for Sample file download
        SimpleDateFormat dateFormatForTapReturn = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dbFormat = authHandler.getDateWithTimeFormat();                          //Database date format
        XSSFSheet sheet = null;
        boolean isCurrencyCode = false;
        XSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = requestJobj.optString(Constants.companyKey);
        KwlReturnObject result = null;
        boolean isFromDocumentDesigner =false;
        boolean dimensionBasedMonthlyPL=requestJobj.optBoolean("dimensionBasedMonthlyPL",false);
        boolean dimensionBasedMonthlyBS=requestJobj.optBoolean("dimensionBasedMonthlyBS",false);
        try {
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            int report =requestJobj.optInt("get",-1);
            boolean isSampleFile =obj.optString("isSampleFile").equals("T");
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0, grandTotalAmountWithTaxInBaseCurrency=0;
            boolean excludeCustomHeaders = requestJobj.optBoolean("excludeCustomHeaders",false);
            String module = requestJobj.optString("moduleId","0000");
//            int moduleId = Integer.parseInt(module);
            int moduleId = requestJobj.optInt("moduleId",0000);
            boolean isBasedOnProduct=requestJobj.optBoolean("isBasedOnProduct",false);
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String tit="";
            String head = "";
            String algn = "";
            isFromDocumentDesigner = obj.optBoolean("isFromDocumentDesigner",false);
            if (requestJobj.optString("header",null) != null) {
                 tit = requestJobj.optString("title");
                head = requestJobj.optString("header");
                algn = requestJobj.optString("align");

                if (report == Constants.DimensionBasedProfitLossReport || report == Constants.DimensionBasedBalanceSheetReport || report == 775) {
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
                }
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                if(dimensionBasedMonthlyPL || dimensionBasedMonthlyBS){
                    String customDimensionName = (obj.has("customDimensionName") && obj.get("customDimensionName")!=null) ? obj.get("customDimensionName").toString():"";
                    if(!StringUtil.isNullOrEmpty(customDimensionName)){
                        for (int cnt = 1; cnt < titles.length-1; cnt++) {
                            titles[cnt] += " (" + customDimensionName + ")";
                        }
                    }
                }
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
            String currencyid = requestJobj.optString(Constants.globalCurrencyKey);
            sheetName=StringUtil.isNullOrEmpty(sheetName)?"Sheet-1":sheetName;
            sheet = wb.createSheet(sheetName);
            cell = writeFilterDetailsInExcelXLSX(requestJobj,sheet,cell,rownum,cellnum,obj);
            rownum+=cell.getRow().getRowNum()+1;
            XSSFRow headerRow = sheet.createRow(rownum++);
            JSONArray repArr = obj.getJSONArray("data");
            companyid = requestJobj.optString("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }
            
            /* 
            Bold and Italic Style created to apply on Sub-totals as per reports 
            */ 
            XSSFCellStyle boldAndItalicStyle = wb.createCellStyle();
            XSSFFont boldAndItalicFont = wb.createFont();
            boldAndItalicFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
            boldAndItalicFont.setItalic(true);
            boldAndItalicStyle.setFont(boldAndItalicFont);
            
            /* 'excludeCustomHeaders' check is added to improve performance while exporting records. 
               Purpose: Insert Custom field headers on server side rather than sending all of them in URL. 
               It will reduce size of URL.*/
            if (excludeCustomHeaders) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(requestJobj.optString("companyid"), moduleId));
                result = accCommonTablesDAO.getFieldParamsforSpecificFields(requestParams);

                ArrayList<String> headList = new ArrayList<String>();
                Collections.addAll(headList, headers);
                if (result.getRecordTotalCount() > 0) {
                    List<String> fields = result.getEntityList();
                    Iterator itr = fields.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String label = oj[2].toString();
                        String fieldname = oj[1].toString();
                        if (headList.contains(fieldname)) {
                            int index = headList.indexOf(fieldname);
                            titles[index] = label;
                        }
                    }
                }
                headers = headList.toArray(new String[headList.size()]);
            }
            //Insert Headers
            cellnum = 0;
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (h < headers.length - 1) {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("") && report!=Constants.GROUP_DETAIL_REPORT_ID) { // Dont' add currency symbol in GL header
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                    } else {
                        cell.setCellValue(headerStr);
                    }
                } else {
                    cell = headerRow.createCell(cellnum++);  //Create new cell
                    if (align[h].equals("currency") && !headers[h].equals("") && report!=Constants.GROUP_DETAIL_REPORT_ID) { // Dont' add currency symbol in GL header
                        String currency = currencyRender("", currencyid, companyid);
                        cell.setCellValue(headerStr + "(" + currency + ")");
                    } else {
                        cell.setCellValue(headerStr);
                    }
                }
            }//headers loop

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                XSSFRow row = sheet.createRow(rownum++);
                JSONObject temp = repArr.getJSONObject(t);
              if (report == 116) { //116:Trial Balance
                if(temp.has("c_amount")){
                    totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                }
                if(temp.has("d_amount")){
                    totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                }
                if (temp.has("c_amount_open")) {
                    totalOpenCre = totalOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_open")) ? temp.getString("c_amount_open") : "0");
                }
                if (temp.has("d_amount_open")) {
                    totalOpenDeb = totalOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_open")) ? temp.getString("d_amount_open") : "0");
                }
                if (temp.has("c_amount_period")) {
                    totalPeriodCre = totalPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_period")) ? temp.getString("c_amount_period") : "0");
                }
                if (temp.has("d_amount_period")) {
                    totalPeriodDeb = totalPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_period")) ? temp.getString("d_amount_period") : "0");
                }
                if (temp.has("ytd_c_amount_open")) {
                    totalYTDOpenCre = totalYTDOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_open")) ? temp.getString("ytd_c_amount_open") : "0");
                }
                if (temp.has("ytd_d_amount_open")) {
                    totalYTDOpenDeb = totalYTDOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_open")) ? temp.getString("ytd_d_amount_open") : "0");
                }
                if (temp.has("ytd_c_amount_period")) {
                    totalYTDPeriodCre = totalYTDPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_period")) ? temp.getString("ytd_c_amount_period") : "0");
                }
                if (temp.has("ytd_d_amount_period")) {
                    totalYTDPeriodDeb = totalYTDPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_period")) ? temp.getString("ytd_d_amount_period") : "0");
                }
                if (temp.has("ytd_c_amount")) {
                    totalYTDCre = totalYTDCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount")) ? temp.getString("ytd_c_amount") : "0");
                }
                if (temp.has("ytd_d_amount")) {
                    totalYTDDeb = totalYTDDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount")) ? temp.getString("ytd_d_amount") : "0");
                }
            } else if (report == 4 || report == 8 || report == 301 || report == 827 || report == 810 || report == 807 || report == 125) { //4: Payment Received;  8: Make Payment   report==810 : "Sales by Product Category Detail Report" 
                grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbase")) ? temp.getString("amountinbase") : "0");
                if(report == 301){
                    grandTotalAmountWithTaxInBaseCurrency = grandTotalAmountWithTaxInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbasewithtax")) ? temp.getString("amountinbasewithtax") : "0");
                }
            }
            String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
            String transactionCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
             String transactionCurrency = temp.has("transactioncurrencyid") ? temp.getString("transactioncurrencyid") : currencyid;
                for (int h = 0; h < headers.length; h++) {
                    if (h < headers.length - 1) {
                        cell = row.createCell(cellnum++);  //Create new cell
                        
                        /* Set Cell Style - Bold and Italic if key - boldAndItalicFont true passed in json - specific row*/
                        if(temp.has(IndiaComplianceConstants.boldAndItalicFontStyleFlag) && !StringUtil.isNullOrEmpty(temp.getString(IndiaComplianceConstants.boldAndItalicFontStyleFlag)) && temp.getBoolean(IndiaComplianceConstants.boldAndItalicFontStyleFlag) ){
                            cell.setCellStyle(boldAndItalicStyle);
                        }
                        
                        if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
                            if(currency.contains("(") && currency.contains(")")){  // SDP-2996 - if check added to show -ve numbers as -287.25 instead of (287.25)
                                currency = currency.replaceAll("\\(","");
                                currency = currency.replaceAll("\\)","");
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
                            currency = currency.replaceAll(",","");
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
                        } 
                        /*
                         * Apply currency render to Amount in transaction
                         * currency in gst form 5
                         */
                        else if (align[h].equals("rowcurrencyGstForm") && !temp.optString(headers[h], "").equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), transactionCurrency, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        }else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
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
                        }
                        else  if (report==771&&align[h].equals("date")&&temp.has("c_reconciledate")&&headers[h].equals("d_reconciledate")) {
                            cell.setCellValue(formatter.format(frmt.parse(temp.getString("c_reconciledate"))));
                        } else if (headers[h].equals("jedate")) {
                            if (temp.has("jedate") && !StringUtil.isNullOrEmpty(temp.getString("jedate"))) {
                                cell.setCellValue(userFormat.format(dateFormatForTapReturn.parse(temp.getString(headers[h]))));
                            }
                        } else if (headers[h].equals("creationDate")) {
                            if (temp.has("creationDate") && !StringUtil.isNullOrEmpty(temp.getString("creationDate"))) {
                                try{
                                    cell.setCellValue(formatter.format(frmt.parse(temp.getString(headers[h]))));
                                } catch(ParseException pe){
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else {
                            if (extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry()!= null && extraCompanyPreferences.getCompany().getCountry().getID().equals(String.valueOf(Constants.indian_country_id)) && report == 1138 && headers[h].equals("invoiceno")) {// For Indian company
                                cell.setCellValue(temp.has("invoiceno") ? temp.getString("invoiceno") : "");
                            } else if (report == 1123 && headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno") ? temp.getString("invoiceno") : "");
                            } else if (report == 53 && headers[h].equals("invoiceno") && temp.has("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno")?temp.getString("invoiceno"):"");//Delivery Order export call
                            } else if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no")?temp.getString("no"):"");
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
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.optString("c_transactionDetails")) ? StringUtil.serverHTMLStripper(temp.optString("d_transactionDetails")) : StringUtil.serverHTMLStripper(temp.optString("c_transactionDetails"))));
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h])) && report != 117) {
                                cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")));
                            } else if (headers[h].equals("c_transactionDetailsBankBook")) {
                                if (!temp.getString("d_transactionDetailsBankBook").equalsIgnoreCase("Transfer")) {
                                    cell.setCellValue((StringUtil.isNullOrEmpty(temp.getString("d_transactionDetailsBankBook")) ? temp.getString("c_transactionDetailsBankBook") : temp.getString("d_transactionDetailsBankBook")));
                                } else {
                                    cell.setCellValue(temp.getString("c_transactionDetailsBankBook"));
                                }
                            } else if (headers[h].equals("d_date")) {
                                if (report == 117) {//Ledger T report
                                    if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling case to show netbalnce in general ledger
                                        double netbalnce = temp.optDouble("netbalance", 0.0);
                                        String currency = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                        cell.setCellValue(currency);
                                    } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general ledger                                                                                           
                                        if (!temp.optString("d_date").equals("")) {
                                            cell.setCellValue(temp.getString("d_date"));
                                        } else {
                                            cell.setCellValue("");
                                        }
                                        
                                    } else if (temp.has("isTotal") && temp.optBoolean("isTotal", false)) {//Handling to dispaly Total in general ledger                                                                                           
                                        cell.setCellValue("");//In total row blank date field required
                                    } else {
                                        if (!temp.optString("d_date").equals("") || !temp.optString("c_date").equals("")) {
                                            cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                        } else {
                                            cell.setCellValue("");
                                        }
                                    }
                                } 
                                if (report == 771) {
                                    if (!(temp.has("d_date") && temp.has("c_date"))) {
                                        cell.setCellValue("");//In
                                    } else {
                                        cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                    }
                                } else {
                                     if (!temp.optString("d_date").equals("") || !temp.optString("c_date").equals("")) {
                                        cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                    } else {
                                        cell.setCellValue("");
                                    }
                                }

                            } else if (headers[h].equals("d_accountname")) {
                                String  d_accountname="";
                                if (temp.has("d_accountname") && !StringUtil.isNullOrEmpty(temp.getString("d_accountname"))) {
                                    d_accountname = temp.getString("d_accountname");
                                } else if (temp.has("c_accountname") && !StringUtil.isNullOrEmpty(temp.getString("c_accountname"))) {
                                    d_accountname = temp.getString("c_accountname");
                                }
                                 cell.setCellValue(d_accountname);
                            } else if (headers[h].equals("d_reconciledate")) {
                                String  d_reconciledate="";
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
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                }
                                d_amount = d_amount.replaceAll(",", "");
                                try {
                                    cell.setCellType(0);
                                    double curr = (!StringUtil.isNullOrEmpty(d_amount)) ? Double.parseDouble(d_amount) : 0;
                                    
                                    if (report == Constants.GROUP_DETAIL_REPORT_ID && temp.optBoolean("mainRow") && StringUtil.isNullOrEmpty(d_amount)) {
                                        cell.setCellValue("");
                                    } else {
                                        cell.setCellValue(curr);
                                    }
                                    
                                } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(d_amount);
                                }
                            } 
                            else if (headers[h].equals("c_amount")) {
                                String  c_amount="";
                                if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                    c_amount = temp.getString("c_amount");
                                }
                                c_amount = c_amount.replaceAll(",", "");
                                try {
                                    cell.setCellType(0);
                                    double curr = (!StringUtil.isNullOrEmpty(c_amount)) ? Double.parseDouble(c_amount) : 0;
                                    
                                    if (report == Constants.GROUP_DETAIL_REPORT_ID && temp.optBoolean("mainRow") && StringUtil.isNullOrEmpty(c_amount)) {
                                        cell.setCellValue("");
                                    } else {
                                        cell.setCellValue(curr);
                                    }
                                    
                                } catch (Exception e) {  // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(c_amount);
                                }
                            } else if (headers[h].equals("d_amountinacc")) {
                                String  amountinacc="";
                                if (temp.has("d_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("d_amountinacc"))) {
                                    amountinacc = temp.getString("d_amountinacc");
                                } else if (temp.has("c_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("c_amountinacc"))) {
                                    amountinacc = temp.getString("c_amountinacc");
                                }
                                 cell.setCellValue(amountinacc);
                            } else if (headers[h].equals("d_amountintransactioncurrency")) {
                                String amountintransactioncurrency="";
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
                            } else if (report == 27 && headers[h].equals("transactionDetails") && StringUtil.isNullOrEmpty(temp.optString("transactionDetails", ""))) {// for JE Export Report
                                try {
                                    cell.setCellValue(StringUtil.DecodeText(temp.optString("journalEntryDetailsDescription", "")));
                                } catch (IllegalArgumentException e) {   //To handle "%" & "+" in String, I took String value as it.
                                    cell.setCellValue(temp.optString("journalEntryDetailsDescription", ""));
                                }
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
                             else if (headers[h].equals("transactionno") || headers[h].equals("donumber")) {
                                String refNo = "" + temp.getString(headers[h]);
                                cell.setCellValue(refNo);
                            }
                             else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || 
                                     headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") ||
                                     headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || 
                                     headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || 
                                     headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") ||
                                     headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") ||
                                     headers[h].equals("c_transactionAmount")||headers[h].equals("doquantity")||headers[h].equals("requestqunatity") ||
                                     headers[h].equals("stockquantity")  || headers[h].equals("returnqty")||headers[h].equals("assessablevalue")||
                                     headers[h].equals("taxamount")||headers[h].equals("vatamt")||headers[h].equals("cstamt")|| headers[h].equals("balance")) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        double quantity=(!StringUtil.isNullOrEmpty(tempStr))?Double.parseDouble(tempStr):0;
                                        cell.setCellValue(quantity);
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else if ( report == 1117 && (headers[h].equals("openingstock") || headers[h].equals("purchasereturenQuantity") || headers[h].equals("closingstock"))) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        if(!StringUtil.isNullOrEmpty(tempStr)){
                                            double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                            cell.setCellValue(quantity);
                                        }else{
                                            cell.setCellValue("");
                                        }
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                if (titles[h].equals("Opening Balance Type")) {
                                    String str1 = "";
                                    try {
                                        if (temp.getString(headers[h]).equalsIgnoreCase("-")) { //chart of accounts export 
                                            str1 = "N/A";
                                        } else {
                                            double bal = Double.parseDouble(temp.getString(headers[h]));
                                            str1 = bal == 0 ? "N/A" : (bal < 0 ? "Credit" : "Debit");
                                        }
                                    } catch (NumberFormatException ex) {
                                        str1 = temp.getString(headers[h]);
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
                        
                        /* Set Cell Style - Bold and Italic if key - boldAndItalicFont true passed in json - specific row*/
                        if(temp.has(IndiaComplianceConstants.boldAndItalicFontStyleFlag) && !StringUtil.isNullOrEmpty(temp.getString(IndiaComplianceConstants.boldAndItalicFontStyleFlag)) && temp.getBoolean(IndiaComplianceConstants.boldAndItalicFontStyleFlag) ){
                            cell.setCellStyle(boldAndItalicStyle);
                        }
                        
                        if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
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
                            currency = currency.replaceAll(",","");
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
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                } else if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                    d_amount = temp.getString("c_amount");
                                }
                            d_amount = d_amount.replaceAll(",", "");
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(d_amount)) ? Double.parseDouble(d_amount) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(d_amount);
                            }
                        } else if(report == Constants.GROUP_DETAIL_REPORT_ID && headers[h].equals("c_amount")) {
                            String c_amount = "";
                            if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                c_amount = temp.getString("c_amount");
                            }
                            c_amount = c_amount.replaceAll(",", "");
                            try {
                                cell.setCellType(0);     // SDP-1239 Fields made numeric in excel file

                                double curr = (!StringUtil.isNullOrEmpty(c_amount)) ? Double.parseDouble(c_amount) : 0;

                                if (report == 1153 && temp.optBoolean("mainRow") && StringUtil.isNullOrEmpty(c_amount)) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(curr);
                                }
                            } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(c_amount);
                            }
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
                        } else if (report == 204 && align[h].equals("rowcurrency") && headers[h].equals("value")) {
                            String tempString = !StringUtil.isNullOrEmpty(temp.optString(headers[h], "")) ? temp.optString(headers[h], "") : "";
                            if (temp.has("delivered")) {
                                tempString = "-" + tempString;
                            }
                            try {
                                cell.setCellType(0);             // SDP-1239 Fields made numeric in excel file
                                double curr = (!StringUtil.isNullOrEmpty(tempString)) ? Double.parseDouble(tempString) : 0;
                                cell.setCellValue(curr);
                            } catch (Exception e) {            // try-catch block added to make Fields string in excel file as previous.
                                cell.setCellValue(tempString);
                            }
                        } else if ( report == 1117 && headers[h].equals("closingstock")) {
                                try {
                                    if (isFromDocumentDesigner) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    } else {
                                        String tempStr = temp.getString(headers[h]);
                                        cell.setCellType(0);   // 0 for numeric type
                                        if(!StringUtil.isNullOrEmpty(tempStr)){
                                            double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                            cell.setCellValue(quantity);
                                        }else{
                                            cell.setCellValue("");
                                        }
                                    }
                                } catch (Exception e) {   // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("invoiceno")?temp.getString("invoiceno"):temp.optString("no",""));
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
                             else if (headers[h].equals("transactionno") || headers[h].equals("donumber")) {
                                String refNo = "" + temp.getString(headers[h]);
                                cell.setCellValue(refNo);
                            }
                              else if (headers[h].equals("quantity") || headers[h].equals("balanceQuantity") || headers[h].equals("dquantity") || 
                                      headers[h].equals("invamount") || headers[h].equals("invamountdue") || headers[h].equals("taxpercent") || 
                                      headers[h].equals("taxamountforaccount") || headers[h].equals("totalamountforaccount") || headers[h].equals("orderrate") || 
                                      headers[h].equals("totalprofitmarginpercent") || headers[h].equals("rate") || headers[h].equals("partamount") || 
                                      headers[h].equals("prdiscount") || headers[h].equals("rowTaxAmount") || headers[h].equals("amountForExcelFile") || 
                                      headers[h].equals("received") || headers[h].equals("delivered") || headers[h].equals("d_transactionAmount") || 
                                      headers[h].equals("c_transactionAmount")||headers[h].equals("doquantity")  || headers[h].equals("returnqty")||
                                      headers[h].equals("assessablevalue")  || headers[h].equals("taxamount")||headers[h].equals("vatamt")||headers[h].equals("cstamt") || headers[h].equals("balance")) {
                                try {
                                    String tempStr = temp.getString(headers[h]);
                                    cell.setCellType(0);      // SDP-1239 Fields made numeric in excel file
                                    double quantity = (!StringUtil.isNullOrEmpty(tempStr)) ? Double.parseDouble(tempStr) : 0;
                                    cell.setCellValue(quantity);
                                } catch (Exception e) {      // try-catch block added to make Fields string in excel file as previous.
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            } else {
                                    cell.setCellValue(temp.getString(headers[h]));
                                }
                            }
                        }
                }//header for loop
            }//Data for loop
            if (report == 116) { //116:Trial Balance
                cellnum = 0;
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDDeb), currencyid, isCurrencyCode, companyid));
                    }
                }
            } else if (report == 4 || report == 8 || report==301 || report==827) {//4: Payment Received; 8: Make Payment
                cellnum = 0;
                XSSFRow row1 = sheet.createRow(rownum++);
                XSSFRow row2 = sheet.createRow(rownum++);
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                if(report==301){
                    cell.setCellValue("Total");
                }else{
                    String gridHeader = (Arrays.toString(titles));
                    if (gridHeader.contains("Amount Paid (In Base Currency)")
                            || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                            || gridHeader.contains("Total Purchase Price with Tax in Base Currency (SG Dollar (SGD))")
                            || gridHeader.contains("Total Amount (In Base Currency)(SG Dollar(SGD)) ")) {
                        cell.setCellValue("Grand Total (In Base Currency)");
                    }
                }
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("amountinbase")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        cell.setCellValue(totalAmount);
                    }else if(headers[h].equals("amountinbasewithtax")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalAmountWithTaxInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        cell.setCellValue(totalAmount);
                    }
                }
            } else if (report == 112 && (head.contains("openbalanceinbase") || head.contains("endingBalanceSummary"))) { //112:COA - added Total at the end
                cellnum = 0;
                XSSFRow row2 = sheet.createRow(rownum++);
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("openbalanceinbase")) {
                        cell.setCellValue(currencyRender(String.valueOf(obj.getDouble("openbalanceSummary")), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("endingBalance")) {
                        cell.setCellValue(currencyRender(String.valueOf(obj.optDouble("endingBalanceSummary")), currencyid, isCurrencyCode, companyid));
                    }
                }
            } else if (report == 810 || report == 807 || (report == 125 && isBasedOnProduct)) {     //report == 807 :  Monthly Sales by Product subject to GST , report==810 : "Sales by Product Category Detail Report" , report == 125 : Monthly Sales by Product
                cellnum = 0;
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("amountinbase")) {
                        cell.setCellValue(withoutCurrencyRender(String.valueOf(grandTotalInBaseCurrency), companyid));
                    }
                }
            }  
//            String fileName = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
//            if (!StringUtil.isNullOrEmpty(fileName)) {
//                fileName = URLDecoder.decode(fileName, "ISO-8859-1");
//            }
//            System.out.println(fileName + ".xls written successfully.");
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xls\"");
  //          wb.write(response.getOutputStream());//To write Excel sheet data into Response object
//            response.getOutputStream().close();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
           
        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (IOException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/ catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return sheet;
    }
    
    public XSSFWorkbook createExcelXFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
        DateFormat frmt = authHandler.getDateFormatter(request);
        XSSFWorkbook wb = null;
        XSSFSheet sheet = null;
        boolean isCurrencyCode = false;
        XSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = "";
        boolean isFromDocumentDesigner =false;
        try {
            int report = Integer.parseInt(request.getParameter("get"));
            boolean displayUnitPriceandAmtInSalesDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,true);
            boolean displayUnitPriceandAmtInPurchaseDocPerm = getDisplayUnitPriceandAmtInDocPerm(request,report,false);
            Set<String> unitPriceAmountheaderName = new HashSet<>();
            if (!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) {
                unitPriceAmountheaderName = getUnitPriceAndAmountDataIndexSet(report,displayUnitPriceandAmtInSalesDocPerm,displayUnitPriceandAmtInPurchaseDocPerm);
            }
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0 ;
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            isFromDocumentDesigner = obj.optBoolean("isFromDocumentDesigner",false);
            if (request.getParameter("header") != null) {
                String head = request.getParameter("header");
                String tit = request.getParameter("title");
                String algn = request.getParameter("align");
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) algn.split(",");
            } else {
                headers = (String[]) obj.getString("header").split(",");
                titles = (String[]) obj.getString("title").split(",");
                align = (String[]) obj.getString("align").split(",");
            }
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            wb = new XSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            if (!isFromDocumentDesigner) {
                cell = writeFilterDetailsInExcel(request, sheet, cell, rownum, cellnum);
                rownum = cell.getRow().getRowNum() + 1;
            }
            XSSFRow headerRow = sheet.createRow(rownum++);
            JSONArray repArr = obj.getJSONArray("data");
            companyid = request.getParameter("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }
            
            //Insert Headers
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (!headerStr.equals("Discountispersent") && !headerStr.equals("flatdiscount")) {
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
                }
            }//headers loop

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                XSSFRow row = sheet.createRow(rownum++);
                JSONObject temp = repArr.getJSONObject(t);
              if (report == 116) { //116:Trial Balance
                if(temp.has("c_amount")){
                    totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                }
                if(temp.has("d_amount")){
                    totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                }
                if (temp.has("c_amount_open")) {
                    totalOpenCre = totalOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_open")) ? temp.getString("c_amount_open") : "0");
                }
                if (temp.has("d_amount_open")) {
                    totalOpenDeb = totalOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_open")) ? temp.getString("d_amount_open") : "0");
                }
                if (temp.has("c_amount_period")) {
                    totalPeriodCre = totalPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount_period")) ? temp.getString("c_amount_period") : "0");
                }
                if (temp.has("d_amount_period")) {
                    totalPeriodDeb = totalPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount_period")) ? temp.getString("d_amount_period") : "0");
                }
                if (temp.has("ytd_c_amount_open")) {
                    totalYTDOpenCre = totalYTDOpenCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_open")) ? temp.getString("ytd_c_amount_open") : "0");
                }
                if (temp.has("ytd_d_amount_open")) {
                    totalYTDOpenDeb = totalYTDOpenDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_open")) ? temp.getString("ytd_d_amount_open") : "0");
                }
                if (temp.has("ytd_c_amount_period")) {
                    totalYTDPeriodCre = totalYTDPeriodCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount_period")) ? temp.getString("ytd_c_amount_period") : "0");
                }
                if (temp.has("ytd_d_amount_period")) {
                    totalYTDPeriodDeb = totalYTDPeriodDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount_period")) ? temp.getString("ytd_d_amount_period") : "0");
                }
                if (temp.has("ytd_c_amount")) {
                    totalYTDCre = totalYTDCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_c_amount")) ? temp.getString("ytd_c_amount") : "0");
                }
                if (temp.has("ytd_d_amount")) {
                    totalYTDDeb = totalYTDDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("ytd_d_amount")) ? temp.getString("ytd_d_amount") : "0");
                }
            } else if (report == 4 || report == 8) { //4: Payment Received;  8: Make Payment
                grandTotalInBaseCurrency = grandTotalInBaseCurrency + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("amountinbase")) ? temp.getString("amountinbase") : "0");
            }
            String rowCurrencyId = temp.has("currencyid") ? temp.getString("currencyid") : currencyid;
            String transactionCurrencyId = temp.has("transactionCurrency") ? temp.getString("transactionCurrency") : currencyid;
                for (int h = 0; h < headers.length; h++) {
                    if (h < headers.length - 1) {
                        cell = row.createCell(cellnum++);  //Create new cell
                        if ((!displayUnitPriceandAmtInSalesDocPerm || !displayUnitPriceandAmtInPurchaseDocPerm) && unitPriceAmountheaderName.contains(headers[h])) {
                            cell.setCellValue(Constants.UPAndAmtDispalyValueNoPerm);
                        } else if (align[h].equals("currency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("transactioncurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), transactionCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                            try {
                                String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                cell.setCellValue(d1);
                            } catch (Exception ex) {
                                cell.setCellValue(temp.getString(headers[h]));
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no")?temp.getString("no"):"");
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
                                if (report == 117) {//Ledger T report
                                    if (temp.has("isnetbalance") && temp.optBoolean("isnetbalance", false)) {//Handling case to show netbalnce in general ledger
                                        double netbalnce = temp.optDouble("netbalance", 0.0);
                                        String currency = "Net Balance " + currencyRender(String.valueOf(netbalnce), currencyid, isCurrencyCode, companyid);
                                        cell.setCellValue(currency);
                                    } else if (temp.has("isgroupname") && temp.optBoolean("isgroupname", false)) {//Handling to dispaly groupname in general ledger                                                                                           
                                        cell.setCellValue(temp.getString("d_date"));
                                    } else if (temp.has("isTotal") && temp.optBoolean("isTotal", false)) {//Handling to dispaly Total in general ledger                                                                                           
                                        cell.setCellValue("");//In total row blank date field required
                                    } else {
                                        cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                    }
                                } if (report == 771) {
                                  if(!(temp.has("d_date") &&  temp.has("c_date"))){
                                    cell.setCellValue("");//In
                                  }else{                                  
                                    cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                  }
                            }     else {
                                    cell.setCellValue(formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("d_date")))));
                                }

                            } else if (headers[h].equals("d_accountname")) {
                                String  d_accountname="";
                                if (temp.has("d_accountname") && !StringUtil.isNullOrEmpty(temp.getString("d_accountname"))) {
                                    d_accountname = temp.getString("d_accountname");
                                } else if (temp.has("c_accountname") && !StringUtil.isNullOrEmpty(temp.getString("c_accountname"))) {
                                    d_accountname = temp.getString("c_accountname");
                                }
                                 cell.setCellValue(d_accountname);
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
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                } else if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                    d_amount = temp.getString("c_amount");
                                }
                                 cell.setCellValue(d_amount);
                            } else if (headers[h].equals("d_amountinacc")) {
                                String  amountinacc="";
                                if (temp.has("d_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("d_amountinacc"))) {
                                    amountinacc = temp.getString("d_amountinacc");
                                } else if (temp.has("c_amountinacc") && !StringUtil.isNullOrEmpty(temp.getString("c_amountinacc"))) {
                                    amountinacc = temp.getString("c_amountinacc");
                                }
                                 cell.setCellValue(amountinacc);
                            } else if (headers[h].equals("d_amountintransactioncurrency")) {
                                String amountintransactioncurrency="";
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
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                cell.setCellValue("");
                            } else if (report == 27 && headers[h].equals("transactionDetails") && StringUtil.isNullOrEmpty(temp.optString("transactionDetails", ""))) {// for JE Export Report
                                try {
                                    cell.setCellValue(StringUtil.DecodeText(temp.optString("journalEntryDetailsDescription", "")));
                                } catch (IllegalArgumentException e) {   //To handle "%" & "+" in String, I took String value as it.
                                    cell.setCellValue(temp.optString("journalEntryDetailsDescription", ""));
                                }
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
                                            if ( headers[h].equals("image")) {
                                                XSSFSheet sheet2 = wb.createSheet("Sheet-2");
                                                Drawing drawing = sheet2.createDrawingPatriarch();
                                                String imgStr = temp.getString(headers[h]);
                                                JSONArray imgArr = new JSONArray(imgStr);
                                                for (int i = 0; i < imgArr.length(); i++) {
                                                    String imagePath = storageHandlerImpl.GetDocStorePath() + imgArr.getJSONObject(i).getString("image");
                                                    InputStream inputStream = new FileInputStream(imagePath);
                                                    byte[] bytes = IOUtils.toByteArray(inputStream);
                                                    int id = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                                                    inputStream.close();
                                                    CreationHelper helper = wb.getCreationHelper();
                                                    ClientAnchor anchor = helper.createClientAnchor();
                                                    anchor.setCol1(i);
                                                    anchor.setRow1(2);
                                                    Picture pict = drawing.createPicture(anchor, id);
                                                }
                                            }
                                        } else {
                                            if (!headers[h].equals("discountispercent") && !headers[h].equals("flatdiscount")) {
                                                String html = StringUtil.DecodeText(temp.optString(headers[h]));
                                                Whitelist wl = Whitelist.simpleText();// For removing extra character
                                                String ValueStr = Jsoup.clean(html, wl);
                                                ValueStr = ValueStr.replaceAll("&amp;", "&");
                                                ValueStr = ValueStr.replaceAll("&nbsp;", " ");
                                                ValueStr = ValueStr.toString().replaceAll("\\<.*?>", "");
                                                cell.setCellValue(ValueStr);
                                            }
                                             if (titles[h].equals("E Way Tax Rate") ) {
                                                 String ValueStr = temp.optString(headers[h]);
                                                cell.setCellValue(ValueStr);
                                        }
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
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("rowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("unitpricecurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = unitPriceCurrencyRender(temp.getString(headers[h]), rowCurrencyId, isCurrencyCode, companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("withoutrowcurrency") && !temp.optString(headers[h], "").equals("")) {
                            String currency = withoutCurrencyRender(temp.getString(headers[h]), companyid);
                            cell.setCellValue(currency);
                        } else if (align[h].equals("date") && !temp.optString(headers[h], "").equals("")) {
                            try {
                                String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                cell.setCellValue(d1);
                            } catch (Exception ex) {
                                cell.setCellValue(temp.getString(headers[h]));
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.optString(headers[h], "").equals("")) {
                            cell.setCellValue(htmlPercentageRender(temp.getString(headers[h]), true));
                        } else if (headers[h].equals("d_amount")) {
                                String  d_amount="";
                                if (temp.has("d_amount") && !StringUtil.isNullOrEmpty(temp.getString("d_amount"))) {
                                    d_amount = temp.getString("d_amount");
                                } else if (temp.has("c_amount") && !StringUtil.isNullOrEmpty(temp.getString("c_amount"))) {
                                    d_amount = temp.getString("c_amount");
                                }
                                 cell.setCellValue(d_amount);
                        }else {
                            if (headers[h].equals("invoiceno")) {
                                cell.setCellValue(temp.has("no")?temp.getString("no"):"");
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
                            } else {
                                
                                 try {
                                        if (isFromDocumentDesigner) {
                                            cell.setCellValue(temp.getString(headers[h]));
                                            if ( headers[h].equals("image")) {
                                                XSSFSheet sheet2 = wb.createSheet("Sheet-2");
                                                Drawing drawing = sheet2.createDrawingPatriarch();
                                                String imgStr = temp.getString(headers[h]);
                                                JSONArray imgArr = new JSONArray(imgStr);
                                                for (int i = 0; i < imgArr.length(); i++) {
                                                    String imagePath = storageHandlerImpl.GetDocStorePath() + imgArr.getJSONObject(i).getString("image");
                                                    InputStream inputStream = new FileInputStream(imagePath);
                                                    byte[] bytes = IOUtils.toByteArray(inputStream);
                                                    int id = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                                                    inputStream.close();
                                                    CreationHelper helper = wb.getCreationHelper();
                                                    ClientAnchor anchor = helper.createClientAnchor();
                                                    anchor.setCol1(i);
                                                    anchor.setRow1(2);
                                                    Picture pict = drawing.createPicture(anchor, id);
                                                }
                                            }
                                       } else {
                                         if (!headers[h].equals("discountispercent") && !headers[h].equals("flatdiscount")) {
                                             String html = StringUtil.DecodeText(temp.optString(headers[h]));
                                             Whitelist wl = Whitelist.simpleText();//For removing extra character
                                             String ValueStr = Jsoup.clean(html, wl);
                                             ValueStr = ValueStr.replaceAll("&amp;", "&");
                                             ValueStr = ValueStr.replaceAll("&nbsp;", " ");
                                             ValueStr = ValueStr.toString().replaceAll("\\<.*?>", "");
                                             cell.setCellValue(ValueStr);
                                         }
                                     }
                                 } catch (Exception e) {
                                        cell.setCellValue(temp.getString(headers[h]));
                                    }
                            }
                        }
                    }
                }//header for loop
            }//Data for loop
            if (report == 116) { //116:Trial Balance
                cellnum = 0;
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Total");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_open")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDOpenDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodCre), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_d_amount_period")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDPeriodDeb), currencyid, isCurrencyCode, companyid));
                    } else if (headers[h].equals("ytd_c_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDCre), currencyid, isCurrencyCode));
                    } else if (headers[h].equals("ytd_d_amount")) {
                        cell.setCellValue(currencyRender(String.valueOf(totalYTDDeb), currencyid, isCurrencyCode, companyid));
                    }
                }
            } else if (report == 4 || report == 8) {//4: Payment Received; 8: Make Payment
                cellnum = 0;
                XSSFRow row1 = sheet.createRow(rownum++);
                XSSFRow row2 = sheet.createRow(rownum++);
                XSSFRow row = sheet.createRow(rownum++);
                cell = row.createCell(cellnum++);
                cell.setCellValue("Grand Total (In Base Currency)");
                for (int h = 1; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (headers[h].equals("amountinbase")) {
                        String totalAmount = currencyRender(String.valueOf(grandTotalInBaseCurrency), currencyid, isCurrencyCode, companyid);
                        cell.setCellValue(totalAmount);
                    }
                }
            }
            
            if (isFromDocumentDesigner) {
                sheet.enableLocking();
            }
            
//            String fileName = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
//            if (!StringUtil.isNullOrEmpty(fileName)) {
//                fileName = URLDecoder.decode(fileName, "ISO-8859-1");
//            }
//            System.out.println(fileName + ".xls written successfully.");
//            response.setContentType("application/vnd.ms-excel");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xls\"");
  //          wb.write(response.getOutputStream());//To write Excel sheet data into Response object
//            response.getOutputStream().close();
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
           
        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (IOException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } */catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
         return wb;
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
            int unitPriceDecimal;
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
    public String htmlCurrencyRender(String currency, String currencyid, boolean isCurrencyCode) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        String symbol = "";
        try {
            if (isCurrencyCode && currencyid.equals("1")) {
                symbol = cur.getCurrencyCode();
            } else {
                symbol = cur.getSymbol();
            }
            DecimalFormat decimalFormat = null;
            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.");
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>" + symbol + " " + fmt + "</label>)";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String htmlCurrencyRender(String currency, String currencyid, boolean isCurrencyCode,String companyid) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        String symbol = "";
        try {
            if (isCurrencyCode && currencyid.equals("1")) {
                symbol = cur.getCurrencyCode();
            } else {
                symbol = cur.getSymbol();
            }
            DecimalFormat decimalFormat = null;
            String formatstring = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>" + symbol + " " + fmt + "</label>)";
            } else {
                fmt = decimalFormat.format(amt);
                fmt = symbol + " " + fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String htmlPeriodRender(String frommonth) throws SessionExpiredException {
        String fmt = "";
        try {
            String[] m_names = {"January", "February", "March",
                "April", "May", "June", "July", "August", "September",
                "October", "November", "December"};

            Date date = new Date(frommonth);
            Calendar cal=Calendar.getInstance();
            cal.setTime(date);
            fmt = m_names[date.getMonth()] + "-" + cal.get(Calendar.YEAR);
        } catch (Exception ex) {
        }
        return fmt;
    }

    public String htmlCurrencyUnitPriceRender(String currency, String currencyid, boolean isCurrencyCode, String companyid) throws SessionExpiredException {//to show purchase price and sales price in comma separated
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        String symbol = "";
        try {
            
            if (isCurrencyCode && currencyid.equals("1")) {
                symbol = cur.getCurrencyCode();
            } else {
                symbol = cur.getSymbol();
            }
            DecimalFormat decimalFormat = null;
            String formatstring = authHandler.getCompleteDFStringwithDigitNumber("#,##0.", companyid);
            decimalFormat = new DecimalFormat(formatstring);
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if (amt < 0) {
                amt = amt * -1;
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>" + symbol + " " + fmt + "</label>)";
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
    public String withoutHtmlCurrencyRender(String currency) throws SessionExpiredException {

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
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>" + fmt + "</label>)";
            } else {
                fmt = decimalFormat.format(amt);
//                fmt = symbol +" "+ fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }
    
    public String withoutHtmlCurrencyRender(String currency, String companyid) throws SessionExpiredException {

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
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>" + fmt + "</label>)";
            } else {
                fmt = decimalFormat.format(amt);
//                fmt = symbol +" "+ fmt;
            }
        } catch (Exception ex) {
            fmt = currency;
        }
        return fmt;
    }

    public void setHeaderFooter(Document doc, String headerText) {
        HeaderFooter footer = new HeaderFooter(new Phrase("  ", FontFactory.getFont("Helvetica", 8, Font.NORMAL, Color.BLACK)), true);
        footer.setBorderWidth(0);
        footer.setBorderWidthTop(1);
        footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
        doc.setFooter(footer);
        HeaderFooter header = new HeaderFooter(new Phrase(headerText, FontFactory.getFont("Helvetica", 14, Font.BOLD, Color.BLACK)), false);
        doc.setHeader(header);
    }
    public HSSFCell writeFilterDetailsInExcel(HttpServletRequest request, HSSFSheet sheet, HSSFCell cell, int rownum, int cellnum, JSONObject jSONObj,HSSFWorkbook wb) throws SessionExpiredException, ServiceException, JSONException,ParseException {
        JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
        cell = writeFilterDetailsInExcel(requestJobj,sheet,cell,rownum,cellnum, jSONObj,wb);
        return cell;
    }
    
    public HSSFCellStyle getCommaSepratedAmountStyle(HSSFWorkbook wb, String companyid) {
        HSSFCellStyle cellStyleAmount = wb.createCellStyle();
        String str = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
        cellStyleAmount.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(str));
        return cellStyleAmount;
    }
    
    public HSSFCell writeFilterDetailsInExcel(JSONObject requestJobj, HSSFSheet sheet, HSSFCell cell,int rownum,int cellnum, JSONObject jSONObj,HSSFWorkbook wb) throws SessionExpiredException, ServiceException,JSONException, ParseException {
        String companyID = requestJobj.optString(Constants.companyKey);
        String moduleNo = "", reportIdNo = "", getNo = "", isBankBook = "", bankBookSumarryReport = "";
        int module1 = 0, reportId = 0, get = 0;
        moduleNo = requestJobj.optString("moduleId");
        module1 = requestJobj.optInt("moduleId");
        reportIdNo = requestJobj.optString("reportId");
        reportId = requestJobj.optInt("reportId");
        getNo = requestJobj.optString("get");
        get = requestJobj.optInt("get");
        isBankBook = requestJobj.optString("isBankBook");
        bankBookSumarryReport = requestJobj.optString("bankBookSumarryReport");
        boolean dimensionBasedMonthlyBS = requestJobj.optBoolean("dimensionBasedMonthlyBS", false);
        boolean dimensionBasedMonthlyPL = requestJobj.optBoolean("dimensionBasedMonthlyPL", false);
        boolean monthlyBS = requestJobj.optBoolean("monthlyBS", false);
        boolean isMonthlyPNL = requestJobj.optBoolean("isMonthlyReport", false);
        boolean isAged=requestJobj.optBoolean("isAged",false);
        SimpleDateFormat df = new SimpleDateFormat(Constants.ddMMyyyy);
        
        HSSFCellStyle style = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        
        //Create new cell for Company Name
        String companyName = requestJobj.optString(Constants.companyname);
        HSSFRow compnayHeaderRow = sheet.createRow(rownum++);
        cell = compnayHeaderRow.createCell(cellnum++);
        cell.setCellValue("Company Name");
        if (get == Constants.GstTapReturnDetailedView) {
            cell.setCellStyle(style);
        }
        cell = compnayHeaderRow.createCell(cellnum++);
        //SDP-4889
           //       if (get == Constants.DimensionBasedProfitLossReport ||get == Constants.DimensionBasedBalanceSheetReport ||get==Constants.GstTapReturnDetailedView ||get==Constants.GstReport) {
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), requestJobj.optString(Constants.companyKey));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity()) {
                    String searchjson = requestJobj.optString(Constants.Acc_Search_Json);
                    boolean ismultientity=false;
                    boolean ismultientityfieldpresent=false;
                    StringBuilder appendimensionString = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
                        //try {
                            searchjson=StringUtil.DecodeText(requestJobj.optString(Constants.Acc_Search_Json));
                        /*} catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }*/
                        JSONObject json = new JSONObject(searchjson);
                        if (json.has("root")) {
                            JSONArray advSearch = json.getJSONArray("root");
//                            if(advSearch.length()==1){
                            for (int i = 0; i < advSearch.length(); i++) {
                                JSONObject dimensionjson = advSearch.getJSONObject(i);
                                ismultientity= dimensionjson.optBoolean(Constants.isMultiEntity, false);
                                    String searchTextArray[] = dimensionjson.optString("searchText").split(",");
                                    if (searchTextArray.length > 0 && dimensionjson.optInt(Constants.fieldtype,0) == 4 && dimensionjson.optBoolean("iscustomfield",true)==false && ismultientity) {//for dimension only
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
        String reporttitle = requestJobj.optString("filename");
        String filename = requestJobj.optString("filename",null) != null ? requestJobj.optString("filename") : requestJobj.optString("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            try {
                reporttitle = URLDecoder.decode(filename, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                reporttitle = filename;
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String reporttype = requestJobj.optString("filetype");
        if (reporttype.equalsIgnoreCase("detailedXls")) {
            reporttype = "_Detail";
        } else if (reporttype.equalsIgnoreCase("xls")) {
            reporttype = "_Summary";
        } 
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId || get==832) {
            reporttitle += reporttype;
        }
        HSSFRow reportTitleHeaderRow = sheet.createRow(rownum++);
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue("Report Title");
         if (get == Constants.GstTapReturnDetailedView) {
            cell.setCellStyle(style);
        }
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue(reporttitle);

        
        //Create new cell for Export Date
        cellnum = 0;
        String exportdate=null;
        if (isAged) {   //format exportDate in dd/MM/yyyy format for Aginig reports
            exportdate = df.format(new Date());   
        } else {
            exportdate = authHandler.getDateOnlyFormatter(requestJobj).format(new Date()); 
        }
        HSSFRow exportDateHeaderRow = sheet.createRow(rownum++);
        cell = exportDateHeaderRow.createCell(cellnum++);
         if (get == Constants.GstTapReturnDetailedView) {
            cell.setCellStyle(style);
        }
        cell.setCellValue("Export Date");
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(exportdate);
      
        
        boolean isDailySalesReport =requestJobj.optBoolean("isDailySalesReport",false);
//        if(!StringUtil.isNullOrEmpty(request.getParameter("isDailySalesReport"))){
//            isDailySalesReport = Boolean.parseBoolean(request.getParameter("isDailySalesReport"));
//        }
        
        if(isDailySalesReport){
            String dateRange = requestJobj.optString("dateRange");
            cellnum = 0;
            HSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue("For Month");
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue(dateRange);
        }else{
            //Create new cell for From Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get != 1119 && get != 1111 && get != 60 && get != 1135 && get != 1139 && get!=1222 && get!=1223 && get!=Constants.GSTR2A_Match_And_Reconcile_Report && get != Constants.AGED_RECEIVABLES_SUMMARY && get != Constants.AGED_RECEIVABLES_DETAILS && get != Constants.AGED_PAYABLES_DETAILS && get!= Constants.AGED_PAYABLES_SUMMARY && get!=Constants.ACC_FIXED_ASSET_GENERATE_DEPRECIATION) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band,1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String startdate = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("startDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("stdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("fromDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("frmDate"))) {
                    if (module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId && !bankBookSumarryReport.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false")) {
                        if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport || get == 135) {
                            startdate = requestJobj.optString("startDate");
                        } else if (get==225 || get == 27 || get == 66 || get == 67 || get == 116 || get == 125 || get == 126 || get == 215 || get == 807 || get == 555 || get == 911 || get == 916 || get==802) { // 27 = Cost Center Report, 66/67=Customer/Vendor Legder Report,116=Trial Balance,125=Monthly Sales By Product Report,215=Inventory Movement Details Report,555=Stock Report,126=Stock Status report,911=Tax Report,916=GST Report
                            if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) && get == 27) {
                                startdate = requestJobj.optString("startdate");
                            } else {
                                startdate = requestJobj.optString("stdate");
                            }
                        } else if (get == 913 || get == 914 || get == 823 || get == 226) { // 913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report,226=Material IN OUT Register
                            startdate = requestJobj.optString("fromDate");
                        } else if (get == 236 || get == 805 || get == Constants.GOODS_PENDING_ORDERS || get == Constants.FULLFILL_ORDERS_REGISTER || get == Constants.Store_Transfer_History) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report
                            startdate = requestJobj.optString("frmDate");
                        } else if (get == 117 || get == 771 || get ==1221 || get == Constants.DimensionBasedProfitLossReport || get == Constants.DimensionBasedBalanceSheetReport || get == 775) { // 771=Bank Reconciliation, 772=Dimension Based Profit and Loss,773=Dimension Based Balance Sheet, 775=Dimension Based Trial Balance
                            startdate = requestJobj.optString("stdate");
                        } else {
                            if (isAged && (get == 23 || get == 21 || get == 26 || get == 24 || get == 51 || get == 52)) {   //23=Age Payable Summary View,21=Age Paayable Report View,26=Age Receivable Report Summary View,24=Age Receivable Report View,51=SOA-Customer Account Statement,52=SOA-Vendor Account Statement.
                                startdate = df.format(authHandler.getGlobalDateFormat().parse(requestJobj.optString("startdate")));
                            } else {
                                startdate = requestJobj.optString("startdate");
                            }
                        }
                    } else {
                        startdate = requestJobj.optString("stdate");
                    }
                }
                if (get == Constants.GstTapReturnDetailedView && !monthlyBS && !isMonthlyPNL) {
                    startdate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(startdate));
                }
                HSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
                cell = fromDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("From Date");
                if (get == Constants.GstTapReturnDetailedView) {
                    cell.setCellStyle(style);
                }
                cell = fromDateHeaderRow.createCell(cellnum++);
                if(!StringUtil.isNullOrEmpty(startdate)){
                    cell.setCellValue(StringUtil.DecodeText(startdate));
                } else{
                    cell.setCellValue(startdate);
                }
            }
            
            //Create new cell for To Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get!=1119 && get!=1111 && get != 1135 && get != 1139 && get!=1222 && get!=1223 && get != Constants.GSTR2A_Match_And_Reconcile_Report && get!=Constants.ACC_FIXED_ASSET_GENERATE_DEPRECIATION) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band, 1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String enddate = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("startDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("stdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("toDate"))||!StringUtil.isNullOrEmpty(requestJobj.optString("enddate"))) {
                    if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport || get == 135) {
                        enddate = requestJobj.optString("endDate");
                    } else if (get == 236 ||get == 805||get == 913||get == 914||get == 823 ||get == Constants.GOODS_PENDING_ORDERS || get == Constants.FULLFILL_ORDERS_REGISTER || get == Constants.Store_Transfer_History || get == 226 ) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report,913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report ,226=Material IN OUT Register
                        enddate = requestJobj.optString("toDate");
                    } else {
                        enddate = requestJobj.optString("enddate");
                    }
                }
                if (get == Constants.GstTapReturnDetailedView && !monthlyBS && !isMonthlyPNL) {
                    enddate = authHandler.getDateOnlyFormatter(requestJobj).format(authHandler.getGlobalDateFormat().parse(enddate));
                }
                HSSFRow toDateHeaderRow = sheet.createRow(rownum++);
                cell = toDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("To Date");
                if (get == Constants.GstTapReturnDetailedView) {
                    cell.setCellStyle(style);
                }
                cell = toDateHeaderRow.createCell(cellnum++);
                if (!StringUtil.isNullOrEmpty(enddate)) {
                    if (isAged && (get == 23 || get == 21 || get == 26 || get == 24 || get == 51 || get == 52)) {  //23=Age Payable Summary View,21=Age Paayable Report View,26=Age Receivable Report Summary View,24=Age Receivable Report View,51=SOA-Customer Account Statement,52=SOA-Vendor Account Statement.
                        cell.setCellValue(StringUtil.DecodeText(df.format(authHandler.getGlobalDateFormat().parse(enddate))));
                    }else{
                        cell.setCellValue(StringUtil.DecodeText(enddate));
                    }
                } else {
                    cell.setCellValue(enddate);
                }
            }
            //Create new cell for As of Date and Aged On filter
            if (isAged) {
                //As of Date filter
                cellnum = 0;
                String asofdate = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("asofdate"))) {
                    asofdate =df.format(authHandler.getGlobalDateFormat().parse(requestJobj.optString("asofdate")));
                }
                HSSFRow asOfDateHeaderRow = sheet.createRow(rownum++);
                cell = asOfDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("As of Date");
                cell = asOfDateHeaderRow.createCell(cellnum++);
                cell.setCellValue(asofdate);

                //Aged On filter
                cellnum = 0;
                String agedon = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("datefilter"))) {
                    if (requestJobj.optInt("datefilter") == Constants.agedDueDate1to30Filter) {
                        agedon = Constants.agedDueDate1to30Days;
                    } else if (requestJobj.optInt("datefilter") == Constants.agedInvoiceDateFilter) {
                        agedon = Constants.agedInvoiceDate;
                    }else if (requestJobj.optInt("datefilter") == Constants.agedInvoiceDate0to30Filter) {
                        agedon = Constants.agedInvoiceDate0to30;
                    } else {
                        agedon = Constants.agedDueDate0to30Days;
                    }
                }
                HSSFRow agedOnHeaderRow = sheet.createRow(rownum++);
                cell = agedOnHeaderRow.createCell(cellnum++);
                cell.setCellValue("Aged On");
                cell = agedOnHeaderRow.createCell(cellnum++);
                cell.setCellValue(agedon);

            }
            
        }
        if (get == Constants.ActualVsBudgetReportNo) {
            String cogaAccountName = requestJobj.optString("cogaAccountName");
            if (!StringUtil.isNullOrEmpty(cogaAccountName)) {
                cellnum = 0;
                HSSFRow accountName = sheet.createRow(rownum++);
                cell = accountName.createCell(cellnum++);
                cell.setCellValue("COGS Account");
                cell = accountName.createCell(cellnum++);
                cell.setCellValue(cogaAccountName);
            }
        }
         /**
         * Added Entity Name in Sheet
         * ERP-34338
         */
        if(get==Constants.GSTR3B_Summary_Report || get == Constants.GSTR3B_DETAIL_REPORT || get == Constants.GSTRComputationDetailReport){
              cellnum = 0;
              String entityName = requestJobj.optString(Constants.entity, "");
              HSSFRow entityHeaderRow = sheet.createRow(rownum++);
              cell = entityHeaderRow.createCell(cellnum++);
              cell.setCellValue(Constants.ENTITY_NAME_EXPORT);
              cell = entityHeaderRow.createCell(cellnum++);
              cell.setCellValue(entityName);
        }
        //Create new cell for Quick Search
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            cellnum = 0;
            String ss = requestJobj.optString("ss");
            HSSFRow ssHeaderRow = sheet.createRow(rownum++);
            cell = ssHeaderRow.createCell(cellnum++);
            cell.setCellValue("Quick Search String");
            cell = ssHeaderRow.createCell(cellnum++);
            cell.setCellValue(ss);
        }

        //Create new cell for Cost Center
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId) {
            if (!isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String costCenterId = requestJobj.optString("costCenterId");
                String costCenterName = "";
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    costCenterName = getCostCenterNameByID(costCenterId, companyID);
                } else {
                    costCenterName = "All Records";
                }
                HSSFRow costCenterHeaderRow = sheet.createRow(rownum++);
                cell = costCenterHeaderRow.createCell(cellnum++);
                cell.setCellValue("Cost Center");
                cell = costCenterHeaderRow.createCell(cellnum++);
                cell.setCellValue(costCenterName);
            }
        }

        //Create new cell for Product
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            if (module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String ProductId = requestJobj.optString("productid");
                String productName = "";
                if (!StringUtil.isNullOrEmpty(ProductId)) {
                    List product = accCostCenterObj.getProductName(ProductId);
                    productName = (String) product.get(0);
                } else {
                    productName = "All Records";
                }
                HSSFRow productHeaderRow = sheet.createRow(rownum++);
                cell = productHeaderRow.createCell(cellnum++);
                cell.setCellValue("Product");
                cell = productHeaderRow.createCell(cellnum++);
                cell.setCellValue(productName);
            }
        }

        //Create new cell for Product Category
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            if (module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String productCategoryId = requestJobj.optString("productCategoryid");
                String productCategoryName = "";
                if (!StringUtil.isNullOrEmpty(productCategoryId)) {
                    productCategoryName = getProductCategoryNameByID(productCategoryId, companyID);
                } else {
                    productCategoryName = "All Records";
                }
                HSSFRow productCategoryHeaderRow = sheet.createRow(rownum++);
                cell = productCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue("Product Category");
                cell = productCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue(productCategoryName);
            }
        }

        //Create new cell for Customer/Vendor
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId) {
            cellnum = 0;
            String customerName = "";
            if (module1 != Constants.Acc_Sales_Return_ModuleId && module1 != Constants.Acc_Purchase_Return_ModuleId && module1 != Constants.Acc_Purchase_Requisition_ModuleId && module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("newcustomerid")) || !StringUtil.isNullOrEmpty(requestJobj.optString("newvendorid"))) {
                    List returnList = new ArrayList();
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyID);
                    if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId) {
                        requestParams.put("module", "customer");
                        requestParams.put("customerID", requestJobj.optString("newcustomerid"));
                    } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId) {
                        requestParams.put("module", "vendor");
                        requestParams.put("customerID", requestJobj.optString("newvendorid"));
                    }
                    returnList = accCostCenterObj.getCustomerNameByID(requestParams);
                    Object[] obj1 = (Object[]) returnList.get(0);
                    customerName = obj1[0].toString();
                } else {
                    customerName = "All Records";
                }
                HSSFRow customerHeaderRow = sheet.createRow(rownum++);
                cell = customerHeaderRow.createCell(cellnum++);
                if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId) {
                    cell.setCellValue("Customer");
                } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId) {
                    cell.setCellValue("Vendor");
                }
                cell = customerHeaderRow.createCell(cellnum++);
                cell.setCellValue(customerName);
            }
        }

        //Create new cell for Customer/Vendor Category
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId) {
            if (module1 != Constants.Acc_Purchase_Requisition_ModuleId && module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String customerCategoryId = requestJobj.optString("customerCategoryid");
                String customerCategoryName = "";
                if (!StringUtil.isNullOrEmpty(customerCategoryId)) {
                    customerCategoryName = getProductCategoryNameByID(customerCategoryId, companyID);
                } else {
                    customerCategoryName = "All Records";
                }
                HSSFRow customerCategoryHeaderRow = sheet.createRow(rownum++);
                cell = customerCategoryHeaderRow.createCell(cellnum++);
                if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId) {
                    cell.setCellValue("Customer Category");
                } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId) {
                    cell.setCellValue("Vendor Category");
                }
                cell = customerCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue(customerCategoryName);
            }
        }

        //Create new cell for Payment Term
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId) {
            cellnum = 0;
            String termid = requestJobj.optString("termid");
            String termName = "";
            if (!StringUtil.isNullOrEmpty(termid)) {
                List terms = accCostCenterObj.getPaymentTermNameByID(termid, companyID);
                termName = (String) terms.get(0);
            } else {
                termName = "All Records";
            }
            HSSFRow termHeaderRow = sheet.createRow(rownum++);
            cell = termHeaderRow.createCell(cellnum++);
            cell.setCellValue("Payment Term");
            cell = termHeaderRow.createCell(cellnum++);
            cell.setCellValue(termName);
        }

        //Create new cell for Credit/Debit Note Type
        if (module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId) {
            cellnum = 0;
            String cndntypeid = requestJobj.optString("cntype");
            String cndnTypeName = "";
            if (!StringUtil.isNullOrEmpty(cndntypeid)) {
                if (module1 == Constants.Acc_Credit_Note_ModuleId) {
                    if (cndntypeid.equalsIgnoreCase("1")) {
                        cndnTypeName = "Credit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("4")) {
                        cndnTypeName = "Credit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("10")) {
                        cndnTypeName = "Opening Credit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("11")) {
                        cndnTypeName = "Opening Credit Note for Vendors";
                    }
                } else if (module1 == Constants.Acc_Debit_Note_ModuleId) {
                    if (cndntypeid.equalsIgnoreCase("1")) {
                        cndnTypeName = "Debit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("4")) {
                        cndnTypeName = "Debit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("10")) {
                        cndnTypeName = "Opening Debit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("11")) {
                        cndnTypeName = "Opening Debit Note for Customers";
                    }
                }
            }
            HSSFRow cndntypeHeaderRow = sheet.createRow(rownum++);
            cell = cndntypeHeaderRow.createCell(cellnum++);
            if (module1 == Constants.Acc_Credit_Note_ModuleId) {
                cell.setCellValue("Credit Note Type");
            } else if (module1 == Constants.Acc_Debit_Note_ModuleId) {
                cell.setCellValue("Debit Note Type");
            }
            cell = cndntypeHeaderRow.createCell(cellnum++);
            cell.setCellValue(cndnTypeName);
        }

        //Create new cell for Cash/Bank Account Name
        if (isBankBook.equalsIgnoreCase("true") || isBankBook.equalsIgnoreCase("false")) {
            cellnum = 0;
            String accountid = requestJobj.optString("accountid");
            String[] accountsArr = accountid.split(",");
            String accountName = "";
            for (int accCount = 0; accCount < accountsArr.length; accCount++) {
                if(accountsArr[accCount].equalsIgnoreCase("All")){
                   accountName += "All";
                }else if (!StringUtil.isNullOrEmpty(accountsArr[accCount])) {
                    List accounts = accCostCenterObj.getAccountNameByID(companyID, accountsArr[accCount]);
                    accountName += (String) accounts.get(0) + ", ";
             }
            }
            if(!accountName.equalsIgnoreCase("All")&&accountName.length() > 0){
                accountName = accountName.substring(0, accountName.length()-2);
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
        String searchjson = requestJobj.optString(Constants.Acc_Search_Json);
        if(dimensionBasedMonthlyBS || dimensionBasedMonthlyPL){
            if(jSONObj.has("dimBasedSearchJson") && jSONObj.get("dimBasedSearchJson")!= null) {
                searchjson = jSONObj.get("dimBasedSearchJson").toString();
            }
        }
        if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
            //try {
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
           /* } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        //Create new cell for BlankRow
        cellnum = 0;
        HSSFRow BlankRow = sheet.createRow(rownum++);
        cell = BlankRow.createCell(cellnum++);
        cell.setCellValue("");
        
        return cell;
    }
    
    public XSSFCell writeFilterDetailsInExcelXLSX(HttpServletRequest request, XSSFSheet sheet, XSSFCell cell, int rownum, int cellnum, JSONObject jSONObj) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
        cell = writeFilterDetailsInExcelXLSX(requestJobj,sheet,cell,rownum,cellnum, jSONObj);
        return cell;
    }
    
        
    
    public XSSFCell writeFilterDetailsInExcelXLSX(JSONObject requestJobj, XSSFSheet sheet, XSSFCell cell,int rownum,int cellnum, JSONObject jSONObj) throws SessionExpiredException, ServiceException,JSONException {
        String companyID = requestJobj.optString(Constants.companyKey);
        String userdateformat=requestJobj.optString(Constants.userdateformat);
        SimpleDateFormat userdf=new SimpleDateFormat(Constants.MMMMddyyyy);
        if(userdateformat!=null){
        userdf = new SimpleDateFormat(userdateformat);
        }
        String moduleNo = "", reportIdNo = "", getNo = "", isBankBook = "", bankBookSumarryReport = "";
        int module1 = 0, reportId = 0, get = 0;
        moduleNo = requestJobj.optString("moduleId");
        module1 = requestJobj.optInt("moduleId");
        reportIdNo = requestJobj.optString("reportId");
        reportId = requestJobj.optInt("reportId");
        getNo = requestJobj.optString("get");
        get = requestJobj.optInt("get");
        isBankBook = requestJobj.optString("isBankBook");
        bankBookSumarryReport = requestJobj.optString("bankBookSumarryReport");
        boolean dimensionBasedMonthlyBS = requestJobj.optBoolean("dimensionBasedMonthlyBS", false);
        boolean dimensionBasedMonthlyPL = requestJobj.optBoolean("dimensionBasedMonthlyPL", false);
        
        //Create new cell for Company Name
        String companyName = requestJobj.optString(Constants.companyname);
        XSSFRow compnayHeaderRow = sheet.createRow(rownum++);
        cell = compnayHeaderRow.createCell(cellnum++);
        cell.setCellValue("Company Name");
        cell = compnayHeaderRow.createCell(cellnum++);
        //SDP-4889
           //       if (get == Constants.DimensionBasedProfitLossReport ||get == Constants.DimensionBasedBalanceSheetReport ||get==Constants.GstTapReturnDetailedView ||get==Constants.GstReport) {
            ExtraCompanyPreferences extrapref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), requestJobj.optString(Constants.companyKey));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                if (extrapref != null && extrapref.isIsMultiEntity()) {
                    String searchjson = requestJobj.optString(Constants.Acc_Search_Json);
                    boolean ismultientity=false;
                    boolean ismultientityfieldpresent=false;
                    StringBuilder appendimensionString = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
                       // try {
                            searchjson=StringUtil.DecodeText(requestJobj.optString(Constants.Acc_Search_Json));
                       /* } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }*/
                        JSONObject json = new JSONObject(searchjson);
                        if (json.has("root")) {
                            JSONArray advSearch = json.getJSONArray("root");
//                            if(advSearch.length()==1){
                            for (int i = 0; i < advSearch.length(); i++) {
                                JSONObject dimensionjson = advSearch.getJSONObject(i);
                                ismultientity= dimensionjson.optBoolean(Constants.isMultiEntity, false);
                                    String searchTextArray[] = dimensionjson.optString("searchText").split(",");
                                    if (searchTextArray.length > 0 && dimensionjson.optInt(Constants.fieldtype,0) == 4 && dimensionjson.optBoolean("iscustomfield",true)==false && ismultientity) {//for dimension only
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
        String reporttitle = requestJobj.optString("filename");
        String filename = requestJobj.optString("filename",null) != null ? requestJobj.optString("filename") : requestJobj.optString("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            try {
                reporttitle = URLDecoder.decode(filename, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                reporttitle = filename;
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String reporttype = requestJobj.optString("filetype");
        if (reporttype.equalsIgnoreCase("detailedXls")) {
            reporttype = "_Detail";
        } else if (reporttype.equalsIgnoreCase("xls")) {
            reporttype = "_Summary";
        } 
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId || get==832) {
            reporttitle += reporttype;
        }
        XSSFRow reportTitleHeaderRow = sheet.createRow(rownum++);
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue("Report Title");
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue(reporttitle);
        
        //Create new cell for Export Date
        cellnum = 0;
//        authHandler.getDateFormatterWithouTime(requestJobj).format(new Date())  Time Part is eleminated in case of CHKL only 
        String exportdate = get==Constants.GROUP_DETAIL_REPORT_ID ? userdf.format(new Date()) : authHandler.getDateFormatter(requestJobj).format(new Date());
        XSSFRow exportDateHeaderRow = sheet.createRow(rownum++);
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue("Export Date");
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(exportdate);
        
        boolean isDailySalesReport =requestJobj.optBoolean("isDailySalesReport",false);
//        if(!StringUtil.isNullOrEmpty(request.getParameter("isDailySalesReport"))){
//            isDailySalesReport = Boolean.parseBoolean(request.getParameter("isDailySalesReport"));
//        }
        
        if(isDailySalesReport){
            String dateRange = requestJobj.optString("dateRange");
            cellnum = 0;
            XSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue("For Month");
            cell = fromDateHeaderRow.createCell(cellnum++);
            cell.setCellValue(dateRange);
        }else{
            //Create new cell for From Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get != 1119 && get != 1111 && get != 60 && get != 1135 && get != 1139 && get != Constants.AGED_RECEIVABLES_SUMMARY && get != Constants.AGED_RECEIVABLES_DETAILS && get != Constants.AGED_PAYABLES_DETAILS && get!= Constants.AGED_PAYABLES_SUMMARY) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band,1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String startdate = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("startDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("stdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("fromDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("frmDate"))) {
                    if (module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId && !bankBookSumarryReport.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false")) {
                        if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport) {
                            startdate = requestJobj.optString("startDate");
                        } else if (get == 27 || get == 66 || get == 67 || get == 116 || get == 125 || get == 126 || get == 215 || get == 807 || get == 555 || get == 911 || get == 916) { // 27 = Cost Center Report, 66/67=Customer/Vendor Legder Report,116=Trial Balance,125=Monthly Sales By Product Report,215=Inventory Movement Details Report,555=Stock Report,126=Stock Status report,911=Tax Report,916=GST Report
                            if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) && get == 27) {
                                startdate = requestJobj.optString("startdate");
                            } else {
                                startdate = requestJobj.optString("stdate");
                            }
                        } else if (get == 913 || get == 914 || get == 823) { // 913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report
                            startdate = requestJobj.optString("fromDate");
                        } else if (get == 236 || get == 805) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report
                            startdate = requestJobj.optString("frmDate");
                        } else if (get == 117 || get == 771 || get == Constants.DimensionBasedProfitLossReport || get == Constants.DimensionBasedBalanceSheetReport || get == 775) { // 771=Bank Reconciliation, 772=Dimension Based Profit and Loss,773=Dimension Based Balance Sheet, 775=Dimension Based Trial Balance
                            startdate = requestJobj.optString("stdate");
                        } else {

                            if (get == Constants.GROUP_DETAIL_REPORT_ID) {
                                // Time Part is eleminated in case of CHKL only i.e. get == 1153  ERP-33247
                                try {
                                    Date sDate = authHandler.getDateFormatter(requestJobj).parse(requestJobj.optString("startdate"));
                                    startdate = userdf.format(sDate);
                                } catch (ParseException ex) {
                                    startdate = requestJobj.optString("startdate");
                                    Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else {

                                startdate = requestJobj.optString("startdate");
                            }
                        }
                    } else {
                        startdate = requestJobj.optString("stdate");
                    }
                }
                XSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
                cell = fromDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("From Date");
                cell = fromDateHeaderRow.createCell(cellnum++);
                if(!StringUtil.isNullOrEmpty(startdate)){
                    cell.setCellValue(StringUtil.DecodeText(startdate));
                } else{
                    cell.setCellValue(startdate);
                }
            }
            
            //Create new cell for To Date
            if (get != 60 && get != 112 && get != 113 && get != 114 && get != 198 && get != 829 && get != 833 && get!=1119 && get!=1111 && get != 1135 && get != 1139) { // 60 = Customer Credit Limit Report, 112=COA Report,113=Customer Master,114=Vendor Master,198=Product Master,829=Vendor Product Price List Report,833=Price List-Band, 1119=Annexure 2A,1111=Driver's tracking Report
                cellnum = 0;
                String enddate = "";
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("startdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("startDate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("stdate")) || !StringUtil.isNullOrEmpty(requestJobj.optString("toDate"))) {
                    if (reportId == Constants.customerRegistryReport || reportId == Constants.vendorRegistryReport || reportId == Constants.dayEndCollectionReport) {
                        enddate = requestJobj.optString("endDate");
                    } else if (get == 236 ||get == 805||get == 913||get == 914||get == 823) { // 236=Stock Adjustment Register,805=Stock Request on Loan Report,913/914 = Sales By Item Summary/Detail Report,823=Stock Movement Report
                        enddate = requestJobj.optString("toDate");
                    } else {
                        if (get == Constants.GROUP_DETAIL_REPORT_ID) {
                                // Time Part is eleminated in case of CHKL only i.e. get == 1153  ERP-33247
                                try {
                                    Date eDate = authHandler.getDateFormatter(requestJobj).parse(requestJobj.optString("enddate"));
                                    enddate = userdf.format(eDate);
                                } catch (ParseException ex) {
                                    enddate = requestJobj.optString("enddate");
                                    Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else {

                                enddate = requestJobj.optString("enddate");
                            }
                    }
                }
                XSSFRow toDateHeaderRow = sheet.createRow(rownum++);
                cell = toDateHeaderRow.createCell(cellnum++);
                cell.setCellValue("To Date");
                cell = toDateHeaderRow.createCell(cellnum++);
                if(!StringUtil.isNullOrEmpty(enddate)){
                    cell.setCellValue(StringUtil.DecodeText(enddate));
                } else{
                    cell.setCellValue(enddate);
                }
            }
        }
        
        //Create new cell for Quick Search
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            cellnum = 0;
            String ss = requestJobj.optString("ss");
            XSSFRow ssHeaderRow = sheet.createRow(rownum++);
            cell = ssHeaderRow.createCell(cellnum++);
            cell.setCellValue("Quick Search String");
            cell = ssHeaderRow.createCell(cellnum++);
            cell.setCellValue(ss);
        }

        //Create new cell for Cost Center
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId) {
            if (!isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String costCenterId = requestJobj.optString("costCenterId");
                String costCenterName = "";
                if (!StringUtil.isNullOrEmpty(costCenterId)) {
                    costCenterName = getCostCenterNameByID(costCenterId, companyID);
                } else {
                    costCenterName = "All Records";
                }
                XSSFRow costCenterHeaderRow = sheet.createRow(rownum++);
                cell = costCenterHeaderRow.createCell(cellnum++);
                cell.setCellValue("Cost Center");
                cell = costCenterHeaderRow.createCell(cellnum++);
                cell.setCellValue(costCenterName);
            }
        }

        //Create new cell for Product
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            if (module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String ProductId = requestJobj.optString("productid");
                String productName = "";
                if (!StringUtil.isNullOrEmpty(ProductId)) {
                    List product = accCostCenterObj.getProductName(ProductId);
                    productName = (String) product.get(0);
                } else {
                    productName = "All Records";
                }
                XSSFRow productHeaderRow = sheet.createRow(rownum++);
                cell = productHeaderRow.createCell(cellnum++);
                cell.setCellValue("Product");
                cell = productHeaderRow.createCell(cellnum++);
                cell.setCellValue(productName);
            }
        }

        //Create new cell for Product Category
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Purchase_Requisition_ModuleId) {
            if (module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String productCategoryId = requestJobj.optString("productCategoryid");
                String productCategoryName = "";
                if (!StringUtil.isNullOrEmpty(productCategoryId)) {
                    productCategoryName = getProductCategoryNameByID(productCategoryId, companyID);
                } else {
                    productCategoryName = "All Records";
                }
                XSSFRow productCategoryHeaderRow = sheet.createRow(rownum++);
                cell = productCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue("Product Category");
                cell = productCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue(productCategoryName);
            }
        }

        //Create new cell for Customer/Vendor
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId) {
            cellnum = 0;
            String customerName = "";
            if (module1 != Constants.Acc_Sales_Return_ModuleId && module1 != Constants.Acc_Purchase_Return_ModuleId && module1 != Constants.Acc_Purchase_Requisition_ModuleId && module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                if (!StringUtil.isNullOrEmpty(requestJobj.optString("newcustomerid")) || !StringUtil.isNullOrEmpty(requestJobj.optString("newvendorid"))) {
                    List returnList = new ArrayList();
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyID);
                    if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId) {
                        requestParams.put("module", "customer");
                        requestParams.put("customerID", requestJobj.optString("newcustomerid"));
                    } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId) {
                        requestParams.put("module", "vendor");
                        requestParams.put("customerID", requestJobj.optString("newvendorid"));
                    }
                    returnList = accCostCenterObj.getCustomerNameByID(requestParams);
                    Object[] obj1 = (Object[]) returnList.get(0);
                    customerName = obj1[0].toString();
                } else {
                    customerName = "All Records";
                }
                XSSFRow customerHeaderRow = sheet.createRow(rownum++);
                cell = customerHeaderRow.createCell(cellnum++);
                if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId) {
                    cell.setCellValue("Customer");
                } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId) {
                    cell.setCellValue("Vendor");
                }
                cell = customerHeaderRow.createCell(cellnum++);
                cell.setCellValue(customerName);
            }
        }

        //Create new cell for Customer/Vendor Category
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId || module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId) {
            if (module1 != Constants.Acc_Purchase_Requisition_ModuleId && module1 != Constants.Acc_Credit_Note_ModuleId && module1 != Constants.Acc_Debit_Note_ModuleId && !isBankBook.equalsIgnoreCase("true") && !isBankBook.equalsIgnoreCase("false") && module1 != Constants.Acc_Make_Payment_ModuleId && module1 != Constants.Acc_Receive_Payment_ModuleId) {
                cellnum = 0;
                String customerCategoryId = requestJobj.optString("customerCategoryid");
                String customerCategoryName = "";
                if (!StringUtil.isNullOrEmpty(customerCategoryId)) {
                    customerCategoryName = getProductCategoryNameByID(customerCategoryId, companyID);
                } else {
                    customerCategoryName = "All Records";
                }
                XSSFRow customerCategoryHeaderRow = sheet.createRow(rownum++);
                cell = customerCategoryHeaderRow.createCell(cellnum++);
                if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Sales_Order_ModuleId || module1 == Constants.Acc_Customer_Quotation_ModuleId || module1 == Constants.Acc_Delivery_Order_ModuleId || module1 == Constants.Acc_Sales_Return_ModuleId) {
                    cell.setCellValue("Customer Category");
                } else if (module1 == Constants.Acc_Vendor_Invoice_ModuleId || module1 == Constants.Acc_Purchase_Order_ModuleId || module1 == Constants.Acc_Vendor_Quotation_ModuleId || module1 == Constants.Acc_Goods_Receipt_ModuleId || module1 == Constants.Acc_Purchase_Return_ModuleId) {
                    cell.setCellValue("Vendor Category");
                }
                cell = customerCategoryHeaderRow.createCell(cellnum++);
                cell.setCellValue(customerCategoryName);
            }
        }

        //Create new cell for Payment Term
        if (module1 == Constants.Acc_Invoice_ModuleId || module1 == Constants.Acc_Vendor_Invoice_ModuleId) {
            cellnum = 0;
            String termid = requestJobj.optString("termid");
            String termName = "";
            if (!StringUtil.isNullOrEmpty(termid)) {
                List terms = accCostCenterObj.getPaymentTermNameByID(termid, companyID);
                termName = (String) terms.get(0);
            } else {
                termName = "All Records";
            }
            XSSFRow termHeaderRow = sheet.createRow(rownum++);
            cell = termHeaderRow.createCell(cellnum++);
            cell.setCellValue("Payment Term");
            cell = termHeaderRow.createCell(cellnum++);
            cell.setCellValue(termName);
        }

        //Create new cell for Credit/Debit Note Type
        if (module1 == Constants.Acc_Credit_Note_ModuleId || module1 == Constants.Acc_Debit_Note_ModuleId) {
            cellnum = 0;
            String cndntypeid = requestJobj.optString("cntype");
            String cndnTypeName = "";
            if (!StringUtil.isNullOrEmpty(cndntypeid)) {
                if (module1 == Constants.Acc_Credit_Note_ModuleId) {
                    if (cndntypeid.equalsIgnoreCase("1")) {
                        cndnTypeName = "Credit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("4")) {
                        cndnTypeName = "Credit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("10")) {
                        cndnTypeName = "Opening Credit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("11")) {
                        cndnTypeName = "Opening Credit Note for Vendors";
                    }
                } else if (module1 == Constants.Acc_Debit_Note_ModuleId) {
                    if (cndntypeid.equalsIgnoreCase("1")) {
                        cndnTypeName = "Debit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("4")) {
                        cndnTypeName = "Debit Note for Customers";
                    } else if (cndntypeid.equalsIgnoreCase("10")) {
                        cndnTypeName = "Opening Debit Note for Vendors";
                    } else if (cndntypeid.equalsIgnoreCase("11")) {
                        cndnTypeName = "Opening Debit Note for Customers";
                    }
                }
            }
            XSSFRow cndntypeHeaderRow = sheet.createRow(rownum++);
            cell = cndntypeHeaderRow.createCell(cellnum++);
            if (module1 == Constants.Acc_Credit_Note_ModuleId) {
                cell.setCellValue("Credit Note Type");
            } else if (module1 == Constants.Acc_Debit_Note_ModuleId) {
                cell.setCellValue("Debit Note Type");
            }
            cell = cndntypeHeaderRow.createCell(cellnum++);
            cell.setCellValue(cndnTypeName);
        }

        //Create new cell for Cash/Bank Account Name
        if (isBankBook.equalsIgnoreCase("true") || isBankBook.equalsIgnoreCase("false")) {
            cellnum = 0;
            String accountid = requestJobj.optString("accountid");
            String[] accountsArr = accountid.split(",");
            String accountName = "";
            for (int accCount = 0; accCount < accountsArr.length; accCount++) {
                if(accountsArr[accCount].equalsIgnoreCase("All")){
                   accountName += "All";
                }else if (!StringUtil.isNullOrEmpty(accountsArr[accCount])) {
                    List accounts = accCostCenterObj.getAccountNameByID(companyID, accountsArr[accCount]);
                    accountName += (String) accounts.get(0) + ", ";
             }
            }
            if(!accountName.equalsIgnoreCase("All")&&accountName.length() > 0){
                accountName = accountName.substring(0, accountName.length()-2);
            }
            XSSFRow cndntypeHeaderRow = sheet.createRow(rownum++);
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
        String searchjson = requestJobj.optString(Constants.Acc_Search_Json);
        if(dimensionBasedMonthlyBS || dimensionBasedMonthlyPL){
            if(jSONObj.has("dimBasedSearchJson") && jSONObj.get("dimBasedSearchJson")!= null) {
                searchjson = jSONObj.get("dimBasedSearchJson").toString();
            }
        }
        if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
          //  try {
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
                            XSSFRow termHeaderRow = sheet.createRow(rownum++);
                            cell = termHeaderRow.createCell(cellnum++);
                            cell.setCellValue(header);
                            cell = termHeaderRow.createCell(cellnum++);
                            cell.setCellValue(value);
                        }
                    }

                }//end of root
           /* } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        //Create new cell for BlankRow
        cellnum = 0;
        XSSFRow BlankRow = sheet.createRow(rownum++);
        cell = BlankRow.createCell(cellnum++);
        cell.setCellValue("");
        
        return cell;
    }
    
    public XSSFCell writeFilterDetailsInExcel(HttpServletRequest request, XSSFSheet sheet, XSSFCell cell, int rownum, int cellnum) throws SessionExpiredException, ServiceException, ParseException {
        String moduleNo = "", reportIdNo = "", getNo = "";
        String module1 = "";
        int reportId = 0, get = 0;
        if (!StringUtil.isNullOrEmpty(request.getParameter("moduleId"))) {
            moduleNo = request.getParameter("moduleId");
            module1 = moduleNo;
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
            reportIdNo = request.getParameter("reportId");
            reportId = Integer.parseInt(reportIdNo);
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("get"))) {
            getNo = request.getParameter("get");
            get = Integer.parseInt(getNo);
        }
        //Create new cell for Company Name
        String companyName = sessionHandlerImplObj.getCompanySessionObj().getCompany();
        XSSFRow compnayHeaderRow = sheet.createRow(rownum++);
        cell = compnayHeaderRow.createCell(cellnum++);
        cell.setCellValue("Company Name");
        cell = compnayHeaderRow.createCell(cellnum++);
        cell.setCellValue(companyName);

        //Create new cell for Report Title
        cellnum = 0;
        String reporttitle = request.getParameter("filename");
        String filename = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            try {
                reporttitle = URLDecoder.decode(filename, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                reporttitle = filename;
                Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        XSSFRow reportTitleHeaderRow = sheet.createRow(rownum++);
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue("Report Title");
        cell = reportTitleHeaderRow.createCell(cellnum++);
        cell.setCellValue(reporttitle);

        //Create new cell for Export Date
        cellnum = 0;
        String exportdate = authHandler.getDateOnlyFormat(request).format(new Date());
        XSSFRow exportDateHeaderRow = sheet.createRow(rownum++);
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue("Export Date");
        cell = exportDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(exportdate);
        //From Date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        cellnum = 0;
        String startdate = "";
        if (!StringUtil.isNullOrEmpty(request.getParameter("fromDate"))) {
            startdate = request.getParameter("fromDate");
            Date date1 = dateFormat.parse(startdate.toString());
            startdate = authHandler.getDateOnlyFormat(request).format(date1);
        }
        XSSFRow fromDateHeaderRow = sheet.createRow(rownum++);
        cell = fromDateHeaderRow.createCell(cellnum++);
        cell.setCellValue("From Date");
        cell = fromDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(startdate);
        //To Date
        cellnum = 0;
        String enddate = "";
        if (!StringUtil.isNullOrEmpty(request.getParameter("toDate"))) {
            enddate = request.getParameter("toDate");
            Date date1 = dateFormat.parse(enddate.toString());
            enddate = authHandler.getDateOnlyFormat(request).format(date1);
        }
        XSSFRow toDateHeaderRow = sheet.createRow(rownum++);
        cell = toDateHeaderRow.createCell(cellnum++);
        cell.setCellValue("To Date");
        cell = toDateHeaderRow.createCell(cellnum++);
        cell.setCellValue(enddate);
        //Create new cell for BlankRow
        cellnum = 0;
        XSSFRow BlankRow = sheet.createRow(rownum++);
        cell = BlankRow.createCell(cellnum++);
        cell.setCellValue("");

        return cell;
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
    
 public void editJsonKeyForExcelFile(JSONObject jobj,int moduleID) throws JSONException
 {
     String companyid = jobj.optString("companyid"); 
     if (moduleID == Constants.Acc_Receive_Payment_ModuleId || moduleID == Constants.Acc_Make_Payment_ModuleId) {

         if (jobj.has("type") && !StringUtil.isNullOrEmpty(jobj.optString("type", ""))) {
             int type = jobj.optInt("type");
             boolean isrefund = jobj.optBoolean("isrefund");
             if (type == 4) {
                 jobj.put("amountinbase", "");
                 if (jobj.has("accountname")) {
                     jobj.put("accountnamepay", jobj.optString("accountname", ""));
                     jobj.remove("accountname");
                 }

                 if (jobj.has("taxpercent")) {
                     jobj.put("acctaxpercent", authHandler.formattedAmount(jobj.optDouble("taxpercent", 0.0), companyid));
                     jobj.remove("taxpercent");
                 }
                 if (jobj.has("taxamount")) {
                     jobj.put("taxamountforaccount", authHandler.formattedAmount(jobj.optDouble("taxamount", 0.0), companyid));
                     jobj.remove("taxamount");
                 }
                 if (jobj.has("totalamount")) {
                     jobj.put("accamountpaid", authHandler.formattedAmount(jobj.optDouble("totalamount", 0.0), companyid));
                     jobj.remove("totalamount");
                 }
                 if (jobj.has("debit")) {
                     jobj.put("debit", jobj.optBoolean("debit", false) ? "Debit" : "Credit");
                 }
             } else if (type == 3) {
                 jobj.put("amountinbase", "");
                 if (jobj.has("transectionno")) {
                     jobj.put("debitnote", jobj.optString("transectionno", ""));
                     jobj.remove("transectionno");
                 }

                 if (jobj.has("accountname")) {
                     jobj.put("debitnoteaccountname", jobj.optString("accountname", ""));
                     jobj.remove("accountname");
                 }

                 if (jobj.has("totalamount")) {
                     jobj.put("debitnotetotalamount", authHandler.formattedAmount(jobj.optDouble("totalamount", 0.0), companyid));
                     jobj.remove("totalamount");
                 }

                 if (jobj.has("amountdue")) {
                     jobj.put("debitnoteamountdue", authHandler.formattedAmount(jobj.optDouble("amountdue", 0.0), companyid));
                     jobj.remove("amountdue");
                 }

                 if (jobj.has("enteramount")) {
                     jobj.put("debitenteramount", authHandler.formattedAmount(jobj.optDouble("enteramount", 0.0), companyid));
                     jobj.remove("enteramount");
                 }

                 if (jobj.has("creationdate")) {
                     jobj.remove("creationdate");
                 }
             } else if (type == 1) {
                 jobj.put("amountinbase", "");
                 if (!isrefund) {
                     if (jobj.has("accountname")) {
                         jobj.put("advanceccountname", jobj.optString("accountname", ""));
                         jobj.remove("accountname");
                     }

                     if (jobj.has("totalamount")) {
                         jobj.put("advancetotalamount", authHandler.formattedAmount(jobj.optDouble("totalamount", 0.0), companyid));
                         jobj.remove("totalamount");
                     }

                     if (jobj.has("amountdue")) {
                         jobj.put("advanceamountdue", authHandler.formattedAmount(jobj.optDouble("amountdue", 0.0), companyid));
                         jobj.remove("amountdue");
                     }
                 } else {
                     if (jobj.has("accountname")) {
                         jobj.put("fundaccountname", jobj.optString("accountname", ""));
                         jobj.remove("accountname");
                     }

                     if (jobj.has("transectionno")) {
                         jobj.put("fundtransectionno", jobj.optString("transectionno", ""));
                         jobj.remove("transectionno");
                     }

                     if (jobj.has("totalamount")) {
                         jobj.put("fundtotalamount", authHandler.formattedAmount(jobj.optDouble("totalamount", 0.0), companyid));
                         jobj.remove("totalamount");
                     }

                     if (jobj.has("amountdue")) {
                         jobj.put("fundamountdue", authHandler.formattedAmount(jobj.optDouble("amountdue", 0.0), companyid));
                         jobj.remove("amountdue");
                     }

                     if (jobj.has("paidamountOriginal")) {
                         jobj.put("fundpaidamountOriginal", authHandler.formattedAmount(jobj.optDouble("paidamountOriginal", 0.0), companyid));
                         jobj.remove("paidamountOriginal");
                     }

                     if (jobj.has("paidamount")) {
                         jobj.remove("paidamount");
                     }
                 }
             }


         }
     }
     if (jobj.has("billno")) {
         jobj.remove("billno");
     }
     if (jobj.has("taxamount")) {
         jobj.remove("taxamount");
     }
     if (jobj.has("amount")) {
         jobj.remove("amount");
     }
     if (!jobj.has(Constants.amountForExcelFile)) {
         double amountForExcelFile = 0.0;
         double discount = jobj.optDouble("prdiscount") != 0.0 ? (jobj.optBoolean("discountispercent")) ? (jobj.optDouble("rate", 0.0) * (jobj.optDouble("prdiscount") / 100)) : jobj.optDouble("prdiscount", 0.0) : 0.0;
         if (jobj.has("discountvalue")) {
             discount = jobj.optDouble("discountvalue", 0.0);
         }
         /*
          * If quantity is Zero
          */
         if (jobj.optDouble("quantity", 0) == 0) {
             amountForExcelFile = jobj.optDouble("rate", 0.0) - discount + jobj.optDouble("rowTaxAmount", 0.0);
         } else {
             double actualAmount = 0.0;
             if (jobj.optDouble("partamount", 0) != 0.0 && moduleID == Constants.Acc_Invoice_ModuleId) {

                 /* If Invoice is saved with Partial Amount 
                 
                  Then Calculation of Amount is as below
                 
                  Calculated Amount is without discount & Tax amount
                 
                  */
                 actualAmount = (jobj.optDouble("quantity", 0) * jobj.optDouble("rate", 0.0)) * (jobj.optDouble("partamount", 0) / 100);
             } else {
                 actualAmount = (jobj.optDouble("quantity", 0) * jobj.optDouble("rate", 0.0));
             }
             /* Here Amount is calculated with discount & Tax Amount*/
             amountForExcelFile = actualAmount - discount + jobj.optDouble("rowTaxAmount", 0.0);
         }
         jobj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
     }
     if (jobj.has("rate")) {
         jobj.put("rate", authHandler.formattedAmount(jobj.optDouble("rate", 0.0),companyid));
     }
     if (jobj.has("rateIncludingGst")) {
         jobj.put("rateIncludingGst", authHandler.formattedAmount(jobj.optDouble("rateIncludingGst", 0.0), companyid));
     }
     if (jobj.has("quantity")) {
         jobj.put("quantity", authHandler.formattedQuantity(jobj.optDouble("quantity", 0.0), companyid));
     }
     if (jobj.has(Constants.unitpriceForExcelFile)) {
         jobj.put(Constants.unitpriceForExcelFile, authHandler.formattedAmount(jobj.optDouble(Constants.unitpriceForExcelFile, 0.0), companyid));
     }
     if (moduleID == Constants.Acc_Purchase_Order_ModuleId || moduleID == Constants.Acc_Sales_Order_ModuleId) {

         if (jobj.has("balanceQuantity")) {
             jobj.put("balanceQuantity", authHandler.formattedQuantity(jobj.optDouble("balanceQuantity", 0.0), companyid));
         }
     }
     
     if (moduleID == Constants.Acc_Sales_Order_ModuleId) {
         if (jobj.has("shortfallQuantity")) {
             jobj.put("shortfallQuantity", authHandler.formattedQuantity(jobj.optDouble("shortfallQuantity", 0.0), companyid));
         }
     }
     
     if (moduleID == Constants.Acc_Delivery_Order_ModuleId || moduleID == Constants.Acc_Goods_Receipt_ModuleId || moduleID == Constants.Acc_Sales_Return_ModuleId) {
         if (jobj.has("dquantity")) {
             jobj.put("dquantity", authHandler.formattedQuantity(jobj.optDouble("dquantity", 0.0), companyid));
         }
     }
     if (moduleID == Constants.Acc_RFQ_ModuleId) {
         if (jobj.has("memo")) {
             jobj.remove("memo");
         }
     }
     if (jobj.has("rowTaxAmount")) {
         jobj.put("rowTaxAmount", authHandler.formattedAmount(jobj.optDouble("rowTaxAmount", 0.0), companyid));
     }
     if (moduleID == Constants.Acc_Credit_Note_ModuleId) {
         if (jobj.has("invamount")) {
             jobj.put("invamount", authHandler.formattedAmount(jobj.optDouble("invamount", 0.0), companyid));
         }
         if (jobj.has("invamountdue")) {
             jobj.put("invamountdue", authHandler.formattedAmount(jobj.optDouble("invamountdue", 0.0), companyid));
         }
         if (jobj.has("taxpercent")) {
             jobj.put("taxpercent", authHandler.formattedAmount(jobj.optDouble("taxpercent", 0.0), companyid));
         }
         if (jobj.has("taxamountforaccount")) {
             jobj.put("taxamountforaccount", authHandler.formattedAmount(jobj.optDouble("taxamountforaccount", 0.0), companyid));
         }
         if (jobj.has("totalamountforaccount")) {
             jobj.put("totalamountforaccount", authHandler.formattedAmount(jobj.optDouble("totalamountforaccount", 0.0), companyid));
         }
     }
     if (moduleID == Constants.Acc_Invoice_ModuleId || moduleID == Constants.Acc_Goods_Receipt_ModuleId || moduleID == Constants.Acc_Vendor_Invoice_ModuleId) {
         if (jobj.has("amountinbase")) {
             jobj.remove("amountinbase");
         }
     }
     }
    public String getCostCenterNameByID(String costCenterId, String companyID) throws ServiceException {
        String costCenterName = "";
        try {
            if (!StringUtil.isNullOrEmpty(costCenterId) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("id");
                filter_params.add(costCenterId);
                requestParams.put(Constants.filterNamesKey, filter_names);
                requestParams.put(Constants.filterParamsKey, filter_params);

                KwlReturnObject retObj = accCostCenterObj.getCostCenter(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    CostCenter costCenter = (CostCenter) retObj.getEntityList().get(0);
                    costCenterName = costCenter.getName();
}
            }
        } catch (Exception ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return costCenterName;
    }
     public String getProductCategoryNameByID(String productCategoryId, String companyID) throws ServiceException {
        String productCategoryName = "";
        try {
            if (!StringUtil.isNullOrEmpty(productCategoryId) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accCostCenterObj.getProductCategoryNameByID(productCategoryId, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    productCategoryName = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return productCategoryName;
    }
     public void processRequestMVATIndia(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        String filename = request.getParameter("filename") != null ? request.getParameter("filename") : request.getParameter("name");
        if (!StringUtil.isNullOrEmpty(filename)) {
            filename = URLDecoder.decode(filename, "ISO-8859-1");
        }
        String fileType = request.getParameter("filetype");
        JSONObject grid = null;
        String productExportFileName = jobj.optString("productExportFileName");
        int totalProducts = 0;
        boolean isSummaryXls = false;
        boolean isFromForm201ExcelReport =false;
        boolean isDVATForm31 =false;
        boolean isServiceTaxCreditRegister =false;
        try {            
            int report =!StringUtil.isNullOrEmpty(request.getParameter("get")) ? Integer.parseInt(request.getParameter("get")) : 0;
            isFromForm201ExcelReport =!StringUtil.isNullOrEmpty(request.getParameter("isForm201ExcelReport"))? Boolean.parseBoolean(request.getParameter("isForm201ExcelReport")):false;
            isDVATForm31 =!StringUtil.isNullOrEmpty(request.getParameter("DVATForm31"))? Boolean.parseBoolean(request.getParameter("DVATForm31")):false;
            isServiceTaxCreditRegister =!StringUtil.isNullOrEmpty(request.getParameter("ServiceTaxCreditRegister"))? Boolean.parseBoolean(request.getParameter("ServiceTaxCreditRegister")):false;
            JSONArray gridmap=null;
            String colHeader="";
            if (report == 772 || report == 773 || report == 775) {
                gridmap =grid.has("data") ?  grid.getJSONArray("data") : null;
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
            if (StringUtil.equal(fileType, "xls")||StringUtil.equal(fileType, "detailedXls")) {      //Generate Excel Sheet       
                if (StringUtil.equal(fileType, "detailedXls")||isSummaryXls) {
                    String append = StringUtil.equal(fileType, "detailedXls") ? "(Detail)" : "(Summary)";
                    if (!StringUtil.isNullOrEmpty(filename)) {
                        if (filename.indexOf("_v") != -1) {
                            String version = filename.substring(filename.indexOf("_v"), filename.length());
                            filename = filename.substring(0, filename.indexOf("_v"));
                            filename = filename.concat(append).concat(version);
                        } else {
                            filename = filename + append;
                        }
                    }
                }
                byte[] bytes=null;
                HSSFWorkbook workBook=null;
                if(isFromForm201ExcelReport){
                    workBook=createExcelFileForm201India(request, response, jobj,colHeader);
                }else if(isDVATForm31){  // DVAT Form 31
                    workBook=createExcelFileDVATForm31(request, response, jobj,colHeader);
                }else if(isServiceTaxCreditRegister){  // Service Tax Credit Register
                    workBook=createExcelFileServiceTaxCreditRegisterIndia(request, response, jobj,colHeader);
                } else{
                    workBook=createExcelFileMVATIndia(request, response, jobj,colHeader);
                }
                
                if (totalProducts < 1000) {
                    writeXLSDataToFile(filename, fileType, workBook, response);
                } else {
                    createExcelExportFile(workBook, fileType, productExportFileName);
                }
            } else if (StringUtil.equal(fileType, "xlsx")) {      //Generate Excel Sheet
                byte[] bytes=null;
                XSSFWorkbook workBook=null;
                workBook=createExcelXFile(request, response, jobj,colHeader);
                writeXLSXDataToFile(filename, fileType, workBook, response);
            }
        } catch (ServiceException ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. " + errorMsg + "');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    public HSSFWorkbook createExcelFileMVATIndia(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException,ParseException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
        DateFormat frmt = authHandler.getDateOnlyFormat(request);
        DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(request);   //date formatter for Sample file download
        DateFormat dbFormat = authHandler.getDateWithTimeFormat();                          //Database date format
        HSSFWorkbook wb = null;
        HSSFSheet sheet = null;        
        boolean isCurrencyCode = false;
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = "";
        KwlReturnObject result = null;
        boolean isFromDocumentDesigner =false;
        boolean isFromCSTReport =false;
        boolean isForm201C=false,isBorder=false ;
        try {
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImplObj.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            int report =!StringUtil.isNullOrEmpty(request.getParameter("get")) ? Integer.parseInt(request.getParameter("get")) : -1;
            boolean isSampleFile =obj.optString("isSampleFile").equals("T");
            double totalCre = 0, totalDeb = 0,grandTotalInBaseCurrency=0, totalOpenCre = 0, totalOpenDeb = 0, totalPeriodCre = 0, totalPeriodDeb = 0, totalYTDOpenCre = 0, totalYTDOpenDeb = 0, totalYTDPeriodCre = 0,totalYTDPeriodDeb = 0, totalYTDCre = 0, totalYTDDeb = 0, grandTotalAmountWithTaxInBaseCurrency=0;
            boolean excludeCustomHeaders = StringUtil.isNullOrEmpty(request.getParameter("excludeCustomHeaders"))?false:Boolean.parseBoolean(request.getParameter("excludeCustomHeaders"));
            String module =request.getAttribute("moduleId")!=null? (String) request.getAttribute("moduleId"):"0000";
            isFromCSTReport =!StringUtil.isNullOrEmpty(request.getParameter("isFromCSTReport"))? Boolean.parseBoolean(request.getParameter("isFromCSTReport")):false;
            int moduleId = Integer.parseInt(module);
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String head = "";
            isFromDocumentDesigner = obj.optBoolean("isFromDocumentDesigner",false);
            isForm201C = StringUtil.isNullOrEmpty(request.getParameter("isform201C"))?false:Boolean.parseBoolean(request.getParameter("isform201C"));
            isBorder = StringUtil.isNullOrEmpty(request.getParameter("border"))?false:Boolean.parseBoolean(request.getParameter("border"));
            if (request.getParameter("header") != null) {
                head = request.getParameter("header");
                String tit = request.getParameter("title");
                if(report == 772 || report == 773 ||report == 775){
                    tit = colHeader;
                }
                String algn = request.getParameter("align");
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
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
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontName(HSSFFont.FONT_ARIAL);
            font.setFontHeightInPoints((short) 10);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setWrapText(true);
            if(isBorder){
                style.setBorderTop(CellStyle.BORDER_MEDIUM);
                style.setBorderBottom(CellStyle.BORDER_MEDIUM);
                style.setBorderLeft(CellStyle.BORDER_MEDIUM);
                style.setBorderRight(CellStyle.BORDER_MEDIUM); 
            }
            
            HSSFCellStyle leftstyle = wb.createCellStyle();
            leftstyle.setAlignment(CellStyle.ALIGN_LEFT);
            leftstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstyle.setWrapText(true);
            leftstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderRight(CellStyle.BORDER_MEDIUM);

            HSSFCellStyle leftstylebold = wb.createCellStyle();
            leftstylebold.setFont(font);
            leftstylebold.setAlignment(CellStyle.ALIGN_LEFT);
            leftstylebold.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstylebold.setWrapText(true);
            leftstylebold.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            if(isForm201C){
                
                HSSFRow headerRow = sheet.createRow(rownum);
                cellnum = 0;
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue("VAT Tin");
                cell.setCellStyle(leftstylebold);

                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(extraCompanyPreferences.getVatNumber());
                cell.setCellStyle(leftstyle);

                if(obj.has("period") && !StringUtil.isNullOrEmpty(obj.getString("period"))){
                    rownum++;

                    HSSFRow headerRowPeriod = sheet.createRow(rownum);
                    cellnum = 0;
                    cell = headerRowPeriod.createCell(cellnum++);
                    cell.setCellValue("Tax Period");
                    cell.setCellStyle(leftstylebold);

                    cell = headerRowPeriod.createCell(cellnum++);
                    cell.setCellValue(obj.getString("period"));
                    cell.setCellStyle(leftstyle);
                }
                if(obj.has("year") && !StringUtil.isNullOrEmpty(obj.getString("year"))){
                    rownum++;

                    HSSFRow headerRowYear = sheet.createRow(rownum);
                    cellnum = 0;
                    cell = headerRowYear.createCell(cellnum++);
                    cell.setCellValue("Year");
                    cell.setCellStyle(leftstylebold);

                    cell = headerRowYear.createCell(cellnum++);
                    cell.setCellValue(obj.getString("year"));
                    cell.setCellStyle(leftstyle);
                }
                rownum = rownum + 2;
            } else{
                cell = writeFilterDetailsInExcel(request,sheet,cell,rownum,cellnum,obj,wb);
                rownum+=cell.getRow().getRowNum()+1;
            }
            int rowInt=rownum++;
            HSSFRow headerRow = sheet.createRow(rowInt);
            JSONArray repArr = obj.getJSONArray("data");
            JSONArray repArrSummary = obj.getJSONArray("summary");
            companyid = request.getParameter("companyids");
            if (!StringUtil.isNullOrEmpty(companyid) && companyid.contains(storageHandlerImpl.SBICompanyId().toString())) {
                isCurrencyCode = true;
            }
            
            /* 'excludeCustomHeaders' check is added to improve performance while exporting records. 
               Purpose: Insert Custom field headers on server side rather than sending all of them in URL. 
               It will reduce size of URL.*/
            if (excludeCustomHeaders) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(request.getAttribute("companyid"), moduleId));
                result = accCommonTablesDAO.getFieldParamsforSpecificFields(requestParams);

                ArrayList<String> headList = new ArrayList<String>();
                Collections.addAll(headList, headers);
                if (result.getRecordTotalCount() > 0) {
                    List<String> fields = result.getEntityList();
                    Iterator itr = fields.iterator();
                    while (itr.hasNext()) {
                        Object[] oj = (Object[]) itr.next();
                        String label = oj[2].toString();
                        String fieldname = oj[1].toString();
                        if (headList.contains(fieldname)) {
                            int index = headList.indexOf(fieldname);
                            titles[index] = label;
                        }
                    }
                }
                headers = headList.toArray(new String[headList.size()]);
            }
            //Insert Headers
            
            cellnum = 0;
            cell = headerRow.createCell(cellnum++);
            String salescheck=request.getParameter("isSalesAnnax");
            String title="Transactionwise Purchases Details";
            if(!StringUtil.isNullOrEmpty(salescheck) && Boolean.valueOf(salescheck)){
                title="Transactionwise Sales Details";
            }
            if(isFromCSTReport){
                title="Form 6";
            }
            if(isForm201C){
                title="FORM 201C";
            }
            cell.setCellValue(title);
            cell.setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(
                    rowInt++, //first row (0-based)
                    rowInt++, //last row  (0-based)
                    0, //first column (0-based)
                    headers.length-1 //last column  (0-based)
                    ));
            cellnum = 0;
            int startrow=++rownum;
            int lastrow=++rownum;
            headerRow = sheet.createRow(startrow);
            for (int h = 0; h < headers.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                cell = headerRow.createCell(cellnum++);  //Create new cell
                cell.setCellValue(headerStr);
                cell.setCellStyle(style);
               int strtcelnum=cellnum-1;
               if(headerStr.equals(IndiaComplianceConstants.REPORT_NET_RS) || headerStr.equals(IndiaComplianceConstants.REPORT_TAX_IF_ANY)){
                   if (headerStr.equals(IndiaComplianceConstants.REPORT_NET_RS)) {                       
                       cell.setCellValue(IndiaComplianceConstants.REPORT_TAXABLE_VALUE);
                       cell.setCellStyle(style);
                       sheet.addMergedRegion(new CellRangeAddress(
                               startrow, //first row (0-based)
                               startrow, //last row  (0-based)
                               strtcelnum, //first column (0-based)
                               strtcelnum + 1 //last column  (0-based)
                               ));
                   }                                                     
               }
               if(isFromCSTReport){
                    if(headerStr.equals(IndiaComplianceConstants.REPORT_UNDER_CENTRAL_ACT) || headerStr.equals(IndiaComplianceConstants.REPORT_REGISTRATION_CERTIFICATE_NO)){
                        if (headerStr.equals(IndiaComplianceConstants.REPORT_UNDER_CENTRAL_ACT)) {                       
                            cell.setCellValue(IndiaComplianceConstants.REPORT_REGISTRATION_CERTIFICATE_NO);
                            cell.setCellStyle(style);
                            sheet.addMergedRegion(new CellRangeAddress(
                                    startrow, //first row (0-based)
                                    startrow, //last row  (0-based)
                                    strtcelnum, //first column (0-based)
                                    strtcelnum + 1 //last column  (0-based)
                                    ));
                        }                                                     
                    }
               } 
            }
            cellnum=0;
            headerRow = sheet.createRow(lastrow);
            for (int h = 0; h < headers.length; h++) {//headers loop       
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                cell = headerRow.createCell(cellnum++);  //Create new cell
                if(headerStr.equals(IndiaComplianceConstants.REPORT_NET_RS) || headerStr.equals(IndiaComplianceConstants.REPORT_TAX_IF_ANY)){
                cell.setCellValue(headerStr);
                cell.setCellStyle(style);
                }
                if(isFromCSTReport){
                    if(headerStr.equals(IndiaComplianceConstants.REPORT_UNDER_CENTRAL_ACT) || headerStr.equals(IndiaComplianceConstants.REPORT_REGISTRATION_CERTIFICATE_NO)){
                        cell.setCellValue(headerStr);
                        cell.setCellStyle(style);
                    }
                }
            }

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row = null;
                if(isForm201C){
                  row = sheet.createRow(rownum++); 
                } else{
                  row = sheet.createRow(++rownum); 
                }
                JSONObject temp = repArr.getJSONObject(t);
                for (int h = 0; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if(isForm201C){
                        cell.setCellStyle(leftstyle);
                    }
                    if (temp.has(headers[h])) {
                        String tempStr = temp.getString(headers[h]);
                        cell.setCellValue(tempStr);
                    }else{
                        cell.setCellValue("");
                    }
                }
            }//Data for loop            
            
            for(int s = 0; s < repArrSummary.length(); s++){ //Summary(Gross Total)
                cellnum = 0;
                HSSFRow row = sheet.createRow(++rownum);
                JSONObject temp = repArrSummary.getJSONObject(s);
                for (int h = 0; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (temp.has(headers[h])) {
                        String tempStr = temp.getString(headers[h]);
                        cell.setCellValue(tempStr);
                    }else{
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(style);
                }
            }
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
         return wb;
    }
    public HSSFWorkbook createExcelFileForm201India(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        HSSFWorkbook wb = null;
        HSSFSheet sheet = null;        
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = "";
        try {
            String headers[] = null;
            String titles[] = null;
            String subtitles[] = null;
            String align[] = null;
            String head = "";
            
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImplObj.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            boolean isform201B = StringUtil.isNullOrEmpty(request.getParameter("isform201B"))?false:Boolean.parseBoolean(request.getParameter("isform201B"));
            
            if (request.getParameter("header") != null) {
                head = request.getParameter("header");
                String tit = request.getParameter("title");
                String subtit = request.getParameter("subtitle");
                String algn = request.getParameter("align");
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
                    tit = tit;
                }
                headers = (String[]) head.split("~@");
                titles = (String[]) tit.split("~@");
                subtitles = (String[]) subtit.split("~@");
                align = (String[]) algn.split(",");
            } 
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            //Insert Headers
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontName(HSSFFont.FONT_ARIAL);
            font.setFontHeightInPoints((short) 10);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setWrapText(true);
            style.setBorderTop(CellStyle.BORDER_MEDIUM);
            style.setBorderBottom(CellStyle.BORDER_MEDIUM);
            style.setBorderLeft(CellStyle.BORDER_MEDIUM);
            style.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            HSSFCellStyle leftstyle = wb.createCellStyle();
            leftstyle.setAlignment(CellStyle.ALIGN_LEFT);
            leftstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstyle.setWrapText(true);
            leftstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            HSSFCellStyle leftstylebold = wb.createCellStyle();
            leftstylebold.setFont(font);
            leftstylebold.setAlignment(CellStyle.ALIGN_LEFT);
            leftstylebold.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstylebold.setWrapText(true);
            leftstylebold.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            
            int rowInt=++rownum;
            
            HSSFRow headerRow = sheet.createRow(rowInt);
            cellnum = 0;
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("VAT Tin");
            cell.setCellStyle(leftstylebold);
            
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(extraCompanyPreferences.getVatNumber());
            cell.setCellStyle(leftstyle);
            
            if(obj.has("period") && !StringUtil.isNullOrEmpty(obj.getString("period"))){
                rowInt++;

                HSSFRow headerRowPeriod = sheet.createRow(rowInt);
                cellnum = 0;
                cell = headerRowPeriod.createCell(cellnum++);
                cell.setCellValue("Tax Period");
                cell.setCellStyle(leftstylebold);

                cell = headerRowPeriod.createCell(cellnum++);
                cell.setCellValue(obj.getString("period"));
                cell.setCellStyle(leftstyle);
            }
            if(obj.has("year") && !StringUtil.isNullOrEmpty(obj.getString("year"))){
                rowInt++;

                HSSFRow headerRowYear = sheet.createRow(rowInt);
                cellnum = 0;
                cell = headerRowYear.createCell(cellnum++);
                cell.setCellValue("Year");
                cell.setCellStyle(leftstylebold);

                cell = headerRowYear.createCell(cellnum++);
                cell.setCellValue(obj.getString("year"));
                cell.setCellStyle(leftstyle);
            }
            rowInt = rowInt + 2;
            HSSFRow headerRowForm = sheet.createRow(rowInt);
            cellnum = 0;
            cell = headerRowForm.createCell(cellnum++);
            if(isform201B){
                cell.setCellValue("FORM 201B");
            } else{
                cell.setCellValue("FORM 201A");
            }
            cell.setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(
                rowInt++, //first row (0-based)
                rowInt++, //last row  (0-based)
                0, //first column (0-based)
                headers.length - 1 //last column  (0-based)
            ));
            
            HSSFRow headerRowTable = sheet.createRow(rowInt);
            JSONArray repArr = obj.getJSONArray("data");
            companyid = request.getParameter("companyids");
            
            cellnum = 0;
            int startrow=rowInt;
            headerRowTable = sheet.createRow(startrow);
           
            
            for (int h = 0; h < titles.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                
                if(h==2){
                    sheet.addMergedRegion(new CellRangeAddress(
                    startrow, //first row (0-based)
                    startrow, //last row  (0-based)
                    2, //first column (0-based)
                    4 //last column  (0-based)
                    ));
                    cell = headerRowTable.createCell(2);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                }else if(h==3){
                    sheet.addMergedRegion(new CellRangeAddress(
                    startrow, //first row (0-based)
                    startrow, //last row  (0-based)
                    5, //first column (0-based)
                    8 //last column  (0-based)
                    ));
                    cell = headerRowTable.createCell(5);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                }else{
                    cell = headerRowTable.createCell(cellnum++);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                }
                
                
            }
            
            headerRowTable = sheet.createRow(startrow+1);
            cellnum=2;
            for(int i=0; i< cellnum; i++){
                cell = headerRowTable.createCell(i++);  //Create new cell
                cell.setCellValue("");
                cell.setCellStyle(style);
            }
            for (int h = 0; h < subtitles.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(subtitles[h]);
                    cell = headerRowTable.createCell(cellnum++);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
            }
            rowInt++;
            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row = sheet.createRow(++rowInt);
                JSONObject temp = repArr.getJSONObject(t);
                for (int h = 0; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    cell.setCellStyle(leftstyle);
                    if (temp.has(headers[h])) {
                        String tempStr = temp.getString(headers[h]);
                        cell.setCellValue(tempStr);
                    }else{
                        cell.setCellValue("");
                    }
                }
            }//Data for loop            
            
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
         return wb;
    }
    public HSSFWorkbook createExcelFileDVATForm31(HttpServletRequest request, HttpServletResponse response, JSONObject obj, String colHeader) throws ServiceException, SessionExpiredException {
        HSSFWorkbook wb = null;
        HSSFSheet sheet = null;
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        try {
            String headers[] = null;
            String titles[] = null;
            String subtitles[] = null;
            String head = "";
            String startDate = "";
            String endDate = "";
            /*
             * Company Information
             */
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpResult.getEntityList().get(0);
            String companyName = !StringUtil.isNullOrEmpty(company.getCompanyName()) ? company.getCompanyName() : "";
            String companyAddress = !StringUtil.isNullOrEmpty(company.getAddress()) ? company.getAddress() : "";
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPref = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            String vatTIN = !StringUtil.isNullOrEmpty(extraCompanyPref.getVatNumber()) ? extraCompanyPref.getVatNumber() : "";
            DateFormat df = authHandler.getDateOnlyFormat();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            if (!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) {
                Date df1 = df.parse(request.getParameter("stdate"));
                startDate = sdf.format(df1);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                Date df1 = df.parse(request.getParameter("enddate"));
                endDate = sdf.format(df1);
            }
            if (request.getParameter("header") != null) {
                head = request.getParameter("header");
                String tit = request.getParameter("title");
                String subtit = request.getParameter("subtitle");
                try {
                    tit = StringUtil.DecodeText(tit);
                } catch (IllegalArgumentException e) {
                    tit = tit;
                }
                headers = (String[]) head.split("~@");
                titles = (String[]) tit.split("~@");
                subtitles = (String[]) subtit.split("~@");
            }
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            
            //Insert Headers
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontName(HSSFFont.FONT_ARIAL);
            font.setFontHeightInPoints((short) 10);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setWrapText(true);
            style.setBorderTop(CellStyle.BORDER_MEDIUM);
            style.setBorderBottom(CellStyle.BORDER_MEDIUM);
            style.setBorderLeft(CellStyle.BORDER_MEDIUM);
            style.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
            
            HSSFCellStyle leftstyle = wb.createCellStyle();
            leftstyle.setAlignment(CellStyle.ALIGN_LEFT);
            leftstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstyle.setWrapText(true);
            leftstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            HSSFCellStyle rightstyle = wb.createCellStyle();
            rightstyle.setAlignment(CellStyle.ALIGN_RIGHT);
            rightstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            rightstyle.setWrapText(true);
            rightstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            rightstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            rightstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            rightstyle.setBorderRight(CellStyle.BORDER_MEDIUM);

            HSSFCellStyle leftstylebold = wb.createCellStyle();
            leftstylebold.setFont(font);
            leftstylebold.setAlignment(CellStyle.ALIGN_LEFT);
            leftstylebold.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstylebold.setWrapText(true);
            
            HSSFFont font12 = wb.createFont();
            font12.setFontName(HSSFFont.FONT_ARIAL);
            font12.setFontHeightInPoints((short) 12);
            font12.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            
            HSSFCellStyle leftstylebold15 = wb.createCellStyle();
            leftstylebold15.setFont(font12);
            leftstylebold15.setAlignment(CellStyle.ALIGN_LEFT);
            leftstylebold15.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstylebold15.setWrapText(true);

            String currencyid = sessionHandlerImpl.getCurrencyID(request);

            int rowInt = ++rownum;

            HSSFRow row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_DEPARTMENT_VALUE_ADDED_TAX);//Department of Value Added Tax ;
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 6));

            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_GOVEMENT_NCT_DELHI); //Government of NCT of Delhi
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 6));

            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_FORM_DVAT_31); // Form DVAT 31
            cell.setCellStyle(leftstylebold15);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 6));

            rowInt++;
            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_SPECIMEN_OF_SALSE); //"Specimen of Sales / outward Branch Transfer Register"
            cell.setCellStyle(leftstylebold15);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 6));

            rowInt++;
            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_REGISTRATION_NO);  // "Registration Number"
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 2));
            cellnum = 3;
            cell = row.createCell(cellnum++);
            cell.setCellValue(vatTIN);
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 3, 6));


            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_NAME_OF_DEALER); //Name of dealer
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 2));
            cellnum = 3;
            cell = row.createCell(cellnum++);
            cell.setCellValue(companyName);
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 3, 6));

            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_ADDRESS); //"Address"
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 3));
            cellnum = 3;
            cell = row.createCell(cellnum++);
            cell.setCellValue(companyAddress);
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 3, 7));


            rowInt++;
            rowInt++;
            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_SALES_TAX_PERIOD); // "Sales for the tax period"
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 6));


            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 4;
            cell = row.createCell(cellnum++);
            cell.setCellValue("From : "+startDate);
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 4, 5));

            cellnum = 7;
            cell = row.createCell(cellnum++);
            cell.setCellValue("To : "+endDate);
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 7, 8));

            rowInt++;
            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 4;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_CASH_ACCRUAL);  //"Method of accounting: Cash  / Accrual"
            cell.setCellStyle(leftstylebold);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 4, 7));

            rowInt++;
            rowInt++;
            row = sheet.createRow(rowInt);
            cellnum = 0;
            cell = row.createCell(cellnum++);
            cell.setCellValue(IndiaComplianceConstants.DVAT31_DETAILS_OF_SALES); // "Details of Sales"
            cell.setCellStyle(leftstylebold15);
            sheet.addMergedRegion(new CellRangeAddress(rowInt, rowInt, 0, 3));

            rowInt = rowInt + 2;
            HSSFRow headerRowTable = sheet.createRow(rowInt);
            JSONArray repArr = obj.getJSONArray("data");

            cellnum = 0;
            int startrow = rowInt;
            headerRowTable = sheet.createRow(startrow);
            headerRowTable.setHeight((short)(1700)); //Set height to column 
            for (int h = 0; h < titles.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                /*Turnover of Inter-State Sale/Stock Transfer / Export (Deductions)*/
                if (h == 5) {
                    sheet.addMergedRegion(new CellRangeAddress(
                            startrow, //first row (0-based)
                            startrow, //last row  (0-based)
                            5, //first column (0-based)
                            17 //last column  (0-based)
                            ));
                    cell = headerRowTable.createCell(5);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                /*Turnover of Inter-State Sale (Taxable)*/
                } else if (h == 6) {
                    sheet.addMergedRegion(new CellRangeAddress(
                            startrow, //first row (0-based)
                            startrow, //last row  (0-based)
                            18, //first column (0-based)
                            22 //last column  (0-based)
                            ));
                    cell = headerRowTable.createCell(18);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                 /*Turnover of Local Sale*/
                } else if (h == 7) {
                    sheet.addMergedRegion(new CellRangeAddress(
                            startrow, //first row (0-based)
                            startrow, //last row  (0-based)
                            23, //first column (0-based)
                            29 //last column  (0-based)
                            ));
                    cell = headerRowTable.createCell(23);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                } else {
                    cell = headerRowTable.createCell(cellnum++);  //Create new cell
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                }


            }
            headerRowTable = sheet.createRow(startrow + 1);
            cellnum = 5;
            for (int i = 0; i < cellnum; i++) {
                cell = headerRowTable.createCell(i++);  //Create new cell
                cell.setCellValue("");
                cell.setCellStyle(style);
            }
            headerRowTable.setHeight((short)(2050)); //Set height to column 
            for (int h = 0; h < subtitles.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(subtitles[h]);
                cell = headerRowTable.createCell(cellnum++);  //Create new cell
                cell.setCellValue(headerStr);
                cell.setCellStyle(style);
            }
            rowInt++;
            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row1 = sheet.createRow(++rowInt);
                JSONObject temp = repArr.getJSONObject(t);
                for (int h = 0; h < headers.length; h++) {
                    cell = row1.createCell(cellnum++);
                    if (h > 3) {
                        cell.setCellStyle(rightstyle);
                    } else {
                        cell.setCellStyle(leftstyle);
                    }
                    if (temp.has(headers[h])) {
                        String tempStr = temp.getString(headers[h]);
                        cell.setCellValue(tempStr);
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
            //Set width to column           
            for (int g = 0; g < 30; g++) {
                sheet.setColumnWidth(g, 3000);
            }            

        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return wb;
    }
     /**
     * Function returns Entity Names if selected else company name
     *
     * @param searchjson  
     * @param Company  
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public String getEntityDimensionNameforExport(String searchjson, Company company) throws JSONException, ServiceException {
        boolean ismultientity = false;
        String dimensionvalue = "";
        StringBuilder appendimensionString = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")) {
            JSONObject json = new JSONObject(searchjson);
            if (json.has("root")) {
                JSONArray advSearch = json.getJSONArray("root");
                for (int i = 0; i < advSearch.length(); i++) {
                    JSONObject dimensionjson = advSearch.getJSONObject(i);
                    ismultientity = dimensionjson.optBoolean(Constants.isMultiEntity, false);
                    String searchTextArray[] = dimensionjson.optString("searchText").split(",");
                    if (searchTextArray.length > 0 && dimensionjson.optInt(Constants.fieldtype, 0) == 4 && dimensionjson.optBoolean("iscustomfield", true) == false && ismultientity) {//for dimension only
                        for (String searchTextvalue : searchTextArray) {
                            FieldComboData fieldcombodata = null;
                            KwlReturnObject fieldcombodataObj = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), searchTextvalue);
                            if (fieldcombodataObj != null && !fieldcombodataObj.getEntityList().isEmpty() && fieldcombodataObj.getEntityList().get(0) != null) {
                                fieldcombodata = (FieldComboData) fieldcombodataObj.getEntityList().get(0);
                                if (!StringUtil.isNullOrEmpty(fieldcombodata.getItemdescription())) {
                                    appendimensionString.append(fieldcombodata.getItemdescription().concat(","));
                                } else {
                                    appendimensionString.append(fieldcombodata.getValue().concat(","));
                                }
                            }
                        }//end of for of SearchTextValue
                    }//end of searchTextArray
                }//end of for loop of advSearch
            }//end of root
            if (StringUtil.isNullOrEmpty(appendimensionString.toString())) {
                dimensionvalue = company.getCompanyName() != null ? company.getCompanyName() : "";
            } else {
                dimensionvalue = appendimensionString.toString();
                dimensionvalue = dimensionvalue.substring(0, dimensionvalue.length() - 1);
            }
        } else {
            dimensionvalue = company.getCompanyName() != null ? company.getCompanyName() : "";
        }
        return dimensionvalue;
    }
    
    public HSSFWorkbook createExcelFileServiceTaxCreditRegisterIndia(HttpServletRequest request, HttpServletResponse response, JSONObject obj,String colHeader) throws ServiceException, SessionExpiredException {
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
        DateFormat frmt = authHandler.getDateOnlyFormat(request);
        DateFormat userFormat = authHandler.getUserDateFormatterWithoutTimeZone(request);   //date formatter for Sample file download
        DateFormat dbFormat = authHandler.getDateWithTimeFormat();                          //Database date format
        HSSFWorkbook wb = null;
        HSSFSheet sheet = null;        
        HSSFCell cell = null;
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        String companyid = "";
        boolean isBorder=false ;
        try {
            KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImplObj.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);
            int report =!StringUtil.isNullOrEmpty(request.getParameter("get")) ? Integer.parseInt(request.getParameter("get")) : -1;
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String head = "";
            isBorder = StringUtil.isNullOrEmpty(request.getParameter("border"))?false:Boolean.parseBoolean(request.getParameter("border"));
            if (request.getParameter("header") != null) {
                head = request.getParameter("header");
                String tit = request.getParameter("title");
                if(report == 772 || report == 773 ||report == 775){
                    tit = colHeader;
                }
                String algn = request.getParameter("align");
                try{
                    tit = StringUtil.DecodeText(tit);
                } catch(IllegalArgumentException e){    //ERP-22395
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
            wb = new HSSFWorkbook();
            sheet = wb.createSheet("Sheet-1");
            
            HSSFCellStyle style = wb.createCellStyle();
            HSSFFont font = wb.createFont();
            font.setFontName(HSSFFont.FONT_ARIAL);
            font.setFontHeightInPoints((short) 10);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setWrapText(true);
            if(isBorder){
                style.setBorderTop(CellStyle.BORDER_MEDIUM);
                style.setBorderBottom(CellStyle.BORDER_MEDIUM);
                style.setBorderLeft(CellStyle.BORDER_MEDIUM);
                style.setBorderRight(CellStyle.BORDER_MEDIUM); 
            }
            
            HSSFCellStyle leftstyle = wb.createCellStyle();
            leftstyle.setAlignment(CellStyle.ALIGN_LEFT);
            leftstyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstyle.setWrapText(true);
            leftstyle.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstyle.setBorderRight(CellStyle.BORDER_MEDIUM);

            HSSFCellStyle leftstylebold = wb.createCellStyle();
            leftstylebold.setFont(font);
            leftstylebold.setAlignment(CellStyle.ALIGN_LEFT);
            leftstylebold.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            leftstylebold.setWrapText(true);
            leftstylebold.setBorderTop(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderBottom(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderLeft(CellStyle.BORDER_MEDIUM);
            leftstylebold.setBorderRight(CellStyle.BORDER_MEDIUM);
            
            rownum++;
            HSSFRow headerRow = sheet.createRow(rownum);
            JSONArray repArr = obj.getJSONArray("data");
            JSONArray leftHeaderArr = obj.getJSONArray("leftheaderarr");
            JSONArray leftHeaderDatarArr = obj.getJSONArray("leftheaderdataarr");
            JSONArray rightHeaderArr = obj.getJSONArray("rightheaderarr");
            JSONArray rightHeaderDatarArr = obj.getJSONArray("rightheaderdataarr");
            //Insert Headers
            
            cellnum = 0;
            cell = headerRow.createCell(cellnum++);
            String title="REGISTER FOR INPUT SERVICE CREDIT";
            cell.setCellValue(title);
            cell.setCellStyle(style);
            sheet.addMergedRegion(new CellRangeAddress(
                    rownum, //first row (0-based)
                    rownum, //last row  (0-based)
                    0, //first column (0-based)
                    headers.length-1 //last column  (0-based)
                    ));
            
            rownum++;// Skip row for Blank row
            for(int i=0; i<leftHeaderArr.length(); i++){
                HSSFRow leftHeaderRow = sheet.createRow(rownum++);
                cellnum = 0;
                cell = leftHeaderRow.createCell(cellnum++);
                cell.setCellValue(leftHeaderArr.getString(i));
                cell.setCellStyle(style);
                
                if(i<leftHeaderDatarArr.length()){
                    cell = leftHeaderRow.createCell(cellnum++);
                    cell.setCellValue(leftHeaderDatarArr.getString(i));
                }
                
                // print right header with respect to left header row.
                if(i<rightHeaderArr.length()){
                    int rightcellnum = headers.length-2;
                    cell = leftHeaderRow.createCell(rightcellnum++);
                    cell.setCellValue(rightHeaderArr.getString(i));
                    cell.setCellStyle(style);

                    if(i<rightHeaderDatarArr.length()){
                        cell = leftHeaderRow.createCell(rightcellnum);
                        cell.setCellValue(rightHeaderDatarArr.getString(i));
                    }
                }
            }
            
            if(obj.has("period") && !StringUtil.isNullOrEmpty(obj.getString("period"))){
                rownum++;

                HSSFRow headerRowPeriod = sheet.createRow(rownum);
                cellnum = 0;
                cell = headerRowPeriod.createCell(cellnum++);
                cell.setCellValue("Tax Period");
                cell.setCellStyle(leftstylebold);

                cell = headerRowPeriod.createCell(cellnum++);
                cell.setCellValue(obj.getString("period"));
                cell.setCellStyle(leftstyle);
            }
            if(obj.has("year") && !StringUtil.isNullOrEmpty(obj.getString("year"))){
                rownum++;

                HSSFRow headerRowYear = sheet.createRow(rownum);
                cellnum = 0;
                cell = headerRowYear.createCell(cellnum++);
                cell.setCellValue("Year");
                cell.setCellStyle(leftstylebold);

                cell = headerRowYear.createCell(cellnum++);
                cell.setCellValue(obj.getString("year"));
                cell.setCellStyle(leftstyle);
            }
            
            cellnum = 0;
            int startrow=++rownum;
            int lastrow=++rownum;
            headerRow = sheet.createRow(startrow);
            for (int h = 0; h < headers.length; h++) {//headers loop                
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                String headerkey = headers[h];
                cell = headerRow.createCell(cellnum++);  //Create new cell
                cell.setCellValue(headerStr);
                cell.setCellStyle(style);
                int strtcelnum=cellnum-1;
                
                if(headerkey.equals("invoiceno") || headerkey.equals("vendorname") || headerkey.equals("servicetax") || headerkey.equals("paymentno")
                    || headerkey.equals("openingbalanceservicetax") || headerkey.equals("credittakenservicetax") || headerkey.equals("creditutilizedservicetax")
                    || headerkey.equals("balanceservicetax")){
                    
                    String mergedtitle = "";
                    int numberOfColumnsToMerge = 0;
                    if(headerkey.equals("invoiceno")){
                        mergedtitle = "Invoice / Bill / Challan";
                        numberOfColumnsToMerge = 1;
                    } else if(headerkey.equals("vendorname")){
                        mergedtitle = "Input Service Provider's details";
                        numberOfColumnsToMerge = 1;
                    } else if(headerkey.equals("servicetax")){
                        mergedtitle = "Tax Paid";
                        numberOfColumnsToMerge = 2;
                    } else if(headerkey.equals("paymentno")){
                        mergedtitle = "Payment Particulars";
                        numberOfColumnsToMerge = 1;
                    } else if(headerkey.equals("openingbalanceservicetax")){
                        mergedtitle = "Opening Balance";
                        numberOfColumnsToMerge = 2;
                    } else if(headerkey.equals("credittakenservicetax")){
                        mergedtitle = "Input Credit (Available)";
                        numberOfColumnsToMerge = 2;
                    } else if(headerkey.equals("creditutilizedservicetax")){
                        mergedtitle = "Credit Utilized";
                        numberOfColumnsToMerge = 2;
                    } else if(headerkey.equals("balanceservicetax")){
                        mergedtitle = "Balance";
                        numberOfColumnsToMerge = 2;
                    }
                    cell.setCellValue(mergedtitle);
                    cell.setCellStyle(style);
                    sheet.addMergedRegion(new CellRangeAddress(
                        startrow, //first row (0-based)
                        startrow, //last row  (0-based)
                        strtcelnum, //first column (0-based)
                        strtcelnum + numberOfColumnsToMerge //last column  (0-based)
                    ));
                }
            }
            cellnum=0;
            headerRow = sheet.createRow(lastrow);
            for (int h = 0; h < headers.length; h++) {//headers loop       
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                String headerkey = headers[h];
                cell = headerRow.createCell(cellnum++);  //Create new cell
                if(headerkey.equals("invoiceno") || headerkey.equals("invoicedate") || headerkey.equals("vendorname") || headerkey.equals("servicetaxregno")
                    || headerkey.equals("servicetax") || headerkey.equals("sbc") || headerkey.equals("kkc") || headerkey.equals("paymentno") || headerkey.equals("paymentdate")
                    || headerkey.equals("openingbalanceservicetax") || headerkey.equals("openingbalancesbc") || headerkey.equals("openingbalancekkc")
                    || headerkey.equals("credittakenservicetax") || headerkey.equals("credittakensbc") || headerkey.equals("credittakenkkc")
                    || headerkey.equals("creditutilizedservicetax") || headerkey.equals("creditutilizedsbc") || headerkey.equals("creditutilizedkkc")
                    || headerkey.equals("balanceservicetax") || headerkey.equals("balancesbc") || headerkey.equals("balancekkc")){
                    
                    cell.setCellValue(headerStr);
                    cell.setCellStyle(style);
                }
            }

            for (int t = 0; t < repArr.length(); t++) {
                cellnum = 0;
                HSSFRow row = null;
                row = sheet.createRow(++rownum); 
                JSONObject temp = repArr.getJSONObject(t);
                for (int h = 0; h < headers.length; h++) {
                    cell = row.createCell(cellnum++);
                    if (temp.has(headers[h])) {
                        Object tempStr = temp.get(headers[h]);
                        if(tempStr instanceof Number && tempStr != null && !StringUtil.isNullOrEmpty(temp.getString(headers[h]))){
                            cell.setCellValue(temp.getDouble(headers[h]));
                            cell.setCellType(0);
                        } else {
                            cell.setCellValue(temp.getString(headers[h]));
                        }
                    }else{
                        cell.setCellValue("");
                    }
                }
            }//Data for loop
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
         return wb;
    }
    public JSONArray addTotalsForPrint(JSONArray DataJArr, String companyid) throws JSONException {
        double totalDiscountAmount = 0;
        double totalTaxAmount = 0;
        double totalTermAmount = 0;
        double totalAmount = 0;
        double totalBaseAmount = 0;
        double totalDiscountInBaseAmount = 0;
        double totalAmountDue = 0;
        double subTotal = 0;
        double totalAmountBeforeTax = 0;
        double productTotalAmount = 0;
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject obj = (JSONObject) DataJArr.get(i);
            totalDiscountAmount += obj.optDouble("discount", 0);
            totalDiscountInBaseAmount += obj.optDouble("discountinbase", 0);
            totalTaxAmount += obj.optDouble("taxamount", 0);
            totalTermAmount += obj.optDouble("termamount", 0);
            totalAmount += obj.optDouble("amount", 0);
            totalBaseAmount += obj.optDouble("amountinbase", 0);
            totalAmountDue += obj.optDouble("amountdue", 0);
            subTotal += obj.optDouble("subtotal", 0);
            totalAmountBeforeTax += obj.optDouble("amountBeforeTax", 0);
            productTotalAmount += obj.optDouble("productTotalAmount", 0);
        }

        JSONObject obj = new JSONObject();
        obj.put("billno", "Total");
        obj.put("discount", authHandler.round(totalDiscountAmount, companyid));
        obj.put("discountinbase", authHandler.round(totalDiscountInBaseAmount, companyid));
        obj.put("taxamount", authHandler.round(totalTaxAmount, companyid));
        obj.put("termamount", authHandler.round(totalTermAmount, companyid));
        obj.put("amount", authHandler.round(totalAmount, companyid));
        obj.put("amountinbase", authHandler.round(totalBaseAmount, companyid));
        obj.put("amountdue", authHandler.round(totalAmountDue, companyid));
        obj.put("subtotal", authHandler.round(subTotal, companyid));
        obj.put("amountBeforeTax", authHandler.round(totalAmountBeforeTax, companyid));
        obj.put("productTotalAmount", authHandler.round(productTotalAmount, companyid));
        DataJArr.put(obj);

        return DataJArr;
    }
}
