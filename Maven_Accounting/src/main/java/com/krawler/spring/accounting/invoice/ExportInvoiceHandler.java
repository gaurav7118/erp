/*/*
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
package com.krawler.spring.accounting.invoice;

import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.Projreport_Template;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.common.util.Constants;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;

public class ExportInvoiceHandler {

    private static final long serialVersionUID = -763555229410947890L;
    private static Font fontSmallRegular = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Times New Roman", 10, Font.BOLD, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Times New Roman", 12, Font.BOLD, Color.BLACK);
    private static Font fontTblMediumBold = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.GRAY);
    private static Font fontTbl = FontFactory.getFont("Times New Roman", 16, Font.NORMAL, Color.GRAY);
    private static Font fontMediumBold1 = FontFactory.getFont("Times New Roman", 11, Font.BOLD, Color.BLACK);
    private static Font fontSmallRegular1 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold1 = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    private static String imgPath = "";
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
//    private accTaxDAO accTaxObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCurrencyDAO accCurrencyobj;
    private exportMPXDAOImpl exportDaoObj;
    private accTaxDAO accTaxObj;
    private MessageSource messageSource;
    private static com.krawler.utils.json.base.JSONObject config = null;
    private PdfPTable header = null;
    private PdfPTable footer = null;
    private static String companyName = "";
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private PdfPTable mainTableGlobal = null;
    private int productHeaderTableGlobalNo = 0;
    private PdfPTable tableClosedLineGlobal = null;
    private static String globalCurrencyValue = "";
    private static boolean isFromProductTable = false;
    private static boolean isAttachProductTable = false;
    private String[] globalTableHeader;
    private static String linkHeader = "";
    private static String CompanyPDFFooter = "";
    private static String CompanyPDFHeader = "";
     private static String CompanyPDFPRETEXT="";
    private static String CompanyPDFPOSTTEXT="";    
    
    private static FontFamilySelector fontFamilySelector=new FontFamilySelector();
    static {
    	FontFamily fontFamily=new FontFamily();
    	fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Times New Roman", 14, Font.NORMAL, Color.BLACK));
    	fontFamily.addFont(FontContext.REPORT_TITLE, FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK));
    	fontFamily.addFont(FontContext.SMALL_TEXT, fontSmallBold);
    	fontFamily.addFont(FontContext.TABLE_HEADER, fontMediumBold);
    	fontFamily.addFont(FontContext.TABLE_DATA, fontSmallRegular);
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
	for(File file:files){
		try {
				BaseFont bfnt = BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				fontFamily=new FontFamily();
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

    public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                try {
                    getHeaderFooter(document);
                } catch (ServiceException ex) {
                }
               try {
                    // Add page header
    //                header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
    //                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());
                    addHeaderContents(document,writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportInvoiceHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportInvoiceHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (mainTableGlobal != null) {

                    mainTableGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    mainTableGlobal.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 33, writer.getDirectContent());
                    mainTableGlobal = null;
                }

                if (tableClosedLineGlobal != null) {
                    tableClosedLineGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    tableClosedLineGlobal.writeSelectedRows(0, 10, document.leftMargin(), document.bottomMargin() + 45, writer.getDirectContent());
                    tableClosedLineGlobal = null;
                }
               // Add page footer
//                footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
//                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());
                try {
                    addHeaderFooter(document, writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportInvoiceHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportInvoiceHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Add page border
                if (config != null && config.getBoolean("pageBorder")) {
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

            	public void getHeaderFooter(Document document) throws ServiceException {
        try {
            
            java.util.Date dt = new java.util.Date();
            String date = "yyyy-MM-dd";
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
//            dtf.setTimeZone(TimeZone.getTimeZone("GMT"+this.tdiff));
            String DateStr = dtf.format(dt);
            java.awt.Color tColor=null;
        if(config!=null){
            tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
            // -------- header ----------------
            header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{20, 60,20});
            String HeadDate = "";
            if (config.getBoolean("headDate")) {
                HeadDate = DateStr;
            }
            PdfPCell headerDateCell = new PdfPCell(fontFamilySelector.process(HeadDate, FontContext.SMALL_TEXT,tColor));//fontSmallRegular));
            headerDateCell.setBorder(0);
            headerDateCell.setPaddingBottom(4);
            headerDateCell.setHorizontalAlignment(PdfCell.ALIGN_LEFT);
            header.addCell(headerDateCell);

            PdfPCell headerNotecell = new PdfPCell(fontFamilySelector.process(config.getString("headNote"), FontContext.HEADER_NOTE,tColor));
            headerNotecell.setBorder(0);
            headerNotecell.setPaddingBottom(4);
            headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            header.addCell(headerNotecell);

            String HeadPager = "";
            if (config.getBoolean("headPager")) {
                HeadPager = String.valueOf(document.getPageNumber());//current page no
            }
            PdfPCell headerPageNocell = new PdfPCell(fontFamilySelector.process(HeadPager,FontContext.HEADER_NOTE,tColor));// fontSmallRegular));
            headerPageNocell.setBorder(0);
            headerPageNocell.setPaddingBottom(4);
            headerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            header.addCell(headerPageNocell);

                PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
                headerSeparator.setBorder(PdfPCell.BOX);
                headerSeparator.setPadding(0);
                headerSeparator.setColspan(3);
                header.addCell(headerSeparator);
            }     
            PdfPCell invcell = null;
            
            if(isAttachProductTable){
                mainTableGlobal = new PdfPTable(1);
                PdfPTable table=getBlankTableForReport();
                addTableHeader(globalCurrencyValue, mainTableGlobal,table,invcell);
                addTableRowGlobal(mainTableGlobal, table); //Break table after adding header row                
                isAttachProductTable = false;
            }
            
            // -------- header end ----------------

            // -------- footer  -------------------
            if(isFromProductTable){
                isAttachProductTable = true;
                tableClosedLineGlobal = new PdfPTable(1);
                PdfPTable table=getBlankTableForReport();
                addingFooterClosedLine(table);
                addTableRow(tableClosedLineGlobal, table); //Break table after adding extra space
                
            }
            footer = new PdfPTable(3);
                PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
                footerSeparator.setBorder(PdfPCell.BOX);
                footerSeparator.setPadding(0);
                footerSeparator.setColspan(3);
                footer.addCell(footerSeparator);
            footer.setWidthPercentage(100);
            footer.setWidths(new float[]{20, 60,20});
            String PageDate = "";
             if(config!=null){ 
            if (config.getBoolean("footDate")) {
                PageDate = DateStr;
            }
            PdfPCell pagerDateCell = new PdfPCell(fontFamilySelector.process(PageDate, FontContext.SMALL_TEXT,tColor));//fontSmallRegular));
            pagerDateCell.setBorder(0);
            pagerDateCell.setHorizontalAlignment(PdfCell.ALIGN_LEFT);
            footer.addCell(pagerDateCell);
            
            PdfPCell footerNotecell = new PdfPCell(fontFamilySelector.process(config.getString("footNote"),FontContext.FOOTER_NOTE,tColor));// fontSmallRegular));
            footerNotecell.setBorder(0);
            footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            footer.addCell(footerNotecell);

            String FootPager = "";
            if (config.getBoolean("footPager")) {
                FootPager = String.valueOf(document.getPageNumber());//current page no
            }
                PdfPCell footerPageNocell = new PdfPCell(fontFamilySelector.process(FootPager,FontContext.SMALL_TEXT,tColor));// fontSmallRegular));
                footerPageNocell.setBorder(0);
                footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
                footer.addCell(footerPageNocell);
             }       
        // -------- footer end   -----------
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.getHeaderFooter", e);
        }
    }
                
    public void addingFooterClosedLine(PdfPTable table) {
        PdfPCell invcell = null;
        if (productHeaderTableGlobalNo == 4) {
            for (int i = 1; i <= 60; i++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                if (i > 55) {
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                }
                table.addCell(invcell);
            }
        } else if (productHeaderTableGlobalNo == 2) {
            for (int j = 1; j <= ((linkHeader.equalsIgnoreCase("")) ? 66 : 77); j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                if (j > ((linkHeader.equalsIgnoreCase("")) ? 60 : 70)) {
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                }
                table.addCell(invcell);
            }
        } else if (productHeaderTableGlobalNo == 3) {
            for (int j = 0; j < 60; j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(invcell);
            }
             for (int j = 0; j < 6; j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.BOTTOM + Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(invcell);
            }
        } else {
            for (int j = 1; j <= ((!linkHeader.equalsIgnoreCase("")) ? 70 : 60); j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                if (j > ((!linkHeader.equalsIgnoreCase("")) ? 63 : 54)) {
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                }
                table.addCell(invcell);
            }
        }
//            addTableRow(mainTable, table); //Break table after adding extra space
    }
    public void addTableRowGlobal(PdfPTable container, PdfPTable table) {
         PdfPCell tableRow = new PdfPCell(table);
        tableRow.setBorder(0);
        tableRow.setPaddingRight(10);
        tableRow.setPaddingLeft(10);
        container.addCell(tableRow);
    }        
    public void addTableHeader(String currencyValue, PdfPTable mainTable,PdfPTable table,PdfPCell invcell) throws DocumentException, SessionExpiredException {
      
//        PdfPTable table = getBlankTable();
//        table.setWidthPercentage(100);

          for (int i = 0; i < globalTableHeader.length; i++) {
            invcell = new PdfPCell(new Paragraph(globalTableHeader[i], fontSmallBold));
            invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            invcell.setBackgroundColor(Color.LIGHT_GRAY);
//            invcell.setBorder(1);
            invcell.setPadding(3);
           if(productHeaderTableGlobalNo==4) 
            invcell.setPaddingBottom(10);
            table.addCell(invcell);
        }
//        for (int i = 0; i < globalTableHeader.length; i++) {
//            invcell = new PdfPCell(new Paragraph(fontFamilySelector.process(globalTableHeader[i], FontContext.SMALL_TEXT)));
//            invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            invcell.setBackgroundColor(Color.LIGHT_GRAY);
//            invcell.setBorder(Rectangle.BOX);
//            invcell.setPadding(3);
//            table.addCell(invcell);
//        }
//        addTableRow(mainTable, table); //Break table after adding header row
//        table = getBlankTable();
    }
    public ByteArrayOutputStream createPdf(HttpServletRequest request, JSONArray DataJArr, 
            Company com, DateFormat formatter,String logoPath, CompanyAccountPreferences companyAccountPreferences, Projreport_Template defaultTemplate) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        Document document = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            double total = 0;
            String poRefno = "";
            DateFormat df = authHandler.getDateOnlyFormat(request);
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.ddMMyyyy);
            Date startdate = null;
            String asofdate=null;
            boolean isAged = request.getParameter("isAged")!=null?Boolean.parseBoolean(request.getParameter("isAged")):false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate"))) {
                startdate = df.parse((String) request.getParameter("startdate"));
            } else {
                if (isAged) {
                    startdate = df.parse(Constants.opening_Date);
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("asofdate"))) {
                asofdate = sdf.format(authHandler.getGlobalDateFormat().parse(request.getParameter("asofdate")));
                }
            int dateFilter = request.getParameter("datefilter")!=null?Integer.parseInt(request.getParameter("datefilter").toString()):0;
            String enddate=sdf.format(authHandler.getGlobalDateFormat().parse(request.getParameter("enddate")));
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 55, 30);
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ExportInvoiceHandler.EndPage());
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);
            PdfPTable blankTable = null;
            PdfPCell blankCell = null;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            boolean isCompanyLogo = true;
            boolean isCompanyTemplateLogo = false;
            PdfPTable tab3 = null;
            String postText = "";
            String approverName = "______________________________";
            String preText = "";
//            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            Rectangle page = document.getPageSize();
            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

//            String companyIdForCAP = sessionHandlerImpl.getCompanyid(request);
//            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyIdForCAP);
            CompanyPDFFooter = companyAccountPreferences.getPdffooter();
            CompanyPDFFooter=companyAccountPreferences.getPdffooter();
            CompanyPDFHeader=companyAccountPreferences.getPdfheader();
//            Projreport_Template defaultTemplate = (Projreport_Template) kwlCommonTablesDAOObj.getClassObject(Projreport_Template.class.getName(), Constants.HEADER_IMAGE_TEMPLATE_ID);
            if (defaultTemplate != null) {
                config = new com.krawler.utils.json.base.JSONObject(defaultTemplate.getConfigstr());
            }
            Rectangle recPage = new Rectangle(PageSize.A4);
            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
            document = new Document(recPage, 15, 15, 55, 30);
//            rec = document.getPageSize();
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ExportInvoiceHandler.EndPage());
            document.open();
            isCompanyLogo = true;
            isCompanyTemplateLogo = true;
            if (!checkCompanyTemplateLogoPresent(com)) {
                isCompanyTemplateLogo = false;
                isCompanyLogo = true;
            }
            addHeaderFooter(document, writer);
            String company[] = new String[4];
            company[0] = com.getCompanyName();
            company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
            company[2] = com.getEmailID();
            company[3] = com.getPhoneNumber();

            PdfPTable table1 = new PdfPTable(2);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{50, 50});
                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = addCompanyLogo(logoPath, com.getCompanyName());
                    }else{
                        tab1 = new PdfPTable(1);
                    }
                }

            tab2 = new PdfPTable(1);
            PdfPCell invCell = null;

//            CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
//            Account cash = companyAccountPreferences.getCashAccount();

            String theader = "";
            String datetheader = theader;
            invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
            tab2.addCell(invCell);
            PdfPTable tab4 = getDateTable(startdate,enddate,asofdate,dateFilter,datetheader,formatter,isAged);
            PdfPTable userTable2 = new PdfPTable(2);
            userTable2.setWidthPercentage(100);
            userTable2.setWidths(new float[]{60, 40});
//            tab3 = getCompanyInfo(company);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            PdfPCell cel2 = new PdfPCell(tab2);
            cel2.setBorder(0);
            table1.addCell(cel2);

            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);
            
            String customerName = "";
            String billAddr = "";
            String customerEmail = "";
            String currenySymbol = "";
            String currenyIdVal = "";
            
            if(DataJArr.length()>0) {
                JSONObject custInfoObj = DataJArr.getJSONObject(0);
                customerName =custInfoObj.has("personname")&&!StringUtil.isNullOrEmpty(custInfoObj.getString("personname"))?custInfoObj.getString("personname"):"";
                billAddr = custInfoObj.has("personAddress")&&!StringUtil.isNullOrEmpty(custInfoObj.getString("personAddress"))?custInfoObj.getString("personAddress"):"";
                currenySymbol = custInfoObj.has("currencysymbol")&&!StringUtil.isNullOrEmpty(custInfoObj.getString("currencysymbol"))?custInfoObj.getString("currencysymbol"):"";
                currenyIdVal = custInfoObj.has("currencyidval")&&!StringUtil.isNullOrEmpty(custInfoObj.getString("currencyidval"))?custInfoObj.getString("currencyidval"):"";
//                authHandlerDAOObj.getCurrency(currenyId);
//                customerEmail = custInfoObj.getString("");
            }
            userTable2.addCell(invCell);            
            theader = "STATEMENT OF ACCOUNT";
             tab2 = new PdfPTable(1);
            invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);            
            userTable2.addCell(invCell);
            
            PdfPTable addressMainTable = getAddressTable(customerName, billAddr, customerEmail);
            cell1 = new PdfPCell(addressMainTable);
            cell1.setBorder(0);
            userTable2.addCell(cell1);
            cel2 = new PdfPCell(tab4);
            cel2.setBorder(0);
            
            userTable2.addCell(cel2);

            PdfPCell mainCell12 = new PdfPCell(userTable2);
            mainCell12.setBorder(0);
            mainCell12.setPaddingTop(10);
            mainCell12.setPaddingLeft(10);
            mainCell12.setPaddingRight(10);
//            if (!isCompanyTemplateLogo && !(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER)) {
//                mainTable.addCell(mainCell12);
//            }

            

//            mainCell12 = new PdfPCell(addressMainTable);
//            mainCell12.setBorder(0);
//            mainCell12.setPaddingTop(5);
//            mainCell12.setPaddingLeft(10);
//            mainCell12.setPaddingRight(10);
            mainCell12.setPaddingBottom(15);
            mainTable.addCell(mainCell12);

//            String referenceNumber = getReferenceNumber(idresult.getEntityList(), mode);
//            PdfPTable detailsTable = getDetailsTable(headerDetails, referenceNumber, terms, dueDate, shipDate, formatter, isInclude, shipvia, fob);
//
//            mainCell12 = new PdfPCell(detailsTable);
//            mainCell12.setBorder(0);
//            mainCell12.setPaddingTop(5);
//            mainCell12.setPaddingLeft(10);
//            mainCell12.setPaddingRight(10);
//            mainCell12.setPaddingBottom(5);
//            mainTable.addCell(mainCell12);

            List<String> headerList = new ArrayList<String>();
            headerList.add("Transaction Date");
            headerList.add("Due Date");
            headerList.add("Document No");
            headerList.add("Due Amount" + " "+currenyIdVal);
            
//            headerList.add(messageSource.getMessage("acc.rem.34", null, RequestContextUtils.getLocale(request)));
//            headerList.add(messageSource.getMessage("acc.invoiceList.due", null, RequestContextUtils.getLocale(request)));
//            headerList.add(messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request)));
//            headerList.add(messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request)));
            String[] header = (String[]) headerList.toArray(new String[0]);
            PdfPTable table = null;
            table = getBlankTableForReport();
            
            PdfPCell invcell = null;
            for (int i = 0; i < header.length; i++) {
                invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBackgroundColor(Color.LIGHT_GRAY);
                invCell.setBorder(0);
                invcell.setPadding(3);
                table.addCell(invcell);
            }
            addTableRow(mainTable, table); //Break table after adding header row
            table = getBlankTableForReport();
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue9 = 0;
            double totaldues = 0;
            DataJArr = AccountingManager.sortJsonArrayOnDateValues(DataJArr,df,"date",true);
            for(int cnt=0;cnt<DataJArr.length();cnt++) {
                JSONObject custInfoObj = DataJArr.getJSONObject(cnt);
                String invoiceDate = !StringUtil.isNullOrEmpty(custInfoObj.getString("date"))||!custInfoObj.getString("date").equals("") ?formatter.format(new Date(custInfoObj.getString("date"))):"";
                String duedate = !StringUtil.isNullOrEmpty(custInfoObj.getString("duedate"))||custInfoObj.getString("duedate")!=""?formatter.format(new Date(custInfoObj.getString("duedate"))):"";
                String invoiceno = custInfoObj.getString("billno");
                double dueamount = Double.parseDouble(custInfoObj.getString("amountdueinbase"));
                invcell = createCell(invoiceDate, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                table.addCell(invcell);   
                invcell = createCell(duedate, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                table.addCell(invcell);
                invcell = createCell(invoiceno, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                table.addCell(invcell);   
                invcell = createCell(authHandler.formattedAmount(dueamount, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                table.addCell(invcell);
                addTableRow(mainTable, table);
                table = getBlankTableForReport();
                amountdue1 += Double.parseDouble(custInfoObj.getString("amountdueinbase1"));
                amountdue2 += Double.parseDouble(custInfoObj.getString("amountdueinbase2"));
                amountdue3 += Double.parseDouble(custInfoObj.getString("amountdueinbase3"));
                amountdue4 += Double.parseDouble(custInfoObj.getString("amountdueinbase4"));
                amountdue9 += Double.parseDouble(custInfoObj.getString("amountdueinbase9"));
                totaldues = amountdue1 + amountdue2 + amountdue3 + amountdue4 + amountdue9;
            }
            for (int j = 1; j <= 60; j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);                    
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                if(j>=57){
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT+Rectangle.BOTTOM);
                }
                table.addCell(invcell);
            }
            
            addTableRow(mainTable, table); //Break table after adding extra space
            
            // add table with diff cell configuration
            table = new PdfPTable(6);
            table.setWidthPercentage(100);        
            table.setWidths(new float[]{15,15,18,18,19,15});
            
            headerList = new ArrayList<String>();
            
            int duration = (!StringUtil.isNullOrEmpty(request.getParameter("duration"))) ? Integer.parseInt(request.getParameter(InvoiceConstants.duration)) : 0;
            
            headerList.add("Current");
            if (dateFilter == 2) {
                headerList.add("0"+ "-"+ duration +" Days");
            } else {
                headerList.add("1"+ "-"+ duration +" Days");
            }
            headerList.add((duration + 1) + "-"+ duration * 2 +" Days");     //            headerList.add("31-60 Days");
            headerList.add(((duration*2) + 1) + "-"+ duration * 3 +" Days"); //            headerList.add("61-90 Days");
            headerList.add("Over "+ duration * 3 +" Days" );                 //            headerList.add("Over 90 Days");
            headerList.add("Total Dues");
            header = (String[]) headerList.toArray(new String[0]);
            for (int i = 0; i < header.length; i++) {
                invcell = new PdfPCell(new Paragraph(header[i], fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBackgroundColor(Color.LIGHT_GRAY);
                invCell.setBorder(0);
                invcell.setPadding(3);
                table.addCell(invcell);
            }
            addTableRow(mainTable, table);
            
            table = new PdfPTable(6);
            table.setWidthPercentage(100);        
            table.setWidths(new float[]{15,15,18,18,19,15});
            
            invcell = createCell(authHandler.formattedAmount(amountdue1, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            table.addCell(invcell);   
            invcell = createCell(authHandler.formattedAmount(amountdue2, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            table.addCell(invcell);
            invcell = createCell(authHandler.formattedAmount(amountdue3, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            table.addCell(invcell);   
            invcell = createCell(authHandler.formattedAmount(amountdue4, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            if(amountdue4>0){
             invcell.setBackgroundColor(Color.YELLOW);   
            }
            table.addCell(invcell);
            invcell = createCell(authHandler.formattedAmount(amountdue9, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            if(amountdue9>0){
             invcell.setBackgroundColor(Color.YELLOW);   
            }
            table.addCell(invcell);
            invcell = createCell(authHandler.formattedAmount(totaldues, companyid), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT +Rectangle.BOTTOM, 5);
            table.addCell(invcell);
            addTableRow(mainTable, table);
            // END 
            
            if (amountdue4>0 || amountdue9>0) {
                table = new PdfPTable(1);
                table.setWidthPercentage(100);
                invcell = new PdfPCell(new Paragraph("The amount highlighted has been long overdue, please arrange for settlement of this account immediately. ", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_LEFT);
                invcell.setBorder(0);
                invcell.setPaddingTop(10);
                invcell.setPaddingLeft(10);
                table.addCell(invcell);
                addTableRow(mainTable, table);
            }
            
            isFromProductTable = true;
            document.add(mainTable);
            document.getPageNumber();
            isFromProductTable = false;
            mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);
            document.add(mainTable);
            
             return baos;
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportMPXDAOImpl.getPdfData", e);
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
             if (baos != null) {
                baos.close();
            }
        }
       
    }
    
    @Deprecated
    public String getFormattedAmount(double value) {
        String str=authHandler.getCompleteDFStringForAmount("#,##0.");
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }
    public void addTableRow(PdfPTable container, PdfPTable table) {
        PdfPCell tableRow = new PdfPCell(table);
        tableRow.setBorder(0);
        tableRow.setPaddingRight(10);
        tableRow.setPaddingLeft(10);
        container.addCell(tableRow);
    }
    public PdfPTable getBlankTableForReport() throws DocumentException{
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);        
        table.setWidths(new float[]{15,15,55,15});
        return table;
    }
    public PdfPTable getDateTable(Date startdate,String enddate,String asofdate,int dateFilter,String theader,DateFormat formatter,boolean isAged) throws DocumentException {
        PdfPTable tab4 = new PdfPTable(2);
        tab4.setWidthPercentage(100);
        tab4.setWidths(new float[]{50, 50});
        PdfPCell cell2 = null;
        PdfPCell cell3 = null;
        String agedon = "  Aged On :";
        if(!isAged){
            cell2 = createCell("From :  " + formatter.format(startdate), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.BOX, 5);
            tab4.addCell(cell2);
        } else {
            cell2 = createCell("As of :  " +asofdate, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.BOX, 5);
        tab4.addCell(cell2);
        }
        cell2 = createCell("To  :  " +enddate, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.BOX, 5);
        tab4.addCell(cell2);
        /*Added blank row for height adjustment of other rows*/
        cell2 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 0);
        tab4.addCell(cell2);
        
        
        cell2 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 0);
        tab4.addCell(cell2);
      
        //cell for Aged on;
        if (dateFilter == Constants.agedDueDate1to30Filter) {
            agedon += Constants.agedDueDate1to30Days;
        } else if (dateFilter == Constants.agedInvoiceDateFilter) {
            agedon += Constants.agedInvoiceDate;
        }else if (dateFilter == Constants.agedInvoiceDate0to30Filter) {
            agedon += Constants.agedInvoiceDate0to30;
        } else {
            agedon += Constants.agedDueDate0to30Days;
        }
        cell3 = createCell(agedon, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.BOX, 5);
        cell3.setColspan(2);
        tab4.addCell(cell3); 
        return tab4;
    }
    public Boolean checkCompanyTemplateLogoPresent(Company com) {
        boolean isPresent=true;  
        PdfPCell imgCell = new PdfPCell();
        try {
            String tempImagePath = storageHandlerImpl.GetDocStorePath() + com.getCompanyID() + "_template.png";
            Image img = Image.getInstance(tempImagePath);
        } catch (Exception e) {
            isPresent=false;
        }
       
        return isPresent;
    }
        private PdfPCell createCell(String string, Font fontTbl, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(string, fontTbl));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }

    private PdfPCell createCell(String string, FontContext context, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(string, context)));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }
    private void addHeaderFooter(Document document, PdfWriter writer) throws DocumentException, ServiceException {
        PdfPTable footer = new PdfPTable(1);
        PdfPCell footerSeparator = new PdfPCell(new Paragraph("THANK YOU FOR YOUR BUSINESS!", fontTblMediumBold));
        if(!StringUtil.isNullOrEmpty(CompanyPDFFooter)){
            String footerText= CompanyPDFFooter.replaceAll("(\\r|\\n)", "");
            footerSeparator = new PdfPCell(new Paragraph(footerText, fontTblMediumBold));
        }   
        footerSeparator.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerSeparator.setBorder(1);
        footerSeparator.setPaddingBottom(1);
        footer.addCell(footerSeparator);

        try {
            Rectangle page = document.getPageSize();            
            footer.setTotalWidth(page.getWidth() - document.leftMargin()- document.rightMargin());
            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin()+3, writer.getDirectContent());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
     private void addHeaderContents(Document document, PdfWriter writer) throws DocumentException, ServiceException {
        PdfPTable header = new PdfPTable(1);
        PdfPCell headerSeparator = new PdfPCell(new Paragraph("", fontTblMediumBold));
        if(!StringUtil.isNullOrEmpty(CompanyPDFHeader)){
             String headerText= CompanyPDFHeader.replaceAll("(\\r|\\n)", "");
            headerSeparator = new PdfPCell(new Paragraph(headerText, fontTblMediumBold));
        }
        headerSeparator.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerSeparator.setBorder(Rectangle.BOTTOM);
        headerSeparator.setPaddingBottom(5);
        header.addCell(headerSeparator);

        try {
            Rectangle page = document.getPageSize();
            header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight()-10, writer.getDirectContent());           
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public PdfPTable getCompanyInfo(String com[]) {
        PdfPTable tab1 = new PdfPTable(1);
        tab1.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell cell = new PdfPCell(new Paragraph(com[0], fontMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(0);
        tab1.addCell(cell);
        for (int i = 1; i < com.length; i++) {
            cell = new PdfPCell(new Paragraph(com[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            tab1.addCell(cell);
        }
        return tab1;
    }

    private PdfPTable addCompanyLogo(String logoPath, String comName) {
        PdfPTable tab1 = new PdfPTable(1);
        imgPath = logoPath;
        PdfPCell imgCell = null;
        try {

            if (imgPath.contains("logo.gif")) {
                imgCell = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            } else {
                Image img = Image.getInstance(imgPath);
                imgCell = new PdfPCell(img);

            }
        } catch (Exception e) {
            imgCell = new PdfPCell(new Paragraph(comName, fontSmallRegular));
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgCell.setBorder(0);
        tab1.addCell(imgCell);
        return tab1;
    }
    public PdfPTable addCompanyTemplateLogo(String logoPath, Company com) {
        PdfPTable tab1 = new PdfPTable(1);
        imgPath = logoPath;

        PdfPCell imgCell = new PdfPCell();
        try {
            String tempImagePath = storageHandlerImpl.GetDocStorePath() + com.getCompanyID() + "_template.png";
            Image img = Image.getInstance(tempImagePath);    
            if (img.getWidth() < 400) {
                img.scaleToFit(img.getWidth(), img.getHeight());
            } else {
                img.scaleToFit(400, 74);
            }
            imgCell.addElement((Element) img);
            imgCell = new PdfPCell(img);
        } catch (Exception e) {
            tab1 = addCompanyLogo(logoPath, com.getCompanyName());
            return tab1;
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgCell.setBorder(0);
        tab1.addCell(imgCell);
        return tab1;
    }   

        public PdfPTable getAddressTable(String customerName, String billingAddress,String customerEmail) throws DocumentException {        
            PdfPTable addressMainTable = new PdfPTable(1);
            addressMainTable.setWidthPercentage(100);
            addressMainTable.setWidths(new float[]{50});

            PdfPTable billToTable = new PdfPTable(1);
            billToTable.setWidthPercentage(100);
            billToTable.setWidths(new float[]{100});

            
            PdfPCell cell2 = new PdfPCell(new Paragraph("To", fontMediumBold));
            cell2.setBorder(0);
            billToTable.addCell(cell2);
            cell2 = new PdfPCell(new Paragraph(customerName, fontTblMediumBold));
            cell2.setBorder(0);
            billToTable.addCell(cell2);
            cell2 = new PdfPCell(new Paragraph(billingAddress, fontTblMediumBold));
            cell2.setBorder(0);
            billToTable.addCell(cell2);
            cell2 = new PdfPCell(new Paragraph(customerEmail, fontTblMediumBold));
            cell2.setBorder(0);
            billToTable.addCell(cell2);
            cell2 = new PdfPCell(new Paragraph("", fontTblMediumBold));
            cell2.setBorder(0);
            billToTable.addCell(cell2);

            PdfPCell cell1 = new PdfPCell(billToTable);
            cell1.setBorder(0);
            addressMainTable.addCell(cell1);

            return addressMainTable;
        }
}
