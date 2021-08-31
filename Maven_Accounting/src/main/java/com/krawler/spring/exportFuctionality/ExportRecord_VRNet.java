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
package com.krawler.spring.exportFuctionality;

import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.AccProductCustomData;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingCreditNote;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingDebitNote;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.DeliveryOrder;
import com.krawler.hql.accounting.DeliveryOrderDetail;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.RequestForQuotation;
import com.krawler.hql.accounting.RequestForQuotationDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Term;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.VendorQuotation;
import com.krawler.hql.accounting.VendorQuotationDetail;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import static com.krawler.spring.exportFuctionality.ExportRecordBeans.fontSmallRegular;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.spring.accounting.vendor.accVendorHandler;
import com.krawler.spring.accounting.customer.accCustomerHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONObject;


/**
 *
 * @author sagar
 */
public class ExportRecord_VRNet extends ExportRecordBeans implements MessageSourceAware{
      private AccCommonTablesDAO accCommonTablesDAO;
      
    private static Font fontSmallRegular = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallRegularsmall = FontFactory.getFont("Times New Roman", 9, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Times New Roman", 10, Font.BOLD, Color.BLACK);
    private static Font fontMediumRegular = FontFactory.getFont("Times New Roman", 11, Font.NORMAL, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Times New Roman", 12, Font.BOLD, Color.BLACK);
    private static Font fontTblMediumBold = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.GRAY);
    private static Font fontTbl = FontFactory.getFont("Times New Roman", 20, Font.NORMAL, Color.GRAY);
    private static Font fontMediumBold1 = FontFactory.getFont("Times New Roman", 11, Font.BOLD, Color.BLACK);
    private static Font fontSmallRegular1 = FontFactory.getFont("Helvetica", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold1 = FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.BLACK);
    Font local_fontSmallBold = FontFactory.getFont("Helvetica", 9, Font.BOLD, Color.BLACK);
    private static  int pageNo=0;
    private static  int modeNo=2;
      
        
    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    @Deprecated
    public ByteArrayOutputStream createVRNetPdf(HttpServletRequest request, String currencyid, String billid, DateFormat formatter, 
            int mode, double amount, String logoPath, String customer, String accname, String address, boolean isExpenseInv, 
            String CompanyID, String userId,String baseCurrency) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        modeNo=mode;
        double cndnTotalOtherwiseAmount = 0;
        Document document = null;
        PdfWriter writer = null;
        boolean otherwiseFlag = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("otherwise"))) {
            otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
        }
//        Font local_fontSmallBold = FontFactory.getFont("Helvetica", 9, Font.BOLD, Color.BLACK);
        try {
            
            String poRefno = "";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 40, 30);
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ExportRecord_VRNet.EndPage());
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = null;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            KwlReturnObject bAmt = null;
            boolean isCompanyLogo = true;
            boolean addShipTo = true;
            boolean isCompanyTemplateLogo = false;
            PdfPTable tab3 = null;
            String approverName = "______________________________";
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            Rectangle page = document.getPageSize();
            Company com = null;
            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();
            String companyIdForCAP = CompanyID;
            CompanyAccountPreferences preferences=(CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),companyIdForCAP);
            int moduleid = -1;
            moduleid = ExportRecordHandler.getModuleId(mode, billid, CompanyID, kwlCommonTablesDAOObj);
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(CompanyID, moduleid);
            List<PdfTemplateConfig> textList = templateConfig.getEntityList();
            if (textList.isEmpty()) {
                CompanyPDFFooter = preferences.getPdffooter()==null?"":preferences.getPdffooter();
                CompanyPDFHeader = preferences.getPdfheader()==null?"":preferences.getPdfheader();
                CompanyPDFPRETEXT = preferences.getPdfpretext()==null?"":preferences.getPdfpretext();
                CompanyPDFPOSTTEXT = preferences.getPdfposttext()==null?"":preferences.getPdfposttext();
            } else {
                for (PdfTemplateConfig config : textList) {
                    CompanyPDFFooter = config.getPdfFooter() == null ? (preferences.getPdffooter()==null?"":preferences.getPdffooter()): config.getPdfFooter();
                    CompanyPDFHeader = config.getPdfHeader() == null ? (preferences.getPdfheader()==null?"":preferences.getPdfheader()): config.getPdfHeader();
                    CompanyPDFPRETEXT = config.getPdfPreText() == null ? (preferences.getPdfpretext()==null?"":preferences.getPdfpretext()) : config.getPdfPreText();
                    CompanyPDFPOSTTEXT = config.getPdfPostText() == null ? (preferences.getPdfposttext()==null?"":preferences.getPdfposttext()) : config.getPdfPostText();
                }
            }
            String preText = StringUtil.isNullOrEmpty(CompanyPDFPRETEXT) ? "" : CompanyPDFPRETEXT;
            String postText = StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) ? "" : CompanyPDFPOSTTEXT;

            Projreport_Template defaultTemplate = (Projreport_Template) kwlCommonTablesDAOObj.getClassObject(Projreport_Template.class.getName(), Constants.HEADER_IMAGE_TEMPLATE_ID);
            if (defaultTemplate != null) {
                config = new com.krawler.utils.json.base.JSONObject(defaultTemplate.getConfigstr());
            }
            String linkTo = "";
            KwlReturnObject idresult = null;
            String customerName = "";
            String customerEmail = "";
            String terms = "";
            String billTo = "";
            String shipAddress = "";
            String memo = "";
            String porefno = "";
            String billAddress = "";
            String salesPerson = null;
            boolean isInclude = false; //Hiding or Showing P.O. NO field in single PDF 
            Iterator itr = null;
            Iterator itr1 = null;
            linkHeader = "";
            String invno = "";
            Date dueDate = null;
            Date shipDate = null;
            String shipvia = null;
            String fob = null;
            Date entryDate = null;
            BillingPurchaseOrder po = null;
            SalesOrder sOrder = null;
            PurchaseOrder pOrder = null;
            Tax mainTax = null;
            Quotation quotation = null;
            VendorQuotation venquotation = null;
            RequestForQuotation RFQ = null;
            String company[] = new String[4];
            com  = null;
            Account cEntry = null;
            String datetheader = "";
            String[] headerDetails = {"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
            String[] headerDetailsForVrnet = {"Terms", "Due Date", "Sales Person"};//Header name for VRNET Comapny 
            HashMap<String,Object> addrParams=new HashMap<String, Object>();
            addrParams.put("isDefaultAddress", true);
            PdfPCell bottomCell=null;
            PdfPTable bottomTable=null;
            PdfPTable thanksGivingTable=null;
            PdfPTable receivedByTable1=null;
            PdfPTable receivedByTable2=null;
            if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT 
                    && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT 
                    && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT 
                    && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE 
                    && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE){
                bottomTable = new PdfPTable(2); //for 2 column
                bottomTable.setWidthPercentage(100);
                bottomTable.setWidths(new float[]{60, 40});
                PdfPTable postText1 = new PdfPTable(1);
                PdfPCell right = ExportRecordHandler.getVRNetHtmlCell(postText==null?"":postText.trim(), mainTable, baseUrl);
                right.setPaddingTop(35);
                if(mode != StaticValues.AUTONUM_PURCHASEORDER)
                    postText1.addCell(right);
                
                PdfPTable rightTable = new PdfPTable(1);
                if(mode==StaticValues.AUTONUM_INVOICE){
                    right = createCell("For VRnet(S) Pte Ltd", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(20);
                    rightTable.addCell(right);
                    
                    right = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(35);
                    rightTable.addCell(right);
                }else{
                    right = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(35);
                    rightTable.addCell(right);
                    
                    right = createCell("Name:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(10);
                    rightTable.addCell(right);
                }
                PdfPCell cell1 = new PdfPCell(postText1);
                cell1.setBorder(0);
                bottomTable.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                bottomTable.addCell(cel2);
                
                bottomCell = new PdfPCell(bottomTable);
                bottomCell.setBorder(0);
            }
            if(mode==StaticValues.AUTONUM_QUOTATION|| mode ==StaticValues.AUTONUM_SALESORDER ) {
                thanksGivingTable = new PdfPTable(2); //for 2 column
                thanksGivingTable.setWidthPercentage(100);
                thanksGivingTable.setWidths(new float[]{70, 40});
                
                PdfPTable leftTable = new PdfPTable(1);
                PdfPCell cell3 = createCell("Yours faithfully,", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingTop(35);
                cell3.setPaddingBottom(5);
                leftTable.addCell(cell3);
                cell3 = createCell("VRnet(S) Pte Ltd", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingBottom(35);
                cell3.setPaddingTop(5);
                leftTable.addCell(cell3);
                
                cell3 = createCell("______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingRight(10);
                leftTable.addCell(cell3);
                if(mode==StaticValues.AUTONUM_QUOTATION){
                    quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                     salesPerson=quotation.getSalesperson()!=null?quotation.getSalesperson().getValue():null;
                    if(salesPerson!=null){
                        cell3 = createCell("             ("+salesPerson+")", fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                        cell3.setPaddingBottom(35);
                        cell3.setPaddingTop(5);
                        leftTable.addCell(cell3);
                    }
                }
                PdfPTable rightTable = new PdfPTable(1);
                cell3 = createCell("Offer Accepted by:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(35);
                cell3.setPaddingBottom(35);
                rightTable.addCell(cell3);
                
                cell3 = createCell("______________________________\n\n Official Signature & Stamp", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                rightTable.addCell(cell3);
                
                PdfPCell cell1 = new PdfPCell(leftTable);
                cell1.setBorder(0);
                thanksGivingTable.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                thanksGivingTable.addCell(cel2);
//                mainTable.addCell(mainCell63);
            } else if(mode==StaticValues.AUTONUM_DEBITNOTE|| mode ==StaticValues.AUTONUM_CREDITNOTE ) {
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+" : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setBorder(0);
               receivedByTable1 = new PdfPTable(1);
                receivedByTable1.addCell(cell3);
                cell3 = createCell(memo, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setBorder(0);
                receivedByTable1.addCell(cell3);
              //  mainTable.addCell(mainCell62);
                
                receivedByTable2 = new PdfPTable(2); //for 2 column
                receivedByTable2.setWidthPercentage(100);
                receivedByTable2.setWidths(new float[]{60, 40});
                
                PdfPTable leftTable = new PdfPTable(1);
                if(mode==StaticValues.AUTONUM_CREDITNOTE) {
                    cell3 = createCell("Goods Received By:______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    cell3.setPaddingRight(10);
                    cell3.setPaddingTop(35);
                    cell3.setPaddingLeft(5);
                    cell3.setPaddingBottom(35);
                    leftTable.addCell(cell3);
                } else {
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    cell3.setPaddingRight(10);
                    cell3.setPaddingTop(35);
                    cell3.setPaddingLeft(5);
                    cell3.setPaddingBottom(35);
                    leftTable.addCell(cell3);
                }
                
                PdfPTable rightTable = new PdfPTable(1);
                cell3 = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(35);
                rightTable.addCell(cell3);
                
                cell3 = createCell("Name:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(10);
                rightTable.addCell(cell3);
                
                PdfPCell cell1 = new PdfPCell(leftTable);
                cell1.setBorder(0);
                receivedByTable2.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                receivedByTable2.addCell(cel2);
               // mainTable.addCell(mainCell63);
                
             } 
             mainTable.setSplitLate(false); 
            if(mode == StaticValues.AUTONUM_DELIVERYORDER) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);    
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);                    
                
                String orderID ="";
                String theader = "";
                String pointPern = "";
                String recQuantity = "";
                String status="";
                DeliveryOrder deliveryOrder=null;
                deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), billid);
                String invoicePostText=deliveryOrder.getPostText()==null?"":deliveryOrder.getPostText();
                postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)&& deliveryOrder.getTemplateid() != null)?deliveryOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = deliveryOrder.getCustomer().getAccount();
                invno = deliveryOrder.getDeliveryOrderNumber();
                entryDate = deliveryOrder.getOrderDate();
                customerName = deliveryOrder.getCustomer().getName();
                status=deliveryOrder.getStatus() != null ? deliveryOrder.getStatus().getValue():"";
                customerEmail= deliveryOrder.getCustomer()!=null?deliveryOrder.getCustomer().getEmail():"";
                billTo="Bill To";
                if (deliveryOrder.getBillingShippingAddresses() != null) { //If DO have address from show address then showing that address otherwise showing customer address
                    billAddress = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), true);
                    shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), false);
                } else {
                    addrParams.put("customerid", deliveryOrder.getCustomer().getID());
                    addrParams.put("companyid", com.getCompanyID());
                    addrParams.put("isBillingAddress", false);
                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    addrParams.put("isBillingAddress", true);
                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                }
                memo = deliveryOrder.getMemo();
                shipDate = deliveryOrder.getShipdate();
                shipvia = deliveryOrder.getShipvia();
                fob = deliveryOrder.getFob();
                orderID = deliveryOrder.getID();
                theader = messageSource.getMessage("acc.accPref.autoDO", null, RequestContextUtils.getLocale(request));
                datetheader = messageSource.getMessage("acc.accPref.autoDateDO", null, RequestContextUtils.getLocale(request));
                pointPern = "acc.common.to";
                recQuantity = "acc.accPref.NewdeliQuant";

                com = deliveryOrder.getCompany();
                if (deliveryOrder.getTemplateid() != null) {
                    config = new com.krawler.utils.json.base.JSONObject(deliveryOrder.getTemplateid().getConfigstr());
                    // document = getTemplateConfig(document,writer);
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(com)) {
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }
                }
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                } 
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                theader = "Delivery Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});
                if (addShipTo) {
                    tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                }
                if (isCompanyTemplateLogo || storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if (addShipTo) {
                        if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                            table1.setWidths(new float[]{65, 35});
                        }
                        tab2 = ExportRecordHandler.getDateTable2(entryDate, invno, datetheader, formatter, invCell);
                    }
                }
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
//                mainCell11.setPaddingBottom(5);
//                cell1.setPaddingTop(140);
                mainTable.addCell(mainCell11);
                
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                    filter_names.add("deliveryOrder.ID");
                }

                filter_params.add(orderID);
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                    idresult = accInvoiceDAOobj.getDeliveryOrderDetails(invRequestParams);
                }

                itr = idresult.getEntityList().iterator();
                itr1 = idresult.getEntityList().iterator();
                if(itr1.hasNext()) {
                    DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr1.next();
                    if(row8.getCidetails()!=null){
                        Invoice inv = row8.getCidetails().getInvoice();
                        linkTo= inv.getInvoiceNumber();
                        porefno = inv.getPoRefNumber();
                        terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
//                        Date InvEntryDate = inv.getJournalEntry().getEntryDate();
                        Date InvEntryDate = inv.getCreationDate();
                        Date InvdueDate = inv.getDueDate();
                        int termdays = (int)( (InvdueDate.getTime() - InvEntryDate.getTime()) / (1000 * 60 * 60 * 24));
                        HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
                        termrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                        termrequestParams.put("termdays", termdays);

                        KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
                        List<Term> termlist = termresult.getEntityList();
                        if(termlist.size()>0) {
                            terms = termlist.get(0).getTermname();
                        }
                        if (inv.getMasterSalesPerson() != null) {   //if User Class returns the Null Valu
                            salesPerson = inv.getMasterSalesPerson() != null ? inv.getMasterSalesPerson().getValue() : "";
                        }
                    }else if(row8.getSodetails()!=null){
                        invno=row8.getSodetails().getSalesOrder().getSalesOrderNumber();                                    
                    } 
                }

                if(!StringUtil.isNullOrEmpty(preText)){
                         ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                }
                
                if (mode != StaticValues.AUTONUM_RFQ) {
                    PdfPTable addressMainTable = null;
//                    .getAddressTable(customerName,billAddres,customerEmail,billTo,shipTo,true);
                    addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, addShipTo);
                    if (!addShipTo) {                                                                       
                        PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj,salesPerson, entryDate, linkTo, invno, porefno, terms, currencyid, formatter);
                        PdfPCell cel3 = new PdfPCell(shipToTable);
                        cel3.setBorder(0);
                        addressMainTable.addCell(cel3);
                    }

                    PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(1);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);
                }     
                boolean companyFlag = (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && (mode == StaticValues.AUTONUM_DELIVERYORDER));
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                    if (!linkHeader.equalsIgnoreCase("")) {
                        headerList.add(linkHeader);
                    }
                if (!(companyFlag)) {
                    headerList.add(messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)));
                }
                 //  get product custom fields 
                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(com.getCompanyID(), Constants.Acc_Product_Master_ModuleId, 0));
                fieldrequestParams.put("relatedmoduleid", String.valueOf(Constants.Acc_Delivery_Order_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                KwlReturnObject kwlcustomColumn = accAccountDAOobj.getFieldParams(fieldrequestParams);
                List<FieldParams> customColList = kwlcustomColumn.getEntityList();
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                HashMap<String, Integer> customMap = new HashMap();
                List<String> customHeaderList = new ArrayList();
                for (FieldParams field : customColList) {
                    headerList.add(field.getFieldlabel());
                    customHeaderList.add(field.getFieldlabel());
                    String[] relatedTo = field.getRelatedmoduleid().split(",");
                    String[] relatedToWidth = field.getRelatedmodulepdfwidth()!=null ? field.getRelatedmodulepdfwidth().split(",") : "".split(",");
                    for(int customCnt=0; customCnt<relatedTo.length; customCnt++) {
                        if(relatedTo[customCnt].equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                            if(relatedToWidth.length > customCnt && !StringUtil.isNullOrEmpty(relatedToWidth[customCnt])) {
                                customMap.put(field.getFieldlabel(), Integer.parseInt(relatedToWidth[customCnt]));
                            } else {
                                customMap.put(field.getFieldlabel(), 0);
                            }
                        }
                    }
                }
                
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                if (mode != StaticValues.AUTONUM_RFQ && mode != StaticValues.AUTONUM_DELIVERYORDER) {
                    headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                    headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                }

                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                PdfPTable table = null;
                if (mode == StaticValues.AUTONUM_RFQ) {
                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    productHeaderTableGlobalNo = 1;
                } else {
                    table = ExportRecordHandler.getTable(linkHeader, true);
                    productHeaderTableGlobalNo = 2;
                }
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                    productHeaderTableGlobalNo = 7;
                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableForVRNetDO();
                    }
                }
                
                
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                if (mode == StaticValues.AUTONUM_RFQ) {
                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
                } else {
                    table = ExportRecordHandler.getTable(linkHeader, true);
                }

                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableForVRNetDO();
                    }
                }
                
//                String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)),
//                        messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)),
//                        messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request))};
//                PdfPTable table = ExportRecordHandler.getBlankTableForVRNetDO();
//                productHeaderTableGlobalNo=3;
//                PdfPCell invcell = null;
//                for (int i = 0; i < header.length; i++) {
//                     invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
//                     invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                     invcell.setBackgroundColor(Color.LIGHT_GRAY);
//                     invCell.setBorder(0);
//                     invcell.setPadding(3);
//                     table.addCell(invcell);
//                }
//                globalTableHeader=header;
//                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
//                table = ExportRecordHandler.getBlankTableForVRNetDO();
                
                int index=0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo= "";
                    double quantity = 0,deliverdQuantity=0;
                    String uom = "";
//                    String linkTo="-";

                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                            prodDesc = row8.getDescription();
                        }else{
                             if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                 prodDesc= row8.getProduct().getDescription();
                             }
                        }
                        prodName = row8.getProduct().getName();
                        if(row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())){
                            partNo = row8.getPartno(); 
//                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
//                            prodName += "\n" + partno;
//                            prodName += "\n" ;
                        }
                        prodName += "\n" ;
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getUom()==null?(row8.getProduct().getUnitOfMeasure()==null?"":row8.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row8.getUom().getNameEmptyforNA();
                                                        if(row8.getCidetails()!=null){
                            linkTo=row8.getCidetails().getInvoice().getInvoiceNumber();
                        }else if(row8.getSodetails()!=null){
                            linkTo=row8.getSodetails().getSalesOrder().getSalesOrderNumber();                                    
                        }
                                                        
                        AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), row8.getProduct().getID());
                        if (obj != null) {
                            productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        }                                    
                    }

                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);                             
//                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    invCell.setBorder(0);
//                    table.addCell(invcell);
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invCell.setBorder(0);
                    table.addCell(invcell);

                        for (String varEntry : customHeaderList) {
                            boolean isBlankVal = true;
                            if(variableMap.containsKey("Custom_"+varEntry)) {
                                String coldata = variableMap.get("Custom_"+varEntry).toString();
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    invcell = createCell(coldata, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                    table.addCell(invcell);
                                    isBlankVal = false;
                                } 
                            }
                            if(isBlankVal){
                                invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                table.addCell(invcell);
                            }
                        }
                    
                    
                    
                    String qtyStr = Double.toString(quantity);
//                             if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_DELIVERYORDER ) {
                                   qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
//                                        }
                    invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
//                    invcell = createCell(Double.toString((double)deliverdQuantity)+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    invCell.setBorder(0);
//                    table.addCell(invcell);                             
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row                                
                    if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                        if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                        } else {
                            table = ExportRecordHandler.getBlankTableForVRNetDO();
                        }
                    }
                }
                
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;

                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
    
                
                // get after Items table height
                bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                bottomTable.calculateHeightsFast();
                float bottomTableHeight = bottomTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - bottomTableHeight - aboveProdTableContent  - 85*pageNo) / blankRowHeight; //top+bottom=5+30
                int noOfCols = 3;
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }

                 mainTable = new PdfPTable(1);
                 mainTable.setWidthPercentage(100);
                 
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
               
                

             //   PdfPCell mainCell61 = new PdfPCell(helpTable);
