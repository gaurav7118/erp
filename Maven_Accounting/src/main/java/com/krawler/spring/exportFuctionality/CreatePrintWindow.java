/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFuctionality;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingCreditNote;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingDebitNote;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingPayment;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.BillingReceipt;
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
import com.krawler.hql.accounting.GoodsReceiptOrder;
import com.krawler.hql.accounting.GoodsReceiptOrderDetails;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.VendorQuotation;
import com.krawler.hql.accounting.VendorQuotationDetail;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import static com.krawler.spring.exportFuctionality.ExportRecordBeans.fontSmallRegular;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.support.RequestContextUtils;

public class CreatePrintWindow extends ExportRecordBeans implements MessageSourceAware {

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void createPrintWindow(HttpServletRequest request, HttpServletResponse response, String currencyid, String billid, DateFormat formatter, int mode, double amount, String logoPath, String customer, String accname, String address, boolean isExpenseInv) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        String recordData = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String reportName = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("filename"))) {
                reportName = request.getParameter("filename");
            }
            String ashtmlString = "<html> "
                    + "<head>" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                    + "<title>" + reportName + "</title>"
                    + "<style type=\"text/css\">@media print {button#print {display: none;}}</style>"
                    + "<div> <img src=" + logoPath + "b/" + com.krawler.common.util.URLUtil.getDomainName(request) + "/images/store/?company=true" + "></img></div>"
                    + "</head><body style = \"font-family: Tahoma, Verdana, Arial, Helvetica, sans-sarif;\">";

            String poRefno = "";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = null;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }

            if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE || mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFHeader, document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;
                Company com = null;
                Account cEntry;
                String invno = "";
                Date entryDate = null;
                BillingPurchaseOrder po = null;
                SalesOrder sOrder = null;
                PurchaseOrder pOrder = null;
                Tax mainTax = null;
                Quotation quotation = null;
                VendorQuotation venquotation = null;
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                    inv = (Invoice) cap.getEntityList().get(0);
                    currencyid = (inv.getCurrency() == null) ? currencyid : inv.getCurrency().getCurrencyID();
                    com = inv.getCompany();
                    cEntry = inv.getCustomerEntry().getAccount();
                    invno = inv.getInvoiceNumber();
//                    entryDate = inv.getJournalEntry().getEntryDate();
                    entryDate = inv.getCreationDate();
                } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
                    so = (BillingSalesOrder) kwlCommonTablesDAOObj.getClassObject(BillingSalesOrder.class.getName(), billid);
                    currencyid = (so.getCurrency() == null) ? currencyid : so.getCurrency().getCurrencyID();
                    com = so.getCompany();
                    cEntry = so.getCustomer().getAccount();
                    invno = so.getSalesOrderNumber();
                    entryDate = so.getOrderDate();
                    mainTax = so.getTax();
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                    po = (BillingPurchaseOrder) kwlCommonTablesDAOObj.getClassObject(BillingPurchaseOrder.class.getName(), billid);
                    currencyid = (po.getCurrency() == null) ? currencyid : po.getCurrency().getCurrencyID();
                    com = po.getCompany();
                    cEntry = po.getVendor().getAccount();
                    invno = po.getPurchaseOrderNumber();
                    entryDate = po.getOrderDate();
                    mainTax = po.getTax();
                } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                    sOrder = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), billid);
                    currencyid = (sOrder.getCurrency() == null) ? currencyid : sOrder.getCurrency().getCurrencyID();
                    com = sOrder.getCompany();
                    cEntry = sOrder.getCustomer().getAccount();
                    invno = sOrder.getSalesOrderNumber();
                    entryDate = sOrder.getOrderDate();
                    mainTax = sOrder.getTax();

                } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                    currencyid = (pOrder.getCurrency() == null) ? currencyid : pOrder.getCurrency().getCurrencyID();
                    com = pOrder.getCompany();
                    cEntry = pOrder.getVendor().getAccount();
                    invno = pOrder.getPurchaseOrderNumber();
                    entryDate = pOrder.getOrderDate();
                    mainTax = pOrder.getTax();

                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                    currencyid = (quotation.getCurrency() == null) ? currencyid : quotation.getCurrency().getCurrencyID();
                    com = quotation.getCompany();
                    cEntry = quotation.getCustomer().getAccount();
                    invno = quotation.getquotationNumber();
                    entryDate = quotation.getQuotationDate();
                    mainTax = quotation.getTax();

                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                    venquotation = (VendorQuotation) kwlCommonTablesDAOObj.getClassObject(VendorQuotation.class.getName(), billid);
                    currencyid = (venquotation.getCurrency() == null) ? currencyid : venquotation.getCurrency().getCurrencyID();
                    com = venquotation.getCompany();
                    cEntry = venquotation.getVendor().getAccount();
                    invno = venquotation.getQuotationNumber();
                    entryDate = venquotation.getQuotationDate();
                    mainTax = venquotation.getTax();

                } else {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    currencyid = (inv1.getCurrency() == null) ? currencyid : inv1.getCurrency().getCurrencyID();
                    com = inv1.getCompany();
                    cEntry = inv1.getCustomerEntry().getAccount();
                    invno = inv1.getBillingInvoiceNumber();
                    entryDate = inv1.getJournalEntry().getEntryDate();
                    poRefno = inv1.getPoRefNumber() == null ? "" : inv1.getPoRefNumber();
                    mainTax = inv1.getTax();
                }


                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();
