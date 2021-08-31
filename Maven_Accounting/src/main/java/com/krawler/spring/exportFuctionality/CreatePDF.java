
package com.krawler.spring.exportFuctionality;

import java.util.Locale;
import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.integration.common.IntegrationCommonServiceImpl;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import org.jsoup.Jsoup;

public class CreatePDF extends ExportRecordBeans implements MessageSourceAware {

    private AccCommonTablesDAO accCommonTablesDAO;
    private static Font fontSmallRegular = FontFactory.getFont("Arial", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallRegularsmall = FontFactory.getFont("Arial", 9, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Arial", 10, Font.BOLD, Color.BLACK);
    private static Font fontMediumRegular = FontFactory.getFont("Arial", 11, Font.NORMAL, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Arial", 12, Font.BOLD, Color.BLACK);
    private static Font fontTblMediumBold = FontFactory.getFont("Arial", 10, Font.NORMAL, Color.GRAY);
    private static Font fontTbl = FontFactory.getFont("Arial", 20, Font.NORMAL, Color.GRAY);
    private static Font fontMediumBold1 = FontFactory.getFont("Arial", 11, Font.BOLD, Color.BLACK);
    private static Font fontSmallRegular1 = FontFactory.getFont("Arial", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold1 = FontFactory.getFont("Arial", 10, Font.BOLD, Color.BLACK);

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public ByteArrayOutputStream createPdf(HashMap<String, Object> requestmap, String currencyid, String billid, DateFormat formatter, int mode, double amount, String logoPath, String customer, String accname, String address, boolean isExpenseInv, String CompanyID, String userId, String baseCurrency) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        double cndnTotalOtherwiseAmount = 0;
        Document document = null;
        PdfWriter writer = null;
        boolean iscontraentryflag = false;
        boolean otherwiseFlag = false;
        boolean advanceFlag = false;
        boolean isOpeningBalanceTransaction = false;
        double advanceAmount = 0;
        boolean isCurrencyCode=false;
        boolean isLetterHead = false;
        try {
            String baseUrl = requestmap.get("baseUrl").toString();
            if (requestmap.containsKey("iscontraentryflag") && !StringUtil.isNullOrEmpty(requestmap.get("iscontraentryflag").toString())) {
                iscontraentryflag = Boolean.parseBoolean(requestmap.get("iscontraentryflag").toString());
            }
            if (requestmap.containsKey("otherwiseFlag") && !StringUtil.isNullOrEmpty(requestmap.get("otherwiseFlag").toString())) {
                otherwiseFlag = Boolean.parseBoolean(requestmap.get("otherwiseFlag").toString());
            }
            if (requestmap.containsKey("advanceFlag") && !StringUtil.isNullOrEmpty(requestmap.get("advanceFlag").toString())) {
                advanceFlag = Boolean.parseBoolean(requestmap.get("advanceFlag").toString());
            }
            if (requestmap.containsKey("isOpeningBalanceTransaction") && !StringUtil.isNullOrEmpty(requestmap.get("isOpeningBalanceTransaction").toString())) {
                isOpeningBalanceTransaction = Boolean.parseBoolean(requestmap.get("isOpeningBalanceTransaction").toString());
            }
            if (requestmap.containsKey("isLetterHead") && !StringUtil.isNullOrEmpty(requestmap.get("isLetterHead").toString())) {
                isLetterHead = Boolean.parseBoolean(requestmap.get("isLetterHead").toString());
            }
            if (storageHandlerImpl.SBICompanyId().equals(CompanyID)){
                isCurrencyCode=true;
            }
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), CompanyID);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            String poRefno = "";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 35, 30);
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new CreatePDF.EndPage());
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            boolean isCompanyLogo = true;
            boolean addShipTo = true;
            boolean isCompanyTemplateLogo = false;
            PdfPTable tab3 = null;
            String approverName = "______________________________";
            Rectangle page = document.getPageSize();
            Company com = null;
            boolean isLeaseFixedAsset = false;
            boolean isConsignment = false;
            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();
            String companyIdForCAP = CompanyID;
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyIdForCAP);
            int moduleid = -1;
            moduleid = ExportRecordHandler.getModuleId(mode, billid, CompanyID, kwlCommonTablesDAOObj);
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(CompanyID, moduleid);
            List<PdfTemplateConfig> list = templateConfig.getEntityList();
            if (list.isEmpty()) {
                CompanyPDFFooter = preferences.getPdffooter() == null ? "" : preferences.getPdffooter();
                CompanyPDFHeader = preferences.getPdfheader() == null ? "" : preferences.getPdfheader();
                CompanyPDFPRETEXT = preferences.getPdfpretext() == null ? "" : preferences.getPdfpretext();
                CompanyPDFPOSTTEXT = preferences.getPdfposttext() == null ? "" : preferences.getPdfposttext();
            } else {
                for (PdfTemplateConfig config : list) {
                    CompanyPDFFooter = config.getPdfFooter() == null ? "" : config.getPdfFooter();
                    CompanyPDFHeader = config.getPdfHeader() == null ? "" : config.getPdfHeader();
                    CompanyPDFPRETEXT = config.getPdfPreText() == null ? "" : config.getPdfPreText();
                    CompanyPDFPOSTTEXT = config.getPdfPostText() == null ? "" : config.getPdfPostText();
                }
            }
            String preText = StringUtil.isNullOrEmpty(CompanyPDFPRETEXT) ? "" : CompanyPDFPRETEXT;
            String postText = StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) ? "" : CompanyPDFPOSTTEXT;

            Projreport_Template defaultTemplate = (Projreport_Template) kwlCommonTablesDAOObj.getClassObject(Projreport_Template.class.getName(), Constants.HEADER_IMAGE_TEMPLATE_ID);
            if (defaultTemplate != null) {
                config = new com.krawler.utils.json.base.JSONObject(defaultTemplate.getConfigstr());
            }
            boolean isRateIncludeGST = false;
            if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE || mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION  || mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION || mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_RFQ || mode==StaticValues.AUTONUM_PURCHASEREQUISITION) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;

                Account cEntry = null;
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
                QuotationVersion quotationVersion = null;
                VendorQuotation venquotation = null;
                VendorQuotationVersion vendorQuotationVersion = null;
                RequestForQuotation RFQ = null;
                PurchaseRequisition  purchaseRequisition = null;
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                    inv = (Invoice) cap.getEntityList().get(0);
                    isRateIncludeGST = inv.isGstIncluded();
                    if (inv.getApprover() != null) {
                        approverName = inv.getApprover().getFirstName() + " " + inv.getApprover().getLastName();
                    }
                    currencyid = (inv.getCurrency() == null) ? currencyid : inv.getCurrency().getCurrencyID();
                    if (inv.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(inv.getTemplateid().getConfigstr());

                        Rectangle rec = null;
                        if (config.getBoolean("landscape")) {
                            Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 55, 30);
                            rec = document.getPageSize();
                        } else {
                            Rectangle recPage = new Rectangle(PageSize.A4);
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 35, 30);
                            rec = document.getPageSize();
                        }
                        writer = PdfWriter.getInstance(document, baos);
                        writer.setPageEvent(new CreatePDF.EndPage());
                        document.open();
                        isCompanyLogo = false;
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
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    if (inv1.getApprover() != null) {
                        approverName = inv1.getApprover().getFirstName() + " " + inv1.getApprover().getLastName();
                    }
                    currencyid = (inv1.getCurrency() == null) ? currencyid : inv1.getCurrency().getCurrencyID();
                    if (inv1.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(inv1.getTemplateid().getConfigstr());

                        Rectangle rec = null;
                        if (config.getBoolean("landscape")) {
                            Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 55, 30);
                            rec = document.getPageSize();
                        } else {
                            Rectangle recPage = new Rectangle(PageSize.A4);
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 55, 30);
                            rec = document.getPageSize();
                        }
                        writer = PdfWriter.getInstance(document, baos);
                        writer.setPageEvent(new CreatePDF.EndPage());
                        isCompanyLogo = false;
                        document.open();

                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(inv1.getCompany())) {
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                        String letterHead = inv1.getTemplateid().getLetterHead();
                        String invoicePostText = inv1.getPostText() == null ? "" : inv1.getPostText();
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? inv1.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)) ? inv1.getTemplateid().getPreText() : CompanyPDFPRETEXT;
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
                }
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    com = inv.getCompany();
                    cEntry = inv.getCustomerEntry().getAccount();
                    invno = inv.getInvoiceNumber();
//                    entryDate = inv.getJournalEntry().getEntryDate();
                    entryDate = inv.getCreationDate();
                    dueDate = inv.getDueDate();
                    shipDate = inv.getShipDate();
                    shipvia = inv.getShipvia();
                    fob = inv.getFob();
                    poRefno = inv.getPoRefNumber() == null ? "" : inv.getPoRefNumber();
                    isLeaseFixedAsset = inv.isFixedAssetLeaseInvoice();
                    isConsignment = inv.isIsconsignment();