//                mainCell61.setBorder(0);
//                mainTable.addCell(mainCell61);
                
                document.add(mainTable);
                document.getPageNumber();
                  
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
            }  else if (mode == StaticValues.AUTONUM_QUOTATION) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                currencyid = (quotation.getCurrency()==null)? currencyid : quotation.getCurrency().getCurrencyID();
                com = quotation.getCompany();
                customerName = quotation.getCustomer().getName();
                customerEmail= quotation.getCustomer()!=null?quotation.getCustomer().getEmail():"";
                String phoneNo = quotation.getCustomer().getContactNumber();
                String faxNo = quotation.getCustomer().getFax();
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(),false);
                 if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in quotation table 
                   shipAddress=quotation.getShipTo()==null?"":quotation.getShipTo();
                } 
                terms=quotation.getCustomer()!=null?quotation.getCustomer().getCreditTerm().getTermname():"";
                billTo="Bill To";
                isInclude=false;
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in quotation table 
                  billAddress=quotation.getBillTo()!=null?quotation.getBillTo():"";
                }
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                
                filter_names.add("quotation.ID");
                filter_params.add(quotation.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                memo = quotation.getMemo();

                    
                String invoicePostText=quotation.getPostText()==null?"":quotation.getPostText();
                postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && quotation.getTemplateid() != null)?quotation.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = quotation.getCustomer().getAccount();
                invno = quotation.getquotationNumber();
                dueDate=quotation.getDueDate();
                entryDate = quotation.getQuotationDate();
                mainTax = quotation.getTax();
                shipDate = quotation.getShipdate();
                shipvia = quotation.getShipvia();
                fob = quotation.getFob();
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                isCompanyTemplateLogo = true;
                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{55, 45});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Customer Quotation";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and Quotation details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, null, phoneNo, faxNo, customerEmail, formatter, mode);
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                productHeaderTableGlobalNo = 6;
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                     Phrase phrase1 =new Phrase();
                     Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    
                    QuotationDetail row7 = (QuotationDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row7.getDescription())){
                        prodDesc = row7.getDescription();
                    }else{
                       if (!StringUtil.isNullOrEmpty(row7.getProduct().getDescription())) {
                            prodDesc = row7.getProduct().getDescription();
                        }
                    }
                    prodName = row7.getProduct().getName();
                    quantity =  row7.getQuantity();
                    rate = row7.getRate() ;
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountQuotation = (row7.getDiscountispercent() == 1)? rateInCurr*quantity *row7.getDiscount()/100 : row7.getDiscount();
                    uom = row7.getUom()==null?(row7.getProduct().getUnitOfMeasure()==null?"":row7.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row7.getUom().getNameEmptyforNA();
                    
                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                     invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                     invcell.setBorder(0);
//                    invcell = createCell(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                        rate = rateInBase;
                    }
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate * quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discount != null) {
                        amount1 -= row7.getDiscount();
                    }
                    if (discountQuotation != 0) {
                        amount1 -= discountQuotation;
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    if (row7!= null&&row7.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row7.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row7.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row7.getRowTaxAmount();
                        }
                    }  
                    
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (mode == StaticValues.AUTONUM_RFQ) {
                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    } else {
                        table = ExportRecordHandler.getTable(linkHeader, true);
                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());   //create table with 6 columns for extra row and Bottom 
                        
                } // END Of Product Details
                
                  //table = getBlankTable();
  
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                if(mode==StaticValues.AUTONUM_QUOTATION){
                    term= ExportRecordHandler.appendTermDetailsQuotation(accSalesOrderDAOobj,authHandlerDAOObj, quotation, table, currencyid,mode, CompanyID);
                }
                total+=term;
                totalAmount+=term;
                if(!quotation.isPerDiscount()){
                    discountTotalQuotation = quotation.getDiscount();
                    total = total - quotation.getDiscount();
                    totalAmount = total;
                } else {
                    discountTotalQuotation = total * quotation.getDiscount()/100;
                    total -= (total * quotation.getDiscount()/100);
                    totalAmount = total;
                }
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation, CompanyID), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
               
                // get after Items table height 
                
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    
                    PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                   
                
                thanksGivingTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * thanksGivingTable.getWidthPercentage() / 100);
                thanksGivingTable.calculateHeightsFast();
                float thanksGivingTableHeight = thanksGivingTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - thanksGivingTableHeight - aboveProdTableContent - summeryTableHeight - 90*pageNo) / blankRowHeight; //top+bottom=25+30+5 padding at end 5
                int noOfCols = headerList.size();
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
              
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                //this code is moved to up for calculation of free page size.
                mainTable.addCell(mainCell12);
            } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                sOrder = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), billid);
                if(sOrder.getApprover()!=null)
                    approverName=sOrder.getApprover().getFirstName() +" "+sOrder.getApprover().getLastName();
                currencyid = (sOrder.getCurrency()==null)? currencyid : sOrder.getCurrency().getCurrencyID();
                com = sOrder.getCompany();

                 if (sOrder.getTemplateid() != null) {
                    config = new com.krawler.utils.json.base.JSONObject(sOrder.getTemplateid().getConfigstr());
                   // document = getTemplateConfig(document,writer);
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if(!ExportRecordHandler.checkCompanyTemplateLogoPresent(com)){
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }                        
                }
                 
                String invoicePostText=sOrder.getPostText()==null?"":sOrder.getPostText();
                postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?sOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = sOrder.getCustomer().getAccount();
                invno = sOrder.getSalesOrderNumber();
                dueDate=sOrder.getDueDate();
                entryDate = sOrder.getOrderDate();
                mainTax = sOrder.getTax();
                shipDate=sOrder.getShipdate();
                shipvia=sOrder.getShipvia();
                fob=sOrder.getFob();
                
                customerName = sOrder.getCustomer().getName();
                customerEmail= sOrder.getCustomer()!=null?sOrder.getCustomer().getEmail():"";
                String faxNo= sOrder.getCustomer().getFax();
                String PhoneNo = sOrder.getCustomer().getContactNumber();
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(),false);
                 if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in invoice table 
                   shipAddress=sOrder.getShipTo()==null?"":sOrder.getShipTo();
                } 
                terms=sOrder.getCustomer()!=null?sOrder.getCustomer().getCreditTerm().getTermname():"";       
                billTo="Bill To";
                isInclude=false;
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                          
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                  if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in invoice table 
                     billAddress = sOrder.getBillTo()!=null?sOrder.getBillTo():"";
                }
                filter_names.add("salesOrder.ID");
                filter_params.add(sOrder.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                memo = sOrder.getMemo();

                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Sales Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and Quotation details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, null, PhoneNo, faxNo, customerEmail, formatter ,mode );
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                productHeaderTableGlobalNo = 6;
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                     Phrase phrase1 =new Phrase();
                     Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    
                    SalesOrderDetail row5 = (SalesOrderDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row5.getDescription())){
                        prodDesc = row5.getDescription();
                    }else{
                       if (!StringUtil.isNullOrEmpty(row5.getProduct().getDescription())) {
                            prodDesc = row5.getProduct().getDescription();
                        }
                    }
                    prodName = row5.getProduct().getName();
                    quantity =  row5.getQuantity();
                    rate = row5.getRate() ;
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountQuotation = (row5.getDiscountispercent() == 1)? rateInCurr*quantity *row5.getDiscount()/100 : row5.getDiscount();
                    uom = row5.getUom()==null?(row5.getProduct().getUnitOfMeasure()==null?"":row5.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row5.getUom().getNameEmptyforNA();

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);
                    
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                        rate = rateInBase;
                    }
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate * quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discount != null) {
                        amount1 -= row5.getDiscount();
                    }
                    if (discountQuotation != 0) {
                        amount1 -= discountQuotation;
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    if (row5!= null&&row5.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row5.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row5.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row5.getRowTaxAmount();
                        }
                    }  
                    
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (mode == StaticValues.AUTONUM_RFQ) {
                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    } else {
                        table = ExportRecordHandler.getTable(linkHeader, true);
                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());   //create table with 6 columns for extra row and Bottom 
                        
                } // END Of Product Details
                
                
                //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                term= ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj,authHandlerDAOObj,sOrder , table1, currencyid,mode, CompanyID);
                total+=term;
                totalAmount+=term;
                if(sOrder.getDiscount() != 0) {
                    if(!sOrder.isPerDiscount()){
                            discountTotalQuotation = sOrder.getDiscount();
                            total = total - sOrder.getDiscount();
                            totalAmount = total;
                    } else {
                            discountTotalQuotation = total * sOrder.getDiscount()/100;
                            total -= (total * sOrder.getDiscount()/100);
                            totalAmount = total;
                    }
                }
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation, CompanyID), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                 ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
             
                
                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);

                 thanksGivingTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * thanksGivingTable.getWidthPercentage() / 100);
                thanksGivingTable.calculateHeightsFast();
                float thanksGivingTableHeight = thanksGivingTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - thanksGivingTableHeight - aboveProdTableContent - summeryTableHeight - 90*pageNo) / blankRowHeight; //top+bottom=30+30+5 padding at end
                int noOfCols = 5;
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);
                 PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);

                
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
            } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                KWLCurrency currency = null;
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                currencyid = (pOrder.getCurrency()==null)? currencyid : pOrder.getCurrency().getCurrencyID();
                com = pOrder.getCompany();
                if(pOrder.getApprover()!=null)
                    approverName=pOrder.getApprover().getFirstName() +" "+pOrder.getApprover().getLastName();

                cEntry = pOrder.getVendor().getAccount();
                invno = pOrder.getPurchaseOrderNumber();
                dueDate=pOrder.getDueDate();
                entryDate = pOrder.getOrderDate();
                mainTax = pOrder.getTax();
                shipDate=pOrder.getShipdate();
                shipvia=pOrder.getShipvia();
                fob=pOrder.getFob();
                currency = pOrder.getCurrency();
                
//                Invoice inv = pOrder.
//                terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
//                Date InvEntryDate = inv.getJournalEntry().getEntryDate();
//                Date InvdueDate = inv.getDueDate();
//                int termdays = (int)( (InvdueDate.getTime() - InvEntryDate.getTime()) / (1000 * 60 * 60 * 24));
//                HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
//                termrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//                termrequestParams.put("termdays", termdays);
//
//                KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
//                List<Term> termlist = termresult.getEntityList();
//                if(termlist.size()>0) {
//                    terms = termlist.get(0).getTermname();
//                }
                
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                
                customerName = pOrder.getVendor().getName();
                customerEmail= pOrder.getVendor()!=null?pOrder.getVendor().getEmail():"";
                terms=pOrder.getVendor()!=null?pOrder.getVendor().getDebitTerm().getTermname():"";                        
                billTo="Sold To";
                isInclude=false;
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(), true);//true used for billing  and false  for shipping 
                if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in purchaseorder table 
                      billAddress = pOrder.getBillTo()==null?"":pOrder.getBillTo();
                }
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(),false);
                if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in purchaseorder table 
                   shipAddress=pOrder.getShipTo()==null?"":pOrder.getShipTo();
                }
                filter_names.add("purchaseOrder.ID");
                filter_params.add(pOrder.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                itr1 = idresult.getEntityList().iterator();
                if(itr1.hasNext()) {
                    PurchaseOrderDetail details = (PurchaseOrderDetail) itr1.next();
                    if (!StringUtil.isNullOrEmpty(details.getSalesorderdetailid())) {
                        KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), details.getSalesorderdetailid());
                        SalesOrderDetail salesOrderDetail = (SalesOrderDetail) sodetailresult.getEntityList().get(0);
                        if(salesOrderDetail!=null) {
                            linkTo = salesOrderDetail.getSalesOrder().getSalesOrderNumber();
                            if(salesOrderDetail.getSalesOrder().getSalesperson()!=null)
                                salesPerson = salesOrderDetail.getSalesOrder().getSalesperson().getValue();
                        }
                    } 
                    else if (details.getVqdetail() != null) {
                        linkTo = details.getVqdetail().getVendorquotation().getQuotationNumber();
                    }
                }
                terms = pOrder.getTerm()!=null ? pOrder.getTerm().getTermname() : "";
                memo = pOrder.getMemo();
                
                isInclude=false;
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }
                
                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                isCompanyTemplateLogo = true;
                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{65, 35});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Purchase Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                mainTable.setSplitLate(false);
                // Added top table with sell address details and CN/DN details
                PdfPTable addressMainTable = null;
//                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddres, customerEmail, billTo, shipTo, false);
                addressMainTable = ExportRecordHandler.getAddressTableForVRnetPurchaseOrder(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, customerName, billAddress, billTo, shipAddress, terms, formatter);