//
//                if (mode != StaticValues.AUTONUM_BILLINGINVOICE) {
//                    cEntry = inv.getCustomerEntry().getAccount();
//                } else {
//                    cEntry = inv1.getCustomerEntry().getAccount();
//                }
                String theader = cEntry == cash ? messageSource.getMessage("acc.accPref.autoCS", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request));
                if (mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_SALESORDER) {
                    theader = messageSource.getMessage("acc.accPref.autoSO", null, RequestContextUtils.getLocale(request));
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    theader = messageSource.getMessage("acc.accPref.autoPO", null, RequestContextUtils.getLocale(request));
                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    theader = messageSource.getMessage("acc.accPref.autoCQN", null, RequestContextUtils.getLocale(request));
                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                    theader = messageSource.getMessage("acc.accPref.autoVQN", null, RequestContextUtils.getLocale(request));
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);
                ashtmlString += "<div align=\"right\" style=\"color:grey;\">"
                        + "<b><font size=\"5\" >" + theader + " </b>" + "</font>"
                        + "</div></br>";

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


                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = ExportRecordHandler.getCompanyInfo(company);
                for (int i = 0; i < company.length; i++) {
                    if (company[i] != null && company[i] != "") {
                        ashtmlString += ""
                                + "<left><div align=\"left\" style='float:left; '>"
                                + "" + company[i]
                                + "</div></left></br>";
                    }
                }

                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});

                PdfPCell cell2 = createCell(theader + "# :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
//                if(mode == StaticValues.AUTONUM_QUOTATION){
                cell2 = createCell(theader + "# :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                String number = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px; '>"
                        + "<b>" + theader + " # : </b>" + invno
                        + "</div></right>";
                ashtmlString += number;
//                }
                tab4.addCell(cell2);
//                String invno = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
                cell2 = createCell(invno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                if (mode != StaticValues.AUTONUM_QUOTATION && mode != StaticValues.AUTONUM_VENQUOTATION && mode != StaticValues.AUTONUM_PURCHASEORDER && mode != StaticValues.AUTONUM_SALESORDER) {
                    cell2 = createCell(messageSource.getMessage("acc.numb.43", null, RequestContextUtils.getLocale(request)) + " # :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                    tab4.addCell(cell2);
                    cell2 = createCell(poRefno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                    tab4.addCell(cell2);
                }

                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(formatter.format(entryDate), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                String date = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px;'>"
                        + "<b>" + messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " : </b>" + formatter.format(entryDate)
                        + "</div></right>";
                ashtmlString += date;

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainCell12.setPadding(10);
                mainTable.addCell(mainCell12);


                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request)) + " , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                ashtmlString += ""
                        + "<left><div style='padding-bottom: 5px; '>"
                        + "" + messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))
                        + "</div></left>";



                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                String customerName = "";
                String shipTo = "";
                String memo = "";
                String linkHeader = "";
                Iterator itr = null;
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    filter_names.add("invoice.ID");
                    filter_params.add(inv.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    if (pref.isWithInvUpdate()) {
                        linkHeader = "SO/DO/CQ. No.";
                    } else {
                        linkHeader = "SO/CQ. No.";
                    }
                    idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                    customerName = inv.getCustomer() == null ? inv.getCustomerEntry().getAccount().getName() : inv.getCustomer().getName();
                    shipTo = inv.getShipTo();
                    itr = idresult.getEntityList().iterator();
                    memo = inv.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
                    customerName = so.getCustomer().getName();
                    shipTo = so.getCustomer().getShippingAddress();
                    filter_names.add("salesOrder.ID");
                    filter_params.add(so.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getBillingSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = so.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                    customerName = po.getVendor().getName();
                    shipTo = po.getVendor().getAddress();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(po.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = po.getMemo();
                } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                    customerName = sOrder.getCustomer().getName();
                    shipTo = sOrder.getCustomer().getShippingAddress();
                    filter_names.add("salesOrder.ID");
                    filter_params.add(sOrder.getID());
                    //linkHeader = "Quotation No.";
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = sOrder.getMemo();
                } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    customerName = pOrder.getVendor().getName();
                    shipTo = pOrder.getVendor().getAddress();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(pOrder.getID());
                    //linkHeader = "SO No.";
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = pOrder.getMemo();
                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    customerName = quotation.getCustomer().getName();
                    shipTo = quotation.getCustomer().getShippingAddress();
                    filter_names.add("quotation.ID");
                    filter_params.add(quotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = quotation.getMemo();
                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                    customerName = venquotation.getVendor().getName();
                    shipTo = venquotation.getVendor().getAddress();
                    filter_names.add("vendorquotation.ID");
                    filter_params.add(venquotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = venquotation.getMemo();
                }
                cell3 = createCell(customerName, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + customerName + ","
                        + "</div></left>";

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + shipTo
                        + "</div></left>";

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainCell14.setPadding(10);
                mainTable.addCell(mainCell14);

//                if(mode == StaticValues.AUTONUM_QUOTATION)
//                	String[] header = {"S.No.","PRODUCT DESCRIPTION", "QUANTITY", "UNIT PRICE", "TAX", "AMOUNT"};

                String[] header;
                if (linkHeader.equalsIgnoreCase("")) {
                    header = new String[]{messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.176", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                } else {
                    header = new String[]{messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)), linkHeader, messageSource.getMessage("acc.rem.176", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                }
                PdfPTable table = ExportRecordHandler.getTable(linkHeader, false);
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ashtmlString += "</br></br>";
                ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "100");

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getTable(linkHeader, false);

                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                KwlReturnObject bAmt = null;
                InvoiceDetail row = null;
                BillingInvoiceDetail row1 = null;
                BillingSalesOrderDetail row3 = null;
                BillingPurchaseOrderDetail row4 = null;
                SalesOrderDetail row5 = null;
                PurchaseOrderDetail row6 = null;
                QuotationDetail row7 = null;
                VendorQuotationDetail row8 = null;
                int index = 0;
                while (itr.hasNext()) {
                    String linkTo = "-";
                    String prodName = "";
                    double quantity = 0, discountQuotation = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row.getInventory().getProduct());
                        quantity = (row.getInventory().isInvrecord() && (row.getInvoice().getPendingapproval() == 0)) ? row.getInventory().getQuantity() : row.getInventory().getActquantity();
                        rate = row.getRate();
                        discount = row.getDiscount();
                        uom = row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        if (row.getDeliveryOrderDetail() != null) {
                            linkTo = row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                        } else if (row.getSalesorderdetail() != null) {
                            linkTo = row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber();
                        } else if (row.getQuotationDetail() != null) {
                            linkTo = row.getQuotationDetail().getQuotation().getquotationNumber();
                        }
                    } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
                        row3 = (BillingSalesOrderDetail) itr.next();
                        prodName = row3.getProductDetail();
                        quantity = row3.getQuantity();
                        rate = row3.getRate();
                    } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                        row4 = (BillingPurchaseOrderDetail) itr.next();
                        prodName = row4.getProductDetail();
                        quantity = row4.getQuantity();
                        rate = row4.getRate();
                    } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                        row5 = (SalesOrderDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row5.getProduct());
                        quantity = row5.getQuantity();
                        rate = row5.getRate();
                        uom = row5.getProduct().getUnitOfMeasure() == null ? "" : row5.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        row6 = (PurchaseOrderDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row6.getProduct());
                        quantity = row6.getQuantity();
                        rate = row6.getRate();
                        uom = row6.getProduct().getUnitOfMeasure() == null ? "" : row6.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                        row7 = (QuotationDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row7.getProduct());
                        quantity = row7.getQuantity();
                        rate = row7.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = (Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row7.getDiscountispercent() == 1) ? rateInCurr * quantity * row7.getDiscount() / 100 : row7.getDiscount();
                        uom = row7.getProduct().getUnitOfMeasure() == null ? "" : row7.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                        row8 = (VendorQuotationDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        quantity = row8.getQuantity();
                        rate = row8.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = (Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row8.getDiscountispercent() == 1) ? rateInCurr * quantity * row8.getDiscount() / 100 : row8.getDiscount();
                        uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    } else {
                        row1 = (BillingInvoiceDetail) itr.next();
                        prodName = row1.getProductDetail();
                        quantity = row1.getQuantity();
                        rate = row1.getRate();
                        discount = row1.getDiscount() != null ? row1.getDiscount() : null;
                        if (row1.getSalesOrderDetail() != null) {
                            linkTo = row1.getSalesOrderDetail().getSalesOrder().getSalesOrderNumber();
                        }
//                        uom = row1.getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getInventory().getProduct().getUnitOfMeasure().getName();
                    }
                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData = "<tr><td align=\"center\">" + (index) + "." + "</td>";


                    if (!linkHeader.equalsIgnoreCase("")) {
                        invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        recordData += "<td>" + linkTo + "&nbsp;</td>";
                    }

                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td>" + prodName + "&nbsp;</td>";

                    String qtyStr = Double.toString(quantity);
                    if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    }
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"center\">" + qtyStr + "&nbsp;</td>";
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                        rate = rateInBase;
                    }
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(rate, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(rate, currencyid, companyid) + "&nbsp;</td>";

                    if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discountQuotation, currencyid, companyid);
                    } else {
                        invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discount, currencyid, companyid);
                    }
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);

                    if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(discountQuotation, currencyid, companyid) + "&nbsp;</td>";
                    } else if (discount != null && discount.getDiscountValue() != 0 && mode != StaticValues.AUTONUM_QUOTATION && mode != StaticValues.AUTONUM_VENQUOTATION) {
                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(discount.getDiscountValue(), currencyid, companyid) + "&nbsp;</td>";
                    } else {
                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(0, currencyid, companyid) + "&nbsp;</td>";
                    }

                    amount1 = rate * quantity;
                    if (discount != null) {
                        amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (row.getDiscount().getDiscountValue()) : (row1.getDiscount().getDiscountValue());
                    }
                    if (discountQuotation != 0) {
                        amount1 -= discountQuotation;
                    }
                    double rowTaxPercent = 0;
                    if (row != null && row.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row1 != null && row1.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row1.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row3 != null && row3.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row3.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row4 != null && row4.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row4.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row5 != null && row5.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row5.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row6 != null && row6.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row6.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row7 != null && row7.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row7.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    } else if (row8 != null && row8.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row8.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency((amount1 * rowTaxPercent / 100), currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency((amount1 * rowTaxPercent / 100), currencyid, companyid) + "&nbsp;</td>";
                    amount1 += amount1 * rowTaxPercent / 100;
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(amount1, currencyid, companyid) + "&nbsp;</td>";
                    total += amount1;

                    ashtmlString += recordData + "</tr>";
//                    ashtmlString += "</table>";
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                    table = ExportRecordHandler.getTable(linkHeader, false);
                }

                for (int j = 0; j < ((!linkHeader.equalsIgnoreCase("")) ? 112 : 98); j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                table = ExportRecordHandler.getTable(linkHeader, false);
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }

                cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.TOP, 5);
                if (!linkHeader.equalsIgnoreCase("")) {
                    cell3.setColspan(7);
                } else {
                    cell3.setColspan(6);
                }
                table.addCell(cell3);
//                if(mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER){
//                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, total, cEntry.getCurrency().getCurrencyID(), entryDate, 0);
//                    double baseTotalAmount = (Double) bAmt.getEntityList().get(0);
//                    total = baseTotalAmount;
//                }
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                if (!linkHeader.equalsIgnoreCase("")) {
                    cell3.setColspan(7);
                } else {
                    cell3.setColspan(6);
                }
                table.addCell(cell3);
                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid) + "</td></tr>";