//inv = (Invoice) session.get(Invoice.class, billid);
                } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
                    so = (BillingSalesOrder) kwlCommonTablesDAOObj.getClassObject(BillingSalesOrder.class.getName(), billid);
                    if (so.getApprover() != null) {
                        approverName = so.getApprover().getFirstName() + " " + so.getApprover().getLastName();
                    }
                    currencyid = (so.getCurrency() == null) ? currencyid : so.getCurrency().getCurrencyID();
                    com = so.getCompany();
                    if (so.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(so.getTemplateid().getConfigstr());
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

                    String invoicePostText = so.getPostText() == null ? "" : so.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? so.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = so.getCustomer().getAccount();
                    invno = so.getSalesOrderNumber();
                    entryDate = so.getOrderDate();
                    dueDate = so.getDueDate();
                    mainTax = so.getTax();
                    shipDate = so.getShipdate();
                    shipvia = so.getShipvia();
                    fob = so.getFob();
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                    po = (BillingPurchaseOrder) kwlCommonTablesDAOObj.getClassObject(BillingPurchaseOrder.class.getName(), billid);
                    if (po.getApprover() != null) {
                        approverName = po.getApprover().getFirstName() + " " + po.getApprover().getLastName();
                    }
                    currencyid = (po.getCurrency() == null) ? currencyid : po.getCurrency().getCurrencyID();
                    com = po.getCompany();
                    if (po.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(po.getTemplateid().getConfigstr());
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
                    String invoicePostText = po.getPostText() == null ? "" : po.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? po.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = po.getVendor().getAccount();
                    invno = po.getPurchaseOrderNumber();
                    dueDate = po.getDueDate();
                    entryDate = po.getOrderDate();
                    mainTax = po.getTax();
                    shipDate = po.getShipdate();
                    shipvia = po.getShipvia();
                    fob = po.getFob();
                } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                    sOrder = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), billid);
                    isRateIncludeGST = sOrder.isGstIncluded();
                    if (sOrder.getApprover() != null) {
                        approverName = sOrder.getApprover().getFirstName() + " " + sOrder.getApprover().getLastName();
                    }
                    currencyid = (sOrder.getCurrency() == null) ? currencyid : sOrder.getCurrency().getCurrencyID();
                    com = sOrder.getCompany();

                    if (sOrder.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(sOrder.getTemplateid().getConfigstr());
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
                    String invoicePostText = sOrder.getPostText() == null ? "" : sOrder.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? sOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = sOrder.getCustomer().getAccount();
                    invno = sOrder.getSalesOrderNumber();
                    dueDate = sOrder.getDueDate();
                    entryDate = sOrder.getOrderDate();
                    mainTax = sOrder.getTax();
                    shipDate = sOrder.getShipdate();
                    shipvia = sOrder.getShipvia();
                    fob = sOrder.getFob();

                } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                    isRateIncludeGST = pOrder.isGstIncluded();
                    currencyid = (pOrder.getCurrency() == null) ? currencyid : pOrder.getCurrency().getCurrencyID();
                    com = pOrder.getCompany();
                    if (pOrder.getApprover() != null) {
                        approverName = pOrder.getApprover().getFirstName() + " " + pOrder.getApprover().getLastName();
                    }
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
                        isCompanyLogo = false;
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                        String invoicePostText = pOrder.getPostText() == null ? "" : pOrder.getPostText();
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && pOrder.getTemplateid() != null) ? pOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)) ? pOrder.getTemplateid().getPreText() : CompanyPDFPRETEXT;
                    } else {
                        if (pOrder.getTemplateid() != null) {
                            config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
//    document = getTemplateConfig(document,writer);
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
                        String invoicePostText = pOrder.getPostText() == null ? "" : pOrder.getPostText();
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? pOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    }
                    cEntry = pOrder.getVendor().getAccount();
                    invno = pOrder.getPurchaseOrderNumber();
                    dueDate = pOrder.getDueDate();
                    entryDate = pOrder.getOrderDate();
                    mainTax = pOrder.getTax();
                    shipDate = pOrder.getShipdate();
                    shipvia = pOrder.getShipvia();
                    fob = pOrder.getFob();

                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                    isRateIncludeGST = quotation.isGstIncluded();
                    currencyid = (quotation.getCurrency() == null) ? currencyid : quotation.getCurrency().getCurrencyID();
                    com = quotation.getCompany();

                    if (quotation.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(quotation.getTemplateid().getConfigstr());
//    document = getTemplateConfig(document,writer);
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

                    String invoicePostText = quotation.getPostText() == null ? "" : quotation.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && quotation.getTemplateid() != null) ? quotation.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = quotation.getCustomer().getAccount();
                    invno = quotation.getquotationNumber();
                    dueDate = quotation.getDueDate();
                    entryDate = quotation.getQuotationDate();
                    mainTax = quotation.getTax();
                    shipDate = quotation.getShipdate();
                    shipvia = quotation.getShipvia();
                    fob = quotation.getFob();

                } else if (mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION) {
                    quotationVersion = (QuotationVersion) kwlCommonTablesDAOObj.getClassObject(QuotationVersion.class.getName(), billid);
                    currencyid = (quotationVersion.getCurrency() == null) ? currencyid : quotationVersion.getCurrency().getCurrencyID();
                    com = quotationVersion.getCompany();
                    if (quotationVersion.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(quotationVersion.getTemplateid().getConfigstr());
//    document = getTemplateConfig(document,writer);
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
                    String invoicePostText = quotationVersion.getPostText() == null ? "" : quotationVersion.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && quotationVersion.getTemplateid() != null) ? quotationVersion.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = quotationVersion.getCustomer().getAccount();
                    invno = quotationVersion.getVersion();
                    dueDate = quotationVersion.getDueDate();
                    entryDate = quotationVersion.getQuotationDate();
                    mainTax = quotationVersion.getTax();
                    shipDate = quotationVersion.getShipdate();
                    shipvia = quotationVersion.getShipvia();
                    fob = quotationVersion.getFob();
                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                    venquotation = (VendorQuotation) kwlCommonTablesDAOObj.getClassObject(VendorQuotation.class.getName(), billid);
                    isRateIncludeGST = venquotation.isGstIncluded();
                    currencyid = (venquotation.getCurrency() == null) ? currencyid : venquotation.getCurrency().getCurrencyID();
                    com = venquotation.getCompany();
                    if (venquotation.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(venquotation.getTemplateid().getConfigstr());
//   document = getTemplateConfig(document,writer);
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
                    String invoicePostText = venquotation.getPostText() == null ? "" : venquotation.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && venquotation.getTemplateid() != null) ? venquotation.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = venquotation.getVendor().getAccount();
                    invno = venquotation.getQuotationNumber();
                    dueDate = venquotation.getDueDate();
                    entryDate = venquotation.getQuotationDate();
                    mainTax = venquotation.getTax();
                    shipDate = venquotation.getShipdate();
                    shipvia = venquotation.getShipvia();
                    fob = venquotation.getFob();

                }  else if (mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION) {
                    vendorQuotationVersion = (VendorQuotationVersion) kwlCommonTablesDAOObj.getClassObject(VendorQuotationVersion.class.getName(), billid);
                    currencyid = (vendorQuotationVersion.getCurrency() == null) ? currencyid : vendorQuotationVersion.getCurrency().getCurrencyID();
                    com = vendorQuotationVersion.getCompany();
                    if (vendorQuotationVersion.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(vendorQuotationVersion.getTemplateid().getConfigstr());
//    document = getTemplateConfig(document,writer);
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
                    String invoicePostText = vendorQuotationVersion.getPostText() == null ? "" : vendorQuotationVersion.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && vendorQuotationVersion.getTemplateid() != null) ? vendorQuotationVersion.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = vendorQuotationVersion.getVendor().getAccount();
                    invno = vendorQuotationVersion.getVersion();
                    dueDate = vendorQuotationVersion.getDueDate();
                    entryDate = vendorQuotationVersion.getQuotationDate();
                    mainTax = vendorQuotationVersion.getTax();
                    shipDate = vendorQuotationVersion.getShipdate();
                    shipvia = vendorQuotationVersion.getShipvia();
                    fob = vendorQuotationVersion.getFob();
                } else if (mode == StaticValues.AUTONUM_RFQ) {
                    RFQ = (RequestForQuotation) kwlCommonTablesDAOObj.getClassObject(RequestForQuotation.class.getName(), billid);
//                    currencyid = (RFQ.getCurrency()==null)? currencyid : RFQ.getCurrency().getCurrencyID();
                    com = RFQ.getCompany();
                    if (RFQ.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(RFQ.getTemplateid().getConfigstr());
//   document = getTemplateConfig(document,writer);
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
//                    cEntry = venquotation.getVendor().getAccount();
                    invno = RFQ.getRfqNumber();
                    dueDate = RFQ.getDueDate();
                    entryDate = RFQ.getRfqDate();
//                    mainTax = RFQ.getTax();
//                    shipDate = venquotation.getShipdate();
//                    shipvia = venquotation.getShipvia();
//                    fob = venquotation.getFob();

                }else if(mode == StaticValues.AUTONUM_PURCHASEREQUISITION){
                    purchaseRequisition=(PurchaseRequisition)kwlCommonTablesDAOObj.getClassObject(PurchaseRequisition.class.getName(), billid);
                    com = purchaseRequisition.getCompany();
                    if (purchaseRequisition.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(purchaseRequisition.getTemplateid().getConfigstr());
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
                    invno = purchaseRequisition.getPrNumber();
                    dueDate = purchaseRequisition.getDueDate();
                    entryDate = purchaseRequisition.getRequisitionDate();
                }else {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    currencyid = (inv1.getCurrency() == null) ? currencyid : inv1.getCurrency().getCurrencyID();
                    com = inv1.getCompany();
                    cEntry = inv1.getCustomerEntry().getAccount();
                    invno = inv1.getBillingInvoiceNumber();
                    dueDate = inv1.getDueDate();

                    entryDate = inv1.getJournalEntry().getEntryDate();
                    poRefno = inv1.getPoRefNumber() == null ? "" : inv1.getPoRefNumber();
                    mainTax = inv1.getTax();
                    shipDate = inv1.getShipDate();
                    shipvia = inv1.getShipvia();
                    fob = inv1.getFob();
//inv1=(BillingInvoice)session.get(BillingInvoice.class,billid);
                }


//                Company com = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getCompany() : inv1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                if (isLetterHead && isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{70, 30});
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
//
//                if (mode != StaticValues.AUTONUM_BILLINGINVOICE) {
//                    cEntry = inv.getCustomerEntry().getAccount();
//                } else {
//                    cEntry = inv1.getCustomerEntry().getAccount();
//                }
                String theader = cEntry == cash ? messageSource.getMessage("acc.accPref.autoCS", null, (Locale) requestmap.get("locale")) : messageSource.getMessage("acc.accPref.autoInvoice", null, (Locale) requestmap.get("locale"));
                String datetheader = theader;
                if (mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_SALESORDER) {
                    if (sOrder.getLeaseOrMaintenanceSO() == 1) {
                        theader = messageSource.getMessage("acc.lease.order", null, (Locale) requestmap.get("locale"));
                    } else if (sOrder.getLeaseOrMaintenanceSO() == 3) {
                        theader = messageSource.getMessage("acc.consignment.order", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = messageSource.getMessage("acc.accPref.autoSO", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if ( pOrder != null && pOrder.isIsconsignment() ) {
                        theader = messageSource.getMessage("acc.venconsignment.order", null, (Locale) requestmap.get("locale"));
                    } else if ( pOrder != null && pOrder.isFixedAssetPO() ) {
                        theader = messageSource.getMessage("acc.field.assetPurchaseOrder", null, (Locale) requestmap.get("locale"));
                    } else { 
                        theader = messageSource.getMessage("acc.accPref.autoPO", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    if (quotation.isLeaseQuotation()) {
                        theader = messageSource.getMessage("acc.field.leaseQuotation", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = messageSource.getMessage("acc.accPref.autoCQN", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION) {
                    theader = messageSource.getMessage("acc.field.custquotationversion", null, (Locale) requestmap.get("locale"));
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION) {
                    theader = messageSource.getMessage("acc.field.venquotationversion", null, (Locale) requestmap.get("locale"));
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                     if ( venquotation != null && venquotation.isFixedAssetVQ() ) {
                         theader = messageSource.getMessage("acc.field.assetVendorQuotation", null, (Locale) requestmap.get("locale"));
                     } else {
                         theader = messageSource.getMessage("acc.accPref.autoVQN", null, (Locale) requestmap.get("locale"));
                     }
                    datetheader = theader;
                } else if (mode == StaticValues.AUTONUM_RFQ) {
                    theader = messageSource.getMessage("acc.accPref.autoRFQ", null, (Locale) requestmap.get("locale"));
                    datetheader = theader;
                }else if (mode == StaticValues.AUTONUM_PURCHASEREQUISITION) {
                    theader = messageSource.getMessage("acc.accPref.autoPRequisition", null, (Locale) requestmap.get("locale"));
                    datetheader = theader;
                }
                if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE) //Invoice header Label as Tax Invoice
                {
                    if (isLeaseFixedAsset == true) {
                        theader = "Lease Invoice";
                        datetheader = theader;
                    } else if (isConsignment) {
                        theader = "Consignment Sales Invoice";
                        datetheader = theader;
                    } else {
                        theader = "Tax Invoice";
                        datetheader = theader;
                    }
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
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
                cell1.setPaddingTop(140);
                mainTable.addCell(mainCell11);




//                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,invno,theader,formatter);


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


//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, (Locale)requestmap.get("locale"))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                String customerName = "";
                String customerEmail = "";
                String terms = "";
                String billTo = "";
                String billAddr = "";
                String shipAddr = "";
                String memo = "";
                String salesPerson = null;
                String billtoAddress = "";
                boolean isInclude = false; //Hiding or Showing P.O. NO field in single PDF 
                Iterator itr = null;
                linkHeader = "";
                String[] headerDetails = {"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                String[] headerDetailsForVrnet = {"Terms", "Due Date", "Sales Person"};//Header name for VRNET Comapny 
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true); 
//if(mode==StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_BILLINGINVOICE){
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
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in invoice table 
                        billAddr = inv.getBillTo() == null ? "" : inv.getBillTo();
                    }
                    customerEmail = inv.getCustomer() != null ? inv.getCustomer().getEmail() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    if (inv.getMasterSalesPerson() != null) {   //if User Class returns the Null Valu
                        salesPerson = inv.getMasterSalesPerson() != null ? inv.getMasterSalesPerson().getValue() : "";
                    } else {  //if salesperson class has no username
                        salesPerson = "";
                    }
                    if (inv.isIsconsignment()) {
                        linkHeader = "";
                    } else if (pref.isWithInvUpdate()) {
                        linkHeader = (inv.isFixedAssetInvoice()) ? "DO. No." : "SO/DO/CQ. No.";
                    } else {
                        linkHeader = "SO/CQ. No.";
                    }
                    terms = inv.getCustomer() != null ? inv.getCustomer().getCreditTerm().getTermname() : "";
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in invoice table 
                        shipAddr = inv.getShipTo() == null ? "" : inv.getShipTo();
                    }
                    itr = idresult.getEntityList().iterator();
                    memo = inv.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER) {
                    customerName = so.getCustomer().getName();
                    customerEmail = so.getCustomer() != null ? so.getCustomer().getEmail() : "";
                    shipAddr = so.getShipTo() != null ? so.getShipTo() : "";
                    terms = so.getTerm() != null ? so.getTerm().getTermname() : so.getCustomer() != null ? so.getCustomer().getCreditTerm().getTermname() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    billAddr = so.getBillTo() != null ? so.getBillTo() : "";
                    filter_names.add("salesOrder.ID");
                    filter_params.add(so.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getBillingSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = so.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                    customerName = po.getVendor().getName();
                    customerEmail = po.getVendor() != null ? po.getVendor().getEmail() : "";
                    terms = po.getTerm() != null ? po.getTerm().getTermname() : po.getVendor() != null ? po.getVendor().getDebitTerm().getTermname() : "";
                    billTo = "Supplier";
                    isInclude = false;
                    billAddr = po.getVendor().getAddress() != null ? po.getVendor().getAddress() : "";
                    shipAddr = po.getShipTo() == null ? "" : po.getShipTo();
                    billtoAddress = po.getBillTo() == null ? "" : po.getBillTo();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(po.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = po.getMemo();
                } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                    customerName = sOrder.getCustomer().getName();
                    customerEmail = sOrder.getCustomer() != null ? sOrder.getCustomer().getEmail() : "";
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in invoice table 
                        shipAddr = sOrder.getShipTo() == null ? "" : sOrder.getShipTo();
                    }
                    terms = sOrder.getTerm() != null ? sOrder.getTerm().getTermname() : sOrder.getCustomer() != null ? sOrder.getCustomer().getCreditTerm().getTermname() : "";
                    billTo = "Bill To";
                    isInclude = false;
//headerDetails[0] = "Quotation No.";
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in invoice table 
                        billAddr = sOrder.getBillTo() != null ? sOrder.getBillTo() : "";
                    }
                    filter_names.add("salesOrder.ID");
                    filter_params.add(sOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = sOrder.getMemo();
                } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
                        isCompanyLogo = false;
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                        String invoicePostText = pOrder.getPostText() != null ? pOrder.getPostText() : "";
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? pOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)) ? pOrder.getTemplateid().getPreText() : CompanyPDFPRETEXT;
                    }
                    customerName = pOrder.getVendor().getName();
                    customerEmail = pOrder.getVendor() != null ? pOrder.getVendor().getEmail() : "";
                    terms = pOrder.getTerm() != null ? pOrder.getTerm().getTermname() : pOrder.getVendor() != null ? pOrder.getVendor().getDebitTerm().getTermname() : "";
                    billTo = "Supplier";
                    isInclude = false;
//headerDetails[0] = "SO No.";                    
                    addrParams.put("vendorid", pOrder.getVendor().getID());
                    addrParams.put("companyid", pOrder.getCompany().getCompanyID());                    
                    addrParams.put("isBillingAddress", true); 
                    billAddr = accountingHandlerDAOobj.getVendorAddress(addrParams);//for supplier billaddr will be vendor address
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in invoice table 
                        shipAddr = pOrder.getShipTo() == null ? "" : pOrder.getShipTo();
                    }

                    billtoAddress = CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billtoAddress)) { //For old record in which addresses were saved in invoice table 
                        billtoAddress = pOrder.getBillTo() == null ? "" : pOrder.getBillTo();
                    }
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(pOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    if(!isExpenseInv){
                        idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                    }else{
                        idresult = accPurchaseOrderobj.getExpensePurchaseOrderDetails(invRequestParams);
                        isInclude=false;
                    }
                    itr = idresult.getEntityList().iterator();
                    memo = pOrder.getMemo();
                } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                    customerName = quotation.getCustomer().getName();
                    customerEmail = quotation.getCustomer() != null ? quotation.getCustomer().getEmail() : "";
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in quotation table 
                        shipAddr = quotation.getShipTo() == null ? "" : quotation.getShipTo();
                    }
                    terms = quotation.getTerm() != null ? quotation.getTerm().getTermname() : (quotation.getCustomer() != null ? quotation.getCustomer().getCreditTerm().getTermname() : "");
                    billTo = "Bill To";
                    isInclude = false;
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in quotation table 
                        billAddr = quotation.getBillTo() != null ? quotation.getBillTo() : "";
                    }
                    filter_names.add("quotation.ID");
                    filter_params.add(quotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = quotation.getMemo();
                } else if (mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION) {
                    customerName = quotationVersion.getCustomer().getName();
                    customerEmail = quotationVersion.getCustomer() != null ? quotationVersion.getCustomer().getEmail() : "";
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(quotationVersion.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in quotation table 
                        shipAddr = quotationVersion.getShipTo() == null ? "" : quotationVersion.getShipTo();
                    }
                    terms = quotationVersion.getCustomer() != null ? quotationVersion.getCustomer().getCreditTerm().getTermname() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(quotationVersion.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in quotation table 
                        billAddr = quotationVersion.getBillTo() != null ? quotationVersion.getBillTo() : "";
                    }
                    filter_names.add("quotationversion.ID");
                    filter_params.add(quotationVersion.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getQuotationVersionDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = quotationVersion.getMemo();
                }else if (mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION) {
                    customerName = vendorQuotationVersion.getVendor().getName();
                    customerEmail = vendorQuotationVersion.getVendor() != null ? vendorQuotationVersion.getVendor().getEmail() : "";
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(vendorQuotationVersion.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //For old record in which addresses were saved in quotation table 
                        shipAddr = vendorQuotationVersion.getShipTo() == null ? "" : vendorQuotationVersion.getShipTo();
                    }
                    terms = vendorQuotationVersion.getVendor() != null ? vendorQuotationVersion.getVendor().getDebitTerm().getTermname() : "";
                    billTo = "Bill To";
                    isInclude = false;
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(vendorQuotationVersion.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in quotation table 
                        billAddr = vendorQuotationVersion.getBillTo() != null ? vendorQuotationVersion.getBillTo() : "";
                    }
                    filter_names.add("quotationversion.ID");
                    filter_params.add(vendorQuotationVersion.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getQuotationVersionDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = vendorQuotationVersion.getMemo();
                } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                    customerName = venquotation.getVendor().getName();
                    customerEmail = venquotation.getVendor() != null ? venquotation.getVendor().getEmail() : "";
//shipTo=venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
                    terms = venquotation.getVendor() != null ? venquotation.getVendor().getDebitTerm().getTermname() : "";
                    billTo = "Supplier";
                    isInclude = false;
                    
                    addrParams.put("vendorid", venquotation.getVendor().getID());
                    addrParams.put("companyid", venquotation.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", true);
                    billAddr = accountingHandlerDAOobj.getVendorAddress(addrParams);//for supplier billaddr will be vendor address
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(venquotation.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //Old record in which addresses were saved in venquotation table 
                        shipAddr = venquotation.getShipTo() == null ? "" : venquotation.getShipTo();
                    }

                    billtoAddress = CommonFunctions.getBillingShippingAddressWithAttn(venquotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billtoAddress)) { //For old record in which addresses were saved in venquotation table 
                        billtoAddress = venquotation.getBillTo() == null ? "" : venquotation.getBillTo();
                    }
                    filter_names.add("vendorquotation.ID");
                    filter_params.add(venquotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = venquotation.getMemo();
                } else if (mode == StaticValues.AUTONUM_RFQ) {
//                    customerName = RFQ.getVendor().getName();
//                    customerEmail= venquotation.getVendor()!=null?venquotation.getVendor().getEmail():"";
//shipTo=venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
//                    terms=venquotation.getVendor()!=null?venquotation.getVendor().getDebitTerm().getTermname():"";
                    billTo = "Supplier";
                    isInclude = false;
//                    billAddr= venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
                    filter_names.add("requestforquotation.ID");
                    filter_params.add(RFQ.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getRFQDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = RFQ.getMemo();
                } else if (mode == StaticValues.AUTONUM_PURCHASEREQUISITION) {
                    billTo = "Supplier";
                    isInclude = false;
                    filter_names.add("purchaserequisition.ID");
                    filter_params.add(purchaseRequisition.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getPurchaseRequisitionDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = purchaseRequisition.getMemo();
                }
//                cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE);
                if (mode != StaticValues.AUTONUM_RFQ) {
                    PdfPTable addressMainTable = null, addressMainTable1 = null;
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        addressMainTable = ExportRecordHandler.getAddressTableForBCHL(accPurchaseOrderobj, customerName, billAddr, customerEmail, pOrder, currencyid);
                    } else {
                        if ((mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_VENQUOTATION) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                            addressMainTable1 = ExportRecordHandler.getSupplierAddress(customerName, billAddr, customerEmail, billTo);
                            addressMainTable = ExportRecordHandler.getAddressTable(null, billtoAddress, null, "Bill To", shipAddr, addShipTo);
                        } else {
                            addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddr, customerEmail, billTo, shipAddr, addShipTo);
                        }
                    }
                    if (!addShipTo) {
                        PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj, salesPerson, entryDate, invno, "", "", terms, currencyid, formatter);
                        PdfPCell cel3 = new PdfPCell(shipToTable);
                        cel3.setBorder(0);
                        addressMainTable.addCell(cel3);
                    }
                    if ((mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_VENQUOTATION) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                        mainCell12 = new PdfPCell(addressMainTable1);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
                        mainCell12.setPaddingLeft(10);
                        mainCell12.setPaddingRight(10);
                        mainCell12.setPaddingBottom(5);
                        mainTable.addCell(mainCell12);
                    }

                    mainCell12 = new PdfPCell(addressMainTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);
                    mainTable.setSplitLate(false);
                    String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE) {
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTableForVRNET(headerDetailsForVrnet, referenceNumber, terms, dueDate, formatter, salesPerson);
                        mainCell12 = new PdfPCell(detailsTable);
                        mainCell12.setBorder(0);
                        mainCell12.setPaddingTop(5);
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
//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainCell14.setPadding(10);
//                mainTable.addCell(mainCell14);

                boolean companyFlag = (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION || mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION || mode == StaticValues.AUTONUM_SALESORDER));
             
                PdfPTable table = null;
                PdfPCell invcell = null;
                if (isExpenseInv && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                    String[] header = {"S.No.", "Account", "Description", "PRICE " + authHandlerDAOObj.getCurrency(currencyid, isCurrencyCode), "LINE TOTAL " + authHandlerDAOObj.getCurrency(currencyid, isCurrencyCode)};
                    table = new PdfPTable(5);
                    globalTableHeader = header;
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{6, 29, 29, 18, 18});
                    for (int i = 0; i < header.length; i++) {
                        invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBackgroundColor(Color.LIGHT_GRAY);
                        invcell.setBorder(Rectangle.BOX);
                        invcell.setPadding(3);
                        table.addCell(invcell);
                    }
                } else {

                List<String> headerList = new ArrayList<String>();
                if (addShipTo) {
                    headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, (Locale) requestmap.get("locale")));//S.No.
                    if (!linkHeader.equalsIgnoreCase("")) {
                        headerList.add(linkHeader);//SO. No.
                    }
                } else {
                    headerList.add("Item");
                }