//                PdfPCell cel3 = new PdfPCell(shipToTable);
//                cel3.setBorder(0);
//                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                headerList.add("Product Code");
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{6, 10, 40,12, 15, 17});
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                table.setSplitLate(false);
                int index = 0;
                productHeaderTableGlobalNo = 8;
                PurchaseOrderDetail row6 = null;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String pID = "";
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    double rate = 0;
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    row6 = (PurchaseOrderDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row6.getDescription())){
                        prodDesc = row6.getDescription();
                    }else{
                        if (!StringUtil.isNullOrEmpty(row6.getProduct().getDescription())) {
                            prodDesc = row6.getProduct().getDescription();
                        }
                    }
                    prodName = row6.getProduct().getName();
                    quantity = row6.getQuantity();
                    rate = row6.getRate();
                    pID = row6.getProduct().getProductid();
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountOrder = (row6.getDiscountispercent() == 1)? rateInCurr*quantity *row6.getDiscount()/100 : row6.getDiscount();
                    uom = row6.getUom()==null?(row6.getProduct().getUnitOfMeasure()==null?"":row6.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row6.getUom().getNameEmptyforNA();

                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInBase = (Double) bAmt.getEntityList().get(0);
                    rate = rateInBase;
                    
                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name 
                    invcell = createCell(pID, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);
                    
                    //String qtyStr = Double.toString(quantity);
                   // if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                        String qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    //}
                    invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate*quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discountQuotation != 0){
                        amount1 -= discountQuotation;
                    }
                    if (discountOrder != 0){
                        amount1 -= discountOrder;
                        discountQuotation = discountOrder;
                    }
                    
                    if (row6!= null&&row6.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row6.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row6.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row6.getRowTaxAmount();
                        }
                    } 
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                
                } // END Of Product Details
                

                     //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                    term= ExportRecordHandler.appendTermDetailsPurchaseOrder(accPurchaseOrderobj,authHandlerDAOObj,row6.getPurchaseOrder(), table1, currencyid,mode, CompanyID);
                }
                total+=term;
                totalAmount+=term;
                if(mode==StaticValues.AUTONUM_PURCHASEORDER && pOrder.getDiscount() != 0){
                    if(!pOrder.isPerDiscount()){
                            discountTotalQuotation = pOrder.getDiscount();
                            total = total - pOrder.getDiscount();
                            totalAmount = total;
                    } else {
                            discountTotalQuotation = total * pOrder.getDiscount()/100;
                            total -= (total * pOrder.getDiscount()/100);
                            totalAmount = total;
                    }
                } 
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, discountTotalQuotation, currencyid, false, CompanyID);//new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                
                //int belowProdTableContent =295;
                 ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
               
                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                table2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * table2.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                 // get after Items table height
                bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                bottomTable.calculateHeightsFast();
                float bottomTableHeight = bottomTable.getTotalHeight();
                
                float table2Height = table2.getTotalHeight();
                int blankRowHeight = 4;

                float addBlankRows = (document.getPageSize().getHeight()*pageNo -summeryTableHeight- aboveProdTableContent -table2Height -bottomTableHeight -100*pageNo) / blankRowHeight;//80 =top+bottom=30+30 paddingTop =20
                int noOfCols = headerList.size();
                if(addBlankRows<0)
                    addBlankRows=10; 
               int BlankCellCnt = (int) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                int lastRow = BlankCellCnt - noOfCols;
                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);

                mainTable.addCell(mainCell12);
                
                
                mainTable.addCell(mainCell62);
                
            } else if (mode == StaticValues.AUTONUM_CREDITNOTE|| mode == StaticValues.AUTONUM_DEBITNOTE) {
                KWLCurrency currency = null;
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                isCompanyTemplateLogo = true;
                CreditNote creNote = null;
                DebitNote dbNote = null;
                
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                com  = null;
                double cndnTotalAmount = 0;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;
                double discountMain = 0;
                double subTotal = 0;
                
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                    creNote = (CreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=(creNote.getJournalEntry() != null)?creNote.getJournalEntry().getDetails():null;
                    customerObj = new Customer();
                    itr=(entryset!=null)?entryset.iterator():null;
                        if(itr!=null){
                        while(itr.hasNext()){
                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
                            customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
                            if(customerObj!=null)
                                    break;
                        }
                    }
                    com = creNote.getCompany();
                    invno = creNote.getCreditNoteNumber();
                    currency = creNote.getCurrency();
                    cndnTotalAmount=creNote.getCnamount();
                    entryDate = (creNote.isNormalCN())?creNote.getJournalEntry().getEntryDate():creNote.getCreationDate();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE ){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
                    com = dbNote.getCompany();
                    currency = dbNote.getCurrency();
                    Set<JournalEntryDetail> entryset=(dbNote.getJournalEntry()!=null)?dbNote.getJournalEntry().getDetails():null;
                    vendorObj=new Vendor();
                    itr=(entryset!=null)?entryset.iterator():null;
                    if(itr!=null){
                        while(itr.hasNext()){
                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
        //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                            vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
                            if(vendorObj!=null)
                                break;
                        }
                    }
                    invno = dbNote.getDebitNoteNumber();
                    entryDate = (dbNote.isNormalDN())?dbNote.getJournalEntry().getEntryDate():dbNote.getCreationDate();
                    cndnTotalAmount=dbNote.getDnamount();
                }
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                Date linkToDate = null;
                String faxNo = "";
                String PhoneNo = "";
                if(mode==StaticValues.AUTONUM_CREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail= customerObj!=null?customerObj.getEmail():"";
                    faxNo= customerObj.getFax();
                    PhoneNo = customerObj.getContactNumber();
                    if (creNote.getBillingShippingAddresses() != null) {
                        billAddress = CommonFunctions.getBillingShippingAddressWithAttn(creNote.getBillingShippingAddresses(), true);
                    } else {
                        billAddress = "";
                    }
                    if (creNote.getBillingShippingAddresses() != null) {
                            shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(creNote.getBillingShippingAddresses(), false);
                    } else {
                        shipAddress = "";
                    }
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", false);
//                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    terms=customerObj!=null?customerObj.getCreditTerm().getTermname():"";
                    billTo="Bill To";
                    addrParams.put("isBillingAddress", true);
//                    billAddress =accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    Iterator cloneItr = idresult.getEntityList().iterator();
                    if(cloneItr.hasNext()) {
                        CreditNoteDetail details = (CreditNoteDetail) cloneItr.next();
                        if(creNote.isOtherwise() && details.getPaidinvflag() != 1) {
                            linkTo = details.getInvoice()==null?"":details.getInvoice().getInvoiceNumber();    
                            linkToDate = details.getInvoice()==null?null:details.getInvoice().getJournalEntry().getEntryDate();
                        } else {
                            linkTo = details.getInvoiceRow()==null?"":details.getInvoiceRow().getInvoice().getInvoiceNumber();
                            linkToDate = details.getInvoiceRow()==null?null:details.getInvoiceRow().getInvoice().getJournalEntry().getEntryDate();
                        }
                    }
                    memo =creNote.getMemo();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail= vendorObj!=null?vendorObj.getEmail():"";
                    faxNo= vendorObj.getFax();
                    PhoneNo = vendorObj.getContactNumber();
                    terms=vendorObj!=null?vendorObj.getDebitTerm().getTermname():"";
                    billTo="Supplier";
                    addrParams.put("vendorid", vendorObj.getID());
                    addrParams.put("companyid", vendorObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", true);
                    if (creNote.getBillingShippingAddresses() != null) {
                            billAddress = CommonFunctions.getBillingShippingAddressWithAttn(dbNote.getBillingShippingAddresses(), true);
                    } else {
                        billAddress = "";
                    }
//                    billAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    filter_names.add("debitNote.ID");
                    filter_params.add(dbNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    Iterator cloneItr = idresult.getEntityList().iterator();
                    if(cloneItr.hasNext()) {
                        
                        DebitNoteDetail details = (DebitNoteDetail) cloneItr.next();
                        if(dbNote.isOtherwise()  && details.getPaidinvflag() != 1) {
                            linkTo = details.getGoodsReceipt()==null?"":details.getGoodsReceipt().getGoodsReceiptNumber();   
                            linkToDate = details.getGoodsReceipt()==null?null:details.getGoodsReceipt().getJournalEntry().getEntryDate();
                        } else {
                            linkTo = details.getGoodsReceiptRow()==null?"":details.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber();
                            linkToDate = details.getGoodsReceiptRow()==null?null:details.getGoodsReceiptRow().getGoodsReceipt().getJournalEntry().getEntryDate();
                        }
//                        if(details.getGoodsReceiptRow()!=null) {
//                            linkTo = details.getGoodsReceipt().getGoodsReceiptNumber();
//                            linkToDate = details.getGoodsReceipt().getJournalEntry().getEntryDate();
//                        }
                    }
                    memo = dbNote.getMemo();
                }

                isInclude=false;
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = mode == StaticValues.AUTONUM_CREDITNOTE ? "Customer Credit Note" : "Customer Debit Note";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and CN/DN details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, linkToDate, PhoneNo, faxNo, customerEmail, formatter ,mode );
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                headerList.add("Product Code");
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{6, 10, 40,12, 15, 17});
                productHeaderTableGlobalNo = 8;
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                int index = 0;
                CreditNoteDetail row = null;
                DebitNoteDetail row1 = null;
                BillingCreditNoteDetail row2 = null;
                BillingDebitNoteDetail row3 = null;  
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    double quantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String cndnName = "";
                    double cndnDiscount=0;
                    double rate = 0;
                    String pID = "";
                    if (mode == StaticValues.AUTONUM_CREDITNOTE ) {
                        row = (CreditNoteDetail) itr.next();
                        if(!otherwiseFlag){    
                            if (!StringUtil.isNullOrEmpty(row.getInvoiceRow().getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInvoiceRow().getInventory().getProduct().getDescription();
                            }
                            pID = row.getInvoiceRow().getInventory().getProduct().getProductid();
                            rate = row.getInvoiceRow().getRate();
                            prodName = row.getInvoiceRow().getInventory().getProduct().getName();
                            quantity =  row.getQuantity();
                            if(row.getDiscount()!=null){
                                if (row.getTotalDiscount() != null) {                                
                                    discountMain = discountMain + row.getTotalDiscount();
                                    total = total - row.getTotalDiscount();
                                }
                                discount = row.getDiscount();
                            } 
                            if(row.getTaxAmount()!=null){
                                taxMain = taxMain + row.getTaxAmount();
                                total = total + row.getTaxAmount();                        
                            }
                            try{
//                                uom = row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getName();
                                 uom = row.getInvoiceRow().getInventory().getUom()==null?(row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getInvoiceRow().getInventory().getUom().getNameEmptyforNA();
                            } catch(Exception ex){//In case of exception use uom="";
                            } 
                        }
                        else if(otherwiseFlag){
                            cndnName = row.getInvoice()!=null?row.getInvoice().getInvoiceNumber():"";
                            cndnDiscount=row.getDiscount()!=null?row.getDiscount().getDiscount():0.0;
                            cndnTotalOtherwiseAmount+=cndnDiscount;
                        }
                    }else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                        row1 = (DebitNoteDetail) itr.next();
                        if(!otherwiseFlag){   
                            if (!StringUtil.isNullOrEmpty(row1.getGoodsReceiptRow().getInventory().getProduct().getDescription())) {
                               prodDesc = row1.getGoodsReceiptRow().getInventory().getProduct().getDescription();
                           }
                           prodName = row1.getGoodsReceiptRow().getInventory().getProduct().getName();
                           rate = row1.getGoodsReceiptRow().getRate();
                           quantity =  row1.getQuantity();
                           pID = row1.getGoodsReceiptRow().getInventory().getProduct().getProductid();
                           if (row1.getDiscount() != null) {
                               if (row1.getTotalDiscount() != null) {
                                   discountMain = discountMain + row1.getTotalDiscount();
                                   total = total - row1.getTotalDiscount();
                               }   
                               discount = row1.getDiscount();
                           }
                           if (row1.getTaxAmount() != null) {
                               taxMain = taxMain + row1.getTaxAmount();
                               total = total + row1.getTaxAmount();
                           }
                           try{
//                               uom = row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getName();
                               uom = row1.getGoodsReceiptRow().getInventory().getUom()==null?(row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row1.getGoodsReceiptRow().getInventory().getUom().getNameEmptyforNA();
                           } catch(Exception ex){//In case of exception use uom="";
                           }
                       }
                        
                    }
                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell = createCell(pID, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);

                    invcell = createCell((double)quantity+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = createCell(String.valueOf(rate), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if(discount!=null){
                        invcell = createCell(authHandler.formattedAmount(discount.getDiscountValue(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);                        
                    }else{
                        invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    }
                    table.addCell(invcell);
                    ExportRecordHandler.addTableRow(mainTable, table);
                    table = ExportRecordHandler.getBlankTableReportForCreditNote();
                    if(discount!=null){
                       subTotal += discount.getDiscountValue();
                       total += discount.getDiscountValue();
                    } 

                } // END Of Product Details 
                 //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(subTotal, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountMain>0.0) {
                    table1.addCell(cell3);
                    invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, discountMain, currencyid, false, CompanyID);//new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountMain), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(taxMain);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)));
                }
                double term=0;
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    term = ExportRecordHandler.appendTermDetailsCreditNote(accCreditNoteDAOobj, authHandlerDAOObj, row.getCreditNote(), table1, currencyid, mode, CompanyID);
                }
                if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    term = ExportRecordHandler.appendTermDetailsDebitNote(accDebitNoteobj, authHandlerDAOObj, row1.getDebitNote(), table1, currencyid, mode, CompanyID);
                }
                cell3 = createCell("GST", local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total+term, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);

                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnet(table1, memo, false);
            //    receivedByTable1
                 String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(total+term)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
               
                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                   
                receivedByTable1.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin() //memo table
                        - document.rightMargin()) * receivedByTable1.getWidthPercentage() / 100);
                receivedByTable1.calculateHeightsFast();
                float receivedByTable1Height = receivedByTable1.getTotalHeight();   //signature table
                receivedByTable2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * receivedByTable2.getWidthPercentage() / 100);
                receivedByTable2.calculateHeightsFast();
                float receivedByTable2Height = receivedByTable2.getTotalHeight();
                 // get main table (having items and global fields) height  table2
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                table2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * table2.getWidthPercentage() / 100);
                table2.calculateHeightsFast();
                float table2Height = table2.getTotalHeight();
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - aboveProdTableContent -table2Height - summeryTableHeight -receivedByTable1Height -receivedByTable2Height - 110*pageNo) / blankRowHeight; //top+bottom=35+30+ 5+5 padding at end 10
                if(addBlankRows<0)
                    addBlankRows=10;
                int noOfCols = header.length; //No of Columns
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);


               
//                Discount totalDiscount = null;
//                double totaltax = 0, discountTotalQuotation = 0;
//                double totalAmount = 0;
//                double taxPercent = 0;
//                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
//                if(mainTax!=null){ //Get tax percent
//                    requestParams.put("transactiondate", entryDate);
//                    requestParams.put("taxid", mainTax.getID());
//                    KwlReturnObject result = accTaxObj.getTax(requestParams);
//                    List taxList = result.getEntityList();
//                    Object[] taxObj=(Object[]) taxList.get(0);
//                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
//                    mainTaxName=mainTax.getName();
//                }
//                totalAmount = total;
//                double term=0;
//                term= ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj,authHandlerDAOObj,sOrder , table, currencyid,mode);
//                total+=term;
//                totalAmount+=term;
//                if(sOrder.getDiscount() != 0) {
//                    if(!sOrder.isPerDiscount()){
//                            discountTotalQuotation = sOrder.getDiscount();
//                            total = total - sOrder.getDiscount();
//                            totalAmount = total;
//                    } else {
//                            discountTotalQuotation = total * sOrder.getDiscount()/100;
//                            total -= (total * sOrder.getDiscount()/100);
//                            totalAmount = total;
//                    }
//                }
//                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
//                totalAmount = total + totaltax;

                
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
                
             
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);
            } else if (mode == StaticValues.AUTONUM_INVOICE) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;
                InvoiceDetail row = null;
               
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                inv = (Invoice) cap.getEntityList().get(0);
                if (inv.getApprover() != null) {
                    approverName = inv.getApprover().getFirstName() + " " + inv.getApprover().getLastName();
                }
                currencyid = (inv.getCurrency() == null) ? currencyid : inv.getCurrency().getCurrencyID();
                if (inv.getTemplateid() != null) {
                    isCompanyLogo = false;
                     writer = PdfWriter.getInstance(document, baos);
                     writer.setPageEvent(new ExportRecord_VRNet.EndPage());
                     document.open();
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(inv.getCompany())) {
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }
                    String letterHead = inv.getTemplateid().getLetterHead();
                    String invoicePostText = inv.getPostText() == null ? "" : inv.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? inv.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)) ? inv.getTemplateid().getPreText() : CompanyPDFPRETEXT;
                    if (config.getBoolean("lHead")) {
                        if (!StringUtil.isNullOrEmpty(letterHead)) {
                            PdfPTable letterHeadTable = new PdfPTable(1);
                            ExportRecordHandler.getHtmlCell(letterHead, letterHeadTable, baseUrl);
                            letterHeadTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                            document.add(letterHeadTable);
                        }
                    }
                }
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);

                com = inv.getCompany();
                cEntry = inv.getCustomerEntry().getAccount();
                invno = inv.getInvoiceNumber();
                entryDate = inv.getJournalEntry().getEntryDate();
                dueDate = inv.getDueDate();
                shipDate = inv.getShipDate();
                shipvia = inv.getShipvia();
                fob = inv.getFob();
                poRefno = inv.getPoRefNumber() == null ? "" : inv.getPoRefNumber();
                //inv = (Invoice) session.get(Invoice.class, billid);
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = cEntry == cash ? messageSource.getMessage("acc.accPref.autoCS", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request));
                datetheader = theader;

                if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE) //Invoice header Label as Tax Invoice
                {
                    theader = "Tax Invoice";
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE);
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});
                if (addShipTo) {
                    tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                }
                if (isCompanyTemplateLogo || storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if (addShipTo) {
                        if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                            table1.setWidths(new float[]{65, 35});
                        }
                        tab2 = ExportRecordHandler.getDateTable2(entryDate, invno, datetheader, formatter, invCell);
                    }
                }
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
//                mainCell11.setPaddingBottom(5);
//                cell1.setPaddingTop(140);
                mainTable.addCell(mainCell11);


                PdfPCell mainCell12 = new PdfPCell(userTable2);

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                if (!StringUtil.isNullOrEmpty(preText)) {
                    ExportRecordHandler.getHtmlCell(preText.trim(), mainTable, baseUrl);
                }
                //}
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    filter_names.add("invoice.ID");
                    filter_params.add(inv.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                    customerName = inv.getCustomer() == null ? inv.getCustomerEntry().getAccount().getName() : inv.getCustomer().getName();
                    billAddress = CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(), true);
                    if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in invoice table 
                       billAddress = inv.getBillTo() != null ? inv.getBillTo() : "";
                    }
                            
//                    customerEmail = inv.getCustomer() != null ? inv.getCustomer().getEmail() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    if (inv.getMasterSalesPerson() != null) {   //if User Class returns the Null Valu
                        salesPerson = inv.getMasterSalesPerson() != null ? inv.getMasterSalesPerson().getValue() : "";
                    } else {  //if salesperson class has no username
                        salesPerson = "";
                    }
                    if (pref.isWithInvUpdate()) {
                        linkHeader = "SO/DO/CQ. No.";
                    } else {
                        linkHeader = "SO/CQ. No.";
                    }
                    terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
                    int termdays = (int)( (dueDate.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24));
                    HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
                    termrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    termrequestParams.put("termdays", termdays);

                    KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
                    List<Term> termlist = termresult.getEntityList();
                    if(termlist.size()>0) {
                        terms = termlist.get(0).getTermname();
                    }
                    shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in invoice table 
                        shipAddress=inv.getShipTo()==null?"":inv.getShipTo();
                    }
                    itr = idresult.getEntityList().iterator();
                    itr1 = idresult.getEntityList().iterator();
                    memo = inv.getMemo();
                    porefno = inv.getPoRefNumber();
                }
                linkTo = "";
                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderFromInvoice(inv.getID(),pref.getCompany().getCompanyID());
                List list = doresult.getEntityList();
                if(list.size()>0){
                    Iterator ite1 = list.iterator();                
                    while(ite1.hasNext()){                        
                        String donumber = (String)ite1.next();
                        linkTo += donumber + ",";
                    }
                    if(linkTo.length()>0)  {
                        linkTo = linkTo.substring(0, linkTo.length()-1);
                    }
                }
                
                if (mode != StaticValues.AUTONUM_RFQ) {
                    PdfPTable addressMainTable = null;
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        addressMainTable = ExportRecordHandler.getAddressTableForBCHL(accPurchaseOrderobj, customerName, billAddress, "", pOrder, currencyid);
                    } else {
                        addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, "", billTo, shipAddress, addShipTo);
                    }
                    if (!addShipTo) {
                        PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj, salesPerson, entryDate, invno,linkTo, porefno, terms, currencyid, formatter);
                        PdfPCell cel3 = new PdfPCell(shipToTable);
                        cel3.setBorder(0);
                        addressMainTable.addCell(cel3);
                    }

                    mainCell12 = new PdfPCell(addressMainTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(1);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);

                    String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE) {
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTableForVRNET(headerDetailsForVrnet, referenceNumber, terms, dueDate, formatter, salesPerson);
                        mainCell12 = new PdfPCell(detailsTable);
                        mainCell12.setBorder(0);
//                        mainCell12.setPaddingTop(0);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                        mainCell12.setPaddingBottom(5);
                        if (addShipTo) {
                            mainTable.addCell(mainCell12);
                        }
                    } else {
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, referenceNumber, terms, dueDate, shipDate, formatter, isInclude, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                        mainCell12.setPaddingBottom(5);
                        mainTable.addCell(mainCell12);
                    }
                    
                }
                mainTable.setSplitLate(false); 
//                mainTable.setExtendLastRow(true);
                boolean companyFlag = (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_SALESORDER));
                List<String> headerList = new ArrayList<String>();
                if (addShipTo) {
                    headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
                    if (!linkHeader.equalsIgnoreCase("")) {
                        headerList.add(linkHeader);
                    }
                } else {
                    headerList.add("Item");
                }
                //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name in header               
                if (!(companyFlag)) {
                    headerList.add(messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)));
                }

                 //  get product custom fields 
                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(com.getCompanyID(), Constants.Acc_Product_Master_ModuleId, 0));
                fieldrequestParams.put("relatedmoduleid", String.valueOf(StaticValues.AUTONUM_INVOICE));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                KwlReturnObject kwlcustomColumn = accAccountDAOobj.getFieldParams(fieldrequestParams);
                List<FieldParams> customColList = kwlcustomColumn.getEntityList();
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                HashMap<String, Integer> customMap = new HashMap();
                List<String> customHeaderList = new ArrayList();
                for (FieldParams field : customColList) {
                    //headerList.add(field.getFieldlabel());
                   customHeaderList.add(field.getFieldlabel());
//                   String[] relatedTo = field.getRelatedmoduleid().split(",");
//                    String[] relatedToWidth = field.getRelatedmodulepdfwidth()!=null ? field.getRelatedmodulepdfwidth().split(",") : "".split(",");
//                    for(int customCnt=0; customCnt<relatedTo.length; customCnt++) {
//                        if(relatedTo[customCnt].equals(String.valueOf(StaticValues.AUTONUM_INVOICE))) {
//                            if(relatedToWidth.length > customCnt && !StringUtil.isNullOrEmpty(relatedToWidth[customCnt])) {
//                                customMap.put(field.getFieldlabel(), Integer.parseInt(relatedToWidth[customCnt]));
//                            } else {
//                                customMap.put(field.getFieldlabel(), 0);
//                            }
//                        }
//                    }
               }
                
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                if (mode != StaticValues.AUTONUM_RFQ) {
                    headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                    headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                }

                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                PdfPTable table = null;
//                if (mode == StaticValues.AUTONUM_RFQ) {
//                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    productHeaderTableGlobalNo = 1;
//                } else {
//                    table = ExportRecordHandler.getTable(linkHeader, true);
//                    productHeaderTableGlobalNo = 2;
//                }
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                    productHeaderTableGlobalNo = 6;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }
                
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
//                if (mode == StaticValues.AUTONUM_RFQ) {
//                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                } else {
//                    table = ExportRecordHandler.getTable(linkHeader, true);
//                }

                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }
//                table.setExtendLastRow(true);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.companyKey, CompanyID);
                requestParams.put(Constants.globalCurrencyKey, currencyid);
                requestParams.put(Constants.df, formatter);
                
                
                BillingInvoiceDetail row1 = null;
                BillingSalesOrderDetail row3 = null;
                BillingPurchaseOrderDetail row4 = null;
                SalesOrderDetail row5 = null;
                PurchaseOrderDetail row6 = null;
                QuotationDetail row7 = null;
                VendorQuotationDetail row8 = null;
                RequestForQuotationDetail row9 = null;
                int index = 0;
                
              while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                    Phrase phrase1 = new Phrase();
                    Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
//                    String linkTo = "-";
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            prodDesc = row.getDescription();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInventory().getProduct().getDescription();
                            }
                        }
                        prodName = row.getInventory().getProduct().getName();
                        if (!addShipTo) {
                            if (row.getDeliveryOrderDetail() != null && !StringUtil.isNullOrEmpty(row.getDeliveryOrderDetail().getPartno().trim())) {
                                String partno = row.getDeliveryOrderDetail().getPartno();
//                            prodDesc += "\n\n Part No. :";
                                partNo = partno;
                            }
                        }
                        quantity = row.getInventory().getQuantity();
                        rate = row.getRate();
                        discount = row.getDiscount();
//                        uom = row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getName();
                        uom = row.getInventory().getUom()==null?row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA():row.getInventory().getUom().getNameEmptyforNA();
                        if (row.getDeliveryOrderDetail() != null) {
                            linkTo = row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                        } else if (row.getSalesorderdetail() != null) {
                            linkTo = row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber();
                        } else if (row.getQuotationDetail() != null) {
                            linkTo = row.getQuotationDetail().getQuotation().getquotationNumber();
                        }
                    }

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (addShipTo) {
                        if (!linkHeader.equalsIgnoreCase("")) {
                            invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                        }
                    }
                    //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name 
                    if (!(companyFlag)) {
                        invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    }
                    table.setSplitLate(false); 
                    AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), row.getInventory().getProduct().getID());
                    if (obj != null) {
                        productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        for (String varEntry : customHeaderList) {
                            boolean isBlankVal = true;
                            if(variableMap.containsKey("Custom_"+varEntry)) {
                                String coldata = variableMap.get("Custom_"+varEntry).toString();
                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                     invcell = createCell(prodDesc + "\n\n Part No. :" +coldata, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                      invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc + "<br/> Part No. :" +coldata,baseUrl));
                                      table.addCell(invcell); // if product have part no
                                    isBlankVal = false;
                                } 
                            }
                            if(isBlankVal){ // product have empty part no
                                 invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                                     table.addCell(invcell);
                            }
                        }
                    } else {//if product have no custom fild part no
//                        for (String varEntry : customHeaderList) {
                          // invcell = createCell(prodDesc , fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                           invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                           table.addCell(invcell);
//                        }
                    }
//                    if (!addShipTo) {
//                        invcell = createCell(partNo, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
//                    }

                    //String qtyStr = Double.toString(quantity);
                    // if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    //}
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                            bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                            double rateInBase = (Double) bAmt.getEntityList().get(0);
                            rate = rateInBase;
                        }
                        invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

                        amount1 = rate * quantity;

                        invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

                        if (discount != null) {
                            amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (row.getDiscount().getDiscountValue()) : (row1.getDiscount().getDiscountValue());
                        }
                        if (discountQuotation != 0) {
                            amount1 -= discountQuotation;
                        }
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName = "";
                        if (row != null && row.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        if (discount != null || discountQuotation != 0) {  //For Discount Row
                            table = ExportRecordHandler.getDiscountRowTableForVRNet(authHandlerDAOObj, table, currencyid, discount, 
                                    discountQuotation, mode, linkHeader, customMap.size(), CompanyID);
                        }

                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                            table = ExportRecordHandler.getTaxRowTableForVRnet(authHandlerDAOObj, table, rowTaxName, currencyid, amount1, 
                                    rowTaxPercent, mode, linkHeader, customMap.size(),rowTaxAmount);
                        }
                        amount1 += rowTaxAmount;//amount1 += amount1 * rowTaxPercent / 100;
                        total += amount1;
                        
                    }     
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (companyFlag) {
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);   //create table with 6 columns for extra row and Bottom 
                        } else {
                            table = ExportRecordHandler.getBlankTableReportForNonINVOICE(); //create table with 5 columns for extra row and Bottom
                          }
                    }   
                    float RowCellCnt = (float) (5 * headerList.size());
                        RowCellCnt = RowCellCnt - (RowCellCnt % (headerList.size()));
                     for (int j = 1; j <=RowCellCnt ; j++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(invcell);
                    }                  
                    ExportRecordHandler.addTableRow(mainTable, table);  
//                    if (mode == StaticValues.AUTONUM_RFQ) {
//                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    } else {
//                        table = ExportRecordHandler.getTable(linkHeader, true);
//                    }
                    ExportRecordHandler.addTableRow(mainTable, table);  
