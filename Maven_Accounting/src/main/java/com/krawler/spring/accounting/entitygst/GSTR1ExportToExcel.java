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

/**
 * A very simple program that writes some data to an Excel file using the Apache
 * POI library.
 *
 * @author www.codejava.net
 *
 */
public class GSTR1ExportToExcel {

    //        /* cell style for locking */
//        HSSFCellStyle lockedCellStyle = wb.createCellStyle();
//        lockedCellStyle.setLocked(true);
//        /* cell style for editable cells */
//        HSSFCellStyle unlockedCellStyle = wb.createCellStyle();
//        unlockedCellStyle.setLocked(false);    
    public static void main(String[] args) throws IOException, JSONException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Java Books");

        HSSFWorkbook wb = new HSSFWorkbook();
        XSSFWorkbook eFilingWorkbook = new XSSFWorkbook();
        XSSFSheet eFilingSheet = workbook.createSheet("GSTR1 E-filing");

        HSSFWorkbook eFilingWb = new HSSFWorkbook();

//        writedatain_B2B(wb, null);
//        writedatain_B2CL(wb, null);
//        writedatain_B2CS(wb, null);
//        writedatain_CDNR(wb, null);
//        writedatain_CDNUR(wb, null);
//        writedatain_EXP(wb, null);
//        writedatain_AT(wb, null);
//        writedatain_ATADJ(wb, null);
//        writedatain_EXEMP(wb, null);
//        writedatain_HSN(wb, null);
//        writedatain_DOCS(wb, null);
        try {
            FileOutputStream outputStream = new FileOutputStream("/home/krawler/Desktop/NewFinancial/Financials/JavaBooks.xls");
            FileOutputStream optStream = new FileOutputStream("/home/krawler/Desktop/NewFinancial/Financials/JavaBooks.xls");

            wb.write(outputStream);
            eFilingWb.write(optStream);
        } catch (Exception ex) {

        }
    }

    public static void writedatain_B2B(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "b2b"));
        } else {
            sheet = wb.createSheet("b2b");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report
         * In E-Filing GSTR report added simple format as this exported file used for import in Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else {
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);
        
      JSONArray b2bArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);