//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                if (mainTax != null) { //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }

                if (mode == StaticValues.AUTONUM_INVOICE) {
                    totalDiscount = inv.getDiscount();
                    totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : 0;
                    totalAmount = inv.getCustomerEntry().getAmount();
                } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                    totalAmount = total;
                    if (mode == StaticValues.AUTONUM_QUOTATION && quotation.getDiscount() != 0) {
                        if (!quotation.isPerDiscount()) {
                            discountTotalQuotation = quotation.getDiscount();
                            total = total - quotation.getDiscount();
                            totalAmount = total;
                        } else {
                            discountTotalQuotation = total * quotation.getDiscount() / 100;
                            total -= (total * quotation.getDiscount() / 100);
                            totalAmount = total;
                        }
                    } else if (mode == StaticValues.AUTONUM_VENQUOTATION && venquotation.getDiscount() != 0) {
                        if (!venquotation.isPerDiscount()) {
                            discountTotalQuotation = venquotation.getDiscount();
                            total = total - venquotation.getDiscount();
                            totalAmount = total;
                        } else {
                            discountTotalQuotation = total * venquotation.getDiscount() / 100;
                            total -= (total * venquotation.getDiscount() / 100);
                            totalAmount = total;
                        }
                    }
                    totaltax = (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);
                    totalAmount = total + totaltax;
                } else {
                    totalDiscount = inv1.getDiscount();
                    totaltax = inv1.getTaxEntry() != null ? inv1.getTaxEntry().getAmount() : 0;
                    totalAmount = (inv1.getCustomerEntry().getAmount());
                }
                if (mode != StaticValues.AUTONUM_PURCHASEORDER || mode != StaticValues.AUTONUM_SALESORDER) {
                    cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                    if (!linkHeader.equalsIgnoreCase("")) {
                        cell3.setColspan(7);
                    } else {
                        cell3.setColspan(6);
                    }
                    table.addCell(cell3);
                    if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discountTotalQuotation, currencyid, companyid);
                    } else {
                        invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, totalDiscount, currencyid, companyid);
                    }
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    table.addCell(invcell);

                    ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)) + "</td>";
                    if (mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(discountTotalQuotation, currencyid, companyid) + "</td></tr>";
                    } else if (mode != StaticValues.AUTONUM_QUOTATION && mode != StaticValues.AUTONUM_VENQUOTATION && totalDiscount != null) {
                        ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(totalDiscount.getDiscountValue(), currencyid, companyid) + "</td></tr>";
                    } else {
                        ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(0.00, currencyid, companyid) + "</td></tr>";
                    }
                }
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                StringBuffer taxNameStr = new StringBuffer();
                if (mainTax != null) {
                    taxNameStr.append(mainTax.getName());
                    taxNameStr.append(" ");
                    taxNameStr.append(taxPercent);
                    taxNameStr.append("% (+)");
                    ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + mainTax.getName() + " " + taxPercent + "% (+)" + "</td>";
                } else {
                    taxNameStr.append(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)));
                    ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)) + "</td>";
                }
                cell3 = createCell(taxNameStr.toString(), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                if (!linkHeader.equalsIgnoreCase("")) {
                    cell3.setColspan(7);
                } else {
                    cell3.setColspan(6);
                }
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, companyid) + "</td></tr>";
                table.addCell(cell3);
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                if (!linkHeader.equalsIgnoreCase("")) {
                    cell3.setColspan(7);
                } else {
                    cell3.setColspan(6);
                }
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);

                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, companyid) + "</td></tr>";


                ExportRecordHandler.addTableRow(mainTable, table);

                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId, companyid);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);

                ashtmlString += "</table>";

                ashtmlString += "</br></br>";

                ashtmlString += "<left><div  align=\"left\" style=\"border:1px solid black; padding-bottom: 5px; padding-top: 5px; padding-left: 25px;\">"
                        + messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only."
                        + "</div></left>";

                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(pref.getDescriptionType() + ":  ", fontSmallBold);
                Phrase phrase2 = new Phrase(memo, fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

                ashtmlString += "</br></br>";
                ashtmlString +=
                        "<left><div align=left style='padding-bottom: 5px; padding-left: 25px;word-wrap: break-word;'>"
                        + "" + pref.getDescriptionType() + " : " + memo
                        + "</div></left>";

            } else if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFHeader, document, writer);

                CreditNote creNote = null;
                DebitNote dbNote = null;
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                Company com = null;
                Account cEntry = null;
                String invno = "";
                Date entryDate = null;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;

                if (mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingCreditNote.class.getName(), billid);
                    biCreNote = (BillingCreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset = biCreNote.getJournalEntry().getDetails();
                    customerObj = new Customer();
                    Iterator itr = entryset.iterator();
                    while (itr.hasNext()) {
                        cEntry = ((JournalEntryDetail) itr.next()).getAccount();
                        //                    customer=(Customer)session.get(Customer.class,acc.getID());
                        customerObj = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), cEntry.getID());
                        if (customerObj != null) {
                            break;
                        }
                    }
                    com = biCreNote.getCompany();
                    invno = biCreNote.getCreditNoteNumber();
                    entryDate = biCreNote.getJournalEntry().getEntryDate();
                } else if (mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingDebitNote.class.getName(), billid);
                    biDeNote = (BillingDebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = biDeNote.getCompany();
                    Set<JournalEntryDetail> entryset = biDeNote.getJournalEntry().getDetails();
                    vendorObj = new Vendor();
                    Iterator itr = entryset.iterator();
                    while (itr.hasNext()) {
                        cEntry = ((JournalEntryDetail) itr.next()).getAccount();
                        //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                        vendorObj = (Vendor) kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(), cEntry.getID());
                        if (vendorObj != null) {
                            break;
                        }
                    }
                    invno = biDeNote.getDebitNoteNumber();
                    entryDate = biDeNote.getJournalEntry().getEntryDate();
                }
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                    creNote = (CreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset = (creNote.getJournalEntry() != null)?creNote.getJournalEntry().getDetails():null;
                    customerObj = new Customer();
                    Iterator itr = (entryset!=null)?entryset.iterator():null;
                    if(itr!=null){
                        while (itr.hasNext()) {
                            cEntry = ((JournalEntryDetail) itr.next()).getAccount();
                            //                    customer=(Customer)session.get(Customer.class,acc.getID());
                            customerObj = (Customer) kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(), cEntry.getID());
                            if (customerObj != null) {
                                break;
                            }
                        }
                    }
                    com = creNote.getCompany();
                    invno = creNote.getCreditNoteNumber();
//                    entryDate = (creNote.isNormalCN())?creNote.getJournalEntry().getEntryDate():creNote.getCreationDate();
                    entryDate = creNote.getCreationDate();
                    //inv = (Invoice) session.get(Invoice.class, billid);
                } else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = dbNote.getCompany();
                    Set<JournalEntryDetail> entryset = (dbNote.getJournalEntry()!=null)?dbNote.getJournalEntry().getDetails():null;
                    vendorObj = new Vendor();
                    Iterator itr = (entryset!=null)?entryset.iterator():null;
                    if(itr!=null){
                        while (itr.hasNext()) {
                            cEntry = ((JournalEntryDetail) itr.next()).getAccount();
                            //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                            vendorObj = (Vendor) kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(), cEntry.getID());
                            if (vendorObj != null) {
                                break;
                            }
                        }
                    }
                    invno = dbNote.getDebitNoteNumber();
//                    entryDate = (dbNote.isNormalDN())?dbNote.getJournalEntry().getEntryDate():dbNote.getCreationDate();
                    entryDate = dbNote.getCreationDate();
                }