//                    if (mode == StaticValues.AUTONUM_RFQ) {
//                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    } else {
//                        table = ExportRecordHandler.getTable(linkHeader, true);
//                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    if (companyFlag) {
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);   //create table with 6 columns for extra row and Bottom 
                        } else {
                            table = ExportRecordHandler.getBlankTableReportForNonINVOICE(); //create table with 5 columns for extra row and Bottom
                          }
                    }                   
                }

                    Discount totalDiscount = null;
                    double totaltax = 0, discountTotalQuotation = 0;
                    double totalAmount = 0;
                    double taxPercent = 0;
                    String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
                    boolean isZero = true;
		    if (mode != StaticValues.AUTONUM_RFQ) {
			if (mainTax != null) { //Get tax percent
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", mainTax.getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            mainTaxName = mainTax.getName();
                        }
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            ExportRecordHandler.appendTermDetails(accInvoiceDAOobj, authHandlerDAOObj, inv, table, currencyid, mode, CompanyID);
                            totalDiscount = inv.getDiscount();
                            totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : 0;
                            mainTaxName = inv.getTax() != null ? inv.getTax().getName() : "";
                            totalAmount = inv.getCustomerEntry().getAmount();
                        }

                        if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discountTotalQuotation, currencyid, CompanyID);
                            if(discountTotalQuotation>0.0)
                                isZero = false;
                        } else {
                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, totalDiscount, currencyid, CompanyID);
                            if(totalDiscount!=null && (totalDiscount.getDiscountValue()>0.0))
                                isZero = false;
                        } 

	            }
                      //table = getBlankTable();
                    table1 = new PdfPTable(2);
                    table1.setWidthPercentage(100);
                    table1.setWidths(new float[]{50, 50});
                     PdfPTable summeryTable=null; 
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        //cell3.setColspan(6);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        
                        if (mode != StaticValues.AUTONUM_PURCHASEORDER || mode != StaticValues.AUTONUM_SALESORDER) {
                            cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                            //cell3.setColspan(6);
                            if(!isZero) {
                                table1.addCell(cell3);
                                invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, totalDiscount, currencyid, false, CompanyID);
                                invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                invcell.setBorder(Rectangle.BOX);
                                invcell.setPadding(5);
                                table1.addCell(invcell);
                            }
                        }
                        StringBuffer taxNameStr = new StringBuffer();

                        taxNameStr.append(mainTaxName);
                        
                        if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                            taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)));
                        }
                        cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        //cell3.setColspan(6);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);

                        // addTableRow(mainTable, table);

                      summeryTable = ExportRecordHandler.getSummeryTableForVRnet(table1, mainTaxName, addShipTo);

                        mainCell12 = new PdfPCell(summeryTable);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                       

                    }
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                    if (companyFlag) {
                          // get after Items table height
                        bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                        bottomTable.calculateHeightsFast();
                        float bottomTableHeight = bottomTable.getTotalHeight();
                        // get main table (having items and global fields) height 
                        summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                        summeryTable.calculateHeightsFast();
                        float summeryTableHeight = summeryTable.getTotalHeight();
                        // get main table (having items and global fields) height 
                        mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                        mainTable.calculateHeightsFast();
                        float aboveProdTableContent = mainTable.getTotalHeight();

                        int blankRowHeight = 4;
                        float addBlankRows = (document.getPageSize().getHeight()*pageNo - bottomTableHeight - aboveProdTableContent - summeryTableHeight - 85*pageNo) / blankRowHeight; //top+bottom=5+30+5 padding at end
                        int noOfCols = headerList.size();
                        if(addBlankRows<0)
                             addBlankRows=10; 
                        float BlankCellCnt = (float) (addBlankRows * noOfCols);
                        BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                        float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                        for (int j = 1; j <= BlankCellCnt; j++) {
                            invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            if (j > lastRow) {
                                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                            }
                            tableCloseLine.addCell(invcell);
                        }

                    }
                    
                   
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                   
                    ExportRecordHandler.addTableRow(mainTable, tableCloseLine);
                     document.add(mainTable);
                    //Break table after adding extra space
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                   
                    

                    if (mode != StaticValues.AUTONUM_RFQ) {
                        mainTable.addCell(mainCell12);

                    }
            } else if (mode == StaticValues.AUTONUM_GOODSRECEIPT|| mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document,writer);                
                GoodsReceipt gr=null;
                BillingGoodsReceipt gr1=null;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
                       if(gr.getApprover()!=null)
                        approverName=gr.getApprover().getFirstName() +" "+gr.getApprover().getLastName();
                    if (gr.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(gr.getTemplateid().getConfigstr());
                        // document = getTemplateConfig(document,writer);
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(gr.getCompany())) {
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                    }
                }
            
                com =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCompany():gr1.getCompany();
                company = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }
                
                KWLCurrency rowCurrency = (mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCurrency():gr1.getCurrency());
                String rowCurrenctID = rowCurrency==null?currencyid:rowCurrency.getCurrencyID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                 if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{74, 26});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref=(CompanyAccountPreferences)kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),com.getCompanyID());
                Account cash=pref.getCashAccount();
                Account vEntry;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vEntry = gr.getVendorEntry().getAccount();
                } else {
                    vEntry = gr1.getVendorEntry().getAccount();
                }
                String theader = vEntry==cash?messageSource.getMessage("acc.accPref.autoCP", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.accPref.autoVI", null, RequestContextUtils.getLocale(request));
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);
                String grno=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getGoodsReceiptNumber():gr1.getBillingGoodsReceiptNumber();
                entryDate=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getJournalEntry().getEntryDate():gr1.getJournalEntry().getEntryDate();
                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,grno,theader,formatter);   
                if (isCompanyTemplateLogo) {
                    tab2= ExportRecordHandler.getDateTable2(entryDate,grno,theader,formatter,invCell);   

                }
                
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
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

                cell1 = new PdfPCell(tab3);
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
                if (!isCompanyTemplateLogo && !(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                mainTable.addCell(mainCell12);
                }

                String vendorName = "";
                String vendorEmail = "";
                String vendorTerms = "";
                billTo="Supplier";
                String Address="";
                isInclude=false; //Hiding or Showing P.O. NO field in single PDF 
                String linkHeader="";
                
                headerDetails = new String []{"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                if(mode==StaticValues.AUTONUM_GOODSRECEIPT){
                    vendorName = gr.getVendor()==null?gr.getVendorEntry().getAccount().getName():gr.getVendor().getName();
                    vendorEmail= gr.getVendor()==null?"":gr.getVendor().getEmail();
                    vendorTerms=gr.getVendor()==null?"":gr.getVendor().getDebitTerm().getTermname();
                    if(pref.isWithInvUpdate()){
                        linkHeader="PO/GR/VQ No.";
                        isInclude=false;
                    }else{
                        linkHeader="PO/VQ. No.";
                        isInclude=false;
                    }                    
                    Address= CommonFunctions.getBillingShippingAddressWithAttn(gr.getBillingShippingAddresses(), true);
                    dueDate=gr.getDueDate();
                    shipDate=gr.getShipDate();        
                    shipvia=gr.getShipvia();        
                    fob=gr.getFob();        
                }
                
                HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                grRequestParams.put("order_by", order_by);
                grRequestParams.put("order_type", order_type);

                idresult = null;
                if(mode != StaticValues.AUTONUM_BILLINGINVOICE||mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                    if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                        filter_names.add("billingGoodsReceipt.ID");
                        filter_params.add(gr1.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                    }
                    else{
                        filter_names.add("goodsReceipt.ID");
                        filter_params.add(gr.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        if(isExpenseInv){
                             idresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                             isInclude=false;
                        }
                        else
                            idresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
                    }
                } else {
                    filter_names.add("billingGoodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                }
                itr = idresult.getEntityList().iterator();  
                
                if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                
                PdfPTable addressMainTable=ExportRecordHandler.getAddressTable(vendorName,Address,vendorEmail,billTo,"",true);

                mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
                
                
                String referenceNumber="";
                if(!isExpenseInv){
                    referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);                    
                }                
                PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,referenceNumber,vendorTerms,dueDate,shipDate,formatter,isInclude, shipvia, fob);
                
                mainCell12 = new PdfPCell(detailsTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                
                PdfPTable table;
                PdfPCell grcell = null;
                if(isExpenseInv){
                    String[] header  = {"S.No.","Account",  "PRICE "+authHandlerDAOObj.getCurrency(rowCurrenctID), "LINE TOTAL "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                     table = new PdfPTable(4);
                    globalTableHeader=header;
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{10, 40, 25, 25});
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                }
                else{

                    String[] header;
                    if(linkHeader.equalsIgnoreCase("")){
                        header=new String[]{messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                    }else{
                        header=new String[]{messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),linkHeader,messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                    }                                        
                    table=ExportRecordHandler.getTable(linkHeader,true);
                    globalTableHeader=header;
                     productHeaderTableGlobalNo=2;
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }                   
                }
                
                GoodsReceiptDetail row=null;
                BillingGoodsReceiptDetail row1=null;
                ExpenseGRDetail exprow=null;
                int index = 0;
                while (itr.hasNext()) {
                    if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                        if(isExpenseInv){
                             exprow = (ExpenseGRDetail) itr.next();
                        }     
                        else{
                            row = (GoodsReceiptDetail) itr.next();
                            if (row.getGoodsReceiptOrderDetails() != null) {
                                linkTo = row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber();
                            } else if (row.getPurchaseorderdetail() != null) {
                                linkTo = row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber();
                            } else if (row.getVendorQuotationDetail() != null) {
                                linkTo = row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber();
                            }
                        }
                    }else {
                        row1 = (BillingGoodsReceiptDetail) itr.next();
                        if (row1.getPurchaseOrderDetail() != null) {
                            linkTo = row1.getPurchaseOrderDetail().getPurchaseOrder().getPurchaseOrderNumber();
                        }
                     }
                    
                    if(isExpenseInv){
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount(exprow.getRate(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);                        
                        double amount1 = exprow.getRate();
                        grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {   //For Discount Row
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("Discount", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , exprow.getDiscount(), rowCurrenctID, CompanyID);
                            grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            grcell.setPadding(5);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                            
                            amount1 -= exprow.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        boolean isRowTaxApplicable=false;
                        double rowTaxAmount=0;
                        String rowTaxName="";
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (exprow!= null&&exprow.getTax() != null) { 
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=exprow.getTax().getName();
                            if (exprow.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = exprow.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell(rowTaxName + "  Tax", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

//                            invcell = createCell(ExportRecordHandler.getFormattedAmount(amount1 * rowTaxPercent / 100), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            invcell = createCell(authHandler.formattedAmount(rowTaxAmount, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);        
                        }
                        amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                        total += amount1;

                    }else{
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        if(!linkHeader.equalsIgnoreCase("")){
                            grcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);                        
                        }
                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                            //    grcell = ExportRecordHandler.getProductNameWithDescriptionPhrase(row.getInventory().getProduct());
                            grcell = createCell(row.getInventory().getProduct().getName(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            if(!StringUtil.isNullOrEmpty(row.getDescription())){
                                grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            }else if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                grcell = createCell(row.getInventory().getProduct().getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            } else {
                                grcell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            }                            
                            table.addCell(grcell);

                        } else {
                            grcell = createCell(row1.getProductDetail(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            grcell = createCell("-", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                        }
//                        grcell = createCell(mode == StaticValues.AUTONUM_GOODSRECEIPT ? getProductNameWithDescription(row.getInventory().getProduct()) : row1.getProductDetail(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        //table.addCell(grcell);
                        String qtyString="";
                        if(mode == StaticValues.AUTONUM_GOODSRECEIPT){
                            qtyString=Double.toString(row.getInventory().getQuantity());
                        }else if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                                qtyString=Double.toString((Double)row1.getQuantity());                                
                        }                       
                        grcell = createCell(qtyString+ " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getUom()==null?(row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getInventory().getUom().getNameEmptyforNA()) : ""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount((mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate()), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
//                        grcell = calculateDiscount(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount(), rowCurrenctID);
//                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                        grcell.setPadding(5);
//                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        table.addCell(grcell);
                        double amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * ( row.getInventory().getQuantity()) : row1.getRate() * row1.getQuantity();
                        grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        Discount disc = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount();
                        if (disc != null) {                              
                            table=ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table,rowCurrenctID,disc,0,mode,linkHeader); //For Discount Row                   
                            amount1 -= mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        double rowTaxAmount=0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName="";
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (row!= null&&row.getTax() != null) {
                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1*rowTaxPercent/100;
                            }

                        }
                        else if (row1!= null&&row1.getTax() != null) {
                            requestParams.put("transactiondate", row1.getBillingGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row1.getTax().getName();
                            if (row1.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1*rowTaxPercent/100;
                            }
                        }
                         if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
                            table=ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,rowCurrenctID,amount1,rowTaxPercent,mode,linkHeader,rowTaxAmount);
                        }
                         
                         ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                        if (isExpenseInv) {
                            table = new PdfPTable(4);
                            table.setWidthPercentage(100);
                            table.setWidths(new float[]{10, 40, 25, 25});

                        } else {
                            table = ExportRecordHandler.getTable(linkHeader, true);
                        }
                         
                        amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                        total += amount1;                         
                    }
                }
                int cellCount = 0, lastRowCellCount = 0;
                if (isExpenseInv) {
                    cellCount = 56;lastRowCellCount = 52;
                    for (int j = 1; j <=cellCount; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        if (j > lastRowCellCount) {
//                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
//                        }
                        table.addCell(grcell);
                    }
                } else {
                    cellCount = (linkHeader.equalsIgnoreCase(""))?84:98;
                    lastRowCellCount = linkHeader.equalsIgnoreCase("")?78:91;
                    for (int j = 1; j <=cellCount; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        if (j > lastRowCellCount) {
//                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
//                        }
                        table.addCell(grcell); 
                    }
                }

                if (true) {
                    int belowProdTableContent = 270;
                    mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                    mainTable.calculateHeightsFast();
                    float aboveProdTableContent = mainTable.getTotalHeight();
                    table.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * table.getWidthPercentage() / 100);
                    table.calculateHeightsFast();
                    int blankRowHeight = 4;
                    float addBlankRows = (document.getPageSize().getHeight() - belowProdTableContent - aboveProdTableContent - table.getTotalHeight()) / blankRowHeight;
                    int noOfCols = 7;
                    if(addBlankRows<0)
                        addBlankRows=10; 
                    int BlankCellCnt = (int) (addBlankRows * noOfCols);
                    BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                    int lastRow = BlankCellCnt - noOfCols;
                    for (int j = 1; j <= BlankCellCnt; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > lastRow) {
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(grcell);
                    }
                }
                ExportRecordHandler.addTableRow(mainTable, table);
                
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                
                table = new PdfPTable(2);
                table.setWidthPercentage(100);                
                table.setWidths(new float[]{50,50});
               
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);                
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode, CompanyID);
                }
                boolean isDiscZero = mode==StaticValues.AUTONUM_GOODSRECEIPT? (gr.getDiscount()!=null && gr.getDiscount().getDiscountValue()>0 ? false : true) : (gr1.getDiscount()!=null && gr1.getDiscount().getDiscountValue()>0 ? false : true);
                if(!isDiscZero) {
                    cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getDiscount():gr1.getDiscount(), rowCurrenctID, CompanyID);
                    grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    grcell.setBorder(Rectangle.BOX);
                    grcell.setPadding(5);
                    table.addCell(grcell); 
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTaxEntry().getAmount():0):(gr1.getTaxEntry()!=null?gr1.getTaxEntry().getAmount():0),rowCurrenctID, CompanyID), fontSmallRegular,Element.ALIGN_RIGHT, Rectangle.BOX,5);
                table.addCell(cell3);                
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold,Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getVendorEntry().getAmount()):(gr1.getVendorEntry().getAmount()), rowCurrenctID, CompanyID), fontSmallRegular,Element.ALIGN_RIGHT, Rectangle.BOX,5);
                table.addCell(cell3);

                String mainTaxName=mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTax().getName():""):(gr1.getTaxEntry()!=null?gr1.getTax().getName():"");
                PdfPTable summeryTable=ExportRecordHandler.getSummeryTable(table,mainTaxName,addShipTo);

                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);                
                mainTable.addCell(mainCell12);
                
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                double totalamount=0;
                if(gr!=null)
                    totalamount=gr.getVendorEntry().getAmount();
                else if(gr1!=null)
                    totalamount=gr1.getVendorEntry().getAmount();
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(2);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);

            }
            
//            ExportRecordHandler.getHtmlCell("<br/>", mainTable, baseUrl);
            if(mode==StaticValues.AUTONUM_QUOTATION|| mode ==StaticValues.AUTONUM_SALESORDER ) {
                PdfPCell thanksGivingTableCell = new PdfPCell(thanksGivingTable);
                thanksGivingTableCell.setBorder(0);
                mainTable.addCell(thanksGivingTableCell);
            } else if(mode==StaticValues.AUTONUM_DEBITNOTE|| mode ==StaticValues.AUTONUM_CREDITNOTE ) {
                PdfPCell receivedByTableCell1 = new PdfPCell(receivedByTable1);
                receivedByTableCell1.setBorder(0);
                receivedByTableCell1.setPadding(10); 
                mainTable.addCell(receivedByTableCell1);
                 PdfPCell receivedByTableCell2= new PdfPCell(receivedByTable2);
                receivedByTableCell2.setBorder(0);
                mainTable.addCell(receivedByTableCell2);
             }else  if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT 
                    && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT 
                    && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT 
                    && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE 
                    && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    mainTable.addCell(bottomCell);
            }

            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Export:" + ex.getMessage(), ex);
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
     
    public ByteArrayOutputStream createVRNetPdf(JSONObject jsonObj, String currencyid, String billid, DateFormat formatter, 
            int mode, double amount, String logoPath, String customer, String accname, String address, boolean isExpenseInv, 
            String CompanyID, String userId,String baseCurrency) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        modeNo=mode;
        double cndnTotalOtherwiseAmount = 0;
        Document document = null;
        PdfWriter writer = null;
        boolean otherwiseFlag = false;
        if (!StringUtil.isNullOrEmpty(jsonObj.optString("otherwise"))) {
            otherwiseFlag = Boolean.parseBoolean(jsonObj.optString("otherwise"));
        }