//for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name in header               
                if (!(companyFlag)) {
                    headerList.add(messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")));//Product Name
                }

                String countryMsgKey = "";
                if (!com.getCountry().getID().equals("106")) {   // Check for Indonesia company
                    countryMsgKey = messageSource.getMessage("acc.invoice.gridUnitPriceIncludingGST", null, (Locale) requestmap.get("locale"));
                } else {
                    countryMsgKey = messageSource.getMessage("acc.invoice.gridUnitPriceIncludingVAT", null, (Locale) requestmap.get("locale"));
                }
                if (addShipTo) {
                    headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, (Locale) requestmap.get("locale")));//Description
                    headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, (Locale) requestmap.get("locale")));//Qty.
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if (isRateIncludeGST) {
                            headerList.add(countryMsgKey + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Unit Price
                        } else {
                                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Amount
                        }
                            headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Amount
                    }
                } else {
                    headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, (Locale) requestmap.get("locale")));//Description
                    headerList.add("Part No.");
                    headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, (Locale) requestmap.get("locale")));//Qty.
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if(isRateIncludeGST) {
                                headerList.add(countryMsgKey + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Unit Price
                        } else {
                                headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Unit Price
                        }
                            headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(currencyid,isCurrencyCode));//Amount
                    }

                }
                String[] header = (String[]) headerList.toArray(new String[0]);
                globalTableHeader = header;
                table = null;
                if (mode == StaticValues.AUTONUM_RFQ) {
                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
                    productHeaderTableGlobalNo = 1;
                } else {
                    table = ExportRecordHandler.getTable(linkHeader, true);
                    productHeaderTableGlobalNo = 2;
                }
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                    productHeaderTableGlobalNo = 5;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForINVOICE();
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }

               
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
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
                table.setSplitLate(false);
                if (companyFlag) {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForINVOICE();
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                }
                
                }
               
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.companyKey, CompanyID);
                requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                requestParams.put(Constants.df, formatter);
                KwlReturnObject bAmt = null;
                InvoiceDetail row = null;
                BillingInvoiceDetail row1 = null;
                BillingSalesOrderDetail row3 = null;
                BillingPurchaseOrderDetail row4 = null;
                SalesOrderDetail row5 = null;
                PurchaseOrderDetail row6 = null;
                QuotationDetail row7 = null;
                VendorQuotationDetail row8 = null;
                RequestForQuotationDetail row9 = null;
                QuotationVersionDetail row10 = null;
                VendorQuotationVersionDetail row11 = null;
                PurchaseRequisitionDetail  row12=null;
                Set<String> uniqueProductTaxList = new HashSet<String>();
                int index = 0;
                double totalTaxAmt=0d;
                double totalOtherTermNonTaxableAmount = 0 ;//only For India Country;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    String partNo = "";
                    Phrase phrase1 = new Phrase();
                    Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String linkTo = "-";
                    double partamount=0;
                    String description = "";
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                                description = StringUtil.DecodeText(row.getDescription());
                                prodDesc =  StringUtil.replaceFullHTML(description.replace("<br>","\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row.getInventory().getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>","\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
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
                        
                        /*Checking whether Invoice is created by linked with SO,Partially*/
                        if (row.getInvoice().isPartialinv()) {
                            partamount = row.getPartamount()/100;
                        }
                        quantity = row.getInventory().getQuantity();
                        rate = isRateIncludeGST ? row.getRateincludegst() : row.getRate();
                        discount = row.getDiscount();
                        uom = row.getInventory().getUom() == null ? row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : row.getInventory().getUom().getNameEmptyforNA();
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
                        prodDesc = "-";
                        quantity = row3.getQuantity();
                        rate = row3.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountOrder = (row3.getDiscountispercent() == 1) ? rateInCurr * quantity * row3.getDiscount() / 100 : row3.getDiscount();
                    } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER) {
                        row4 = (BillingPurchaseOrderDetail) itr.next();
                        prodName = row4.getProductDetail();
                        prodDesc = "-";
                        quantity = row4.getQuantity();
                        rate = row4.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountOrder = (row4.getDiscountispercent() == 1) ? rateInCurr * quantity * row4.getDiscount() / 100 : row4.getDiscount();
                    } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                        row5 = (SalesOrderDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row5.getDescription())) {
                            description = StringUtil.DecodeText(row5.getDescription());
                            prodDesc =  StringUtil.replaceFullHTML(description.replace("<br>","\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row5.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row5.getProduct().getDescription());
                                prodDesc =  StringUtil.replaceFullHTML(description.replace("<br>","\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row5.getProduct().getName();
                        quantity = row5.getQuantity();
                        rate = isRateIncludeGST ? row5.getRateincludegst() : row5.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row5.getDiscountispercent() == 1) ? rateInCurr * quantity * row5.getDiscount() / 100 : row5.getDiscount();
                        uom = row5.getUom() ==null?row5.getProduct().getUnitOfMeasure() == null ? "" :row5.getProduct().getUnitOfMeasure().getNameEmptyforNA():row5.getUom().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        if (!isExpenseInv) {
                            row6 = (PurchaseOrderDetail) itr.next();
                            if (!StringUtil.isNullOrEmpty(row6.getDescription())) {
                                description = StringUtil.DecodeText(row6.getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            } else {
                                if (!StringUtil.isNullOrEmpty(row6.getProduct().getDescription())) {
                                    description = StringUtil.DecodeText(row6.getProduct().getDescription());
                                    prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                    prodDesc=Jsoup.parse(prodDesc).text();
                                }
                            }
                            prodName = row6.getProduct().getName();
                            quantity = row6.getQuantity();
                            rate = isRateIncludeGST ? row6.getRateincludegst() : row6.getRate();
                            bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                            double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                            discountOrder = (row6.getDiscountispercent() == 1) ? rateInCurr * quantity * row6.getDiscount() / 100 : row6.getDiscount();
                            uom = row6.getUom() ==null?row6.getProduct().getUnitOfMeasure() == null ? "" :row6.getProduct().getUnitOfMeasure().getNameEmptyforNA():row6.getUom().getNameEmptyforNA();
                        } 
                    } else if (mode == StaticValues.AUTONUM_QUOTATION) {
                        row7 = (QuotationDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row7.getDescription())) {
                            description = StringUtil.DecodeText(row7.getDescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row7.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row7.getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row7.getProduct().getName();
                        quantity = row7.getQuantity();
                        rate = isRateIncludeGST ? row7.getRateincludegst() : row7.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row7.getDiscountispercent() == 1) ? rateInCurr * quantity * row7.getDiscount() / 100 : row7.getDiscount();
                        uom = row7.getUom() ==null?row7.getProduct().getUnitOfMeasure() == null ? "" :row7.getProduct().getUnitOfMeasure().getNameEmptyforNA():row7.getUom().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION) {
                        row10 = (QuotationVersionDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row10.getDescription())) {
                            description = StringUtil.DecodeText(row10.getDescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row10.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row10.getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row10.getProduct().getName();
                        quantity = row10.getQuantity();
                        rate = row10.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row10.getDiscountispercent() == 1) ? rateInCurr * quantity * row10.getDiscount() / 100 : row10.getDiscount();
                        uom = row10.getUom() ==null?row10.getProduct().getUnitOfMeasure() == null ? "" :row10.getProduct().getUnitOfMeasure().getNameEmptyforNA():row10.getUom().getNameEmptyforNA();
                    }else if (mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION) {
                        row11 = (VendorQuotationVersionDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row11.getDescription())) {
                            description = StringUtil.DecodeText(row11.getDescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row11.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row11.getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row11.getProduct().getName();
                        quantity = row11.getQuantity();
                        rate = row11.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row11.getDiscountispercent() == 1) ? rateInCurr * quantity * row11.getDiscount() / 100 : row11.getDiscount();
                        uom = row11.getUom() ==null?row11.getProduct().getUnitOfMeasure() == null ? "" :row11.getProduct().getUnitOfMeasure().getNameEmptyforNA():row11.getUom().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                        row8 = (VendorQuotationDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row8.getDescription())) {
                            description = StringUtil.DecodeText(row8.getDescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row8.getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row8.getProduct().getName();
                        quantity = row8.getQuantity();
                        rate = isRateIncludeGST ? row8.getRateincludegst() : row8.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row8.getDiscountispercent() == 1) ? rateInCurr * quantity * row8.getDiscount() / 100 : row8.getDiscount();
                        uom = row8.getUom() ==null?row8.getProduct().getUnitOfMeasure() == null ? "" :row8.getProduct().getUnitOfMeasure().getNameEmptyforNA():row8.getUom().getNameEmptyforNA();
                    } else if (mode == StaticValues.AUTONUM_RFQ) {
                        row9 = (RequestForQuotationDetail) itr.next();
                        if (!StringUtil.isNullOrEmpty(row9.getProduct().getDescription())) {
                            description = StringUtil.DecodeText(row9.getProduct().getDescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        }
                        prodName = row9.getProduct().getName();
                        quantity = row9.getQuantity();
//                        rate = row8.getRate() ;
//                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
//                        double rateInCurr = (Double) bAmt.getEntityList().get(0);
//                        discountQuotation = (row8.getDiscountispercent() == 1)? rateInCurr*quantity *row8.getDiscount()/100 : row8.getDiscount();
                        uom = row9.getUom() ==null?row9.getProduct().getUnitOfMeasure() == null ? "" :row9.getProduct().getUnitOfMeasure().getNameEmptyforNA():row9.getUom().getNameEmptyforNA();
                     }else if (mode == StaticValues.AUTONUM_PURCHASEREQUISITION) {
                        row12 = (PurchaseRequisitionDetail) itr.next();
                        /**
                         * If Product description is edited from product grid
                         */
                        if (!StringUtil.isNullOrEmpty(row12.getProductdescription())) {
                            description = StringUtil.DecodeText(row12.getProductdescription());
                            prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row12.getProduct().getDescription())) {
                                description = StringUtil.DecodeText(row12.getProduct().getDescription());
                                prodDesc = StringUtil.replaceFullHTML(description.replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row12.getProduct().getName();
                        quantity = row12.getQuantity();
                        rate = row12.getRate();
                        uom = row12.getUom() ==null?row12.getProduct().getUnitOfMeasure() == null ? "" :row12.getProduct().getUnitOfMeasure().getNameEmptyforNA():row12.getUom().getNameEmptyforNA();
                    } else {
                        row1 = (BillingInvoiceDetail) itr.next();
                        prodName = row1.getProductDetail();
                        prodDesc = "-";
                        quantity = row1.getQuantity();
                        rate = row1.getRate();
                        discount = row1.getDiscount() != null ? row1.getDiscount() : null;
//                        uom = row1.getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getInventory().getProduct().getUnitOfMeasure().getName();
                        if (row1.getSalesOrderDetail() != null) {
                            linkTo = row1.getSalesOrderDetail().getSalesOrder().getSalesOrderNumber();
                        }
                    }
                    
                     if (isExpenseInv && mode ==StaticValues.AUTONUM_PURCHASEORDER) {
                        ExpensePODetail  exprow= (ExpensePODetail) itr.next();
                        invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
//Added Description in VI PDF Report for Expense TAB
                        if (!StringUtil.isNullOrEmpty(exprow.getDescription())) {
                            invcell = createCell(StringUtil.replaceFullHTML(StringUtil.DecodeText(exprow.getDescription()).replace("<br>", "\n")), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        } else {
                            invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }
                        table.addCell(invcell);
                        invcell = createCell(authHandler.formattedAmount(exprow.getRate(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        amount1 = exprow.getRate();
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {   //For Discount Row
                            //If discount is selected, then line level total ll be "Total-Discount"
                            amount1 -= exprow.getDiscount().getDiscountValue();
                            invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                            
                             invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("Discount", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, exprow.getDiscount(), currencyid, CompanyID);
                            invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            invcell.setPadding(5);
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            //amount1 -= exprow.getDiscount().getDiscountValue();
                        } else {
                            invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                        }

                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable = false;
                        String rowTaxName = "";
                       requestParams = requestmap;
                        if (exprow != null && exprow.getTax() != null) {
                            requestParams.put("transactiondate", exprow.getPurchaseOrder().getOrderDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = exprow.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = exprow.getRowTaxAmount();
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }

                        }
                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                             invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell(rowTaxName + "  Tax", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

//                            invcell = createCell(ExportRecordHandler.getFormattedAmount(amount1 * rowTaxPercent / 100), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            invcell = createCell(authHandler.formattedAmount(rowTaxAmount, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                            
                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                        }
                        if (isRateIncludeGST) {
                            amount1 -= rowTaxAmount;
                        }
                        totalTaxAmt += rowTaxAmount;
                        if (exprow.isIsdebit()) {//In case of Debit type
                        total += amount1;
                        } else {// In case of credit type
                            total -= amount1;
                        }
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);

                    }else{
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
                         invcell = createCellAllowingChinese(prodName, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    }
// The BCHL and VRNet is using special HTML format for description in very old release
// Hold/update the change request before sending this codebase to them
// invcell = ExportRecordHandler.getHtmlCellTable(prodDesc, baseUrl);
// invcell.setBorder(12);
// end BCHL and VRNet exception
                    invcell = createCellAllowingChinese(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (!addShipTo) {
                        invcell = createCell(partNo, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    }

//String qtyStr = Double.toString(quantity);
// if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                    String qtyStr = authHandler.formattedQuantity(quantity, CompanyID);//For with-Inventory flow, Don't show decimal point as inventory has integer value
                    if(com.getTemplateflag() == Constants.Tony_FiberGlass_templateflag) {
                        qtyStr = authHandler.formattingDecimalForQuantity(quantity, CompanyID);
                    } else if(com.getTemplateflag() == Constants.lsh_templateflag) {
                        DecimalFormat df = new DecimalFormat("0");
                        qtyStr = df.format(quantity);
                    }
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if (mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION || mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION|| mode == StaticValues.AUTONUM_VENQUOTATION) {
                            bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                            double rateInBase = rate;//(Double) bAmt.getEntityList().get(0);
                            rate = rateInBase;
                        }
                        invcell = createCell(authHandler.getFormattedUnitPrice(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

                        amount1 = authHandler.round(rate * quantity, CompanyID);
                      /* If Exported Invoice is Created by linked with SO partially*/
                        if(partamount!=0 && mode == StaticValues.AUTONUM_INVOICE){
                         amount1=authHandler.round(amount1*partamount, CompanyID);  
                       } 
                        

//                        invcell = createCell(ExportRecordHandler.getFormattedAmount(amount1), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);

                        if (discount != null) {
                            if (row.getInvoice().isPartialinv()) {
                                amount1 -= (authHandler.round(row.getPartialDiscount(), CompanyID));
                            }else{
                                amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (authHandler.round(row.getDiscount().getDiscountValue(), CompanyID)) : (authHandler.round(row1.getDiscount().getDiscountValue(), CompanyID));
                            }
                        }
                        if (discountQuotation != 0) {
                            amount1 -= authHandler.round(discountQuotation, CompanyID);
                        }
                        if (discountOrder != 0) {
                            amount1 -= authHandler.round(discountOrder, CompanyID);
                            discountQuotation = discountOrder;
                        }
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        double OtherTermNonTaxableAmount = 0, LineLevelTaxesAmt = 0; //only for INDIA Country
                        boolean isRowTaxApplicable = false;
                        String rowTaxName = "";
                        if (row != null ) {
                            if (row.getTax() != null) {
                                requestParams.put("transactiondate", entryDate);
                                requestParams.put("taxid", row.getTax().getID());
                                uniqueProductTaxList.add(row.getTax().getID());
                                KwlReturnObject result = accTaxObj.getTax(requestParams);
                                List taxList = result.getEntityList();
                                Object[] taxObj = (Object[]) taxList.get(0);
                                rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                                if (taxObj[1] != null) {
                                    isRowTaxApplicable = true;
                                }
                                rowTaxName = row.getTax().getName();
                                if (row.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. - DATE - 28 -Jan-2014
                                    if (isRowTaxApplicable) {
                                        rowTaxAmount = row.getRowTaxAmount()+row.getRowTermTaxAmount();
                                    }
                                } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                    rowTaxAmount = amount1 * rowTaxPercent / 100;
                                }
                            }
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row.getRowTermAmount();
                                OtherTermNonTaxableAmount = row.getOtherTermNonTaxableAmount();
                            }
                        } else if (row1 != null && row1.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row1.getTax().getName();
                            if (row1.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. - DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        } else if (row3 != null && row3.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row3.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row3.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row3.getRowTaxAmount();
                            }
                        } else if (row4 != null && row4.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row4.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row4.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row4.getRowTaxAmount();
                            }
                        } else if (row5 != null ) {
                            if (row5.getTax() != null) {
                                requestParams.put("transactiondate", entryDate);
                                requestParams.put("taxid", row5.getTax().getID());
                                uniqueProductTaxList.add(row5.getTax().getID());
                                KwlReturnObject result = accTaxObj.getTax(requestParams);
                                List taxList = result.getEntityList();
                                Object[] taxObj = (Object[]) taxList.get(0);
                                rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                                if (taxObj[1] != null) {
                                    isRowTaxApplicable = true;
                                }
                                rowTaxName = row5.getTax().getName();
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row5.getRowTaxAmount();
                                }
                            }
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row5.getRowtermamount();
                                OtherTermNonTaxableAmount = row5.getOtherTermNonTaxableAmount();
                            }
                        } else if (row6 != null ) {
                            if (row6.getTax() != null) {
                                requestParams.put("transactiondate", entryDate);
                                requestParams.put("taxid", row6.getTax().getID());
                                uniqueProductTaxList.add(row6.getTax().getID());
                                KwlReturnObject result = accTaxObj.getTax(requestParams);
                                List taxList = result.getEntityList();
                                Object[] taxObj = (Object[]) taxList.get(0);
                                rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                                if (taxObj[1] != null) {
                                    isRowTaxApplicable = true;
                                }
                                rowTaxName = row6.getTax().getName();
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row6.getRowTaxAmount();
                                }
                            }
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row6.getRowTermAmount();
                                OtherTermNonTaxableAmount = row6.getOtherTermNonTaxableAmount();
                            }
                        } else if (row7 != null) {
                            if (row7.getTax() != null) {
                                requestParams.put("transactiondate", entryDate);
                                requestParams.put("taxid", row7.getTax().getID());
                                uniqueProductTaxList.add(row7.getTax().getID());
                                KwlReturnObject result = accTaxObj.getTax(requestParams);
                                List taxList = result.getEntityList();
                                Object[] taxObj = (Object[]) taxList.get(0);
                                rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                                if (taxObj[1] != null) {
                                    isRowTaxApplicable = true;
                                }
                                rowTaxName = row7.getTax().getName();
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row7.getRowTaxAmount();
                                }
                            }
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row7.getRowtermamount();
                                OtherTermNonTaxableAmount = row7.getOtherTermNonTaxableAmount();
                            }
                        } else if (row10 != null && row10.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row10.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row10.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row10.getRowTaxAmount();
                            }
                        } else if (row8 != null) {
                            if (row8.getTax() != null) {
                                requestParams.put("transactiondate", entryDate);
                                requestParams.put("taxid", row8.getTax().getID());
                                uniqueProductTaxList.add(row8.getTax().getID());
                                KwlReturnObject result = accTaxObj.getTax(requestParams);
                                List taxList = result.getEntityList();
                                Object[] taxObj = (Object[]) taxList.get(0);
                                rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                                if (taxObj[1] != null) {
                                    isRowTaxApplicable = true;
                                }
                                rowTaxName = row8.getTax().getName();
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row8.getRowTaxAmount();
                                }
                            }
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row8.getRowTermAmount();
                                OtherTermNonTaxableAmount = row8.getOtherTermNonTaxableAmount();
                            }
                        }
                        invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        totalTaxAmt+=rowTaxAmount;
                        if(extraCompanyPreferences.getLineLevelTermFlag() == 1){
                            totalTaxAmt += LineLevelTaxesAmt ;
                            totalOtherTermNonTaxableAmount += OtherTermNonTaxableAmount ;
                        }
                        double amountForTax = amount1;
                       if (isRateIncludeGST) {//If rate is including the GST then for Subtotal remove the tax
                            total += amount1 - rowTaxAmount;
                        } else {
                            total += amount1;
                        }
                        if (!isRateIncludeGST) {
                            amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
//                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
//                                amount1 += LineLevelTaxesAmt;//amount1+=amount1*rowTaxPercent/100;
//                                amount1 += OtherTermNonTaxableAmount;//amount1+=amount1*rowTaxPercent/100;
//                            }
                        }
                        
                        if(row != null && row.getInvoice() != null && row.getInvoice().isPartialinv()){
                            table = ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table, currencyid, discount,mode, linkHeader,row.getPartialDiscount(),CompanyID);
                        }else if (discount != null || discountQuotation != 0) {  //For Discount Row
                            table = ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table, currencyid, discount, discountQuotation, mode, linkHeader);
                        }

                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                            table = ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table, rowTaxName, currencyid, amountForTax, rowTaxPercent, mode, linkHeader, rowTaxAmount);
                        }
                        
                    } else {
//                        invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
//                        invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
                    }

                  }
                }        
//                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
//                if (mode == StaticValues.AUTONUM_RFQ) {
//                    table = ExportRecordHandler.getBlankTableForReportForRFQ();
//                } else {
//                    table = ExportRecordHandler.getTable(linkHeader, true);
//                }
//for Company VRNET and sales,Invoice and Customer Quotation for poduct name hidden 
                if (companyFlag) {
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        table = ExportRecordHandler.getBlankTableReportForINVOICE();   //create table with 6 columns for extra row and Bottom 
                    } else {
                        table = ExportRecordHandler.getBlankTableReportForNonINVOICE(); //create table with 5 columns for extra row and Bottom
                    }
                }
                int cellCount = 0, lastRowCellCount = 0;
                if ((!linkHeader.equalsIgnoreCase(""))) {// for link header column in Invoice 
                    if (companyFlag) {      // if Company VRNET and sales,Invoice and Customer Quotation then 7 column 
                        cellCount = 60;
                        lastRowCellCount = 54;
                    } else {
// if Company VRNET and sales,Invoice and Customer Quotation then 6 column    
                        cellCount = 70;
                        lastRowCellCount = 63;
// for 10 blank rows 10*7=70 and last row 7*9=63
                    }
                } else {
                    if (companyFlag) { //if Company VRNET and sales,Invoice and Customer Quotation then 5 columns     
                        cellCount = 50;
                        lastRowCellCount = 45;
                    } else {//if Company VRNET and sales,Invoice and Customer Quotation then 6 columns
                        if(isExpenseInv && mode ==StaticValues.AUTONUM_PURCHASEORDER){
                            cellCount = 50;
                           lastRowCellCount = 45;
                        }else{
                            cellCount = 60;
                            lastRowCellCount = 54;
                        }
                    }
                }

                for (int j = 1; j <= cellCount; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if (j > lastRowCellCount && !companyFlag) {
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                    }
                    table.addCell(invcell);
                }
                if (companyFlag) {
                    int belowProdTableContent = 390;
                    mainTable.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * mainTable.getWidthPercentage() / 100);
                    mainTable.calculateHeightsFast();
                    float aboveProdTableContent = mainTable.getTotalHeight();
                    table.setTotalWidth((PageSize.A4.getWidth() - document.leftMargin()
                            - document.rightMargin()) * table.getWidthPercentage() / 100);
                    table.calculateHeightsFast();
                    int blankRowHeight = 4;
                    float addBlankRows = (document.getPageSize().getHeight() - belowProdTableContent - aboveProdTableContent - table.getTotalHeight()) / blankRowHeight;
                    int noOfCols = cellCount / 10;
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
                        table.addCell(invcell);
                    }
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space
                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);