//                Company com = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getCompany() : inv1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();
//
//                if (mode != StaticValues.AUTONUM_BILLINGINVOICE) {
//                    cEntry = inv.getCustomerEntry().getAccount();
//                } else {
//                    cEntry = inv1.getCustomerEntry().getAccount();
//                }
                String theader = "";
                if (mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {
                    theader = messageSource.getMessage("acc.accPref.autoCN", null, RequestContextUtils.getLocale(request));
                } else if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                    theader = messageSource.getMessage("acc.accPref.autoDN", null, RequestContextUtils.getLocale(request));
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);
                ashtmlString += "<div align=\"right\" style=\"color:grey;\">"
                        + "<b><font size=\"5\" >" + theader + " </b>" + "</font>"
                        + "</div></br>";

                for (int i = 0; i < company.length; i++) {
                    if (company[i] != null && company[i] != "") {
                        ashtmlString += ""
                                + "<left><div align=\"left\" style='float:left; '>"
                                + "" + company[i]
                                + "</div></left></br>";
                    }
                }

                String number = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px; '>"
                        + "<b>" + theader + " # : </b>" + invno
                        + "</div></right>";
                ashtmlString += number;

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


                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = ExportRecordHandler.getCompanyInfo(company);

                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{30, 70});

                PdfPCell cell2 = createCell(theader + " #", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
//                String invno = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
                cell2 = createCell(": " + invno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(": " + formatter.format(entryDate), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                String date = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px;'>"
                        + "<b>" + messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " : </b>" + formatter.format(entryDate)
                        + "</div></right>";
                ashtmlString += date;

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainCell12.setPadding(10);
                mainTable.addCell(mainCell12);


                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request)) + " , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                ashtmlString += ""
                        + "<left><div style='padding-bottom: 5px; '>"
                        + "" + messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))
                        + ",</div></left>";

                String customerName = "";
                String shipTo = "";
                String memo = "";
                Iterator itr = null;

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                if (mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    shipTo = customerObj.getBillingAddress();//inv.getShipTo();
                    filter_names.add("creditNote.ID");
                    filter_params.add(biCreNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biCreNote.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    shipTo = vendorObj.getAddress();
                    filter_names.add("debitNote.ID");
                    filter_params.add(biDeNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getBillingDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biDeNote.getMemo();
                } else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    shipTo = customerObj.getBillingAddress();//inv.getShipTo();
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = creNote.getMemo();
                } else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    shipTo = vendorObj.getAddress();
                    filter_names.add("debitNote.ID");
                    filter_params.add(dbNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = dbNote.getMemo();
                }
                cell3 = createCell(customerName, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + customerName + ","
                        + "</div></left>";

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + shipTo
                        + "</div></left>";

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainCell14.setPadding(10);
                mainTable.addCell(mainCell14);


                String[] header = {messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.190", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{7, 35, 29, 29});
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ashtmlString += "</br></br>";
                ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "100");
                ashtmlString += "</br></br>";

//                Iterator itr = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getRows().iterator() : inv1.getRows().iterator();
                CreditNoteDetail row = null;
                DebitNoteDetail row1 = null;
                BillingCreditNoteDetail row2 = null;
                BillingDebitNoteDetail row3 = null;
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    double quantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                        row = (CreditNoteDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row.getInvoiceRow().getInventory().getProduct());
                        quantity = row.getQuantity();
                        if (row.getDiscount() != null) {
                            discount = row.getDiscount();
                        }
                        if (row.getTaxAmount() != null) {
                            taxMain = taxMain + row.getTaxAmount();
                            total = total + row.getTaxAmount();
                        }
                        try {
                            uom = row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        } catch (Exception ex) {//In case of exception use uom="";
                        }
                    } else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                        row1 = (DebitNoteDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row1.getGoodsReceiptRow().getInventory().getProduct());
                        quantity = row1.getQuantity();
                        discount = row1.getDiscount();
                        taxMain = taxMain + row1.getTaxAmount();
                        total = total + row1.getTaxAmount();
                        try {
                            uom = row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() == null ? "" : row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        } catch (Exception ex) {//In case of exception use uom="";
                        }
                    } else if (mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {
                        row2 = (BillingCreditNoteDetail) itr.next();
                        prodName = row2.getInvoiceRow().getProductDetail();
                        quantity = row2.getQuantity();
                        if (row2.getDiscount() != null) {
                            discount = row2.getDiscount();
                        }
                        if (row2.getTaxAmount() != null) {
                            taxMain = taxMain + row2.getTaxAmount();
                            total = total + row2.getTaxAmount();
                        }
                    } else if (mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                        row3 = (BillingDebitNoteDetail) itr.next();
                        prodName = row3.getGoodsReceiptRow().getProductDetail();
                        quantity = row3.getQuantity();
                        if (row3.getDiscount() != null) {
                            discount = row3.getDiscount();
                        }
                        if (row3.getTaxAmount() != null) {
                            taxMain = taxMain + row3.getTaxAmount();
                            total = total + row3.getTaxAmount();
                        }
                    }

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"center\">" + (index) + "." + "&nbsp;</td>";
                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"left\">" + prodName + "&nbsp;</td>";
                    invcell = createCell((double) quantity + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    recordData += "<td align=\"center\">" + (double) quantity + " " + uom + "&nbsp;</td>";
                    invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discount, currencyid, companyid);
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (discount != null && discount.getDiscountValue() != 0) {
                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(discount.getDiscountValue(), currencyid, companyid) + "&nbsp;</td>";
                    }

                    ashtmlString += recordData + "</tr>";

                    total += discount.getDiscountValue();
//                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    table.addCell(invcell);

                }
//                for (int j = 0; j < 70; j++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                    table.addCell(invcell);
//                }
//                for (int i = 0; i < 3; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }

                for (int i = 0; i < 2; i++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.TOP);
                    table.addCell(invcell);
                }


                cell3 = createCell(messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_CENTER, Rectangle.TOP, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);

                ashtmlString += "<tr><td align=\"right\" colspan=\"3\">" + messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid, companyid) + "</td></tr>";

                for (int i = 0; i < 2; i++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(0);
                    table.addCell(invcell);
                }

                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_CENTER, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
                ashtmlString += "<tr><td align=\"right\" colspan=\"3\">" + messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(total, currencyid, companyid) + "</td></tr>";


                ashtmlString += "</table>";

                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainCell5.setPadding(10);
                mainTable.addCell(mainCell5);

                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(pref.getDescriptionType() + " : ", fontSmallBold);
                Phrase phrase2 = new Phrase(memo, fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);
                ashtmlString += "</br></br>";
                ashtmlString +=
                        "<left><div align=left style='padding-bottom: 5px; padding-left: 25px;word-wrap: break-word;'>"
                        + "" + pref.getDescriptionType() + " : " + memo
                        + "</div></left>";
            } else if (mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFHeader, document, writer);
                GoodsReceipt gr = null;
                BillingGoodsReceipt gr1 = null;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
                } else {
                    gr1 = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), billid);
                }

                Company com = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCompany() : gr1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();
                
                KWLCurrency rowCurrency = (mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCurrency() : gr1.getCurrency());
                String rowCurrenctID = rowCurrency == null ? currencyid : rowCurrency.getCurrencyID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();
                Account vEntry;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vEntry = gr.getVendorEntry().getAccount();
                } else {
                    vEntry = gr1.getVendorEntry().getAccount();
                }
                String theader = vEntry == cash ? messageSource.getMessage("acc.accPref.autoCP", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.accPref.autoVI", null, RequestContextUtils.getLocale(request));
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);

                ashtmlString += "<div align=\"right\" style=\"color:grey;\">"
                        + "<b><font size=\"5\" >" + theader + " </b>" + "</font>"
                        + "</div></br>";

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainTable.addCell(mainCell11);

                blankTable = ExportRecordHandler.addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = ExportRecordHandler.getCompanyInfo(company);
                for (int i = 0; i < company.length; i++) {
                    if (company[i] != null && company[i] != "") {
                        ashtmlString += ""
                                + "<left><div align=\"left\" style='float:left; '>"
                                + "" + company[i]
                                + "</div></left></br>";
                    }
                }
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{30, 70});

                PdfPCell cell2 = createCell(theader + " #", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                String grno = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getGoodsReceiptNumber() : gr1.getBillingGoodsReceiptNumber();
                cell2 = createCell(": " + grno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + "  ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
//                cell2 = createCell(": " + formatter.format(mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getJournalEntry().getEntryDate() : gr1.getJournalEntry().getEntryDate()), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                cell2 = createCell(": " + formatter.format(mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCreationDate(): gr1.getJournalEntry().getEntryDate()), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                String number = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px; '>"
                        + "<b>" + theader + " # : </b>" + grno
                        + "</div></right>";
                ashtmlString += number;

                String date = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px;'>"
//                        + "<b>" + messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " : </b>" + formatter.format((mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getJournalEntry().getEntryDate() : gr1.getJournalEntry().getEntryDate()))
                        + "<b>" + messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " : </b>" + formatter.format((mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCreationDate() : gr1.getJournalEntry().getEntryDate()))
                        + "</div></right>";
                ashtmlString += date;

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainTable.addCell(mainCell12);

                blankTable = ExportRecordHandler.addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});

                ashtmlString += ""
                        + "<left><div style='padding-bottom: 5px; '>"
                        + "" + messageSource.getMessage("acc.common.from", null, RequestContextUtils.getLocale(request)) + " , "
                        + "</div></left>";


                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.from", null, RequestContextUtils.getLocale(request)) + " , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                String vendorName = "";
                String linkHeader = "";
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vendorName = gr.getVendor() == null ? gr.getVendorEntry().getAccount().getName() : gr.getVendor().getName();
                    if (pref.isWithInvUpdate()) {
                        linkHeader = "PO/GR/VQ. No.";
                    } else {
                        linkHeader = "PO/VQ. No.";
                    }
                } else {
                    vendorName = gr1.getVendor() == null ? gr1.getVendorEntry().getAccount().getName() : gr1.getVendor().getName();
                    linkHeader = "PO. No.";
                }
                cell3 = createCell(vendorName, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getBillFrom() : gr1.getBillFrom(), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + vendorName + ","
                        + "</div></left>";

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + (mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getBillFrom() : gr1.getBillFrom())
                        + "</div></left>";


                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainTable.addCell(mainCell14);

                blankTable = ExportRecordHandler.addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);
                PdfPTable table;
                PdfPCell grcell = null;
                if (isExpenseInv) {
                    String[] header = {"S.No.", "Account", "PRICE", "DISCOUNT", "TAX", "LINE TOTAL"};
                    table = new PdfPTable(6);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{7, 23, 18, 18, 18, 16});
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(0);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                } else {
                    String[] header;
                    if (linkHeader.equalsIgnoreCase("")) {
                        header = new String[]{messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.190", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))};
                    } else {
                        header = new String[]{messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)), linkHeader, messageSource.getMessage("acc.rem.190", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))};
                    }
                    table = ExportRecordHandler.getTable(linkHeader, false);
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(0);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }

                    ashtmlString += "</br></br>";
                    ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "100");

                }
