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
package com.krawler.spring.exportFuctionality;

import java.io.OutputStream;
import java.util.*;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.jasperreports.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
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
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpServletResponse;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.pdf.PdfPageEventHelper;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.jasperreports.FinanceDetails;
import com.krawler.spring.accounting.reports.accOtherReportsController;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import javax.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

public class ExportRecord implements MessageSourceAware{

    private static final long serialVersionUID = -763555229410947890L;
    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    private static Font fontMediumRegular = FontFactory.getFont("Helvetica", 11, Font.NORMAL, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    private static Font fontMediumBold1 = FontFactory.getFont("Helvetica", 11, Font.BOLD, Color.BLACK);
    private static Font fontSmallRegular1 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold1 = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    private static Font fontXSmallRegular1 = FontFactory.getFont("Helvetica", 8, Font.NORMAL, Color.BLACK);
    private static Font fontXSmallBold1 = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private exportMPXDAOImpl exportDaoObj;
    private MessageSource messageSource;
    private BaseDAO baseDAO;
    private static com.krawler.utils.json.base.JSONObject config = null;
    private PdfPTable header = null;
    private PdfPTable footer = null;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private PdfPTable mainTableGlobal = null;
    private int  productHeaderTableGlobalNo = 0;
    private PdfPTable tableClosedLineGlobal = null;
    private static String globalCurrencyValue = "";
    private static boolean isFromProductTable = false;
    private static boolean isAttachProductTable = false;
    private String[] globalTableHeader;
    private static String linkHeader="";
    private static String CompanyPDFFooter="";
    private static String CompanyPDFHeader="";

	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
        
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setBaseDAO(BaseDAO baseDAO) {
        this.baseDAO = baseDAO;
    }
    

    private static FontFamilySelector fontFamilySelector=new FontFamilySelector();
    static{
    	FontFamily fontFamily=new FontFamily();
    	fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Helvetica", 14, Font.NORMAL, Color.BLACK));
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



    public ByteArrayOutputStream exportRatioAnalysis(HttpServletRequest request,JSONObject jobj,String logoPath,String comName)  throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {
            //flag 1 = BalanceSheet , 2 = P&L

            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

//            ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);

            PdfPTable table1 = new PdfPTable(4);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{25,25,25,25});


            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell headerCell =  createCell(messageSource.getMessage("acc.ra.tabTT", null, RequestContextUtils.getLocale(request)), FontContext.TABLE_HEADER, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);



            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);




            JSONArray ObjArr = jobj.getJSONArray("data");

            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.ra.principalGroups", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthLeft(1);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell1.setBorderWidthTop(1);
            PdfPCell HeaderCell2 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            HeaderCell2.setBorderWidthTop(1);

            PdfPCell HeaderCell3 = createCell(messageSource.getMessage("acc.ra.principalRatios", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_LEFT, 0, 5);
            HeaderCell3.setBorderWidthBottom(1);
            HeaderCell3.setBorderWidthTop(1);
            HeaderCell3.setBorderWidthLeft(1);
            PdfPCell HeaderCell4 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell4.setBorderWidthBottom(1);
            HeaderCell4.setBorderWidthRight(1);
            HeaderCell4.setBorderWidthTop(1);
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            table1.addCell(HeaderCell3);
            table1.addCell(HeaderCell4);
            PdfPCell objCell1 = null;
            PdfPCell objCell2 = null;
            PdfPCell objCell3 = null;
            PdfPCell objCell4 = null;
            int objArrLength = ObjArr.length();
            for(int i=0;i<objArrLength;i++){
                JSONObject leftObj = ObjArr.getJSONObject(i);
                objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5,0);
                objCell2 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lvalue").equals("")?"N/A":leftObj.getString("lvalue"), FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
                objCell3 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("rname"), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5,0);
                objCell4 = ExportRecordHandler.createBalanceSheetCell((leftObj.getString("rvalue").equals("") && !leftObj.getString("rname").equals(""))?"N/A":leftObj.getString("rvalue"), FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
                objCell1.setBorderWidthLeft(1);
                objCell3.setBorderWidthLeft(1);
                objCell4.setBorderWidthRight(1);
                if(i!=(objArrLength-1)){
                    table1.addCell(objCell1);
                    table1.addCell(objCell2);
                    table1.addCell(objCell3);
                    table1.addCell(objCell4);
                }
            }
            objCell1.setBorderWidthBottom(1);
            objCell2.setBorderWidthBottom(1);
            objCell3.setBorderWidthBottom(1);
            objCell4.setBorderWidthBottom(1);
            table1.addCell(objCell1);
            table1.addCell(objCell2);
            table1.addCell(objCell3);
            table1.addCell(objCell4);

            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);





            document.add(mainTable);
        } catch (Exception ex) {
            return null;
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
           return baos;
    }

    public ByteArrayOutputStream exportBalanceSheetPdf(HttpServletRequest request, String currencyid,DateFormat formatter, String logoPath,String comName,JSONObject jobj,Date startDate,Date endDate,int flag, int toggle, String address,Date endPreDate) throws DocumentException, ServiceException, IOException {
       ByteArrayOutputStream baos = null;
        double total = 0;
        boolean periodView=Boolean.parseBoolean(request.getParameter("periodView"));
        Document document = null;
        PdfWriter writer = null;
        try {
            //flag 1 = BalanceSheet , 2 = P&L
            boolean isAlignment =Boolean.parseBoolean((String)request.getParameter("isAlignment"));                            
            boolean isCompare = request.getParameter("isCompareGlobal")!=null?Boolean.parseBoolean((String)request.getParameter("isCompareGlobal")):false;            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date stDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("stdate"));
            
            ExtraCompanyPreferences extrapref = null;
            boolean isShowAccountCode = false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                isShowAccountCode = extrapref.isShowAccountCodeInFinancialReport();
            }
            
            request.setAttribute("isShowAccountCode", isShowAccountCode);;
            String headingString = "";
            String DateheadingString   = "";
            String value = "";
            String prevalue = "";
            String subHeading1 = "";
            String subHeadingPre1 = "";
            String subHeading2 = "";
            String subHeading3="";
            String startPreDateValue = !StringUtil.isNullOrEmpty(request.getParameter("stpredate"))?formatter.format(authHandler.getDateOnlyFormat().parse(request.getParameter("stpredate"))):"";
            String startDateValue = startDate!=null ? formatter.format(startDate):"";
            if(flag==1){
                if(periodView){
                    subHeading1="Opening Amount";
                    subHeading2="Period Amount";
                    subHeading3="Ending Amount";
                }else{
                    headingString = messageSource.getMessage("acc.rem.123", null, RequestContextUtils.getLocale(request)); //"Balance Sheet For : ";
                    DateheadingString =messageSource.getMessage("acc.rem.124", null, RequestContextUtils.getLocale(request)); //"Balance Sheet Till :";
                    value = endDate!=null?formatter.format(endDate):"";
                    prevalue = endPreDate!=null?formatter.format(endPreDate):"";
                    subHeading1 = !StringUtil.isNullOrEmpty(startDateValue)? startDateValue +" to "+value : value; //messageSource.getMessage("acc.balanceSheet.Amount(asset)", null, RequestContextUtils.getLocale(request));   //"Asset";
                    subHeadingPre1 = !StringUtil.isNullOrEmpty(startPreDateValue)? startPreDateValue +" to "+prevalue : prevalue; //messageSource.getMessage("acc.balanceSheet.Amount(asset)", null, RequestContextUtils.getLocale(request));   //"Asset";
                    subHeading2 = "";//messageSource.getMessage("acc.balanceSheet.Amount(liability)", null, RequestContextUtils.getLocale(request));   //"Liability";
                    if(toggle == 1){
                	subHeading1 = "";//messageSource.getMessage("acc.balanceSheet.Amount(liability)", null, RequestContextUtils.getLocale(request));   //"Liability";
                	subHeadingPre1 = "";//messageSource.getMessage("acc.balanceSheet.Amount(liability)", null, RequestContextUtils.getLocale(request));   //"Liability";
                        subHeading2 = value;//messageSource.getMessage("acc.balanceSheet.Amount(asset)", null, RequestContextUtils.getLocale(request));   //"Asset";
                    }
                }
            }else{
                headingString = messageSource.getMessage("acc.rem.125", null, RequestContextUtils.getLocale(request)); // "P&L Statement For : ";
                DateheadingString = messageSource.getMessage("acc.rem.126", null, RequestContextUtils.getLocale(request)); // "P&L Statement From-To :";
                value = formatter.format(startDate)+" "+messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" "+formatter.format(endDate);
                subHeading1 = messageSource.getMessage("acc.P&L.Amount(Debit)", null, RequestContextUtils.getLocale(request));   //"Debit";
                subHeading2 = messageSource.getMessage("acc.P&L.Amount(Credit)", null, RequestContextUtils.getLocale(request));   //"Credit";
            }
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            PdfPTable tab2 = new PdfPTable(1);
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

//            ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            PdfPCell LineCell = new PdfPCell();
            LineCell.setBorder(0);
            LineCell.setBorderWidthBottom(1);

            PdfPTable tableMain = new PdfPTable(2);
            tableMain.setWidthPercentage(100);
            tableMain.setWidths(new float[]{5, 85});
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) result.getEntityList().get(0);
            if(ExportRecordHandler.checkCompanyTemplateLogoPresent(company)){
                tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, company);
            }else{
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);    
            String companyArray[] = new String[4];
            companyArray[0] = company.getCompanyName();
            companyArray[1] = company.getAddress();
            companyArray[2] = company.getEmailID();
            companyArray[3] = company.getPhoneNumber();
            tab2 = ExportRecordHandler.getCompanyInfo(companyArray);
            }
            
            PdfPCell cell1 = new PdfPCell(tab1);
            PdfPCell cellcompanyInfo = new PdfPCell(tab2);
            cell1.setBorder(0);
            cellcompanyInfo.setBorder(0);
            
            tableMain.addCell(blankCell);
            tableMain.addCell(cell1);

            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);
            tableMain.addCell(cellcompanyInfo);
            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);            
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);            
            tableMain.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, tableMain);