//table = getBlankTable();
                table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{40, 60});

//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }
                if (mode != StaticValues.AUTONUM_RFQ && mode!=StaticValues.AUTONUM_PURCHASEREQUISITION) {
                    PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//cell3.setColspan(6);
                    table.addCell(cell3);
//                if(mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER){
//                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, total, cEntry.getCurrency().getCurrencyID(), entryDate, 0);
//                    double baseTotalAmount = (Double) bAmt.getEntityList().get(0);
//                    total = baseTotalAmount;
//                }
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
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
                    String mainTaxName = "";
                    if (mainTax != null) { //Get tax percent
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", mainTax.getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        mainTaxName = mainTax.getName();
                    }

                    double term = 0;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
//                        ExportRecordHandler.appendTermDetails(accInvoiceDAOobj,authHandlerDAOObj,inv, table, currencyid,mode);
                        totalDiscount = inv.getDiscount();
                        totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : totalTaxAmt;
                        mainTaxName = inv.getTax() != null ? inv.getTax().getName() : "";
                        totalAmount = inv.getCustomerEntry().getAmount();
                    } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION || mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_VENQUOTATION) {
                        totalAmount = total;
//                        term=0;

//                           total+=term;
//                           totalAmount+=term;
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
                        } else if (mode == StaticValues.AUTONUM_CUSTOMERQUOTATIONVERSION && quotationVersion.getDiscount() != 0) {
                            if (!quotationVersion.isPerDiscount()) {
                                discountTotalQuotation = quotationVersion.getDiscount();
                                total = total - quotationVersion.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * quotationVersion.getDiscount() / 100;
                                total -= (total * quotationVersion.getDiscount() / 100);
                                totalAmount = total;
                            }
                        }  else if (mode == StaticValues.AUTONUM_VENDORQUOTATIONVERSION && vendorQuotationVersion.getDiscount() != 0) {
                            if (!vendorQuotationVersion.isPerDiscount()) {
                                discountTotalQuotation = vendorQuotationVersion.getDiscount();
                                total = total - vendorQuotationVersion.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * vendorQuotationVersion.getDiscount() / 100;
                                total -= (total * vendorQuotationVersion.getDiscount() / 100);
                                totalAmount = total;
                            }
                        }else if (mode == StaticValues.AUTONUM_VENQUOTATION && venquotation.getDiscount() != 0) {
                            if (!venquotation.isPerDiscount()) {
                                discountTotalQuotation = venquotation.getDiscount();
                                total = total - venquotation.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * venquotation.getDiscount() / 100;
                                total -= (total * venquotation.getDiscount() / 100);
                                totalAmount = total;
                            }
                        } else if (mode == StaticValues.AUTONUM_PURCHASEORDER && pOrder.getDiscount() != 0) {
                            if (!pOrder.isPerDiscount()) {
                                discountTotalQuotation = pOrder.getDiscount();
                                total = total - pOrder.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * pOrder.getDiscount() / 100;
                                total -= (total * pOrder.getDiscount() / 100);
                                totalAmount = total;
                            }
                        } else if (mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER && po.getDiscount() != 0) {
                            if (!po.isPerDiscount()) {
                                discountTotalQuotation = po.getDiscount();
                                total = total - po.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * po.getDiscount() / 100;
                                total -= (total * po.getDiscount() / 100);
                                totalAmount = total;
                            }
                        } else if (mode == StaticValues.AUTONUM_SALESORDER && sOrder.getDiscount() != 0) {
                            if (!sOrder.isPerDiscount()) {
                                discountTotalQuotation = sOrder.getDiscount();
                                total = total - sOrder.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * sOrder.getDiscount() / 100;
                                total -= (total * sOrder.getDiscount() / 100);
                                totalAmount = total;
                            }
                        } else if (mode == StaticValues.AUTONUM_BILLINGSALESORDER && so.getDiscount() != 0) {
                            if (!so.isPerDiscount()) {
                                discountTotalQuotation = so.getDiscount();
                                total = total - so.getDiscount();
                                totalAmount = total;
                            } else {
                                discountTotalQuotation = total * so.getDiscount() / 100;
                                total -= (total * so.getDiscount() / 100);
                                totalAmount = total;
                            }
                        }
//                        / ----------- Get taxable Term amount for the CQ / VQ / SO  and PO-----------------
                        double taxableamount = 0,lineleveltermTaxAmount=0;
                        Map<String, Object> taxListParams = new HashMap<String, Object>();
                        KwlReturnObject taxListResult = null;
                        List<TaxList> taxListPercent = new ArrayList<TaxList>();
                            HashMap<String, Object> requestParam = new HashMap<String, Object>();
                            HashMap<String, Object> filterrequestParams = new HashMap<String, Object>();
                             KwlReturnObject termMapResult = null;
                        if (mode == StaticValues.AUTONUM_QUOTATION) {
                            requestParam.put("quotation", quotation.getID());
                            filterrequestParams.put("taxid", quotation.getTax() == null ? "" : quotation.getTax().getID());
                            boolean isApplyTaxToTerms = quotation.isApplyTaxToTerms();
                            boolean isTermMappedwithTax = false;
                            termMapResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                            List<QuotationTermMap> quotationTermMapList = termMapResult.getEntityList();
                            for (QuotationTermMap quotationTermMap : quotationTermMapList) {
                                double termAmnt = quotationTermMap.getTermamount();
                                filterrequestParams.put("term", quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                                if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
                                    for (String taxId : uniqueProductTaxList) {
                                        filterrequestParams.put("taxid", taxId);
                                        filterrequestParams.put("term",  quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                                        taxListParams.put("taxid", taxId);
                                        isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                        if (isTermMappedwithTax) {
                                            taxListResult = accTaxObj.getTaxList(taxListParams);
                                            if (taxListResult != null && taxListResult.getEntityList() != null) {
                                                taxListPercent = taxListResult.getEntityList();
                                                lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
                                            }
//                                    break;
                                        }
                                    }
                                } else {
                                    isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                }

                                if (isTermMappedwithTax) {
                                    taxableamount += termAmnt;
                                }
                            }
                        } else if (mode == StaticValues.AUTONUM_SALESORDER) {
                            requestParam.put("salesOrder", sOrder.getID());
                            filterrequestParams.put("taxid", sOrder.getTax() == null ? "" : sOrder.getTax().getID());
                            boolean isApplyTaxToTerms = sOrder.isApplyTaxToTerms();
                            boolean isTermMappedwithTax = false;
                            termMapResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                            List<SalesOrderTermMap>  soTermMapList = termMapResult.getEntityList();
                            for (SalesOrderTermMap soTermMap : soTermMapList) {
                                double termAmnt = soTermMap.getTermamount();
                                filterrequestParams.put("term", soTermMap.getTerm()==null?"":soTermMap.getTerm().getId());
                                if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
                                    for (String taxId : uniqueProductTaxList) {
                                        filterrequestParams.put("taxid", taxId);
                                        filterrequestParams.put("term",  soTermMap.getTerm() == null ? "" : soTermMap.getTerm().getId());
                                        taxListParams.put("taxid", taxId);
                                        isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                        if (isTermMappedwithTax) {
                                            taxListResult = accTaxObj.getTaxList(taxListParams);
                                            if (taxListResult != null && taxListResult.getEntityList() != null) {
                                                taxListPercent = taxListResult.getEntityList();
                                                lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
                                            }
//                                    break;
                                        }
                                    }
                                } else {
                                    isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                }

                                if (isTermMappedwithTax) {
                                    taxableamount += termAmnt;
                                }
                            }
                        } else if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                            requestParam.put("vendorQuotation", venquotation.getID());
                            filterrequestParams.put("taxid", venquotation.getTax() == null ? "" : venquotation.getTax().getID());
                            boolean isApplyTaxToTerms = venquotation.isApplyTaxToTerms();
                            boolean isTermMappedwithTax = false;
                            termMapResult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                            List<VendorQuotationTermMap> quotationTermMapList = termMapResult.getEntityList();
                            for (VendorQuotationTermMap quotationTermMap : quotationTermMapList) {
                                double termAmnt = quotationTermMap.getTermamount();
                                filterrequestParams.put("term", quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                                if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
                                    for (String taxId : uniqueProductTaxList) {
                                        filterrequestParams.put("taxid", taxId);
                                        filterrequestParams.put("term",  quotationTermMap.getTerm() == null ? "" : quotationTermMap.getTerm().getId());
                                        taxListParams.put("taxid", taxId);
                                        isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                        if (isTermMappedwithTax) {
                                            taxListResult = accTaxObj.getTaxList(taxListParams);
                                            if (taxListResult != null && taxListResult.getEntityList() != null) {
                                                taxListPercent = taxListResult.getEntityList();
                                                lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
                                            }
//                                    break;
                                        }
                                    }
                                } else {
                                    isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                }

                                if (isTermMappedwithTax) {
                                    taxableamount += termAmnt;
                                }
                            }
                         } else if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                            requestParam.put("purchaseOrder", pOrder.getID());
                            filterrequestParams.put("taxid", pOrder.getTax() == null ? "" : pOrder.getTax().getID());
                            boolean isApplyTaxToTerms = pOrder.isApplyTaxToTerms();
                            boolean isTermMappedwithTax = false;
                            termMapResult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                            List<PurchaseOrderTermMap> TermMapList = termMapResult.getEntityList();
                            for (PurchaseOrderTermMap poTermMap : TermMapList) {
                                double termAmnt = poTermMap.getTermamount();
                                filterrequestParams.put("term", pOrder.getTerm() == null ? "" : pOrder.getTerm().getID());
                                if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
                                    for (String taxId : uniqueProductTaxList) {
                                        filterrequestParams.put("taxid", taxId);
                                        filterrequestParams.put("term",  poTermMap.getTerm() == null ? "" : poTermMap.getTerm().getId());
                                        taxListParams.put("taxid", taxId);
                                        isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                        if (isTermMappedwithTax) {
                                            taxListResult = accTaxObj.getTaxList(taxListParams);
                                            if (taxListResult != null && taxListResult.getEntityList() != null) {
                                                taxListPercent = taxListResult.getEntityList();
                                                lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
                                            }
//                                    break;
                                        }
                                    }
                                } else {
                                    isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                                }

                                if (isTermMappedwithTax) {
                                    taxableamount += termAmnt;
                                }
                            }
                        }
//                            totaltax = (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);
                        totalAmount = authHandler.round(totalAmount, CompanyID);
                        totaltax = (taxPercent == 0 ? 0 : (totalAmount + taxableamount) * taxPercent / 100);
                        //Assigned Line Level Tax if global tax is not included.
                        if(mainTax==null && totalTaxAmt!=0){
                            totaltax = totalTaxAmt+ lineleveltermTaxAmount;
                        }
//                        total+=term;
//                        totalAmount+=term;
                        total = authHandler.round(total, CompanyID);
                        totalAmount = total + totaltax;
                        if(extraCompanyPreferences.getLineLevelTermFlag() == 1){//If Line Level Terms/Taxes are applicable (For India Country)
                            totalAmount += totalOtherTermNonTaxableAmount ;
                        }
                    } else {
                        totalDiscount = inv1.getDiscount();
                        totaltax = inv1.getTaxEntry() != null ? inv1.getTaxEntry().getAmount() : 0;
                        mainTaxName = inv1.getTax() != null ? inv1.getTax().getName() : "";
                        totalAmount = (inv1.getCustomerEntry().getAmount());
                    }
//                    if(mode!=StaticValues.AUTONUM_SALESORDER){
//                            cell3 = createCell(messageSource.getMessage("acc.rem.195", null, (Locale)requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                            //cell3.setColspan(6);
//                            table.addCell(cell3);
//                            if(mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_VENQUOTATION || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_BILLINGSALESORDER)
//                                    invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , discountTotalQuotation, currencyid);
//                        else
//                            invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , totalDiscount, currencyid);
//                            invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                            invcell.setBorder(Rectangle.BOX);
//                            invcell.setPadding(5);
//                            table.addCell(invcell);
//                    }
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                    StringBuffer taxNameStr = new StringBuffer();
//                if(mainTax != null){
//                    taxNameStr.append(mainTax.getName());
//                    taxNameStr.append(" ");
//                    taxNameStr.append(taxPercent);
//                    taxNameStr.append("% (+)");
//                } else {
                    taxNameStr.append(messageSource.getMessage("acc.rem.196", null, (Locale) requestmap.get("locale")));
//                }
                    cell3 = createCell(taxNameStr.toString(), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//cell3.setColspan(6);
                    table.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        ExportRecordHandler.appendTermDetails(accInvoiceDAOobj, authHandlerDAOObj, inv, table, currencyid, mode, CompanyID);
                    }
                    if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                        term = ExportRecordHandler.appendTermDetailsVendorQuotation(accPurchaseOrderobj, authHandlerDAOObj, row8.getVendorquotation(), table, currencyid, mode, CompanyID);
                        totalAmount += term;
                    }
                    if (mode == StaticValues.AUTONUM_QUOTATION) {
                        term = ExportRecordHandler.appendTermDetailsQuotation(accSalesOrderDAOobj, authHandlerDAOObj, row7.getQuotation(), table, currencyid, mode, CompanyID);
                        totalAmount += term;
                    }
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        term = ExportRecordHandler.appendTermDetailsPurchaseOrder(accPurchaseOrderobj, authHandlerDAOObj, pOrder, table, currencyid, mode, CompanyID);
                        totalAmount += term;
                    }

                    if (mode == StaticValues.AUTONUM_SALESORDER) {
                        term = ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj, authHandlerDAOObj, row5.getSalesOrder(), table, currencyid, mode, CompanyID);
                        totalAmount += term;
                    }
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                    if ((mode == StaticValues.AUTONUM_VENQUOTATION || mode == StaticValues.AUTONUM_QUOTATION || mode == StaticValues.AUTONUM_PURCHASEORDER 
                            || mode == StaticValues.AUTONUM_SALESORDER || mode == StaticValues.AUTONUM_INVOICE) && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                        cell3 = createCell(messageSource.getMessage("acc.rem.258", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalOtherTermNonTaxableAmount, currencyid, isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        table.addCell(cell3);
                    }
                    cell3 = createCell(messageSource.getMessage("acc.rem.197", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//cell3.setColspan(6);
                    table.addCell(cell3);
                        cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);

// addTableRow(mainTable, table);
                   
                    PdfPTable summeryTable;
                    /**
                     * ERM - 294
                     * Get Tax information from invoice object when Avalara Integration is on
                     * And pass this information to getSummeryTableForAvalara() method
                     */
                    boolean isAvalaraIntegration = extraCompanyPreferences.isAvalaraIntegration();
                    if(isAvalaraIntegration && mode == StaticValues.AUTONUM_INVOICE) {
                        KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                        inv = (Invoice) cap.getEntityList().get(0);
                        Set<InvoiceDetail> invoicelist = inv.getRows();
                        JSONObject jObj = new JSONObject();
                        JSONArray jArray = null;
                        HashMap<String, Double> detailedTaxInformation = new HashMap<String, Double>();
                        for (Iterator<InvoiceDetail> iterator = invoicelist.iterator(); iterator.hasNext();) {
                            InvoiceDetail next = iterator.next();
                            jObj.put(IntegrationConstants.parentRecordID, next.getID());
                            jArray = integrationCommonService.getTransactionDetailTaxMapping(jObj);
                            for (int i = 0; i < jArray.length(); i++) {
                                jObj = jArray.getJSONObject(i);
                                if (detailedTaxInformation.containsKey(jObj.getString(Constants.term))) {
                                    Double subtaxammount = detailedTaxInformation.get(jObj.getString(Constants.term)) + jObj.optDouble(Constants.termamount);
                                    detailedTaxInformation.put(jObj.getString(Constants.term), authHandler.round(subtaxammount, CompanyID));
                                } else {
                                    detailedTaxInformation.put(jObj.getString(Constants.term), authHandler.round(jObj.getDouble(Constants.termamount), CompanyID));
                                }
                            }
                        }
                        summeryTable = ExportRecordHandler.getSummeryTableForAvalara(table, detailedTaxInformation, currencyid, isCurrencyCode, authHandlerDAOObj,requestmap,messageSource);
                    } else {
                        summeryTable = ExportRecordHandler.getSummeryTable(table, mainTaxName, addShipTo);
                    }
                    
                    mainCell12 = new PdfPCell(summeryTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainTable.addCell(mainCell12);
            
                    KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                    
                    String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId, CompanyID);
                    String currencyname = currency.getName();
                    cell3 = createCell(messageSource.getMessage("acc.rem.177", null, (Locale) requestmap.get("locale")) + " : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                    PdfPTable table2 = new PdfPTable(1);
                    table2.addCell(cell3);
                    PdfPCell mainCell62 = new PdfPCell(table2);
                    mainCell62.setBorder(0);
                    mainCell62.setPadding(10);
                    mainCell62.setPaddingTop(5);
                    if (addShipTo) {
                        mainTable.addCell(mainCell62);
                    }
                }
                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
//                Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, (Locale)requestmap.get("locale"))+":  ",fontSmallBold);
                String DescType = pref.getDescriptionType();
               // Phrase phrase2 = new Phrase(DescType + ": " + memo, fontSmallRegular);
                 PdfPCell pcell2 = createCellAllowingChinese(DescType+": " + memo, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
//                PdfPCell pcell1 = new PdfPCell(phrase1);
                //PdfPCell pcell2 = new PdfPCell(phrase2);
//                pcell1.setBorder(0);
//                pcell1.setPadding(10);
//                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
//                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

            } else if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);

                CreditNote creNote = null;
                DebitNote dbNote = null;
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                com = null;
                Account cEntry = null;
                String invno = "";
                double cndnTotalAmount = 0;
                Date entryDate = null;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;
                double discountMain = 0;
                double subTotal = 0;

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
                    Set<JournalEntryDetail> entryset = (creNote.getJournalEntry() != null) ? creNote.getJournalEntry().getDetails() : null;
                    customerObj = creNote.getCustomer();
//                    Iterator itr=(entryset!=null)?entryset.iterator():null;
//                    if(itr!=null){
//                        while(itr.hasNext()){
//                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
//        //                    customer=(Customer)session.get(Customer.class,acc.getID());
//                            customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
//                            if(customerObj!=null)
//                                    break;
//                        }
//                    }
                    com = creNote.getCompany();
                    invno = creNote.getCreditNoteNumber();
                    cndnTotalAmount = creNote.getCnamount();
                    currencyid = (creNote.getCurrency() == null) ? currencyid : creNote.getCurrency().getCurrencyID();
//                    entryDate = (creNote.isNormalCN()) ? creNote.getJournalEntry().getEntryDate() : creNote.getCreationDate();
                    entryDate = creNote.getCreationDate();
//inv = (Invoice) session.get(Invoice.class, billid);
                } else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = dbNote.getCompany();
                    Set<JournalEntryDetail> entryset = (dbNote.isNormalDN()) ? dbNote.getJournalEntry().getDetails() : null;
                    vendorObj = dbNote.getVendor();

//                    vendorObj=new Vendor();                    
//                    Iterator itr=(entryset!=null)?entryset.iterator():null;
//                    if(itr != null){
//                        while(itr.hasNext()){
//                            cEntry=((JournalEntryDetail)itr.next()).getAccount();
//        //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
//                            vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
//                            if(vendorObj!=null)
//                                break;
//                        }
//                    }
                    invno = dbNote.getDebitNoteNumber();
                    currencyid = (dbNote.getCurrency() == null) ? currencyid : dbNote.getCurrency().getCurrencyID();
//                    entryDate = (dbNote.isNormalDN()) ? dbNote.getJournalEntry().getEntryDate() : dbNote.getCreationDate();
                    entryDate = dbNote.getCreationDate();
                    cndnTotalAmount = dbNote.getDnamount();
                }