//                for (int k = 0; k < itms.length(); k++) {
//                    JSONObject item = itms.getJSONObject(k);
//                    JSONObject itm_det = item.getJSONObject("itm_det");
//                    System.out.println("rt " + itm_det.optDouble("rt"));
//                    System.out.print("iamt" + itm_det.optDouble("iamt"));
//                    System.out.print("camt" + itm_det.optDouble("camt"));
//                    System.out.print("samt" + itm_det.optDouble("samt"));
//                    System.out.println("tatal tax " + (itm_det.optDouble("iamt") + itm_det.optDouble("camt") + itm_det.optDouble("samt")));
//                    System.out.println("csamt" + itm_det.optDouble("csamt"));
//                    totalTax = (itm_det.optDouble("iamt") + itm_det.optDouble("camt") + itm_det.optDouble("samt"));
//                    totalCess = itm_det.optDouble("csamt");
//                    if(totaltaxableAmount.containsKey(itm_det.getDouble("rt"))){
//                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
//                        totalTax = totalTax + tax;
//                    }
//                    if(cessAmount.containsKey(itm_det.getDouble("rt"))){
//                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
//                        totalCess = totalCess + tax;
//                    }
//                    totaltaxableAmount.put(itm_det.getDouble("rt"), totalTax);
//                    cessAmount.put(itm_det.getDouble("rt"), totalCess);
//                }
//                //inserting total tax records in excel
//                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {
//                    System.out.println("entry "+entry.getKey());
//                    cellnum = 0;
//                    headerRow = sheet.createRow(rownum++);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("ctin"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("inum"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("idt"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optDouble("val"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("pos"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(GSTRConstants.Reverse_Charge);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(GSTRConstants.Invoice_Type);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("etin"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(entry.getKey());
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(entry.getValue());
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
//                }
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt"));
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
            cell.setCellValue(invDetails.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt"));
        }
    }
    public static void writedatain_MisMatchReportData(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet(jobj.optJSONArray("summaryArr").optJSONObject(0).optString("typeofinvoice"));
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
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
        JSONArray b2bArray = jobj.getJSONArray("data");
        cellnum = 0;
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Invoice Type");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Invoice No");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Sales Return");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Date");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Customer");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Products");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Tax Amount");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Total Amount");
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue("Reason");
        if (jobj.optString("typeofinvoice", "").equalsIgnoreCase(GSTRConstants.GSTMisMatch_SECTION_CustomerVendorTypeblank)
                || jobj.optString("typeofinvoice", "").equalsIgnoreCase(GSTRConstants.GSTMisMatch_SECTION_GSTRegistrationTypeblank)) {
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.GST_TYPE_COLUMN_NAME);
        }

        for (int i = 0; i < b2bArray.length(); i++) {

            JSONObject invDetails = b2bArray.getJSONObject(i);

            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("typeofinvoice"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("billno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("returnno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("billdate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("customer"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("productid"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxamount"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("amount"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("reason"));
            if (jobj.optString("typeofinvoice", "").equalsIgnoreCase(GSTRConstants.GSTMisMatch_SECTION_CustomerVendorTypeblank)
                    || jobj.optString("typeofinvoice", "").equalsIgnoreCase(GSTRConstants.GSTMisMatch_SECTION_GSTRegistrationTypeblank)) {
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(invDetails.optString("gsttypename"));
            }
        }
    }

    public static void writedatain_TDSTCS(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        sheet = wb.createSheet(jobj.optString("sheetname", "TDS"));
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_B2B);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        /////////// Total Header End ////////////
        JSONArray b2bArray = jobj.getJSONArray("data");

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellValue(b2bArray.length());
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(B5:B40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.JENO);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TOTALAMT);
        for (int i = 0; i < b2bArray.length(); i++) {
            JSONObject invDetails = b2bArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("journalentryno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optDouble("jeamount"));

        }
    }
    public static void writedatain_excel(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "b2b"));
        } else {
            sheet = wb.createSheet("b2b");
        }

    }
    public static void writedatain_B2CL(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = wb.createSheet("b2cl");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);
        JSONArray b2clArray = jobj.getJSONArray("data");
        for (int i = 0; i < b2clArray.length(); i++) {

            JSONObject invDetails = b2clArray.getJSONObject(i);

//                for (int k = 0; k < itms.length(); k++) {
//                    JSONObject item = itms.getJSONObject(k);
//                    JSONObject itm_det = item.getJSONObject("itm_det");
//                    System.out.println("rt " + itm_det.optDouble("rt"));
//                    System.out.print("iamt" + itm_det.optDouble("iamt"));
//                    System.out.print("camt" + itm_det.optDouble("camt"));
//                    System.out.print("samt" + itm_det.optDouble("samt"));
//                    System.out.println("tatal tax " + (itm_det.optDouble("iamt") + itm_det.optDouble("camt") + itm_det.optDouble("samt")));
//                    System.out.println("csamt" + itm_det.optDouble("csamt"));
//                    totalTax = (itm_det.optDouble("iamt") + itm_det.optDouble("camt") + itm_det.optDouble("samt"));
//                    totalCess = itm_det.optDouble("csamt");
//                    if(totaltaxableAmount.containsKey(itm_det.getDouble("rt"))){
//                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
//                        totalTax = totalTax + tax;
//                    }
//                    if(cessAmount.containsKey(itm_det.getDouble("rt"))){
//                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
//                        totalCess = totalCess + tax;
//                    }
//                    totaltaxableAmount.put(itm_det.getDouble("rt"), totalTax);
//                    cessAmount.put(itm_det.getDouble("rt"), totalCess);
//                }
//                //inserting total tax records in excel
//                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {
//                    System.out.println("entry "+entry.getKey());
//                    cellnum = 0;
//                    headerRow = sheet.createRow(rownum++);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("ctin"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("inum"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("idt"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optDouble("val"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("pos"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(GSTRConstants.Reverse_Charge);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(GSTRConstants.Invoice_Type);
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(inv.optString("etin"));
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(entry.getKey());
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(entry.getValue());
//                    cell = headerRow.createCell(cellnum++);
//                    cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
//                }
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(invDetails.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("taxableAmt"));
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
            cell.setCellValue(invDetails.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(invDetails.optDouble("totalAmt"));
        }

    }

    public static void writedatain_B2CS(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = wb.createSheet("b2cs");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
       /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.docType);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

        JSONArray b2csArray = jobj.optJSONArray("data");
        for (int i = 0; i < b2csArray.length(); i++) {
            JSONObject b2cs = b2csArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("type"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(b2cs.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("taxableAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("IGSTamount",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("CGSTamount",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("SGSTamount",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(b2cs.optDouble("totalAmt"));

        }
    }

    public static void writedatain_CDNR(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "cdnr"));
        } else {
            sheet = wb.createSheet("cdnr");
        }

        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.creditNoteNo);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.originalInvoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Reason_Description);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

        JSONArray cdnrArray = new JSONArray();
        cdnrArray = jobj.optJSONArray("data");
        for (int i = 0; i < cdnrArray.length(); i++) {
            JSONObject cdnr = cdnrArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("originalinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("originalinvoicedate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("invoicetype"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("reasondesc"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnr.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnr.optDouble("taxableAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnr.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnr.optDouble("totalAmt"));

        }
    }

    public static void writedatain_CDNUR(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = wb.createSheet("cdnur");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.creditNoteNo);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.originalInvoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Reason_Description);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

        JSONArray cdnurArray = new JSONArray();
        cdnurArray = jobj.optJSONArray("data");
        for (int i = 0; i < cdnurArray.length(); i++) {
            JSONObject cdnur = cdnurArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("originalinvoicedate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.getString("placeofsupply"));     
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("originalinvoiceno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.getString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(cdnur.optString("reasondesc"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("taxableAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("cess",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(cdnur.optDouble("totalAmt"));

        }
    }

    public static void writedatain_EXP(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = wb.createSheet("exp");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Integrated_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.StateTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Central_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

       JSONArray exportArray = new JSONArray();
        exportArray = jobj.optJSONArray("data");

        for (int i = 0; i < exportArray.length(); i++) {
            JSONObject export = exportArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(export.optString("date"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(export.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(export.optString("invNum"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(export.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(export.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("taxableAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("igstamt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("sgstamt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("cgstamt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("cess"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("totalTax"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(export.optDouble("totalAmt"));
        }
    }

    public static void writedatain_AT(HSSFWorkbook wb, JSONObject jobj) {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "at"));
        } else {
            sheet = wb.createSheet("at");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

        JSONArray jSONArray = jobj.optJSONArray("data");
        for (int index = 0; index < jSONArray.length(); index++) {
            cellnum = 0;
            JSONObject nObject = jSONArray.optJSONObject(index);
           cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(nObject.optString("receiptdate"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(nObject.optString("gstin"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(nObject.optString("customername"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(nObject.optString("receiptno"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(nObject.optString("placeofsupply"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("sumTaxableAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("igstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("cgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("sgstamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("cessamt",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("sumTaxAmt"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(nObject.optDouble("sumTotalAmt"));
        }
    }

    public static void writedatain_ATADJ(HSSFWorkbook wb, JSONObject jobj) {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "atadj"));
        } else {
            sheet = wb.createSheet("atadj");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
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
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.CustomerName);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Invoice_Number);
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
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Amount_Incl_Taxes);

        JSONArray jSONArray = jobj.optJSONArray("data");
        if (jSONArray != null) {
            for (int index = 0; index < jSONArray.length(); index++) {
                cellnum = 0;
                JSONObject nObject = jSONArray.optJSONObject(index);
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("receiptdate"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("gstin"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("customername"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("receiptno"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("placeofsupply"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("sumTaxableAmt"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("igstamt", 0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("cgstamt", 0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("sgstamt", 0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("cessamt", 0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("sumTaxAmt"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("sumTotalAmt"));
            }
        }
    }

    public static void writedatain_EXEMP(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "exemp"));
        } else {
            sheet = wb.createSheet("exemp");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
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
          
        if (jobj.has("isEfiling") && jobj.optBoolean("isEfiling")) {
          /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_EXEMP);
        sheet.autoSizeColumn(cellnum);

        cellnum = 1;
        totalHeaderRow = sheet.createRow(rownum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Nil_Rated_Supplies);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Exempted_Supplies);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_NonGST_Supplies);

        /////////// Total Header End ////////////
        cellnum = 1;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(B5:B40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(C5:C40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(D5:D40000)");

    }
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Description);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Nil_Rated_Supplies);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Exempt_otherthan_Nil_rated);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.NonGST_Supplies);

        JSONArray exemptArray = new JSONArray();
        exemptArray = jobj.optJSONArray("data");
        for (int i = 0; i < exemptArray.length(); i++) {
            JSONObject exempt = exemptArray.getJSONObject(i);
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(exempt.optString("description"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(exempt.optDouble("nilratedsupplies"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(exempt.optDouble("exempted",0));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(exempt.optDouble("nongstsupplies",0));
        }
    }

    public static void writedatain_HSN(HSSFWorkbook wb, JSONObject jobj) {
        HSSFSheet sheet = wb.createSheet("hsn");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        /**
         * Number format in for Export E-Filing GSTR report In E-Filing GSTR
         * report added simple format as this exported file used for import in
         * Government Portal
         */
        if (jobj.optBoolean("isEfiling",false)) {
            DataFormat format = wb.createDataFormat();
            amountStyle.setDataFormat(format.getFormat("0.00"));
        }else{
            amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
        }
        CellStyle dateStyle = wb.createCellStyle();
        DataFormat dateFormat = wb.createDataFormat();
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
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
        
        if (jobj.has("isEfiling") && jobj.optBoolean("isEfiling")) {
            /////////// Total Header Start ////////////
            HSSFRow totalHeaderRow = sheet.createRow(rownum++);
            cell = totalHeaderRow.createCell(cellnum++);
            totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Summary_For_HSN);
            sheet.autoSizeColumn(cellnum);

            cellnum = 0;
            totalHeaderRow = sheet.createRow(rownum++);
            totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
            cell.setCellStyle(rowstyle);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.No_of_HSN);
            cellnum = cellnum + 3;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_Value);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_Taxable_Value);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_Integrated_Tax);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_Central_Tax);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_State_UT_Tax);
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
            cell.setCellValue(GSTRConstants.Total_Cess);

            /////////// Total Header End ////////////
            JSONArray jSONArray = jobj.optJSONArray("data");

            cellnum = 0;
            totalHeaderRow = sheet.createRow(rownum++);
            cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(jSONArray.length());
            cell.setCellFormula("SUMPRODUCT((A5:A40001<>\"\")/COUNTIF(A5:A40001,A5:A40001&\"\"))");
            cellnum = cellnum + 3;
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(E5:E40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(F5:F40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(G5:G40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(H5:H40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(I5:I40000)");
            cell = totalHeaderRow.createCell(cellnum++);
            cell.setCellFormula("SUM(J5:J40000)");
        }
        /////////// Header Start ////////////             
        
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.HSN_code);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
            cell.setCellValue(GSTRConstants.Invoice_Number);
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(rowstyle);
        }
        cell.setCellValue(GSTRConstants.Description);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.UQC);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Quantity);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Value);
//        if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Taxable_Value);
//        }
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Integrated_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Central_Tax_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.State_UT_Tax_Amount);        
        if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Tax_Amount);
        }
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Cess_Amount);


        JSONArray jSONArray = jobj.optJSONArray("data");
        if (jSONArray != null) {
            for (int index = 0; index < jSONArray.length(); index++) {
                cellnum = 0;
                JSONObject nObject = jSONArray.optJSONObject(index);
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("hsnno"));
                cell = headerRow.createCell(cellnum++);
                if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
                    cell.setCellValue(nObject.optString("invNum"));
                    cell = headerRow.createCell(cellnum++);
                }
                cell.setCellValue(nObject.optString("description"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("uqc"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optDouble("totalquantity"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("totalAmt"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("taxableAmt"));
//                if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("IGSTamount"));
//                }
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("CGSTamount"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("SGSTamount"));
                if (!jobj.has("isEfiling") && !jobj.optBoolean("isEfiling")) {
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("totaltax"));
                }
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("cess"));
            }
        }
    }

    public static void writedatain_DOCS(HSSFWorkbook wb, JSONObject jobj) {
        HSSFSheet sheet = wb.createSheet("docs");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
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

       if (jobj.has("isEfiling") && jobj.optBoolean("isEfiling")) {
        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_DOCS);
        sheet.autoSizeColumn(cellnum);

        cellnum = 3;
        totalHeaderRow = sheet.createRow(rownum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Number);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cancelled);

        /////////// Total Header End ////////////
        cellnum = 3;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(D5:D40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(E5:E40000)");
    }    
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        if (!jobj.optBoolean("isEfiling",false)) {
            rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        }
        HSSFRow headerRow1 = sheet.createRow(rownum++);
        headerRow1.setRowStyle((HSSFCellStyle) rowstyle);
        HSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Nature_of_Document);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.From);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.To);
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Documents);
        if (jobj.has("isEfiling") && jobj.optBoolean("isEfiling")) {
        cell = headerRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Cancelled);
        }
        JSONArray jSONArray = jobj.optJSONArray("coldata");
        if (jSONArray != null) {
            for (int index = 0; index < jSONArray.length(); index++) {
                cellnum = 0;
                JSONObject nObject = jSONArray.optJSONObject(index);
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("natureofdocument"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("fromInv"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("toInv"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optInt("noofinvoices"));
            }
        }

    }

    public static void writedatain_GST3B(HSSFWorkbook wb, JSONObject jobj) {
        HSSFSheet sheet = wb.createSheet("GST Computation");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = StringUtil.getCommaSepratedAmountStyle(wb, jobj.optString(Constants.companyKey));
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

//        /////////// Total Header Start ////////////
//        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
//        cell = totalHeaderRow.createCell(cellnum++);
//        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
//        cell.setCellStyle(rowstyle);
//        cell.setCellValue(GSTRConstants.Section);
//        sheet.autoSizeColumn(cellnum);
//
//        cellnum = 2;
//        totalHeaderRow = sheet.createRow(rownum++);
//        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
//        cell.setCellStyle(rowstyle);
//        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellStyle(rowstyle);
//        cell.setCellValue(GSTRConstants.Total_Advance_Adjusted);
//        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellStyle(rowstyle);
//        cell.setCellValue(GSTRConstants.Total_Cess);
//
//        /////////// Total Header End ////////////
//        cellnum = 2;
//        totalHeaderRow = sheet.createRow(rownum++);
//        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(C5:C40000)");
//        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(D5:D40000)");
        /////////// Header Start ////////////
        cellnum = 0;
        /**
         * Added Entity Name in Sheet
         */
        rownum = GSTR1ExportToExcel.addEntityNameCellForGSTRExport(jobj, sheet, rownum);
        HSSFRow epmptyROW = sheet.createRow(rownum++);
        epmptyROW.setRowStyle((HSSFCellStyle) rowstyle);
        
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Section);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Headings);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TypeofSalesPurchases);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TaxableValue);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.IntegratedTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.CentralTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.StateTaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.CessAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TaxAmount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TotalAmount);

        JSONArray jSONArray = jobj.optJSONArray("data");
        if (jSONArray != null) {
            for (int index = 0; index < jSONArray.length(); index++) {
                cellnum = 0;
                JSONObject nObject = jSONArray.optJSONObject(index);
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("section"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("heading"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("typeofsales"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(nObject.optString("pos"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("taxableamt",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("igst",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("cgst",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("sgst",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("csgst",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("totaltax",0));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(nObject.optDouble("totalamount",0));
            }
        }
    }

    public static void writedatain_efilingB2B(HSSFWorkbook eFilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = eFilingWb.createSheet("b2b");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        Map<Double, Double> Entry = new HashMap<Double, Double>();
        HSSFCell cell = null;
        CellStyle rowstyle = eFilingWb.createCellStyle();
        CellStyle cellstyle = eFilingWb.createCellStyle();
        CellStyle amountStyle = eFilingWb.createCellStyle();
        CellStyle dateStyle = eFilingWb.createCellStyle();
        DataFormat format = eFilingWb.createDataFormat();
        DataFormat dateFormat = eFilingWb.createDataFormat();
        Font font = eFilingWb.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        rowstyle.setFont(font);
        amountStyle.setDataFormat(format.getFormat("0.00"));
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
        cellstyle.setFont(font);
        cellstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellstyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        cellstyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_B2B);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Recipients);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cellnum = cellnum + 5;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);
        /////////// Total Header End ////////////
        JSONArray b2bArray = jobj.getJSONArray("b2b");
        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(b2bArray.length());
        cell.setCellFormula("SUMPRODUCT((A5:A40000<>\"\")/COUNTIF(A5:A40000,A5:A40000&\"\"))");
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(totalInvoices);
        cell.setCellFormula("SUMPRODUCT((B5:B40000<>\"\")/COUNTIF(B5:B40000,B5:B40000&\"\"))");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(D5:D40000)");
        cell.setCellFormula("SUMPRODUCT(1/COUNTIF(B5:B40000,B5:B40000&\"\"),D5:D40000)");
        cellnum = cellnum + 5;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(J5:J40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(K5:K40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.GSTIN_UINof_Recipient);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Receiver_Name);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Reverse_Charge);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.E_Commerce_GSTIN);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Cess_Amount);

        for (int i = 0; i < b2bArray.length(); i++) {
            JSONObject b2b = b2bArray.getJSONObject(i);
            String Gstin = b2b.getString("ctin");
            JSONArray invArray = b2b.getJSONArray("inv");
            for (int j = 0; j < invArray.length(); j++) {
                double taxableAmt = 0, totalCess = 0,totalTax = 0,taxableAmtInv = 0.0,totalAmt=0;
                JSONObject inv = invArray.getJSONObject(j);
                JSONArray itms = inv.getJSONArray("itms");

                Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
                Map<Double, Double> cessAmount = new HashMap<Double, Double>();

                for (int k = 0; k < itms.length(); k++) {
                    JSONObject item = itms.getJSONObject(k);
                    JSONObject itm_det = item.getJSONObject("itm_det");
                    taxableAmtInv += itm_det.optDouble("txval");
                    taxableAmt = itm_det.optDouble("txval");
                    totalCess = itm_det.optDouble("csamt");
                    Double cgst = itm_det.optDouble("camt");
                    Double sgst = itm_det.optDouble("samt");
                    Double igst = itm_det.optDouble("iamt");
                    Double csgst = itm_det.optDouble("csamt");
                    totalTax += (cgst + sgst + igst + csgst);
                    if (totaltaxableAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
                        taxableAmt = taxableAmt + tax;
                    }
                    if (cessAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
                        totalCess = totalCess + tax;
                    }
                   
                        totaltaxableAmount.put(itm_det.getDouble("rt"), taxableAmt);
                        cessAmount.put(itm_det.getDouble("rt"), totalCess);
                    
                }
                totalAmt = (taxableAmtInv + totalTax);

                //inserting total tax records in excel
                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {
                    //System.out.println("entry " + entry.getKey());
                    cellnum = 0;
                    headerRow = sheet.createRow(rownum++);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(Gstin);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("customerName"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("inum"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(dateStyle);
                    if (!StringUtil.isNullOrEmpty(inv.optString("idt"))) {
                    cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(inv.optString("idt")));
                    }
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue((totalAmt));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(getPos(inv));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("rchrg"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("inv_typ"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("etin"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue((entry.getKey()));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue((entry.getValue()));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
                }

            }

        }
    }

    public static void writedatain_efilingB2CL(HSSFWorkbook eFilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = eFilingWb.createSheet("b2cl");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;    //Total number of invoices
        HSSFCell cell = null;
        CellStyle rowstyle = eFilingWb.createCellStyle();
        CellStyle cellstyle = eFilingWb.createCellStyle();
        CellStyle amountStyle = eFilingWb.createCellStyle();
        CellStyle dateStyle = eFilingWb.createCellStyle();
        DataFormat format = eFilingWb.createDataFormat();
        DataFormat dateFormat = eFilingWb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
        Font font = eFilingWb.createFont();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_B2CL);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);
        /////////// Total Header End ////////////

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(totalInvoices);
        cell.setCellFormula("SUMPRODUCT((A5:A40000<>\"\")/COUNTIF(A5:A40000,A5:A40000&\"\"))");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(C5:C40000)");
        cell.setCellFormula("SUMPRODUCT(1/COUNTIF(A5:A40000,A5:A40000&\"\"),C5:C40000)");
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(F5:F40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(G5:G40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.E_Commerce_GSTIN);

        JSONArray b2clArray = jobj.getJSONArray("b2cl");
        System.out.println("b2clArray : " + b2clArray);
        System.out.println("jsonArray " + b2clArray.length());
        for (int i = 0; i < b2clArray.length(); i++) {
            JSONObject b2b = b2clArray.getJSONObject(i);
            JSONArray invArray = b2b.getJSONArray("inv");
            for (int j = 0; j < invArray.length(); j++) {
                totalInvoices++; // counting invoices
                double taxableAmt = 0, totalCess = 0, totalTax = 0, taxableAmtInv = 0.0, totalAmt = 0;
                JSONObject inv = invArray.getJSONObject(j);
                JSONArray itms = inv.getJSONArray("itms");

                Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
                Map<Double, Double> cessAmount = new HashMap<Double, Double>();

                for (int k = 0; k < itms.length(); k++) {
                    JSONObject item = itms.getJSONObject(k);
                    JSONObject itm_det = item.getJSONObject("itm_det");
                    taxableAmtInv += itm_det.optDouble("txval");
                    taxableAmt = itm_det.optDouble("txval");
                    totalCess = itm_det.optDouble("csamt");
                    Double cgst = itm_det.optDouble("camt");
                    Double sgst = itm_det.optDouble("samt");
                    Double igst = itm_det.optDouble("iamt");
                    Double csgst = itm_det.optDouble("csamt");
                    totalTax += (cgst + sgst + igst + csgst);
                    totalTax = (itm_det.optDouble("iamt") + itm_det.optDouble("camt") + itm_det.optDouble("samt"));
                    if (totaltaxableAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
                        taxableAmt = taxableAmt + tax;
                    }
                    if (cessAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
                        totalCess = totalCess + tax;
                    }
                    totaltaxableAmount.put(itm_det.getDouble("rt"), taxableAmt);
                    cessAmount.put(itm_det.getDouble("rt"), totalCess);
                }
                totalAmt = (taxableAmtInv + totalTax);
//                //inserting total tax records in excel
                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {

                    cellnum = 0;
                    headerRow = sheet.createRow(rownum++);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("inum"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(dateStyle);
                    if (!StringUtil.isNullOrEmpty(inv.optString("idt"))) {
                    cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(inv.optString("idt")));
                    }
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(totalAmt);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(getPos(inv));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getKey());
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getValue());
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("etin"));

                }

            }
        }

    }

    public static void writedatain_efilingB2CS(HSSFWorkbook efilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = efilingWb.createSheet("b2cs");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = efilingWb.createCellStyle();
        CellStyle cellstyle = efilingWb.createCellStyle();
        CellStyle amountStyle = efilingWb.createCellStyle();
        DataFormat format = efilingWb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));

        Font font = efilingWb.createFont();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_B2CS);
        sheet.autoSizeColumn(cellnum);

        cellnum = 3;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);
        /////////// Total Header End ////////////
        cellnum = 3;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(D5:D40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(E5:E40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.TYPE);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.E_Commerce_GSTIN);

        JSONArray b2csArray = jobj.optJSONArray("b2cs");
        ExportFormatB2CS b2csFormatData = new ExportFormatB2CS();
        Map<ExportFormatB2CS.UniqueParam, Map<String, Double>> collection = b2csFormatData.exportDataFormat(b2csArray);
        for (Entry<ExportFormatB2CS.UniqueParam, Map<String, Double>> entry : collection.entrySet()) {
            ExportFormatB2CS.UniqueParam uniqueparam = entry.getKey();
            Map<String, Double> valuemap = entry.getValue();

       
                    
            cellnum = 0;
            headerRow = sheet.createRow(rownum++);
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(uniqueparam.getType());
            cell = headerRow.createCell(cellnum++);
//            cell.setCellValue(b2cs.optString("pos"));
            cell.setCellValue(uniqueparam.getPos());
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(uniqueparam.getRate());
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(valuemap.get("txval"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellStyle(amountStyle);
            cell.setCellValue(valuemap.get("cess"));
            cell = headerRow.createCell(cellnum++);
            cell.setCellValue(uniqueparam.geteComNumber());
        }
    }

    public static void writedatain_efilingCDNR(HSSFWorkbook eFilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = eFilingWb.createSheet("cdnr");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;
        String typeofinvoice = "CDNR";
        HSSFCell cell = null;
        CellStyle rowstyle = eFilingWb.createCellStyle();
        CellStyle cellstyle = eFilingWb.createCellStyle();
        CellStyle amountStyle = eFilingWb.createCellStyle();
        CellStyle dateStyle = eFilingWb.createCellStyle();
        DataFormat format = eFilingWb.createDataFormat();
        DataFormat dateFormat = eFilingWb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
        Font font = eFilingWb.createFont();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_CDNR);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Recipients);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Notes_Vouchers);
        cellnum = cellnum + 4;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Note_Refund_Voucher_Value);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);
        /////////// Total Header End ////////////
        JSONArray cdnrArray = new JSONArray();
        cdnrArray = jobj.optJSONArray("cndr");
        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(totalInvoices);
        cell.setCellFormula("SUMPRODUCT((A5:A40001<>\"\")/COUNTIF(A5:A40001,A5:A40001&\"\"))");
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(cdnrArray.length());
        cell.setCellFormula("SUMPRODUCT((B5:B40001<>\"\")/COUNTIF(B5:B40001,B5:B40001&\"\"))");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue("");
        cell.setCellFormula("SUMPRODUCT((D5:D40001<>\"\")/COUNTIF(D5:D40001,D5:D40001&\"\"))");
        cellnum = cellnum + 4;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(I5:I40000)");
        cell.setCellFormula("SUMPRODUCT(1/COUNTIF(D5:D10001,D5:D10001&\"\"),I5:I10001)");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(K5:K40000)");
        cell.setCellStyle(amountStyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(L5:L40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.GSTIN_UINof_Recipient);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Receiver_Name);//showing Receiver name ERM-1033 
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Advance_Receipt_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Advance_Receipt_date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Document_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Reason_For_Issuing_document);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Pre_GST);

        for (int i = 0; i < cdnrArray.length(); i++) {
            JSONObject cdnr = cdnrArray.getJSONObject(i);
            JSONArray invArray = cdnr.getJSONArray("nt");
            for (int j = 0; j < invArray.length(); j++) {
                totalInvoices++; // counting invoices
                double taxableAmt = 0, totalTax = 0, totalCess = 0,taxableAmtInv=0,totalAmt=0;
                JSONObject inv = invArray.getJSONObject(j);
                JSONArray itms = inv.getJSONArray("itms");

                Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
                Map<Double, Double> cessAmount = new HashMap<Double, Double>();

                for (int k = 0; k < itms.length(); k++) {
                    JSONObject item = itms.getJSONObject(k);
                    JSONObject itm_det = item.getJSONObject("itm_det");
                    taxableAmtInv += itm_det.optDouble("txval");
                    taxableAmt = itm_det.optDouble("txval");
                    totalCess = itm_det.optDouble("csamt");
                    Double cgst = itm_det.optDouble("camt");
                    Double sgst = itm_det.optDouble("samt");
                    Double igst = itm_det.optDouble("iamt");
                    Double csgst = itm_det.optDouble("csamt");
                    totalTax += (cgst + sgst + igst + csgst);
                    /*
                     * Rate wise Taxable Amount
                     */
                    if (totaltaxableAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
                        taxableAmt = taxableAmt + tax;
                    }
                    if (cessAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
                        totalCess = totalCess + tax;
                    }
                    totaltaxableAmount.put(itm_det.getDouble("rt"), taxableAmt);
                    cessAmount.put(itm_det.getDouble("rt"), totalCess);
                }
                totalAmt = (taxableAmtInv + totalTax);

//                //inserting total tax records in excel
                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {

                    cellnum = 0;
                    headerRow = sheet.createRow(rownum++);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(cdnr.optString("ctin"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("customerName"));//showing Receiver name ERM-1033 
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("inum"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(dateStyle);
                    if (!StringUtil.isNullOrEmpty(inv.optString("idt"))) {
                        cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(inv.optString("idt")));
                    }
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("nt_num"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(dateStyle);
                    if (!StringUtil.isNullOrEmpty(inv.optString("nt_dt"))) {
                        cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(inv.optString("nt_dt")));
                    }
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("ntty"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("rsn"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(getPos(inv));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(totalAmt);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getKey());
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getValue());
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.getString("p_gst"));

                }

            }
        }
    }

    public static void writedatain_efilingCDNUR(HSSFWorkbook eFilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = eFilingWb.createSheet("cdnur");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;
        String typeofinvoice = "CDNUR";
        HSSFCell cell = null;
        CellStyle rowstyle = eFilingWb.createCellStyle();
        CellStyle cellstyle = eFilingWb.createCellStyle();
        CellStyle amountStyle = eFilingWb.createCellStyle();
        CellStyle dateStyle = eFilingWb.createCellStyle();
        DataFormat format = eFilingWb.createDataFormat();
        DataFormat dateFormat = eFilingWb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
        Font font = eFilingWb.createFont();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_CDNUR);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        
       cell = totalHeaderRow.createCell(cellnum++); // No.of receipants not required ERM-1033
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
//        cell.setCellStyle(rowstyle);
//        cell.setCellValue(GSTRConstants.No_of_Recipients);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Notes_Vouchers);
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cellnum = cellnum + 3;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Note_Refund_Voucher_Value);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);
        /////////// Total Header End ////////////
        JSONArray cdnurArray = new JSONArray();
        cdnurArray = jobj.optJSONArray("cdnur");

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);  // No.of receipants not required ERM-1033
////        cell.setCellValue(totalInvoices);
//        cell.setCellFormula("SUMPRODUCT((A5:A40001<>\"\")/COUNTIF(A5:A40001,A5:A40001&\"\"))");
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(cdnurArray.length());
        cell.setCellFormula("SUMPRODUCT((B5:B40001<>\"\")/COUNTIF(B5:B40001,B5:B40001&\"\"))");
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue("");
        cell.setCellFormula("SUMPRODUCT((E5:E40001<>\"\")/COUNTIF(E5:E40001,E5:E40001&\"\"))");
        cellnum = cellnum + 3;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellFormula("SUM(I5:I40000)");
        cell.setCellFormula("SUMPRODUCT(1/COUNTIF(B5:B10001,B5:B10001&\"\"),I5:I10001)");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(K5:K40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(L5:L40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.UR_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Document_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Advance_Receipt_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Advance_Receipt_date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Reason_For_Issuing_document);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Note_Refund_Voucher_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Cess_Amount);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Pre_GST);

        for (int i = 0; i < cdnurArray.length(); i++) {
            JSONObject cdnur = cdnurArray.getJSONObject(i);

            totalInvoices++; // counting invoices
            double taxableAmt = 0, totalTax = 0, totalCess = 0,taxableAmtInv=0,totalAmt=0;

            JSONArray itms = cdnur.getJSONArray("itms");

            Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
            Map<Double, Double> cessAmount = new HashMap<Double, Double>();

            for (int k = 0; k < itms.length(); k++) {
                JSONObject item = itms.getJSONObject(k);
                JSONObject itm_det = item.getJSONObject("itm_det");
                taxableAmtInv += itm_det.optDouble("txval");
                taxableAmt = itm_det.optDouble("txval");
                totalCess = itm_det.optDouble("csamt");
                Double cgst = itm_det.optDouble("camt");
                Double sgst = itm_det.optDouble("samt");
                Double igst = itm_det.optDouble("iamt");
                Double csgst = itm_det.optDouble("csamt");
                totalTax += (cgst + sgst + igst + csgst);
               /*
                * Rate wise Taxable Amount
                */
                if (totaltaxableAmount.containsKey(itm_det.getDouble("rt"))) {
                    double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
                    taxableAmt = taxableAmt + tax;
                }
                if (cessAmount.containsKey(itm_det.getDouble("rt"))) {
                    double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
                    totalCess = totalCess + tax;
                }
                totaltaxableAmount.put(itm_det.getDouble("rt"), taxableAmt);
                cessAmount.put(itm_det.getDouble("rt"), totalCess);
            }
            totalAmt = (taxableAmtInv + totalTax);

//                //inserting total tax records in excel
            for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {

                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.optString("typ"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.optString("nt_num"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(dateStyle);
                if(!StringUtil.isNullOrEmpty(cdnur.optString("nt_dt"))){
                cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(cdnur.optString("nt_dt")));
                 }
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.optString("ntty"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.optString("inum"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(dateStyle);
                if(!StringUtil.isNullOrEmpty(cdnur.optString("idt"))){
                cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(cdnur.optString("idt")));
                }
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.optString("rsn"));
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(getPos(cdnur));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(totalAmt);
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getKey());
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getValue());
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(cdnur.getString("p_gst"));

            }

        }
    }
    public static void writedatain_EfilingAT(HSSFWorkbook wb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "at"));
        } else {
            sheet = wb.createSheet("at");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_AT);
        sheet.autoSizeColumn(cellnum);

        cellnum = 2;
        totalHeaderRow = sheet.createRow(rownum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Advance_Received);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);

        /////////// Total Header End ////////////
        cellnum = 2;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(C5:C40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(D5:D40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Gross_Advance_Received);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.CessAmount);

        JSONArray dataArray = jobj.optJSONArray("at");
        for (int index = 0; index < dataArray.length(); index++) {
            JSONObject posObj = dataArray.getJSONObject(index);

            Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
            Map<Double, Double> cessAmount = new HashMap<Double, Double>();
            double taxableAmt = 0, totalTax = 0, totalCess = 0, taxableAmtInv = 0, totalAmt = 0;
            JSONArray itemsarrArray = posObj.getJSONArray("itms");
            for (int itemindex = 0; itemindex < itemsarrArray.length(); itemindex++) {

                JSONObject itemObj = itemsarrArray.getJSONObject(itemindex);
                taxableAmtInv += itemObj.optDouble("txval");
                taxableAmt = itemObj.optDouble("txval");
                totalCess = itemObj.optDouble("csamt");
                Double IGSTAmount = itemObj.optDouble("iamt");
                Double CGSTAmount = itemObj.optDouble("camt");
                Double SGSTAmount = itemObj.optDouble("samt");
                Double CESSAmount = itemObj.optDouble("csamt");
                totalTax += (IGSTAmount + CGSTAmount + SGSTAmount + CESSAmount);
                /*
                * Rate wise Taxable Amount
                */
                if (totaltaxableAmount.containsKey(itemObj.getDouble("rt"))) {
                    double tax = Double.parseDouble(totaltaxableAmount.get(itemObj.getDouble("rt")).toString());
                    taxableAmt = taxableAmt + tax;
                }
                if (cessAmount.containsKey(itemObj.getDouble("rt"))) {
                    double tax = Double.parseDouble(cessAmount.get(itemObj.getDouble("rt")).toString());
                    totalCess = totalCess + tax;
                }
                totaltaxableAmount.put(itemObj.getDouble("rt"), taxableAmt);
                cessAmount.put(itemObj.getDouble("rt"), totalCess);
            }
            totalAmt = (taxableAmtInv + totalTax);

            for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(getPos(posObj));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getKey());
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getValue());
                cell.setCellStyle(amountStyle);
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
            }
        }
    }
    public static void writedatain_EfilingATADJ(HSSFWorkbook wb, JSONObject jobj) throws JSONException{
        HSSFSheet sheet = null;
        if (jobj.optBoolean("isPurchase")) {
            sheet = wb.createSheet(jobj.optString("sheetname", "atadj"));
        } else {
            sheet = wb.createSheet("atadj");
        }
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        HSSFCell cell = null;
        CellStyle rowstyle = wb.createCellStyle();
        CellStyle cellstyle = wb.createCellStyle();
        CellStyle amountStyle = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_AT);
        sheet.autoSizeColumn(cellnum);

        cellnum = 2;
        totalHeaderRow = sheet.createRow(rownum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Advance_Adjusted);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Cess);

        /////////// Total Header End ////////////
        cellnum = 2;
        totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(C5:C40000)");
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(amountStyle);
        cell.setCellFormula("SUM(D5:D40000)");

        /////////// Header Start ////////////
         cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Place_Of_Supply);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Gross_Advance_Adjusted);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.CessAmount);
        
        
        JSONArray dataArray = jobj.optJSONArray("atadj");
        for (int index = 0; index < dataArray.length(); index++) {
            JSONObject posObj = dataArray.getJSONObject(index);

            Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
            Map<Double, Double> cessAmount = new HashMap<Double, Double>();
            double taxableAmt = 0, totalTax = 0, totalCess = 0, taxableAmtInv = 0, totalAmt = 0;
            JSONArray itemsarrArray = posObj.getJSONArray("itms");
            for (int itemindex = 0; itemindex < itemsarrArray.length(); itemindex++) {

                JSONObject itemObj = itemsarrArray.getJSONObject(itemindex);
                taxableAmtInv += itemObj.optDouble("txval");
                taxableAmt = itemObj.optDouble("txval");
                totalCess = itemObj.optDouble("csamt");
                Double IGSTAmount = itemObj.optDouble("iamt");
                Double CGSTAmount = itemObj.optDouble("camt");
                Double SGSTAmount = itemObj.optDouble("samt");
                Double CESSAmount = itemObj.optDouble("csamt");
                totalTax += (IGSTAmount + CGSTAmount + SGSTAmount + CESSAmount);
                /*
                * Rate wise Taxable Amount
                */
                if (totaltaxableAmount.containsKey(itemObj.getDouble("rt"))) {
                    double tax = Double.parseDouble(totaltaxableAmount.get(itemObj.getDouble("rt")).toString());
                    taxableAmt = taxableAmt + tax;
                }
                if (cessAmount.containsKey(itemObj.getDouble("rt"))) {
                    double tax = Double.parseDouble(cessAmount.get(itemObj.getDouble("rt")).toString());
                    totalCess = totalCess + tax;
                }
                totaltaxableAmount.put(itemObj.getDouble("rt"), taxableAmt);
                cessAmount.put(itemObj.getDouble("rt"), totalCess);
            }
            totalAmt = (taxableAmtInv + totalTax);


       for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {
                cellnum = 0;
                headerRow = sheet.createRow(rownum++);
                cell = headerRow.createCell(cellnum++);
                cell.setCellValue(getPos(posObj));
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getKey());
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(entry.getValue());
                cell.setCellStyle(amountStyle);
                cell = headerRow.createCell(cellnum++);
                cell.setCellStyle(amountStyle);
                cell.setCellValue(cessAmount.containsKey(entry.getKey()) ? cessAmount.get(entry.getKey()) : 0.00);
            }
        }
    }

    public static void writedatain_efilingExport(HSSFWorkbook eFilingWb, JSONObject jobj) throws JSONException {
        HSSFSheet sheet = eFilingWb.createSheet("exp");
        int rownum = 0;     //Row count
        int cellnum = 0;    //Cell count
        int totalInvoices = 0;
        String typeofinvoice = "EXPORT";
        HSSFCell cell = null;
        CellStyle rowstyle = eFilingWb.createCellStyle();
        CellStyle cellstyle = eFilingWb.createCellStyle();
        CellStyle amountStyle = eFilingWb.createCellStyle();
        CellStyle dateStyle = eFilingWb.createCellStyle();
        DataFormat format = eFilingWb.createDataFormat();
        DataFormat dateFormat = eFilingWb.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("0.00"));
        dateStyle.setDataFormat(dateFormat.getFormat("D-MMM-YYYY"));
        Font font = eFilingWb.createFont();
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

        /////////// Total Header Start ////////////
        HSSFRow totalHeaderRow = sheet.createRow(rownum++);
        cell = totalHeaderRow.createCell(cellnum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Summary_For_EXP);
        sheet.autoSizeColumn(cellnum);

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        totalHeaderRow.setRowStyle((HSSFCellStyle) rowstyle);
        cellnum++;
        cell.setCellStyle(rowstyle);
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_of_Invoices);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Invoice_Value);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.No_ofShipping_Bill);
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellStyle(rowstyle);
        cell.setCellValue(GSTRConstants.Total_Taxable_Value);
        /////////// Total Header End ////////////
        JSONArray cdnrArray = new JSONArray();
        cdnrArray = jobj.optJSONArray("exp");

        cellnum = 0;
        totalHeaderRow = sheet.createRow(rownum++);
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue(cdnrArray.length());
        cell.setCellFormula("SUMPRODUCT((B5:B40001<>\"\")/COUNTIF(B5:B40001,B5:B40001&\"\"))");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUMPRODUCT(1/COUNTIF(B5:B40001,B5:B40001&\"\"),D5:D40001)");
        cellnum++;
        cell = totalHeaderRow.createCell(cellnum++);
//        cell.setCellValue("");
        cell.setCellFormula("SUMPRODUCT((F5:F40001<>\"\")/COUNTIF(F5:F40001,F5:F40001&\"\"))");
        cellnum = cellnum + 2;
        cell = totalHeaderRow.createCell(cellnum++);
        cell.setCellFormula("SUM(I5:I40000)");

        /////////// Header Start ////////////
        cellnum = 0;
        HSSFRow headerRow = sheet.createRow(rownum++);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Export_Type);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.InvoiceDate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Invoice_Value);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Port_Code);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Shipping_Bill_Number);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Shipping_Bill_Date);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Rate);
        cell = headerRow.createCell(cellnum++);
        cell.setCellValue(GSTRConstants.Taxable_Value);

        for (int i = 0; i < cdnrArray.length(); i++) {
            JSONObject cdnr = cdnrArray.getJSONObject(i);
            JSONArray invArray = cdnr.getJSONArray("inv");
            for (int j = 0; j < invArray.length(); j++) {
                totalInvoices++; // counting invoices
                double totalTax = 0, totalCess = 0;
                JSONObject inv = invArray.getJSONObject(j);
                JSONArray itms = inv.getJSONArray("itms");

                Map<Double, Double> totaltaxableAmount = new HashMap<Double, Double>();
                Map<Double, Double> cessAmount = new HashMap<Double, Double>();

                for (int k = 0; k < itms.length(); k++) {
                    JSONObject item = itms.getJSONObject(k);
                    JSONObject itm_det = item.getJSONObject("itm_det");
                    totalTax = itm_det.optDouble("txval");
                    totalCess = itm_det.optDouble("csamt");
                    if (totaltaxableAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(totaltaxableAmount.get(itm_det.getDouble("rt")).toString());
                        totalTax = totalTax + tax;
                    }
                    if (cessAmount.containsKey(itm_det.getDouble("rt"))) {
                        double tax = Double.parseDouble(cessAmount.get(itm_det.getDouble("rt")).toString());
                        totalCess = totalCess + tax;
                    }
                    totaltaxableAmount.put(itm_det.getDouble("rt"), totalTax);
                    cessAmount.put(itm_det.getDouble("rt"), totalCess);
                }
                String exportType = "";
                if (inv.optString("export_type").equals(Constants.CUSTVENTYPE_Export)) {
                    exportType = "WPAY";
                } else if (inv.optString("export_type").equals(Constants.CUSTVENTYPE_ExportWOPAY)) {
                    exportType = "WOPAY";
                }
//                //inserting total tax records in excel
                for (Entry<Double, Double> entry : totaltaxableAmount.entrySet()) {

                    cellnum = 0;
                    headerRow = sheet.createRow(rownum++);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(exportType);
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("inum"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(dateStyle);
                    if(!StringUtil.isNullOrEmpty(inv.optString("idt"))){
                    cell.setCellValue(GSTR1ExportToExcel.getDateFormattedForEfiling(inv.optString("idt")));}
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(inv.optDouble("val"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("sbpcode"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("sbnum"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellValue(inv.optString("sbdt"));
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getKey());
                    cell = headerRow.createCell(cellnum++);
                    cell.setCellStyle(amountStyle);
                    cell.setCellValue(entry.getValue());

                }

            }
        }
    }

    public static Date getDateFormattedForEfiling(String InputDate) {
        SimpleDateFormat sdf_gst = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dbDate = null;
        String response = "";
        try {
            dbDate = sdf.parse(InputDate);
            response = sdf_gst.format(dbDate);
        } catch (ParseException ex) {
            Logger.getLogger(GSTR1ExportToExcel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbDate;
    }

    private static String getPos(JSONObject inv) {
        return String.format("%02d", inv.optInt("stcode")) + "-" + inv.optString("pos");
    }
    /**
     * Added Entity name in Export Sheet 
     * @param jobj
     * @param sheet
     * @param rownum
     * @return 
     */
    static public int addEntityNameCellForGSTRExport(JSONObject jobj, HSSFSheet sheet, int rownum) {
        String entityName = jobj.optString(Constants.entity, "");
        HSSFRow entityHeaderRow = sheet.createRow(rownum++);
        HSSFCell cell = entityHeaderRow.createCell(0);
        cell.setCellValue(Constants.ENTITY_NAME_EXPORT);
        cell = entityHeaderRow.createCell(1);
        cell.setCellValue(entityName);
        return rownum;
    }
}