//                Iterator itr =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getRows().iterator():gr1.getRows().iterator();
                HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                grRequestParams.put("order_by", order_by);
                grRequestParams.put("order_type", order_type);

                KwlReturnObject idresult = null;
                if (mode != StaticValues.AUTONUM_BILLINGINVOICE || mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                    if (mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                        filter_names.add("billingGoodsReceipt.ID");
                        filter_params.add(gr1.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                    } else {
                        filter_names.add("goodsReceipt.ID");
                        filter_params.add(gr.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        if (isExpenseInv) {
                            idresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                        } else {
                            idresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
                        }
                    }
                } else {
                    filter_names.add("billingGoodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                }
                Iterator itr = idresult.getEntityList().iterator();

                GoodsReceiptDetail row = null;
                BillingGoodsReceiptDetail row1 = null;
                ExpenseGRDetail exprow = null;
                int index = 0;
                while (itr.hasNext()) {
                    String linkTo = "-";
                    if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                        if (isExpenseInv) {
                            exprow = (ExpenseGRDetail) itr.next();
                        } else {
                            row = (GoodsReceiptDetail) itr.next();
                            if (row.getGoodsReceiptOrderDetails() != null) {
                                linkTo = row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber();
                            } else if (row.getPurchaseorderdetail() != null) {
                                linkTo = row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber();
                            } else if (row.getVendorQuotationDetail() != null) {
                                linkTo = row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber();
                            }
                        }
                    } else {
                        row1 = (BillingGoodsReceiptDetail) itr.next();
                        if (row1.getPurchaseOrderDetail() != null) {
                            linkTo = row1.getPurchaseOrderDetail().getPurchaseOrder().getPurchaseOrderNumber();
                        }
                    }
                    if (isExpenseInv) {
                        grcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(exprow.getRate(), rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, exprow.getDiscount(), rowCurrenctID, companyid);
                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        grcell.setPadding(5);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(grcell);
                        double amount1 = exprow.getRate();
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {
                            amount1 -= exprow.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent = 0;
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (exprow != null && exprow.getTax() != null) {
//                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getCreationDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency((amount1 * rowTaxPercent / 100), rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        amount1 += amount1 * rowTaxPercent / 100;
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        total += amount1;
                        for (int j = 0; j < 84; j++) {
                            grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                        }

                    } else {
                        grcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData = "<tr><td align=\"center\">" + (index) + "." + "</td>";

                        if (!linkHeader.equalsIgnoreCase("")) {
                            grcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);

                            recordData += "<td>" + linkTo + "&nbsp;</td>";
                        }
                        grcell = createCell(mode == StaticValues.AUTONUM_GOODSRECEIPT ? ExportRecordHandler.getProductNameWithDescription(row.getInventory().getProduct()) : row1.getProductDetail(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData += "<td>" + (mode == StaticValues.AUTONUM_GOODSRECEIPT ? ExportRecordHandler.getProductNameWithDescription(row.getInventory().getProduct()) : row1.getProductDetail()) + "&nbsp;</td>";

                        grcell = createCell(Double.toString(mode == StaticValues.AUTONUM_GOODSRECEIPT ? ((row.getInventory().isInvrecord() && (row.getGoodsReceipt().getPendingapproval() == 0)) ? row.getInventory().getQuantity() : row.getInventory().getActquantity()) : row1.getQuantity()) + " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : ""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData += "<td align=\"center\">" + (Double.toString(mode == StaticValues.AUTONUM_GOODSRECEIPT ? ((row.getInventory().isInvrecord() && (row.getGoodsReceipt().getPendingapproval() == 0)) ? row.getInventory().getQuantity() : row.getInventory().getActquantity()) : row1.getQuantity()) + " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : "")) + "&nbsp;</td>";

                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate(), rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate(), rowCurrenctID, companyid) + "&nbsp;</td>";

                        grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount(), rowCurrenctID, companyid);
                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        grcell.setPadding(5);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(grcell);

                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                            if (row.getDiscount() != null) {
                                recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(row.getDiscount().getDiscountValue(), rowCurrenctID, companyid) + "&nbsp;</td>";
                            } else {
                                recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(0.00, currencyid, companyid) + "&nbsp;</td>";
                            }
                        } else {
                            if (row1.getDiscount() != null) {
                                recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(row1.getDiscount().getDiscountValue(), rowCurrenctID, companyid) + "&nbsp;</td>";
                            } else {
                                recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(0.00, currencyid, companyid) + "&nbsp;</td>";
                            }
                        }

//                        recordData +="<td align=\"right\">"+authHandlerDAOObj.getFormattedCurrency((mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue()), currencyid)+"&nbsp;</td>";


                        double amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * ((row.getInventory().isInvrecord() && (row.getGoodsReceipt().getPendingapproval() == 0)) ? row.getInventory().getQuantity() : row.getInventory().getActquantity()) : row1.getRate() * row1.getQuantity();




                        Discount disc = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount();
                        if (disc != null) {
                            amount1 -= mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent = 0;
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (row != null && row.getTax() != null) {
//                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", row.getGoodsReceipt().getCreationDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        } else if (row1 != null && row1.getTax() != null) {
                            requestParams.put("transactiondate", row1.getBillingGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1 * rowTaxPercent / 100, rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(amount1 * rowTaxPercent / 100, rowCurrenctID, companyid) + "&nbsp;</td>";

                        amount1 += amount1 * rowTaxPercent / 100;
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);

                        recordData += "<td align=\"right\">" + authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID, companyid) + "&nbsp;</td>";

                        total += amount1;
                        for (int j = 0; j < 98; j++) {
                            grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                        }

                        recordData += "</tr>";

                        ashtmlString += recordData;
                    }
                }
                int length = isExpenseInv ? 4 : (linkHeader.equalsIgnoreCase("") ? 5 : 6);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setBorder(Rectangle.TOP);
                    table.addCell(grcell);
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, Rectangle.TOP, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }

                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID, companyid) + "</td></tr>";


                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getDiscount() : gr1.getDiscount(), rowCurrenctID, companyid);
                grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                grcell.setPadding(5);
                table.addCell(grcell);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }

                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getDiscount() != null ? gr.getDiscount().getDiscountValue() : 0.00) : (gr1.getDiscount() != null ? gr1.getDiscount().getDiscountValue() : 0.00), rowCurrenctID, companyid) + "</td></tr>";

                cell3 = createCell(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getTaxEntry() != null ? gr.getTaxEntry().getAmount() : 0) : (gr1.getTaxEntry() != null ? gr1.getTaxEntry().getAmount() : 0), rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }

                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getTaxEntry() != null ? gr.getTaxEntry().getAmount() : 0) : (gr1.getTaxEntry() != null ? gr1.getTaxEntry().getAmount() : 0), rowCurrenctID, companyid) + "</td></tr>";

                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getVendorEntry().getAmount()) : (gr1.getVendorEntry().getAmount()), rowCurrenctID, companyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);

                ashtmlString += "<tr><td align=\"right\" colspan=\"" + ((!linkHeader.equalsIgnoreCase("")) ? 7 : 6) + "\">" + messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)) + "</td>";
                ashtmlString += "<td align=\"right\" width=\"17%\">" + authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getVendorEntry().getAmount()) : (gr1.getVendorEntry().getAmount()), rowCurrenctID, companyid) + "</td></tr>";


                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainTable.addCell(mainCell5);
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                double totalamount = 0;
                if (gr != null) {
                    totalamount = gr.getVendorEntry().getAmount();
                } else if (gr1 != null) {
                    totalamount = gr1.getVendorEntry().getAmount();
                }
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency,countryLanguageId, companyid);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);


                ashtmlString += "</table>";

                ashtmlString += "</br></br>";

                ashtmlString += "<left><div  align=\"left\" style=\"border:1px solid black; padding-bottom: 5px; padding-top: 5px; padding-left: 25px;\">"
                        + messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only."
                        + "</div></left>";


                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(pref.getDescriptionType() + ":  ", fontSmallBold);
                Phrase phrase2 = new Phrase(mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getMemo() : gr1.getMemo(), fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

                ashtmlString += "</br></br>";
                ashtmlString +=
                        "<left><div align=left style='padding-bottom: 5px; padding-left: 25px;word-wrap: break-word;'>"
                        + "" + pref.getDescriptionType() + " : " + (mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getMemo() : gr1.getMemo())
                        + "</div></left>";

            } else if (mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT) {
                String td = "";
                String td1 = "";
                Receipt rc = null;
                BillingReceipt rc1 = null;
                if (mode != StaticValues.AUTONUM_BILLINGRECEIPT) {
                    rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
                } else {
                    rc1 = (BillingReceipt) kwlCommonTablesDAOObj.getClassObject(BillingReceipt.class.getName(), billid);
                }

                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                Company com = mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getCompany() : rc1.getCompany();
                String company[] = new String[3];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{25, 75});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);

                tab3 = new PdfPTable(1);
                tab3.setWidthPercentage(100);
                PdfPCell mainCell1 = null;
                mainCell1 = createCell(com.getCompanyName(), fontMediumBold, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(125);
                tab3.addCell(mainCell1);
                ashtmlString += "<center><div align=\"center\" > <b>" + com.getCompanyName() + "</b></div></center>";
                mainCell1 = createCell(com.getAddress(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getEmailID(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab1);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab3);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);

                mainCell1 = new PdfPCell(table1);
                mainCell1.setBorder(0);
                mainTable.addCell(mainCell1);

                ashtmlString += "</br></br>";

                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPCell mainCell2 = createCell(messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_CENTER, 0, 0);
                mainTable.addCell(mainCell2);
                ashtmlString += "<center><div align=\"center\" > <b>" + messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)) + "</b></div></center>";
                ashtmlString += "</br></br>";

                blankTable = ExportRecordHandler.addBlankLine(2);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab3 = new PdfPTable(2);
                tab3.setWidthPercentage(100);
                tab3.setWidths(new float[]{60, 40});

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{10, 90});

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request)) + " : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                new PdfPCell(new Paragraph());
                tab1.addCell(cell3);
                cell3 = createCell(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getReceiptNumber() : rc1.getBillingReceiptNumber(), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab1.addCell(cell3);
                ashtmlString += "<left><div align=\"left\" style='float:left; '>" + messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request)) + " :" + " <b>" + (mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getReceiptNumber() : rc1.getBillingReceiptNumber()) + "</b></div></left>";
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{30, 70});
                cell3 = createCell(messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(cell3);
//                cell3 = createCell(formatter.format(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getJournalEntry().getEntryDate() : rc1.getJournalEntry().getEntryDate()), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                cell3 = createCell(formatter.format(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getCreationDate() : rc1.getJournalEntry().getEntryDate()), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab2.addCell(cell3);
//                ashtmlString += "<right><div align=\"right\" style='float:right; '>" + messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)) + " <b>" + formatter.format(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getJournalEntry().getEntryDate() : rc1.getJournalEntry().getEntryDate()) + "</b></div></right>";
                ashtmlString += "<right><div align=\"right\" style='float:right; '>" + messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)) + " <b>" + formatter.format(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getCreationDate() : rc1.getJournalEntry().getEntryDate()) + "</b></div></right>";

                PdfPCell mainCell3 = new PdfPCell(tab1);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);
                mainCell3 = new PdfPCell(tab2);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);

                PdfPCell mainCell4 = new PdfPCell(tab3);
                mainCell4.setBorder(0);
                mainTable.addCell(mainCell4);

                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                String[] header = {messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                ashtmlString += "</br></br>";
                ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "80");

                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{75, 25});
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.RIGHT);
                tab2.addCell(cell3);
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
                tab2.addCell(cell3);
                td += messageSource.getMessage("acc.je.acc", null, RequestContextUtils.getLocale(request)) + " : " + "</br>";
                cell3 = createCell(messageSource.getMessage("acc.je.acc", null, RequestContextUtils.getLocale(request)) + " : ", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                td += "<pre>            " + accname + "</pre></br></br>";
                cell3 = createCell(accname, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                td1 += "<pre>            " + authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid) + "</pre></br></br>";
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 15);
                tab2.addCell(cell3);
                td += "<pre>            " + address + "</pre></br></br>";
                cell3 = createCell(address, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                for (int i = 0; i < 30; i++) {
                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
                    cell3.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    tab2.addCell(cell3);
                }
                td += messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)) + "</br>";
                cell3 = createCell(messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                td += "<pre>            " + (mode != StaticValues.AUTONUM_BILLINGRECEIPT ? (rc.getPayDetail() == null ? "Cash" : rc.getPayDetail().getPaymentMethod().getMethodName()) : (rc1.getPayDetail() == null ? "Cash" : rc1.getPayDetail().getPaymentMethod().getMethodName())) + "</pre></br></br>";
                cell3 = createCell(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? (rc.getPayDetail() == null ? "Cash" : rc.getPayDetail().getPaymentMethod().getMethodName()) : (rc1.getPayDetail() == null ? "Cash" : rc1.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? (rc.getPayDetail() != null ? (rc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque") ? "Cheque No : " + rc.getPayDetail().getCheque().getChequeNo() + " and Bank Name : " + rc.getPayDetail().getCheque().getBankName() : "") : "")
                        : (rc1.getPayDetail() != null ? (rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque") ? "Cheque No : " + rc1.getPayDetail().getCheque().getChequeNo() + " and Bank Name : " + rc1.getPayDetail().getCheque().getBankName() : "") : ""), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                String str = "";
                if (mode != StaticValues.AUTONUM_BILLINGRECEIPT) {
                    if (rc.getPayDetail() != null) {
                        if (rc.getPayDetail().getPaymentMethod().getDetailType() == 2 || rc.getPayDetail().getPaymentMethod().getDetailType() == 1) {
                            if (rc.getPayDetail().getCard() != null) {
                                str = "Card No : " + (rc.getPayDetail().getCard().getCardNo()) + " and Card Holder : " + rc.getPayDetail().getCard().getCardHolder();
                            } else if (rc.getPayDetail().getCheque() != null) {
                                str = "Bank Name : " + (rc.getPayDetail().getCheque().getBankName()) + "and Ref. No : " + (rc.getPayDetail().getCheque().getChequeNo());
                            }
                        }
                    }
                } else if (rc1.getPayDetail() != null) {
                    if (rc1.getPayDetail().getPaymentMethod().getDetailType() == 2 || rc1.getPayDetail().getPaymentMethod().getDetailType() == 1) {
                        if (rc1.getPayDetail().getCard() != null) {
                            str = "Card No : " + (rc1.getPayDetail().getCard().getCardNo()) + " and Card Holder : " + rc1.getPayDetail().getCard().getCardHolder();
                        } else if (rc1.getPayDetail().getCheque() != null) {
                            str = "Bank Name : " + (rc1.getPayDetail().getCheque().getBankName()) + "and Ref. No : " + (rc1.getPayDetail().getCheque().getChequeNo());
                        }
                    }
                }


//                mode!=StaticValues.AUTONUM_BILLINGRECEIPT?(rc.getPayDetail()!=null?(rc.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc.getPayDetail().getCard()!=null?"Card No : "+(rc.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc.getPayDetail().getCard().getCardHolder()):"":""):""):
//                        (rc1.getPayDetail()!=null?(rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc1.getPayDetail().getCard()!=null?"Card No : "+(rc1.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc1.getPayDetail().getCard().getCardHolder()):"":""):"")

                cell3 = createCell(str, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                td += messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)) + "</br>";
                cell3 = createCell(messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                td += "<pre>            " + (mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getMemo() : rc1.getMemo()) + "</pre></br></br>";
                cell3 = createCell(mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getMemo() : rc1.getMemo(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId, companyid);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                PdfPCell mainCell5 = new PdfPCell(tab2);
                mainCell5.setBorder(1);
                mainTable.addCell(mainCell5);

                ashtmlString += "<tr><td width=\"75%\">" + td + "</td>";
                ashtmlString += "<td width=\"25%\">" + td1 + "</td></tr>";
                blankTable = ExportRecordHandler.addBlankLine(25);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                ashtmlString += "<tr><td>" + messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only." + "</td>";
                ashtmlString += "<td align=\"center\">" + authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid) + "</td></tr>";
                ashtmlString += "</table>";

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                PdfPCell mainCell6 = new PdfPCell(tab1);
                mainCell6.setPadding(5);
                mainCell6.setBorder(0);
                mainTable.addCell(mainCell6);

                ashtmlString += "</br></br></br></br></br></br>";
                ashtmlString += "<table cellspacing=0 border=0 cellpadding=0 width=80% style='font-size:11pt'>";
                ashtmlString += "<tr><th></th></tr>";
                ashtmlString += "<tr>" + "<td color=\"grey\" align=\"center\" >" + messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)) + "</td>" + "<td color=\"grey\" align=\"center\" >" + messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)) + "</td>" + "</tr>";
                ashtmlString += "</table>";

                blankTable = ExportRecordHandler.addBlankLine(15);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);

                ashtmlString += "</br></br></br></br>";
                ashtmlString += "<table cellspacing=0 border=0 cellpadding=0 width=80% style='font-size:11pt'>";
                ashtmlString += "<tr>" + "<td color=\"grey\" align=\"center\">" + messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)) + "</tdd>" + "<td color=\"grey\" align=\"center\">" + messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)) + "</td>" + "</tr>";
                ashtmlString += "</table>";

                PdfPCell mainCell7 = new PdfPCell(tab2);
                mainCell7.setPadding(5);
                mainCell7.setBorder(0);
                mainTable.addCell(mainCell7);
                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

            } else if (mode == StaticValues.AUTONUM_PAYMENT || mode == StaticValues.AUTONUM_BILLINGPAYMENT) {
                String td = "";
                String td1 = "";
                Payment pc = null;
                BillingPayment pc1 = null;
                if (mode == StaticValues.AUTONUM_PAYMENT) {
                    pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
                } else {
                    pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
                }
                currencyid = (mode == StaticValues.AUTONUM_PAYMENT) ? pc.getCurrency().getCurrencyID() : pc1.getCurrency().getCurrencyID();
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                Company com = mode == StaticValues.AUTONUM_PAYMENT ? pc.getCompany() : pc1.getCompany();
                String company[] = new String[3];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();


                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{25, 75});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);

                tab3 = new PdfPTable(1);
                tab3.setWidthPercentage(100);
                PdfPCell mainCell1 = null;
                ashtmlString += "<center><div align=\"center\" > <b>" + com.getCompanyName() + "</b></div></center>";
                mainCell1 = createCell(com.getCompanyName(), fontMediumBold, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(125);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getAddress(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getEmailID(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab1);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab3);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);

                mainCell1 = new PdfPCell(table1);
                mainCell1.setBorder(0);
                mainTable.addCell(mainCell1);

                ashtmlString += "</br></br>";

                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                ashtmlString += "<center><div align=\"center\" > <b>" + messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)) + "</b></div></center>";
                ashtmlString += "</br></br>";
                PdfPCell mainCell2 = createCell(messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_CENTER, 0, 0);
                mainTable.addCell(mainCell2);

                blankTable = ExportRecordHandler.addBlankLine(2);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab3 = new PdfPTable(2);
                tab3.setWidthPercentage(100);
                tab3.setWidths(new float[]{60, 40});

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{10, 90});

                ashtmlString += "<left><div align=\"left\" style='float:left; '>" + messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request)) + " :" + " <b>" + (mode == StaticValues.AUTONUM_PAYMENT ? pc.getPaymentNumber() : pc1.getBillingPaymentNumber()) + "</b></div></left>";
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request)) + " : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                new PdfPCell(new Paragraph());
                tab1.addCell(cell3);
                cell3 = createCell(mode == StaticValues.AUTONUM_PAYMENT ? pc.getPaymentNumber() : pc1.getBillingPaymentNumber(), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab1.addCell(cell3);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{30, 70});
                cell3 = createCell(messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(cell3);
//                ashtmlString += "<right><div align=\"right\" style='float:right; '>" + messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)) + " <b>" + formatter.format(mode == StaticValues.AUTONUM_PAYMENT ? pc.getJournalEntry().getEntryDate() : pc1.getJournalEntry().getEntryDate()) + "</b></div></right>";
//                cell3 = createCell(formatter.format(mode == StaticValues.AUTONUM_PAYMENT ? pc.getJournalEntry().getEntryDate() : pc1.getJournalEntry().getEntryDate()), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                ashtmlString += "<right><div align=\"right\" style='float:right; '>" + messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)) + " <b>" + formatter.format(mode == StaticValues.AUTONUM_PAYMENT ? pc.getCreationDate() : pc1.getJournalEntry().getEntryDate()) + "</b></div></right>";
                cell3 = createCell(formatter.format(mode == StaticValues.AUTONUM_PAYMENT ? pc.getCreationDate() : pc1.getJournalEntry().getEntryDate()), fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab2.addCell(cell3);

                PdfPCell mainCell3 = new PdfPCell(tab1);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);
                mainCell3 = new PdfPCell(tab2);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);

                PdfPCell mainCell4 = new PdfPCell(tab3);
                mainCell4.setBorder(0);
                mainTable.addCell(mainCell4);

                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{75, 25});
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.RIGHT);
                tab2.addCell(cell3);

                String[] header = {messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                ashtmlString += "</br></br>";
                ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "80");




                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)) + " : ", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                td += "<pre>" + messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)) + " : " + "</pre>";

                td += "<pre>            " + accname + "</pre>";
                td += "<pre>            " + address + "</pre></br></br>";

                cell3 = createCell(accname, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                td1 += "<pre>            " + authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid) + "</pre></br></br>";

                tab2.addCell(cell3);
                cell3 = createCell(address, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                for (int i = 0; i < 30; i++) {
                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
                    cell3.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    tab2.addCell(cell3);
                }

                td += messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)) + "</br>";

                cell3 = createCell(messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode == StaticValues.AUTONUM_PAYMENT ? (pc.getPayDetail() == null ? "Cash" : pc.getPayDetail().getPaymentMethod().getMethodName()) : (pc1.getPayDetail() == null ? "Cash" : pc1.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode == StaticValues.AUTONUM_PAYMENT ? (pc.getPayDetail() != null ? (pc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque") ? "Cheque No : " + pc.getPayDetail().getCheque().getChequeNo() + " and Bank Name : " + pc.getPayDetail().getCheque().getBankName() : "") : "") : (pc1.getPayDetail() != null ? (pc1.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque") ? "Cheque No : " + pc1.getPayDetail().getCheque().getChequeNo() + " and Bank Name : " + pc1.getPayDetail().getCheque().getBankName() : "") : ""), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                String str = "";

                td += "<pre>            " + (mode == StaticValues.AUTONUM_PAYMENT ? (pc.getPayDetail() == null ? "Cash" : pc.getPayDetail().getPaymentMethod().getMethodName()) : (pc1.getPayDetail() == null ? "Cash" : pc1.getPayDetail().getPaymentMethod().getMethodName())) + "</pre></br></br>";


                if (mode == StaticValues.AUTONUM_PAYMENT) {
                    if (pc.getPayDetail() != null) {
                        if (pc.getPayDetail().getPaymentMethod().getDetailType() == 2 || pc.getPayDetail().getPaymentMethod().getDetailType() == 1) {
                            if (pc.getPayDetail().getCard() != null) {
                                str = "Card No : " + (pc.getPayDetail().getCard().getCardNo()) + " and Card Holder : " + pc.getPayDetail().getCard().getCardHolder();
                            } else if (pc.getPayDetail().getCheque() != null) {
                                str = "Bank Name : " + (pc.getPayDetail().getCheque().getBankName()) + "and Ref. No : " + (pc.getPayDetail().getCheque().getChequeNo());
                            }
                        }
                    }
                } else if (pc1.getPayDetail() != null) {
                    if (pc1.getPayDetail().getPaymentMethod().getDetailType() == 2 || pc1.getPayDetail().getPaymentMethod().getDetailType() == 1) {
                        if (pc1.getPayDetail().getCard() != null) {
                            str = "Card No : " + (pc1.getPayDetail().getCard().getCardNo()) + " and Card Holder : " + pc1.getPayDetail().getCard().getCardHolder();
                        } else if (pc1.getPayDetail().getCheque() != null) {
                            str = "Bank Name : " + (pc1.getPayDetail().getCheque().getBankName()) + "and Ref. No : " + (pc1.getPayDetail().getCheque().getChequeNo());
                        }
                    }
                }
                cell3 = createCell(str, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                td += messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)) + "</br>";

                cell3 = createCell(messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);

                td += "<pre>            " + (mode == StaticValues.AUTONUM_PAYMENT ? pc.getMemo() : pc1.getMemo()) + "</pre></br></br>";
                cell3 = createCell(mode == StaticValues.AUTONUM_PAYMENT ? pc.getMemo() : pc1.getMemo(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency,countryLanguageId, companyid);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                PdfPCell mainCell5 = new PdfPCell(tab2);
                mainCell5.setBorder(1);
                mainTable.addCell(mainCell5);

                ashtmlString += "<tr><td width=\"75%\">" + td + "</td>";
                ashtmlString += "<td width=\"25%\">" + td1 + "</td></tr>";


                blankTable = ExportRecordHandler.addBlankLine(25);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);


                ashtmlString += "<tr><td>" + messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request)) + " : " + currencyname + " " + netinword + " Only." + "</td>";
                ashtmlString += "<td align=\"center\">" + authHandlerDAOObj.getFormattedCurrency(amount, currencyid, companyid) + "</td></tr>";
                ashtmlString += "</table>";


                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                PdfPCell mainCell6 = new PdfPCell(tab1);
                mainCell6.setPadding(5);
                mainCell6.setBorder(0);
                mainTable.addCell(mainCell6);

                ashtmlString += "</br></br></br></br></br></br>";
                ashtmlString += "<table cellspacing=0 border=0 cellpadding=0 width=80% style='font-size:11pt'>";
                ashtmlString += "<tr><th></th></tr>";
                ashtmlString += "<tr>" + "<td color=\"grey\" align=\"center\" >" + messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)) + "</td>" + "<td color=\"grey\" align=\"center\" >" + messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)) + "</td>" + "</tr>";
                ashtmlString += "</table>";

                blankTable = ExportRecordHandler.addBlankLine(15);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);

                ashtmlString += "</br></br></br></br>";
                ashtmlString += "<table cellspacing=0 border=0 cellpadding=0 width=80% style='font-size:11pt'>";
                ashtmlString += "<tr>" + "<td color=\"grey\" align=\"center\">" + messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)) + "</tdd>" + "<td color=\"grey\" align=\"center\">" + messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)) + "</td>" + "</tr>";
                ashtmlString += "</table>";


                PdfPCell mainCell7 = new PdfPCell(tab2);
                mainCell7.setPadding(5);
                mainCell7.setBorder(0);
                mainTable.addCell(mainCell7);
                blankTable = ExportRecordHandler.addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

            } else if (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFHeader, document, writer);
                Company com = null;
                Account cEntry;
                String invno = "";
                Date entryDate = null;
                String company[] = new String[4];
                String customerName = "";
                String shipTo = "";
                String memo = "";
                String orderID = "";
                String theader = "";
                String pointPern = "";
                String recQuantity = "";
                String linkHeader = "";
                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    DeliveryOrder deliveryOrder = null;
                    deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), billid);
                    com = deliveryOrder.getCompany();
                    cEntry = deliveryOrder.getCustomer().getAccount();
                    invno = deliveryOrder.getDeliveryOrderNumber();
                    entryDate = deliveryOrder.getOrderDate();
                    customerName = deliveryOrder.getCustomer().getName();
                    shipTo = deliveryOrder.getCustomer().getShippingAddress();
                    memo = deliveryOrder.getMemo();
                    orderID = deliveryOrder.getID();
                    theader = messageSource.getMessage("acc.accPref.autoDO", null, RequestContextUtils.getLocale(request));
                    pointPern = "acc.common.to";
                    recQuantity = "acc.accPref.deliQuant";
                    linkHeader = "CI/SO No.";
                } else {
                    GoodsReceiptOrder grOrder = null;
                    grOrder = (GoodsReceiptOrder) kwlCommonTablesDAOObj.getClassObject(GoodsReceiptOrder.class.getName(), billid);
                    com = grOrder.getCompany();
                    cEntry = grOrder.getVendor().getAccount();
                    invno = grOrder.getGoodsReceiptOrderNumber();
                    entryDate = grOrder.getOrderDate();
                    customerName = grOrder.getVendor().getName();
                    shipTo = grOrder.getVendor().getAddress();
                    memo = grOrder.getMemo();
                    orderID = grOrder.getID();
                    theader = messageSource.getMessage("acc.accPref.autoGRO", null, RequestContextUtils.getLocale(request));
                    pointPern = "acc.common.from";
                    recQuantity = "acc.accPref.recQuant";
                    linkHeader = "VI/PO No.";
                }

                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = ExportRecordHandler.addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                //String theader = messageSource.getMessage("acc.accPref.autoDO", null, RequestContextUtils.getLocale(request));

                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);

                ashtmlString += "<div align=\"right\" style=\"color:grey;\">"
                        + "<b><font size=\"5\" >" + theader + " </b>" + "</font>"
                        + "</div></br>";

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


                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = ExportRecordHandler.getCompanyInfo(company);

                for (int i = 0; i < company.length; i++) {
                    if (company[i] != null && company[i] != "") {
                        ashtmlString += ""
                                + "<left><div align=\"left\" style='float:left; '>"
                                + "" + company[i]
                                + "</div></left></br>";
                    }
                }


                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});

                PdfPCell cell2 = createCell(theader + "# :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                //if(mode == StaticValues.AUTONUM_QUOTATION){
                cell2 = createCell(theader + "# :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                String number = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px; '>"
                        + "<b>" + theader + " # : </b>" + invno
                        + "</div></right>";
                ashtmlString += number;
                //}
                tab4.addCell(cell2);
                //                String invno = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
                cell2 = createCell(invno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(formatter.format(entryDate), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                String date = ""
                        + "<right><div align=\"right\" style='padding-bottom: 5px; padding-right: 5px;'>"
                        + "<b>" + messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request)) + " : </b>" + formatter.format(entryDate)
                        + "</div></right>";
                ashtmlString += date;


                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainCell12.setPadding(10);
                mainTable.addCell(mainCell12);


                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage(pointPern, null, RequestContextUtils.getLocale(request)) + " , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                ashtmlString += ""
                        + "<left><div style='padding-bottom: 5px; '>"
                        + "" + messageSource.getMessage(pointPern, null, RequestContextUtils.getLocale(request))
                        + "</div></left>";

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;


                Iterator itr = null;


                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    filter_names.add("deliveryOrder.ID");
                } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                    filter_names.add("grOrder.ID");
                }
                filter_params.add(orderID);

                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    idresult = accInvoiceDAOobj.getDeliveryOrderDetails(invRequestParams);
                } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                    idresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(invRequestParams);
                }
                itr = idresult.getEntityList().iterator();

                cell3 = createCell(customerName, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + customerName + ","
                        + "</div></left>";

                ashtmlString +=
                        "<left><div style='padding-bottom: 5px; padding-left: 25px;'>"
                        + "     " + shipTo
                        + "</div></left>";

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainCell14.setPadding(10);
                mainTable.addCell(mainCell14);

                String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)), linkHeader, messageSource.getMessage("acc.rem.176", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage(recQuantity, null, RequestContextUtils.getLocale(request))};
                PdfPTable table = ExportRecordHandler.getBlankTableForDO();

                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ashtmlString += "</br></br>";
                ashtmlString = ExportRecordHandler.getBlankTable(ashtmlString, header, "100");

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableForDO();

                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                KwlReturnObject bAmt = null;

                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    double quantity = 0, deliverdQuantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String linkTo = "-";
                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row8.getProduct());
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "<br/><br/>" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                            prodName += "<br/>" + partno;
                            prodName += "<br/>";
                        }
                        prodName += "<br/>";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        if (row8.getCidetails() != null) {
                            linkTo = row8.getCidetails().getInvoice().getInvoiceNumber();
                        } else if (row8.getSodetails() != null) {
                            linkTo = row8.getSodetails().getSalesOrder().getSalesOrderNumber();
                        }
                    } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                        GoodsReceiptOrderDetails row8 = (GoodsReceiptOrderDetails) itr.next();
                        prodName = ExportRecordHandler.getProductNameWithDescription(row8.getProduct());
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "<br/><br/>" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                            prodName += "<br/>" + partno;
                            prodName += "<br/>";
                        }
                        prodName += "<br/>";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        if (row8.getVidetails() != null) {
                            linkTo = row8.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
                        } else if (row8.getPodetails() != null) {
                            linkTo = row8.getPodetails().getPurchaseOrder().getPurchaseOrderNumber();
                        }
                    }
                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    recordData = "<tr><td align=\"center\">" + (index) + "." + "</td>";
                    table.addCell(invcell);
                    invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    recordData += "<td align=\"center\">" + linkTo + "&nbsp;</td>";
                    table.addCell(invcell);
                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    recordData += "<td>" + prodName + "&nbsp;</td>";
                    table.addCell(invcell);

                    String qtyStr = Double.toString(quantity);