//                Company com = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getCompany() : inv1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
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
                    theader = messageSource.getMessage("acc.accPref.autoCN", null, (Locale) requestmap.get("locale"));
                } else if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                    theader = messageSource.getMessage("acc.accPref.autoDN", null, (Locale) requestmap.get("locale"));
                }
                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);

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

                PdfPTable tab4 = ExportRecordHandler.getDateTable(entryDate, invno, theader, formatter);

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
                mainTable.addCell(mainCell12);


//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, (Locale)requestmap.get("locale"))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);

                String customerName = "";
                String customerEmail = "";
                String terms = "";
                String billTo = "";
                String shipAddress = "";
                String billAddress = "";
                String memo = "";
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true); 
                Iterator itr = null;

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                if (!StringUtil.isNullOrEmpty(preText)) {
                    ExportRecordHandler.getHtmlCell(preText.trim(), mainTable, baseUrl);
                }

                if (mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {                                                          
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail = customerObj != null ? customerObj.getEmail() : "";
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID()); 
                    addrParams.put("isBillingAddress", false);
                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    terms = customerObj != null ? customerObj.getCreditTerm().getTermname() : "";
                    billTo = "Bill To";
                    addrParams.put("isBillingAddress", true);
                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(biCreNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biCreNote.getMemo();
                } else if (mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail = vendorObj != null ? vendorObj.getEmail() : "";
                    terms = vendorObj != null ? vendorObj.getDebitTerm().getTermname() : "";
                    billTo = "Supplier";                    
                    addrParams.put("vendorid", vendorObj.getID());
                    addrParams.put("companyid", vendorObj.getCompany().getCompanyID());                    
                    addrParams.put("isBillingAddress", true);
                    billAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    filter_names.add("debitNote.ID");
                    filter_params.add(biDeNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getBillingDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biDeNote.getMemo();
                }else if (mode == StaticValues.AUTONUM_CREDITNOTE && creNote.getCntype()==4) { //Credit Note Against Vendor
                    vendorObj = creNote.getVendor();
                    customerName = vendorObj.getName();
                    customerEmail = vendorObj != null ? vendorObj.getEmail() : "";
                    addrParams.put("vendorid", vendorObj.getID());
                    addrParams.put("companyid", vendorObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", false);
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
//                    shipAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    terms = vendorObj != null ? vendorObj.getDebitTerm().getTermname() : "";
                    billTo = "Bill To";
                    addrParams.put("isBillingAddress", true);
//                    billAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = creNote.getMemo();
                } 
                else if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", false);
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail = customerObj != null ? customerObj.getEmail() : "";
                    addrParams.put("isBillingAddress", false);
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
//                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    terms = customerObj != null ? customerObj.getCreditTerm().getTermname() : "";
                    billTo = "Bill To";
                    addrParams.put("isBillingAddress", true);
//                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = creNote.getMemo();
                }else if (mode == StaticValues.AUTONUM_DEBITNOTE && dbNote.getDntype()==4) { //Debit Note Agains Customer
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
                    customerObj = dbNote.getCustomer();
                    customerName = customerObj.getName();//dbNote.getCustomer().getName();
                    customerEmail = customerObj != null ? customerObj.getEmail() : "";
                    terms = customerObj != null ? customerObj.getCreditTerm().getTermname() : "";
                    billTo = "Supplier";
                    addrParams.put("isBillingAddress", true);
                    if (creNote.getBillingShippingAddresses() != null) {
                            billAddress = CommonFunctions.getBillingShippingAddressWithAttn(dbNote.getBillingShippingAddresses(), true);
                    } else {
                        billAddress = "";
                    }
//                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("debitNote.ID");
                    filter_params.add(dbNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = dbNote.getMemo();
                }
                else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail = vendorObj != null ? vendorObj.getEmail() : "";
                    terms = vendorObj != null ? vendorObj.getDebitTerm().getTermname() : "";
                    billTo = "Supplier";
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
                    memo = dbNote.getMemo();
                }


//                cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);

//                
//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainCell14.setPadding(10);
//                mainTable.addCell(mainCell14);

                PdfPTable addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddress, customerEmail, billTo, shipAddress, true);

                mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);
                mainTable.setSplitLate(false);
//                String[] headerDetails = {"P.O. NO.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
//                PdfPTable detailsTable=getDetailsTable(headerDetails,"",terms,null,null,formatter);