//        Font local_fontSmallBold = FontFactory.getFont("Helvetica", 9, Font.BOLD, Color.BLACK);
        try {
            
            String poRefno = "";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 40, 30);
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ExportRecord_VRNet.EndPage());
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = null;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            KwlReturnObject bAmt = null;
            boolean isCompanyLogo = true;
            boolean addShipTo = true;
            boolean isCompanyTemplateLogo = false;
            PdfPTable tab3 = null;
            String approverName = "______________________________";
            String path = com.krawler.common.util.URLUtil.getDomainURL(jsonObj.optString(Constants.COMPANY_PARAM), false);
            jsonObj.put("path", path);
            jsonObj.put("servPath", jsonObj.optString(Constants.SERVLET_PATH));
            Map<String,Object> paramMap=new HashMap<String,Object>();
            Iterator<String> keys = jsonObj.keys();
             while (keys.hasNext()) {
                            String key = keys.next();
                            String value = jsonObj.getString(key);
                            paramMap.put(key,value);
                   }
            String baseUrl = URLUtil.getPageURLJson(paramMap, loginpageFull);
            Rectangle page = document.getPageSize();
            Company com = null;
            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();
            String companyIdForCAP = CompanyID;
            CompanyAccountPreferences preferences=(CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),companyIdForCAP);
            int moduleid = -1;
            moduleid = ExportRecordHandler.getModuleId(mode, billid, CompanyID, kwlCommonTablesDAOObj);
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(CompanyID, moduleid);
            List<PdfTemplateConfig> textList = templateConfig.getEntityList();
            if (textList.isEmpty()) {
                CompanyPDFFooter = preferences.getPdffooter()==null?"":preferences.getPdffooter();
                CompanyPDFHeader = preferences.getPdfheader()==null?"":preferences.getPdfheader();
                CompanyPDFPRETEXT = preferences.getPdfpretext()==null?"":preferences.getPdfpretext();
                CompanyPDFPOSTTEXT = preferences.getPdfposttext()==null?"":preferences.getPdfposttext();
            } else {
                for (PdfTemplateConfig config : textList) {
                    CompanyPDFFooter = config.getPdfFooter() == null ? (preferences.getPdffooter()==null?"":preferences.getPdffooter()): config.getPdfFooter();
                    CompanyPDFHeader = config.getPdfHeader() == null ? (preferences.getPdfheader()==null?"":preferences.getPdfheader()): config.getPdfHeader();
                    CompanyPDFPRETEXT = config.getPdfPreText() == null ? (preferences.getPdfpretext()==null?"":preferences.getPdfpretext()) : config.getPdfPreText();
                    CompanyPDFPOSTTEXT = config.getPdfPostText() == null ? (preferences.getPdfposttext()==null?"":preferences.getPdfposttext()) : config.getPdfPostText();
                }
            }
            String preText = StringUtil.isNullOrEmpty(CompanyPDFPRETEXT) ? "" : CompanyPDFPRETEXT;
            String postText = StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) ? "" : CompanyPDFPOSTTEXT;

            Projreport_Template defaultTemplate = (Projreport_Template) kwlCommonTablesDAOObj.getClassObject(Projreport_Template.class.getName(), Constants.HEADER_IMAGE_TEMPLATE_ID);
            if (defaultTemplate != null) {
                config = new com.krawler.utils.json.base.JSONObject(defaultTemplate.getConfigstr());
            }
            String linkTo = "";
            KwlReturnObject idresult = null;
            String customerName = "";
            String customerEmail = "";
            String terms = "";
            String billTo = "";
            String shipAddress = "";
            String memo = "";
            String porefno = "";
            String billAddress = "";
            String salesPerson = null;
            boolean isInclude = false; //Hiding or Showing P.O. NO field in single PDF 
            Iterator itr = null;
            Iterator itr1 = null;
            linkHeader = "";
            String invno = "";
            Date dueDate = null;
            Date shipDate = null;
            String shipvia = null;
            String fob = null;
            Date entryDate = null;
            BillingPurchaseOrder po = null;
            SalesOrder sOrder = null;
            PurchaseOrder pOrder = null;
            Tax mainTax = null;
            Quotation quotation = null;
            VendorQuotation venquotation = null;
            RequestForQuotation RFQ = null;
            String company[] = new String[4];
            com  = null;
            Account cEntry = null;
            String datetheader = "";
            String[] headerDetails = {"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
            String[] headerDetailsForVrnet = {"Terms", "Due Date", "Sales Person"};//Header name for VRNET Comapny 
            HashMap<String,Object> addrParams=new HashMap<String, Object>();
            addrParams.put("isDefaultAddress", true);
            PdfPCell bottomCell=null;
            PdfPTable bottomTable=null;
            PdfPTable thanksGivingTable=null;
            PdfPTable receivedByTable1=null;
            PdfPTable receivedByTable2=null;
            if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT 
                    && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT 
                    && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT 
                    && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE 
                    && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE){
                bottomTable = new PdfPTable(2); //for 2 column
                bottomTable.setWidthPercentage(100);
                bottomTable.setWidths(new float[]{60, 40});
                PdfPTable postText1 = new PdfPTable(1);
                PdfPCell right = ExportRecordHandler.getVRNetHtmlCell(postText==null?"":postText.trim(), mainTable, baseUrl);
                right.setPaddingTop(35);
                if(mode != StaticValues.AUTONUM_PURCHASEORDER)
                    postText1.addCell(right);
                
                PdfPTable rightTable = new PdfPTable(1);
                if(mode==StaticValues.AUTONUM_INVOICE){
                    right = createCell("For VRnet(S) Pte Ltd", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(20);
                    rightTable.addCell(right);
                    
                    right = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(35);
                    rightTable.addCell(right);
                }else{
                    right = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(35);
                    rightTable.addCell(right);
                    
                    right = createCell("Name:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    right.setPaddingRight(10);
                    right.setPaddingTop(10);
                    rightTable.addCell(right);
                }
                PdfPCell cell1 = new PdfPCell(postText1);
                cell1.setBorder(0);
                bottomTable.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                bottomTable.addCell(cel2);
                
                bottomCell = new PdfPCell(bottomTable);
                bottomCell.setBorder(0);
            }
            if(mode==StaticValues.AUTONUM_QUOTATION|| mode ==StaticValues.AUTONUM_SALESORDER ) {
                thanksGivingTable = new PdfPTable(2); //for 2 column
                thanksGivingTable.setWidthPercentage(100);
                thanksGivingTable.setWidths(new float[]{70, 40});
                
                PdfPTable leftTable = new PdfPTable(1);
                PdfPCell cell3 = createCell("Yours faithfully,", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingTop(35);
                cell3.setPaddingBottom(5);
                leftTable.addCell(cell3);
                cell3 = createCell("VRnet(S) Pte Ltd", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingBottom(35);
                cell3.setPaddingTop(5);
                leftTable.addCell(cell3);
                
                cell3 = createCell("______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 10);
                cell3.setPaddingRight(10);
                leftTable.addCell(cell3);
                if(mode==StaticValues.AUTONUM_QUOTATION){
                    quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                     salesPerson=quotation.getSalesperson()!=null?quotation.getSalesperson().getValue():null;
                    if(salesPerson!=null){
                        cell3 = createCell("             ("+salesPerson+")", fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                        cell3.setPaddingBottom(35);
                        cell3.setPaddingTop(5);
                        leftTable.addCell(cell3);
                    }
                }
                PdfPTable rightTable = new PdfPTable(1);
                cell3 = createCell("Offer Accepted by:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(35);
                cell3.setPaddingBottom(35);
                rightTable.addCell(cell3);
                
                cell3 = createCell("______________________________\n\n Official Signature & Stamp", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                rightTable.addCell(cell3);
                
                PdfPCell cell1 = new PdfPCell(leftTable);
                cell1.setBorder(0);
                thanksGivingTable.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                thanksGivingTable.addCell(cel2);
//                mainTable.addCell(mainCell63);
            } else if(mode==StaticValues.AUTONUM_DEBITNOTE|| mode ==StaticValues.AUTONUM_CREDITNOTE ) {
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.memo", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setBorder(0);
               receivedByTable1 = new PdfPTable(1);
                receivedByTable1.addCell(cell3);
                cell3 = createCell(memo, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setBorder(0);
                receivedByTable1.addCell(cell3);
              //  mainTable.addCell(mainCell62);
                
                receivedByTable2 = new PdfPTable(2); //for 2 column
                receivedByTable2.setWidthPercentage(100);
                receivedByTable2.setWidths(new float[]{60, 40});
                
                PdfPTable leftTable = new PdfPTable(1);
                if(mode==StaticValues.AUTONUM_CREDITNOTE) {
                    cell3 = createCell("Goods Received By:______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    cell3.setPaddingRight(10);
                    cell3.setPaddingTop(35);
                    cell3.setPaddingLeft(5);
                    cell3.setPaddingBottom(35);
                    leftTable.addCell(cell3);
                } else {
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                    cell3.setPaddingRight(10);
                    cell3.setPaddingTop(35);
                    cell3.setPaddingLeft(5);
                    cell3.setPaddingBottom(35);
                    leftTable.addCell(cell3);
                }
                
                PdfPTable rightTable = new PdfPTable(1);
                cell3 = createCell("Signature: ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(35);
                rightTable.addCell(cell3);
                
                cell3 = createCell("Name:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(10);
                rightTable.addCell(cell3);
                
                PdfPCell cell1 = new PdfPCell(leftTable);
                cell1.setBorder(0);
                receivedByTable2.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(rightTable);
                cel2.setBorder(0);
                receivedByTable2.addCell(cel2);
               // mainTable.addCell(mainCell63);
                
             } 
             mainTable.setSplitLate(false); 
            if(mode == StaticValues.AUTONUM_DELIVERYORDER) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);    
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);                    
                
                String orderID ="";
                String theader = "";
                String pointPern = "";
                String recQuantity = "";
                String status="";
                DeliveryOrder deliveryOrder=null;
                deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), billid);
                String invoicePostText=deliveryOrder.getPostText()==null?"":deliveryOrder.getPostText();
                postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)&& deliveryOrder.getTemplateid() != null)?deliveryOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = deliveryOrder.getCustomer().getAccount();
                invno = deliveryOrder.getDeliveryOrderNumber();
                entryDate = deliveryOrder.getOrderDate();
                customerName = deliveryOrder.getCustomer().getName();
                status=deliveryOrder.getStatus() != null ? deliveryOrder.getStatus().getValue():"";
                customerEmail= deliveryOrder.getCustomer()!=null?deliveryOrder.getCustomer().getEmail():"";
                billTo="Bill To";
                if (deliveryOrder.getBillingShippingAddresses() != null) { //If DO have address from show address then showing that address otherwise showing customer address
                    billAddress = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), true);
                    shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), false);
                } else {
                    addrParams.put("customerid", deliveryOrder.getCustomer().getID());
                    addrParams.put("companyid", com.getCompanyID());
                    addrParams.put("isBillingAddress", false);
                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    addrParams.put("isBillingAddress", true);
                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                }
                memo = deliveryOrder.getMemo();
                shipDate = deliveryOrder.getShipdate();
                shipvia = deliveryOrder.getShipvia();
                fob = deliveryOrder.getFob();
                orderID = deliveryOrder.getID();
                theader = messageSource.getMessage("acc.accPref.autoDO", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                datetheader = messageSource.getMessage("acc.accPref.autoDateDO", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                pointPern = "acc.common.to";
                recQuantity = "acc.accPref.NewdeliQuant";

                com = deliveryOrder.getCompany();
                if (deliveryOrder.getTemplateid() != null) {
                    config = new com.krawler.utils.json.base.JSONObject(deliveryOrder.getTemplateid().getConfigstr());
                    // document = getTemplateConfig(document,writer);
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(com)) {
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }
                }
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                } 
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                theader = "Delivery Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});
                if (addShipTo) {
                    tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                }
                if (isCompanyTemplateLogo || storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if (addShipTo) {
                        if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                            table1.setWidths(new float[]{65, 35});
                        }
                        tab2 = ExportRecordHandler.getDateTable2(entryDate, invno, datetheader, formatter, invCell);
                    }
                }
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
//                mainCell11.setPaddingBottom(5);
//                cell1.setPaddingTop(140);
                mainTable.addCell(mainCell11);
                
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                    filter_names.add("deliveryOrder.ID");
                }

                filter_params.add(orderID);
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                    idresult = accInvoiceDAOobj.getDeliveryOrderDetails(invRequestParams);
                }

                itr = idresult.getEntityList().iterator();
                itr1 = idresult.getEntityList().iterator();
                if(itr1.hasNext()) {
                    DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr1.next();
                    if(row8.getCidetails()!=null){
                        Invoice inv = row8.getCidetails().getInvoice();
                        linkTo= inv.getInvoiceNumber();
                        porefno = inv.getPoRefNumber();
                        terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
//                        Date InvEntryDate = inv.getJournalEntry().getEntryDate();
                        Date InvEntryDate = inv.getCreationDate();
                        Date InvdueDate = inv.getDueDate();
                        int termdays = (int)( (InvdueDate.getTime() - InvEntryDate.getTime()) / (1000 * 60 * 60 * 24));
                        HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
                        termrequestParams.put("companyid", jsonObj.optString(Constants.companyKey));
                        termrequestParams.put("termdays", termdays);

                        KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
                        List<Term> termlist = termresult.getEntityList();
                        if(termlist.size()>0) {
                            terms = termlist.get(0).getTermname();
                        }
                        if (inv.getMasterSalesPerson() != null) {   //if User Class returns the Null Valu
                            salesPerson = inv.getMasterSalesPerson() != null ? inv.getMasterSalesPerson().getValue() : "";
                        }
                    }else if(row8.getSodetails()!=null){
                        invno=row8.getSodetails().getSalesOrder().getSalesOrderNumber();                                    
                    } 
                }

                if(!StringUtil.isNullOrEmpty(preText)){
                         ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                }
                
                if (mode != StaticValues.AUTONUM_RFQ) {
                    PdfPTable addressMainTable = null;
//                    .getAddressTable(customerName,billAddres,customerEmail,billTo,shipTo,true);
                    addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, addShipTo);
                    if (!addShipTo) {                                                                       
                        PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj,salesPerson, entryDate, linkTo, invno, porefno, terms, currencyid, formatter);
                        PdfPCell cel3 = new PdfPCell(shipToTable);
                        cel3.setBorder(0);
                        addressMainTable.addCell(cel3);
                    }

                    PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(1);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);
                }     
                boolean companyFlag = (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && (mode == StaticValues.AUTONUM_DELIVERYORDER));
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                    if (!linkHeader.equalsIgnoreCase("")) {
                        headerList.add(linkHeader);
                    }
                if (!(companyFlag)) {
                    headerList.add(messageSource.getMessage("acc.rem.prodName", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }
                 //  get product custom fields 
                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(com.getCompanyID(), Constants.Acc_Product_Master_ModuleId, 0));
                fieldrequestParams.put("relatedmoduleid", String.valueOf(Constants.Acc_Delivery_Order_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                KwlReturnObject kwlcustomColumn = accAccountDAOobj.getFieldParams(fieldrequestParams);
                List<FieldParams> customColList = kwlcustomColumn.getEntityList();
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                HashMap<String, Integer> customMap = new HashMap();
                List<String> customHeaderList = new ArrayList();
                for (FieldParams field : customColList) {
                    headerList.add(field.getFieldlabel());
                    customHeaderList.add(field.getFieldlabel());
                    String[] relatedTo = field.getRelatedmoduleid().split(",");
                    String[] relatedToWidth = field.getRelatedmodulepdfwidth()!=null ? field.getRelatedmodulepdfwidth().split(",") : "".split(",");
                    for(int customCnt=0; customCnt<relatedTo.length; customCnt++) {
                        if(relatedTo[customCnt].equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                            if(relatedToWidth.length > customCnt && !StringUtil.isNullOrEmpty(relatedToWidth[customCnt])) {
                                customMap.put(field.getFieldlabel(), Integer.parseInt(relatedToWidth[customCnt]));
                            } else {
                                customMap.put(field.getFieldlabel(), 0);
                            }
                        }
                    }
                }
                
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                if (mode != StaticValues.AUTONUM_RFQ && mode != StaticValues.AUTONUM_DELIVERYORDER) {
                    headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                    headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                }

                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                PdfPTable table = null;
                if (mode == StaticValues.AUTONUM_RFQ) {
                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    productHeaderTableGlobalNo = 1;
                } else {
                    table = ExportRecordHandler.getTable(linkHeader, true);
                    productHeaderTableGlobalNo = 2;
                }
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                    productHeaderTableGlobalNo = 7;
                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableForVRNetDO();
                    }
                }
                
                
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                if (mode == StaticValues.AUTONUM_RFQ) {
                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
                } else {
                    table = ExportRecordHandler.getTable(linkHeader, true);
                }

                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableForVRNetDO();
                    }
                }
                
//                String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)),
//                        messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)),
//                        messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request))};
//                PdfPTable table = ExportRecordHandler.getBlankTableForVRNetDO();
//                productHeaderTableGlobalNo=3;
//                PdfPCell invcell = null;
//                for (int i = 0; i < header.length; i++) {
//                     invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
//                     invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                     invcell.setBackgroundColor(Color.LIGHT_GRAY);
//                     invCell.setBorder(0);
//                     invcell.setPadding(3);
//                     table.addCell(invcell);
//                }
//                globalTableHeader=header;
//                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
//                table = ExportRecordHandler.getBlankTableForVRNetDO();
                
                int index=0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo= "";
                    double quantity = 0,deliverdQuantity=0;
                    String uom = "";
//                    String linkTo="-";

                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                            prodDesc = row8.getDescription();
                        }else{
                             if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                 prodDesc= row8.getProduct().getDescription();
                             }
                        }
                        prodName = row8.getProduct().getName();
                        if(row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())){
                            partNo = row8.getPartno(); 
//                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
//                            prodName += "\n" + partno;
//                            prodName += "\n" ;
                        }
                        prodName += "\n" ;
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getUom()==null?(row8.getProduct().getUnitOfMeasure()==null?"":row8.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row8.getUom().getNameEmptyforNA();
                                                        if(row8.getCidetails()!=null){
                            linkTo=row8.getCidetails().getInvoice().getInvoiceNumber();
                        }else if(row8.getSodetails()!=null){
                            linkTo=row8.getSodetails().getSalesOrder().getSalesOrderNumber();                                    
                        }
                                                        
                        AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), row8.getProduct().getID());
                        if (obj != null) {
                            productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        }                                    
                    }

                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);                             
//                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    invCell.setBorder(0);
//                    table.addCell(invcell);
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invCell.setBorder(0);
                    table.addCell(invcell);

                        for (String varEntry : customHeaderList) {
                            boolean isBlankVal = true;
                            if(variableMap.containsKey("Custom_"+varEntry)) {
                                String coldata = variableMap.get("Custom_"+varEntry).toString();
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    invcell = createCell(coldata, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                    table.addCell(invcell);
                                    isBlankVal = false;
                                } 
                            }
                            if(isBlankVal){
                                invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                table.addCell(invcell);
                            }
                        }
                    
                    
                    
                    String qtyStr = Double.toString(quantity);
//                             if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_DELIVERYORDER ) {
                                   qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
//                                        }
                    invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
//                    invcell = createCell(Double.toString((double)deliverdQuantity)+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    invCell.setBorder(0);
//                    table.addCell(invcell);                             
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row                                
                    if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                        if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetDO(customMap);
                        } else {
                            table = ExportRecordHandler.getBlankTableForVRNetDO();
                        }
                    }
                }
                
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;

                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
    
                
                // get after Items table height
                bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                bottomTable.calculateHeightsFast();
                float bottomTableHeight = bottomTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - bottomTableHeight - aboveProdTableContent  - 85*pageNo) / blankRowHeight; //top+bottom=5+30
                int noOfCols = 3;
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }

                 mainTable = new PdfPTable(1);
                 mainTable.setWidthPercentage(100);
                 
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
               
                

             //   PdfPCell mainCell61 = new PdfPCell(helpTable);
//                mainCell61.setBorder(0);
//                mainTable.addCell(mainCell61);
                
                document.add(mainTable);
                document.getPageNumber();
                  
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
            }  else if (mode == StaticValues.AUTONUM_QUOTATION) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                currencyid = (quotation.getCurrency()==null)? currencyid : quotation.getCurrency().getCurrencyID();
                com = quotation.getCompany();
                customerName = quotation.getCustomer().getName();
                customerEmail= quotation.getCustomer()!=null?quotation.getCustomer().getEmail():"";
                String phoneNo = quotation.getCustomer().getContactNumber();
                String faxNo = quotation.getCustomer().getFax();
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(),false);
                 if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in quotation table 
                   shipAddress=quotation.getShipTo()==null?"":quotation.getShipTo();
                } 
                terms=quotation.getCustomer()!=null?quotation.getCustomer().getCreditTerm().getTermname():"";
                billTo="Bill To";
                isInclude=false;
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in quotation table 
                  billAddress=quotation.getBillTo()!=null?quotation.getBillTo():"";
                }
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                
                filter_names.add("quotation.ID");
                filter_params.add(quotation.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                memo = quotation.getMemo();

                    
                String invoicePostText=quotation.getPostText()==null?"":quotation.getPostText();
                postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && quotation.getTemplateid() != null)?quotation.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = quotation.getCustomer().getAccount();
                invno = quotation.getquotationNumber();
                dueDate=quotation.getDueDate();
                entryDate = quotation.getQuotationDate();
                mainTax = quotation.getTax();
                shipDate = quotation.getShipdate();
                shipvia = quotation.getShipvia();
                fob = quotation.getFob();
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                isCompanyTemplateLogo = true;
                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{55, 45});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Customer Quotation";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and Quotation details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, null, phoneNo, faxNo, customerEmail, formatter, mode);
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                productHeaderTableGlobalNo = 6;
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                     Phrase phrase1 =new Phrase();
                     Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    
                    QuotationDetail row7 = (QuotationDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row7.getDescription())){
                        prodDesc = row7.getDescription();
                    }else{
                       if (!StringUtil.isNullOrEmpty(row7.getProduct().getDescription())) {
                            prodDesc = row7.getProduct().getDescription();
                        }
                    }
                    prodName = row7.getProduct().getName();
                    quantity =  row7.getQuantity();
                    rate = row7.getRate() ;
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountQuotation = (row7.getDiscountispercent() == 1)? rateInCurr*quantity *row7.getDiscount()/100 : row7.getDiscount();
                    uom = row7.getUom()==null?(row7.getProduct().getUnitOfMeasure()==null?"":row7.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row7.getUom().getNameEmptyforNA();
                    
                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                     invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                     invcell.setBorder(0);
//                    invcell = createCell(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                        rate = rateInBase;
                    }
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate * quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discount != null) {
                        amount1 -= row7.getDiscount();
                    }
                    if (discountQuotation != 0) {
                        amount1 -= discountQuotation;
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    if (row7!= null&&row7.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row7.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row7.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row7.getRowTaxAmount();
                        }
                    }  
                    
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (mode == StaticValues.AUTONUM_RFQ) {
                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    } else {
                        table = ExportRecordHandler.getTable(linkHeader, true);
                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());   //create table with 6 columns for extra row and Bottom 
                        
                } // END Of Product Details
                
                  //table = getBlankTable();
  
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                if(mode==StaticValues.AUTONUM_QUOTATION){
                    term= ExportRecordHandler.appendTermDetailsQuotation(accSalesOrderDAOobj,authHandlerDAOObj, quotation, table, currencyid,mode, CompanyID);
                }
                total+=term;
                totalAmount+=term;
                if(!quotation.isPerDiscount()){
                    discountTotalQuotation = quotation.getDiscount();
                    total = total - quotation.getDiscount();
                    totalAmount = total;
                } else {
                    discountTotalQuotation = total * quotation.getDiscount()/100;
                    total -= (total * quotation.getDiscount()/100);
                    totalAmount = total;
                }
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null,StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation, CompanyID), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
               
                // get after Items table height 
                
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    
                    PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                   
                
                thanksGivingTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * thanksGivingTable.getWidthPercentage() / 100);
                thanksGivingTable.calculateHeightsFast();
                float thanksGivingTableHeight = thanksGivingTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - thanksGivingTableHeight - aboveProdTableContent - summeryTableHeight - 90*pageNo) / blankRowHeight; //top+bottom=25+30+5 padding at end 5
                int noOfCols = headerList.size();
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
              
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                //this code is moved to up for calculation of free page size.
                mainTable.addCell(mainCell12);
            } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                sOrder = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), billid);
                if(sOrder.getApprover()!=null)
                    approverName=sOrder.getApprover().getFirstName() +" "+sOrder.getApprover().getLastName();
                currencyid = (sOrder.getCurrency()==null)? currencyid : sOrder.getCurrency().getCurrencyID();
                com = sOrder.getCompany();

                 if (sOrder.getTemplateid() != null) {
                    config = new com.krawler.utils.json.base.JSONObject(sOrder.getTemplateid().getConfigstr());
                   // document = getTemplateConfig(document,writer);
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if(!ExportRecordHandler.checkCompanyTemplateLogoPresent(com)){
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }                        
                }
                 
                String invoicePostText=sOrder.getPostText()==null?"":sOrder.getPostText();
                postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?sOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                cEntry = sOrder.getCustomer().getAccount();
                invno = sOrder.getSalesOrderNumber();
                dueDate=sOrder.getDueDate();
                entryDate = sOrder.getOrderDate();
                mainTax = sOrder.getTax();
                shipDate=sOrder.getShipdate();
                shipvia=sOrder.getShipvia();
                fob=sOrder.getFob();
                
                customerName = sOrder.getCustomer().getName();
                customerEmail= sOrder.getCustomer()!=null?sOrder.getCustomer().getEmail():"";
                String faxNo= sOrder.getCustomer().getFax();
                String PhoneNo = sOrder.getCustomer().getContactNumber();
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(),false);
                 if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in invoice table 
                   shipAddress=sOrder.getShipTo()==null?"":sOrder.getShipTo();
                } 
                terms=sOrder.getCustomer()!=null?sOrder.getCustomer().getCreditTerm().getTermname():"";       
                billTo="Bill To";
                isInclude=false;
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                          
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                  if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in invoice table 
                     billAddress = sOrder.getBillTo()!=null?sOrder.getBillTo():"";
                }
                filter_names.add("salesOrder.ID");
                filter_params.add(sOrder.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                memo = sOrder.getMemo();

                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Sales Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and Quotation details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, null, PhoneNo, faxNo, customerEmail, formatter ,mode );
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());
                productHeaderTableGlobalNo = 6;
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                     Phrase phrase1 =new Phrase();
                     Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    
                    SalesOrderDetail row5 = (SalesOrderDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row5.getDescription())){
                        prodDesc = row5.getDescription();
                    }else{
                       if (!StringUtil.isNullOrEmpty(row5.getProduct().getDescription())) {
                            prodDesc = row5.getProduct().getDescription();
                        }
                    }
                    prodName = row5.getProduct().getName();
                    quantity =  row5.getQuantity();
                    rate = row5.getRate() ;
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountQuotation = (row5.getDiscountispercent() == 1)? rateInCurr*quantity *row5.getDiscount()/100 : row5.getDiscount();
                    uom = row5.getUom()==null?(row5.getProduct().getUnitOfMeasure()==null?"":row5.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row5.getUom().getNameEmptyforNA();

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);
                    
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                        rate = rateInBase;
                    }
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate * quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discount != null) {
                        amount1 -= row5.getDiscount();
                    }
                    if (discountQuotation != 0) {
                        amount1 -= discountQuotation;
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    if (row5!= null&&row5.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row5.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row5.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row5.getRowTaxAmount();
                        }
                    }  
                    
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (mode == StaticValues.AUTONUM_RFQ) {
                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    } else {
                        table = ExportRecordHandler.getTable(linkHeader, true);
                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(new HashMap<String, Integer>());   //create table with 6 columns for extra row and Bottom 
                        
                } // END Of Product Details
                
                
                //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                term= ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj,authHandlerDAOObj,sOrder , table1, currencyid,mode, CompanyID);
                total+=term;
                totalAmount+=term;
                if(sOrder.getDiscount() != 0) {
                    if(!sOrder.isPerDiscount()){
                            discountTotalQuotation = sOrder.getDiscount();
                            total = total - sOrder.getDiscount();
                            totalAmount = total;
                    } else {
                            discountTotalQuotation = total * sOrder.getDiscount()/100;
                            total -= (total * sOrder.getDiscount()/100);
                            totalAmount = total;
                    }
                }
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation, CompanyID), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                 ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
             
                
                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);

                 thanksGivingTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * thanksGivingTable.getWidthPercentage() / 100);
                thanksGivingTable.calculateHeightsFast();
                float thanksGivingTableHeight = thanksGivingTable.getTotalHeight();
                
                // get main table (having items and global fields) height 
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - thanksGivingTableHeight - aboveProdTableContent - summeryTableHeight - 90*pageNo) / blankRowHeight; //top+bottom=30+30+5 padding at end
                int noOfCols = 5;
                if(addBlankRows<0)
                    addBlankRows=10; 
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);
                 PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);

                
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
            } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                KWLCurrency currency = null;
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                
                pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                currencyid = (pOrder.getCurrency()==null)? currencyid : pOrder.getCurrency().getCurrencyID();
                com = pOrder.getCompany();
                if(pOrder.getApprover()!=null)
                    approverName=pOrder.getApprover().getFirstName() +" "+pOrder.getApprover().getLastName();

                cEntry = pOrder.getVendor().getAccount();
                invno = pOrder.getPurchaseOrderNumber();
                dueDate=pOrder.getDueDate();
                entryDate = pOrder.getOrderDate();
                mainTax = pOrder.getTax();
                shipDate=pOrder.getShipdate();
                shipvia=pOrder.getShipvia();
                fob=pOrder.getFob();
                currency = pOrder.getCurrency();
                
