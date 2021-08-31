/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

/**
 *
 * @author krawler
 */
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.gst.dto.ExportFormatB2CS;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class GSTR2ExportToExcel {
     public static void main(String[] args) throws IOException, JSONException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Java Books");

        HSSFWorkbook wb = new HSSFWorkbook();
        XSSFWorkbook eFilingWorkbook = new XSSFWorkbook();
        XSSFSheet eFilingSheet = workbook.createSheet("GSTR2");

        HSSFWorkbook eFilingWb = new HSSFWorkbook();

        try {
            FileOutputStream outputStream = new FileOutputStream("/home/krawler/Desktop/NewFinancial/Financials/JavaBooks.xls");
            FileOutputStream optStream = new FileOutputStream("/home/krawler/Desktop/NewFinancial/Financials/JavaBooks.xls");

            wb.write(outputStream);
            eFilingWb.write(optStream);
        } catch (Exception ex) {

        }
    }
    
    public static void writedatain_GSTR2B2B(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("B2B Invoices_3_4A");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierGSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.invoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SupplierInvoiceNumber);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);        
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));            
            cell.setCellStyle(amountStyle);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("status"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue("pending");
        }
    }
     public static void writedatain_GSTR2CDNR(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Credit_Debit Notes Regular_6C");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierGSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.invoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Document_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Credit_Debit_Note_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Credit_Debit_Note_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invoicetype"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("status"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("originalinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("originalinvoicedate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue("pending");
        }
    }
      public static void writedatain_GSTR2B2B_URD(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("B2BUR Invoices_4B");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.invoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierType);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierType"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue("pending");
        }
    }
    public static void writedatain_GSTR2Import_service(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Import of Services_4C");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.invoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SupplierInvoiceNumber);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.reason);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("reason"));           
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle); 
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);          
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);           
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            //cell.setCellStyle(amountStyle);
            cell.setCellValue("pending");
        }
    }
     public static void writedatain_GSTR2Import_goods(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Import of Goods_5");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
         cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierGSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SupplierInvoiceNumber);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.billNo);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.billDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Port_Code);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);         
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("portcode"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));           
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle); 
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);          
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("pending");
        }
    }
      public static void writedatain_GSTR2CDNUR(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Credit_Debit Notes Unregistered_6C");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
         cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.invoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Document_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierType);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.reason);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SGST_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invoicetype"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierType"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("typeofinvoice"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("reason"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("pending");
        }
    }
       public static void writedatain_GSTR2Nil_Rated(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Nil Rated Invoices_7");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierType);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierGSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.SupplierInvoiceNumber);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.InvoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("vendorname"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invoicenumber"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("entrydate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("termamount",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("pending");
        }
    }
      public static void writedatain_GSTR2AT(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Advance Paid_10A");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IntegratedTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CentralTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.StateTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optString("rate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cessamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("pending");
        }
    }
      public static void writedatain_GSTR2ATADJ(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet("Adjustment of Advance_10B");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
//        DataFormat format = wb.createDataFormat();
//        amountStyle.setDataFormat(format.getFormat("0.00"));
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
  
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierType);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.supplierGSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.InvoiceDate);         
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.IntegratedTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CentralTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.StateTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.status);
        
        JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("supplierName"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("receiptno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("receiptdate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sumTaxableAmt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("cessamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue("pending");
        }
    }
}