//                mainCell12 = new PdfPCell(detailsTable);
//                mainCell12.setBorder(0);
//                mainCell12.setPaddingTop(5);
//                mainCell12.setPaddingLeft(10);
//                mainCell12.setPaddingRight(10);
//                mainCell12.setPaddingBottom(5);
//                mainTable.addCell(mainCell12);


                PdfPCell invcell = null;
                PdfPTable table = null;
                if ((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag) {
                    String[] header = {messageSource.getMessage("acc.cnList.Sno", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.het.11", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.dnList.gridAmt", null, (Locale) requestmap.get("locale"))};
                    table = new PdfPTable(3);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{20, 40, 40});

                    globalTableHeader = header;
                    productHeaderTableGlobalNo = 3;
                    for (int i = 0; i < header.length; i++) {
                        invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBackgroundColor(Color.LIGHT_GRAY);
                        invCell.setBorder(0);
                        invcell.setPadding(3);
                        table.addCell(invcell);
                    }
                } else {

                    String[] header = {messageSource.getMessage("acc.cnList.Sno", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodDesc", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.187", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.193", null, (Locale) requestmap.get("locale"))};
                    table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{6, 20, 42, 12, 16});

                    globalTableHeader = header;
                    productHeaderTableGlobalNo = 4;
                    for (int i = 0; i < header.length; i++) {
                        invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBackgroundColor(Color.LIGHT_GRAY);
                        invCell.setBorder(0);
                        invcell.setPadding(3);
                        table.addCell(invcell);
                    }
                }
//                Iterator itr = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getRows().iterator() : inv1.getRows().iterator();
                CreditNoteDetail row = null;
                DebitNoteDetail row1 = null;
                BillingCreditNoteDetail row2 = null;
                BillingDebitNoteDetail row3 = null;
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    double quantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String cndnName = "";
                    double cndnDiscount = 0;
                    if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                        row = (CreditNoteDetail) itr.next();
                        if (!otherwiseFlag) {
                            if (!StringUtil.isNullOrEmpty(row.getInvoiceRow().getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInvoiceRow().getInventory().getProduct().getDescription();
                            }
                            prodName = row.getInvoiceRow().getInventory().getProduct().getName();
                            quantity = row.getQuantity();
                            if (row.getDiscount() != null) {
                                if (row.getTotalDiscount() != null) {
                                    discountMain = discountMain + row.getTotalDiscount();
                                    total = total - row.getTotalDiscount();
                                }
                                discount = row.getDiscount();
                            }
                            if (row.getTaxAmount() != null) {
                                taxMain = taxMain + row.getTaxAmount();
                                total = total + row.getTaxAmount();
                            }
                            try {
                                uom = row.getInvoiceRow().getInventory().getUom() == null ? row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : row.getInvoiceRow().getInventory().getUom().getNameEmptyforNA();
                            } catch (Exception ex) {//In case of exception use uom="";
                            }
                        } else if (otherwiseFlag) {
                            cndnName = row.getInvoice()!=null?row.getInvoice().getInvoiceNumber():"";
                            cndnDiscount = row.getDiscount()!=null?row.getDiscount().getDiscount():0.00;
                            cndnTotalOtherwiseAmount += cndnDiscount;
                        }
                    } else if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                        row1 = (DebitNoteDetail) itr.next();
                        if (!otherwiseFlag) {
                            if (!StringUtil.isNullOrEmpty(row1.getGoodsReceiptRow().getInventory().getProduct().getDescription())) {
                                prodDesc = row1.getGoodsReceiptRow().getInventory().getProduct().getDescription();
                            }
                            prodName = row1.getGoodsReceiptRow().getInventory().getProduct().getName();
                            quantity = row1.getQuantity();
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
                            try {
                                uom = row1.getGoodsReceiptRow().getInventory().getUom() == null ? row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure() == null ? "" : row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA() : row1.getGoodsReceiptRow().getInventory().getUom().getNameEmptyforNA();
                            } catch (Exception ex) {//In case of exception use uom="";
                            }
                        } else if (otherwiseFlag) {
                            cndnName = row1.getGoodsReceipt()!=null?row1.getGoodsReceipt().getGoodsReceiptNumber():"";
                            cndnDiscount = row1.getDiscount()!=null?row1.getDiscount().getDiscount():0.00;
                            cndnTotalOtherwiseAmount += cndnDiscount;
                        }
                    } else if (mode == StaticValues.AUTONUM_BILLINGCREDITNOTE) {
                        row2 = (BillingCreditNoteDetail) itr.next();
                        prodName = row2.getInvoiceRow().getProductDetail();
                        prodDesc = "-";
                        quantity = row2.getQuantity();
                        if (row2.getDiscount() != null) {
                            if (row2.getTotalDiscount() != null) {
                                discountMain = discountMain + row2.getTotalDiscount();
                                total = total - row2.getTotalDiscount();
                            }
                            discount = row2.getDiscount();
                        }
                        if (row2.getTaxAmount() != null) {
                            taxMain = taxMain + row2.getTaxAmount();
                            total = total + row2.getTaxAmount();
                        }
                    } else if (mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                        row3 = (BillingDebitNoteDetail) itr.next();
                        prodName = row3.getGoodsReceiptRow().getProductDetail();
                        prodDesc = "-";
                        quantity = row3.getQuantity();
                        if (row3.getDiscount() != null) {
                            if (row3.getTotalDiscount() != null) {
                                discountMain = discountMain + row3.getTotalDiscount();
                                total = total - row3.getTotalDiscount();
                            }
                            discount = row3.getDiscount();
                        }
                        if (row3.getTaxAmount() != null) {
                            taxMain = taxMain + row3.getTaxAmount();
                            total = total + row3.getTaxAmount();
                        }
                    }

                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    if ((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag) {
                        invcell = createCell(cndnName, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(authHandlerDAOObj.getFormattedCurrency(cndnDiscount, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        ExportRecordHandler.addTableRow(mainTable, table);
                        table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[]{20, 40, 40});
                    } else {
                        invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(prodDesc, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);

//                        invcell = createCell((double) quantity + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
                        invcell = createCell(authHandler.formattedQuantity(quantity, CompanyID) + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(authHandlerDAOObj.getFormattedCurrency(discount.getDiscountValue(), currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        ExportRecordHandler.addTableRow(mainTable, table);
                        table = new PdfPTable(5);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[]{6, 20, 42, 12, 16});

                        subTotal += discount.getDiscountValue();
                        total += discount.getDiscountValue();
                    }
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



                if ((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag) {

                    for (int i = 1; i <= 15; i++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (i > 12) {
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(invcell);
                    }
                    ExportRecordHandler.addTableRow(mainTable, table);
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                    table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{7, 20, 25, 24, 24});

                    PdfPCell cell3 = createCell(messageSource.getMessage("acc.pdf.24", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalOtherwiseAmount, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);

                    cell3 = createCell(messageSource.getMessage("acc.customerList.gridAmountDue", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalAmount - cndnTotalOtherwiseAmount, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    cell3 = createCell(messageSource.getMessage("acc.product.gridTotal", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalAmount, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                } else {
                    for (int i = 1; i <= 25; i++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (i > 20) {
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(invcell);
                    }

                    ExportRecordHandler.addTableRow(mainTable, table);
                    isFromProductTable = true;
                    document.add(mainTable);
                    document.getPageNumber();
                    isFromProductTable = false;
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                    table = new PdfPTable(5);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{7, 20, 25, 24, 24});
                    PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(subTotal, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    double term = 0;
                    if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                        term = ExportRecordHandler.appendTermDetailsCreditNote(accCreditNoteDAOobj, authHandlerDAOObj, row.getCreditNote(), table, currencyid, mode, CompanyID);
                    }
                    if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                        term = ExportRecordHandler.appendTermDetailsDebitNote(accDebitNoteobj, authHandlerDAOObj, row1.getDebitNote(), table, currencyid, mode, CompanyID);
                    }

                    cell3 = createCell(messageSource.getMessage("acc.rem.195", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, discountMain, currencyid, CompanyID);
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    invcell.setBorder(Rectangle.BOX);
                    table.addCell(invcell);
                    cell3 = createCell(messageSource.getMessage("acc.rem.192", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    cell3 = createCell(messageSource.getMessage("acc.rem.197", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total + term, currencyid,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);

                }

                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainCell5.setPaddingLeft(10);
                mainCell5.setPaddingRight(10);
                mainTable.addCell(mainCell5);

                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
//Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, (Locale)requestmap.get("locale"))+" : ",fontSmallBold);
                String DescType = pref.getDescriptionType();
                Phrase phrase2 = new Phrase(DescType + ": " + memo, fontSmallRegular);
//PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
//pcell1.setBorder(0);
///pcell1.setPadding(10);
//pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
//helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

            } else if (mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                GoodsReceipt gr = null;
                BillingGoodsReceipt gr1 = null;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
                    isRateIncludeGST = gr.isGstIncluded();
                    if (gr.getApprover() != null) {
                        approverName = gr.getApprover().getFirstName() + " " + gr.getApprover().getLastName();
                    }
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
                } else {
                    gr1 = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), billid);
                    if (gr1.getApprover() != null) {
                        approverName = gr1.getApprover().getFirstName() + " " + gr1.getApprover().getLastName();
                    }
                    if (gr1.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(gr1.getTemplateid().getConfigstr());
// document = getTemplateConfig(document,writer);
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if (!ExportRecordHandler.checkCompanyTemplateLogoPresent(gr1.getCompany())) {
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                    }
                }

                com = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCompany() : gr1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                KWLCurrency rowCurrency = (mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCurrency() : gr1.getCurrency());
                String rowCurrenctID = rowCurrency == null ? currencyid : rowCurrency.getCurrencyID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});
                if (isLetterHead && isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{75, 25});
                    tab1 = ExportRecordHandler.addCompanyTemplateLogo(logoPath, com);
                } else if (isCompanyTemplateLogo) {
                    table1.setWidths(new float[]{70, 30});
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
                Account vEntry;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vEntry = gr.getVendorEntry().getAccount();
                } else {
                    vEntry = gr1.getVendorEntry().getAccount();
                }
                boolean fixedAsset=gr.isFixedAssetInvoice();
                String theader = vEntry==cash?messageSource.getMessage("acc.accPref.autoCP", null, (Locale)requestmap.get("locale")):(fixedAsset?(messageSource.getMessage("acc.field.PurchaseInvoice", null, (Locale)requestmap.get("locale"))):(messageSource.getMessage("acc.accPref.autoVI", null, (Locale)requestmap.get("locale"))));
                if (gr.isIsconsignment()) {
                    theader = messageSource.getMessage("acc.field.ConsignmentPurchaseInvoice", null, (Locale) requestmap.get("locale"));
                }
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);
                String grno = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getGoodsReceiptNumber() : gr1.getBillingGoodsReceiptNumber();
//                Date entryDate = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getJournalEntry().getEntryDate() : gr1.getJournalEntry().getEntryDate();
                Date entryDate = mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getCreationDate() : gr1.getJournalEntry().getEntryDate();
                PdfPTable tab4 = ExportRecordHandler.getDateTable(entryDate, grno, theader, formatter);
                if (isCompanyTemplateLogo) {
                    tab2 = ExportRecordHandler.getDateTable2(entryDate, grno, theader, formatter, invCell);

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

//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);



//                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,invno,theader,formatter);

//                PdfPTable tab4 = new PdfPTable(2);
//                tab4.setWidthPercentage(100);
//                tab4.setWidths(new float[]{30, 70});
//
//                PdfPCell cell2=createCell(theader+" #",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                String grno=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getGoodsReceiptNumber():gr1.getBillingGoodsReceiptNumber();
//                cell2=createCell(": "+grno,fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell(messageSource.getMessage("acc.rem.198", null, (Locale)requestmap.get("locale"))+"  ",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell(": "+formatter.format(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getJournalEntry().getEntryDate():gr1.getJournalEntry().getEntryDate()),fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);

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

//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);

//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.from", null, (Locale)requestmap.get("locale"))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);

                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true); 
                String vendorName = "";
                String vendorTerms = "";
                String billTo = "Supplier";
                String billAddress = "";
                String shipAddress = "";
                String venAddress = "";
                Date dueDate = null;
                boolean isInclude = false; //Hiding or Showing P.O. NO field in single PDF 
                Date shipDate = null;
                String shipvia = null;
                String fob = null;
                linkHeader = "";

                String[] headerDetails = {"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                if(mode==StaticValues.AUTONUM_GOODSRECEIPT){
                    vendorName = gr.getVendor()==null?gr.getVendorEntry().getAccount().getName():gr.getVendor().getName();
                    vendorTerms=gr.getVendor()==null?"":gr.getVendor().getDebitTerm().getTermname();
                    if(pref.isWithInvUpdate()){
                        linkHeader=(gr.isFixedAssetInvoice() || gr.isIsconsignment())?"GR No.":"PO/GR/VQ No.";
                        isInclude=false;
                    }else{
                        linkHeader="PO/VQ. No.";
                        isInclude=false;
                    }  
                    addrParams.put("vendorid", gr.getVendor().getID());
                    addrParams.put("companyid", gr.getCompany().getCompanyID());                    
                    addrParams.put("isBillingAddress", true);
                    venAddress= accountingHandlerDAOobj.getVendorAddress(addrParams);
                    dueDate=gr.getDueDate();
                    shipDate=gr.getShipDate();        
                    shipvia=gr.getShipvia();        
                    fob=gr.getFob();
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) {
                        billAddress = venAddress;
                    } else {
                        billAddress = CommonFunctions.getBillingShippingAddressWithAttn(gr.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                        if (StringUtil.isNullOrEmpty(billAddress)) { //For old record in which addresses were saved in invoice table 
                            billAddress = gr.getBillTo() == null ? "" : gr.getBillTo();
                        }
                    }
                    shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(gr.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddress)) { //For old record in which addresses were saved in invoice table 
                        shipAddress = gr.getShipTo() == null ? "" : gr.getShipTo();
                    }
                } else {
                    vendorName = gr1.getVendor() == null ? gr1.getVendorEntry().getAccount().getName() : gr1.getVendor().getName();
                    vendorTerms = gr1.getVendor() == null ? "" : gr1.getVendor().getDebitTerm().getTermname();
                    linkHeader = "PO. No.";
                    isInclude = false;
                    addrParams.put("vendorid", gr1.getVendor().getID());
                    addrParams.put("companyid", gr1.getCompany().getCompanyID());                    
                    addrParams.put("isBillingAddress", true);
                    venAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    dueDate = gr1.getDueDate();
                    shipDate = gr1.getShipDate();
                    shipvia = gr1.getShipvia();
                    fob = gr1.getFob();
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) {
                        billAddress = venAddress;
                    } else {
                        billAddress = gr1.getBillTo() == null ? "" : gr1.getBillTo();
                    }
                    shipAddress = gr1.getShipTo() == null ? "" : gr1.getShipTo();
                }

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
                            isInclude = false;
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

                if (!StringUtil.isNullOrEmpty(preText)) {
                    ExportRecordHandler.getHtmlCell(preText.trim(), mainTable, baseUrl);
                }
                PdfPTable addressMainTable;
                PdfPTable addressMainTable1 = null;
                if ((mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT || mode == StaticValues.AUTONUM_GOODSRECEIPT) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                    addressMainTable1 = ExportRecordHandler.getSupplierAddress(vendorName, venAddress, "", "Supplier");
                    addressMainTable = ExportRecordHandler.getAddressTable(null, billAddress, null, "Bill To", shipAddress, true);
                } else {
                    addressMainTable = ExportRecordHandler.getAddressTable(vendorName, billAddress, "", billTo, shipAddress, true);
                }

                if ((mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT || mode == StaticValues.AUTONUM_GOODSRECEIPT) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                    mainCell12 = new PdfPCell(addressMainTable1);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);
                }
                mainCell12 = new PdfPCell(addressMainTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);
                mainTable.setSplitLate(false);

                String referenceNumber = "";
                if (!isExpenseInv) {
                    referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                }
                PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, referenceNumber, vendorTerms, dueDate, shipDate, formatter, isInclude, shipvia, fob);

                mainCell12 = new PdfPCell(detailsTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainCell12.setPaddingBottom(5);
                mainTable.addCell(mainCell12);

//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainTable.addCell(mainCell14);

//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
                
                PdfPTable table;
                PdfPCell grcell = null;
                if (isExpenseInv) {
                    String[] header = {"S.No.", "Account", "Description", "PRICE " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode), "LINE TOTAL " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode)};
                    table = new PdfPTable(5);
                    globalTableHeader = header;
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{6, 29, 29, 18, 18});
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                } else {
                    String countryMsgKey = "";
                    if (!com.getCountry().getID().equals("106")) {
                      countryMsgKey =  messageSource.getMessage("acc.invoice.gridUnitPriceIncludingGST", null, (Locale) requestmap.get("locale"));
                    }else{
                      countryMsgKey =  messageSource.getMessage("acc.invoice.gridUnitPriceIncludingVAT", null, (Locale) requestmap.get("locale"));
                    }
                    String[] header;
                    if (linkHeader.equalsIgnoreCase("")) {
                        header = new String[]{messageSource.getMessage("acc.cnList.Sno", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodDesc", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.187", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.188", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode), messageSource.getMessage("acc.rem.212", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode)};
                    } else {
                        if(isRateIncludeGST) {// Only Replace Table header from "Unit Price" to "Unit Price Including GST"
                                header = new String[]{messageSource.getMessage("acc.cnList.Sno", null, (Locale) requestmap.get("locale")), linkHeader, messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodDesc", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.187", null, (Locale) requestmap.get("locale")), countryMsgKey + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode), messageSource.getMessage("acc.rem.212", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode)};
                        } else {
                                header = new String[]{messageSource.getMessage("acc.cnList.Sno", null, (Locale) requestmap.get("locale")), linkHeader, messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodDesc", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.187", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.188", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode), messageSource.getMessage("acc.rem.212", null, (Locale) requestmap.get("locale")) + " " + authHandlerDAOObj.getCurrency(rowCurrenctID,isCurrencyCode)};
                        }
                    }
                    table = ExportRecordHandler.getTable(linkHeader, true);
                    globalTableHeader = header;
                    productHeaderTableGlobalNo = 2;
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(Rectangle.BOX);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                }
//                Iterator itr =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getRows().iterator():gr1.getRows().iterator();

                GoodsReceiptDetail row = null;
                BillingGoodsReceiptDetail row1 = null;
                ExpenseGRDetail exprow = null;
                int index = 0;
                double totalTax=0d;
                double totalOtherTermNonTaxableAmount=0d;
                double OtherTermNonTaxableAmount = 0, LineLevelTaxesAmt = 0; //only for INDIA Country
                while (itr.hasNext()) {
                    String linkTo = "-";
                    table.setSplitLate(false);
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
                        grcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
//Added Description in VI PDF Report for Expense TAB
                        if (!StringUtil.isNullOrEmpty(exprow.getDescription())) {
                            grcell = createCell(StringUtil.replaceFullHTML(StringUtil.DecodeText(exprow.getDescription()).replace("<br>","\n")), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        } else {
                            grcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount(exprow.getRate(), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        double amount1 = exprow.getRate();
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {   //For Discount Row
                            //If discount is selected, then line level total ll be "Total-Discount"
                            amount1 -= exprow.getDiscount().getDiscountValue();
                            grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("Discount", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj, exprow.getDiscount(), rowCurrenctID, CompanyID);
                            grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            grcell.setPadding(5);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            //amount1 -= exprow.getDiscount().getDiscountValue();
                        } else {
                            grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                        }

                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable = false;
                        String rowTaxName = "";
                        HashMap<String, Object> requestParams = requestmap;
                        if (exprow != null && exprow.getTax() != null) {
//                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getCreationDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = exprow.getTax().getName();
                            if (exprow.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = exprow.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }

                        }
                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                            PdfPCell invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell(rowTaxName + "  Tax", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);

//                            invcell = createCell(ExportRecordHandler.getFormattedAmount(amount1 * rowTaxPercent / 100), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            invcell = createCell(authHandler.formattedAmount(rowTaxAmount, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                            
                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(invcell);
                        }
                        if (isRateIncludeGST) {
                            amount1 -= rowTaxAmount;
                        } else  {
                            amount1 += rowTaxAmount;
                        } 
                        totalTax += rowTaxAmount;
                        if (exprow.isIsdebit()) {//In case of Debit type
                            total += amount1;
                        } else {// In case of credit type
                            total -= amount1;
                        }
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);

                    } else {
                        grcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        if (!linkHeader.equalsIgnoreCase("")) {
                            grcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                        }
                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
//    grcell = ExportRecordHandler.getProductNameWithDescriptionPhrase(row.getInventory().getProduct());
                            grcell = createCell(row.getInventory().getProduct().getName(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            table.addCell(grcell);
                            if (!StringUtil.isNullOrEmpty(row.getDescription())) {
//                                  grcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(row.getDescription(),baseUrl));
                                grcell = new PdfPCell(ExportRecordHandler.getHtmlCellTable(StringUtil.DecodeText(row.getDescription()), baseUrl));
//                                 grcell.setBorder(0);
//grcell.setBorder(0);
//                                 grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                                grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            } else if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                grcell = createCell(StringUtil.DecodeText(row.getInventory().getProduct().getDescription()), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                                grcell = createCell(row.getInventory().getProduct().getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
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
                        String qtyString = "";
                        if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
//                            qtyString = Double.toString((row.getInventory().isInvrecord() && (row.getGoodsReceipt().getPendingapproval() == 0)) ? row.getInventory().getQuantity() : row.getInventory().getActquantity());
                        //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
                            qtyString = authHandler.formattedQuantity(row.getInventory().getQuantity(), CompanyID);
                        } else if (mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
//                            qtyString = Double.toString((Double) row1.getQuantity());
                        //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
                            qtyString = authHandler.formattedQuantity( row1.getQuantity(), CompanyID);
                        }
                        if(com.getTemplateflag() == Constants.Tony_FiberGlass_templateflag)
                            qtyString = authHandler.formattingDecimalForQuantity(Double.parseDouble(qtyString), CompanyID);
                        grcell = createCell(qtyString + " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getUom() == null ? "" : row.getInventory().getUom().getNameEmptyforNA()) : ""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.getFormattedUnitPrice((mode == StaticValues.AUTONUM_GOODSRECEIPT ? (isRateIncludeGST ? row.getRateincludegst() : row.getRate()) : row1.getRate()), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        double amount1 = 0d;
                        if(isRateIncludeGST) {
                            amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? 
                                    row.getRateincludegst() * (row.getInventory().getQuantity()) : row1.getRate() * row1.getQuantity();
                        } else {
                            amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * (row.getInventory().getQuantity()) : row1.getRate() * row1.getQuantity();
                        }
                        amount1 = authHandler.round(amount1, CompanyID);
//                        grcell = createCell(ExportRecordHandler.getFormattedAmount(amount1), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                        Discount disc = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount();
                        if (disc != null) {                              
                            amount1 -= mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable = false;
                        String rowTaxName = "";
                        HashMap<String, Object> requestParams = requestmap;
                        if (row != null ) {
                            if(row.getTax() != null ){
//                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", row.getGoodsReceipt().getCreationDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }}
                            //If Line Level Terms/Taxes are applicable (For India Country)
                            if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                                LineLevelTaxesAmt = row.getRowTermAmount();
                                OtherTermNonTaxableAmount = row.getOtherTermNonTaxableAmount();
                            }
                        } else if (row1 != null && row1.getTax() != null) {
                            requestParams.put("transactiondate", row1.getBillingGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            if (taxObj[1] != null) {
                                isRowTaxApplicable = true;
                            }
                            rowTaxName = row1.getTax().getName();
                            if (row1.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                                }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        grcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        double amountForTax=amount1; 
                        totalTax += rowTaxAmount;
                        if(extraCompanyPreferences.getLineLevelTermFlag() == 1){
                            totalTax += LineLevelTaxesAmt ;
                            totalOtherTermNonTaxableAmount += OtherTermNonTaxableAmount ;
                        }
                        if (isRateIncludeGST) {
                            total += amount1 - rowTaxAmount;
                        } else {
                            total += amount1;
                        }
                        if(!isRateIncludeGST) {
                            amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                        }
                        if (disc != null) {                              
                            table=ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table,rowCurrenctID,disc,0,mode,linkHeader); //For Discount Row                   
                        }
                        if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
//                            table=ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,rowCurrenctID,amount1,rowTaxPercent,mode,linkHeader);
                            table = ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,rowCurrenctID,amountForTax,rowTaxPercent,mode,linkHeader,rowTaxAmount);
                        }
                         
                        ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row
                        if (isExpenseInv) {
                            table = new PdfPTable(5);
                            table.setWidthPercentage(100);
                            table.setWidths(new float[]{6, 29, 29, 18, 18});

                        } else {
                            table = ExportRecordHandler.getTable(linkHeader, true);
                        }
                         
//                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(grcell);
                    }
                }

                if (isExpenseInv) {
                    for (int j = 1; j <= 60; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > 55) {
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(grcell);
                    }
                } else {
                    for (int j = 1; j <= ((linkHeader.equalsIgnoreCase("")) ? 84 : 98); j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > ((linkHeader.equalsIgnoreCase("")) ? 78 : 91)) {
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
                table.setWidths(new float[]{50, 50});

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
//                if(row!=null){
//                 if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
//                    ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode);
//                 }   
//                }
//                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, (Locale)requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                table.addCell(cell3);
//                grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getDiscount():gr1.getDiscount(), rowCurrenctID);
//                grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                grcell.setBorder(Rectangle.BOX);
//                grcell.setPadding(5);
//                table.addCell(grcell); 
                cell3 = createCell(messageSource.getMessage("acc.rem.196", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getTaxEntry() != null ? gr.getTaxEntry().getAmount() : totalTax) : (gr1.getTaxEntry() != null ? gr1.getTaxEntry().getAmount() : 0), rowCurrenctID,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                if ((mode == StaticValues.AUTONUM_GOODSRECEIPT) && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                    cell3 = createCell(messageSource.getMessage("acc.rem.258", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalOtherTermNonTaxableAmount, currencyid, isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                }
                if (row != null) {
                    if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                        ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode, CompanyID);
                    }
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, (Locale) requestmap.get("locale")), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getVendorEntry().getAmount()) : (gr1.getVendorEntry().getAmount()), rowCurrenctID,isCurrencyCode), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);

                String mainTaxName = mode == StaticValues.AUTONUM_GOODSRECEIPT ? (gr.getTaxEntry() != null ? gr.getTax().getName() : "") : (gr1.getTaxEntry() != null ? gr1.getTax().getName() : "");
                PdfPTable summeryTable = ExportRecordHandler.getSummeryTable(table, mainTaxName, addShipTo);

                mainCell12 = new PdfPCell(summeryTable);
                mainCell12.setBorder(0);
                mainCell12.setPaddingTop(5);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                mainTable.addCell(mainCell12);

                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                double totalamount = 0;
                if (gr != null) {
                    totalamount = gr.getVendorEntry().getAmount();
                } else if (gr1 != null) {
                    totalamount = gr1.getVendorEntry().getAmount();
                }
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency,countryLanguageId, CompanyID);
                String currencyname = rowCurrency != null ? rowCurrency.getName() : currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, (Locale) requestmap.get("locale")) + " : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
//Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, (Locale)requestmap.get("locale"))+":  ",fontSmallBold);
                String DescType = pref.getDescriptionType();
                Phrase phrase2 = new Phrase(DescType + ": " + (mode == StaticValues.AUTONUM_GOODSRECEIPT ? gr.getMemo() : gr1.getMemo()), fontSmallRegular);
// PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
// pcell1.setBorder(0);
// pcell1.setPadding(10);
// pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
//helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

            } else if (mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT || mode == StaticValues.AUTONUM_INVOICERECEIPT || mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT || mode == StaticValues.AUTONUM_PAYMENT || mode == StaticValues.AUTONUM_BILLINGPAYMENT) {

                Receipt rc = null;
                BillingReceipt rc1 = null;
                Invoice inv = null;
                BillingInvoice inv1 = null;
                Payment pc = null;
                BillingPayment pc1 = null;

                if (mode == StaticValues.AUTONUM_RECEIPT) {
                    rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
                    currencyid = (rc.getCurrency() == null) ? currencyid : rc.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
//                    }
//                    config = new com.krawler.utils.json.base.JSONObject(rc.getTemplateid().getConfigstr());
                } else if (mode == StaticValues.AUTONUM_BILLINGRECEIPT) {
                    rc1 = (BillingReceipt) kwlCommonTablesDAOObj.getClassObject(BillingReceipt.class.getName(), billid);
                    currencyid = (rc1.getCurrency() == null) ? currencyid : rc1.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
//                    }
//                    config = new com.krawler.utils.json.base.JSONObject(rc1.getTemplateid().getConfigstr());
                } else if (mode == StaticValues.AUTONUM_INVOICERECEIPT) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                    inv = (Invoice) cap.getEntityList().get(0);
                    config = new com.krawler.utils.json.base.JSONObject(inv.getTemplateid().getConfigstr());
                    currencyid = (inv.getCurrency() == null) ? currencyid : inv.getCurrency().getCurrencyID();
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    config = new com.krawler.utils.json.base.JSONObject(inv1.getTemplateid().getConfigstr());
                    currencyid = (inv1.getCurrency() == null) ? currencyid : inv1.getCurrency().getCurrencyID();
                } else if (mode == StaticValues.AUTONUM_PAYMENT) {
                    pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
                    currencyid = (pc.getCurrency() == null) ? currencyid : pc.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
//                    }
                } else if (mode == StaticValues.AUTONUM_BILLINGPAYMENT) {
                    pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
                    currencyid = (pc1.getCurrency() == null) ? currencyid : pc1.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        rc1 = (BillingReceipt) kwlCommonTablesDAOObj.getClassObject(BillingReceipt.class.getName(), billid);
//                    }
                }
//                } else if (mode == StaticValues.AUTONUM_PURCHASEORDERRECEIPT){
//                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
//                    config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
//                } else if (mode == StaticValues.AUTONUM_BILLINGINPURCHASEORDERRECEIPT){
//                    gr1 = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), billid);
//                    config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
//                }

//                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
//                com = mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getCompany() : rc1.getCompany();
                String receiptNumber = "";
                Date journalEntryDate = new Date();//mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()
                PayDetail PayDetail = null;
                String memo = "";
                String AccountName = "";
                boolean ismanycrdb = false;
                int receiptType = 0;
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
                List<String> debitAccountNameRow = new ArrayList<String>();
                List<String> creditAccountNameRow = new ArrayList<String>();
                List<Double> debitAccountAmount = new ArrayList<Double>();
                List<Double> creditAccountAmount = new ArrayList<Double>();
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);
                HashMap pdoMAp = new HashMap();
                if (mode == StaticValues.AUTONUM_BILLINGRECEIPT) {
                    receiptNumber = rc1.getBillingReceiptNumber();
                    journalEntryDate = rc1.getJournalEntry().getEntryDate();
                    PayDetail = rc1.getPayDetail();
                    memo = rc1.getMemo();
                    com = rc1.getCompany();
                    ismanycrdb = rc1.isIsmanydbcr();

                    if (ismanycrdb) {
                        filter_names.add("billingReceipt.ID");
                        filter_params.add(rc1.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject pdoresult = accReceiptDAOobj.getBillingReceiptDetailOtherwise(rRequestParams);
                        List<BillingReceiptDetailOtherwise> list1 = pdoresult.getEntityList();
                        Iterator pdoRow = list1.iterator();
                        if (pdoRow != null && list1.size() > 0) {
                            for (BillingReceiptDetailOtherwise pdo : list1) {
                                if (rc1.getID().equals(pdo.getBillingReceipt().getID())) {
                                    if (rc1.isIsmanydbcr()) {
                                        if (pdo.isIsdebit()) {
                                            debitAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            debitAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        } else {
                                            creditAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            creditAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        }
                                    }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow", debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount", debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow", creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount", creditAccountAmount);
                        pdoMAp.put("creditdebitflag", 2);

                    }
                    AccountName = rc1.getPayDetail() != null ? rc1.getPayDetail().getPaymentMethod().getAccount().getName() : "";

//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = rc1.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                        }
//                    }
                } else if (mode == StaticValues.AUTONUM_RECEIPT) {
                    receiptNumber = rc.getReceiptNumber();

                    journalEntryDate = rc.getCreationDate();
//                    if (isOpeningBalanceTransaction) //For Opening Balance In Payment Receipt
//                    {
//                        journalEntryDate = rc.getCreationDate();
//                    } else {
//                        journalEntryDate = rc.getJournalEntry().getEntryDate();
//                    }
                    PayDetail = rc.getPayDetail();
                    memo = rc.getMemo();
                    com = rc.getCompany();
                    receiptType = rc.getReceipttype();
                    ismanycrdb = rc.isIsmanydbcr();

                    Customer cust = rc.getCustomer();
                    if (cust != null) {
                        customer = cust.getName();
                        addrParams.put("customerid", cust.getID());
                        addrParams.put("companyid", cust.getCompany().getCompanyID());                        
                        addrParams.put("isBillingAddress", true);
                        address = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    }

                    Vendor vendor = null;
                    if (rc.getVendor() != null) {
                        KwlReturnObject vendResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), rc.getVendor());
                        vendor = (Vendor) vendResult.getEntityList().get(0);
                        customer = vendor.getName();
                        addrParams.put("vendorid", vendor.getID());
                        addrParams.put("companyid", vendor.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", true);
                        address = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    }

                    KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(rc.getCompany().getCompanyID(), rc.getID());
                    List cNameList = result.getEntityList();
                    Iterator cNamesItr = cNameList.iterator();
                    String customerNames = "";
                    while (cNamesItr.hasNext()) {
//                        String tempName = URLEncoder.encode((String) , "UTF-8");
                        customerNames += cNamesItr.next();
                        customerNames += ",";
                    }
                    customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));

                    accname = (rc.getReceipttype() == 9 || rc.getReceipttype() == 2) ? customerNames : ((cust != null) ? cust.getName() : (vendor != null) ? vendor.getName() : "");

                    filter_names.add("receipt.ID");
                    filter_params.add(rc.getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject pdoresult = accReceiptDAOobj.getReceiptDetailOtherwise(rRequestParams);
                    List<ReceiptDetailOtherwise> list1 = pdoresult.getEntityList();
                    Iterator pdoRow = list1.iterator();

                    Iterator itrRow = rc.getRows().iterator();
                    double totalamount = 0, totaltaxamount = 0;
                    if (!rc.getRows().isEmpty()) {
                        while (itrRow.hasNext()) {
                            totalamount += ((ReceiptDetail) itrRow.next()).getAmount();
                        }
                    } else if (pdoRow != null && list1.size() > 0) {
                        for (ReceiptDetailOtherwise receiptDetailOtherwise : list1) {
                            if (rc.getID().equals(receiptDetailOtherwise.getReceipt().getID())) {
                                double taxamount = 0;
                                if (receiptDetailOtherwise.getTax() != null) {
                                    taxamount = receiptDetailOtherwise.getTaxamount();
                                    totaltaxamount += taxamount;
                                }
                                if (rc.isIsmanydbcr()) {
                                    if (receiptDetailOtherwise.isIsdebit()) {
                                        totalamount -= Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), CompanyID));
                                    } else {
                                        totalamount += Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), CompanyID));
                                    }
                                } else {
                                    totalamount += Double.parseDouble(authHandler.formattedAmount(((receiptDetailOtherwise.getAmount() + receiptDetailOtherwise.getTaxamount())), CompanyID));
                                }

                            }
                        }

                    } else {
                        if (isOpeningBalanceTransaction) {
                            totalamount = rc.getDepositAmount();  //Amount Received in Payment Receipt_Opening Balance
                        } else {
                            itrRow = rc.getJournalEntry().getDetails().iterator();
                            while (itrRow.hasNext()) {
                                JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                                if (jed.isDebit()) {
                                    if (rc.getDeposittoJEDetail() != null) {
                                        totalamount = rc.getDeposittoJEDetail().getAmount();
                                    } else {
                                        totalamount = jed.getAmount();
                                    }
                                }
                            }
                        }
                    }
                    amount = totalamount;

                    if (ismanycrdb) {

                        if (pdoRow != null && list1.size() > 0) {
                            for (ReceiptDetailOtherwise pdo : list1) {
                                if (rc.getID().equals(pdo.getReceipt().getID())) {
                                    if (rc.isIsmanydbcr()) {
                                        if (pdo.isIsdebit()) {
                                            debitAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            debitAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        } else {
                                            creditAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            creditAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        }
                                    } else {
                                        creditAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                        creditAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));

                                    }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow", debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount", debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow", creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount", creditAccountAmount);
                        pdoMAp.put("creditdebitflag", 2);

                    }
                    AccountName = rc.getPayDetail() != null ? rc.getPayDetail().getPaymentMethod().getAccount().getName() : "";

