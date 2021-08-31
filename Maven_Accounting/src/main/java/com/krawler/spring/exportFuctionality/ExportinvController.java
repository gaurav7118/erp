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

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingCreditNote;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingDebitNote;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PaymentDetail;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONException;
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
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;



public class ExportinvController extends MultiActionController {

    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 9, Font.NORMAL, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Helvetica", 11, Font.BOLD, Color.BLACK);
    private static Font fontTblMediumBold = FontFactory.getFont("Helvetica", 9, Font.BOLD, Color.BLACK);
    private static Font fontBig = FontFactory.getFont("Helvetica", 22, Font.UNDERLINE, Color.BLACK);
    private static String imgPath = "";

    private authHandlerDAO authHandlerDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    private static void addHeaderFooter(Document document,PdfWriter writer) throws DocumentException,ServiceException{
        fontSmallRegular.setColor(Color.BLACK);
        java.util.Date dt = new java.util.Date();
        String date="yyyy-MM-dd";
        java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
        String DateStr = dtf.format(dt);
        PdfPTable footer = new PdfPTable(1);
        PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
        footerSeparator.setBorder(PdfPCell.BOX);
        footerSeparator.setPadding(0);
        footerSeparator.setColspan(3);
        footer.addCell(footerSeparator);

        String PageDate = DateStr;
        PdfPCell pagerDateCell = new PdfPCell(new Phrase("Generated Date : "+ PageDate, fontSmallRegular));
        pagerDateCell.setBorder(0);
        pagerDateCell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
        footer.addCell(pagerDateCell);
        // -------- footer end   -----------
       try {
        Rectangle page = document.getPageSize();
        footer.setTotalWidth(page.getWidth()-document.leftMargin()-document.rightMargin());
        footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() ,writer.getDirectContent());
    } catch (Exception e) {
        throw new ExceptionConverter(e);
    }
    }

    public ModelAndView getExportInv(HttpServletRequest request, HttpServletResponse response) throws IOException  {
        if (sessionHandlerImpl.isValidSession(request, response)) {
            //Session session = null;
            try {
              //  session = HibernateUtil.getCurrentSession();
                String companyid = sessionHandlerImpl.getCompanyid(request);
                double amount=Double.parseDouble(request.getParameter("amount"));
                int mode =Integer.parseInt(request.getParameter("mode"));
                String billid=request.getParameter("bills");
                String logoPath=ProfileImageServlet.getProfileImagePath(request, true,"images/deskera-accounting-big-without-username.gif");
                String currencyid=sessionHandlerImpl.getCurrencyID(request);
                DateFormat formatter=authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request),true);
                ByteArrayOutputStream baos=createPdf(currencyid, billid, formatter, mode,amount,getServletContext().getRealPath(""),logoPath, companyid);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + sessionHandlerImpl.getCompanyName(request) + ".pdf\"");
                response.setContentType("application/octet-stream");
                response.setContentLength(baos.size());
                response.getOutputStream().write(baos.toByteArray());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } catch (Exception ex) {
                
            } finally {
//                HibernateUtil.closeSession(session);
            }
        } else {
            response.getOutputStream().println("{\"valid\": false}");
        }
        return new ModelAndView("", "", "");
    }

    public ByteArrayOutputStream createPdf(String currencyid, String billid, DateFormat formatter, int mode, double amount, String contextpath, String logoPath, String companyid) throws JSONException {
        ByteArrayOutputStream baos=null;
        try {
            if(mode == StaticValues.AUTONUM_GOODSRECEIPT) {

            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), billid);
            GoodsReceipt gRec = (GoodsReceipt) cap.getEntityList().get(0);
//            GoodsReceipt gRec = (GoodsReceipt) session.get(GoodsReceipt.class, billid);
                String[] header1 = {"DATE","GR NO"};
                String[] header2 = {"VENDOR","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
//                String[] values1 = {formatter.format(gRec.getJournalEntry().getEntryDate()),gRec.getGoodsReceiptNumber()};
                String[] values1 = {formatter.format(gRec.getCreationDate()),gRec.getGoodsReceiptNumber()};
                String[] values2 = {gRec.getVendorEntry().getAccount().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),gRec.getMemo()};
                String[] values3 = new String[5];
//                String[] values4={Long.toString((gRec.getDueDate().getTime()-gRec.getJournalEntry().getEntryDate().getTime())/(3600000*24))+" Days",formatter.format(gRec.getDueDate())};
                String[] values4={Long.toString((gRec.getDueDate().getTime()-gRec.getCreationDate().getTime())/(3600000*24))+" Days",formatter.format(gRec.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "GoodsReceipt", gRec, gRec.getCompany(),contextpath,logoPath,formatter);
            }else if(mode==StaticValues.AUTONUM_DEBITNOTE){

                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                DebitNote deb = (DebitNote) cap.getEntityList().get(0);
//                DebitNote deb = (DebitNote) session.get(DebitNote.class, billid);
                Set<JournalEntryDetail> entryset=(deb.getJournalEntry() !=null)?deb.getJournalEntry().getDetails():null;
                Vendor vendor=new Vendor();
                Iterator itr=(entryset !=null)?entryset.iterator():null;
                if(itr!=null){
                    while(itr.hasNext()){
                        Account acc=((JournalEntryDetail)itr.next()).getAccount();
                        cap = kwlCommonTablesDAOObj.getObject(Vendor.class.getName(), acc.getID());
                        vendor = (Vendor) cap.getEntityList().get(0);
    //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                        if(vendor!=null)break;
                    }
                }
//                Date dnCreationDate = (deb.isNormalDN())?deb.getJournalEntry().getEntryDate():deb.getCreationDate();
                Date dnCreationDate = deb.getCreationDate();
                
                String[] header1 = {"DATE","DEBIT NO"};
                String[] header2 = {"VENDOR","AMOUNT","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","DISCOUNT AMOUNT"};
                String[] header4 =new String[0];
                String[] values1 = {formatter.format(dnCreationDate),deb.getDebitNoteNumber()};
                String[] values2 = {vendor.getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),deb.getMemo()};
                String[] values3 = new String[3];
                String[] values4 = new String[0];
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "debit", deb, deb.getCompany(),contextpath,logoPath,formatter);
            }else if(mode==StaticValues.AUTONUM_BILLINGDEBITNOTE){
//                BillingDebitNote deb = (BillingDebitNote) session.get(BillingDebitNote.class, billid);
                BillingDebitNote deb = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                Set<JournalEntryDetail> entryset=deb.getJournalEntry().getDetails();
                Vendor vendor=new Vendor();
                Iterator itr=entryset.iterator();
                while(itr.hasNext()){
                    Account acc=((JournalEntryDetail)itr.next()).getAccount();
//                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                    vendor=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),acc.getID());
                    if(vendor!=null)break;
                }
                String[] header1 = {"DATE","DEBIT NO"};
                String[] header2 = {"VENDOR","AMOUNT","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","DISCOUNT AMOUNT"};
                String[] header4 =new String[0];
                String[] values1 = {formatter.format(deb.getJournalEntry().getEntryDate()),deb.getDebitNoteNumber()};
//                String[] values2 = {vendor.getName(),ProfileHandler.getFormattedCurrency(amount,currencyid, session),deb.getMemo()};
                String[] values2 = {vendor.getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),deb.getMemo()};
                String[] values3 = new String[3];
                String[] values4 = new String[0];
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "billingdebit", deb, deb.getCompany(),contextpath,logoPath,formatter);
            }else if(mode==StaticValues.AUTONUM_CREDITNOTE){
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                CreditNote cre = (CreditNote) cap.getEntityList().get(0);
//                CreditNote cre = (CreditNote) session.get(CreditNote.class, billid);
                Set<JournalEntryDetail> entryset=(cre.getJournalEntry() != null)?cre.getJournalEntry().getDetails():null;
                Customer customer=new Customer();
                Iterator itr=(entryset!=null)?entryset.iterator():null;
                if(itr!=null){
                    while(itr.hasNext()){
                        Account acc=((JournalEntryDetail)itr.next()).getAccount();
                        cap = kwlCommonTablesDAOObj.getObject(Customer.class.getName(), acc.getID());
                        customer = (Customer) cap.getEntityList().get(0);
    //                    customer=(Customer)session.get(Customer.class,acc.getID());
                        if(customer!=null)break;
                    }
                }
                String[] header1 = {"DATE","CREDIT NO"};
                String[] header2 = {"CUSTOMER","AMOUNT","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","DISCOUNT AMOUNT"};
                String[] header4 =new String[0];
//                Date creDate = (cre.isNormalCN())?cre.getJournalEntry().getEntryDate():cre.getCreationDate();
                Date creDate = cre.getCreationDate();
                String[] values1 = {formatter.format(creDate),cre.getCreditNoteNumber()};
                String[] values2 = {customer.getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),cre.getMemo()};
                String[] values3 = new String[3];
                String[] values4 = new String[0];
                baos=createForm(currencyid, header1, header2, header3,header4,values1, values2, values3,values4, "credit", cre, cre.getCompany(),contextpath, logoPath,formatter);
            }else if(mode==StaticValues.AUTONUM_BILLINGCREDITNOTE){
//                BillingCreditNote cre = (BillingCreditNote) session.get(BillingCreditNote.class, billid);
                BillingCreditNote cre = (BillingCreditNote) kwlCommonTablesDAOObj.getClassObject(BillingCreditNote.class.getName(), billid);
                Set<JournalEntryDetail> entryset=cre.getJournalEntry().getDetails();
                Customer customer=new Customer();
                Iterator itr=entryset.iterator();
                while(itr.hasNext()){
                    Account acc=((JournalEntryDetail)itr.next()).getAccount();
//                    customer=(Customer)session.get(Customer.class,acc.getID());
                    customer=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),acc.getID());
                    if(customer!=null)break;
                }
                String[] header1 = {"DATE","CREDIT NO"};
                String[] header2 = {"CUSTOMER","AMOUNT","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","DISCOUNT AMOUNT"};
                String[] header4 =new String[0];
                String[] values1 = {formatter.format(cre.getJournalEntry().getEntryDate()),cre.getCreditNoteNumber()};
                String[] values2 = {customer.getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),cre.getMemo()};
                String[] values3 = new String[3];
                String[] values4 = new String[0];
                baos=createForm(currencyid, header1, header2, header3,header4,values1, values2, values3,values4, "billingcredit", cre, cre.getCompany(),contextpath, logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(PurchaseOrder.class.getName(), billid);
                PurchaseOrder po = (PurchaseOrder) cap.getEntityList().get(0);
//                PurchaseOrder po = (PurchaseOrder) session.get(PurchaseOrder.class, billid);
                String[] header1 = {"DATE","PO NO"};
                String[] header2 = {"VENDOR","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
                String[] values1 = {formatter.format(po.getOrderDate()),po.getPurchaseOrderNumber()};
                String[] values2 = {po.getVendor().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),po.getMemo()};
                String[] values3 = new String[5];
                String[] values4={Long.toString((po.getDueDate().getTime()-po.getOrderDate().getTime())/(3600000*24))+" Days",formatter.format(po.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "PurchaseOrder", po, po.getCompany(),contextpath,logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
//                BillingPurchaseOrder po = (BillingPurchaseOrder) session.get(BillingPurchaseOrder.class, billid);
                BillingPurchaseOrder po = (BillingPurchaseOrder) kwlCommonTablesDAOObj.getClassObject(BillingPurchaseOrder.class.getName(), billid);
                String[] header1 = {"DATE","PO NO"};
                String[] header2 = {"VENDOR","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
                String[] values1 = {formatter.format(po.getOrderDate()),po.getPurchaseOrderNumber()};
                String[] values2 = {po.getVendor().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),po.getMemo()};
                String[] values3 = new String[5];
                String[] values4={Long.toString((po.getDueDate().getTime()-po.getOrderDate().getTime())/(3600000*24))+" Days",formatter.format(po.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "BillingPurchaseOrder", po, po.getCompany(),contextpath,logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_SALESORDER) {
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(SalesOrder.class.getName(), billid);
                SalesOrder so = (SalesOrder) cap.getEntityList().get(0);
//                SalesOrder so = (SalesOrder) session.get(SalesOrder.class, billid);
                String[] header1 = {"DATE","SO NO"};
                String[] header2 = {"CUSTOMER","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
                String[] values1 = {formatter.format(so.getOrderDate()),so.getSalesOrderNumber()};
                String[] values2 = {so.getCustomer().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),so.getMemo()};
                String[] values3 = new String[5];
                String[] values4={Long.toString((so.getDueDate().getTime()-so.getOrderDate().getTime())/(3600000*24))+" Days",formatter.format(so.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4,"SalesOrder", so, so.getCompany(),contextpath,logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
//                BillingSalesOrder so = (BillingSalesOrder) session.get(BillingSalesOrder.class, billid);
                BillingSalesOrder so = (BillingSalesOrder) kwlCommonTablesDAOObj.getClassObject(BillingSalesOrder.class.getName(), billid);
                String[] header1 = {"DATE","SO NO"};
                String[] header2 = {"CUSTOMER","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
                String[] values1 = {formatter.format(so.getOrderDate()),so.getSalesOrderNumber()};
                String[] values2 = {so.getCustomer().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),so.getMemo()};
                String[] values3 = new String[5];
                String[] values4={Long.toString((so.getDueDate().getTime()-so.getOrderDate().getTime())/(3600000*24))+" Days",formatter.format(so.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4,"BillingSalesOrder", so, so.getCompany(),contextpath,logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_PAYMENT) {
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Payment.class.getName(), billid);
                Payment pay = (Payment) cap.getEntityList().get(0);

//                Payment pay = (Payment) session.get(Payment.class, billid);
                Set<JournalEntryDetail> entryset=pay.getJournalEntry().getDetails();
                Vendor vendor=new Vendor();
                Iterator itr=entryset.iterator();
                while(itr.hasNext()){
                    Account acc=((JournalEntryDetail)itr.next()).getAccount();
                    cap = kwlCommonTablesDAOObj.getObject(Vendor.class.getName(), acc.getID());
                    vendor = (Vendor) cap.getEntityList().get(0);
//                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                    if(vendor!=null)break;
                }
                String[] header1 = {"DATE","PAYMENT NO"};
                String[] header2 = {"VENDOR","AMOUNT","MEMO"};
                String[] header3 = {"GOODRECEIPT NO","CREATION DATE","DUE DATE","AMOUNT PAID"};
                String[] header4 = new String[0];
//                String[] values1 = {formatter.format(pay.getJournalEntry().getEntryDate()),pay.getPaymentNumber()};
                String[] values1 = {formatter.format(pay.getCreationDate()),pay.getPaymentNumber()};
                String[] values2 = {vendor.getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),pay.getMemo()};
                String[] values3 = new String[4];
                String[] values4=new String[0];
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4, "Payment", pay, pay.getCompany(),contextpath,logoPath,formatter);
            }else if (mode == StaticValues.AUTONUM_QUOTATION) {
                KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Quotation.class.getName(), billid);
                Quotation quotation = (Quotation) cap.getEntityList().get(0);
                String[] header1 = {"DATE","QUOTATION NO"};
                String[] header2 = {"CUSTOMER","AMOUNT DUE","MEMO"};
                String[] header3 = {"PRODUCT","QUANTITY","RATE","DISCOUNT","AMOUNT"};
                String[] header4 = {"TERMS","DUE DATE"};
                String[] values1 = {formatter.format(quotation.getQuotationDate()),quotation.getquotationNumber()};
                String[] values2 = {quotation.getCustomer().getName(),authHandlerDAOObj.getFormattedCurrency(amount,currencyid, companyid),quotation.getMemo()};
                String[] values3 = new String[5];
                String[] values4= null;//{Long.toString((quotation.getDueDate().getTime()-so.getOrderDate().getTime())/(3600000*24))+" Days",formatter.format(so.getDueDate())};
                baos=createForm(currencyid, header1, header2, header3,header4, values1, values2, values3,values4,"Quotation", quotation, quotation.getCompany(),contextpath,logoPath,formatter);
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return baos;
    }

    private ByteArrayOutputStream createForm(String currencyid, String[] header1, String[] header2, String[] header3,String[] header4, String[] values1, String[] values2, String[] values3,String[] values4, String string, Object ob, Company com, String contextpath, String logoPath,DateFormat formatter) throws JSONException, DocumentException,  ServiceException, IOException {
        ByteArrayOutputStream baos=null;
        Document document = null;
        PdfWriter writer = null;
        try {
        String company[] = new String[3];
        company[0] = com.getCompanyName();
        company[1] = com.getAddress();
        company[2] = com.getEmailID();
        baos=new ByteArrayOutputStream();
        document = new Document(PageSize.A4, 15, 15, 15, 15);
        writer = PdfWriter.getInstance(document, baos);
        document.open();
        addHeaderFooter(document,writer);
        PdfPTable tab1 = null;
        PdfPTable tab2 = null;
        PdfPTable tab3 = null;

        /*-----------------------------Add Company Name in Center ------------------*/
        tab1 = new PdfPTable(1);
        tab1.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell = new PdfPCell(new Paragraph(com.getCompanyName(), fontBig));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(0);
        tab1.addCell(cell);

        tab2 = new PdfPTable(1);
        tab2.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgPath = logoPath;
        PdfPCell imgCell = null;
        try {
            Image img = Image.getInstance(imgPath);
            imgCell = new PdfPCell(img);
        } catch (Exception e) {
            imgCell = new PdfPCell(new Paragraph(com.getCompanyName(), fontBig));
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgCell.setBorder(0);
        tab2.addCell(imgCell);

        PdfPTable table=new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40,60});
        PdfPCell cellCompimg = new PdfPCell(tab2);
        cellCompimg.setBorder(0);
        table.addCell(cellCompimg);
        PdfPCell cellCompname = new PdfPCell(tab1);
        cellCompname.setBorder(0);
        table.addCell(cellCompname);
        document.add(table);
        document.add(new Paragraph("\n\n\n"));

        /*-----------------------------Add Company information and Invoice Info------------------*/
        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(100);
        if(header4.length!=0)
            table2.setWidths(new float[]{40,30,30});
        else
            table2.setWidths(new float[]{50,50,0});
        tab1 = getCompanyInfo(company);
        tab2 = createTable(header1, values1);
        PdfPCell cell1 = new PdfPCell(tab1);
        cell1.setBorder(0);
        table2.addCell(cell1);
        PdfPCell cell2 = new PdfPCell(tab2);
        cell2.setBorder(1);
        table2.addCell(cell2);
        PdfPCell cel =new PdfPCell();
        if(header4.length!=0){
            tab3 = createTable(header4, values4);
            cel = new PdfPCell(tab3);
        }else
            cel = new PdfPCell(new Paragraph("", fontSmallRegular));
        cel.setBorder(1);
        table2.addCell(cel);
        document.add(table2);
        document.add(new Paragraph("\n\n\n"));

        /*-----------------------------Add BillTo Amount Enclosed -------------------------*/
        PdfPTable table3 = new PdfPTable(1);
        table3.setWidthPercentage(100);
        tab1 = createTable(header2, values2);
        PdfPCell cell3 = new PdfPCell(tab1);
        cell3.setBorder(1);
        table3.addCell(cell3);
        document.add(table3);
        document.add(new Paragraph("\n\n\n\n\n\n"));

        /*-----------------------------Add Cutting Line -------------------------*/
        PdfPTable table4 = new PdfPTable(1);
        imgPath = contextpath+"/images/pdf-cut.jpg";
        table4.setHorizontalAlignment(Element.ALIGN_LEFT);
        table4.setWidthPercentage(100);
        PdfPCell cell11 = null;
        try {
            Image img = Image.getInstance(imgPath);
            img.scalePercent(35);
            cell11 = new PdfPCell(img);
        } catch (Exception e) {
        }
        cell11.setBorder(0);
        table4.addCell(cell11);
        document.add(table4);
        document.add(new Paragraph("\n\n"));

        /*-----------------------------Add Product Information ------------------*/
        PdfPTable table5 = new PdfPTable(1);
        table5.setWidthPercentage(100);
        if(string.equals("GoodsReceipt")){
            GoodsReceipt gr = (GoodsReceipt) ob;
            tab1 = createGoodsReceiptTable(header3, values3, currencyid, gr, com.getCompanyID());
        }else if(string.equals("debit")){
            DebitNote dn = (DebitNote) ob;
            tab1 = createDebitTable(header3, values3, currencyid, dn, com.getCompanyID());
        }else if(string.equals("billingdebit")){
            BillingDebitNote dn = (BillingDebitNote) ob;
            tab1 = createDebitTable(header3, values3, currencyid, dn, com.getCompanyID());
        }else if(string.equals("credit")){
            CreditNote cn = (CreditNote) ob;
            tab1 = createCreditTable(header3, values3, currencyid, cn, com.getCompanyID());
        }else if(string.equals("billingcredit")){
            BillingCreditNote cn = (BillingCreditNote) ob;
            tab1 = createCreditTable(header3, values3, currencyid, cn, com.getCompanyID());
        }else if(string.equals("PurchaseOrder")){
            PurchaseOrder po = (PurchaseOrder) ob;
            tab1 = createPurchaseOrderTable(header3, values3, currencyid, po, com.getCompanyID());
        }else if(string.equals("BillingPurchaseOrder")){
            BillingPurchaseOrder po = (BillingPurchaseOrder) ob;
            tab1 = createPurchaseOrderTable(header3, values3, currencyid, po, com.getCompanyID());
        }else if(string.equals("SalesOrder")){
            SalesOrder so = (SalesOrder) ob;
            tab1 = createSalesOrderTable(header3, values3, currencyid, so, com.getCompanyID());
        }else if(string.equals("BillingSalesOrder")){
            BillingSalesOrder so = (BillingSalesOrder) ob;
            tab1 = createSalesOrderTable(header3, values3, currencyid, so, com.getCompanyID());
        }else if(string.equals("Payment")){
            Payment pay = (Payment) ob;
            tab1 = createPaymentTable(header3, values3, currencyid, pay,formatter, com.getCompanyID());
        }else if(string.equals("Quotation")){
        	Quotation quotation = (Quotation) ob;
            tab1 = createQuotationTable(header3, values3, currencyid, quotation, com.getCompanyID());
        }
        PdfPCell cell5 = new PdfPCell(tab1);
        cell5.setBorder(1);
        table5.addCell(cell5);
        document.add(table5);
        document.add(new Paragraph("\n\n\n"));

        /*-----------------------------Download file ------------------*/
        return baos;
        } catch (Exception ex) {
            return null;
        } finally {
            if(document!=null)
                document.close();
            if(writer!=null)
                writer.close();
            if(baos!=null)
                baos.close();
        }
    }

    private  PdfPTable createDebitTable(String header[], String values[], String currencyid, DebitNote deb, String companyid) throws DocumentException, ServiceException, JSONException {
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 40});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = deb.getRows().iterator();
            while (itr.hasNext()) {
                DebitNoteDetail debDet = (DebitNoteDetail) itr.next();
                cell = new PdfPCell(new Paragraph(debDet.getGoodsReceiptRow().getInventory().getProduct().getName(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(Double.toString(debDet.getQuantity()) + " " +  debDet.getGoodsReceiptRow().getInventory().getUom()==null?(debDet.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":debDet.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):debDet.getGoodsReceiptRow().getInventory().getUom().getNameEmptyforNA(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell=calculateDiscount(debDet.getDiscount(),currencyid, companyid);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
            }
         for (int i = 0; i < 3; i++) {
            PdfPCell cell1 = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell1.setBorder(Rectangle.TOP);
            table.addCell(cell1);
        }
        return table;
    }

    private PdfPTable createCreditTable(String header[], String values[], String currencyid, CreditNote cre, String companyid) throws DocumentException, ServiceException, JSONException {
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 40});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
           Iterator itr = cre.getRows().iterator();
            while (itr.hasNext()) {
                CreditNoteDetail creDet = (CreditNoteDetail) itr.next();
                cell = new PdfPCell(new Paragraph(creDet.getInvoiceRow().getInventory().getUom()==null?(creDet.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":creDet.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):creDet.getInvoiceRow().getInventory().getUom().getNameEmptyforNA(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(Double.toString(creDet.getQuantity()) + " " +  creDet.getInvoiceRow().getInventory().getUom()==null?(creDet.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":creDet.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):creDet.getInvoiceRow().getInventory().getUom().getNameEmptyforNA(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell=calculateDiscount(creDet.getDiscount(),currencyid, companyid);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
            }
        for (int i = 0; i < 3; i++) {
            PdfPCell cell1 = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell1.setBorder(Rectangle.TOP);
            table.addCell(cell1);
        }
        return table;
    }

    private PdfPTable createDebitTable(String header[], String values[], String currencyid, BillingDebitNote deb, String companyid) throws DocumentException, ServiceException, JSONException {
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 40});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = deb.getRows().iterator();
            while (itr.hasNext()) {
                BillingDebitNoteDetail debDet = (BillingDebitNoteDetail) itr.next();
                cell = new PdfPCell(new Paragraph(debDet.getGoodsReceiptRow().getProductDetail(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(Double.toString(debDet.getQuantity()), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell=calculateDiscount(debDet.getDiscount(),currencyid, companyid);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
            }
         for (int i = 0; i < 3; i++) {
            PdfPCell cell1 = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell1.setBorder(Rectangle.TOP);
            table.addCell(cell1);
        }
        return table;
    }

    private PdfPTable createCreditTable(String header[], String values[], String currencyid, BillingCreditNote cre, String companyid) throws DocumentException, ServiceException, JSONException {
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 30, 40});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
           Iterator itr = cre.getRows().iterator();
            while (itr.hasNext()) {
                BillingCreditNoteDetail creDet = (BillingCreditNoteDetail) itr.next();
                cell = new PdfPCell(new Paragraph(creDet.getInvoiceRow().getProductDetail(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(Double.toString(creDet.getQuantity()), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell=calculateDiscount(creDet.getDiscount(),currencyid, companyid);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
            }
        for (int i = 0; i < 3; i++) {
            PdfPCell cell1 = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell1.setBorder(Rectangle.TOP);
            table.addCell(cell1);
        }
        return table;
    }

    private PdfPTable createGoodsReceiptTable(String[] header, String[] values, String currencyid, GoodsReceipt gr, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
            Iterator itr = gr.getRows().iterator();
            while (itr.hasNext()) {
                GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();
                cell = new PdfPCell(new Paragraph(row.getInventory().getProduct().getName(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(Double.toString(row.getInventory().getQuantity()) + " " + row.getInventory().getUom()==null?(row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getInventory().getUom().getNameEmptyforNA(), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                cell=calculateDiscount(row.getDiscount(),currencyid, companyid);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
                double amount = row.getRate() * row.getInventory().getQuantity();
                if (row.getDiscount() != null) {
                    amount -= row.getDiscount().getDiscountValue();
                }
                total += amount;
                cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                table.addCell(cell);
            }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4);
        table.addCell(cell);
        cell=calculateDiscount(gr.getDiscount(),currencyid, companyid);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SHIPPING CHARGES", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell= getCharges(gr.getShipEntry(),currencyid, companyid);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  OTHER CHARGES", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell= getCharges(gr.getOtherEntry(),currencyid, companyid);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(gr.getVendorEntry().getAmount(),currencyid, companyid) ,fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createPurchaseOrderTable(String[] header, String[] values, String currencyid, PurchaseOrder po, String companyid) throws  DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = po.getRows().iterator();
        while (itr.hasNext()) {
            PurchaseOrderDetail row = (PurchaseOrderDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getProduct().getName(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(Double.toString(row.getQuantity()) + " " + row.getUom()==null?(row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getUom().getNameEmptyforNA(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getRate() * row.getQuantity();
            total += amount;
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph("--", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createSalesOrderTable(String[] header, String[] values, String currencyid, SalesOrder so, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = so.getRows().iterator();
        while (itr.hasNext()) {
            SalesOrderDetail row = (SalesOrderDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getProduct().getName(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(Double.toString(row.getQuantity()) + " " + row.getUom()==null?(row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getUom().getNameEmptyforNA(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getRate() * row.getQuantity();
            total += amount;
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph("--", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createPurchaseOrderTable(String[] header, String[] values, String currencyid, BillingPurchaseOrder po, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = po.getRows().iterator();
        while (itr.hasNext()) {
            BillingPurchaseOrderDetail row = (BillingPurchaseOrderDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getProductDetail(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(Double.toString(row.getQuantity()), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getRate() * row.getQuantity();
            total += amount;
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph("--", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createSalesOrderTable(String[] header, String[] values, String currencyid, BillingSalesOrder so, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = so.getRows().iterator();
        while (itr.hasNext()) {
            BillingSalesOrderDetail row = (BillingSalesOrderDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getProductDetail(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(Double.toString(row.getQuantity()), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getRate() * row.getQuantity();
            total += amount;
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph("--", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createPaymentTable(String[] header, String[] values, String currencyid, Payment pay, DateFormat formatter, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30,25, 25, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = pay.getRows().iterator();
        while (itr.hasNext()) {
            PaymentDetail row = (PaymentDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getGoodsReceipt().getGoodsReceiptNumber(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(formatter.format(row.getGoodsReceipt().getShipDate()), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(formatter.format(row.getGoodsReceipt().getDueDate()), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getAmount(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getAmount();
            total += amount;
        }

        for (int j = 0; j < 40; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 2; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    private PdfPTable createQuotationTable(String[] header, String[] values, String currencyid, Quotation quotation, String companyid) throws DocumentException {
        double total = 0;
        PdfPTable table = new PdfPTable(header.length);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30, 15, 15, 20, 20});
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        Iterator itr = quotation.getRows().iterator();
        while (itr.hasNext()) {
        	QuotationDetail row = (QuotationDetail) itr.next();
            cell = new PdfPCell(new Paragraph(row.getProduct().getName(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(Double.toString(row.getQuantity()) + " " + row.getUom()==null?(row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA()):row.getUom().getNameEmptyforNA(), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(row.getRate(), currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
            double amount = row.getRate() * row.getQuantity();
            total += amount;
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }

        for (int j = 0; j < 50; j++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
            table.addCell(cell);
        }
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.TOP);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  SUB TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  DISCOUNT", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph("--", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        for (int i = 0; i < 3; i++) {
            cell = new PdfPCell(new Paragraph("", fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(0);
            table.addCell(cell);
        }
        cell = new PdfPCell(new Paragraph("  TOTAL", fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
        cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontTblMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        return table;
    }

    @Deprecated
    private  PdfPCell calculateDiscount(Discount disc,String currencyid) {
        PdfPCell cell=null;
        if (disc== null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        } else if (disc.isInPercent()) {
            cell = new PdfPCell(new Paragraph(Double.toString(disc.getDiscount()) + "%", fontSmallRegular));
        } else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(disc.getDiscountValue(),currencyid), fontSmallRegular));
        }
        return cell;
    }
    
    private  PdfPCell calculateDiscount(Discount disc,String currencyid, String companyid) {
        PdfPCell cell=null;
        if (disc== null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        } else if (disc.isInPercent()) {
            cell = new PdfPCell(new Paragraph(Double.toString(disc.getDiscount()) + "%", fontSmallRegular));
        } else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(disc.getDiscountValue(),currencyid, companyid), fontSmallRegular));
        }
        return cell;
    }

    @Deprecated
    private PdfPCell getCharges(JournalEntryDetail jEntry,String currencyid)  {
        PdfPCell cell=null;
        if (jEntry== null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        }else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(jEntry.getAmount(),currencyid), fontSmallRegular));
        }
        return cell;
    }
    
    private PdfPCell getCharges(JournalEntryDetail jEntry,String currencyid, String companyid)  {
        PdfPCell cell=null;
        if (jEntry== null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        }else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(jEntry.getAmount(),currencyid, companyid), fontSmallRegular));
        }
        return cell;
    }

    private PdfPTable createTable(String header[], String values[]) {
        PdfPTable table = new PdfPTable(header.length);
        PdfPCell cell = null;
        for (int i = 0; i < header.length; i++) {
            cell = new PdfPCell(new Paragraph(header[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            table.addCell(cell);
        }
        for (int j = 0; j < values.length; j++) {
            cell = new PdfPCell(new Paragraph(values[j], fontSmallRegular));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    public PdfPTable getCompanyInfo(String com[]) {
        PdfPTable tab1 = new PdfPTable(1);
        tab1.setHorizontalAlignment(Element.ALIGN_LEFT);
        for (int i = 0; i < 3; i++) {
            PdfPCell cell = new PdfPCell(new Paragraph(com[i], fontMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            tab1.addCell(cell);
        }
        return tab1;
    }
}