//            tableMain.addCell(blankCell);
//            tableMain.addCell(blankCell);
//            tableMain.addCell(blankCell);
//
//            PdfPCell headerCell =  createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
//            headerCell.setBorder(0);
//            headerCell.setPaddingBottom(5);
//            tableMain.addCell(blankCell);
//            tableMain.addCell(headerCell);
//            PdfPCell headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
//            headerNameCell.setBorder(0);
//            headerNameCell.setPaddingBottom(5);
//            tableMain.addCell(headerNameCell);
            //tableMain.addCell(blankCell);
            tableMain = new PdfPTable(3);
            tableMain.setWidthPercentage(100);
            tableMain.setWidths(new float[]{5, 85, 10}); 
            
            PdfPCell reportPeriodCell = createCell("Reporting Period: " + formatter.format(stDate) + " to " + formatter.format(endDate), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            reportPeriodCell.setBorder(0);
            reportPeriodCell.setPaddingBottom(5);
            tableMain.addCell(blankCell);
            tableMain.addCell(reportPeriodCell);
            tableMain.addCell(blankCell);            
            
            PdfPCell headerCell =  createCell("Statement of financial position as at "+formatter.format(endDate)+"  (Amount in "+exportDaoObj.currencyRender("", currencyid, companyid)+")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            headerCell.setBorder(0);
            headerCell.setPaddingBottom(5);
            tableMain.addCell(blankCell);
            tableMain.addCell(headerCell);
            PdfPCell headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
            headerNameCell.setBorder(0);
            headerNameCell.setPaddingBottom(5);
            tableMain.addCell(headerNameCell);
            //tableMain.addCell(blankCell);

            for(int i = 0; i < 18; i++)
                tableMain.addCell(blankCell);

            ExportRecordHandler.addTableRow(mainTable, tableMain);

            PdfPTable table1 = new PdfPTable(5);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{10, 50, 15, 15, 10});

            blankCell = new PdfPCell();
            blankCell.setBorder(0);
            
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            jobj = jobj.getJSONObject("data");
            JSONArray rightObjArr = flag==1?jobj.getJSONArray("right"):jobj.getJSONArray("left");
            JSONArray leftObjArr = flag==1?jobj.getJSONArray("left"):jobj.getJSONArray("right");
            if(toggle == 1 && flag != 1){
            	rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            JSONArray finalValArr = (periodView)?jobj.getJSONArray("periodtotal") :jobj.getJSONArray("total");
            JSONArray openValArray = jobj.getJSONArray("opentotal");
            JSONArray endValArray = jobj.getJSONArray("endtotal");
            JSONArray prefinalValArr = jobj.getJSONArray("pretotal");
            PdfPCell HeaderCell1=null;
            PdfPCell HeaderCell2=null;
            PdfPCell HeaderCell3=null;
            PdfPCell HeaderPreCell2=null;
            PdfPCell HeaderPreCell3=null;
            PdfPCell accCodeHeaderCell=createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            
            if (periodView) {
                if (isShowAccountCode) {
                    HeaderCell1 = createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                    HeaderCell1.setPaddingBottom(5);
                    HeaderCell2 = createCell(subHeading1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderCell2.setPaddingBottom(5);
                    HeaderPreCell2 = createCell(subHeading2, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderPreCell2.setPaddingBottom(5);
                    HeaderPreCell3 = createCell(subHeading3, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderPreCell3.setPaddingBottom(5);
                    accCodeHeaderCell = createCell(messageSource.getMessage("acc.coa.accCode", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                    accCodeHeaderCell.setPaddingBottom(5);

                    table1 = ExportRecordHandler.getBlankTable6Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(accCodeHeaderCell);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderPreCell2);
                    table1.addCell(HeaderPreCell3);
                    table1.addCell(blankCell);

                    table1 = ExportRecordHandler.getBlankTable6Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(accCodeHeaderCell);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderPreCell2);
                    table1.addCell(HeaderPreCell3);
                    table1.addCell(blankCell);
                } else {
                    HeaderCell1 = createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                    HeaderCell1.setPaddingBottom(5);
                    HeaderCell2 = createCell(subHeading1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderCell2.setPaddingBottom(5);
                    HeaderPreCell2 = createCell(subHeading2, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderPreCell2.setPaddingBottom(5);
                    HeaderPreCell3 = createCell(subHeading3, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderPreCell3.setPaddingBottom(5);
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderPreCell2);
                    table1.addCell(HeaderPreCell3);
                    table1.addCell(blankCell);

                    table1 = ExportRecordHandler.getBlankTable5Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderPreCell2);
                    table1.addCell(HeaderPreCell3);
                    table1.addCell(blankCell);
                }
            } else{
                HeaderCell1 = createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                HeaderCell1.setPaddingBottom(5);
                accCodeHeaderCell = createCell(messageSource.getMessage("acc.coa.accCode", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                accCodeHeaderCell.setPaddingBottom(5);
                //HeaderCell1.setBorderWidthBottom(1);
                HeaderCell2 = createCell(subHeading1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell2.setPaddingBottom(5);
                if (isCompare) {
                    HeaderPreCell2 = createCell(subHeadingPre1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    HeaderPreCell2.setPaddingBottom(5);
                }
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
                if (isShowAccountCode) {
                    table1.addCell(accCodeHeaderCell);
                }
                table1.addCell(HeaderCell2);
                if (isCompare) {
                    table1.addCell(HeaderPreCell2);
                } else {
                    table1.addCell(blankCell);     
                }              
                table1.addCell(blankCell);   
                
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
                if (isShowAccountCode) {
                    table1.addCell(accCodeHeaderCell);
                }
                table1.addCell(HeaderCell2);
                if (isCompare) {
                    table1.addCell(HeaderPreCell2);
                }  else {
                    table1.addCell(blankCell);   
                }            
                table1.addCell(blankCell);
            }

            ExportRecordHandler.addTableRow(mainTable, table1);
            
            HeaderCell1 = createCell("ASSETS", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            HeaderCell1.setPaddingBottom(5);
            //HeaderCell1.setBorderWidthBottom(1);
            HeaderCell2 = createCell("", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
//            HeaderCell2.setPaddingBottom(5);
            HeaderPreCell2 = createCell("", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
//            HeaderPreCell2.setPaddingBottom(5);
            //HeaderCell2.setBorderWidthBottom(1);
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            }  else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }
            table1.addCell(blankCell);
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            if(isCompare){
               table1.addCell(HeaderPreCell2);                
            } else {
               table1.addCell(blankCell);
            }
            
//            table1.addCell(blankCell);
            
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
//            table1.addCell(blankCell);
            
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
//            table1.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(mainTable, table1);
            if(periodView){
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else{
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }
            double totalOpenAsset= Double.parseDouble(openValArray.getString(1));
            double totalAsset = Double.parseDouble(finalValArr.getString(1));
            double totalEndAsset = Double.parseDouble(endValArray.getString(1));
            double pretotalAsset = Double.parseDouble(prefinalValArr.getString(1));
            
            double totalLibility = Double.parseDouble(finalValArr.getString(0));
            double totalOpenLiability = Double.parseDouble(openValArray.getString(0));
            double totalEndLiability = Double.parseDouble(endValArray.getString(0));
            double pretotalLibility = Double.parseDouble(prefinalValArr.getString(0));
            for(int i=0;i<rightObjArr.length();i++){
                JSONObject leftObj = rightObjArr.getJSONObject(i);
                if(i+1<rightObjArr.length() && i-1>0){
                    JSONObject temp = rightObjArr.getJSONObject(i+1);
                    JSONObject temp1 = rightObjArr.getJSONObject(i-1);
                    if(Integer.parseInt(leftObj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                        leftObj.put("bold", true);
                    else if(Integer.parseInt(leftObj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                        leftObj.put("bold", true);
                }
                ExportRecordHandler.addBalanceSheetCell(request,leftObj,table1,currencyid,isAlignment);
                if (periodView) {
                    ExportRecordHandler.addTableRow1(mainTable, table1);
                    if (isShowAccountCode) {
                        table1 = ExportRecordHandler.getBlankTable6Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    }
                } else {
                    ExportRecordHandler.addTableRow(mainTable, table1);
                    if (isShowAccountCode) {
                        if (isCompare) {
                            table1 = ExportRecordHandler.getBlankTable5Columns();
                        } else {
                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                        }
                    } else {
                        if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                    }
                }
//                PdfPCell cell3 = createCell(leftObj.get("accountname").toString(), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                PdfPCell cell4 = createCell(com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                cell3.setBorder(0);
//                table1.addCell(cell3);
//                cell4.setBorder(0);
//                table1.addCell(cell4);
            }
            table1.addCell(blankCell);
            PdfPCell totalAsscell = ExportRecordHandler.createBalanceSheetCell("TOTAL ASSETS", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
            totalAsscell.setPaddingBottom(5);
            table1.addCell(totalAsscell);
            if (periodView) {
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                PdfPCell totalAssValcell1 = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalOpenAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                totalAssValcell1.setBorderWidthBottom(2);
                totalAssValcell1.setBorderWidthTop(1);
                totalAssValcell1.setBorderColor(Color.gray);
                table1.addCell(totalAssValcell1);
                PdfPCell pretotalAssValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalEndAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                pretotalAssValcell.setBorderWidthBottom(2);
                pretotalAssValcell.setBorderWidthTop(1);
                pretotalAssValcell.setBorderColor(Color.gray);
                PdfPCell totalAssValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                totalAssValcell.setBorderWidthBottom(2);
                totalAssValcell.setBorderWidthTop(1);
                totalAssValcell.setBorderColor(Color.gray);
                table1.addCell(totalAssValcell);
                table1.addCell(pretotalAssValcell);
            } else {
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                    PdfPCell pretotalAssValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(pretotalAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                    pretotalAssValcell.setBorderWidthBottom(2);
                    pretotalAssValcell.setBorderWidthTop(1);
                    pretotalAssValcell.setBorderColor(Color.gray);
                }
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                PdfPCell totalAssValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                totalAssValcell.setBorderWidthBottom(2);
                totalAssValcell.setBorderWidthTop(1);
                totalAssValcell.setBorderColor(Color.gray);
                table1.addCell(totalAssValcell);
                if(isCompare){
                    PdfPCell pretotalAssValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(pretotalAsset, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                    pretotalAssValcell.setBorderWidthBottom(2);
                    pretotalAssValcell.setBorderWidthTop(1);
                    pretotalAssValcell.setBorderColor(Color.gray);
                    table1.addCell(pretotalAssValcell);
                } else {
                    table1.addCell(blankCell);
                }
                
            }        
            table1.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, table1);
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }
            for(int i = 0; i < 17; i++)
            	table1.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, table1);
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }

            HeaderCell1 = createCell("EQUITY AND LIABILITIES ", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            HeaderCell1.setPaddingBottom(5);

            if (periodView) {
                //HeaderCell1.setBorderWidthBottom(1);
                HeaderCell2 = createCell(subHeading1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell2.setPaddingBottom(5);
                //HeaderCell2.setBorderWidthBottom(1);
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);

                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);

                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
//                if (isShowAccountCode) {
//                    table1 = ExportRecordHandler.getBlankTable6Columns();
//                } else {
//                    table1 = ExportRecordHandler.getBlankTable5Columns();
//                }
            } else {
                //HeaderCell1.setBorderWidthBottom(1);
                HeaderCell2 = createCell(subHeading2, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell2.setPaddingBottom(5);
                //HeaderCell2.setBorderWidthBottom(1);
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);

                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);

                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
//                if (isShowAccountCode) {
//                    table1 = ExportRecordHandler.getBlankTable5Columns();
//                } else {
//                    table1 = ExportRecordHandler.getBlankTable4Columns();
//                }
            }
            
            ExportRecordHandler.addTableRow(mainTable, table1);
            
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }
                        
            for(int i=0;i< leftObjArr.length();i++){
                JSONObject leftObj = leftObjArr.getJSONObject(i);
                if(i+1<leftObjArr.length() && i-1>0){
                    JSONObject temp = leftObjArr.getJSONObject(i+1);
                    JSONObject temp1 = leftObjArr.getJSONObject(i-1);
                    if(Integer.parseInt(leftObj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                        leftObj.put("bold", true);
                    else if(Integer.parseInt(leftObj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                        leftObj.put("bold", true);
                }
                 ExportRecordHandler.addBalanceSheetCell(request,leftObj,table1,currencyid,isAlignment);
                 ExportRecordHandler.addTableRow(mainTable, table1);                 
                if (periodView) {
                    if (isShowAccountCode) {
                        table1 = ExportRecordHandler.getBlankTable6Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    }
                } else {
                    if (isShowAccountCode) {
                        if (isCompare) {
                            table1 = ExportRecordHandler.getBlankTable5Columns();
                        } else {
                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                        }
                    } else {
                        if (isCompare) {
                            table1 = ExportRecordHandler.getBlankTable4Columns();
                        } else {
                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                        }
                    }
                }
//                PdfPCell cell3 = createCell(leftObj.get("accountname").toString(), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                PdfPCell cell4 = createCell(com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                cell3.setBorder(0);
//                table1.addCell(cell3);
//                cell4.setBorder(0);
//                table1.addCell(cell4);
            }
            table1.addCell(blankCell);
            PdfPCell totalLibcell = ExportRecordHandler.createBalanceSheetCell("TOTAL EQUITY AND LIABILITIES", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
            table1.addCell(totalLibcell);
            if (periodView) {
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                PdfPCell totalLibValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalLibility, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                totalLibValcell.setBorderWidthBottom(2);
                totalLibValcell.setBorderWidthTop(1);
                totalLibValcell.setBorderColor(Color.gray);
                PdfPCell pretotalLibValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalOpenLiability, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                pretotalLibValcell.setBorderWidthBottom(2);
                pretotalLibValcell.setBorderWidthTop(1);
                pretotalLibValcell.setBorderColor(Color.gray);
                PdfPCell pretotalLibValcellend = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalEndLiability, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                pretotalLibValcellend.setBorderWidthBottom(2);
                pretotalLibValcellend.setBorderWidthTop(1);
                pretotalLibValcellend.setBorderColor(Color.gray);
                table1.addCell(pretotalLibValcell);
                table1.addCell(totalLibValcell);
                table1.addCell(pretotalLibValcellend);
            } else {
                PdfPCell totalLibValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(totalLibility, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                totalLibValcell.setBorderWidthBottom(2);
                totalLibValcell.setBorderWidthTop(1);
                totalLibValcell.setBorderColor(Color.gray);
                if(isCompare){
                     PdfPCell pretotalLibValcell = ExportRecordHandler.createBalanceSheetCell(ExportRecordHandler.currencyRenderer(pretotalLibility, companyid), fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
                pretotalLibValcell.setBorderWidthBottom(2);
                pretotalLibValcell.setBorderWidthTop(1);
                pretotalLibValcell.setBorderColor(Color.gray);
                    if (isShowAccountCode) {
                        table1.addCell(blankCell);
                    }
                table1.addCell(totalLibValcell);
                table1.addCell(pretotalLibValcell);
                } else {
                    table1.addCell(blankCell);
                    if (flag == 1) {
                        table1.addCell(totalLibValcell);
                    }
                    table1.addCell(blankCell);
                }
               
            }            
            table1.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, table1);
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }


//            PdfPCell mainCell11 = new PdfPCell(tableMain);
//            mainCell11.setBorder(0);
//            mainCell11.setPadding(10);
//            mainTable.addCell(mainCell11);
//
//
//            mainCell11 = new PdfPCell(table1);
//            mainCell11.setBorder(0);
//            mainCell11.setPadding(10);
//            mainTable.addCell(mainCell11);




            document.add(mainTable);
        } catch (Exception ex) {
            return null;
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
        return baos;
    }

    
     
    
    public  PdfPCell createCell(String string, Font fontTbl, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(string, fontTbl));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }

    public  PdfPCell createCell(String string, FontContext context, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(string, context)));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }

    public void directPrintToPrinter(String filename, ByteArrayOutputStream baos,
            HttpServletResponse response, String companyId) throws IOException {
        List param = new ArrayList();
        param.add(companyId);
        String selectQuery = "select filepath from compprintersetting where company = ?";
        List list = null;
        try {
            list = baseDAO.executeSQLQuery(selectQuery, param.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
        }

        String filePath = (String) list.get(0);

        try (OutputStream outputStream = new FileOutputStream(filePath + filename)) {
            baos.writeTo(outputStream);
        }

        PrintService[] services = PrinterJob.lookupPrintServices();
        PrintService[] services1 = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
        DocPrintJob docPrintJob = null;

        PDDocument pdf = PDDocument.load(new File(filePath + filename));
        PrinterJob job = PrinterJob.getPrinterJob();

        for (PrintService service : services1) {
//            PrintServiceAttributeSet printServiceAttributes = service.getAttributes();
//            for (Attribute a : printServiceAttributes.toArray()) {
//                System.out.println("Attr  :: " + a.getName() + " : " + printServiceAttributes.get(a.getClass()).toString());
//            }
            docPrintJob = service.createPrintJob();
            try {
                job.setPrintService(docPrintJob.getPrintService());
                job.setPageable(new PDFPageable(pdf));
                job.print();
//                System.out.println("Printed on : "+service.getName());
                Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, "Printed on : "+service.getName());
            } catch (PrinterException pe) {
                pe.printStackTrace();
            }
        }
        File file = new File(filePath + filename);
        file.delete();
    }
    
    public void writeDataToFile(String filename, ByteArrayOutputStream baos,
            HttpServletResponse response) throws IOException {

        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentType("application/octet-stream");
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    private static void printFile(PrintService service, String filePath) {
        FileInputStream textStream = null;
        try {
            textStream = new FileInputStream(filePath);
//            textStream = new FileInputStream("/home/krawler/Downloads/User.csv");

//            DocFlavor flavor = DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8;
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            

            System.out.println(service.getName());

            // Create the print job
            DocPrintJob job = service.createPrintJob();
            Doc doc = new SimpleDoc(textStream, flavor, null);

            // Monitor print job events; for the implementation of PrintJobWatcher,
            // see e702 Determining When a Print Job Has Finished
            PrintJobWatcher pjDone = new PrintJobWatcher(job);

            // Print it
            job.print(doc, null);
            // Wait for the print job to be done
            pjDone.waitForDone();

            System.out.println("service :: " + service + "    :: DONE");

        } catch (PrintException e) {
            e.printStackTrace();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // It is now safe to close the input stream
            if (textStream != null) {
                try {
                    textStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
        
    static class PrintJobWatcher {

        // true iff it is safe to close the print job's input stream
        boolean done = false;

        PrintJobWatcher(DocPrintJob job) {
            // Add a listener to the print job
            job.addPrintJobListener(new PrintJobAdapter() {
                public void printJobCanceled(PrintJobEvent pje) {
                    allDone();
                }

                public void printJobCompleted(PrintJobEvent pje) {
                    allDone();
                }

                public void printJobFailed(PrintJobEvent pje) {
                    allDone();
                }

                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    allDone();
                }

                void allDone() {
                    synchronized (PrintJobWatcher.this) {
                        done = true;
                        PrintJobWatcher.this.notify();
                    }
                }
            });
        }

        public synchronized void waitForDone() {
            try {
                while (!done) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public static void concatPDFs(List<ByteArrayOutputStream> streamOfPDFFiles, String filename, HttpServletResponse response) {
        Document document = new Document(PageSize.A4,15, 15, 35, 30);
        try {
            ByteArrayOutputStream finalbaos = new ByteArrayOutputStream();
            List<ByteArrayOutputStream> pdfs = streamOfPDFFiles;
            List<PdfReader> readers = new ArrayList<PdfReader>();
            int totalPages = 0;
            Iterator<ByteArrayOutputStream> iteratorPDFs = pdfs.iterator();
            // Create Readers for the pdfs.
            while (iteratorPDFs.hasNext()) {
                ByteArrayOutputStream pdf = iteratorPDFs.next();
                PdfReader pdfReader = new PdfReader(pdf.toByteArray());
                readers.add(pdfReader);
                totalPages += pdfReader.getNumberOfPages();
            }
            // Create a writer for the outputstream
            PdfWriter writer = PdfWriter.getInstance(document, finalbaos);
            document.open();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
                    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent(); // Holds the PDF data 
            PdfImportedPage page;
            int currentPageNumber = 0;
            int pageOfCurrentReaderPDF = 0;
            Iterator<PdfReader> iteratorPDFReader = readers.iterator(); 
            // Loop through the PDF files and add to the output.
            while (iteratorPDFReader.hasNext()) {
                PdfReader pdfReader = iteratorPDFReader.next(); 
                // Create a new page in the target for each source page.
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;
                   page = writer.getImportedPage(pdfReader,
                            pageOfCurrentReaderPDF);
                    cb.addTemplate(page, 0, 0); 
//                    // Code for pagination.
//                    if (paginate) {
//                        cb.beginText();
//                        cb.setFontAndSize(bf, 9);
//                        cb.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
//                                + currentPageNumber + " of " + totalPages, 520,
//                                5, 0);
//                        cb.endText();
//                    }
                }
                pageOfCurrentReaderPDF = 0;
            }
            finalbaos.flush();
            document.close();
            finalbaos.close();
            response.setHeader("Content-Disposition", "attachment; filename=\""+filename+"\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(finalbaos.size());
            response.getOutputStream().write(finalbaos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen())
                document.close();
        }
    }
    
    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar +" "+ val;
            }
            return " And " + numNames[number] +" "+ val + soFar;
        }

        public String convert(Double number, KWLCurrency currency) {
            if (number == 0) {
                return "Zero";
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        }
    }

    public ByteArrayOutputStream exportCashFlow(JSONObject jobj,String logoPath,String comName, HttpServletRequest request)  throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {

            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(2);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{30,20});

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell headerCell =  createCell(messageSource.getMessage("acc.dashboard.cashFlowStatement", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            JSONArray ObjArr = jobj.getJSONArray("data");

            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthLeft(1);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell1.setBorderWidthTop(1);
            HeaderCell1.setBorderWidthRight(1);
            PdfPCell HeaderCell2 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            HeaderCell2.setBorderWidthTop(1);
            HeaderCell2.setBorderWidthRight(1);

            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            PdfPCell objCell1 = null;
            PdfPCell objCell2 = null;
            int objArrLength = ObjArr.length();
            for(int i=0;i<objArrLength;i++){
                JSONObject leftObj = ObjArr.getJSONObject(i);
                if(leftObj.has("lfmt")  &&  leftObj.getString("lfmt").equals("title"))
                	objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), fontSmallBold, Element.ALIGN_CENTER, 0, 5,0);
                else
                	objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5,0);
                objCell2 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lvalue"), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0,0);
                objCell1.setBorderWidthLeft(1);
                objCell2.setBorderWidthRight(1);
                objCell1.setBorderWidthRight(1);
                objCell1.setBorderWidthBottom(1);
                objCell2.setBorderWidthBottom(1);
                if(i!=(objArrLength-1)){
                    table1.addCell(objCell1);
                    table1.addCell(objCell2);
                }
            }
            objCell1.setBorderWidthBottom(1);
            objCell2.setBorderWidthBottom(1);
            table1.addCell(objCell1);
            table1.addCell(objCell2);

            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);

            document.add(mainTable);
        } catch (Exception ex) {
            return null;
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
           return baos;
    }

        public ByteArrayOutputStream exportCashFlowStatementAsPerCOA(JSONObject jobj,String logoPath,String comName, HttpServletRequest request)  throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {

            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(3);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{10, 30, 20});

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell headerCell =  createCell(messageSource.getMessage("acc.dashboard.cashFlowStatement", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_CENTER, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(blankCell);
            table1.addCell(headerCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            JSONArray ObjArr = jobj.getJSONArray("data");
            
            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthLeft(1);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell1.setBorderWidthTop(1);
            HeaderCell1.setBorderWidthRight(1);
            
            PdfPCell HeaderCell2 = createCell(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            HeaderCell2.setBorderWidthTop(1);
            HeaderCell2.setBorderWidthRight(1);
            
            PdfPCell HeaderCell3 = createCell(messageSource.getMessage("acc.field.COAinDeskera", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell3.setBorderWidthBottom(1);
            HeaderCell3.setBorderWidthTop(1);
            HeaderCell3.setBorderWidthRight(1);
            
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            table1.addCell(HeaderCell3);
            PdfPCell objCellNo = null;
            PdfPCell objCell1 = null;
            PdfPCell objCell2 = null;
            int objArrLength = ObjArr.length();
            for(int i=0;i<objArrLength;i++){
                JSONObject leftObj = ObjArr.getJSONObject(i);
                if (leftObj.has("lformat") && (leftObj.getString("lformat").equals("maintitle") || leftObj.getString("lformat").equals("title") || leftObj.getString("lformat").equals("total"))) {    //For Main title in Bold Format
                    objCellNo = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lno"), fontSmallBold, Element.ALIGN_CENTER, 0, 5, 0);
                    if (leftObj.getString("lformat").equals("total")) {
                        objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), fontSmallBold, Element.ALIGN_RIGHT, 0, 0, 20);
                    } else {
                        objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), fontSmallBold, Element.ALIGN_CENTER, 0, 5, 0);                        
                    }
                    objCell2 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lvalue"), fontSmallBold, Element.ALIGN_RIGHT, 0, 0, 5);
                } else {
                    objCellNo = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lno"), fontSmallBold, Element.ALIGN_CENTER, 0, 5, 0);
                    objCell1 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lname"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5, 0);
                    objCell2 = ExportRecordHandler.createBalanceSheetCell(leftObj.getString("lvalue"), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0, 5);
                }
                objCellNo.setBorderWidthLeft(1);
                objCellNo.setBorderWidthRight(1);
                objCellNo.setBorderWidthBottom(1);
                
                objCell1.setBorderWidthRight(1);
                objCell1.setBorderWidthBottom(1);
                
                objCell2.setBorderWidthRight(1);
                objCell2.setBorderWidthBottom(1);
                
                if(i!=(objArrLength-1)){
                    table1.addCell(objCellNo);
                    table1.addCell(objCell1);
                    table1.addCell(objCell2);
                }
            }
            objCellNo.setBorderWidthBottom(1);
            objCell1.setBorderWidthBottom(1);
            objCell2.setBorderWidthBottom(1);
            
            table1.addCell(objCellNo);
            table1.addCell(objCell1);
            table1.addCell(objCell2);

            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);

            document.add(mainTable);
        } catch (Exception ex) {
            return null;
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
           return baos;
    }

        public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                try {
                    ExportRecordHandler.getHeaderFooter(isAttachProductTable, isFromProductTable, globalCurrencyValue, globalTableHeader, linkHeader, productHeaderTableGlobalNo, config, header, footer, document);
                } catch (ServiceException ex) {
                }
                try {
                    // Add page header
    //                header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
    //                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());
                    ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document,writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(mainTableGlobal != null){
                    
                    mainTableGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    mainTableGlobal.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 33, writer.getDirectContent());
                    mainTableGlobal = null;
                }
               
                if(tableClosedLineGlobal != null){
                    tableClosedLineGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    tableClosedLineGlobal.writeSelectedRows(0, 10, document.leftMargin(), document.bottomMargin() + 45, writer.getDirectContent());
                    tableClosedLineGlobal = null;
                }
                // Add page footer
//                footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
//                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());
                try {
                    ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Add page border
                if (config!=null && config.getBoolean("pageBorder")) {
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
        
        
    public ByteArrayOutputStream generateGSTReportPdf(HttpServletRequest request, String currencyid, String logoPath, String comName, String address, String startDate, String endDate, JSONArray salesjArr, JSONArray purchasejArr, Company company,HashMap<String, Object> assignedvariables) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        String taxYearName="",taxPeriodName="";
        try {
            double totalSale = 0, totalSaleTax = 0, totalPurchase = 0, totalPurchaseTax = 0,totalPurchaseAmount = 0, totalSalesAmount = 0;
            String poRefno ="";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 75, 75, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = new PdfPCell();
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            if (assignedvariables.containsKey("taxYearName")) {
                taxYearName = (String) assignedvariables.get("taxYearName");
            }

            if (assignedvariables.containsKey("taxPeriodName")) {
                taxPeriodName = (String) assignedvariables.get("taxPeriodName");
            }
            PdfPTable table1 = new PdfPTable(1);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{100});

            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell reportTitle =  createCell("GST Transaction Summary Report", fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);

            /*Showing Tax Year and Tax period Name*/
//            if (!StringUtil.isNullOrEmpty(taxYearName)) {
//                String taxyearLabel = "";
//                if (!StringUtil.isNullOrEmpty(taxPeriodName)) {
//                    taxyearLabel = "  Tax Period :  " + taxPeriodName;
//                }
//                PdfPCell taxYearandPeriod = createCell(" Tax Year :  " + taxYearName +taxyearLabel, fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
//                taxYearandPeriod.setBorder(0);
//                taxYearandPeriod.setHorizontalAlignment(Element.ALIGN_CENTER);
//                table1.addCell(taxYearandPeriod);
//            }

            PdfPCell fromTo =  createCell("From: "+ startDate + "  To:  " + endDate, fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
            fromTo.setBorder(0);
            fromTo.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(fromTo);


            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);

            PdfPCell comAddress = new PdfPCell(new Paragraph(company.getAddress() != null ? StringUtil.serverHTMLStripper(company.getAddress()) : "", fontSmallRegular));
            comAddress.setBorder(0);
            table1.addCell(comAddress);

            PdfPCell comEmail = new PdfPCell(new Paragraph(company.getEmailID(), fontSmallRegular));
            comEmail.setBorder(0);
            comEmail.setBorder(Rectangle.BOTTOM);
            table1.addCell(comEmail);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell purchaseTitle =  createCell("Purchase and Expenses with GST Paid", fontMediumBold1, Element.ALIGN_LEFT, 0, 5);
            purchaseTitle.setBorder(0);
            purchaseTitle.setPaddingLeft(10);
            purchaseTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
            table1.addCell(purchaseTitle);

            PdfPTable purchaseTable = ExportRecordHandler.getBlankTableGST();
            String header1[] = {"", "Amount Without Tax" , "Tax Amount", "Amount With Tax"};
            PdfPCell tableCell = null;
            for (int i = 0; i < header1.length; i++) {
            	tableCell = new PdfPCell(new Paragraph(header1[i], fontSmallBold));
            	tableCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            	tableCell.setBorder(Rectangle.BOTTOM);
            	purchaseTable.addCell(tableCell);
            }
            purchaseTable.addCell(blankCell);

            ExportRecordHandler.addTableRow(table1, purchaseTable);

            for(int i = 0; i < purchasejArr.length(); i++) {
            	purchaseTable = ExportRecordHandler.getBlankTableGST();
	            PdfPCell purchaseCell = createCell(purchasejArr.getJSONObject(i).getString("taxname"), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            ExportRecordHandler.addTableRow(table1, purchaseTable);

                    totalPurchase += Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")), companyid) : 0;
                    totalPurchaseTax += Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")), companyid) : 0;
                    totalPurchaseAmount += Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid) : 0;
            }



            PdfPTable salesTable = ExportRecordHandler.getBlankTableGST();
            PdfPCell salesCell = createCell("Total GST Paid", fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
            salesCell.setPaddingRight(30);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchaseAmount, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchaseTax, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchase, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            ExportRecordHandler.addTableRow(table1, salesTable);
            salesTable = ExportRecordHandler.getBlankTableGST();
            salesTable.addCell(blankCell);
            ExportRecordHandler.addTableRow(table1, salesTable);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell salesTitle =  createCell("Sales and Income with GST Received", fontMediumBold1, Element.ALIGN_LEFT, 0, 5);
            salesTitle.setBorder(0);
            salesTitle.setPaddingLeft(10);
            salesTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
            table1.addCell(salesTitle);

            salesTable = ExportRecordHandler.getBlankTableGST();
            String header[] = {"", "Amount Without Tax", "Tax Amount","Amount With Tax"};
            for (int i = 0; i < header.length; i++) {
            	tableCell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
            	tableCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            	tableCell.setBorder(Rectangle.BOTTOM);
            	salesTable.addCell(tableCell);
            }
            salesTable.addCell(blankCell);

            ExportRecordHandler.addTableRow(table1, salesTable);

            for(int i = 0; i < salesjArr.length(); i++) {
            	salesTable = ExportRecordHandler.getBlankTableGST();
	            salesCell = createCell(salesjArr.getJSONObject(i).getString("taxname"), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign((Double) Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")), currencyid): "", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            ExportRecordHandler.addTableRow(table1, salesTable);
                    totalSale += Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")),  companyid) : 0;
                    totalSaleTax += Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")) != 0 ?authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")), companyid) : 0;
                    totalSalesAmount += Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid) : 0;
            }

            salesTable = ExportRecordHandler.getBlankTableGST();
            salesCell = createCell("Total GST Received", fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
            salesCell.setPaddingRight(30);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSalesAmount, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSaleTax, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSale, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(Rectangle.TOP);
            salesTable.addCell(salesCell);
            ExportRecordHandler.addTableRow(table1, salesTable);
            salesTable = ExportRecordHandler.getBlankTableGST();
            salesTable.addCell(blankCell);
            ExportRecordHandler.addTableRow(table1, salesTable);

            PdfPCell mainCell = new PdfPCell(table1);
            mainCell.setBorder(0);
            mainCell.setPadding(10);
            mainTable.addCell(mainCell);
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("generateGSTReportPdf: "+ex.getMessage(), ex);
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

    
    public ByteArrayOutputStream generateGSTReportPdfDetailed(HttpServletRequest request, String currencyid, String logoPath, String comName, String address, String startDate, String endDate, JSONArray salesjArr, JSONArray purchasejArr, Company company,HashMap<String, Object> assignedvariables) throws DocumentException, ServiceException, IOException, SessionExpiredException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        String taxYearName="",taxPeriodName="";
        //ERP-9201 : This date format ll convert the date according to timezone.
    	DateFormat sdf=authHandler.getUserDateFormatter(request);
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));        
                
        try {
            double totalSale = 0, totalSaleTax = 0, totalPurchase = 0, totalPurchaseTax = 0,totalPurchaseAmount=0,totalSalesAmount=0;
            String poRefno ="";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            baos = new ByteArrayOutputStream();
            boolean isLandscape = request.getParameter("reportType").equals("11") ? true : false;
            if (isLandscape) {
                    document = new Document(PageSize.A4.rotate(), 5, 5, 10, 10);
            } else {
                    document = new Document(PageSize.A4, 5, 5, 10, 10);
            }
            if (assignedvariables.containsKey("taxYearName")) {
                taxYearName=(String)assignedvariables.get("taxYearName");
            }
            
            if (assignedvariables.containsKey("taxPeriodName")) {
                taxPeriodName = (String) assignedvariables.get("taxPeriodName");
            }
            
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = new PdfPCell();
            PdfPCell blankCellTop = new PdfPCell();
            blankCellTop.setBorder(Rectangle.TOP);
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(1);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{100});

            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell reportTitle =  createCell("GST Transaction Detailed Report", fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);

            /*Showing Tax Year and Tax period Name*/
//            if (!StringUtil.isNullOrEmpty(taxYearName)) {
//                String taxyearlabel="";
//                if(!StringUtil.isNullOrEmpty(taxPeriodName)){
//                 taxyearlabel= "  Tax Period:  " + taxPeriodName;
//                }
//                PdfPCell taxYearandPeriod = createCell(" Tax Year:  " + taxYearName +taxyearlabel, fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
//                taxYearandPeriod.setBorder(0);
//                taxYearandPeriod.setHorizontalAlignment(Element.ALIGN_CENTER);
//                table1.addCell(taxYearandPeriod);
//            }
            
            PdfPCell fromTo = createCell("From: "+ startDate + "  To:  " + endDate, fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
            fromTo.setBorder(0);
            fromTo.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(fromTo);


            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);

            PdfPCell comAddress = new PdfPCell(new Paragraph(company.getAddress() !=null ? StringUtil.serverHTMLStripper(company.getAddress()) : "", fontSmallRegular));
            comAddress.setBorder(0);
            table1.addCell(comAddress);

            PdfPCell comEmail = new PdfPCell(new Paragraph(company.getEmailID(), fontSmallRegular));
            comEmail.setBorder(0);
            comEmail.setBorder(Rectangle.BOTTOM);
            table1.addCell(comEmail);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);


            PdfPCell purchaseTitle =  createCell("Purchase and Expenses with GST Paid", fontMediumBold1, Element.ALIGN_LEFT, 0, 5);
            purchaseTitle.setBorder(0);
            purchaseTitle.setPaddingLeft(10);
            purchaseTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
            table1.addCell(purchaseTitle);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPTable purchaseTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
            ArrayList<String> newArrayList = new ArrayList();
            newArrayList.add("Date");
            newArrayList.add("ID No");
            newArrayList.add("Name");
            newArrayList.add("Amount Without Tax");
            newArrayList.add("Tax Amount");
            newArrayList.add("Amount With Tax");

            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                newArrayList.add("Exchange Rate");
                newArrayList.add("Amount in Transaction Currency");
            }
            String header1[]= newArrayList.toArray(new String[newArrayList.size()]);
            for (int i = 0; i < header1.length; i++) {
            	PdfPCell tableCell = new PdfPCell(new Paragraph(header1[i], fontSmallRegular));
            	if(i<=2)
            		tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            	else
            		tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            	tableCell.setBorder(Rectangle.BOTTOM);
            	purchaseTable.addCell(tableCell);
            }
            purchaseTable.addCell(blankCell);

            ExportRecordHandler.addTableRow(table1, purchaseTable);

            for(int i = 0; i < purchasejArr.length(); i++) {
            	purchaseTable = ExportRecordHandler.getBlankTableGST();
	            PdfPCell purchaseCell = createCell(purchasejArr.getJSONObject(i).getString("taxname"), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            purchaseCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            purchaseTable.addCell(purchaseCell);
	            ExportRecordHandler.addTableRow(table1, purchaseTable);

	            JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
	            for(int j = 0; j < grArray.length(); j++) {
	            	purchaseTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
	            	String grDate = grArray.getJSONObject(j).getString("grdate");
	            	PdfPCell grCell = createCell(grDate, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	purchaseTable.addCell(grCell);
	            	grCell = createCell(grArray.getJSONObject(j).getString("grno"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	purchaseTable.addCell(grCell);
	            	grCell = createCell(grArray.getJSONObject(j).getString("grname"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	purchaseTable.addCell(grCell);                        
	            	grCell = createCell(Double.parseDouble(grArray.getJSONObject(j).optString("gramtexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(grArray.getJSONObject(j).optString("gramtexcludingtax","0.0")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	purchaseTable.addCell(grCell);
	            	grCell = createCell(Double.parseDouble(grArray.getJSONObject(j).getString("grtaxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(grArray.getJSONObject(j).getString("grtaxamount")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	purchaseTable.addCell(grCell);
	            	grCell = createCell(Double.parseDouble(grArray.getJSONObject(j).getString("gramt")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(grArray.getJSONObject(j).getString("gramt")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	purchaseTable.addCell(grCell);
                        if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                            if(grArray.getJSONObject(j).has("transactionexchangerate")){
                                grCell = createCell(grArray.getJSONObject(j).getString("transactionexchangerate"), fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            } else {
                                grCell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            }
                            purchaseTable.addCell(grCell);
                            if(grArray.getJSONObject(j).has("originalamountincludingtax")&&grArray.getJSONObject(j).has("transactioncurrencyid")){
                                grCell = createCell(Double.parseDouble(grArray.getJSONObject(j).getString("originalamountincludingtax")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(grArray.getJSONObject(j).getString("originalamountincludingtax")), companyid), grArray.getJSONObject(j).getString("transactioncurrencyid")) : "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            } else {
                                grCell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            }    
                            purchaseTable.addCell(grCell);
                        }
	            	ExportRecordHandler.addTableRow(table1, purchaseTable);
	            }
                    
                    purchaseTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
	            purchaseCell = createCell("Total", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
//	            purchaseCell.setPaddingRight(30);
                    purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                    purchaseTable.addCell(purchaseCell);
                    purchaseCell = new PdfPCell();
                    purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                    purchaseTable.addCell(purchaseCell);
                    purchaseCell = new PdfPCell();
                    purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                    purchaseTable.addCell(purchaseCell);
                    purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
	            purchaseTable.addCell(purchaseCell);

	            purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
	            purchaseTable.addCell(purchaseCell);

	            purchaseCell = createCell(Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
	            purchaseTable.addCell(purchaseCell);
                    if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                        purchaseCell = new PdfPCell();
                        purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        purchaseTable.addCell(purchaseCell);
                        purchaseCell = new PdfPCell();
                        purchaseCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        purchaseTable.addCell(purchaseCell);
                    }
	            ExportRecordHandler.addTableRow(table1, purchaseTable);
              
//	            purchaseTable = ExportRecordHandler.getBlankTableGST();
//	            purchaseTable.addCell(blankCell);
//	            purchaseTable.addCell(blankCellTop);
//	            purchaseTable.addCell(blankCellTop);
//	            purchaseTable.addCell(blankCellTop);
//	            ExportRecordHandler.addTableRow(table1, purchaseTable);

                    totalPurchase += Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale")), companyid) : 0;
                    totalPurchaseTax += Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxamount")), companyid) : 0;
                    totalPurchaseAmount += Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandler.round(Double.parseDouble(purchasejArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid) : 0;

            }

            PdfPTable salesTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
            PdfPCell salesCell = createCell("Total GST Paid", fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
//            salesCell.setPaddingRight(30);
            salesTable.addCell(salesCell);
            salesCell = new PdfPCell();
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            salesCell = new PdfPCell();
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchaseAmount, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchaseTax, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalPurchase, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                salesCell = new PdfPCell();
                salesCell.setBorder(0);
                salesTable.addCell(salesCell);
                salesCell = new PdfPCell();
                salesCell.setBorder(0);
                salesTable.addCell(salesCell);
            }
            ExportRecordHandler.addTableRow(table1, salesTable);
            salesTable = ExportRecordHandler.getBlankTableGST();
            salesTable.addCell(blankCell);
            ExportRecordHandler.addTableRow(table1, salesTable);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell salesTitle =  createCell("Sales and Income with GST Received", fontMediumBold1, Element.ALIGN_LEFT, 0, 5);
            salesTitle.setBorder(0);
            salesTitle.setPaddingLeft(10);
            salesTitle.setHorizontalAlignment(Element.ALIGN_LEFT);
            table1.addCell(salesTitle);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            salesTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
            ArrayList<String> newArrayList1 = new ArrayList();
            newArrayList.add("Date");
            newArrayList.add("ID No");
            newArrayList.add("Name");
            newArrayList.add("Amount Without Tax");
            newArrayList.add("Tax Amount");
            newArrayList.add("Amount With Tax");

            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                newArrayList.add("Exchange Rate");
                newArrayList.add("Amount in Transaction Currency");
            }
            String header[]= newArrayList1.toArray(new String[newArrayList1.size()]);            
            PdfPCell tableCell = null;
            for (int i = 0; i < header.length; i++) {
            	tableCell = new PdfPCell(new Paragraph(header[i], fontSmallRegular));
            	if(i<=2)
            		tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            	else
            		tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            	tableCell.setBorder(Rectangle.BOTTOM);
            	salesTable.addCell(tableCell);
            }
            salesTable.addCell(blankCell);

            ExportRecordHandler.addTableRow(table1, salesTable);

            for(int i = 0; i < salesjArr.length(); i++) {
                    salesTable = ExportRecordHandler.getBlankTableGST();
	            salesCell = createCell(salesjArr.getJSONObject(i).getString("taxname"), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            salesCell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 5);
	            salesTable.addCell(salesCell);
	            ExportRecordHandler.addTableRow(table1, salesTable);


	            JSONArray invArray = salesjArr.getJSONObject(i).getJSONArray("details");
	            for(int j = 0; j < invArray.length(); j++) {
	            	salesTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
	            	String invDate =invArray.getJSONObject(j).getString("invdate");
                        
                        /*Following code removes associated time of the date. So while converting date according to timezone
                         * we miss the time calculation & we do not get expected output. ERP-9201
                        */
//	            	Calendar cal = Calendar.getInstance();
//	            	cal.setTime(invDate);
//	            	cal.set(Calendar.HOUR_OF_DAY, 0);
//	            	cal.set(Calendar.MINUTE, 0);
//	            	cal.set(Calendar.SECOND, 0);
//	            	cal.set(Calendar.MILLISECOND, 0);
//	            	invDate = cal.getTime();

	            	PdfPCell invCell = createCell(invDate, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	salesTable.addCell(invCell);
	            	invCell = createCell(invArray.getJSONObject(j).getString("invno"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	salesTable.addCell(invCell);
	            	invCell = createCell(invArray.getJSONObject(j).getString("invname"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
	            	salesTable.addCell(invCell);
	            	invCell = createCell(Double.parseDouble(invArray.getJSONObject(j).optString("gramtexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(invArray.getJSONObject(j).optString("gramtexcludingtax","0.0")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	salesTable.addCell(invCell);
	            	invCell = createCell(Double.parseDouble(invArray.getJSONObject(j).getString("invtaxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(invArray.getJSONObject(j).getString("invtaxamount")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	salesTable.addCell(invCell);
	            	invCell = createCell(Double.parseDouble(invArray.getJSONObject(j).getString("invamt")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(invArray.getJSONObject(j).getString("invamt")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            	salesTable.addCell(invCell);
                        if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                            if(invArray.getJSONObject(j).has("transactionexchangerate")){
                                invCell = createCell(invArray.getJSONObject(j).getString("transactionexchangerate"), fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            } else {
                                invCell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                            }
                            salesTable.addCell(invCell);
                            if(invArray.getJSONObject(j).has("originalamountincludingtax")&&invArray.getJSONObject(j).has("transactioncurrencyid")){
                                invCell = createCell(Double.parseDouble(invArray.getJSONObject(j).getString("originalamountincludingtax")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(invArray.getJSONObject(j).getString("originalamountincludingtax")), companyid), invArray.getJSONObject(j).getString("transactioncurrencyid")) : "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);                                
                            }else {
                                invCell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                            }    
                            salesTable.addCell(invCell);
                            
                        }
	            	ExportRecordHandler.addTableRow(table1, salesTable);
	            }
                    

	            salesTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
	            salesCell = createCell("Total", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
	            salesTable.addCell(salesCell);
                    salesCell = new PdfPCell();
                    salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
                    salesTable.addCell(salesCell);
                    salesCell = new PdfPCell();
                    salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
                    salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
	            salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
	            salesTable.addCell(salesCell);
	            salesCell = createCell(Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandlerDAOObj.getFormattedCurrencyWithSign(authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")), companyid), currencyid): "", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
	            salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
	            salesTable.addCell(salesCell);
                    if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                        salesCell = new PdfPCell();
                        salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
                        salesTable.addCell(salesCell);
                        salesCell = new PdfPCell();
                        salesCell.setBorder(Rectangle.TOP|Rectangle.BOTTOM);
                        salesTable.addCell(salesCell);
                    }
	            ExportRecordHandler.addTableRow(table1, salesTable);
//	            salesTable = ExportRecordHandler.getBlankTableGST();
//	            salesTable.addCell(blankCell);
//	            salesTable.addCell(blankCellTop);
//	            salesTable.addCell(blankCellTop);
//	            salesTable.addCell(blankCellTop);
//	            ExportRecordHandler.addTableRow(table1, salesTable);
                    totalSale += Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")) != 0 ? authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale")), companyid) : 0;
                    totalSaleTax += Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")) != 0 ? authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).getString("taxamount")), companyid) : 0;
                    totalSalesAmount += Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")) != 0 ? authHandler.round(Double.parseDouble(salesjArr.getJSONObject(i).optString("totalsaleexcludingtax","0.0")), companyid) : 0;
            }

            salesTable = ExportRecordHandler.getBlankTableGSTdetailed(company);
            salesCell = createCell("Total GST Received", fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
//            salesCell.setPaddingRight(30);
            salesTable.addCell(salesCell);
            salesCell = new PdfPCell();
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            salesCell = new PdfPCell();
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSalesAmount, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSaleTax, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesCell.setBorder(0);
            salesTable.addCell(salesCell);
            salesCell = createCell(authHandlerDAOObj.getFormattedCurrencyWithSign(totalSale, currencyid), fontMediumBold1, Element.ALIGN_RIGHT, 0, 5);
            salesTable.addCell(salesCell);
            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                salesCell = new PdfPCell();
                salesCell.setBorder(0);
                salesTable.addCell(salesCell);
                salesCell = new PdfPCell();
                salesCell.setBorder(0);
                salesTable.addCell(salesCell);
            }
            ExportRecordHandler.addTableRow(table1, salesTable);
//            salesTable = ExportRecordHandler.getBlankTableGST();
//            salesTable.addCell(blankCell);
//            ExportRecordHandler.addTableRow(table1, salesTable);

            PdfPCell mainCell = new PdfPCell(table1);
            mainCell.setBorder(0);
            mainCell.setPadding(10);
            mainTable.addCell(mainCell);
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("generateGSTReportPdf: "+ex.getMessage(), ex);
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
    /**
     * Function for jasper - GST Transaction Detail Report
     * @param request
     * @param salesjArr Json for Sales Tax Received
     * @param purchasejArr Json for Purchase Tax Paid
     * @return BAOS for jasper print
     * @throws DocumentException
     * @throws ServiceException
     * @throws IOException
     * @throws SessionExpiredException 
     */
     public ByteArrayOutputStream generateGSTReportPdfDetailed(HttpServletRequest request, JSONArray salesjArr, JSONArray purchasejArr) throws DocumentException, ServiceException, IOException, SessionExpiredException {

        List<JasperPrint> l = new ArrayList<>();
        DateFormat sdf = authHandler.getUserDateFormatter(request);

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat dateFormatForTapReturn = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = authHandler.getDateOnlyFormat().parse(request.getParameter("stdate"));
            Date endDate = authHandler.getDateOnlyFormat().parse(request.getParameter("enddate"));
            ArrayList<OnlyDatePojo> datePojoList = new ArrayList<OnlyDatePojo>();
            OnlyDatePojo dobj = new OnlyDatePojo();
            dobj.setDate("");
            datePojoList.add(dobj);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) cpresult.getEntityList().get(0);
            KwlReturnObject comAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) comAccPrefResult.getEntityList().get(0);

            comAccPrefResult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) comAccPrefResult.getEntityList().get(0);
            DateFormat udf = authHandler.getUserDateFormatter(request);

            boolean isLandscape = request.getParameter("reportType").equals("11") ? true : false;
            Map<String, Object> row = Collections.EMPTY_MAP;
            List<Map<String, Object>> taxDetailList = new ArrayList<>();
            Map<String, Object> invoiceMap = new HashMap<String, Object>();


            for (int i = 0; i < purchasejArr.length(); i++) {
                String taxname = purchasejArr.getJSONObject(i).getString("taxname");
                String taxcode = purchasejArr.getJSONObject(i).optString("taxcode");
                JSONArray grArray = purchasejArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    JSONObject grObj = grArray.getJSONObject(j);
                    row = new HashMap<>();
                    row.put("type", "Purchase and Expenses with GST Paid");
//                    row.put("taxname", taxname);
                    row.put("taxname", taxcode);
//                    row.put("date", grObj.optString("jedate", ""));
                    row.put("date", grObj.optString("grdate", ""));
                    row.put("transactionid", grObj.optString("grno", ""));
                    row.put("entryno", grObj.optString("journalEntryNo", ""));
                    row.put("name", grObj.optString("grname", ""));
                    row.put("amtwithouttax", grObj.optDouble("gramtexcludingtax", 0));
                    row.put("taxamt", grObj.optDouble("grtaxamount", 0));
                    row.put("amtwithtax", grObj.optDouble("gramt", 0));
                    row.put("exchangerate", grObj.optDouble("transactionexchangerate", 0));
                    row.put("transactioncurrencysymbol", grObj.optString("transactioncurrencysymbol", ""));
                    row.put("amtintranscationcurr", grObj.optDouble("originalamountincludingtax", 0));
                    row.put("amtintranscationcurrwithSymbol", grObj.optString("transactioncurrencysymbol", "") + " " + authHandler.formattedCommaSeparatedAmount(grObj.optDouble("originalamountincludingtax", 0), companyid));
                    taxDetailList.add(row);
                }
            }


            for (int i = 0; i < salesjArr.length(); i++) {
                String taxname = salesjArr.getJSONObject(i).getString("taxname");
                String taxcode = salesjArr.getJSONObject(i).getString("taxcode");
                JSONArray grArray = salesjArr.getJSONObject(i).getJSONArray("details");
                for (int j = 0; j < grArray.length(); j++) {
                    JSONObject grObj = grArray.getJSONObject(j);
                    row = new HashMap<>();
                    row.put("type", "Sales and Income with GST Received");
//                    row.put("taxname", taxname);
                    row.put("taxname", taxcode);
//                    row.put("date", grObj.optString("jedate", ""));
                    row.put("date", grObj.optString("invdate", ""));
                    row.put("transactionid", grObj.optString("invno", ""));
                    row.put("entryno", grObj.optString("journalEntryNo", ""));
                    row.put("name", grObj.optString("invname", ""));
                    row.put("amtwithouttax", grObj.optDouble("gramtexcludingtax", 0));
                    row.put("taxamt", grObj.optDouble("invtaxamount", 0));
                    row.put("amtwithtax", grObj.optDouble("invamt", 0));
                    row.put("exchangerate", grObj.optDouble("transactionexchangerate", 0));
                    row.put("transactioncurrencysymbol", grObj.optString("transactioncurrencysymbol", ""));
                    row.put("amtintranscationcurr", grObj.optDouble("originalamountincludingtax", 0));
                    row.put("amtintranscationcurrwithSymbol", grObj.optString("transactioncurrencysymbol", "") + " " + authHandler.formattedCommaSeparatedAmount(grObj.optDouble("originalamountincludingtax", 0), companyid));
                    taxDetailList.add(row);
                }
            }
            // Export Jasper
            String searchJson = request.getParameter(Constants.Acc_Search_Json);
            invoiceMap.put("CompanyName", extraCompanyPreferences.isIsMultiEntity() ? exportDaoObj.getEntityDimensionNameforExport(searchJson, company) : company.getCompanyName() != null ? company.getCompanyName() : "");
            invoiceMap.put("CompanyAddress", AccountingAddressManager.getCompanyDefaultBillingAddress(company.getCompanyID(), accountingHandlerDAOobj));
            invoiceMap.put("datasource", new JRBeanCollectionDataSource(taxDetailList));
            invoiceMap.put("date", "From Date: " + udf.format(startDate) + ",To Date: " + udf.format(endDate));
            invoiceMap.put("currency", company.getCurrency().getName());
            InputStream inputStream = null;
            InputStream inputStreamSubreport = null;
            String filename = "/GSTDetailReport";
            String singapore = "";
            if (isLandscape) {
                filename = "/GSTDetailReportLandsacpe";
            }
            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                singapore = "Singapore";
            }
            inputStreamSubreport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + filename + singapore + "_subreport.jrxml");
            invoiceMap.put("TITLE", "GST Transaction Detailed Report");
            JasperDesign jasperDesignSubreport = JRXmlLoader.load(inputStreamSubreport);
            JasperReport jasperReportSubreport = JasperCompileManager.compileReport(jasperDesignSubreport);
            invoiceMap.put("SubReport", jasperReportSubreport);

            inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + filename + ".jrxml");

            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(datePojoList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, invoiceMap, beanColDataSource);
            l.add(jasperPrint);
            // Return Baos 
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, l);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos1);
            exporter.exportReport();

            return baos1;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("generateGSTReportPdf: " + ex.getMessage(), ex);
        }

    }
 
    public void exportMonthlyTradingJasperPdf(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException {
        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        String view = "MonthlyTradingProfitLoss";
        FinanceDetails financeDetails = new FinanceDetails();
        ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            String currencyName = company.getCurrency() != null ? company.getCurrency().getName() : "";
            
            String filterCurrencyid = null;
            if(!StringUtil.isNullOrEmpty(request.getParameter("filterCurrency"))) {
                filterCurrencyid = request.getParameter("filterCurrency");                
                if(!StringUtil.isNullOrEmpty(filterCurrencyid)) {
                    KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), filterCurrencyid);
                    KWLCurrency filterCurrency = (KWLCurrency) currencyResult.getEntityList().get(0);
                    currencyName = filterCurrency.getName();
                }
            }
            
            financeDetails.setName(company.getCompanyName());
            financeDetails.setEmail(company.getEmailID() != null ? company.getEmailID() : "");
            financeDetails.setFax(company.getFaxNumber() != null ? company.getFaxNumber() : "");
            financeDetails.setPhone(company.getPhoneNumber() != null ? company.getPhoneNumber() : "");
            financeDetails.setCurrencyinword(currencyName);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            
            ExtraCompanyPreferences extrapref = null;
            boolean isShowAccountCode = false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                isShowAccountCode = extrapref.isShowAccountCodeInFinancialReport();
            }
            
            boolean isFromBS = request.getAttribute("isFromBS")!=null?(Boolean)request.getAttribute("isFromBS"):false;
            String startDate1 = "";
            String endDate1 = "";
            if(isFromBS){
                Date sdate = authHandler.getGlobalDateFormat().parse(request.getParameter("stdate"));
                Date edate = authHandler.getGlobalDateFormat().parse(request.getParameter("enddate"));
                startDate1 = authHandler.getUserDateFormatterWithoutTimeZone(request).format(sdate);
                endDate1 = authHandler.getUserDateFormatterWithoutTimeZone(request).format(edate);
            } else {
                startDate1 = request.getParameter("stdate");
                endDate1 = request.getParameter("enddate");                
            }
            String date = "From " + startDate1 + " To " + endDate1;
            financeDetails.setDateRange(date);
            JSONObject tempObj = jobj.getJSONObject("data");
            JSONArray jArrR = tempObj.getJSONArray("right");
            JSONObject jmonthObj = jArrR.getJSONObject(jArrR.length() - 1);
            JSONArray jmonth = jmonthObj.getJSONArray("months");
            for (int i = 0; i < jmonth.length(); i++) {
                JSONObject getmon = jmonth.getJSONObject(i);
                switch (i) {
                    case 0:
                        financeDetails.setMonth_0(getmon.getString("monthname"));
                        break;
                    case 1:
                        financeDetails.setMonth_1(getmon.getString("monthname"));
                        break;
                    case 2:
                        financeDetails.setMonth_2(getmon.getString("monthname"));
                        break;
                    case 3:
                        financeDetails.setMonth_3(getmon.getString("monthname"));
                        break;
                    case 4:
                        financeDetails.setMonth_4(getmon.getString("monthname"));
                        break;
                    case 5:
                        financeDetails.setMonth_5(getmon.getString("monthname"));
                        break;
                    case 6:
                        financeDetails.setMonth_6(getmon.getString("monthname"));
                        break;
                    case 7:
                        financeDetails.setMonth_7(getmon.getString("monthname"));
                        break;
                    case 8:
                        financeDetails.setMonth_8(getmon.getString("monthname"));
                        break;
                    case 9:
                        financeDetails.setMonth_9(getmon.getString("monthname"));
                        break;
                    case 10:
                        financeDetails.setMonth_10(getmon.getString("monthname"));
                        break;
                    case 11:
                        financeDetails.setMonth_11(getmon.getString("monthname"));
                        break;
                    case 12:
                        financeDetails.setTotal(getmon.getString("monthname"));
                        break;
                }
            }
            financeDetailsMap = getMonthlyTradingProfitLossDetailsJasper(request, jobj);
            financeDetailsList.add(financeDetails);
            financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
            financeDetailsMap.put("isShowAccountCode", isShowAccountCode);

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyTradingProfitLoss.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyTradingProfitLossSubReport.jrxml");
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);

            financeDetailsMap.put("MonthlyTradingProfitLossSubReport", jasperReportSubReport);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "MonthlyTradingProfitLoss_v1.pdf");
            String fileName = request.getParameter("filename");
            if (!StringUtil.isNullOrEmpty(fileName) && fileName.equalsIgnoreCase("Monthly Revenue")) {
                   response.setHeader("Content-Disposition", "attachment;filename=" + "MonthlyRevenue_v1.pdf");
            }
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();

        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exportMonthlyBalanceSheetJasperPdf(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException {
        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        String view = "MonthlyBalanceSheet";
        FinanceDetails financeDetails = new FinanceDetails();
        ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            financeDetails.setName(company.getCompanyName());
            financeDetails.setEmail(company.getEmailID() != null ? company.getEmailID() : "");
            financeDetails.setFax(company.getFaxNumber() != null ? company.getFaxNumber() : "");
            financeDetails.setPhone(company.getPhoneNumber() != null ? company.getPhoneNumber() : "");
            financeDetails.setCurrencyinword(company.getCurrency() != null ? company.getCurrency().getName() : "");
            boolean isFromBS = request.getAttribute("isFromBS")!=null?(Boolean)request.getAttribute("isFromBS"):false;
            String startDate1 = "";
            String endDate1 = "";
            if(isFromBS){
                Date sdate = authHandler.getGlobalDateFormat().parse(request.getParameter("stdate"));
                Date edate = authHandler.getGlobalDateFormat().parse(request.getParameter("enddate"));
                startDate1 = authHandler.getUserDateFormatterWithoutTimeZone(request).format(sdate);
                endDate1 = authHandler.getUserDateFormatterWithoutTimeZone(request).format(edate);
            } else {
                startDate1 = request.getParameter("stdate");
                endDate1 = request.getParameter("enddate");                
            }
            
            ExtraCompanyPreferences extrapref = null;
            boolean isShowAccountCode = false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                isShowAccountCode = extrapref.isShowAccountCodeInFinancialReport();
            }
            
            String date = "From " + startDate1 + " To " + endDate1;
            financeDetails.setDateRange(date);
            JSONArray jmonth = jobj.getJSONArray("months");
            for (int i = 0; i < jmonth.length(); i++) {
                JSONObject getmon = jmonth.getJSONObject(i);
                switch (i) {
                    case 0:
                        financeDetails.setMonth_0(getmon.getString("monthname"));
                        break;
                    case 1:
                        financeDetails.setMonth_1(getmon.getString("monthname"));
                        break;
                    case 2:
                        financeDetails.setMonth_2(getmon.getString("monthname"));
                        break;
                    case 3:
                        financeDetails.setMonth_3(getmon.getString("monthname"));
                        break;
                    case 4:
                        financeDetails.setMonth_4(getmon.getString("monthname"));
                        break;
                    case 5:
                        financeDetails.setMonth_5(getmon.getString("monthname"));
                        break;
                    case 6:
                        financeDetails.setMonth_6(getmon.getString("monthname"));
                        break;
                    case 7:
                        financeDetails.setMonth_7(getmon.getString("monthname"));
                        break;
                    case 8:
                        financeDetails.setMonth_8(getmon.getString("monthname"));
                        break;
                    case 9:
                        financeDetails.setMonth_9(getmon.getString("monthname"));
                        break;
                    case 10:
                        financeDetails.setMonth_10(getmon.getString("monthname"));
                        break;
                    case 11:
                        financeDetails.setMonth_11(getmon.getString("monthname"));
                        break;
                    case 12:
                        financeDetails.setTotal(getmon.getString("monthname"));
                        break;
                }
            }
            financeDetailsMap = getMonthlyBalanceSheetDetailsJasper(request, jobj);
            financeDetailsList.add(financeDetails);
            financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
            financeDetailsMap.put("isShowAccountCode", isShowAccountCode);

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyBalanceSheet.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyBalanceSheetSubReport.jrxml");
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);

            financeDetailsMap.put("MonthlyBalanceSheetSubReport", jasperReportSubReport);
            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "MonthlyBalanceSheet_v1.pdf");
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<String, Object> getMonthlyBalanceSheetDetailsJasper(HttpServletRequest request, JSONObject jobj) throws ServiceException {

        ArrayList<MonthlyTradingProfitLoss> monthlyTradingProfitLossList = new ArrayList<MonthlyTradingProfitLoss>();
        HashMap<String, MonthlyTradingProfitLoss> monthlyTradingProfitLossMap = new HashMap<String, MonthlyTradingProfitLoss>();
        Map<String, Object> monthlyprolossMap = new HashMap<String, Object>();
        KWLCurrency currency = null;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray rightObjArr = jobj.getJSONArray("refright");
            JSONArray leftObjArr = jobj.getJSONArray("refleft");
            JSONObject jArrR = jobj.getJSONObject("right");
            JSONObject jArrL = jobj.getJSONObject("left");
            for (int count = 0; count < rightObjArr.length(); count++) {
                JSONObject tempobj = rightObjArr.getJSONObject(count);
                String accname = tempobj.getString("accountid");
                if (jArrR.has(accname)) {
                    JSONObject tempobjput = jArrR.getJSONObject(accname);
                    MonthlyTradingProfitLoss monthlyTradingProfitLoss = getObjectOfMonthlyTradingProfitLoss(tempobjput, paramJobj);
                    monthlyTradingProfitLossList.add(monthlyTradingProfitLoss);
                }
            }
            for (int count = 0; count < leftObjArr.length(); count++) {
                JSONObject tempobj = leftObjArr.getJSONObject(count);
                String accname = tempobj.getString("accountid");
                if (jArrL.has(accname)) {
                    JSONObject tempobjput = jArrL.getJSONObject(accname);
                    MonthlyTradingProfitLoss monthlyTradingProfitLoss = getObjectOfMonthlyTradingProfitLoss(tempobjput, paramJobj);
                    monthlyTradingProfitLossList.add(monthlyTradingProfitLoss);
                }
            }
            monthlyprolossMap.put("MonthlyBalanceSheetSubReportData", new JRBeanCollectionDataSource(monthlyTradingProfitLossList));
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getMonthlyBalanceSheetJasper : " + ex.getMessage(), ex);
        }
        return monthlyprolossMap;
    }

    public Map<String, Object> getMonthlyTradingProfitLossDetailsJasper(HttpServletRequest request, JSONObject jobj) throws ServiceException {

        ArrayList<MonthlyTradingProfitLoss> monthlyTradingProfitLossList = new ArrayList<MonthlyTradingProfitLoss>();
        HashMap<String, MonthlyTradingProfitLoss> monthlyTradingProfitLossMap = new HashMap<String, MonthlyTradingProfitLoss>();
        Map<String, Object> monthlyprolossMap = new HashMap<String, Object>();
        KWLCurrency currency = null;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONObject tempObj = jobj.getJSONObject("data");
            JSONArray jArrL = tempObj.getJSONArray("left");
            for (int i = 0; i < jArrL.length(); i++) { // looping thru left array
                JSONObject leftobj = jArrL.getJSONObject(i);
                MonthlyTradingProfitLoss monthlyTradingProfitLoss = getObjectOfMonthlyTradingProfitLoss(leftobj, paramJobj);
                monthlyTradingProfitLossList.add(monthlyTradingProfitLoss);
            }

            monthlyprolossMap.put("MonthlyTradingProfitLossSubReportData", new JRBeanCollectionDataSource(monthlyTradingProfitLossList));
            String fileName = request.getParameter("filename");
            String title="Monthly Trading Profit Loss";
            if (!StringUtil.isNullOrEmpty(fileName) && fileName.equalsIgnoreCase("Monthly Revenue")) {
                title="Monthly Revenue";
            }
            monthlyprolossMap.put("title",title);
        } catch (Exception ex) {
            Logger.getLogger(accOtherReportsController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getMonthlyTradingProfitLossJasper : " + ex.getMessage(), ex);
        }
        return monthlyprolossMap;
    }

    public MonthlyTradingProfitLoss getObjectOfMonthlyTradingProfitLoss(JSONObject leftobj, JSONObject paramJobj) throws JSONException {
        MonthlyTradingProfitLoss monthlyTradingProfitLoss = new MonthlyTradingProfitLoss();
        String companyid=paramJobj.optString("companyid");
        int level = Integer.parseInt(leftobj.get("level").toString());
        switch (level) {
            case 0:
                monthlyTradingProfitLoss.setAccName(leftobj.getString("accountname"));
                break;
            case 1:
                monthlyTradingProfitLoss.setAccName1(leftobj.getString("accountname"));
                monthlyTradingProfitLoss.setAccCode(leftobj.optString("accountcode"));
                break;
            case 2:
                monthlyTradingProfitLoss.setAccName2(leftobj.getString("accountname"));
                monthlyTradingProfitLoss.setAccCode(leftobj.optString("accountcode"));
                break;
            case 3:
                monthlyTradingProfitLoss.setAccName3(leftobj.getString("accountname"));
                monthlyTradingProfitLoss.setAccCode(leftobj.optString("accountcode"));
                break;
            case 4:
                monthlyTradingProfitLoss.setAccName3(leftobj.getString("accountname"));
                monthlyTradingProfitLoss.setAccCode(leftobj.optString("accountcode"));
                break;
            case 5:
                monthlyTradingProfitLoss.setAccName3(leftobj.getString("accountname"));
                monthlyTradingProfitLoss.setAccCode(leftobj.optString("accountcode"));
                break;
        }
        if (leftobj.has("amount_0")) {
            monthlyTradingProfitLoss.setAmount_0(leftobj.get("amount_0").toString().equals("") ? "" : (leftobj.get("amount_0").toString().equals("-") || leftobj.get("amount_0").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_0"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_0("");
        }
        if (leftobj.has("amount_1")) {
            monthlyTradingProfitLoss.setAmount_1(leftobj.get("amount_1").toString().equals("") ? "" : (leftobj.get("amount_1").toString().equals("-") || leftobj.get("amount_1").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_1"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_1("");
        }
        if (leftobj.has("amount_2")) {
            monthlyTradingProfitLoss.setAmount_2(leftobj.get("amount_2").toString().equals("") ? "" : (leftobj.get("amount_2").toString().equals("-") || leftobj.get("amount_2").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_2"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_2("");
        }
        if (leftobj.has("amount_3")) {
            monthlyTradingProfitLoss.setAmount_3(leftobj.get("amount_3").toString().equals("") ? "" : (leftobj.get("amount_3").toString().equals("-") || leftobj.get("amount_3").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_3"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_3("");
        }
        if (leftobj.has("amount_4")) {
            monthlyTradingProfitLoss.setAmount_4(leftobj.get("amount_4").toString().equals("") ? "" : (leftobj.get("amount_4").toString().equals("-") || leftobj.get("amount_4").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_4"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_4("");
        }
        if (leftobj.has("amount_5")) {
            monthlyTradingProfitLoss.setAmount_5(leftobj.get("amount_5").toString().equals("") ? "" : (leftobj.get("amount_5").toString().equals("-") || leftobj.get("amount_5").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_5"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_5("");
        }
        if (leftobj.has("amount_6")) {
            monthlyTradingProfitLoss.setAmount_6(leftobj.get("amount_6").toString().equals("") ? "" : (leftobj.get("amount_6").toString().equals("-") || leftobj.get("amount_6").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_6"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_6("");
        }
        if (leftobj.has("amount_7")) {
            monthlyTradingProfitLoss.setAmount_7(leftobj.get("amount_7").toString().equals("") ? "" : (leftobj.get("amount_7").toString().equals("-") || leftobj.get("amount_7").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_7"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_7("");
        }
        if (leftobj.has("amount_8")) {
            monthlyTradingProfitLoss.setAmount_8(leftobj.get("amount_8").toString().equals("") ? "" : (leftobj.get("amount_8").toString().equals("-") || leftobj.get("amount_8").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_8"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_8("");
        }
        if (leftobj.has("amount_9")) {
            monthlyTradingProfitLoss.setAmount_9(leftobj.get("amount_9").toString().equals("") ? "" : (leftobj.get("amount_9").toString().equals("-") || leftobj.get("amount_9").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_9"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_9("");
        }
        if (leftobj.has("amount_10")) {
            monthlyTradingProfitLoss.setAmount_10(leftobj.get("amount_10").toString().equals("") ? "" : (leftobj.get("amount_10").toString().equals("-") || leftobj.get("amount_10").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_10"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_10("");
        }
        if (leftobj.has("amount_11")) {
            monthlyTradingProfitLoss.setAmount_11(leftobj.get("amount_11").toString().equals("") ? "" : (leftobj.get("amount_11").toString().equals("-") || leftobj.get("amount_11").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_11"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_11("");
        }
        if (leftobj.has("amount_12")) {
            monthlyTradingProfitLoss.setAmount_12(leftobj.get("amount_12").toString().equals("") ? "" : (leftobj.get("amount_12").toString().equals("-") || leftobj.get("amount_12").toString().contains("<")) ? "0.0" : authHandler.formattedCommaSeparatedAmount(leftobj.optDouble("amount_12"), companyid));
        } else {
            monthlyTradingProfitLoss.setAmount_12("");
        }
        return monthlyTradingProfitLoss;
    }
    // being used by Monthly Trading and Profit/Loss report
    public ByteArrayOutputStream exportMonthlyTradingPdf(HttpServletRequest request, String currencyid,DateFormat formatter, 
    		String logoPath,String comName,JSONObject jobj,Date startDate,Date endDate,List monthList, int flag, int toggle, String address) throws DocumentException, ServiceException, IOException {
    	
        ByteArrayOutputStream baos = null;
         double total = 0;
         Document document = null;
         PdfWriter writer = null;
         try {
             String companyid = sessionHandlerImpl.getCompanyid(request);
             boolean isAlignment =Boolean.parseBoolean((String)request.getParameter("isAlignment"));       
             String headingString = "";
             String DateheadingString   = "";
             String value = "";
             String subHeading1 = "";
             String subHeading2 = "";
             
             headingString = messageSource.getMessage("acc.rem.125", null, RequestContextUtils.getLocale(request)); // "P&L Statement For : ";
             DateheadingString = messageSource.getMessage("acc.rem.126", null, RequestContextUtils.getLocale(request)); // "P&L Statement From-To :";
             value = formatter.format(startDate)+" "+messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" "+formatter.format(endDate);
             subHeading1 = "Amount";//messageSource.getMessage("acc.P&L.Amount(Debit)", null, RequestContextUtils.getLocale(request));   //"Debit";
             subHeading2 = messageSource.getMessage("acc.P&L.Amount(Credit)", null, RequestContextUtils.getLocale(request));   //"Credit";
             
             baos = new ByteArrayOutputStream();
             // SON - ATTEMP TO ROTATE THE PAGE TO LANDSCAPE MODE
             document = new Document(PageSize.A4.rotate(), 15, 15, 15, 15);
             
             writer = PdfWriter.getInstance(document, baos);
             document.open();
             PdfPTable mainTable = new PdfPTable(1);
             mainTable.setWidthPercentage(100);

             PdfPTable tab1 = null;
             Rectangle page = document.getPageSize();

             int bmargin = 15;  //border margin
             PdfContentByte cb = writer.getDirectContent();
             cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
             cb.setColorStroke(Color.WHITE);
             cb.stroke();

             PdfPCell blankCell = new PdfPCell();
             blankCell.setBorder(0);
             PdfPCell LineCell = new PdfPCell();
             LineCell.setBorder(0);
             LineCell.setBorderWidthBottom(1);

             PdfPTable tableMain = new PdfPTable(3);
             tableMain.setWidthPercentage(100);
             tableMain.setWidths(new float[]{10, 80, 10});

             tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
             PdfPCell cell1 = new PdfPCell(tab1);
             cell1.setBorder(0);
             tableMain.addCell(blankCell);
             tableMain.addCell(cell1);
             tableMain.addCell(blankCell);

             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);

             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);

             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);
             tableMain.addCell(blankCell);

             PdfPCell headerCell =  createCell(comName, fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
             headerCell.setBorder(0);
             headerCell.setPaddingBottom(5);
             tableMain.addCell(blankCell);
             tableMain.addCell(headerCell);
             
             PdfPCell headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
             headerNameCell.setBorder(0);
             headerNameCell.setPaddingBottom(5);
             tableMain.addCell(headerNameCell);
             //tableMain.addCell(blankCell);

             headerCell =  createCell("Statement of profit or loss and other comprehensive income for the year ended "+
             formatter.format(endDate)+"  (Amount in "+exportDaoObj.currencyRender("", currencyid, companyid)+")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
             
             headerCell.setBorder(0);
             headerCell.setPaddingBottom(5);
             tableMain.addCell(blankCell);
             tableMain.addCell(headerCell);
             headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
             headerNameCell.setBorder(0);
             headerNameCell.setPaddingBottom(5);
             tableMain.addCell(headerNameCell);
             //tableMain.addCell(blankCell);
             
             for(int i = 0; i < 18; i++)
                 tableMain.addCell(blankCell);

             PdfPTable table1 = new PdfPTable(4);
             table1.setWidthPercentage(100);
             table1.setWidths(new float[]{10, 60, 20, 10});        

             PdfPCell cell2 = null;
             PdfPCell cell3 = ExportRecordHandler.createBalanceSheetCell("Income", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
             PdfPCell cell4 = ExportRecordHandler.createBalanceSheetCell("", FontContext.TABLE_HEADER, Element.ALIGN_RIGHT, 0, 0,0);
             cell3.setPaddingBottom(5);

             jobj = jobj.getJSONObject("data");
             
             JSONArray rightObjArr = flag==1?jobj.getJSONArray("right"):jobj.getJSONArray("left");
             JSONArray leftObjArr = flag==1?jobj.getJSONArray("left"):jobj.getJSONArray("right");
             if(toggle == 1){
             	rightObjArr = jobj.getJSONArray("left");
                 leftObjArr = jobj.getJSONArray("right");
             }
             
             JSONArray finalValArr = jobj.getJSONArray("total");
             PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.P&L.particulars", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
             HeaderCell1.setPaddingBottom(5);
             
             // SON - ATTEMP TO PRINT OUT ALL THE MONTHS
             PdfPTable monthsTable = new PdfPTable(monthList.size()+1); // Code 1
             monthsTable.addCell(HeaderCell1);
             
             for(int i=0; i<monthList.size(); i++){
            	 PdfPCell HeaderCell2 = createCell(monthList.get(i).toString(), fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                 HeaderCell2.setPaddingBottom(5);
                 
                 monthsTable.addCell(HeaderCell2);
//                 monthsTable.addCell(blankCell); // this causes only 6 months to be printed out
             }
             ExportRecordHandler.addTableRow(mainTable, tableMain);              
             ExportRecordHandler.addTableRow(mainTable, monthsTable);             
             
             double totalAsset = Double.parseDouble(finalValArr.getString(0));
             double totalLibility = Double.parseDouble(finalValArr.getString(1));

             if(flag==2) {
                 int i = 0,j = 1, accountNumbers = 0;
                 boolean existflag = false;
                 
                 for(i = 0; i < rightObjArr.length(); i++){                	 
                	 JSONObject rightJSONObj = rightObjArr.getJSONObject(i);                	 
                     leftObjArr.put(rightJSONObj);
                 }                 
                 
                 PdfPTable incomeAccountsGroupTable = new PdfPTable(monthList.size()+1); // Code 1
                 
                 incomeAccountsGroupTable.addCell(cell3);                 
                 for(int monthIndex=0; monthIndex<monthList.size(); monthIndex++){
                	 incomeAccountsGroupTable.addCell(cell4);	                		                		
                 }                 
                 incomeAccountsGroupTable.addCell(blankCell);                 
                 ExportRecordHandler.addTableRow(mainTable, incomeAccountsGroupTable);
                                                   
                 // FIRST, PRINT OUT INCOME ACCOUNTS GROUP
                 for(i = 0; i < leftObjArr.length(); i++) { // looping thru left array
                     JSONObject leftobj = leftObjArr.getJSONObject(i);
                     
                     if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                         JSONObject temp = leftObjArr.getJSONObject(i + 1);
                         JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                         
                         if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                             leftobj.put("bold", true);
                         
                         else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                             leftobj.put("bold", true);
                     }
                     
                     if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
                         if(!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                        	 
                        	 PdfPTable incomeAccountsTable = new PdfPTable(monthList.size()+1); // Code 1                            	                             	 
                        	 ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,incomeAccountsTable,currencyid,isAlignment, monthList);                            	 
                        	 // add 3 blank cells
                        	 for(int cellCount=0; cellCount<3; cellCount++){
                        		 incomeAccountsTable.addCell(blankCell);
                        	 }                                               	 
                             ExportRecordHandler.addTableRow(mainTable, incomeAccountsTable);                                              
                             
                             
                         }
                     }
                 }// end looping thru left array
                     
                  // SECOND, PRINT OUT EXPENSE ACCOUNTS GROUP     

                 cell3 = ExportRecordHandler.createBalanceSheetCell("Expense", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
                 cell3.setPaddingBottom(5);                     
                 
                 PdfPTable expensesAccountsGroupTable = new PdfPTable(monthList.size()+1); // Code 1
                 expensesAccountsGroupTable.addCell(cell3);
                 for(int monthIndex=0; monthIndex<monthList.size(); monthIndex++){
                	 expensesAccountsGroupTable.addCell(cell4);	                		                		
                 }
                 expensesAccountsGroupTable.addCell(blankCell);
                 
                 ExportRecordHandler.addTableRow(mainTable, expensesAccountsGroupTable);                                       
                     
                 for(i = 0; i < leftObjArr.length(); i++) {// looping thru left array
                     JSONObject leftobj = leftObjArr.getJSONObject(i);
                     
                     if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                         JSONObject temp = leftObjArr.getJSONObject(i + 1);
                         JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                         if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                             leftobj.put("bold", true);
                         else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                             leftobj.put("bold", true);
                     }
                     
                     
                     if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("expense") && leftobj.has("accountname") && !leftobj.get("accountname").toString().equals("")) {
                         if(!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("expense"))) {                            	 
                        	 
                        	 PdfPTable expenseAccountsTable = new PdfPTable(monthList.size()+1); // Code 1                            	                             	 
                        	 ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,expenseAccountsTable,currencyid,isAlignment, monthList);                            	 
                        	 // add 3 blank cells
                        	 for(int cellCount=0; cellCount<3; cellCount++){
                        		 expenseAccountsTable.addCell(blankCell);
                        	 }                                               	 
                             ExportRecordHandler.addTableRow(mainTable, expenseAccountsTable);                                                                         
                         }
                     }
                 }// end looping thru left array
                     
                  // THIRD, PRINT OUT GROSS PROFIT OR GROSS LOSS     

                     for(i = 0; i < leftObjArr.length(); i++) {// looping thru left array

                         JSONObject leftobj = leftObjArr.getJSONObject(i);
                         if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit") ) {
                        	 
                        	 PdfPTable grossProfitTable = new PdfPTable(monthList.size()+1); // Code 1                            	                             	 
                        	 ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,grossProfitTable,currencyid,isAlignment, monthList);                            	 
                        	 // add 4 blank cells
                        	 for(int cellCount=0; cellCount<4; cellCount++){
                        		 grossProfitTable.addCell(blankCell);
                        	 }
                        	 ExportRecordHandler.addTableRow(mainTable, grossProfitTable);
                        	 

                         } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss") ) {
                        	 
                        	 PdfPTable grossLossTable = new PdfPTable(monthList.size()+1); // Code 1                            	                             	 
                        	 ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,grossLossTable,currencyid,isAlignment, monthList);                            	 
                        	 // add 4 blank cells
                        	 for(int cellCount=0; cellCount<4; cellCount++){
                        		 grossLossTable.addCell(blankCell);
                        	 }
                        	 ExportRecordHandler.addTableRow(mainTable, grossLossTable);
                        	 
                         }
                     }// end looping thru left array
                     
                  // FOURTH, PRINT OUT OTHER INCOME ACCOUNTS GROUP 

                     int count = 1;
                     for(i = 0; i < leftObjArr.length(); i++) {// looping thru left array

                         JSONObject leftobj = leftObjArr.getJSONObject(i);  
                         if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                             JSONObject temp = leftObjArr.getJSONObject(i + 1);
                             JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                             if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                 leftobj.put("bold", true);
                             else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                 leftobj.put("bold", true);
                         }
                         if(leftobj.has("group") && leftobj.get("group").toString().equals("income")) {
                        	 
                        	 PdfPTable otherIncomeAccountsTable = new PdfPTable(monthList.size()+1); // Code 1                            	                             	 
                             ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,otherIncomeAccountsTable,currencyid,isAlignment, monthList);       
                             
                             // add 4 blank cells
                        	 for(int cellCount=0; cellCount<3; cellCount++){
                        		 otherIncomeAccountsTable.addCell(blankCell);
                        	 }
                        	 
                             ExportRecordHandler.addTableRow(mainTable, otherIncomeAccountsTable);
                             
                         }
                     }// end looping thru left array
                     
                  // FIFTH, PRINT OUT OTHER EXPENSE ACCOUNTS GROUP 

                     count = 1;
                     for(i = 0; i < leftObjArr.length(); i++) { // looping thru left array
                         JSONObject leftobj = leftObjArr.getJSONObject(i);
                         if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                             JSONObject temp = leftObjArr.getJSONObject(i + 1);
                             JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                             if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                 leftobj.put("bold", true);
                             else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                 leftobj.put("bold", true);
                         }
                         if(leftobj.has("group") && leftobj.get("group").toString().equals("expense")) {
                        	 PdfPTable otherExpenseAccountsTable = new PdfPTable(monthList.size()+1); // Code 1          
                             ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,otherExpenseAccountsTable,currencyid,isAlignment, monthList); 
                             // add 4 blank cells
                        	 for(int cellCount=0; cellCount<4; cellCount++){
                        		 otherExpenseAccountsTable.addCell(blankCell);
                        	 }
                             ExportRecordHandler.addTableRow(mainTable, otherExpenseAccountsTable);
                         }
                     }// end looping thru left array
                     
                  // LAST, PRINT OUT NET PROFIT OR NET LOSS                      
                     
                     for(i = 0; i < leftObjArr.length(); i++) {// looping thru left array
                         JSONObject leftobj = leftObjArr.getJSONObject(i);
                         
                         PdfPTable netProfitLossTable = new PdfPTable(monthList.size()+1); // Code 1
                         
                         if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netprofit") ) {
                             
                             ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,netProfitLossTable,currencyid,isAlignment, monthList);
                             // add 4 blank cells
                        	 for(int cellCount=0; cellCount<4; cellCount++){
                        		 netProfitLossTable.addCell(blankCell);
                        	 }
                             ExportRecordHandler.addTableRow(mainTable, netProfitLossTable);
                             
                             leftobj.remove("fmt");

                         } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netloss") ) {
                        	 ExportRecordHandler.addMonthlyBalanceSheetCell(leftobj,netProfitLossTable,currencyid,isAlignment, monthList);
                             // add 4 blank cells
                        	 for(int cellCount=0; cellCount<4; cellCount++){
                        		 netProfitLossTable.addCell(blankCell);
                        	 }
                             ExportRecordHandler.addTableRow(mainTable, netProfitLossTable);
                         }
                     }// end looping thru left array
             }
             
             document.add(mainTable);
         } catch (Exception ex) {
//             System.out.println(ex.toString());
//             ex.printStackTrace();
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
         return baos;
     }
    

    
    public ByteArrayOutputStream exportTradingPdf(HttpServletRequest request, String currencyid,DateFormat formatter, String logoPath,String comName,JSONObject jobj,Date startDate,Date endDate,int flag, int toggle, String address,Date endPreDate) throws DocumentException, ServiceException, IOException {
       ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {
            boolean isForTradingAndProfitLoss = false;
            if ((request.getAttribute("isForTradingAndProfitLoss") != null)) {
                isForTradingAndProfitLoss = Boolean.parseBoolean(request.getAttribute("isForTradingAndProfitLoss").toString());
            }
            String isCompareGlobal = request.getParameter("isCompareGlobal");
            String isSelectedCurrencyDiff = request.getParameter("isSelectedCurrencyDiff");
            String filterCurrency = request.getParameter("filterCurrency");
            Date  startPreDate = null;
            if (!StringUtil.isNullOrEmpty(request.getParameter("stpredate"))) {
                startPreDate = authHandler.getDateOnlyFormat().parse(request.getParameter("stpredate"));
            }
            Boolean isCompare = isCompareGlobal!=null?Boolean.parseBoolean(isCompareGlobal):true;
            Boolean isSelectedCurrencyDiffFlag = isSelectedCurrencyDiff!=null?Boolean.parseBoolean(isSelectedCurrencyDiff):false;
             boolean isAlignment =Boolean.parseBoolean((String)request.getParameter("isAlignment"));
             boolean periodView=Boolean.parseBoolean(request.getParameter("periodView"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            ExtraCompanyPreferences extrapref = null;
            boolean isShowAccountCode = false;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                isShowAccountCode = extrapref.isShowAccountCodeInFinancialReport();
            }
            
            request.setAttribute("isShowAccountCode", isShowAccountCode);;
            
            String headingString = "";
            String DateheadingString   = "";
            String value = "";
            String prevalue = "";
            String subHeading1 = "";
            String subHeading2 = "";
            String subHeading3 = "";
            headingString = messageSource.getMessage("acc.rem.125", null, RequestContextUtils.getLocale(request)); // "P&L Statement For : ";
            DateheadingString = messageSource.getMessage("acc.rem.126", null, RequestContextUtils.getLocale(request)); // "P&L Statement From-To :";
            value = formatter.format(startDate) + " " + messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request)) + " " + formatter.format(endDate);
            if(periodView){
                subHeading1 = "Opening Amount";
                subHeading2 = "Period Amount";
                subHeading3 = "Ending Amount";
            }else{    
                subHeading1 = "Amount";//messageSource.getMessage("acc.P&L.Amount(Debit)", null, RequestContextUtils.getLocale(request));   //"Debit";
                subHeading2 = messageSource.getMessage("acc.P&L.Amount(Credit)", null, RequestContextUtils.getLocale(request));   //"Credit";            
            }
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            PdfPCell LineCell = new PdfPCell();
            LineCell.setBorder(0);
            LineCell.setBorderWidthBottom(1);


            PdfPTable tab2 = new PdfPTable(1);
            PdfPTable tableMain = new PdfPTable(2);
            tableMain.setWidthPercentage(100);
            tableMain.setWidths(new float[]{5, 85});
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) result.getEntityList().get(0);
            if (ExportRecordHandler.checkCompanyTemplateLogoPresent(company)) {
                tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, company);
            } else {
                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
                String companyArray[] = new String[4];
                companyArray[0] = company.getCompanyName();
                companyArray[1] = company.getAddress();
                companyArray[2] = company.getEmailID();
                companyArray[3] = company.getPhoneNumber();
                tab2 = ExportRecordHandler.getCompanyInfo(companyArray);
            }
            
            PdfPCell cell1 = new PdfPCell(tab1);
            PdfPCell cellcompanyInfo = new PdfPCell(tab2);
            cell1.setBorder(0);
            cellcompanyInfo.setBorder(0);
            
            tableMain.addCell(blankCell);
            tableMain.addCell(cell1);

            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);
            tableMain.addCell(cellcompanyInfo);
            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);            
            tableMain.addCell(blankCell);
            
            tableMain.addCell(blankCell);            
            tableMain.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, tableMain);

            tableMain = new PdfPTable(3);
            tableMain.setWidthPercentage(100);
            tableMain.setWidths(new float[]{5, 85, 10}); 

            PdfPCell reportPeriodCell = createCell("Reporting Period: " + formatter.format(startDate) + " to " + formatter.format(endDate), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            reportPeriodCell.setBorder(0);
            reportPeriodCell.setPaddingBottom(5);
            tableMain.addCell(blankCell);
            tableMain.addCell(reportPeriodCell);
            tableMain.addCell(blankCell);

            PdfPCell headerCell =  createCell("Statement of profit or loss and other comprehensive income for the year ended "+formatter.format(endDate)+"  (Amount in "+exportDaoObj.currencyRender("", currencyid, companyid)+")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            headerCell.setBorder(0);
            headerCell.setPaddingBottom(5);
            tableMain.addCell(blankCell);
            tableMain.addCell(headerCell);
            PdfPCell headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
            headerNameCell.setBorder(0);
            headerNameCell.setPaddingBottom(5);
            tableMain.addCell(headerNameCell);
            //tableMain.addCell(blankCell);

            for(int i = 0; i < 18; i++)
                tableMain.addCell(blankCell);


            PdfPTable table1 = new PdfPTable(5);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{10, 50, 15, 15, 10});

            PdfPCell cell2 = null;
            PdfPCell cell3 = ExportRecordHandler.createBalanceSheetCell("Income", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
            PdfPCell cell4 = ExportRecordHandler.createBalanceSheetCell("", FontContext.TABLE_HEADER, Element.ALIGN_RIGHT, 0, 0,0);
//            cell3.setBorderWidthBottom(3);
//            cell3.setBorderColor(Color.GRAY);
              cell3.setPaddingBottom(5);
//            cell4.setBorderWidthBottom(3);
//            cell4.setBorderColor(Color.gray);

            jobj = jobj.getJSONObject("data");
            JSONArray rightObjArr = flag==1?jobj.getJSONArray("right"):jobj.getJSONArray("left");
            JSONArray leftObjArr = flag==1?jobj.getJSONArray("left"):jobj.getJSONArray("right");
            if(toggle == 1){
            	rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            JSONArray finalValArr = jobj.getJSONArray("total");
            JSONArray prefinalValArr = jobj.getJSONArray("pretotal");
            
            PdfPCell accountcodeHeaderCell1 = createCell(messageSource.getMessage("acc.coa.accCode", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            accountcodeHeaderCell1.setPadding(5);
            PdfPCell HeaderCell1  = createCell(messageSource.getMessage("acc.P&L.particulars", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            HeaderCell1.setPadding(5);
            
            if (periodView) {
                PdfPCell   HeaderCell2 = createCell(subHeading1, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell2.setPadding(5);
                PdfPCell HeaderCell3 = createCell(subHeading2, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell3.setPadding(5);
                PdfPCell HeaderCell4 = createCell(subHeading3, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCell4.setPadding(5);
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(accountcodeHeaderCell1);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderCell3);
                    table1.addCell(HeaderCell4);
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                    table1.addCell(blankCell);
                    table1.addCell(HeaderCell1);
                    table1.addCell(HeaderCell2);
                    table1.addCell(HeaderCell3);
                    table1.addCell(HeaderCell4);
                }
            } else {
                //HeaderCell1.setBorderWidthBottom(1);
                PdfPCell HeaderCell2 = createCell("", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                if (endDate != null && startDate !=null) {
                    HeaderCell2 = createCell(formatter.format(startDate)+" to "+formatter.format(endDate), fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                }
                if (!isCompare) {
                    if (isSelectedCurrencyDiffFlag) {
                        HeaderCell2 = createCell(formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    }
                }
                HeaderCell2.setPadding(5);
                PdfPCell HeaderCellDiffCurrency = createCell(formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", filterCurrency, companyid) + ")", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                HeaderCellDiffCurrency.setPadding(5);
                PdfPCell HeaderPreCell2 = createCell("", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                if (endPreDate != null && startPreDate !=null) {
                    HeaderPreCell2 = createCell(formatter.format(startPreDate)+" to "+formatter.format(endPreDate), fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                }
                HeaderPreCell2.setPadding(5);
                //HeaderCell2.setBorderWidthBottom(1);
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare){
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
                if (isShowAccountCode) {
                    table1.addCell(accountcodeHeaderCell1);
                }
                table1.addCell(HeaderCell2);
                if (isCompare) {
                    table1.addCell(HeaderPreCell2);
                } else {
                    if (isSelectedCurrencyDiffFlag) {
//                    "  (Amount in "+exportDaoObj.currencyRender("", currencyid)+")"
                        table1.addCell(HeaderCellDiffCurrency);
                    } else {
                        table1.addCell(blankCell);
                    }
                }
            }
            table1.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, tableMain);
            ExportRecordHandler.addTableRow(mainTable, table1);
            if (periodView) {
                if (isShowAccountCode) {
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    ExportRecordHandler.addTableRow(mainTable, table1);
                    table1 = ExportRecordHandler.getBlankTable6Columns();
                } else {
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    table1.addCell(blankCell);
                    ExportRecordHandler.addTableRow(mainTable, table1);
                    table1 = ExportRecordHandler.getBlankTable5Columns();
                }
            } else {
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else{
                    if (isCompare){
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
                if (isShowAccountCode) {
                    table1.addCell(blankCell);
                }
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
                table1.addCell(blankCell);
//            addTableRow(mainTable, tableMain);
                ExportRecordHandler.addTableRow(mainTable, table1);
                if (isShowAccountCode) {
                    if (isCompare) {
                        table1 = ExportRecordHandler.getBlankTable5Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                    }
                } else {
                    if (isCompare){
                        table1 = ExportRecordHandler.getBlankTable4Columns();
                    } else {
                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                    }
                }
            }
            
            double totalAsset = Double.parseDouble(finalValArr.getString(0));
            double totalLibility = Double.parseDouble(finalValArr.getString(1));

            if(flag==2) {
                int i = 0,j = 1, accountNumbers = 0;
                boolean existflag = false;
                for(i = 0; i < rightObjArr.length(); i++)
                    leftObjArr.put(rightObjArr.getJSONObject(i));
                    table1.addCell(blankCell);
                    table1.addCell(cell3);
                    if(isShowAccountCode){
                        table1.addCell(blankCell);
                    }
                    table1.addCell(cell4);
                    table1.addCell(blankCell);
                    if(periodView){
                        table1.addCell(blankCell);
                    }
                    for(i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                            JSONObject temp = leftObjArr.getJSONObject(i + 1);
                            JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                            if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                leftobj.put("bold", true);
                            else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                leftobj.put("bold", true);
                        }
                        if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
                            if(!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                                ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);                          
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (periodView) {
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                            }
                        }
                    }
                if (isForTradingAndProfitLoss) { // show CoGS group seperately
                    cell3 = ExportRecordHandler.createBalanceSheetCell("Cost of Goods Sold", fontSmallBold1, Element.ALIGN_LEFT, 0, 0, 0);
                    cell3.setPaddingBottom(5);
                    table1.addCell(blankCell);
                    table1.addCell(cell3);
                    if (isShowAccountCode) {
                        table1.addCell(blankCell);
                    }
                    table1.addCell(cell4);
                    table1.addCell(blankCell);
                    if (periodView) {
                        table1.addCell(blankCell);
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        if (isShowAccountCode) {
                            table1 = ExportRecordHandler.getBlankTable6Columns();
                        } else {
                            table1 = ExportRecordHandler.getBlankTable5Columns();
                        }
                    } else {
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        if (isShowAccountCode) {
                            if (isCompare) {
                                table1 = ExportRecordHandler.getBlankTable5Columns();
                            } else {
                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                            }
                        } else {
                            if (isCompare) {
                                table1 = ExportRecordHandler.getBlankTable4Columns();
                            } else {
                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                            }
                        }
                    }
                    for (i = 0; i < leftObjArr.length(); i++) {
                            JSONObject leftobj = leftObjArr.getJSONObject(i);
                            if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                                JSONObject temp = leftObjArr.getJSONObject(i + 1);
                                JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                                if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString())) {
                                    leftobj.put("bold", true);
                                } else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString())) {
                                    leftobj.put("bold", true);
                                }
                            }
                            if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("costofgoodssold")) {
                                if (!leftobj.has("group") || (leftobj.has("group") && leftobj.get("group").toString().equals("costofgoodssold"))) {
                                    ExportRecordHandler.addBalanceSheetCell(request, leftobj, table1, currencyid, isAlignment);
                                    table1.addCell(blankCell);
                                    table1.addCell(blankCell);
                                    table1.addCell(blankCell);
                                    table1.addCell(blankCell);
                                    if (periodView) {
                                        table1.addCell(blankCell);
                                        ExportRecordHandler.addTableRow(mainTable, table1);
                                        if (isShowAccountCode) {
                                            table1 = ExportRecordHandler.getBlankTable6Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        }
                                    } else {
                                        ExportRecordHandler.addTableRow(mainTable, table1);
                                        if (isShowAccountCode) {
                                            if (isCompare) {
                                                table1 = ExportRecordHandler.getBlankTable5Columns();
                                            } else {
                                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                            }
                                        } else {
                                            if (isCompare) {
                                                table1 = ExportRecordHandler.getBlankTable4Columns();
                                            } else {
                                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    for (i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                            JSONObject temp = leftObjArr.getJSONObject(i + 1);
                            JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                            if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString())) {
                                leftobj.put("bold", true);
                            } else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString())) {
                                leftobj.put("bold", true);
                            }
                        }
                        if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("totalcogs")) {
                            ExportRecordHandler.addBalanceSheetCell(request, leftobj, table1, currencyid, isAlignment);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            if (periodView) {
                                table1.addCell(blankCell);
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    table1 = ExportRecordHandler.getBlankTable6Columns();
                                } else {
                                    table1 = ExportRecordHandler.getBlankTable5Columns();
                                }
                            } else {
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                    }
                                } else {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable4Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                    }
                                }
                            }
                        }
                    }
                    for (i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit")) {
                            ExportRecordHandler.addBalanceSheetCell(request, leftobj, table1, currencyid, isAlignment);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            if (periodView) {
                                table1.addCell(blankCell);
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    table1 = ExportRecordHandler.getBlankTable6Columns();
                                } else {
                                    table1 = ExportRecordHandler.getBlankTable5Columns();
                                }
                            } else {
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                    }
                                } else {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable4Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                    }
                                }
                            }

                        } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss")) {
                            ExportRecordHandler.addBalanceSheetCell(request, leftobj, table1, currencyid, isAlignment);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            if (periodView) {
                                table1.addCell(blankCell);
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    table1 = ExportRecordHandler.getBlankTable6Columns();
                                } else {
                                    table1 = ExportRecordHandler.getBlankTable5Columns();
                                }
                            } else {
                                ExportRecordHandler.addTableRow(mainTable, table1);
                                if (isShowAccountCode) {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                    }
                                } else {
                                    if (isCompare) {
                                        table1 = ExportRecordHandler.getBlankTable4Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                    }
                                }
                            }
                        }

                    }
                    
                }
                    cell3 = ExportRecordHandler.createBalanceSheetCell("Expense", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
//                    cell3.setBorderWidthBottom(3);
                    cell3.setPaddingBottom(5);
//                    cell3.setBorderColor(Color.GRAY);
                    table1.addCell(blankCell);
                    table1.addCell(cell3);
                    table1.addCell(cell4);
                    table1.addCell(blankCell);
                    if (periodView) {
                        table1.addCell(blankCell);
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        if (isShowAccountCode) {
                            table1 = ExportRecordHandler.getBlankTable6Columns();
                        } else {
                            table1 = ExportRecordHandler.getBlankTable5Columns();
                        }
                    } else {
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        if (isShowAccountCode) {
                            if (isCompare) {
                                table1 = ExportRecordHandler.getBlankTable5Columns();
                            } else {
                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                            }
                        } else {
                            if (isCompare) {
                                table1 = ExportRecordHandler.getBlankTable4Columns();
                            } else {
                                table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                            }
                        }
                    }
                    for(i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                            JSONObject temp = leftObjArr.getJSONObject(i + 1);
                            JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                            if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                leftobj.put("bold", true);
                            else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                leftobj.put("bold", true);
                        }
                        if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("expense") && leftobj.has("accountname") && !leftobj.get("accountname").toString().equals("")) {
                            if(!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("expense"))) {
                                ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                            }
                        }
                    }
                if (!isForTradingAndProfitLoss) {
                    for(i = 0; i < leftObjArr.length(); i++) {

                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossprofit") ) {
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }

                        } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("grossloss") ) {
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                        }

                    }
                }

                    int count = 1;
                    for(i = 0; i < leftObjArr.length(); i++) {

                        JSONObject leftobj = leftObjArr.getJSONObject(i);  
                        if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                            JSONObject temp = leftObjArr.getJSONObject(i + 1);
                            JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                            if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                leftobj.put("bold", true);
                            else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                leftobj.put("bold", true);
                        }
                        if(leftobj.has("group") && leftobj.get("group").toString().equals("income")) {
                            if(count == 1) {
                                cell3 = ExportRecordHandler.createBalanceSheetCell("Other Income", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
//                                cell3.setBorderWidthBottom(3);
                                cell3.setPaddingBottom(5);
//                                cell3.setBorderColor(Color.GRAY);
                                table1.addCell(blankCell);
                                table1.addCell(cell3);
                                table1.addCell(cell4);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    table1 = ExportRecordHandler.getBlankTable5Columns();
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                                count++;
                            }
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                               if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                   if (isShowAccountCode) {
                                       table1 = ExportRecordHandler.getBlankTable6Columns();
                                   } else {
                                       table1 = ExportRecordHandler.getBlankTable5Columns();
                                   }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                   if (isShowAccountCode) {
                                       if (isCompare) {
                                           table1 = ExportRecordHandler.getBlankTable5Columns();
                                       } else {
                                           table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                       }
                                   } else {
                                       if (isCompare) {
                                           table1 = ExportRecordHandler.getBlankTable4Columns();
                                       } else {
                                           table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                       }
                                   }
                                }

                        }
                    }

                    count = 1;
                    for(i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if (i + 1 < leftObjArr.length() && i - 1 > 0) {
                            JSONObject temp = leftObjArr.getJSONObject(i + 1);
                            JSONObject temp1 = leftObjArr.getJSONObject(i - 1);
                            if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString()))
                                leftobj.put("bold", true);
                            else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString()))
                                leftobj.put("bold", true);
                        }
                        if(leftobj.has("group") && leftobj.get("group").toString().equals("expense")) {
                            if(count == 1) {
                                cell3 = ExportRecordHandler.createBalanceSheetCell("Other Expense", fontSmallBold1, Element.ALIGN_LEFT, 0, 0,0);
//                                cell3.setBorderWidthBottom(3);
                                cell3.setPaddingBottom(5);
//                                cell3.setBorderColor(Color.GRAY);
                                table1.addCell(blankCell);
                                table1.addCell(cell3);
                                table1.addCell(cell4);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                                count++;
                            }
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }

                        }
                    }

                    for(i = 0; i < leftObjArr.length(); i++) {
                        JSONObject leftobj = leftObjArr.getJSONObject(i);
                        if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netprofit") ) {
                            leftobj.remove("fmt");
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }

                        } else if (leftobj.has("acctype") && leftobj.get("acctype").toString().equals("netloss") ) {
                            leftobj.remove("fmt");
                            ExportRecordHandler.addBalanceSheetCell(request,leftobj,table1,currencyid,isAlignment);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                table1.addCell(blankCell);
                                if (periodView) {
                                    table1.addCell(blankCell);
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        table1 = ExportRecordHandler.getBlankTable6Columns();
                                    } else {
                                        table1 = ExportRecordHandler.getBlankTable5Columns();
                                    }
                                } else {
                                    ExportRecordHandler.addTableRow(mainTable, table1);
                                    if (isShowAccountCode) {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable5Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(5);
                                        }
                                    } else {
                                        if (isCompare) {
                                            table1 = ExportRecordHandler.getBlankTable4Columns();
                                        } else {
                                            table1 = ExportRecordHandler.getBlankTableColumnsNotCompare(4);
                                        }
                                    }
                                }
                        }

                    }

 

            }
//            table1.addCell(blankCell);
//
//            tableMain.addCell(blankCell);
//            PdfPCell mainCell11 = new PdfPCell(tableMain);
//            mainCell11.setBorder(0);
//            mainCell11.setPaddingLeft(10);
//            mainCell11.setPaddingRight(10);
//            mainTable.addCell(mainCell11);
//
//            mainCell11 = new PdfPCell(table1);
//            mainCell11.setBorder(0);
//            mainCell11.setPaddingLeft(10);
//            mainCell11.setPaddingRight(10);
//            mainTable.addCell(mainCell11);
            document.add(mainTable);
        } catch (Exception ex) {
            Logger.getLogger(ExportRecord.class.getName()).log(Level.WARNING, ex.getMessage());
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
        return baos;
    }

    public ByteArrayOutputStream exportLayoutPnLAndBalancesheetPDF(HttpServletRequest request, String currencyid, DateFormat formatter, String logoPath, String comName, JSONObject jobj, Date startDate, Date endDate, int flag, boolean isBalanceSheet, String address, Date endPreDate, ExtraCompanyPreferences extrapref) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        boolean isCompare = request.getParameter("isCompare") != null ? Boolean.parseBoolean(request.getParameter("isCompare")) : false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isOnlyPeriodBalances = request.getParameter("isOnlyPeriodBalances") != null ? Boolean.parseBoolean(request.getParameter("isOnlyPeriodBalances")) : false;
            boolean isOnlyEndingBalances = request.getParameter("isOnlyEndingBalances") != null ? Boolean.parseBoolean(request.getParameter("isOnlyEndingBalances")) : false;
            String customHeaderName = isOnlyPeriodBalances ? "Period Amount" : isOnlyEndingBalances ? "Ending Amount" : "";
            boolean isAlignment = Boolean.parseBoolean((String) request.getParameter("isAlignment"));
            String reportView=request.getParameter("reportView");
            String templateheadings = !StringUtil.isNullOrEmpty(request.getParameter("templateheadings"))?request.getParameter("templateheadings"):"";//For India Country Only
            boolean isCashFlowStatement=!StringUtil.isNullOrEmpty(reportView)?reportView.equals("CashFlowStatement"):false;
            boolean isTrialBalance = request.getParameter("isTrialBalance") != null ? Boolean.parseBoolean(request.getParameter("isTrialBalance")) : false;
            String headingString = "";
            String DateheadingString = "";
            String value = "";
            String prevalue = "";
            String subHeading1 = "";
            String subHeading2 = "";
            headingString = messageSource.getMessage("acc.rem.125", null, RequestContextUtils.getLocale(request)); // "P&L Statement For : ";
            DateheadingString = messageSource.getMessage("acc.rem.126", null, RequestContextUtils.getLocale(request)); // "P&L Statement From-To :";
            value = formatter.format(startDate) + " " + messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request)) + " " + formatter.format(endDate);
            subHeading1 = "Amount";//messageSource.getMessage("acc.P&L.Amount(Debit)", null, RequestContextUtils.getLocale(request));   //"Debit";
            subHeading2 = messageSource.getMessage("acc.P&L.Amount(Credit)", null, RequestContextUtils.getLocale(request));   //"Credit";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            PdfPCell LineCell = new PdfPCell();
            LineCell.setBorder(0);
            LineCell.setBorderWidthBottom(1);

            PdfPTable tab2 = new PdfPTable(1);
            PdfPTable tableMain = new PdfPTable(2);
            tableMain.setWidthPercentage(100);
            if (isCompare ||isTrialBalance) {
                tableMain.setWidths(new float[]{5, 85});
            } else {
                tableMain.setWidths(new float[]{10, 80});
            }
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) result.getEntityList().get(0);
            int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            if (ExportRecordHandler.checkCompanyTemplateLogoPresent(company)) {
                tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, company);
            } else {
                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
                String companyArray[] = new String[4];
                companyArray[0] = company.getCompanyName();
                companyArray[1] = company.getAddress();
                companyArray[2] = company.getEmailID();
                companyArray[3] = company.getPhoneNumber();
                tab2 = ExportRecordHandler.getCompanyInfo(companyArray);
            }

            PdfPCell cell1 = new PdfPCell(tab1);
            PdfPCell cellcompanyInfo = new PdfPCell(tab2);
            cell1.setBorder(0);
            cellcompanyInfo.setBorder(0);

            tableMain.addCell(blankCell);
            tableMain.addCell(cell1);

            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);

            tableMain.addCell(blankCell);
            tableMain.addCell(cellcompanyInfo);
            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);

            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);

            tableMain.addCell(blankCell);
            tableMain.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, tableMain);

            tableMain = new PdfPTable(3);
            tableMain.setWidthPercentage(100);
            if (isCompare || isTrialBalance) {
                tableMain.setWidths(new float[]{5, 90, 5});
            } else {
                tableMain.setWidths(new float[]{10, 80, 10});
            }
            PdfPCell headerCell = null;
            if (isBalanceSheet) {
                headerCell = createCell("Statement of financial position as at " + formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            } else if(isCashFlowStatement){
                headerCell = createCell("Cash flow statement- " + formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            } else if(isTrialBalance){
                  headerCell = createCell("Trial Balance for the period " + value + " (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            } else{
                headerCell = createCell("Statement of profit or loss and other comprehensive income for the year ended " + formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            }
            if(!StringUtil.isNullOrEmpty(templateheadings) && countryid == Constants.indian_country_id){
                headerCell = createCell( templateheadings +" as date : "+ formatter.format(endDate) + "  (Amount in " + exportDaoObj.currencyRender("", currencyid, companyid) + ")", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
                headerCell.setPaddingLeft(8);
            }

            headerCell.setBorder(0);
            headerCell.setPaddingBottom(5);
            tableMain.addCell(blankCell);
            tableMain.addCell(headerCell);
            PdfPCell headerNameCell = createCell("", FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0);
            headerNameCell.setBorder(0);
            headerNameCell.setPaddingBottom(5);
            tableMain.addCell(headerNameCell);
            //tableMain.addCell(blankCell);

            for (int i = 0; i < 18; i++) {
                tableMain.addCell(blankCell);
            }

            PdfPTable table1 = new PdfPTable(5);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{10, 40, 20, 20, 10});

            PdfPCell cell2 = null;
            PdfPCell cell3 = ExportRecordHandler.createBalanceSheetCell("Income", fontSmallBold1, Element.ALIGN_LEFT, 0, 0, 0);//  
            PdfPCell cell4 = ExportRecordHandler.createBalanceSheetCell("", FontContext.TABLE_HEADER, Element.ALIGN_RIGHT, 0, 0, 0);
//            cell3.setBorderWidthBottom(3);
//            cell3.setBorderColor(Color.GRAY);
            cell3.setPaddingBottom(5);
//            cell4.setBorderWidthBottom(3);
//            cell4.setBorderColor(Color.gray);

            jobj = jobj.getJSONObject("data");
            JSONArray rightObjArr = flag == 1 ? jobj.getJSONArray("right") : jobj.getJSONArray("left");
//            JSONArray leftObjArr = flag==1?jobj.getJSONArray("left"):jobj.getJSONArray("right");
            PdfPCell HeaderCell1 = createCell("", fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            HeaderCell1.setPaddingBottom(5);
            //HeaderCell1.setBorderWidthBottom(1);            
            PdfPCell HeaderCell2 = createCell(formatter.format(endDate), fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
            HeaderCell2.setPaddingBottom(5);
//            PdfPCell HeaderPreCell2 = createCell("", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
//            if(endPreDate!=null)
//                HeaderPreCell2 = createCell(formatter.format(endPreDate), fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
//            HeaderPreCell2.setPaddingBottom(5);
            //HeaderCell2.setBorderWidthBottom(1);
//            table1 = ExportRecordHandler.getBlankTable4Columns();
            if (!isOnlyPeriodBalances) {
                table1.addCell(blankCell);
                table1.addCell(HeaderCell1);
//                table1.addCell(HeaderCell2);
            }
//            table1.addCell(HeaderPreCell2);     
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            ExportRecordHandler.addTableRow(mainTable, tableMain);
            ExportRecordHandler.addTableRow(mainTable, table1);

            PdfPCell perticularHeaderCell1 = createCell(messageSource.getMessage("acc.P&L.particulars", null, RequestContextUtils.getLocale(request)), fontSmallBold1, Element.ALIGN_LEFT, 0, 0);
            perticularHeaderCell1.setPaddingBottom(5);
            
            PdfPCell accountcodeHeaderCell1 = createCell("Account Code", fontSmallBold1, Element.ALIGN_CENTER, 0, 0);
            accountcodeHeaderCell1.setPaddingBottom(5);

            if (isOnlyPeriodBalances || isOnlyEndingBalances) {
                PdfPCell customHeaderCell1 = createCell(customHeaderName, fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                customHeaderCell1.setPaddingBottom(5);
                table1 = ExportRecordHandler.getBlankTable4Columns_Custom(extrapref);
                table1.addCell(blankCell);
                table1.addCell(perticularHeaderCell1);
                if (extrapref != null && extrapref.isShowAccountCodeInFinancialReport()) {
                    table1.addCell(accountcodeHeaderCell1);
                }
                table1.addCell(customHeaderCell1);
                table1.addCell(blankCell);
            } else {
                PdfPCell openingHeaderCell1 = createCell("Opening Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                openingHeaderCell1.setPaddingBottom(5);

                PdfPCell periodHeaderCell1 = createCell("Period Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                periodHeaderCell1.setPaddingBottom(5);

                PdfPCell endingHeaderCell1 = createCell("Ending Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                endingHeaderCell1.setPaddingBottom(5);
                if (isTrialBalance) {
                     openingHeaderCell1 = createCell("Opening Amount (Credit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                     periodHeaderCell1 = createCell("Period Amount (Credit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                     endingHeaderCell1 = createCell("Ending Amount (Credit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                }
                if (isCompare || isTrialBalance) {
                    table1 = ExportRecordHandler.getBlankTable9Columns_Custom(extrapref);
                } else {
                    table1 = ExportRecordHandler.getBlankTable6Columns_Custom(extrapref);
                }
                table1.addCell(blankCell);
                table1.addCell(perticularHeaderCell1);
                if (extrapref != null && extrapref.isShowAccountCodeInFinancialReport()) {
                    table1.addCell(accountcodeHeaderCell1);
                }
                table1.addCell(openingHeaderCell1);
                if (isTrialBalance) {
                    PdfPCell debitOpeningHeader = createCell("Opening Amount (Debit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    openingHeaderCell1.setPaddingBottom(5);
                    table1.addCell(debitOpeningHeader);
                }
                table1.addCell(periodHeaderCell1);
                  if (isTrialBalance) {
                    PdfPCell debitOpeningHeader = createCell("Period Amount (Debit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    openingHeaderCell1.setPaddingBottom(5);
                    table1.addCell(debitOpeningHeader);
                }
                table1.addCell(endingHeaderCell1);
                if (isTrialBalance) {
                    PdfPCell debitOpeningHeader = createCell("Ending Amount (Debit)", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    openingHeaderCell1.setPaddingBottom(5);
                    table1.addCell(debitOpeningHeader);
                }
                if (isCompare) {
                    PdfPCell preopeningHeaderCell1 = createCell("PreOpening Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    openingHeaderCell1.setPaddingBottom(5);

                    PdfPCell preperiodHeaderCell1 = createCell("PrePeriod Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    periodHeaderCell1.setPaddingBottom(5);

                    PdfPCell preendingHeaderCell1 = createCell("PreEnding Amount", fontSmallBold1, Element.ALIGN_RIGHT, 0, 0);
                    endingHeaderCell1.setPaddingBottom(5);
                    
                    table1.addCell(preopeningHeaderCell1);
                    table1.addCell(preperiodHeaderCell1);
                    table1.addCell(preendingHeaderCell1);
                }
                table1.addCell(blankCell);
            }

//            addTableRow(mainTable, tableMain);
            ExportRecordHandler.addTableRow(mainTable, table1);

            if (isOnlyPeriodBalances || isOnlyEndingBalances) {
                table1 = ExportRecordHandler.getBlankTable4Columns_Custom(extrapref);
            } else {
                if (isCompare || isTrialBalance) {
                    table1 = ExportRecordHandler.getBlankTable9Columns_Custom(extrapref);
                } else {
                    table1 = ExportRecordHandler.getBlankTable6Columns_Custom(extrapref);
                }
            }
            if (flag == 2) {
                int i = 0, j = 1, accountNumbers = 0;
                boolean existflag = false;

                for (i = 0; i < rightObjArr.length(); i++) {
                    JSONObject leftobj = rightObjArr.getJSONObject(i);
                    if (i + 1 < rightObjArr.length() && i - 1 > 0) {
                        JSONObject temp = rightObjArr.getJSONObject(i + 1);
                        JSONObject temp1 = rightObjArr.getJSONObject(i - 1);
                        if (leftobj.has("level") && temp.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp.get("level").toString())) {
                            leftobj.put("bold", true);
                        } else if (leftobj.has("level") && temp1.has("level") && Integer.parseInt(leftobj.get("level").toString()) < Integer.parseInt(temp1.get("level").toString())) {
                            leftobj.put("bold", true);
                        }
                    }
//                        if(leftobj.has("acctype") && leftobj.get("acctype").toString().equals("income")) {
//                            if(!leftobj.has("group") || (leftobj.has("group") && !leftobj.get("group").toString().equals("income"))) {
                    if (isOnlyPeriodBalances || isOnlyEndingBalances) {
                        if (isOnlyEndingBalances) {
                            leftobj.put("isOnlyEndingBalances", true);   // 'isOnlyEndingBalances' flag is required to put either period amount on ending amount in PDF
                        }
                        ExportRecordHandler.addBalanceSheetCell_CustomOnlyPeriodBalances(leftobj, table1, currencyid, isAlignment, extrapref);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        table1 = ExportRecordHandler.getBlankTable4Columns_Custom(extrapref);
                    } else {
                        if (isTrialBalance) {
                            addTrialBalanceCell_Custom(leftobj, table1, extrapref);
                        } else {
                            ExportRecordHandler.addBalanceSheetCell_Custom(leftobj, table1, currencyid, isAlignment, isCompare, extrapref);
                        }
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        table1.addCell(blankCell);
                        if (isCompare || isTrialBalance) {
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                            table1.addCell(blankCell);
                        }
                        ExportRecordHandler.addTableRow(mainTable, table1);
                        if (isCompare || isTrialBalance) {
                            table1 = ExportRecordHandler.getBlankTable9Columns_Custom(extrapref);
                        } else {
                            table1 = ExportRecordHandler.getBlankTable6Columns_Custom(extrapref);
                        }
                    }
//                            }
//                        }
                }
            }
            document.add(mainTable);
        } catch (Exception ex) {
            System.out.println(ex.toString());
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
        return baos;
    }

    public ByteArrayOutputStream generateGSTForm5ReportPdf(HttpServletRequest request, String currencyid, String logoPath, String comName, String address, String startDate, String endDate, JSONArray salesjArr, JSONArray purchasejArr, Company company) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
    	SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        try {
            String companyid = company.getCompanyID();
            String poRefno ="";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 75, 75, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);
    
            PdfPTable blankTable = null;
            PdfPCell blankCell = new PdfPCell();
            PdfPCell blankCellTop = new PdfPCell();
            blankCellTop.setBorder(Rectangle.TOP);
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(1);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{100});

            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());

            PdfPCell reportTitle =  createCell("GST F5", fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle =  createCell("GOODS AND SERVICES TAX RETURN", fontMediumBold, Element.ALIGN_LEFT, 0, 0);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle =  createCell("Goods and Services Tax Act (Cap 117A)", fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);

            PdfPCell leftSub =  new PdfPCell(new Paragraph("Name : "+company.getCompanyName(), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Tax Reference No. : ", fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("GST Reference No. : "+(pref.getGstNumber()!=null?pref.getGstNumber():""), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Due Date : ", fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Period covered by this Return : " + startDate + "  To  " + endDate, fontSmallRegular));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);


            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            String GSTSR = "0", GSTZR = "0";
            double GST3 = 0, amtdue = 0, totalSale = 0;
            for(int i = 0; i < salesjArr.length(); i++) {
                if(salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(SR)@7.00%"))
                    GSTSR = salesjArr.getJSONObject(i).getString("totalsale").toString();
                if(salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(ZR)@0.00%"))
                    GSTZR = salesjArr.getJSONObject(i).getString("totalsale").toString();
                if(salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(EP)@0.00%") || salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(TX_E33)@7.00%")  || salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(TX_N33)@7.00%")
                         || salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(TX_RE)@7.00%") || salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(ES33)@0.00%") || salesjArr.getJSONObject(i).getString("taxname").toString().equals("GST(ESN33)@0.00%"))
                    GST3 = GST3 + Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale").toString());

                amtdue = amtdue + Double.parseDouble(salesjArr.getJSONObject(i).getString("taxcollected").toString());
                totalSale = totalSale + Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale").toString());
}

            double GSTpurchase = 0, amtduepurchase = 0 , netGst = 0, gstIMME = 0;
            for(int i = 0; i < purchasejArr.length(); i++) {
                if(purchasejArr.getJSONObject(i).getString("taxname").toString().equals("GST(IM)@7.00%") || purchasejArr.getJSONObject(i).getString("taxname").toString().equals("GST(ME)@0.00%"))
                    gstIMME = gstIMME + Double.parseDouble(purchasejArr.getJSONObject(i).getString("totalsale").toString());

                GSTpurchase = GSTpurchase + Double.parseDouble(salesjArr.getJSONObject(i).getString("totalsale").toString());
                amtduepurchase = amtduepurchase + Double.parseDouble(purchasejArr.getJSONObject(i).getString("taxcollected").toString());
            }

            netGst = amtdue - amtduepurchase;

            leftSub =  new PdfPCell(new Paragraph("Supplies", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            PdfPCell tableCell = new PdfPCell(new Paragraph("Total value of standard rated supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(Double.parseDouble(GSTSR) > 0 ? authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GSTSR), currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of zero rated supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(Double.parseDouble(GSTZR) > 0 ? authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GSTZR), currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of exempt supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(GST3 > 0 ? authHandlerDAOObj.getFormattedCurrency(GST3, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            GST3 = (Double) Double.parseDouble(GSTSR) + (Double) Double.parseDouble(GSTZR) + GST3;
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of (1)+(2)+(3)", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(GST3 > 0 ? authHandlerDAOObj.getFormattedCurrency(GST3, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Purchases", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of taxable purchases", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(GSTpurchase > 0 ? authHandlerDAOObj.getFormattedCurrency(GSTpurchase, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Taxes", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Output Tax Due", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(amtdue > 0 ? authHandlerDAOObj.getFormattedCurrency(amtdue, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Less : Input Tax and Refunds Claim", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(amtduepurchase > 0 ? authHandlerDAOObj.getFormattedCurrency(amtduepurchase, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Equals : Net GST to be paid to IRAS", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(netGst > 0 ? authHandlerDAOObj.getFormattedCurrency(netGst, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Applicable to Taxable persons under Major Exporter Scheme / Third party approved Logistic Company / Other Approved Schemes only", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of Goods imported under this Scheme", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(gstIMME > 0 ? authHandlerDAOObj.getFormattedCurrency(gstIMME, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Did you make the following claims in Box 7?", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you claim the GST you had refunded to tourists?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("0", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you make any bad debt relief claims?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you make any pre-registration claims?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Revenue", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Revenue for the Accounting period", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph(totalSale > 0 ? authHandlerDAOObj.getFormattedCurrency(totalSale, currencyid, companyid): "", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Declaration", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("I declare that the Information given above is true and complete", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Name of Declarant / Declarant ID : "+ user.getFirstName()+" "+user.getLastName(), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Designation : " + ((user.getDesignation()!=null)?user.getDesignation():""), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Contact Person :", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Contact Tel No. : " + ((user.getContactNumber()!=null)?user.getContactNumber():""), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            tableCell = new PdfPCell(new Paragraph("", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);



            


            PdfPCell mainCell = new PdfPCell(table1);
            mainCell.setBorder(0);
            mainCell.setPadding(10);
            mainTable.addCell(mainCell);
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("generateGSTForm5ReportPdf: "+ex.getMessage(), ex);
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
    
    public ByteArrayOutputStream generateGSTForm5ReportPdf(HttpServletRequest request, 
    		String currencyid, String logoPath, String comName, String address, String startDate, String endDate, JSONArray jArr, Company company) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
    	SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        String name="";
        String id="";
        String desg="";
        String per="";
        String contact="";
        List valueTextField=new ArrayList();
        try {
            String poRefno ="";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            baos = new ByteArrayOutputStream();
            if (!StringUtil.isNullOrEmpty(request.getParameter("nameDe"))) {
                name = request.getParameter("nameDe");
                valueTextField.add(name);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                id = request.getParameter("id");
                valueTextField.add(id);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("desg"))) {
                desg = request.getParameter("desg");
                valueTextField.add(desg);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("per"))) {
                per = request.getParameter("per");
                valueTextField.add(per);
            }else{
                 valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("contact"))) {
                contact = request.getParameter("contact");
                valueTextField.add(contact);
            } else {
                valueTextField.add("");
            }
            document = new Document(PageSize.A4, 75, 75, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = new PdfPCell();
            PdfPCell blankCellTop = new PdfPCell();
            blankCellTop.setBorder(Rectangle.TOP);
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(1);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{100});

            blankCell.setBorder(0);
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());

            PdfPCell reportTitle =  createCell("GST F5", fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle =  createCell("GOODS AND SERVICES TAX RETURN", fontMediumBold, Element.ALIGN_LEFT, 0, 0);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle =  createCell("Goods and Services Tax Act (Cap 117A)", fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);

            PdfPCell leftSub =  new PdfPCell(new Paragraph("Name : "+comName, fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Tax Reference No. : "+(pref.getTaxNumber()!=null?pref.getTaxNumber():""), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("GST Reference No. : "+(pref.getGstNumber()!=null?pref.getGstNumber():""), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Due Date : ", fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub =  new PdfPCell(new Paragraph("Period covered by this Return : " + startDate + "  To  " + endDate, fontSmallRegular));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);


            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            String GSTSR = "0.00", GSTZR = "0.00", GST3 = "0.00", totalFirstThree = "0.00";
            String GSTpurchase = "0.00", taxDue = "0.00" , inputTaxRefundsClaimed = "0.00", netGST = "0.00";            
            String totalValueOfGoodsImported = "0.00";
            String totalRevenue = "0.00";
            
            for(int i=0; i<jArr.length(); i++){
            	JSONObject jObj = jArr.getJSONObject(i);
            	if (jObj.getString("taxname").equalsIgnoreCase("Total value of standard rated supplies")){
            		GSTSR = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Total value of zero rated supplies")){
            		GSTZR = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Total value of exempt supplies")){
            		GST3 = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Total value of [1]+[2]+[3]")){
            		totalFirstThree = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Total value of taxable purchase")){
            		GSTpurchase = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Output tax due")){
            		taxDue = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Less : input tax and refunds claimed")){
            		inputTaxRefundsClaimed = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Equal : Net GST to be paid to IRAS")){
            		netGST = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Total value of goods imported under this scheme")){
            		totalValueOfGoodsImported = jObj.getString("taxamount");
            	}else if (jObj.getString("taxname").equalsIgnoreCase("Revenue for the accounting period")){
            		totalRevenue = jObj.getString("taxamount");
            	}
            }
            
            PdfPTable tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5();
            
            PdfPCell tableHeaderCell = new PdfPCell(new Paragraph("Supplies", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("SG Dollar(SGD)", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("Box", fontMediumBold1));            
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);
            tableHeaderContent.addCell(tableHeaderCell);
            
            ExportRecordHandler.addTableRow(table1, tableHeaderContent);                                
            

            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            
            PdfPCell tableCell = new PdfPCell(new Paragraph("Total value of standard rated supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);            
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GSTSR), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[1]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of zero rated supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GSTZR), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[2]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of exempt supplies", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GSTZR), currencyid, companyid), fontSmallRegular));
            }else{
                tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency((Double) Double.parseDouble(GST3), currencyid, companyid), fontSmallRegular));
            }
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[3]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            //ExportRecordHandler.addTableRow(table1, tableContent);

            GST3 = (Double) Double.parseDouble(GSTSR) + (Double) Double.parseDouble(GSTZR) + GST3;
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of [1]+[2]+[3]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
                        
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(totalFirstThree), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[4]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            ExportRecordHandler.addTableRow(table1, tableContent);            

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5();
            
            tableHeaderCell = new PdfPCell(new Paragraph("Purchases", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);            
            
            ExportRecordHandler.addTableRow(table1, tableHeaderContent); 

            
            

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of taxable purchases", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(GSTpurchase), currencyid, companyid), fontSmallRegular));            
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[5]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);             
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5();
            
            tableHeaderCell = new PdfPCell(new Paragraph("Taxes", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);
            
            tableHeaderCell = new PdfPCell(new Paragraph("", fontMediumBold1));
            tableHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableHeaderCell.setBorder(0);
            tableHeaderCell.setBorder(Rectangle.BOTTOM);            
            tableHeaderContent.addCell(tableHeaderCell);            
            
            ExportRecordHandler.addTableRow(table1, tableHeaderContent); 

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Output Tax Due", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(taxDue), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[6]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);  
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Less : Input Tax and Refunds Claim", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(inputTaxRefundsClaimed), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[7]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);              
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Equals : Net GST to be paid to IRAS", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(netGST), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[8]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);  
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5();
            
            leftSub =  new PdfPCell(new Paragraph("Applicable to Taxable persons under Major Exporter Scheme / Third party approved Logistic Company / Other Approved Schemes only", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);                        

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Total value of Goods imported under this Scheme", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(totalValueOfGoodsImported), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[9]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);  
                        
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Did you make the following claims in Box 7?", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you claim the GST you had refunded to tourists?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            
            tableCell = new PdfPCell(new Paragraph("[10]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);  
            
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you make any bad debt relief claims?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableCell = new PdfPCell(new Paragraph("[11]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Did you make any pre-registration claims?", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("      Yes                No", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableCell = new PdfPCell(new Paragraph("[12]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Revenue", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Revenue for the Accounting period", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(Double.parseDouble(totalRevenue), currencyid, companyid), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);    
            tableContent.addCell(tableCell);
            
            tableCell = new PdfPCell(new Paragraph("[13]", fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableCell.setBorder(0);         
            tableContent.addCell(tableCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            leftSub =  new PdfPCell(new Paragraph("Declaration", fontMediumBold1));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

//            tableContent = getBlankTableGSTForm5();
//            tableCell = new PdfPCell(new Paragraph("I declare that the Information given above is true and complete", fontSmallRegular));
//            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
//            tableCell.setBorder(0);
//            tableContent.addCell(tableCell);
//            
//            tableContent.addCell(blankCell);
//            tableContent.addCell(blankCell);
//            
//            addTableRow(table1, tableContent);
//
//            table1.addCell(blankCell);
//            table1.addCell(blankCell);
//            table1.addCell(blankCell);
            
            leftSub =  new PdfPCell(new Paragraph("I declare that the Information given above is true and complete", fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);            

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Name of Declarant :   "+valueTextField.get(0), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);
            
            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Declarant Id :              "+valueTextField.get(1), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);            

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Designation :              "+valueTextField.get(2), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Contact Person :        "+valueTextField.get(3), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);

            tableContent = ExportRecordHandler.getBlankTableGSTForm5();
            tableCell = new PdfPCell(new Paragraph("Contact Tel No. :        "+valueTextField.get(4), fontSmallRegular));
            tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tableCell.setBorder(0);
            tableContent.addCell(tableCell);
            
            tableContent.addCell(blankCell);
            tableContent.addCell(blankCell);
            
            ExportRecordHandler.addTableRow(table1, tableContent);       

            PdfPCell mainCell = new PdfPCell(table1);
            mainCell.setBorder(0);
            mainCell.setPadding(10);
            mainTable.addCell(mainCell);
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("generateGSTForm5ReportPdf: "+ex.getMessage(), ex);
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
    
    public ByteArrayOutputStream generateGSTForm5DetailedReportPdf(HttpServletRequest request,
            String currencyid, String logoPath, String comName, String address, String startDate, String endDate, JSONArray jArr, Company company) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        Document document = null;
        PdfWriter writer = null;
        String nameDe = "";
        String id = "";
        String desg = "";
        String per = "";
        String contact = "";
        List valueTextField = new ArrayList();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            baos = new ByteArrayOutputStream();
            if (!StringUtil.isNullOrEmpty(request.getParameter("nameDe"))) {
                nameDe = request.getParameter("nameDe");
                valueTextField.add(nameDe);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                id = request.getParameter("id");
                valueTextField.add(id);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("desg"))) {
                desg = request.getParameter("desg");
                valueTextField.add(desg);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("per"))) {
                per = request.getParameter("per");
                valueTextField.add(per);
            } else {
                valueTextField.add("");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("contact"))) {
                contact = request.getParameter("contact");
                valueTextField.add(contact);
            } else {
                valueTextField.add("");
            }
            document = new Document(PageSize.A4);
//            document = new Document(PageSize.LETTER.rotate());
//            document = new Document(PageSize.A4, 75, 75, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPCell blankCellTop = new PdfPCell();
            blankCellTop.setBorder(Rectangle.TOP);
            PdfPTable tab1 = null;
            PdfPCell pdfCell = null;
            String emptyString = "";
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(1);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{100});
            tab1 = ExportRecordHandler.addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            ExportRecordHandler.addBlankCell(table1, 6);

            User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
            Account forexAccount = pref.getForeignexchange();
            String accountName = forexAccount!=null?forexAccount.getAccountName():"";
            PdfPCell reportTitle = createCell("GST Form-5 Details", fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle = createCell("GOODS AND SERVICES TAX RETURN", fontMediumBold, Element.ALIGN_LEFT, 0, 0);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);
            reportTitle = createCell("Goods and Services Tax Act (Cap 117A)", fontMediumRegular, Element.ALIGN_LEFT, 0, 5);
            reportTitle.setBorder(0);
            reportTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            table1.addCell(reportTitle);

            PdfPCell leftSub = new PdfPCell(new Paragraph("Name : " + comName, fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub = new PdfPCell(new Paragraph("Tax Reference No. : " + (pref.getTaxNumber() != null ? pref.getTaxNumber() : ""), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub = new PdfPCell(new Paragraph("GST Reference No. : " + (pref.getGstNumber() != null ? pref.getGstNumber() : ""), fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub = new PdfPCell(new Paragraph("Due Date : ", fontSmallRegular));
            leftSub.setBorder(0);
            table1.addCell(leftSub);

            leftSub = new PdfPCell(new Paragraph("Period covered by this Return : " + startDate + "  To  " + endDate, fontSmallRegular));
            leftSub.setBorder(0);
            leftSub.setBorder(Rectangle.BOTTOM);
            table1.addCell(leftSub);
            ExportRecordHandler.addBlankCell(table1, 6);

            PdfPTable tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
            pdfCell = ExportRecordHandler.addTitleCell("Particulars", fontSmallBold1);
            tableHeaderContent.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTitleCell("Txn.ID",fontSmallBold1);
            tableHeaderContent.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTitleCell("JE",fontSmallBold1);
            tableHeaderContent.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTitleCell("Name",fontSmallBold1);
            tableHeaderContent.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTitleCell("S $", fontSmallBold1);
            pdfCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableHeaderContent.addCell(pdfCell);
            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                pdfCell = ExportRecordHandler.addTitleCell("Exchange Rate", fontSmallBold1);
                pdfCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableHeaderContent.addCell(pdfCell);
                pdfCell = ExportRecordHandler.addTitleCell("Amount in Transaction Currency", fontSmallBold1);
                pdfCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tableHeaderContent.addCell(pdfCell);
            }
            pdfCell = ExportRecordHandler.addTitleCell("Box", fontSmallBold1);
            tableHeaderContent.addCell(pdfCell);            
            ExportRecordHandler.addTableRow(table1, tableHeaderContent);                    

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jObj = jArr.getJSONObject(i);

                if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Supplies")) {  //Supplies
                    if (jObj.getString("taxname").equalsIgnoreCase("Supplies")) {
                        tableHeaderContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTitleCell("Supplies", fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableHeaderContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
//                             addExtraColumnsForSingaporeCountry(jObj,tableHeaderContent);  
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableHeaderContent.addCell(pdfCell);
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableHeaderContent.addCell(pdfCell);
                        }                       
                        ExportRecordHandler.addTableRow(table1, tableHeaderContent);
                        continue;
                    } else {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                        pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                             addExtraColumnsForSingaporeCountry(jObj,tableContent);                            
                        }
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("box"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);  
                        continue;

                    }
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Total value of standard rated supplies")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);                    
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);                            
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontSmallRegular);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Total value of zero rated supplies")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);                    
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Total value of exempt supplies")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallBold1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallBold1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallBold1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase(accountName + " (Absolute value)")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Purchases")) {
                    if (jObj.getString("taxname").equalsIgnoreCase("Purchases")) {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);  
                        pdfCell = ExportRecordHandler.addTitleCell("Purchases", fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);                        
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
//                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                        }
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    } else {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                        pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                        }
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);  
                        continue;
                    }
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Total value of taxable purchase")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);  
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Taxes")) {
                    if (jObj.getString("taxname").equalsIgnoreCase("Taxes")) {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTitleCell("Taxes", fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
//                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                            pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                        }
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    } else {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                        pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                        }
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("box"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    }
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Output Tax Due")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);  
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Less : input tax and refunds claimed")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);   
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Equal : Net GST to be paid to IRAS")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent); 
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Applicable to Taxable Persons under Major Exporter Scheme / Approved 3rd Party Logistics Company / Other Approved Schemes Only")) {
                    if (jObj.getString("taxname").equalsIgnoreCase("Applicable to Taxable Persons under Major Exporter Scheme / Approved 3rd Party Logistics Company / Other Approved Schemes Only")) {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company); 
                        pdfCell = ExportRecordHandler.addTitleCell("Applicable to Taxable Persons under Major Exporter Scheme / Approved 3rd Party Logistics Company / Other Approved Schemes Only", fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                        }
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    } else {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                        pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                        }
                        pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("box"), fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    }
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Total value of goods imported under this scheme")) {
                    PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("transactionid"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("journalentry"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("name"), fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallRegular1, company.getCompanyID());
                    pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    tableContent.addCell(pdfCell);
                    if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                        addExtraColumnsForSingaporeCountry(jObj,tableContent);   
                    }
                    pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallRegular1);
                    tableContent.addCell(pdfCell);
                    ExportRecordHandler.addTableRow(table1, tableContent);
                    continue;
                } else if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Did you make the following claims in Box 7?")) {
                    if (jObj.getString("taxname").equalsIgnoreCase("Did you make the following claims in Box 7?")) {
                        PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);  
                        pdfCell = ExportRecordHandler.addTitleCell("Did you make the following claims in Box 7?", fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        if(company.getCountry().getID().equals(Constants.SINGAPOREID)){
                            addExtraColumnsForSingaporeCountry(jObj,tableContent);
                        }
                        pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                        tableContent.addCell(pdfCell);
                        ExportRecordHandler.addTableRow(table1, tableContent);
                        continue;
                    } else {
                        if (jObj.getString("taxname").equalsIgnoreCase("Did you claim for GST you had refunded to tourists?")) {
                            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("Did you claim for GST you had refunded to tourists?", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 4);  //Add Blank Cell  
                            pdfCell = ExportRecordHandler.addTaxnameCell("[10]", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addTableRow(table1, tableContent);
                            tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("      Yes                No", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 5);  //Add Blank Cell
                            ExportRecordHandler.addTableRow(table1, tableContent);  
                            continue;
                        } else if (jObj.getString("taxname").equalsIgnoreCase("Did you make any bad debt relief claims?")) {
                            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("Did you make any bad debt relief claims?", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 4);  //Add Blank Cell
                            pdfCell = ExportRecordHandler.addTaxnameCell("[11]", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addTableRow(table1, tableContent);
                            tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("      Yes                No", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 5);  //Add Blank Cell
                            ExportRecordHandler.addTableRow(table1, tableContent); 
                            continue;
                        } else if (jObj.getString("taxname").equalsIgnoreCase("Did you make any pre-registration claims?")) {
                            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("Did you make any pre-registration claims?", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 4);  //Add Blank Cell
                            pdfCell = ExportRecordHandler.addTaxnameCell("[12]", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addTableRow(table1, tableContent);
                            tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell("      Yes                No", fontXSmallRegular1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addBlankCell(tableContent, 5);  //Add Blank Cell
                            ExportRecordHandler.addTableRow(table1, tableContent); 
                            continue;
                        }

                    }
                } else {
                    if (jObj.getString("mergedCategoryData").equalsIgnoreCase("Revenue")) {
                        if (jObj.getString("taxname").equalsIgnoreCase("Revenue")) {
                            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company); 
                            pdfCell = ExportRecordHandler.addTitleCell("Revenue", fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
//                                addExtraColumnsForSingaporeCountry(jObj, tableContent);
                                pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                                pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                                tableContent.addCell(pdfCell);
                                pdfCell = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
                                pdfCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                                tableContent.addCell(pdfCell);
                            }
                            pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
                            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addTableRow(table1, tableContent);
                            continue;
                        } else {
                            PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
                            pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("taxname"), fontXSmallBold1);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addTaxnameCell(emptyString, fontXSmallBold1);
                            tableContent.addCell(pdfCell);
                            pdfCell = ExportRecordHandler.addCurrencyCell(jObj.getString("taxamount"), currencyid, authHandlerDAOObj, fontXSmallBold1, company.getCompanyID());
                            pdfCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                            tableContent.addCell(pdfCell);
                            if (company.getCountry().getID().equals(Constants.SINGAPOREID)) {
                                addExtraColumnsForSingaporeCountry(jObj, tableContent);
                            }
                            pdfCell = ExportRecordHandler.addTaxnameCell(jObj.getString("box"), fontXSmallBold1);
                            tableContent.addCell(pdfCell);
                            ExportRecordHandler.addTableRow(table1, tableContent);
//                            ExportRecordHandler.addTableRow(table1, ExportRecordHandler.addBlankCell(tableContent, 6));  
                            continue;
                        }
                    }
                }
            }//for  
//                PdfPTable tableContent = ExportRecordHandler.getBlankTableGSTForm5Details(company);
//                ExportRecordHandler.addTableRow(table1, ExportRecordHandler.addBlankCell(tableContent, 6));
//                pdfCell = ExportRecordHandler.addTitleCell("Declaration", fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                pdfCell = ExportRecordHandler.addTitleCell(emptyString, fontSmallBold1);
//                tableContent.addCell(pdfCell);
//                ExportRecordHandler.addTableRow(table1, tableContent);
            PdfPTable tableContent = new PdfPTable(1);
            tableContent.setWidthPercentage(100);
            pdfCell = ExportRecordHandler.addTaxnameCell("Declaration", fontXSmallBold1);
            pdfCell.setBorderWidth(0.5f);
            pdfCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            tableContent.addCell(pdfCell);
            ExportRecordHandler.addTableRow(table1, tableContent);
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            pdfCell = ExportRecordHandler.addTaxnameCell("I declare that the Information given above is true and complete", fontXSmallRegular1);
            table.addCell(pdfCell);
            ExportRecordHandler.addTableRow(table1, table);
            PdfPTable tableContent2 = ExportRecordHandler.getBlankTableGSTForm5();
            pdfCell = ExportRecordHandler.addTaxnameCell("Name of Declarant :  "+valueTextField.get(0), fontXSmallRegular1);
            tableContent2.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTaxnameCell(" " , fontXSmallRegular1);
            tableContent2.addCell(pdfCell);
            ExportRecordHandler.addBlankCell(tableContent2, 1);  //Add Blank Cell
            ExportRecordHandler.addTableRow(table1, tableContent2);
            PdfPTable tableContent3 = ExportRecordHandler.getBlankTableGSTForm5();
            pdfCell = ExportRecordHandler.addTaxnameCell("Declarant Id :             "+valueTextField.get(1), fontXSmallRegular1);
            tableContent3.addCell(pdfCell);
            ExportRecordHandler.addBlankCell(tableContent3, 2);  //Add Blank Cell
            ExportRecordHandler.addTableRow(table1, tableContent3);
            PdfPTable tableContent4 = ExportRecordHandler.getBlankTableGSTForm5();
            pdfCell = ExportRecordHandler.addTaxnameCell("Designation :             " +valueTextField.get(2), fontXSmallRegular1);
            tableContent4.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTaxnameCell(((user.getDesignation() != null) ? user.getDesignation() : ""), fontXSmallRegular1);;
            tableContent4.addCell(pdfCell);
            ExportRecordHandler.addBlankCell(tableContent4, 1);  //Add Blank Cell
            ExportRecordHandler.addTableRow(table1, tableContent4);
            PdfPTable tableContent5 = ExportRecordHandler.getBlankTableGSTForm5();
            pdfCell = ExportRecordHandler.addTaxnameCell("Contact Person :       "+valueTextField.get(3), fontXSmallRegular1);
            tableContent5.addCell(pdfCell);
            ExportRecordHandler.addBlankCell(tableContent5, 2);  //Add Blank Cell
            ExportRecordHandler.addTableRow(table1, tableContent5);
            PdfPTable tableContent6 = ExportRecordHandler.getBlankTableGSTForm5();
            pdfCell = ExportRecordHandler.addTaxnameCell("Contact Tel No. :       "+valueTextField.get(4), fontXSmallRegular1);
            tableContent6.addCell(pdfCell);
            pdfCell = ExportRecordHandler.addTaxnameCell(((user.getContactNumber() != null) ? user.getContactNumber() : ""), fontXSmallRegular1);
            tableContent6.addCell(pdfCell);
            ExportRecordHandler.addBlankCell(tableContent6, 1);  //Add Blank Cell
            ExportRecordHandler.addTableRow(table1, tableContent6);


            PdfPCell mainCell = new PdfPCell(table1);
            mainCell.setBorder(0);
//            mainCell.setPadding(10);
            mainTable.addCell(mainCell);
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("generateGSTForm5DetailsReportPdf: " + ex.getMessage(), ex);
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
    
    public void addExtraColumnsForSingaporeCountry(JSONObject jobj,PdfPTable tableContent) throws DocumentException {
        PdfPCell pdfCell1 = null,pdfCell2 = null;
        String emptyString="";
        String companyid = jobj.optString("companyid");
        if (jobj.has("transactionexchangerate")) {
            pdfCell1  = ExportRecordHandler.addTaxnameCell(jobj.optString("transactionexchangerate", ""), fontXSmallRegular1);            
        } else {
//            pdfCell1 = ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
            pdfCell1 = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
            pdfCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfCell1.setBorder(0);
        }
        pdfCell1.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableContent.addCell(pdfCell1);
        if (jobj.has("originalamount") && jobj.has("transactioncurrencyid")) {
            pdfCell2 = ExportRecordHandler.addCurrencyCell(jobj.optString("originalamount", ""), jobj.optString("transactioncurrencyid", ""), authHandlerDAOObj, fontXSmallRegular1, companyid);                        
        } else {
            pdfCell2 = new PdfPCell(new Paragraph(emptyString, fontXSmallBold1));
            pdfCell2.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfCell2.setBorder(0);
//                    ExportRecordHandler.addTitleCell(emptyString, fontXSmallBold1);
        }       
        pdfCell2.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        tableContent.addCell(pdfCell2);
    }
     private void addTrialBalanceCell_Custom(JSONObject jobj, PdfPTable table,ExtraCompanyPreferences extraCompanyPreferences) throws JSONException, SessionExpiredException {
        String d_openingval = "";
        String c_openingval = "";
        String d_periodval = "";
        String c_periodval = "";
        String d_endingval = "";
        String c_endingval = "";
        PdfPCell cell3 = null;
        PdfPCell accountCodeCell = null;
        PdfPCell d_endingcell = null;
        PdfPCell c_endingcell = null;
        PdfPCell d_openingcell = null;
        PdfPCell c_openingcell = null;
        PdfPCell d_periodcell = null;
        PdfPCell c_periodcell = null;
        String companyid = jobj.optString("companyid");
        PdfPCell cell5 = ExportRecordHandler.createBalanceSheetCell(c_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 0);
        if (!jobj.toString().equalsIgnoreCase("{}")) {
            String accName = jobj.get("accountname").toString();
            String accCode = (jobj.has("accountcode") && !StringUtil.isNullOrEmpty(jobj.getString("accountcode"))) ? jobj.get("accountcode").toString() : "";
            double d_openingamount = 0, c_openingamount = 0;
            double d_periodamount = 0, c_periodamount = 0;
            double d_endingamount = 0, c_endingamount = 0;
            int padding = Integer.parseInt(jobj.get("level").toString()) * 10;
            try {
                if (jobj.has("d_openingamount") && jobj.get("d_openingamount") != null && !jobj.optString("d_openingamount", "").equals("")) {
                    d_openingamount = Double.parseDouble(jobj.get("d_openingamount").toString());
                    d_openingamount = Double.valueOf(authHandler.formattedAmount(d_openingamount, companyid));
                    d_openingval = ExportRecordHandler.currencyRenderer(d_openingamount, companyid);
                }
                if (jobj.has("c_openingamount") && jobj.get("c_openingamount") != null && !jobj.optString("c_openingamount", "").equals("")) {
                    c_openingamount = Double.parseDouble(jobj.get("c_openingamount").toString());
                    c_openingamount = Double.valueOf(authHandler.formattedAmount(c_openingamount, companyid));
                    c_openingval = ExportRecordHandler.currencyRenderer(c_openingamount, companyid);
                }
                if (jobj.has("d_periodamount") && jobj.get("d_periodamount") != null && !jobj.optString("d_periodamount", "").equals("")) {
                    d_periodamount = Double.parseDouble(jobj.get("d_periodamount").toString());
                    d_periodamount = Double.valueOf(authHandler.formattedAmount(d_periodamount, companyid));
                    d_periodval = ExportRecordHandler.currencyRenderer(d_periodamount, companyid);
                }
                if (jobj.has("c_periodamount") && jobj.get("c_periodamount") != null && !jobj.optString("c_periodamount", "").equals("")) {
                    c_periodamount = Double.parseDouble(jobj.get("c_periodamount").toString());
                    c_periodamount = Double.valueOf(authHandler.formattedAmount(c_periodamount, companyid));
                    c_periodval = ExportRecordHandler.currencyRenderer(c_periodamount, companyid);
                }
                if (jobj.has("d_endingamount") && jobj.get("d_endingamount") != null && !jobj.optString("d_endingamount", "").equals("")) {
                    d_endingamount = Double.parseDouble(jobj.get("d_endingamount").toString());
                    d_endingamount = Double.valueOf(authHandler.formattedAmount(d_endingamount, companyid));
                    d_endingval = ExportRecordHandler.currencyRenderer(d_endingamount, companyid);
                }
                if (jobj.has("c_endingamount") && jobj.get("c_endingamount") != null && !jobj.optString("c_endingamount", "").equals("")) {
                    c_endingamount = Double.parseDouble(jobj.get("c_endingamount").toString());
                    c_endingamount = Double.valueOf(authHandler.formattedAmount(c_endingamount, companyid));
                    c_endingval = ExportRecordHandler.currencyRenderer(c_endingamount, companyid);
                }
            } catch (NumberFormatException ex) {
                Logger.getLogger(ExportRecord.class.getName()).log(Level.WARNING, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(ExportRecord.class.getName()).log(Level.WARNING, ex.getMessage());
            }
            if (jobj.has("fmt")) {
                cell3 = ExportRecordHandler.createBalanceSheetCell(accName, fontSmallBold1, Element.ALIGN_LEFT, 0, 0, 0);
                accountCodeCell = ExportRecordHandler.createBalanceSheetCell(accCode, fontSmallBold1, Element.ALIGN_CENTER, 0, 0, 0);
                d_openingcell = ExportRecordHandler.createBalanceSheetCell(d_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_openingcell = ExportRecordHandler.createBalanceSheetCell(c_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                d_periodcell = ExportRecordHandler.createBalanceSheetCell(d_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_periodcell = ExportRecordHandler.createBalanceSheetCell(c_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                d_endingcell = ExportRecordHandler.createBalanceSheetCell(d_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_endingcell = ExportRecordHandler.createBalanceSheetCell(c_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);

            } else if (padding == 0 && !accName.equals("")) {
                cell3 = ExportRecordHandler.createBalanceSheetCell(accName, fontSmallBold1, Element.ALIGN_LEFT, 0, 0, 0);
                accountCodeCell = ExportRecordHandler.createBalanceSheetCell(accCode, fontSmallBold1, Element.ALIGN_CENTER, 0, 0, 0);
                d_openingcell = ExportRecordHandler.createBalanceSheetCell(d_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_openingcell = ExportRecordHandler.createBalanceSheetCell(c_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                d_periodcell = ExportRecordHandler.createBalanceSheetCell(d_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_periodcell = ExportRecordHandler.createBalanceSheetCell(c_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                d_endingcell = ExportRecordHandler.createBalanceSheetCell(d_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                c_endingcell = ExportRecordHandler.createBalanceSheetCell(c_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                if (!d_endingval.equals("") || !c_endingval.equals("")) {
                    d_openingcell.setBorderWidthTop(1);
                    d_openingcell.setBorderWidthBottom(1);
                    d_openingcell.setBorderColor(Color.gray);
                    c_openingcell.setBorderWidthTop(1);
                    c_openingcell.setBorderWidthBottom(1);
                    c_openingcell.setBorderColor(Color.gray);
                    d_periodcell.setBorderWidthTop(1);
                    d_periodcell.setBorderWidthBottom(1);
                    d_periodcell.setBorderColor(Color.gray);
                    c_periodcell.setBorderWidthTop(1);
                    c_periodcell.setBorderWidthBottom(1);
                    c_periodcell.setBorderColor(Color.gray);
                    d_endingcell.setBorderWidthTop(1);
                    d_endingcell.setBorderWidthBottom(1);
                    d_endingcell.setBorderColor(Color.gray);
                    c_endingcell.setBorderWidthTop(1);
                    c_endingcell.setBorderWidthBottom(1);
                    c_endingcell.setBorderColor(Color.gray);
                    table.addCell(cell5);
                    table.addCell(cell3);
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isShowAccountCodeInFinancialReport()) {
                        table.addCell(accountCodeCell);
                    }
                    table.addCell(c_openingcell);
                    table.addCell(d_openingcell);
                    table.addCell(c_periodcell);
                    table.addCell(d_periodcell);
                    table.addCell(c_endingcell);
                    table.addCell(d_endingcell);
                    table.addCell(cell5);
                    for (int i = 1; i <= 10; i++) {
                        table.addCell(cell5);
                    }
                    return;
                }
            } else {
                if (jobj.has("bold")) {
                    cell3 = ExportRecordHandler.createBalanceSheetCell(accName, fontSmallBold1, Element.ALIGN_LEFT, 0, 0, 0);
                    accountCodeCell = ExportRecordHandler.createBalanceSheetCell(accCode, fontSmallBold1, Element.ALIGN_CENTER, 0, 0, 0);
                    d_openingcell = ExportRecordHandler.createBalanceSheetCell(d_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_openingcell = ExportRecordHandler.createBalanceSheetCell(c_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    d_periodcell = ExportRecordHandler.createBalanceSheetCell(d_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_periodcell = ExportRecordHandler.createBalanceSheetCell(c_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    d_endingcell = ExportRecordHandler.createBalanceSheetCell(d_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_endingcell = ExportRecordHandler.createBalanceSheetCell(c_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                } else {
                    cell3 = ExportRecordHandler.createBalanceSheetCell(accName, fontSmallRegular1, Element.ALIGN_LEFT, 0, 0, 0);
                    accountCodeCell = ExportRecordHandler.createBalanceSheetCell(accCode, fontSmallRegular1, Element.ALIGN_CENTER, 0, 0, 0);
                    d_openingcell = ExportRecordHandler.createBalanceSheetCell(d_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_openingcell = ExportRecordHandler.createBalanceSheetCell(c_openingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    d_periodcell = ExportRecordHandler.createBalanceSheetCell(d_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_periodcell = ExportRecordHandler.createBalanceSheetCell(c_periodval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    d_endingcell = ExportRecordHandler.createBalanceSheetCell(d_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                    c_endingcell = ExportRecordHandler.createBalanceSheetCell(c_endingval, fontSmallRegular1, Element.ALIGN_RIGHT, 0, 0, 5);
                }
            }
            table.addCell(cell5);
            table.addCell(cell3);
            if (extraCompanyPreferences != null && extraCompanyPreferences.isShowAccountCodeInFinancialReport()) {
                table.addCell(accountCodeCell);
            }
            table.addCell(c_openingcell);
            table.addCell(d_openingcell);
            table.addCell(c_periodcell);
            table.addCell(d_periodcell);
            table.addCell(c_endingcell);
            table.addCell(d_endingcell);
            table.addCell(cell5);
        }
    }
}