//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = rc.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                            
//                        }
//                    }
                } else if (mode == StaticValues.AUTONUM_INVOICERECEIPT) {
                    receiptNumber = inv.getInvoiceNumber();
//                    journalEntryDate = inv.getJournalEntry().getEntryDate();
                    journalEntryDate = inv.getCreationDate();
//                    PayDetail = inv.getPayDetail();                    
                    memo = inv.getMemo();
                    com = inv.getCompany();
                    AccountName = Constants.CashInHand;
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT) {
                    receiptNumber = inv1.getBillingInvoiceNumber();
                    journalEntryDate = inv1.getJournalEntry().getEntryDate();
//                    PayDetail = inv1.getPayDetail();
                    memo = inv1.getMemo();
                    com = inv1.getCompany();
                    AccountName = Constants.CashInHand;
                } else if (mode == StaticValues.AUTONUM_PAYMENT) {
                    receiptNumber = pc.getPaymentNumber();
                    journalEntryDate = pc.getCreationDate();
//                    if (isOpeningBalanceTransaction) //For Opening Balance In Payment Made
//                    {
//                        journalEntryDate = pc.getCreationDate();
//                    } else {
//                        journalEntryDate = pc.getJournalEntry().getEntryDate();
//                    }
                    PayDetail = pc.getPayDetail();
                    memo = pc.getMemo();
                    com = pc.getCompany();
                    ismanycrdb = pc.isIsmanydbcr();
                    receiptType = pc.getReceipttype();

                    Vendor vendor = pc.getVendor();
                    if (vendor != null) {
                        customer = vendor.getName();
                        addrParams.put("vendorid", vendor.getID());
                        addrParams.put("companyid", vendor.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", true);
                        address = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    }

                    Customer cust = null;
                    if (pc.getCustomer() != null) {
                        KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), pc.getCustomer());
                        cust = (Customer) custResult.getEntityList().get(0);
                        customer = cust.getName();
                        addrParams.put("customerid", cust.getID());
                        addrParams.put("companyid", cust.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", true);
                        address = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    }

                    KwlReturnObject result = accVendorPaymentobj.getPaymentVendorNames(com.getCompanyID(), pc.getID());
                    List vNameList = result.getEntityList();
                    Iterator vNamesItr = vNameList.iterator();
                    String vendorNames = "";
                    while (vNamesItr.hasNext()) {
//                        String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
                        vendorNames += vNamesItr.next();
                        vendorNames += ",";
                    }
                    vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
                    if (Constants.isNewPaymentStructure) {
                        accname = (vendor == null && cust == null) ? vendorNames : ((vendor != null) ? vendor.getName() : (cust != null) ? cust.getName() : "");
                    } else {
                        accname = (pc.getReceipttype() == 9 || pc.getReceipttype() == 2) ? vendorNames : ((vendor != null) ? vendor.getName() : (cust != null) ? cust.getName() : "");
                    }
                    filter_names.add("payment.ID");
                    filter_params.add(pc.getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject pdoresult = accVendorPaymentobj.getPaymentDetailOtherwise(rRequestParams);
                    List<PaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                    Iterator pdoRow = list1.iterator();


                    Iterator itrRow = pc.getRows().iterator();
                    double totalamount = 0, totaltaxamount = 0;
                    if (!pc.getRows().isEmpty()) {
                        while (itrRow.hasNext()) {
                            totalamount += ((PaymentDetail) itrRow.next()).getAmount();
                        }
                    } else if (pdoRow != null && list1.size() > 0) {
                        for (PaymentDetailOtherwise paymentDetailOtherwise : list1) {
                            if (pc.getID().equals(paymentDetailOtherwise.getPayment().getID())) {
                                double taxamount = 0;
                                if (paymentDetailOtherwise.getTax() != null) {
                                    taxamount = paymentDetailOtherwise.getTaxamount();
                                    totaltaxamount += taxamount;
                                }
                                if (pc.isIsmanydbcr()) {
                                    if (paymentDetailOtherwise.isIsdebit()) {
                                        totalamount += Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), CompanyID));
                                    } else {
                                        totalamount -= Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), CompanyID));
                                    }
                                } else {
                                    totalamount = totalamount + Double.parseDouble(authHandler.formattedAmount(((paymentDetailOtherwise.getAmount() + paymentDetailOtherwise.getTaxamount())), CompanyID));
                                }
                            }
                        }

                    } else {
                        if (isOpeningBalanceTransaction) {
                            totalamount = pc.getDepositAmount();    //Amount Paid in Payment Made_Opening Balance.
                        } else {
                            itrRow = pc.getJournalEntry().getDetails().iterator();
                            while (itrRow.hasNext()) {
                                JournalEntryDetail jed = ((JournalEntryDetail) itrRow.next());
                                if (!jed.isDebit()) {
                                    if (pc.getDeposittoJEDetail() != null) {
                                        totalamount = pc.getDeposittoJEDetail().getAmount();
                                    } else {
                                        totalamount = jed.getAmount();
                                    }
                                }
                            }
                        }
                    }
                    amount = totalamount;

                    if (ismanycrdb) {
                        if (pdoRow != null && list1.size() > 0) {
                            for (PaymentDetailOtherwise pdo : list1) {
                                if (pc.getID().equals(pdo.getPayment().getID())) {
                                    if (pc.isIsmanydbcr()) {
                                        if (pdo.isIsdebit()) {
                                            debitAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            debitAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        } else {
                                            creditAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            creditAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        }
                                    } else {
                                        debitAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                        debitAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                    }

                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow", debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount", debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow", creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount", creditAccountAmount);
                        pdoMAp.put("creditdebitflag", 1);

                    }
                    AccountName = pc.getPayDetail() != null ? pc.getPayDetail().getPaymentMethod().getAccount().getName() : "";

//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = pc.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                        }
//                    }
                } else if (mode == StaticValues.AUTONUM_BILLINGPAYMENT) {
                    receiptNumber = pc1.getBillingPaymentNumber();
                    journalEntryDate = pc1.getJournalEntry().getEntryDate();
                    PayDetail = pc1.getPayDetail();
                    memo = pc1.getMemo();
                    com = pc1.getCompany();
                    ismanycrdb = pc1.isIsmanydbcr();
                    if (ismanycrdb) {
                        filter_names.add("billingPayment.ID");
                        filter_params.add(pc1.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject pdoresult = accVendorPaymentobj.getBillingPaymentDetailOtherwise(rRequestParams);
                        List<BillingPaymentDetailOtherwise> list1 = pdoresult.getEntityList();
                        Iterator pdoRow = list1.iterator();
                        if (pdoRow != null && list1.size() > 0) {
                            for (BillingPaymentDetailOtherwise pdo : list1) {
                                if (pc1.getID().equals(pdo.getBillingPayment().getID())) {
                                    if (pc1.isIsmanydbcr()) {
                                        if (pdo.isIsdebit()) {
                                            debitAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            debitAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        } else {
                                            creditAccountAmount.add(pdo.getAmount() + (pdo.getTax() != null ? pdo.getTaxamount() : 0));
                                            creditAccountNameRow.add(pdo.getAccount().getName() + (pdo.getTax() != null ? ',' + pdo.getTax().getName() : ""));
                                        }
                                    }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow", debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount", debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow", creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount", creditAccountAmount);
                        pdoMAp.put("creditdebitflag", 1);

                    }
                    AccountName = pc1.getPayDetail() != null ? pc1.getPayDetail().getPaymentMethod().getAccount().getName() : "";

//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = pc1.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                        }
//                    }
                }


//else if (mode == StaticValues.AUTONUM_PURCHASEORDERRECEIPT){
//                    receiptNumber = gr.getGoodsReceiptNumber();//BillingInvoiceNumber();
//                    journalEntryDate = gr.getJournalEntry().getEntryDate();
////                    PayDetail = inv.getPayDetail();
//                    memo = gr.getMemo();
//                    com = gr.getCompany();
//                } else if (mode == StaticValues.AUTONUM_BILLINGINPURCHASEORDERRECEIPT){
//                    receiptNumber = gr1.getBillingGoodsReceiptNumber();//BillingInvoiceNumber();
//                    journalEntryDate = gr1.getJournalEntry().getEntryDate();
////                    PayDetail = inv.getPayDetail();
//                    memo = gr1.getMemo();
//                    com = gr1.getCompany();
//                }

                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                ExportRecordHandler.generateReceiptPDF(authHandlerDAOObj, kwlCommonTablesDAOObj, EnglishNumberToWordsOjb,
                        messageSource, mainTable, requestmap, (Locale) requestmap.get("locale"), com, logoPath, currencyid, receiptNumber,
                        journalEntryDate, formatter, accname, address, amount, mode, PayDetail, memo,
                        config, AccountName, iscontraentryflag, preText, baseUrl, ismanycrdb, pdoMAp);




            } else if (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader, document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                com = null;
                Account cEntry;
                String invno = "";
                Date entryDate = null;
                String company[] = new String[4];
                String customerName = "";
                String customerEmail = "";
                String billAddres = "";
                String billTo = "";
                String shipAddress = "";
                String memo = "";
                String orderID = "";
                String theader = "";
                String datetheader = "";
                String pointPern = "";
                String recQuantity = "";
                String status = "";
                Date shipDate = null;
                String shipvia = "";
                String fob = "";
                HashMap<String, Object> addrParams = new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);


                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    DeliveryOrder deliveryOrder = null;
                    deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), billid);
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
                    String invoicePostText = deliveryOrder.getPostText() == null ? "" : deliveryOrder.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT) && deliveryOrder.getTemplateid() != null) ? deliveryOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = deliveryOrder.getCustomer().getAccount();
                    invno = deliveryOrder.getDeliveryOrderNumber();
                    entryDate = deliveryOrder.getOrderDate();
                    customerName = deliveryOrder.getCustomer().getName();
                    status = deliveryOrder.getStatus() != null ? deliveryOrder.getStatus().getValue() : "";
                    customerEmail = deliveryOrder.getCustomer() != null ? deliveryOrder.getCustomer().getEmail() : "";
                    billTo = "Bill To";
                    if(deliveryOrder.getBillingShippingAddresses()!=null){ //If DO have address from show address then showing that address otherwise showing customer address
                       billAddres=CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), true);
                       shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), false); 
                    } else {                        
                        addrParams.put("customerid", deliveryOrder.getCustomer().getID());
                        addrParams.put("companyid", com.getCompanyID());                       
                        addrParams.put("isBillingAddress", false);
                        shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                        addrParams.put("isBillingAddress", true);
                        billAddres = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    }          
                    memo = deliveryOrder.getMemo();
                    shipDate = deliveryOrder.getShipdate();
                    shipvia = deliveryOrder.getShipvia();
                    fob = deliveryOrder.getFob();
                    orderID = deliveryOrder.getID();
                    if (deliveryOrder.isLeaseDO()) {
                        theader = messageSource.getMessage("acc.lease.DO", null, (Locale) requestmap.get("locale"));
                    } else if (deliveryOrder.isIsconsignment()) {
                        theader = messageSource.getMessage("acc.Consignment.DO", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = messageSource.getMessage("acc.accPref.autoDO", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                    pointPern = "acc.common.to";
                    recQuantity = "acc.accPref.NewdeliQuant";
                } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                    GoodsReceiptOrder grOrder = null;
                    grOrder = (GoodsReceiptOrder) kwlCommonTablesDAOObj.getClassObject(GoodsReceiptOrder.class.getName(), billid);
                    com = grOrder.getCompany();

                    if (grOrder.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(grOrder.getTemplateid().getConfigstr());
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

                    String invoicePostText = grOrder.getPostText() == null ? "" : grOrder.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? grOrder.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = grOrder.getVendor().getAccount();
                    invno = grOrder.getGoodsReceiptOrderNumber();
                    entryDate = grOrder.getOrderDate();
                    status = grOrder.getStatus() != null ? grOrder.getStatus().getValue() : "";
                    customerName = grOrder.getVendor().getName();
                    customerEmail = grOrder.getVendor() != null ? grOrder.getVendor().getEmail() : "";
                    billTo = "Supplier";
                    if(grOrder.getBillingShippingAddresses()!=null){ //If GRO have address from show address then showing that address otherwise showing customer address
                       billAddres=CommonFunctions.getBillingShippingAddressWithAttn(grOrder.getBillingShippingAddresses(), true);
                       shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(grOrder.getBillingShippingAddresses(), false);
                    }else {
                        addrParams.put("vendorid", grOrder.getVendor().getID());
                        addrParams.put("companyid", grOrder.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", true);
                        billAddres = accountingHandlerDAOobj.getVendorAddress(addrParams);
                        addrParams.put("isBillingAddress", false);
                        shipAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    }    
                    memo = grOrder.getMemo();
                    shipDate = grOrder.getShipdate();
                    shipvia = grOrder.getShipvia();
                    fob = grOrder.getFob();
                    orderID = grOrder.getID();
                    if (grOrder.isIsconsignment()) {
                        theader = messageSource.getMessage("acc.Consignment.GR", null, (Locale) requestmap.get("locale"));
                    } else if (grOrder.isFixedAssetGRO()) {
                        theader = messageSource.getMessage("acc.wtfTrans.grdoasset", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = messageSource.getMessage("acc.accPref.autoGRO", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                    pointPern = "acc.common.from";
                    recQuantity = "acc.accPref.recQuant";
                } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                    SalesReturn salesReturn = null;
                    salesReturn = (SalesReturn) kwlCommonTablesDAOObj.getClassObject(SalesReturn.class.getName(), billid);
                    com = salesReturn.getCompany();
                    if (salesReturn.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(salesReturn.getTemplateid().getConfigstr());
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

                    String invoicePostText = salesReturn.getPostText() == null ? "" : salesReturn.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? salesReturn.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = salesReturn.getCustomer().getAccount();
                    invno = salesReturn.getSalesReturnNumber();
                    entryDate = salesReturn.getOrderDate();
                    customerName = salesReturn.getCustomer().getName();
                    status = salesReturn.getStatus() != null ? salesReturn.getStatus().getValue() : "";
                    customerEmail = salesReturn.getCustomer() != null ? salesReturn.getCustomer().getEmail() : "";
                    addrParams.put("customerid", salesReturn.getCustomer().getID());
                    addrParams.put("companyid", com.getCompanyID());
                    addrParams.put("isBillingAddress", false);
                    shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    billTo = "Bill To";
                    addrParams.put("isBillingAddress", true);
                    billAddres = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    memo = salesReturn.getMemo();
                    shipDate = salesReturn.getShipdate();
                    shipvia = salesReturn.getShipvia();
                    fob = salesReturn.getFob();
                    orderID = salesReturn.getID();
                    if (salesReturn.isIsconsignment()) {
                        theader = messageSource.getMessage("acc.Consignment.salesreturn", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = salesReturn.isLeaseSalesReturn()?messageSource.getMessage("acc.accPref.leaseautoSR1", null, (Locale) requestmap.get("locale")):messageSource.getMessage("acc.accPref.autoSR", null, (Locale) requestmap.get("locale"));
                    }

                    datetheader = theader;
                    pointPern = "acc.common.to";
                    recQuantity = "acc.accPref.returnQuant";
                } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                    PurchaseReturn purchaseReturn = null;
                    purchaseReturn = (PurchaseReturn) kwlCommonTablesDAOObj.getClassObject(PurchaseReturn.class.getName(), billid);
                    com = purchaseReturn.getCompany();
                    if (purchaseReturn.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(purchaseReturn.getTemplateid().getConfigstr());
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

                    String invoicePostText = purchaseReturn.getPostText() == null ? "" : purchaseReturn.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText)) ? invoicePostText : (StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)) ? purchaseReturn.getTemplateid().getPostText() : CompanyPDFPOSTTEXT;
                    cEntry = purchaseReturn.getVendor().getAccount();
                    invno = purchaseReturn.getPurchaseReturnNumber();
                    entryDate = purchaseReturn.getOrderDate();
                    customerName = purchaseReturn.getVendor().getName();
                    status = purchaseReturn.getStatus() != null ? purchaseReturn.getStatus().getValue() : "";
                    customerEmail = purchaseReturn.getVendor() != null ? purchaseReturn.getVendor().getEmail() : "";
                    addrParams.put("vendorid", purchaseReturn.getVendor().getID());
                    addrParams.put("companyid", purchaseReturn.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", false);
                    shipAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    billTo = "Supplier";
                    addrParams.put("isBillingAddress", true);
                    billAddres = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    memo = purchaseReturn.getMemo();
                    shipDate = purchaseReturn.getShipdate();
                    shipvia = purchaseReturn.getShipvia();
                    fob = purchaseReturn.getFob();
                    orderID = purchaseReturn.getID();
                    if (purchaseReturn.isIsconsignment()) {
                        theader = messageSource.getMessage("acc.Consignment.purchasereturn", null, (Locale) requestmap.get("locale"));
                    } else {
                        theader = messageSource.getMessage("acc.accPref.autoPR", null, (Locale) requestmap.get("locale"));
                    }
                    datetheader = theader;
                    pointPern = "acc.common.to";
                    recQuantity = "acc.accPref.returnQuant";
                }

                company[0] = com.getCompanyName();
                company[1] = com.getAddress() != null ? StringUtil.serverHTMLStripper(com.getAddress()) : "";
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

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

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();

                invCell = createCell(theader, fontTbl, Element.ALIGN_RIGHT, 0, 5);
                tab2.addCell(invCell);
                PdfPTable tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                if (isCompanyTemplateLogo) {
                    tab2 = ExportRecordHandler.getDateTable2(entryDate, invno, datetheader, formatter, invCell);

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




//                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,invno,theader,formatter);

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


//                    PdfPTable tab5 = new PdfPTable(2);
//                    tab5.setWidthPercentage(100);
//                    tab5.setWidths(new float[]{10, 90});
//                    PdfPCell cell3 = createCell(messageSource.getMessage(pointPern, null, (Locale)requestmap.get("locale"))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);

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
                } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                    filter_names.add("salesReturn.ID");
                } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                    filter_names.add("purchaseReturn.ID");
                }

                filter_params.add(orderID);
                invRequestParams.put("filter_names", filter_names);
                invRequestParams.put("filter_params", filter_params);
                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    idresult = accInvoiceDAOobj.getDeliveryOrderDetails(invRequestParams);
                } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                    idresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(invRequestParams);
                } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                    idresult = accInvoiceDAOobj.getSalesReturnDetails(invRequestParams);
                } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                    idresult = accGoodsReceiptobj.getPurchaseReturnDetails(invRequestParams);
                }

                itr = idresult.getEntityList().iterator();