//                Invoice inv = pOrder.
//                terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
//                Date InvEntryDate = inv.getJournalEntry().getEntryDate();
//                Date InvdueDate = inv.getDueDate();
//                int termdays = (int)( (InvdueDate.getTime() - InvEntryDate.getTime()) / (1000 * 60 * 60 * 24));
//                HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
//                termrequestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//                termrequestParams.put("termdays", termdays);
//
//                KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
//                List<Term> termlist = termresult.getEntityList();
//                if(termlist.size()>0) {
//                    terms = termlist.get(0).getTermname();
//                }
                
                
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                
                customerName = pOrder.getVendor().getName();
                customerEmail= pOrder.getVendor()!=null?pOrder.getVendor().getEmail():"";
                terms=pOrder.getVendor()!=null?pOrder.getVendor().getDebitTerm().getTermname():"";                        
                billTo="Sold To";
                isInclude=false;
                billAddress = CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(), true);//true used for billing  and false  for shipping 
                if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in purchaseorder table 
                      billAddress = pOrder.getBillTo()==null?"":pOrder.getBillTo();
                }
                shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(),false);
                if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in purchaseorder table 
                   shipAddress=pOrder.getShipTo()==null?"":pOrder.getShipTo();
                }
                filter_names.add("purchaseOrder.ID");
                filter_params.add(pOrder.getID());
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                itr = idresult.getEntityList().iterator();
                itr1 = idresult.getEntityList().iterator();
                if(itr1.hasNext()) {
                    PurchaseOrderDetail details = (PurchaseOrderDetail) itr1.next();
                    if (!StringUtil.isNullOrEmpty(details.getSalesorderdetailid())) {
                        KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), details.getSalesorderdetailid());
                        SalesOrderDetail salesOrderDetail = (SalesOrderDetail) sodetailresult.getEntityList().get(0);
                        if(salesOrderDetail!=null) {
                            linkTo = salesOrderDetail.getSalesOrder().getSalesOrderNumber();
                            if(salesOrderDetail.getSalesOrder().getSalesperson()!=null)
                                salesPerson = salesOrderDetail.getSalesOrder().getSalesperson().getValue();
                        }
                    } 
                    else if (details.getVqdetail() != null) {
                        linkTo = details.getVqdetail().getVendorquotation().getQuotationNumber();
                    }
                }
                terms = pOrder.getTerm()!=null ? pOrder.getTerm().getTermname() : "";
                memo = pOrder.getMemo();
                
                isInclude=false;
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }
                
                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                isCompanyTemplateLogo = true;
                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{65, 35});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = "Purchase Order";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                mainTable.setSplitLate(false);
                // Added top table with sell address details and CN/DN details
                PdfPTable addressMainTable = null;
//                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddres, customerEmail, billTo, shipTo, false);
                addressMainTable = ExportRecordHandler.getAddressTableForVRnetPurchaseOrder(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, customerName, billAddress, billTo, shipAddress, terms, formatter);
//                PdfPCell cel3 = new PdfPCell(shipToTable);
//                cel3.setBorder(0);
//                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add("Product Code");
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{6, 10, 40,12, 15, 17});
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                table.setSplitLate(false);
                int index = 0;
                productHeaderTableGlobalNo = 8;
                PurchaseOrderDetail row6 = null;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String pID = "";
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    double rate = 0;
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable=false;
                    String rowTaxName = "";
                    row6 = (PurchaseOrderDetail) itr.next();
                    if(!StringUtil.isNullOrEmpty(row6.getDescription())){
                        prodDesc = row6.getDescription();
                    }else{
                        if (!StringUtil.isNullOrEmpty(row6.getProduct().getDescription())) {
                            prodDesc = row6.getProduct().getDescription();
                        }
                    }
                    prodName = row6.getProduct().getName();
                    quantity = row6.getQuantity();
                    rate = row6.getRate();
                    pID = row6.getProduct().getProductid();
                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInCurr = (Double) bAmt.getEntityList().get(0);
                    discountOrder = (row6.getDiscountispercent() == 1)? rateInCurr*quantity *row6.getDiscount()/100 : row6.getDiscount();
                    uom = row6.getUom()==null?(row6.getProduct().getUnitOfMeasure()==null?"":row6.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row6.getUom().getNameEmptyforNA();

                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    double rateInBase = (Double) bAmt.getEntityList().get(0);
                    rate = rateInBase;
                    
                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name 
                    invcell = createCell(pID, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);
                    
                    //String qtyStr = Double.toString(quantity);
                   // if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                        String qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    //}
                    invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    
                    invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    amount1 = rate*quantity;

                    invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (discountQuotation != 0){
                        amount1 -= discountQuotation;
                    }
                    if (discountOrder != 0){
                        amount1 -= discountOrder;
                        discountQuotation = discountOrder;
                    }
                    
                    if (row6!= null&&row6.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row6.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName=row6.getTax().getName();
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row6.getRowTaxAmount();
                        }
                    } 
                    amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                    total += amount1;
                
                } // END Of Product Details
                

                     //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                    mainTaxName=mainTax.getName();
                }
                totalAmount = total;
                double term=0;
                if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                    term= ExportRecordHandler.appendTermDetailsPurchaseOrder(accPurchaseOrderobj,authHandlerDAOObj,row6.getPurchaseOrder(), table1, currencyid,mode, CompanyID);
                }
                total+=term;
                totalAmount+=term;
                if(mode==StaticValues.AUTONUM_PURCHASEORDER && pOrder.getDiscount() != 0){
                    if(!pOrder.isPerDiscount()){
                            discountTotalQuotation = pOrder.getDiscount();
                            total = total - pOrder.getDiscount();
                            totalAmount = total;
                    } else {
                            discountTotalQuotation = total * pOrder.getDiscount()/100;
                            total -= (total * pOrder.getDiscount()/100);
                            totalAmount = total;
                    }
                } 
                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                totalAmount = total + totaltax;

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountTotalQuotation>0.0) {
                    table1.addCell(cell3);
                    invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, discountTotalQuotation, currencyid, false, CompanyID);//new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountTotalQuotation), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(mainTaxName);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }
                cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);
                cell3 = createCell("", local_fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, 0, 5);
                table1.addCell(cell3);
                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnetQuotation(table1, memo, addShipTo);
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                
                //int belowProdTableContent =295;
                 ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
               
                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                table2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * table2.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                 // get after Items table height
                bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                bottomTable.calculateHeightsFast();
                float bottomTableHeight = bottomTable.getTotalHeight();
                
                float table2Height = table2.getTotalHeight();
                int blankRowHeight = 4;

                float addBlankRows = (document.getPageSize().getHeight()*pageNo -summeryTableHeight- aboveProdTableContent -table2Height -bottomTableHeight -100*pageNo) / blankRowHeight;//80 =top+bottom=30+30 paddingTop =20
                int noOfCols = headerList.size();
                if(addBlankRows<0)
                    addBlankRows=10; 
               int BlankCellCnt = (int) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                int lastRow = BlankCellCnt - noOfCols;
                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);

                mainTable.addCell(mainCell12);
                
                
                mainTable.addCell(mainCell62);
                
            } else if (mode == StaticValues.AUTONUM_CREDITNOTE|| mode == StaticValues.AUTONUM_DEBITNOTE) {
                KWLCurrency currency = null;
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                isCompanyTemplateLogo = true;
                CreditNote creNote = null;
                DebitNote dbNote = null;
                
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                com  = null;
                double cndnTotalAmount = 0;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;
                double discountMain = 0;
                double subTotal = 0;
                
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                    creNote = (CreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=(creNote.getJournalEntry() != null)?creNote.getJournalEntry().getDetails():null;
                    customerObj = new Customer();
                    itr=(entryset!=null)?entryset.iterator():null;
                        if(itr!=null){
                        while(itr.hasNext()){
                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
                            customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
                            if(customerObj!=null)
                                    break;
                        }
                    }
                    com = creNote.getCompany();
                    invno = creNote.getCreditNoteNumber();
                    currency = creNote.getCurrency();
                    cndnTotalAmount=creNote.getCnamount();
                    entryDate = (creNote.isNormalCN())?creNote.getJournalEntry().getEntryDate():creNote.getCreationDate();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE ){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
                    com = dbNote.getCompany();
                    currency = dbNote.getCurrency();
                    Set<JournalEntryDetail> entryset=(dbNote.getJournalEntry()!=null)?dbNote.getJournalEntry().getDetails():null;
                    vendorObj=new Vendor();
                    itr=(entryset!=null)?entryset.iterator():null;
                    if(itr!=null){
                        while(itr.hasNext()){
                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
        //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                            vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
                            if(vendorObj!=null)
                                break;
                        }
                    }
                    invno = dbNote.getDebitNoteNumber();
                    entryDate = (dbNote.isNormalDN())?dbNote.getJournalEntry().getEntryDate():dbNote.getCreationDate();
                    cndnTotalAmount=dbNote.getDnamount();
                }
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                Date linkToDate = null;
                String faxNo = "";
                String PhoneNo = "";
                if(mode==StaticValues.AUTONUM_CREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail= customerObj!=null?customerObj.getEmail():"";
                    faxNo= customerObj.getFax();
                    PhoneNo = customerObj.getContactNumber();
                    if (creNote.getBillingShippingAddresses() != null) {
                        billAddress = CommonFunctions.getBillingShippingAddressWithAttn(creNote.getBillingShippingAddresses(), true);
                    } else {
                        billAddress = "";
                    }
                    if (creNote.getBillingShippingAddresses() != null) {
                            shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(creNote.getBillingShippingAddresses(), false);
                    } else {
                        shipAddress = "";
                    }
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", false);
//                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    terms=customerObj!=null?customerObj.getCreditTerm().getTermname():"";
                    billTo="Bill To";
                    addrParams.put("isBillingAddress", true);
//                    billAddress =accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    Iterator cloneItr = idresult.getEntityList().iterator();
                    if(cloneItr.hasNext()) {
                        CreditNoteDetail details = (CreditNoteDetail) cloneItr.next();
                        if(creNote.isOtherwise() && details.getPaidinvflag() != 1) {
                            linkTo = details.getInvoice()==null?"":details.getInvoice().getInvoiceNumber();    
                            linkToDate = details.getInvoice()==null?null:details.getInvoice().getJournalEntry().getEntryDate();
                        } else {
                            linkTo = details.getInvoiceRow()==null?"":details.getInvoiceRow().getInvoice().getInvoiceNumber();
                            linkToDate = details.getInvoiceRow()==null?null:details.getInvoiceRow().getInvoice().getJournalEntry().getEntryDate();
                        }
                    }
                    memo =creNote.getMemo();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail= vendorObj!=null?vendorObj.getEmail():"";
                    faxNo= vendorObj.getFax();
                    PhoneNo = vendorObj.getContactNumber();
                    terms=vendorObj!=null?vendorObj.getDebitTerm().getTermname():"";
                    billTo="Supplier";
                    addrParams.put("vendorid", vendorObj.getID());
                    addrParams.put("companyid", vendorObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", true);
                    if (creNote.getBillingShippingAddresses() != null) {
                            billAddress = CommonFunctions.getBillingShippingAddressWithAttn(dbNote.getBillingShippingAddresses(), true);
                    } else {
                        billAddress = "";
                    }
//                    billAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    filter_names.add("debitNote.ID");
                    filter_params.add(dbNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    Iterator cloneItr = idresult.getEntityList().iterator();
                    if(cloneItr.hasNext()) {
                        
                        DebitNoteDetail details = (DebitNoteDetail) cloneItr.next();
                        if(dbNote.isOtherwise()  && details.getPaidinvflag() != 1) {
                            linkTo = details.getGoodsReceipt()==null?"":details.getGoodsReceipt().getGoodsReceiptNumber();   
                            linkToDate = details.getGoodsReceipt()==null?null:details.getGoodsReceipt().getJournalEntry().getEntryDate();
                        } else {
                            linkTo = details.getGoodsReceiptRow()==null?"":details.getGoodsReceiptRow().getGoodsReceipt().getGoodsReceiptNumber();
                            linkToDate = details.getGoodsReceiptRow()==null?null:details.getGoodsReceiptRow().getGoodsReceipt().getJournalEntry().getEntryDate();
                        }
//                        if(details.getGoodsReceiptRow()!=null) {
//                            linkTo = details.getGoodsReceipt().getGoodsReceiptNumber();
//                            linkToDate = details.getGoodsReceipt().getJournalEntry().getEntryDate();
//                        }
                    }
                    memo = dbNote.getMemo();
                }

                isInclude=false;
                
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = mode == StaticValues.AUTONUM_CREDITNOTE ? "Customer Credit Note" : "Customer Debit Note";
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_DELIVERYORDER);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
                mainTable.addCell(mainCell11);
                
                // Added top table with sell address details and CN/DN details
                PdfPTable addressMainTable = null;
                addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, false);
                PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNetQuotation(authHandlerDAOObj,salesPerson,
                        entryDate, invno, linkTo, linkToDate, PhoneNo, faxNo, customerEmail, formatter ,mode );
                PdfPCell cel3 = new PdfPCell(shipToTable);
                cel3.setBorder(0);
                addressMainTable.addCell(cel3);

                PdfPCell mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(1);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                // 
                
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                requestParams.put(Constants.companyKey,CompanyID );
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df,formatter);  
                
                List<String> headerList = new ArrayList<String>();
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add("Product Code");
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                
                PdfPTable table = null;
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{6, 10, 40,12, 15, 17});
                productHeaderTableGlobalNo = 8;
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableReportForCreditNote();
                int index = 0;
                CreditNoteDetail row = null;
                DebitNoteDetail row1 = null;
                BillingCreditNoteDetail row2 = null;
                BillingDebitNoteDetail row3 = null;  
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    double quantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String cndnName = "";
                    double cndnDiscount=0;
                    double rate = 0;
                    String pID = "";
                    if (mode == StaticValues.AUTONUM_CREDITNOTE ) {
                        row = (CreditNoteDetail) itr.next();
                        if(!otherwiseFlag){    
                            if (!StringUtil.isNullOrEmpty(row.getInvoiceRow().getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInvoiceRow().getInventory().getProduct().getDescription();
                            }
                            pID = row.getInvoiceRow().getInventory().getProduct().getProductid();
                            rate = row.getInvoiceRow().getRate();
                            prodName = row.getInvoiceRow().getInventory().getProduct().getName();
                            quantity =  row.getQuantity();
                            if(row.getDiscount()!=null){
                                if (row.getTotalDiscount() != null) {                                
                                    discountMain = discountMain + row.getTotalDiscount();
                                    total = total - row.getTotalDiscount();
                                }
                                discount = row.getDiscount();
                            } 
                            if(row.getTaxAmount()!=null){
                                taxMain = taxMain + row.getTaxAmount();
                                total = total + row.getTaxAmount();                        
                            }
                            try{
//                                uom = row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getName();
                                 uom = row.getInvoiceRow().getInventory().getUom()==null?(row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getInvoiceRow().getInventory().getUom().getNameEmptyforNA();
                            } catch(Exception ex){//In case of exception use uom="";
                            } 
                        }
                        else if(otherwiseFlag){
                            cndnName = row.getInvoice()!=null?row.getInvoice().getInvoiceNumber():"";
                            cndnDiscount=row.getDiscount()!=null?row.getDiscount().getDiscount():0.0;
                            cndnTotalOtherwiseAmount+=cndnDiscount;
                        }
                    }else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                        row1 = (DebitNoteDetail) itr.next();
                        if(!otherwiseFlag){   
                            if (!StringUtil.isNullOrEmpty(row1.getGoodsReceiptRow().getInventory().getProduct().getDescription())) {
                               prodDesc = row1.getGoodsReceiptRow().getInventory().getProduct().getDescription();
                           }
                           prodName = row1.getGoodsReceiptRow().getInventory().getProduct().getName();
                           rate = row1.getGoodsReceiptRow().getRate();
                           quantity =  row1.getQuantity();
                           pID = row1.getGoodsReceiptRow().getInventory().getProduct().getProductid();
                           if (row1.getDiscount() != null) {
                               if (row1.getTotalDiscount() != null) {
                                   discountMain = discountMain + row1.getTotalDiscount();
                                   total = total - row1.getTotalDiscount();
                               }   
                               discount = row1.getDiscount();
                           }
                           if (row1.getTaxAmount() != null) {
                               taxMain = taxMain + row1.getTaxAmount();
                               total = total + row1.getTaxAmount();
                           }
                           try{
//                               uom = row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getName();
                               uom = row1.getGoodsReceiptRow().getInventory().getUom()==null?(row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row1.getGoodsReceiptRow().getInventory().getUom().getNameEmptyforNA();
                           } catch(Exception ex){//In case of exception use uom="";
                           }
                       }
                        
                    }
                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    
                    invcell = createCell(pID, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                    invcell.setBorder(0);
                    table.addCell(invcell);

                    invcell = createCell((double)quantity+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = createCell(String.valueOf(rate), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if(discount!=null){
                        invcell = createCell(authHandler.formattedAmount(discount.getDiscountValue(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);                        
                    }else{
                        invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    }
                    table.addCell(invcell);
                    ExportRecordHandler.addTableRow(mainTable, table);
                    table = ExportRecordHandler.getBlankTableReportForCreditNote();
                    if(discount!=null){
                       subTotal += discount.getDiscountValue();
                       total += discount.getDiscountValue();
                    } 

                } // END Of Product Details 
                 //table = getBlankTable();
                table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(subTotal, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);

                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                if(discountMain>0.0) {
                    table1.addCell(cell3);
                    invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, discountMain, currencyid, false, CompanyID);//new PdfPCell(new Paragraph(ExportRecordHandler.currencyRenderer(discountMain), fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setBorder(Rectangle.BOX);
                    invcell.setPadding(5);
                    table1.addCell(invcell);
                }
                
                StringBuffer taxNameStr = new StringBuffer();

                taxNameStr.append(taxMain);

                if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                    taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }
                double term=0;
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    term = ExportRecordHandler.appendTermDetailsCreditNote(accCreditNoteDAOobj, authHandlerDAOObj, row.getCreditNote(), table1, currencyid, mode, CompanyID);
                }
                if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    term = ExportRecordHandler.appendTermDetailsDebitNote(accDebitNoteobj, authHandlerDAOObj, row1.getDebitNote(), table1, currencyid, mode, CompanyID);
                }
                cell3 = createCell("GST", local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                //cell3.setColspan(6);
                table1.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total+term, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                cell3.setFixedHeight(5f);
                table1.addCell(cell3);

                // addTableRow(mainTable, table);

                PdfPTable summeryTable = ExportRecordHandler.getSummeryTableForVRnet(table1, memo, false);
            //    receivedByTable1
                 String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(total+term)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
               
                PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                   
                receivedByTable1.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin() //memo table
                        - document.rightMargin()) * receivedByTable1.getWidthPercentage() / 100);
                receivedByTable1.calculateHeightsFast();
                float receivedByTable1Height = receivedByTable1.getTotalHeight();   //signature table
                receivedByTable2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * receivedByTable2.getWidthPercentage() / 100);
                receivedByTable2.calculateHeightsFast();
                float receivedByTable2Height = receivedByTable2.getTotalHeight();
                 // get main table (having items and global fields) height  table2
                mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                mainTable.calculateHeightsFast();
                float aboveProdTableContent = mainTable.getTotalHeight();
                 // get main table (having items and global fields) height 
                summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                summeryTable.calculateHeightsFast();
                float summeryTableHeight = summeryTable.getTotalHeight();
                table2.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                        - document.rightMargin()) * table2.getWidthPercentage() / 100);
                table2.calculateHeightsFast();
                float table2Height = table2.getTotalHeight();
                int blankRowHeight = 4;
                float addBlankRows = (document.getPageSize().getHeight()*pageNo - aboveProdTableContent -table2Height - summeryTableHeight -receivedByTable1Height -receivedByTable2Height - 110*pageNo) / blankRowHeight; //top+bottom=35+30+ 5+5 padding at end 10
                if(addBlankRows<0)
                    addBlankRows=10;
                int noOfCols = header.length; //No of Columns
                float BlankCellCnt = (float) (addBlankRows * noOfCols);
                BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                for (int j = 1; j <= BlankCellCnt; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRow) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    tableCloseLine.addCell(invcell);
                }
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                ExportRecordHandler.addTableRow(mainTable, tableCloseLine); //Break table after adding extra space
                document.add(mainTable);
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);


               
//                Discount totalDiscount = null;
//                double totaltax = 0, discountTotalQuotation = 0;
//                double totalAmount = 0;
//                double taxPercent = 0;
//                String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, RequestContextUtils.getLocale(request));
//                if(mainTax!=null){ //Get tax percent
//                    requestParams.put("transactiondate", entryDate);
//                    requestParams.put("taxid", mainTax.getID());
//                    KwlReturnObject result = accTaxObj.getTax(requestParams);
//                    List taxList = result.getEntityList();
//                    Object[] taxObj=(Object[]) taxList.get(0);
//                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
//                    mainTaxName=mainTax.getName();
//                }
//                totalAmount = total;
//                double term=0;
//                term= ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj,authHandlerDAOObj,sOrder , table, currencyid,mode);
//                total+=term;
//                totalAmount+=term;
//                if(sOrder.getDiscount() != 0) {
//                    if(!sOrder.isPerDiscount()){
//                            discountTotalQuotation = sOrder.getDiscount();
//                            total = total - sOrder.getDiscount();
//                            totalAmount = total;
//                    } else {
//                            discountTotalQuotation = total * sOrder.getDiscount()/100;
//                            total -= (total * sOrder.getDiscount()/100);
//                            totalAmount = total;
//                    }
//                }
//                totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
//                totalAmount = total + totaltax;

                
                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
                
             
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);
            } else if (mode == StaticValues.AUTONUM_INVOICE) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;
                InvoiceDetail row = null;
               
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                inv = (Invoice) cap.getEntityList().get(0);
                if (inv.getApprover() != null) {
                    approverName = inv.getApprover().getFirstName() + " " + inv.getApprover().getLastName();
                }
                currencyid = (inv.getCurrency() == null) ? currencyid : inv.getCurrency().getCurrencyID();
                if (inv.getTemplateid() != null) {
                    isCompanyLogo = false;
                     writer = PdfWriter.getInstance(document, baos);
                     writer.setPageEvent(new ExportRecord_VRNet.EndPage());
                     document.open();
                    if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                        isCompanyTemplateLogo = true;
                        if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(inv.getCompany())) {
                            isCompanyTemplateLogo = false;
                            isCompanyLogo = true;
                        }
                    } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                        isCompanyLogo = true;
                    }
                    String letterHead = inv.getTemplateid().getLetterHead();
                    String invoicePostText = inv.getPostText() == null ? "" : inv.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? inv.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)) ? inv.getTemplateid().getPreText() : CompanyPDFPRETEXT;
                    if (config.getBoolean("lHead")) {
                        if (!StringUtil.isNullOrEmpty(letterHead)) {
                            PdfPTable letterHeadTable = new PdfPTable(1);
                            ExportRecordHandler.getHtmlCell(letterHead, letterHeadTable, baseUrl);
                            letterHeadTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                            document.add(letterHeadTable);
                        }
                    }
                }
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);

                com = inv.getCompany();
                cEntry = inv.getCustomerEntry().getAccount();
                invno = inv.getInvoiceNumber();
                entryDate = inv.getJournalEntry().getEntryDate();
                dueDate = inv.getDueDate();
                shipDate = inv.getShipDate();
                shipvia = inv.getShipvia();
                fob = inv.getFob();
                poRefno = inv.getPoRefNumber() == null ? "" : inv.getPoRefNumber();
                //inv = (Invoice) session.get(Invoice.class, billid);
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);

                if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                        tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                String theader = cEntry == cash ? messageSource.getMessage("acc.accPref.autoCS", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) : messageSource.getMessage("acc.accPref.autoInvoice", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                datetheader = theader;

                if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE) //Invoice header Label as Tax Invoice
                {
                    theader = "Tax Invoice";
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE);
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});
                if (addShipTo) {
                    tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                }
                if (isCompanyTemplateLogo || storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if (addShipTo) {
                        if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                            table1.setWidths(new float[]{65, 35});
                        }
                        tab2 = ExportRecordHandler.getDateTable2(entryDate, invno, datetheader, formatter, invCell);
                    }
                }
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
//                isCompanyTemplateLogo = true;
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(5);
                mainCell11.setPaddingRight(5);
                mainCell11.setPaddingTop(5);
