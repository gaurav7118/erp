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

import com.itextpdf.text.html.HtmlTags;
import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONException;
import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.MessageSource;
import org.tuckey.web.filters.urlrewrite.utils.RegexPattern;

public class ExportRecordBeans {
    protected static final long serialVersionUID = -763555229410947890L;
    protected static Font fontSmallRegular = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    protected static Font fontSmallRegularsmall = FontFactory.getFont("Helvetica", 9, Font.NORMAL, Color.BLACK);
    protected static Font fontSmallBold = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    protected static Font fontMediumRegular = FontFactory.getFont("Helvetica", 11, Font.NORMAL, Color.BLACK);
    protected static Font fontMediumBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
    protected static Font fontTblMediumBold = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.GRAY);
    protected static Font fontTbl = FontFactory.getFont("Helvetica", 20, Font.NORMAL, Color.GRAY);
    protected static Font fontTbl1 = FontFactory.getFont("Helvetica", 20, Font.NORMAL, Color.BLACK);
    protected static Font fontMediumBold1 = FontFactory.getFont("Helvetica", 11, Font.BOLD, Color.BLACK);
    protected static Font fontSmallRegular1 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    protected static Font fontSmallBold1 = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    protected static String imgPath = "";
    protected ExportRecordBeans.EnglishNumberToWords EnglishNumberToWordsOjb = new ExportRecordBeans.EnglishNumberToWords();
    protected kwlCommonTablesDAO kwlCommonTablesDAOObj;
    protected accTermDAO accTermObj;
    protected authHandlerDAO authHandlerDAOObj;
    protected accInvoiceDAO accInvoiceDAOobj;
//    protected accTaxDAO accTaxObj;
    protected accReceiptDAO accReceiptDAOobj;
    protected accVendorPaymentDAO accVendorPaymentobj;
    protected accSalesOrderDAO accSalesOrderDAOobj;
    protected accPurchaseOrderDAO accPurchaseOrderobj;
    protected accCreditNoteDAO accCreditNoteDAOobj;
    protected accDebitNoteDAO accDebitNoteobj;
    protected accGoodsReceiptDAO accGoodsReceiptobj;
    protected accCurrencyDAO accCurrencyobj;
    protected exportMPXDAOImpl exportDaoObj;
    protected accTaxDAO accTaxObj;
    protected MessageSource messageSource;
    protected static com.krawler.utils.json.base.JSONObject config = null;
    protected PdfPTable header = null;
    protected PdfPTable footer = null;
    protected static String companyName = "";
    protected AccountingHandlerDAO accountingHandlerDAOobj;
    protected accAccountDAO accAccountDAOobj;
    protected PdfPTable mainTableGlobal = null;
    protected int  productHeaderTableGlobalNo = 0;
    protected PdfPTable tableClosedLineGlobal = null;
    protected static String globalCurrencyValue = "";
    protected static boolean isFromProductTable = false;
    protected static boolean isAttachProductTable = false;
    protected String[] globalTableHeader;
    protected static String linkHeader="";
    protected static String CompanyPDFFooter="";
    protected static String CompanyPDFHeader="";
    protected static String CompanyPDFPRETEXT="";
    protected static String CompanyPDFPOSTTEXT="";
    protected IntegrationCommonService integrationCommonService;
    
    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }
    
    protected static FontFamilySelector fontFamilySelector=new FontFamilySelector();
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

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
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
    
       public static PdfPCell createCellAllowingChinese(String string, Font fontTbl, int ALIGN_RIGHT, int i, int padd) {
            Phrase phrase = null;
            
            string = StringUtil.isNullOrEmpty(string)? "" : string;

            phrase = fontFamilySelector.process(string, FontContext.TABLE_DATA, fontTbl);
            
            PdfPCell cell = new PdfPCell(new Paragraph(phrase));
            
            cell.setHorizontalAlignment(ALIGN_RIGHT);
                cell.setBorder(i);
                cell.setPadding(padd);
        return cell;
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
                if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryIndiaCurrencyId || Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                    return " And " + soFar +" "+ val;
                } else {
                    return " And " + val + " " + soFar;
                }
            }
            return " And " + numNames[number] +" "+ val + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId, String companyid) {
            String answer = "";
            
            if (number == 0) {
                return "Zero";
            }

            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency, companyid);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }
        
        public String universalConvert(Double number, KWLCurrency currency, String companyid) {

            String snumber = Double.toString(number);
            String mask=authHandler.getCompleteDFStringForAmount("000000000000.", companyid);
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
        
        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            int n = Integer.parseInt(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = n / factor[i];
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }
    
    /*AMOL EDEWAR - HTML to PDF is not working for chinese characters. 
     * For now we have provided HTML parser to string not containing chinese.
     String containing chinese will be displayed as plain text without text formatting
     * Will update code if any solution appears.
     */
    public static PdfPCell createCellAllowingChineseAndEnglishHtmlTag(String string, Font fontTbl, int ALIGN_LEFT, int i, int padd) {
        PdfPCell cell = new PdfPCell();
        StyleSheet styles = new StyleSheet();
        styles.loadTagStyle("body", "size", "10pt");
        try {
            string = StringUtil.isNullOrEmpty(string) ? "" : string;
            StringReader sr = new StringReader(string);
            Phrase phrase = null;
            int flag = 0;
            String chineseString = "";
            phrase = fontFamilySelector.process(string, FontContext.TABLE_DATA, fontTbl);
            String pat = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";

            Pattern pt = Pattern.compile(pat);
            Matcher m = pt.matcher(phrase.toString());

            chineseString = phrase.toString().replaceFirst("\\[", "");            //Replace First  opening square bracket [ 
            chineseString = chineseString.replace(chineseString.substring(chineseString.length() - 1), "");  //Replace last closeing square bracket ] 

            if (m.find() && chineseString.equals(string)) {                      // find matching html tag in english string
                flag = 1;
            } else if (m.find() && !chineseString.equals(string)) {                      // find matching html tag in chinese string
                string = string.replaceAll(pat, "");                               //Replace html Tag from Chinese string
                phrase = fontFamilySelector.process(string, FontContext.TABLE_DATA, fontTbl);
            }

            if (flag == 1) {
                ArrayList arrayElementList = HTMLWorker.parseToList(sr, styles);
                for (int j = 0; j < arrayElementList.size(); ++j) {
                    Element e = (Element) arrayElementList.get(j);
                    cell.addElement(e);
                }
            } else {
                cell = new PdfPCell(new Paragraph(phrase));
            }
            cell.setHorizontalAlignment(ALIGN_LEFT);
            cell.setBorder(i);
            cell.setPadding(padd);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return cell;
    }
}