//                    cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);
//                    cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
//                    tab5.addCell(cell3);

                if (!StringUtil.isNullOrEmpty(preText)) {
                    ExportRecordHandler.getHtmlCell(preText.trim(), mainTable, baseUrl);
                }

                PdfPTable addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddres, customerEmail, billTo, shipAddress, true);

                PdfPCell mainCell14 = new PdfPCell(addressMainTable);
                mainCell14.setBorder(0);
                mainCell14.setPaddingTop(5);
                mainCell14.setPaddingLeft(10);
                mainCell14.setPaddingRight(10);
                mainCell14.setPaddingBottom(5);
                mainTable.addCell(mainCell14);

                String linkHeader = "";
                DeliveryOrder deliveryOrder = (DeliveryOrder) kwlCommonTablesDAOObj.getClassObject(DeliveryOrder.class.getName(), billid);
                if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                    String[] headerDetails = {"CI/SO. No.", "Status", "Ship Date", "Ship Via", "FOB"};
                    String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                    PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, "-", status, null, shipDate, formatter, false, shipvia, fob);
                    mainCell12 = new PdfPCell(detailsTable);
                    linkHeader = (deliveryOrder.isFixedAssetDO()) ? "SI.No." :deliveryOrder.isLeaseDO()?"LSO No.":deliveryOrder.isIsconsignment()?"CR No.":"CI/SO No.";
                } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                    String[] headerDetails = new String[]{"VI/PO. No.", "Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                    String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                    PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, "-", status, null, shipDate, formatter, false, shipvia, fob);
                    mainCell12 = new PdfPCell(detailsTable);
                    GoodsReceiptOrder grOrder = null;
                    grOrder = (GoodsReceiptOrder) kwlCommonTablesDAOObj.getClassObject(GoodsReceiptOrder.class.getName(), billid);
                    linkHeader = (grOrder.isFixedAssetGRO()) ? "PI No." : (grOrder.isIsconsignment() ? "CR No." : "VI/PO No.");

                } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                    SalesReturn salesreturn = null;
                    salesreturn = (SalesReturn) kwlCommonTablesDAOObj.getClassObject(SalesReturn.class.getName(), billid);
                    if (!salesreturn.isIsconsignment()) {
                        String[] headerDetails = {"Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                        String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails, "", status, null, shipDate, formatter, true, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                        linkHeader =  "DO/CI No.";
                    }
                    else{
                        linkHeader =  "CDO No.";
                        PdfPTable table3 = new PdfPTable(2);
                        mainCell12 = new PdfPCell(table3);
                    }
                } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                    PurchaseReturn pr = null;
                    pr = (PurchaseReturn) kwlCommonTablesDAOObj.getClassObject(PurchaseReturn.class.getName(), billid);
                    if(!pr.isIsconsignment()){
                        String[] headerDetails={"Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                        String referenceNumber = ExportRecordHandler.getReferenceNumber(idresult.getEntityList(), mode);
                        PdfPTable detailsTable = ExportRecordHandler.getDetailsTable(headerDetails,"", status, null, shipDate, formatter, true, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                        linkHeader =pr.isIsconsignment()?"":"GR/VI No.";
                    }else{
                        linkHeader =  "CGR No.";
                        PdfPTable table3 = new PdfPTable(2);
                        mainCell12 = new PdfPCell(table3);
                    }
                    
                }
                
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);
               

                String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, (Locale) requestmap.get("locale")), linkHeader, messageSource.getMessage("acc.rem.prodName", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.prodDesc", null, (Locale) requestmap.get("locale")), messageSource.getMessage("acc.rem.187", null, (Locale) requestmap.get("locale")), messageSource.getMessage(recQuantity, null, (Locale) requestmap.get("locale"))};
                PdfPTable table = ExportRecordHandler.getBlankTableForDO();
                productHeaderTableGlobalNo = 3;
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                globalTableHeader = header;
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                table = ExportRecordHandler.getBlankTableForDO();

                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    String prodDesc = "";
                    double quantity = 0, deliverdQuantity = 0;
                    String uom = "";
                    String linkTo = "-";

                    if (mode == StaticValues.AUTONUM_DELIVERYORDER) {
                        DeliveryOrderDetail row8 = (DeliveryOrderDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        if (!StringUtil.isNullOrEmpty(row8.getDescription())) {
                            /*
                             * Removing Html character While Export 
                             */
                            prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getDescription()).replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getProduct().getDescription()).replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        prodName = row8.getProduct().getName();
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, (Locale) requestmap.get("locale")) + " :";
                            prodName += "\n" + partno;
                            prodName += "\n";
                        }
                        prodName += "\n";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getUom() == null ? row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA() :row8.getUom().getNameEmptyforNA();
                        if (row8.getCidetails() != null) {
                            linkTo = row8.getCidetails().getInvoice().getInvoiceNumber();
                        } else if (row8.getSodetails() != null) {
                            linkTo = row8.getSodetails().getSalesOrder().getSalesOrderNumber();
                        }
                    } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                        SalesReturnDetail row8 = (SalesReturnDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        if (!StringUtil.isNullOrEmpty(row8.getDescription())) {
                            prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getDescription()).replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getProduct().getDescription()).replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, (Locale) requestmap.get("locale")) + " :";
                            prodName += "\n" + partno;
                            prodName += "\n";
                        }
                        prodName += "\n";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getReturnQuantity();
                        uom = row8.getUom() == null ? row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA() :row8.getUom().getNameEmptyforNA();
                         if (row8.getCidetails() != null && !row8.getSalesReturn().isIsconsignment()) {
                            linkTo = row8.getCidetails().getInvoice().getInvoiceNumber();
                        } else if (row8.getDodetails() != null) {
                            linkTo = row8.getDodetails().getDeliveryOrder().getDeliveryOrderNumber();
                        }
                    } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                        PurchaseReturnDetail row8 = (PurchaseReturnDetail) itr.next();
                        prodName = row8.getProduct().getName();
                        if (!StringUtil.isNullOrEmpty(row8.getDescription())) {
                            prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getDescription()).replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getProduct().getDescription()).replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, (Locale) requestmap.get("locale")) + " :";
                            prodName += "\n" + partno;
                            prodName += "\n";
                        }
                        prodName += "\n";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getReturnQuantity();
                        uom = row8.getUom() == null ? row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA() :row8.getUom().getNameEmptyforNA();
                        if (row8.getVidetails() != null && !row8.getPurchaseReturn().isIsconsignment()) {
                            linkTo = row8.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
                        } else if (row8.getGrdetails() != null) {
                            linkTo = row8.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber();
                        }
                    } else if (mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                        GoodsReceiptOrderDetails row8 = (GoodsReceiptOrderDetails) itr.next();
                        prodName = row8.getProduct().getName();
                        if (!StringUtil.isNullOrEmpty(row8.getDescription())) {
                            prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getDescription()).replace("<br>", "\n"));
                            prodDesc=Jsoup.parse(prodDesc).text();
                        } else {
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                prodDesc = StringUtil.replaceFullHTML(StringUtil.DecodeText(row8.getProduct().getDescription()).replace("<br>", "\n"));
                                prodDesc=Jsoup.parse(prodDesc).text();
                            }
                        }
                        if (row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())) {
                            String partno = row8.getPartno();
                            prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, (Locale) requestmap.get("locale")) + " :";
                            prodName += "\n" + partno;
                            prodName += "\n";
                        }
                        prodName += "\n";
                        quantity = row8.getActualQuantity();
                        deliverdQuantity = row8.getDeliveredQuantity();
                        uom = row8.getUom() == null ? row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA() :row8.getUom().getNameEmptyforNA();
                        if (row8.getVidetails() != null) {
                            linkTo = row8.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
                        } else if (row8.getPodetails() != null) {
                            linkTo = row8.getPodetails().getPurchaseOrder().getPurchaseOrderNumber();
                        } 
                    }


                    invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
                    invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
                    invcell = createCellAllowingChinese(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
                    //createCellAllowingChinese(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    // invcell = new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc, baseUrl));
                    invcell = createCellAllowingChineseAndEnglishHtmlTag(prodDesc, fontSmallRegular, Element.ALIGN_LEFT,0, 1);
                   // invcell = createCellAllowingChinese(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invcell.setBorder(0);
                    table.addCell(invcell);
                    String qtyStr = Double.toString(quantity);
                    if(com.getTemplateflag() == Constants.Tony_FiberGlass_templateflag){
                        qtyStr = authHandler.formattingDecimalForQuantity(quantity, CompanyID);   
                    } else if(com.getTemplateflag() == Constants.lsh_templateflag){
                        DecimalFormat df = new DecimalFormat("0");
                        qtyStr = df.format(quantity);
                    } else {
                        //ERP-8911, Quantity formatted so that we can set decimal digit as per requirement.
                        qtyStr = authHandler.formattedQuantity(quantity, CompanyID);
                    }
                       
//                             if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_DELIVERYORDER ) {
//                                            qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
//                                        }
                    invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
//                    invcell = createCell(Double.toString((double) deliverdQuantity) + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    //For TonyFiberGlass Deliverwd Quantity has quantitydigitafterdecimal = 2. 
                    String deliQty = Double.toString(deliverdQuantity);
                    if(com.getTemplateflag() == Constants.Tony_FiberGlass_templateflag){
                        deliQty = authHandler.formattingDecimalForQuantity(deliverdQuantity, CompanyID);   
                    } else if(com.getTemplateflag() == Constants.lsh_templateflag){
                        DecimalFormat df = new DecimalFormat("0");
                        deliQty = df.format(deliverdQuantity);
                    } else {
                        deliQty = authHandler.formattedQuantity(deliverdQuantity, CompanyID);
                    }

                    invcell = createCell(deliQty + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    invCell.setBorder(0);
                    table.addCell(invcell);
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding detail's row                                
                    table = ExportRecordHandler.getBlankTableForDO();
                }
                for (int j = 0; j < 117; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding extra space

                isFromProductTable = true;
                document.add(mainTable);
                document.getPageNumber();
                isFromProductTable = false;
                mainTable = new PdfPTable(1);
                mainTable.setWidthPercentage(100);

                table = ExportRecordHandler.getBlankTableForDO();
                for (int j = 0; j < 6; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.BOTTOM + Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                ExportRecordHandler.addTableRow(mainTable, table);
                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
//                    Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, (Locale)requestmap.get("locale"))+":  ",fontSmallBold);
                String DescType = pref.getDescriptionType();
                PdfPCell pcell2 = createCellAllowingChinese(DescType+": " + memo, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                //Phrase phrase2 = new Phrase(DescType + ": " + memo, fontSmallRegular);
//                    PdfPCell pcell1 = new PdfPCell(phrase1);
               
//                    pcell1.setBorder(0);
//                    pcell1.setPadding(10);
//                    pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
//                    helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);
            }

            ExportRecordHandler.getHtmlCell("<br/>", mainTable, baseUrl);
            if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                PdfPTable table3 = new PdfPTable(2); //for 2 column
//                table3.setWidthPercentage(50);

                KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
//  KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(),  sessionHandlerImpl.getUserid(request));
                User userDetails = (User) userResult.getEntityList().get(0);
                String username = userDetails.getFirstName() + " " + userDetails.getLastName();
                PdfPCell cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingTop(-10);
//                cell3.setPaddingBottom(30);
                table3.addCell(cell3);
                cell3 = (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN) ? createCell("ORDER ACCEPTANCE", fontSmallBold1, Element.ALIGN_CENTER, 0, 0) : createCell("", fontSmallBold1, Element.ALIGN_CENTER, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(-10);
                table3.addCell(cell3);
                if (addShipTo) {
                    cell3 = createCellAllowingChinese("Prepared By: " + username, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                } else {
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }
                cell3.setPaddingLeft(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                if (addShipTo) {
                    cell3 = (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN) ? createCell("Kindly return within TWO DAYS of receipt with your signature and company stamp. Thank you.", fontSmallRegularsmall, Element.ALIGN_CENTER, 0, 0) : createCell("", fontSmallRegularsmall, Element.ALIGN_CENTER, 0, 0);
                } else {
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }
                cell3.setPaddingRight(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);

//                cell3.setPaddingTop(-9);
                table3.addCell(cell3);

                if (addShipTo) {
                     cell3 = createCellAllowingChinese("Approved By: " + approverName, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                } else {
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }
                cell3.setPaddingLeft(10);
                cell3.setPaddingTop(5);
//                cell3.setPaddingBottom(30);
                table3.addCell(cell3);
                cell3 = createCell("Signature: __________________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);

                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                cell3 = createCell("Name: _____________________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                PdfPCell mainCell63 = new PdfPCell(table3);
                mainCell63.setBorder(0);
                mainTable.addCell(mainCell63);
            }

            if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {// || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                PdfPTable table3 = new PdfPTable(2); //for 2 column
//                table3.setWidthPercentage(50);
//                String username = sessionHandlerImpl.getUserName(request);
                PdfPCell cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3 = createCell("Receiver's ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingTop(5);
//            cell3.setPaddingBottom(10);
                table3.addCell(cell3);
                cell3 = createCell("Authorized ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(5);
//            cell3.setPaddingBottom(10);
                table3.addCell(cell3);

                cell3 = createCell("Signature : ________________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingTop(5);
                cell3.setPaddingBottom(10);
                table3.addCell(cell3);
                cell3 = createCell("Signature : ________________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(5);
                cell3.setPaddingBottom(10);
                table3.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, (Locale) requestmap.get("locale")) + " : ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, (Locale) requestmap.get("locale")) + " : _______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
//                cell3.setPaddingTop(-9);
                table3.addCell(cell3);
                PdfPCell mainCell63 = new PdfPCell(table3);
                mainCell63.setBorder(0);
                mainCell63.setPaddingTop(-20);
                mainTable.addCell(mainCell63);
            }
            if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                if (!StringUtil.isNullOrEmpty(postText)) {
                    document.add(mainTable);
                    document.newPage();
                    mainTable = new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                    ExportRecordHandler.getHtmlCell(postText.trim(), mainTable, baseUrl);

                }
            } else {
                if (!StringUtil.isNullOrEmpty(postText)) {
                    ExportRecordHandler.getHtmlCell(postText.trim(), mainTable, baseUrl);
                }
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
                if (mainTableGlobal != null) {

                    mainTableGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    mainTableGlobal.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 30, writer.getDirectContent());
                    mainTableGlobal = null;
                }

                if (tableClosedLineGlobal != null) {
                    tableClosedLineGlobal.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                    tableClosedLineGlobal.writeSelectedRows(0, 10, document.leftMargin(), document.bottomMargin() + 45, writer.getDirectContent());
                    tableClosedLineGlobal = null;
                }
                try {
                    ExportRecordHandler.addHeaderFooter(CompanyPDFFooter, document, writer);
                } catch (DocumentException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ServiceException ex) {
                    Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (config != null && config.getBoolean("pageBorder")) {
                    int bmargin = 8;  //border margin
                    PdfContentByte cb = writer.getDirectContent();
                    cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.stroke();
                }

            } catch (Exception e) {
                Logger.getLogger(ExportRecord.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void addTableHeader(String currencyValue, PdfPTable mainTable, PdfPTable table, PdfPCell invcell) throws DocumentException, SessionExpiredException {
        for (int i = 0; i < globalTableHeader.length; i++) {
            invcell = new PdfPCell(new Paragraph(globalTableHeader[i], fontSmallBold));
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

                PdfPCell footerNotecell = new PdfPCell(fontFamilySelector.process(config.getString("footNote"), FontContext.FOOTER_NOTE, tColor));// fontSmallRegular));
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