//                mainCell11.setPaddingBottom(5);
//                cell1.setPaddingTop(140);
                mainTable.addCell(mainCell11);


                PdfPCell mainCell12 = new PdfPCell(userTable2);

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                if (!StringUtil.isNullOrEmpty(preText)) {
                    ExportRecordHandler.getHtmlCell(preText.trim(), mainTable, baseUrl);
                }
                //}
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    filter_names.add("invoice.ID");
                    filter_params.add(inv.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                    customerName = inv.getCustomer() == null ? inv.getCustomerEntry().getAccount().getName() : inv.getCustomer().getName();
                    billAddress = CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(), true);
                    if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in invoice table 
                       billAddress = inv.getBillTo() != null ? inv.getBillTo() : "";
                    }
                            
//                    customerEmail = inv.getCustomer() != null ? inv.getCustomer().getEmail() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    if (inv.getMasterSalesPerson() != null) {   //if User Class returns the Null Valu
                        salesPerson = inv.getMasterSalesPerson() != null ? inv.getMasterSalesPerson().getValue() : "";
                    } else {  //if salesperson class has no username
                        salesPerson = "";
                    }
                    if (pref.isWithInvUpdate()) {
                        linkHeader = "SO/DO/CQ. No.";
                    } else {
                        linkHeader = "SO/CQ. No.";
                    }
                    terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
                    int termdays = (int)( (dueDate.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24));
                    HashMap<String, Object> termrequestParams = new HashMap<String, Object>();
                    termrequestParams.put("companyid", jsonObj.optString(Constants.companyKey));
                    termrequestParams.put("termdays", termdays);

                    KwlReturnObject termresult = accTermObj.getTerm(termrequestParams);
                    List<Term> termlist = termresult.getEntityList();
                    if(termlist.size()>0) {
                        terms = termlist.get(0).getTermname();
                    }
                    shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in invoice table 
                        shipAddress=inv.getShipTo()==null?"":inv.getShipTo();
                    }
                    itr = idresult.getEntityList().iterator();
                    itr1 = idresult.getEntityList().iterator();
                    memo = inv.getMemo();
                    porefno = inv.getPoRefNumber();
                }
                linkTo = "";
                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderFromInvoice(inv.getID(),pref.getCompany().getCompanyID());
                List list = doresult.getEntityList();
                if(list.size()>0){
                    Iterator ite1 = list.iterator();                
                    while(ite1.hasNext()){                        
                        String donumber = (String)ite1.next();
                        linkTo += donumber + ",";
                    }
                    if(linkTo.length()>0)  {
                        linkTo = linkTo.substring(0, linkTo.length()-1);
                    }
                }
                
                if (mode != StaticValues.AUTONUM_RFQ) {
                    PdfPTable addressMainTable = null;
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        addressMainTable = ExportRecordHandler.getAddressTableForBCHL(accPurchaseOrderobj, customerName, billAddress, "", pOrder, currencyid);
                    } else {
                        addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, "", billTo, shipAddress, addShipTo);
                    }
                    if (!addShipTo) {
                        PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj, salesPerson, entryDate, invno,linkTo, porefno, terms, currencyid, formatter);
                        PdfPCell cel3 = new PdfPCell(shipToTable);
                        cel3.setBorder(0);
                        addressMainTable.addCell(cel3);
                    }

                    mainCell12 = new PdfPCell(addressMainTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(1);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);

                    String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE) {
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTableForVRNET(headerDetailsForVrnet, referenceNumber, terms, dueDate, formatter, salesPerson);
                        mainCell12 = new PdfPCell(detailsTable);
                        mainCell12.setBorder(0);
//                        mainCell12.setPaddingTop(0);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                        mainCell12.setPaddingBottom(5);
                        if (addShipTo) {
                            mainTable.addCell(mainCell12);
                        }
                    } else {
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, referenceNumber, terms, dueDate, shipDate, formatter, isInclude, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                        mainCell12.setPaddingBottom(5);
                        mainTable.addCell(mainCell12);
                    }
                    
                }
                mainTable.setSplitLate(false); 
//                mainTable.setExtendLastRow(true);
                boolean companyFlag = (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_SALESORDER));
                List<String> headerList = new ArrayList<String>();
                if (addShipTo) {
                    headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                    if (!linkHeader.equalsIgnoreCase("")) {
                        headerList.add(linkHeader);
                    }
                } else {
                    headerList.add("Item");
                }
                //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name in header               
                if (!(companyFlag)) {
                    headerList.add(messageSource.getMessage("acc.rem.prodName", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                }

                 //  get product custom fields 
                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(com.getCompanyID(), Constants.Acc_Product_Master_ModuleId, 0));
                fieldrequestParams.put("relatedmoduleid", String.valueOf(StaticValues.AUTONUM_INVOICE));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                KwlReturnObject kwlcustomColumn = accAccountDAOobj.getFieldParams(fieldrequestParams);
                List<FieldParams> customColList = kwlcustomColumn.getEntityList();
                headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                HashMap<String, Integer> customMap = new HashMap();
                List<String> customHeaderList = new ArrayList();
                for (FieldParams field : customColList) {
                    //headerList.add(field.getFieldlabel());
                   customHeaderList.add(field.getFieldlabel());
//                   String[] relatedTo = field.getRelatedmoduleid().split(",");
//                    String[] relatedToWidth = field.getRelatedmodulepdfwidth()!=null ? field.getRelatedmodulepdfwidth().split(",") : "".split(",");
//                    for(int customCnt=0; customCnt<relatedTo.length; customCnt++) {
//                        if(relatedTo[customCnt].equals(String.valueOf(StaticValues.AUTONUM_INVOICE))) {
//                            if(relatedToWidth.length > customCnt && !StringUtil.isNullOrEmpty(relatedToWidth[customCnt])) {
//                                customMap.put(field.getFieldlabel(), Integer.parseInt(relatedToWidth[customCnt]));
//                            } else {
//                                customMap.put(field.getFieldlabel(), 0);
//                            }
//                        }
//                    }
               }
                
                headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))));
                if (mode != StaticValues.AUTONUM_RFQ) {
                    headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                    headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, StringUtil.getLocale(jsonObj.optString(Constants.language))) + " " + authHandlerDAOObj.getCurrency(currencyid));
                }

                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                PdfPTable table = null;
//                if (mode == StaticValues.AUTONUM_RFQ) {
//                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    productHeaderTableGlobalNo = 1;
//                } else {
//                    table = ExportRecordHandler.getTable(linkHeader, true);
//                    productHeaderTableGlobalNo = 2;
//                }
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                    productHeaderTableGlobalNo = 6;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }
                
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], local_fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
//                if (mode == StaticValues.AUTONUM_RFQ) {
//                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                } else {
//                    table = ExportRecordHandler.getTable(linkHeader, true);
//                }

                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }
//                table.setExtendLastRow(true);
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.companyKey, CompanyID);
                requestParams.put(Constants.globalCurrencyKey, currencyid);
                requestParams.put(Constants.df, formatter);
                
                
                BillingInvoiceDetail row1 = null;
                BillingSalesOrderDetail row3 = null;
                BillingPurchaseOrderDetail row4 = null;
                SalesOrderDetail row5 = null;
                PurchaseOrderDetail row6 = null;
                QuotationDetail row7 = null;
                VendorQuotationDetail row8 = null;
                RequestForQuotationDetail row9 = null;
                int index = 0;
                
              while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                    Phrase phrase1 = new Phrase();
                    Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
//                    String linkTo = "-";
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            prodDesc = row.getDescription();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInventory().getProduct().getDescription();
                            }
                        }
                        prodName = row.getInventory().getProduct().getName();
                        if (!addShipTo) {
                            if (row.getDeliveryOrderDetail() != null && !StringUtil.isNullOrEmpty(row.getDeliveryOrderDetail().getPartno().trim())) {
                                String partno = row.getDeliveryOrderDetail().getPartno();
//                            prodDesc += "\n\n Part No. :";
                                partNo = partno;
                            }
                        }
                        quantity = row.getInventory().getQuantity();
                        rate = row.getRate();
                        discount = row.getDiscount();
//                        uom = row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getName();
                        uom = row.getInventory().getUom()==null?row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA():row.getInventory().getUom().getNameEmptyforNA();
                        if (row.getDeliveryOrderDetail() != null) {
                            linkTo = row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                        } else if (row.getSalesorderdetail() != null) {
                            linkTo = row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber();
                        } else if (row.getQuotationDetail() != null) {
                            linkTo = row.getQuotationDetail().getQuotation().getquotationNumber();
                        }
                    }

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (addShipTo) {
                        if (!linkHeader.equalsIgnoreCase("")) {
                            invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                        }
                    }
                    //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name 
                    if (!(companyFlag)) {
                        invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    }
                    table.setSplitLate(false); 
                    AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), row.getInventory().getProduct().getID());
                    if (obj != null) {
                        productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        for (String varEntry : customHeaderList) {
                            boolean isBlankVal = true;
                            if(variableMap.containsKey("Custom_"+varEntry)) {
                                String coldata = variableMap.get("Custom_"+varEntry).toString();
                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                     invcell = createCell(prodDesc + "\n\n Part No. :" +coldata, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                                      invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc + "<br/> Part No. :" +coldata,baseUrl));
                                      table.addCell(invcell); // if product have part no
                                    isBlankVal = false;
                                } 
                            }
                            if(isBlankVal){ // product have empty part no
                                 invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                                     table.addCell(invcell);
                            }
                        }
                    } else {//if product have no custom fild part no
//                        for (String varEntry : customHeaderList) {
                          // invcell = createCell(prodDesc , fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                           invcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));
                           table.addCell(invcell);
//                        }
                    }
//                    if (!addShipTo) {
//                        invcell = createCell(partNo, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
//                    }

                    //String qtyStr = Double.toString(quantity);
                    // if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                    String qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    //}
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                            bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, baseCurrency, entryDate, 0);
                            double rateInBase = (Double) bAmt.getEntityList().get(0);
                            rate = rateInBase;
                        }
                        invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

                        amount1 = rate * quantity;

                        invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

                        if (discount != null) {
                            amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (row.getDiscount().getDiscountValue()) : (row1.getDiscount().getDiscountValue());
                        }
                        if (discountQuotation != 0) {
                            amount1 -= discountQuotation;
                        }
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName = "";
                        if (row != null && row.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        if (discount != null || discountQuotation != 0) {  //For Discount Row
                            table = ExportRecordHandler.getDiscountRowTableForVRNet(authHandlerDAOObj, table, currencyid, discount, 
                                    discountQuotation, mode, linkHeader, customMap.size(), CompanyID);
                        }

                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                            table = ExportRecordHandler.getTaxRowTableForVRnet(authHandlerDAOObj, table, rowTaxName, currencyid, amount1, 
                                    rowTaxPercent, mode, linkHeader, customMap.size(),rowTaxAmount);
                        }
                        amount1 += rowTaxAmount;//amount1 += amount1 * rowTaxPercent / 100;
                        total += amount1;
                        
                    }     
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    if (companyFlag) {
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);   //create table with 6 columns for extra row and Bottom 
                        } else {
                            table = ExportRecordHandler.getBlankTableReportForNonINVOICE(); //create table with 5 columns for extra row and Bottom
                          }
                    }   
                    float RowCellCnt = (float) (5 * headerList.size());
                        RowCellCnt = RowCellCnt - (RowCellCnt % (headerList.size()));
                     for (int j = 1; j <=RowCellCnt ; j++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(invcell);
                    }                  
                    ExportRecordHandler.addTableRow(mainTable, table);  
//                    if (mode == StaticValues.AUTONUM_RFQ) {
//                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    } else {
//                        table = ExportRecordHandler.getTable(linkHeader, true);
//                    }
                    ExportRecordHandler.addTableRow(mainTable, table);  