//                             if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_DELIVERYORDER ) {
                    qtyStr = Double.toString((double) quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
//                                        }
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    recordData += "<td align=\"center\">" + qtyStr + " " + uom + "&nbsp;</td>";
                    table.addCell(invcell);
                    invcell = createCell(Double.toString((double) deliverdQuantity) + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    recordData += "<td align=\"center\">" + Integer.toString((int) deliverdQuantity) + " " + uom + "&nbsp;</td>";
                    table.addCell(invcell);

                    ashtmlString += recordData + "</tr>";

                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row                                
                    table = ExportRecordHandler.getBlankTableForDO();
                }
                for (int j = 0; j < 98; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space

                table = ExportRecordHandler.getBlankTableForDO();

                for (int j = 0; j < 5; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.BOTTOM + Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table);

                ashtmlString += "</table>";
                ashtmlString += "</br></br>";

                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(pref.getDescriptionType() + ":  ", fontSmallBold);
                Phrase phrase2 = new Phrase(memo, fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

                ashtmlString +=
                        "<left><div align=left style='padding-bottom: 5px; padding-left: 25px;word-wrap: break-word;'>"
                        + "" + pref.getDescriptionType() + " : " + memo
                        + "</div></left>";

            }
            document.add(mainTable);








            ashtmlString += "<div style='float: left; padding-top: 3px; padding-right: 5px;'>"
                    + "<button id = 'print' title='Print Invoice' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>" + messageSource.getMessage("acc.common.print", null, RequestContextUtils.getLocale(request)) + "</button>"
                    + "</div>";


            ashtmlString += "</body>"
                    + "</html>";
            response.getOutputStream().write(ashtmlString.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("createPrintWindow: " + ex.getMessage(), ex);
        } finally {
        }

    }
}