//                    if (mode == StaticValues.AUTONUM_RFQ) {
//                        table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                    } else {
//                        table = ExportRecordHandler.getTable(linkHeader, true);
//                    }
                    //for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                    if (companyFlag) {
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            table = ExportRecordHandler.getBlankTableReportForVRNetINVOICE(customMap);   //create table with 6 columns for extra row and Bottom 
                        } else {
                            table = ExportRecordHandler.getBlankTableReportForNonINVOICE(); //create table with 5 columns for extra row and Bottom
                          }
                    }                   
                }

                    Discount totalDiscount = null;
                    double totaltax = 0, discountTotalQuotation = 0;
                    double totalAmount = 0;
                    double taxPercent = 0;
                    String mainTaxName = messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                    boolean isZero = true;
		    if (mode != StaticValues.AUTONUM_RFQ) {
			if (mainTax != null) { //Get tax percent
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", mainTax.getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            mainTaxName = mainTax.getName();
                        }
                        if (mode == StaticValues.AUTONUM_INVOICE) {
                            ExportRecordHandler.appendTermDetails(accInvoiceDAOobj, authHandlerDAOObj, inv, table, currencyid, mode, CompanyID);
                            totalDiscount = inv.getDiscount();
                            totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : 0;
                            mainTaxName = inv.getTax() != null ? inv.getTax().getName() : "";
                            totalAmount = inv.getCustomerEntry().getAmount();
                        }

                        if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discountTotalQuotation, currencyid, CompanyID);
                            if(discountTotalQuotation>0.0)
                                isZero = false;
                        } else {
                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, totalDiscount, currencyid, CompanyID);
                            if(totalDiscount!=null && (totalDiscount.getDiscountValue()>0.0))
                                isZero = false;
                        } 

	            }
                      //table = getBlankTable();
                    table1 = new PdfPTable(2);
                    table1.setWidthPercentage(100);
                    table1.setWidths(new float[]{50, 50});
                     PdfPTable summeryTable=null; 
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        //cell3.setColspan(6);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        
                        if (mode != StaticValues.AUTONUM_PURCHASEORDER || mode != StaticValues.AUTONUM_SALESORDER) {
                            cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                            //cell3.setColspan(6);
                            if(!isZero) {
                                table1.addCell(cell3);
                                invcell = ExportRecordHandler.calculateVRNetDiscount(authHandlerDAOObj, totalDiscount, currencyid, false, CompanyID);
                                invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                invcell.setBorder(Rectangle.BOX);
                                invcell.setPadding(5);
                                table1.addCell(invcell);
                            }
                        }
                        StringBuffer taxNameStr = new StringBuffer();

                        taxNameStr.append(mainTaxName);
                        
                        if(StringUtil.isNullOrEmpty(taxNameStr.toString())) {
                            taxNameStr = new StringBuffer(messageSource.getMessage("acc.rem.vrnet.196", null,StringUtil.getLocale(jsonObj.optString(Constants.language))));
                        }
                        cell3 = createCell(taxNameStr.toString(), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);
                        cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), local_fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        //cell3.setColspan(6);
                        table1.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table1.addCell(cell3);

                        // addTableRow(mainTable, table);

                      summeryTable = ExportRecordHandler.getSummeryTableForVRnet(table1, mainTaxName, addShipTo);

                        mainCell12 = new PdfPCell(summeryTable);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                       

                    }
                    
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    PdfPTable tableCloseLine = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                    if (companyFlag) {
                          // get after Items table height
                        bottomTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * bottomTable.getWidthPercentage() / 100);
                        bottomTable.calculateHeightsFast();
                        float bottomTableHeight = bottomTable.getTotalHeight();
                        // get main table (having items and global fields) height 
                        summeryTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * summeryTable.getWidthPercentage() / 100);
                        summeryTable.calculateHeightsFast();
                        float summeryTableHeight = summeryTable.getTotalHeight();
                        // get main table (having items and global fields) height 
                        mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                                - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                        mainTable.calculateHeightsFast();
                        float aboveProdTableContent = mainTable.getTotalHeight();

                        int blankRowHeight = 4;
                        float addBlankRows = (document.getPageSize().getHeight()*pageNo - bottomTableHeight - aboveProdTableContent - summeryTableHeight - 85*pageNo) / blankRowHeight; //top+bottom=5+30+5 padding at end
                        int noOfCols = headerList.size();
                        if(addBlankRows<0)
                             addBlankRows=10; 
                        float BlankCellCnt = (float) (addBlankRows * noOfCols);
                        BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                        float lastRow = (float) Math.ceil(BlankCellCnt - noOfCols);

                        for (int j = 1; j <= BlankCellCnt; j++) {
                            invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            if (j > lastRow) {
                                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                            }
                            tableCloseLine.addCell(invcell);
                        }

                    }
                    
                   
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                   
                    ExportRecordHandler.addTableRow(mainTable, tableCloseLine);
                     document.add(mainTable);
                    //Break table after adding extra space
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                   
                    

                    if (mode != StaticValues.AUTONUM_RFQ) {
                        mainTable.addCell(mainCell12);

                    }
            } else if (mode == StaticValues.AUTONUM_GOODSRECEIPT|| mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document,writer);                
                GoodsReceipt gr=null;
                BillingGoodsReceipt gr1=null;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
                       if(gr.getApprover()!=null)
                        approverName=gr.getApprover().getFirstName() +" "+gr.getApprover().getLastName();
                    if (gr.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(gr.getTemplateid().getConfigstr());
                        // document = getTemplateConfig(document,writer);
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(gr.getCompany())) {
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                    }
                }
            
                com =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCompany():gr1.getCompany();
                company = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), com.getCompanyID());
                ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }
                
                KWLCurrency rowCurrency = (mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCurrency():gr1.getCurrency());
                String rowCurrenctID = rowCurrency==null?currencyid:rowCurrency.getCurrencyID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                 if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{74, 26});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else {
                    table1.setWidths(new float[]{50, 50});
                    if (isCompanyLogo) {
                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                    } else {
                        tab1 = new PdfPTable(1);
                    }
                }
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref=(CompanyAccountPreferences)kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),com.getCompanyID());
                Account cash=pref.getCashAccount();
                Account vEntry;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vEntry = gr.getVendorEntry().getAccount();
                } else {
                    vEntry = gr1.getVendorEntry().getAccount();
                }
                String theader = vEntry==cash?messageSource.getMessage("acc.accPref.autoCP", null, StringUtil.getLocale(jsonObj.optString(Constants.language))):messageSource.getMessage("acc.accPref.autoVI", null, StringUtil.getLocale(jsonObj.optString(Constants.language)));
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);
                String grno=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getGoodsReceiptNumber():gr1.getBillingGoodsReceiptNumber();
                entryDate=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getJournalEntry().getEntryDate():gr1.getJournalEntry().getEntryDate();
                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,grno,theader,formatter);   
                if (isCompanyTemplateLogo) {
                    tab2= ExportRecordHandler.getDateTable2(entryDate,grno,theader,formatter,invCell);   

                }
                
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    tab3 = ExportRecordHandler.getCompanyInfo(company);
                } else {
                    userTable2.setWidths(new float[]{80, 20});
                    tab3 = new PdfPTable(1);
                }
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    tab1 = tab3;
                    tab3 = new PdfPTable(1);
                }
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

                cell1 = new PdfPCell(tab3);
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
                if (!isCompanyTemplateLogo && !(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                mainTable.addCell(mainCell12);
                }

                String vendorName = "";
                String vendorEmail = "";
                String vendorTerms = "";
                billTo="Supplier";
                String Address="";
                isInclude=false; //Hiding or Showing P.O. NO field in single PDF 
                String linkHeader="";
                
                headerDetails = new String []{"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                if(mode==StaticValues.AUTONUM_GOODSRECEIPT){
                    vendorName = gr.getVendor()==null?gr.getVendorEntry().getAccount().getName():gr.getVendor().getName();
                    vendorEmail= gr.getVendor()==null?"":gr.getVendor().getEmail();
                    vendorTerms=gr.getVendor()==null?"":gr.getVendor().getDebitTerm().getTermname();
                    if(pref.isWithInvUpdate()){
                        linkHeader="PO/GR/VQ No.";
                        isInclude=false;
                    }else{
                        linkHeader="PO/VQ. No.";
                        isInclude=false;
                    }                    
                    Address= CommonFunctions.getBillingShippingAddressWithAttn(gr.getBillingShippingAddresses(), true);
                    dueDate=gr.getDueDate();
                    shipDate=gr.getShipDate();        
                    shipvia=gr.getShipvia();        
                    fob=gr.getFob();        
                }
                
                HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                grRequestParams.put("order_by", order_by);
                grRequestParams.put("order_type", order_type);

                idresult = null;
                if(mode != StaticValues.AUTONUM_BILLINGINVOICE||mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                    if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                        filter_names.add("billingGoodsReceipt.ID");
                        filter_params.add(gr1.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                    }
                    else{
                        filter_names.add("goodsReceipt.ID");
                        filter_params.add(gr.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        if(isExpenseInv){
                             idresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                             isInclude=false;
                        }
                        else
                            idresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
                    }
                } else {
                    filter_names.add("billingGoodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                }
                itr = idresult.getEntityList().iterator();  
                
                if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                
                PdfPTable addressMainTable=ExportRecordHandler.getAddressTable(vendorName,Address,vendorEmail,billTo,"",true);

                mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
                
                
                String referenceNumber="";
                if(!isExpenseInv){
                    referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);                    
                }                
                PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,referenceNumber,vendorTerms,dueDate,shipDate,formatter,isInclude, shipvia, fob);
                
                mainCell12 = new PdfPCell(detailsTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                
                PdfPTable table;
                PdfPCell grcell = null;
                if(isExpenseInv){
                    String[] header  = {"S.No.","Account",  "PRICE "+authHandlerDAOObj.getCurrency(rowCurrenctID), "LINE TOTAL "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                     table = new PdfPTable(4);
                    globalTableHeader=header;
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{10, 40, 25, 25});
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                }
                else{

                    String[] header;
                    if(linkHeader.equalsIgnoreCase("")){
                        header=new String[]{messageSource.getMessage("acc.cnList.Sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))),messageSource.getMessage("acc.rem.prodName", null, StringUtil.getLocale(jsonObj.optString(Constants.language))),messageSource.getMessage("acc.rem.prodDesc", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), messageSource.getMessage("acc.rem.187", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), messageSource.getMessage("acc.rem.188", null,StringUtil.getLocale(jsonObj.optString(Constants.language)))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID), messageSource.getMessage("acc.rem.212", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                    }else{
                        header=new String[]{messageSource.getMessage("acc.cnList.Sno", null, StringUtil.getLocale(jsonObj.optString(Constants.language))),linkHeader,messageSource.getMessage("acc.rem.prodName", null, StringUtil.getLocale(jsonObj.optString(Constants.language))),messageSource.getMessage("acc.rem.prodDesc", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), messageSource.getMessage("acc.rem.187", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), messageSource.getMessage("acc.rem.188", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID), messageSource.getMessage("acc.rem.212", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                    }                                        
                    table=ExportRecordHandler.getTable(linkHeader,true);
                    globalTableHeader=header;
                     productHeaderTableGlobalNo=2;
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }                   
                }
                
                GoodsReceiptDetail row=null;
                BillingGoodsReceiptDetail row1=null;
                ExpenseGRDetail exprow=null;
                int index = 0;
                while (itr.hasNext()) {
                    if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                        if(isExpenseInv){
                             exprow = (ExpenseGRDetail) itr.next();
                        }     
                        else{
                            row = (GoodsReceiptDetail) itr.next();
                            if (row.getGoodsReceiptOrderDetails() != null) {
                                linkTo = row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber();
                            } else if (row.getPurchaseorderdetail() != null) {
                                linkTo = row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber();
                            } else if (row.getVendorQuotationDetail() != null) {
                                linkTo = row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber();
                            }
                        }
                    }else {
                        row1 = (BillingGoodsReceiptDetail) itr.next();
                        if (row1.getPurchaseOrderDetail() != null) {
                            linkTo = row1.getPurchaseOrderDetail().getPurchaseOrder().getPurchaseOrderNumber();
                        }
                     }
                    
                    if(isExpenseInv){
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount(exprow.getRate(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);                        
                        double amount1 = exprow.getRate();
                        grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {   //For Discount Row
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("Discount", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , exprow.getDiscount(), rowCurrenctID, CompanyID);
                            grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            grcell.setPadding(5);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                            
                            amount1 -= exprow.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        boolean isRowTaxApplicable=false;
                        double rowTaxAmount=0;
                        String rowTaxName="";
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(jsonObj);
                        if (exprow!= null&&exprow.getTax() != null) { 
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=exprow.getTax().getName();
                            if (exprow.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = exprow.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell(rowTaxName + "  Tax", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

//                            invcell = createCell(ExportRecordHandler.getFormattedAmount(amount1 * rowTaxPercent / 100), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            invcell = createCell(authHandler.formattedAmount(rowTaxAmount, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);        
                        }
                        amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                        total += amount1;

                    }else{
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        if(!linkHeader.equalsIgnoreCase("")){
                            grcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);                        
                        }
                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                            //    grcell = ExportRecordHandler.getProductNameWithDescriptionPhrase(row.getInventory().getProduct());
                            grcell = createCell(row.getInventory().getProduct().getName(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            if(!StringUtil.isNullOrEmpty(row.getDescription())){
                                grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            }else if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                grcell = createCell(row.getInventory().getProduct().getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            } else {
                                grcell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            }                            
                            table.addCell(grcell);

                        } else {
                            grcell = createCell(row1.getProductDetail(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            grcell = createCell("-", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                        }
//                        grcell = createCell(mode == StaticValues.AUTONUM_GOODSRECEIPT ? getProductNameWithDescription(row.getInventory().getProduct()) : row1.getProductDetail(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        //table.addCell(grcell);
                        String qtyString="";
                        if(mode == StaticValues.AUTONUM_GOODSRECEIPT){
                            qtyString=Double.toString(row.getInventory().getQuantity());
                        }else if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                                qtyString=Double.toString((Double)row1.getQuantity());                                
                        }                       
                        grcell = createCell(qtyString+ " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getUom()==null?(row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getInventory().getUom().getNameEmptyforNA()) : ""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount((mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate()), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
//                        grcell = calculateDiscount(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount(), rowCurrenctID);
//                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                        grcell.setPadding(5);
//                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        table.addCell(grcell);
                        double amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * ( row.getInventory().getQuantity()) : row1.getRate() * row1.getQuantity();
                        grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        Discount disc = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount();
                        if (disc != null) {                              
                            table=ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table,rowCurrenctID,disc,0,mode,linkHeader); //For Discount Row                   
                            amount1 -= mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        double rowTaxAmount=0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName="";
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(jsonObj);
                        if (row!= null&&row.getTax() != null) {
                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1*rowTaxPercent/100;
                            }

                        }
                        else if (row1!= null&&row1.getTax() != null) {
                            requestParams.put("transactiondate", row1.getBillingGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row1.getTax().getName();
                            if (row1.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1*rowTaxPercent/100;
                            }
                        }
                         if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
                            table=ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,rowCurrenctID,amount1,rowTaxPercent,mode,linkHeader,rowTaxAmount);
                        }
                         
                         ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                        if (isExpenseInv) {
                            table = new PdfPTable(4);
                            table.setWidthPercentage(100);
                            table.setWidths(new float[]{10, 40, 25, 25});

                        } else {
                            table = ExportRecordHandler.getTable(linkHeader, true);
                        }
                         
                        amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                        total += amount1;                         
                    }
                }
                int cellCount = 0, lastRowCellCount = 0;
                if (isExpenseInv) {
                    cellCount = 56;lastRowCellCount = 52;
                    for (int j = 1; j <=cellCount; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        if (j > lastRowCellCount) {
//                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
//                        }
                        table.addCell(grcell);
                    }
                } else {
                    cellCount = (linkHeader.equalsIgnoreCase(""))?84:98;
                    lastRowCellCount = linkHeader.equalsIgnoreCase("")?78:91;
                    for (int j = 1; j <=cellCount; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        if (j > lastRowCellCount) {
//                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
//                        }
                        table.addCell(grcell); 
                    }
                }

                if (true) {
                    int belowProdTableContent = 270;
                    mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                    mainTable.calculateHeightsFast();
                    float aboveProdTableContent = mainTable.getTotalHeight();
                    table.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * table.getWidthPercentage() / 100);
                    table.calculateHeightsFast();
                    int blankRowHeight = 4;
                    float addBlankRows = (document.getPageSize().getHeight() - belowProdTableContent - aboveProdTableContent - table.getTotalHeight()) / blankRowHeight;
                    int noOfCols = 7;
                    if(addBlankRows<0)
                        addBlankRows=10; 
                    int BlankCellCnt = (int) (addBlankRows * noOfCols);
                    BlankCellCnt = BlankCellCnt - (BlankCellCnt % noOfCols);
                    int lastRow = BlankCellCnt - noOfCols;
                    for (int j = 1; j <= BlankCellCnt; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > lastRow) {
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(grcell);
                    }
                }
                ExportRecordHandler.addTableRow(mainTable, table);
                
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);
                
                table = new PdfPTable(2);
                table.setWidthPercentage(100);                
                table.setWidths(new float[]{50,50});
               
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);                
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode, CompanyID);
                }
                boolean isDiscZero = mode==StaticValues.AUTONUM_GOODSRECEIPT? (gr.getDiscount()!=null && gr.getDiscount().getDiscountValue()>0 ? false : true) : (gr1.getDiscount()!=null && gr1.getDiscount().getDiscountValue()>0 ? false : true);
                if(!isDiscZero) {
                    cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.195", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getDiscount():gr1.getDiscount(), rowCurrenctID, CompanyID);
                    grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    grcell.setBorder(Rectangle.BOX);
                    grcell.setPadding(5);
                    table.addCell(grcell); 
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.vrnet.196", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTaxEntry().getAmount():0):(gr1.getTaxEntry()!=null?gr1.getTaxEntry().getAmount():0),rowCurrenctID, CompanyID), fontSmallRegular,Element.ALIGN_RIGHT, Rectangle.BOX,5);
                table.addCell(cell3);                
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, StringUtil.getLocale(jsonObj.optString(Constants.language))), fontSmallBold,Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getVendorEntry().getAmount()):(gr1.getVendorEntry().getAmount()), rowCurrenctID, CompanyID), fontSmallRegular,Element.ALIGN_RIGHT, Rectangle.BOX,5);
                table.addCell(cell3);

                String mainTaxName=mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTax().getName():""):(gr1.getTaxEntry()!=null?gr1.getTax().getName():"");
                PdfPTable summeryTable=ExportRecordHandler.getSummeryTable(table,mainTaxName,addShipTo);

                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);                
                mainTable.addCell(mainCell12);
                
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                double totalamount=0;
                if(gr!=null)
                    totalamount=gr.getVendorEntry().getAmount();
                else if(gr1!=null)
                    totalamount=gr1.getVendorEntry().getAmount();
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency,countryLanguageId, CompanyID);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, StringUtil.getLocale(jsonObj.optString(Constants.language)))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(2);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);

            }
            
//            ExportRecordHandler.getHtmlCell("<br/>", mainTable, baseUrl);
            if(mode==StaticValues.AUTONUM_QUOTATION|| mode ==StaticValues.AUTONUM_SALESORDER ) {
                PdfPCell thanksGivingTableCell = new PdfPCell(thanksGivingTable);
                thanksGivingTableCell.setBorder(0);
                mainTable.addCell(thanksGivingTableCell);
            } else if(mode==StaticValues.AUTONUM_DEBITNOTE|| mode ==StaticValues.AUTONUM_CREDITNOTE ) {
                PdfPCell receivedByTableCell1 = new PdfPCell(receivedByTable1);
                receivedByTableCell1.setBorder(0);
                receivedByTableCell1.setPadding(10); 
                mainTable.addCell(receivedByTableCell1);
                 PdfPCell receivedByTableCell2= new PdfPCell(receivedByTable2);
                receivedByTableCell2.setBorder(0);
                mainTable.addCell(receivedByTableCell2);
             }else  if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT 
                    && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT 
                    && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT 
                    && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE 
                    && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    mainTable.addCell(bottomCell);
            }

            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Export:" + ex.getMessage(), ex);
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
                    ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
               if (mainTableGlobal != null && modeNo!=2) {
                    mainTableGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    mainTableGlobal.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight()-20 , writer.getDirectContent());
                    mainTableGlobal = null;
                }
                if (tableClosedLineGlobal != null) {
                    tableClosedLineGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    tableClosedLineGlobal.writeSelectedRows(0, 10, document.leftMargin(), document.bottomMargin() + 45, writer.getDirectContent());
                    tableClosedLineGlobal = null;
                }
              
                if (config != null && config.getBoolean("pageBorder")) {
                    int bmargin = 15;  //border margin
                    PdfContentByte cb = writer.getDirectContent();
                    cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.stroke();
                }

            } catch (Exception e) {
                Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                pageNo=document.getPageNumber();
                if(document.getPageNumber()>1)
                {   
                if (mainTableGlobal != null && modeNo==2) {
                    mainTableGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    mainTableGlobal.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight()-20 , writer.getDirectContent());
                    mainTableGlobal = null;
                }
                }
              

            } catch (Exception e) {
                Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void addTableHeader(String currencyValue, PdfPTable mainTable, PdfPTable table, PdfPCell invcell) throws DocumentException, SessionExpiredException {
        for (int i = 0; i < globalTableHeader.length; i++) {
            invcell = new PdfPCell(new Paragraph(globalTableHeader[i], local_fontSmallBold));
            invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            invcell.setBackgroundColor(Color.LIGHT_GRAY);
            invcell.setPadding(2);
            if (productHeaderTableGlobalNo == 4) {
                invcell.setPaddingBottom(10);
            }          
            
            
            table.addCell(invcell);
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
            for (int j = 1; j <= globalTableHeader.length*12; j++) {
                invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                if (j > (globalTableHeader.length*11)) {
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                }
                table.addCell(invcell);
            }
        }
    }

    public void getHeaderFooter(Document document) throws ServiceException {
        try {

            java.util.Date dt = new java.util.Date();
            String date = "yyyy-MM-dd";
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
            String DateStr = dtf.format(dt);
            java.awt.Color tColor = null;
            if (config != null) {
                tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
                // -------- header ----------------
                header = new PdfPTable(3);
                header.setWidthPercentage(100);
                header.setWidths(new float[]{20, 60, 20});
                String HeadDate = "";
                if (config.getBoolean("headDate")) {
                    HeadDate = DateStr;
                }
                PdfPCell headerDateCell = new PdfPCell(fontFamilySelector.process(HeadDate, FontContext.SMALL_TEXT, tColor));//fontSmallRegular));
                headerDateCell.setBorder(0);
                headerDateCell.setPaddingBottom(4);
                headerDateCell.setHorizontalAlignment(PdfCell.ALIGN_LEFT);
                header.addCell(headerDateCell);

                PdfPCell headerNotecell = new PdfPCell(fontFamilySelector.process(config.getString("headNote"), FontContext.HEADER_NOTE, tColor));
                headerNotecell.setBorder(0);
                headerNotecell.setPaddingBottom(4);
                headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
                header.addCell(headerNotecell);

                String HeadPager = "";
                if (config.getBoolean("headPager")) {
                    HeadPager = String.valueOf(document.getPageNumber());//current page no
                }
                PdfPCell headerPageNocell = new PdfPCell(fontFamilySelector.process(HeadPager, FontContext.HEADER_NOTE, tColor));// fontSmallRegular));
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

            if (isAttachProductTable) {
                mainTableGlobal = new PdfPTable(1);
                PdfPTable table = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                addTableHeader(globalCurrencyValue, mainTableGlobal, table, invcell);
                ExportRecordHandler.addTableRowGlobal(mainTableGlobal, table); //Break table after adding header row                
                isAttachProductTable = false;
            }

            if (isFromProductTable) {
                isAttachProductTable = true;
                tableClosedLineGlobal = new PdfPTable(1);
                PdfPTable table = ExportRecordHandler.getTableForNextPage(productHeaderTableGlobalNo, linkHeader);
                addingFooterClosedLine(table);
                ExportRecordHandler.addTableRow(tableClosedLineGlobal, table);

            }
            footer = new PdfPTable(3);
            PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
            footerSeparator.setBorder(PdfPCell.BOX);
            footerSeparator.setPadding(0);
            footerSeparator.setColspan(3);
            footer.addCell(footerSeparator);
            footer.setWidthPercentage(100);
            footer.setWidths(new float[]{20, 60, 20});
            String PageDate = "";
            if (config != null) {
                if (config.getBoolean("footDate")) {
                    PageDate = DateStr;
                }
                PdfPCell pagerDateCell = new PdfPCell(fontFamilySelector.process(PageDate, FontContext.SMALL_TEXT, tColor));//fontSmallRegular));
                pagerDateCell.setBorder(0);
                pagerDateCell.setHorizontalAlignment(PdfCell.ALIGN_LEFT);
                footer.addCell(pagerDateCell);

                PdfPCell footerNotecell = new PdfPCell(fontFamilySelector.process(CompanyPDFFooter, FontContext.FOOTER_NOTE, tColor));// fontSmallRegular));
                footerNotecell.setBorder(0);
                footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
                footer.addCell(footerNotecell);

                String FootPager = "";
                if (config.getBoolean("footPager")) {
                    FootPager = String.valueOf(document.getPageNumber());//current page no
                }
                PdfPCell footerPageNocell = new PdfPCell(fontFamilySelector.process(FootPager, FontContext.SMALL_TEXT, tColor));// fontSmallRegular));
                footerPageNocell.setBorder(0);
                footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
                footer.addCell(footerPageNocell);
            }
            // -------- footer end   -----------
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.getHeaderFooter", e);
        }
    }
}
