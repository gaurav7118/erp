/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFuctionality;

/**
 *
 * @author krawler
 */


import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
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
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.support.RequestContextUtils;


public class ExportRecord_SATS  extends ExportRecordBeans implements MessageSourceAware{
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
            
    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
   public ByteArrayOutputStream createPdf(HttpServletRequest request, String currencyid, String billid, DateFormat formatter, int mode, double amount, String logoPath, String customer, String accname, String address,boolean isExpenseInv,String CompanyID,String userId, String baseCurrency) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        double cndnTotalOtherwiseAmount=0;
        Document document = null;
        boolean isDependentType = false;
        PdfWriter writer = null;   
        boolean otherwiseFlag=false;
                if(!StringUtil.isNullOrEmpty(request.getParameter("otherwise")))
                    otherwiseFlag = Boolean.parseBoolean(request.getParameter("otherwise"));
        try {
            String poRefno ="";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4,15, 15, 5, 10);
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ExportRecord_SATS.EndPage());
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);
            String invno = "";
            String transactionAccountCode = "";
            double totalAmounttodisplay = 0;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            boolean isCompanyLogo = true;
            boolean isSATSCompany = false;
            boolean addShipTo = true;
            boolean isCompanyTemplateLogo = false;
            PdfPTable tab3 = null;            
            String approverName="______________________________";            
            String baseUrl = URLUtil.getPageURL(request, loginpageFull);
            Rectangle page = document.getPageSize();
            Company com  = null;
            HashMap<String, Object> requestParamsForCustColumn = new HashMap<String, Object>();
            int bmargin = 15;  //border margin
            double shiplenght = 0;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();
            KwlReturnObject resultCustColumn=null;
            String jeId="";
            String invoiceType="";
            String companyIdForCAP=CompanyID;
            CompanyAccountPreferences preferences=(CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),companyIdForCAP);
            int moduleid = -1;
            moduleid = ExportRecordHandler.getModuleId(mode, billid, CompanyID, kwlCommonTablesDAOObj);
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(CompanyID, moduleid);
            List<PdfTemplateConfig> list = templateConfig.getEntityList();
            String customerName = "";
            isSATSCompany=storageHandlerImpl.GetSATSCompanyId().contains(CompanyID);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyIdForCAP);
            isDependentType=companyAccountPreferences.isDependentField();
            if (list.isEmpty()) {
                CompanyPDFFooter = preferences.getPdffooter()==null?"":preferences.getPdffooter();
                CompanyPDFHeader = preferences.getPdfheader()==null?"":preferences.getPdfheader();
                CompanyPDFPRETEXT = preferences.getPdfpretext()==null?"":preferences.getPdfpretext();
                CompanyPDFPOSTTEXT = preferences.getPdfposttext()==null?"":preferences.getPdfposttext();
            } else {
                for (PdfTemplateConfig config : list) {
                    CompanyPDFFooter = config.getPdfFooter() == null ? "" : config.getPdfFooter();
                    CompanyPDFHeader = config.getPdfHeader() == null ? "" : config.getPdfHeader();
                    CompanyPDFPRETEXT = config.getPdfPreText() == null ? "" : config.getPdfPreText();
                    CompanyPDFPOSTTEXT = config.getPdfPostText() == null ? "" : config.getPdfPostText();
                }
            }
            String preText=StringUtil.isNullOrEmpty(CompanyPDFPRETEXT)?"":CompanyPDFPRETEXT;
            String postText=StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)?"":CompanyPDFPOSTTEXT;
    
            Projreport_Template defaultTemplate = (Projreport_Template) kwlCommonTablesDAOObj.getClassObject(Projreport_Template.class.getName(), Constants.HEADER_IMAGE_TEMPLATE_ID);
            if (defaultTemplate != null) {
                config = new com.krawler.utils.json.base.JSONObject(defaultTemplate.getConfigstr());
            }

            if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE||mode == StaticValues.AUTONUM_BILLINGSALESORDER||mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_SALESORDER||mode==StaticValues.AUTONUM_PURCHASEORDER||mode==StaticValues.AUTONUM_QUOTATION||mode==StaticValues.AUTONUM_VENQUOTATION||mode==StaticValues.AUTONUM_RFQ) {
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;
                
                Account cEntry = null;
               
                Date dueDate = null;
                Date shipDate = null;                 
                String shipvia = null;                 
                String fob = null;  
                Date entryDate = null;
                BillingPurchaseOrder po=null;
                SalesOrder sOrder = null;
                PurchaseOrder pOrder = null;
                Tax mainTax = null;
                Quotation quotation = null;
                VendorQuotation venquotation = null;
                RequestForQuotation RFQ= null;
                if (mode == StaticValues.AUTONUM_INVOICE ) {
                 KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);    
                 inv = (Invoice) cap.getEntityList().get(0);
                 requestParamsForCustColumn.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
                 requestParamsForCustColumn.put(Constants.filter_values, Arrays.asList(inv.getCompany().getCompanyID(), Constants.Acc_Invoice_ModuleId,0));
                 resultCustColumn = accAccountDAOobj.getFieldParams(requestParamsForCustColumn);
                 jeId=inv.getJournalEntry().getID();
                 shiplenght=inv.getShiplength();
                if(!StringUtil.isNullOrEmpty(inv.getInvoicetype())) 
                    invoiceType=inv.getInvoicetype();
                 if(inv.getApprover()!=null)
                     approverName=inv.getApprover().getFirstName() +" "+inv.getApprover().getLastName();
                 currencyid = (inv.getCurrency()==null)? currencyid : inv.getCurrency().getCurrencyID();
                    if (inv.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(inv.getTemplateid().getConfigstr());

                        Rectangle rec = null;
                        if (config.getBoolean("landscape")) {
                            Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 5, 10);
                            rec = document.getPageSize();
                        } else {
                            Rectangle recPage = new Rectangle(PageSize.A4);
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 5, 10);
                            rec = document.getPageSize();
                        }
                        writer = PdfWriter.getInstance(document, baos);
                        writer.setPageEvent(new ExportRecordBeans.EndPage());
                        document.open();
                        isCompanyLogo=false;
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if(!ExportRecordHandler.checkCompanyTemplateLogoPresent(inv.getCompany())){
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                        String letterHead = inv.getTemplateid().getLetterHead();
                        String invoicePostText=inv.getPostText()==null?"":inv.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?inv.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT))?inv.getTemplateid().getPreText():CompanyPDFPRETEXT;
                      if (config.getBoolean("lHead")) {   
                        if (!StringUtil.isNullOrEmpty(letterHead)) {
                            PdfPTable letterHeadTable = new PdfPTable(1);
                            ExportRecordHandler.getHtmlCell(letterHead, letterHeadTable, baseUrl);
                            letterHeadTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                            document.add(letterHeadTable);
                        }
                      }
                    }
                    ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                    ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    if(inv1.getApprover()!=null)
                     approverName=inv1.getApprover().getFirstName() +" "+inv1.getApprover().getLastName();
                    currencyid = (inv1.getCurrency()==null)? currencyid : inv1.getCurrency().getCurrencyID();
                    if (inv1.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(inv1.getTemplateid().getConfigstr());

                        Rectangle rec = null;
                        if (config.getBoolean("landscape")) {
                            Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 5, 10);
                            rec = document.getPageSize();
                        } else {
                            Rectangle recPage = new Rectangle(PageSize.A4);
                            recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                            document = new Document(recPage, 15, 15, 5, 10);
                            rec = document.getPageSize();
                        }
                        writer = PdfWriter.getInstance(document, baos);
                        writer.setPageEvent(new ExportRecordBeans.EndPage());
                        isCompanyLogo=false;
                        document.open();
                       
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                            if(!ExportRecordHandler.checkCompanyTemplateLogoPresent(inv1.getCompany())){
                                isCompanyTemplateLogo = false;
                                isCompanyLogo = true;
                            }
                       } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }
                        String letterHead = inv1.getTemplateid().getLetterHead();
                        String invoicePostText=inv1.getPostText()==null?"":inv1.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?inv1.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT))?inv1.getTemplateid().getPreText():CompanyPDFPRETEXT;
                         if (config.getBoolean("lHead")) {
                            if (!StringUtil.isNullOrEmpty(letterHead)) {
                                PdfPTable letterHeadTable = new PdfPTable(1);
                                ExportRecordHandler.getHtmlCell(letterHead, letterHeadTable, baseUrl);
                                letterHeadTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                                document.add(letterHeadTable);
                            }
                         }
                    }
                     ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                     ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);
                }
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    com = inv.getCompany();
                    cEntry = inv.getCustomerEntry().getAccount();
                    invno = inv.getInvoiceNumber();
//                    entryDate = inv.getJournalEntry().getEntryDate();
                    entryDate = inv.getCreationDate();
                    dueDate=inv.getDueDate();
                    shipDate=inv.getShipDate();
                    shipvia=inv.getShipvia();
                    fob=inv.getFob();
                    poRefno = inv.getPoRefNumber()==null?"":inv.getPoRefNumber();
                //inv = (Invoice) session.get(Invoice.class, billid);
                }else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                    so = (BillingSalesOrder) kwlCommonTablesDAOObj.getClassObject(BillingSalesOrder.class.getName(), billid);
                    if(so.getApprover()!=null)
                        approverName=so.getApprover().getFirstName() +" "+so.getApprover().getLastName();
                    currencyid = (so.getCurrency()==null)? currencyid : so.getCurrency().getCurrencyID();
                    com = so.getCompany();
                     if (so.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(so.getTemplateid().getConfigstr());
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
                    
                    String invoicePostText=so.getPostText()==null?"":so.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?so.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                    cEntry = so.getCustomer().getAccount();
                    invno = so.getSalesOrderNumber();
                    entryDate = so.getOrderDate();
                    dueDate=so.getDueDate();                    
                    mainTax = so.getTax();
                    shipDate=so.getShipdate();
                    shipvia=so.getShipvia();
                    fob=so.getFob();
                } else if(mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                    po = (BillingPurchaseOrder) kwlCommonTablesDAOObj.getClassObject(BillingPurchaseOrder.class.getName(), billid);
                    if(po.getApprover()!=null)
                        approverName=po.getApprover().getFirstName() +" "+po.getApprover().getLastName();
                    currencyid = (po.getCurrency()==null)? currencyid : po.getCurrency().getCurrencyID();
                    com = po.getCompany();
                    if (po.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(po.getTemplateid().getConfigstr());
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
                    String invoicePostText=po.getPostText()==null?"":po.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?po.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                    cEntry = po.getVendor().getAccount();
                    invno = po.getPurchaseOrderNumber();
                    dueDate=po.getDueDate();
                    entryDate = po.getOrderDate();
                    mainTax = po.getTax();
                    shipDate=po.getShipdate();
                    shipvia=po.getShipvia();
                    fob=po.getFob();
                }else if(mode==StaticValues.AUTONUM_SALESORDER){
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

                }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                    pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                    currencyid = (pOrder.getCurrency()==null)? currencyid : pOrder.getCurrency().getCurrencyID();
                    com = pOrder.getCompany();
                     if(pOrder.getApprover()!=null)
                        approverName=pOrder.getApprover().getFirstName() +" "+pOrder.getApprover().getLastName();
                    if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
                        isCompanyLogo = false;
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }                        
                        String invoicePostText=pOrder.getPostText()==null?"":pOrder.getPostText();
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)&& pOrder.getTemplateid() != null)?pOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT))?pOrder.getTemplateid().getPreText():CompanyPDFPRETEXT;
                    }else{
                      if (pOrder.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
                    //    document = getTemplateConfig(document,writer);
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
                      String invoicePostText=pOrder.getPostText()==null?"":pOrder.getPostText();
                      postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?pOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                    }
                    cEntry = pOrder.getVendor().getAccount();
                    invno = pOrder.getPurchaseOrderNumber();
                    dueDate=pOrder.getDueDate();
                    entryDate = pOrder.getOrderDate();
                    mainTax = pOrder.getTax();
                    shipDate=pOrder.getShipdate();
                    shipvia=pOrder.getShipvia();
                    fob=pOrder.getFob();

                }else if(mode==StaticValues.AUTONUM_QUOTATION){
                	quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                        currencyid = (quotation.getCurrency()==null)? currencyid : quotation.getCurrency().getCurrencyID();
                    com = quotation.getCompany();
                    
                    if (quotation.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(quotation.getTemplateid().getConfigstr());
                    //    document = getTemplateConfig(document,writer);
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
                
                }else if(mode==StaticValues.AUTONUM_VENQUOTATION){
                    venquotation = (VendorQuotation) kwlCommonTablesDAOObj.getClassObject(VendorQuotation.class.getName(), billid);
                    currencyid = (venquotation.getCurrency()==null)? currencyid : venquotation.getCurrency().getCurrencyID();
                    com = venquotation.getCompany();
                    if (venquotation.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(venquotation.getTemplateid().getConfigstr());
                     //   document = getTemplateConfig(document,writer);
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
                    String invoicePostText=venquotation.getPostText()==null?"":venquotation.getPostText();
                    postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)&& venquotation.getTemplateid() != null)?venquotation.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                    cEntry = venquotation.getVendor().getAccount();
                    invno = venquotation.getQuotationNumber();
                    dueDate=venquotation.getDueDate();
                    entryDate = venquotation.getQuotationDate();
                    mainTax = venquotation.getTax();
                    shipDate = venquotation.getShipdate();
                    shipvia = venquotation.getShipvia();
                    fob = venquotation.getFob();
                
                }else if(mode==StaticValues.AUTONUM_RFQ){
                    RFQ = (RequestForQuotation) kwlCommonTablesDAOObj.getClassObject(RequestForQuotation.class.getName(), billid);
//                    currencyid = (RFQ.getCurrency()==null)? currencyid : RFQ.getCurrency().getCurrencyID();
                    com = RFQ.getCompany();
                    if (RFQ.getTemplateid() != null) {
                        config = new com.krawler.utils.json.base.JSONObject(RFQ.getTemplateid().getConfigstr());
                     //   document = getTemplateConfig(document,writer);
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
//                    cEntry = venquotation.getVendor().getAccount();
                    invno = RFQ.getRfqNumber();
                    dueDate=RFQ.getDueDate();
                    entryDate = RFQ.getRfqDate();
//                    mainTax = RFQ.getTax();
//                    shipDate = venquotation.getShipdate();
//                    shipvia = venquotation.getShipvia();
//                    fob = venquotation.getFob();
                
                } else {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    currencyid = (inv1.getCurrency()==null)? currencyid : inv1.getCurrency().getCurrencyID();
                    com = inv1.getCompany();
                    cEntry = inv1.getCustomerEntry().getAccount();
                    invno = inv1.getBillingInvoiceNumber();
                    dueDate=inv1.getDueDate();
                    
                    entryDate = inv1.getJournalEntry().getEntryDate();
                    poRefno = inv1.getPoRefNumber()==null?"":inv1.getPoRefNumber();
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
                    }else{
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
                String theader = cEntry==cash?messageSource.getMessage("acc.accPref.autoCS", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request));
                String datetheader = theader;
                if(mode == StaticValues.AUTONUM_BILLINGSALESORDER||mode==StaticValues.AUTONUM_SALESORDER){
                    theader = messageSource.getMessage("acc.accPref.autoSO", null, RequestContextUtils.getLocale(request));
                    datetheader = messageSource.getMessage("acc.accPref.autoSODate", null, RequestContextUtils.getLocale(request));
                }else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_PURCHASEORDER){
                     theader = messageSource.getMessage("acc.accPref.autoPO", null, RequestContextUtils.getLocale(request));
                     datetheader = theader;
                }else if(mode == StaticValues.AUTONUM_QUOTATION){
                    theader = messageSource.getMessage("acc.accPref.autoCQN", null, RequestContextUtils.getLocale(request));
                    datetheader = theader;
                }else if(mode == StaticValues.AUTONUM_VENQUOTATION){
                    theader = messageSource.getMessage("acc.accPref.autoVQN", null, RequestContextUtils.getLocale(request));
                    datetheader = theader;
                } else if(mode == StaticValues.AUTONUM_RFQ){
                    theader = messageSource.getMessage("acc.accPref.autoRFQ", null, RequestContextUtils.getLocale(request));
                    datetheader = theader;
                }
                if(mode==StaticValues.AUTONUM_INVOICE||mode==StaticValues.AUTONUM_BILLINGINVOICE) //Invoice header Label as Tax Invoice
                    theader="Tax Invoice";
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                 if(isSATSCompany && (mode==StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER))
                    invCell=createCell(theader,fontTbl1,Element.ALIGN_LEFT,0,0);
                tab2.addCell(invCell);
                addShipTo = !(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_INVOICE);
                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});
                if (addShipTo) {
                    if(isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode==StaticValues.AUTONUM_PURCHASEORDER))
                        tab4 = ExportRecordHandler.getDateTableForSATS(entryDate, invno, "Doc.", formatter);
                    else{
                        tab4 = ExportRecordHandler.getDateTable(entryDate, invno, datetheader, formatter);
                    }
                }
                if (isCompanyTemplateLogo || storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                  if (addShipTo){
                    if(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER)  
                       table1.setWidths(new float[]{65, 35});
                    tab2= ExportRecordHandler.getDateTable2(entryDate,invno,datetheader,formatter,invCell);   
                  } 
                }
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                     if (isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode==StaticValues.AUTONUM_PURCHASEORDER)) {
                        tab3 = ExportRecordHandler.getCompanyInfoForSATS(company);
                    } else {
                        tab3 = ExportRecordHandler.getCompanyInfo(company);
                    }
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
               
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                
                
                if (isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER)) {
                    table1.addCell(cel2);
                    table1.addCell(cell1);
                } else {
                    table1.addCell(cell1);
                    table1.addCell(cel2);
                }

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPaddingLeft(10);
                mainCell11.setPaddingRight(10);
                mainCell11.setPaddingTop(10);
                cell1.setPaddingTop(20);
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
               if(!(isSATSCompany && (mode==StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER)))  
                    mainCell12.setPaddingTop(10);
                mainCell12.setPaddingLeft(10);
                mainCell12.setPaddingRight(10);
                if (!isCompanyTemplateLogo && !(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                    mainTable.addCell(mainCell12);
                }

                                
//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;
                
                customerName = "";
                String customerAccountCode = "";
                String customerEmail = "";
                String terms = "";
                String billTo = "";
                String billAddr="";
                String shipAddr = "";
                String memo = "";
                String salesPerson=null;
                String billtoAddress="";                
                boolean isInclude=false; //Hiding or Showing P.O. NO field in single PDF 
                Iterator itr = null;
                linkHeader="";
                String[] headerDetails={"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                String[] headerDetailsForVrnet={"Terms","Due Date","Sales Person"};//Header name for VRNET Comapny 
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);
                 //if(mode==StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_BILLINGINVOICE){
                    if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                 //}
                if(mode==StaticValues.AUTONUM_INVOICE){
                    filter_names.add("invoice.ID");
                    filter_params.add(inv.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                    customerName = inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in invoice table 
                       billAddr = inv.getBillTo() == null ? "" : inv.getBillTo();
                    }
                    customerEmail= inv.getCustomer()!=null?inv.getCustomer().getEmail():"";
                    billTo="Bill To";      
                    isInclude=false;
                    if(inv.getMasterSalesPerson()!=null)
                    {   //if User Class returns the Null Valu
                        salesPerson=inv.getMasterSalesPerson()!=null?inv.getMasterSalesPerson().getValue():"";
                    }else{  //if salesperson class has no username
                        salesPerson="";
                    }
                    if(pref.isWithInvUpdate()){
                        linkHeader = "SO/DO/CQ. No.";                    
                    }else{
                        linkHeader = "SO/CQ. No.";
                    }                    
                    terms=inv.getCustomer()!=null?inv.getCustomer().getCreditTerm().getTermname():"";    
                    shipAddr=CommonFunctions.getBillingShippingAddressWithAttn(inv.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddr)){ //For old record in which addresses were saved in invoice table 
                        shipAddr=inv.getShipTo()==null?"":inv.getShipTo();
                    }                    
                    itr = idresult.getEntityList().iterator();
                    memo =inv.getMemo();
                } else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                    customerName = so.getCustomer().getName();
                    customerAccountCode=so.getCustomer().getAcccode();
                    customerEmail= so.getCustomer()!=null?so.getCustomer().getEmail():"";
                    shipAddr=so.getShipTo()!=null? so.getShipTo():"";
                    terms=so.getTerm()!=null?so.getTerm().getTermname():so.getCustomer()!=null?so.getCustomer().getCreditTerm().getTermname():"";    
                    billTo="Bill To";         
                    isInclude=false;
                    billAddr = so.getBillTo()!=null ? so.getBillTo():"";
                    filter_names.add("salesOrder.ID");
                    filter_params.add(so.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getBillingSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = so.getMemo();
                } else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                    customerName = po.getVendor().getName();
                    customerAccountCode=po.getVendor().getAcccode();
                    customerEmail= po.getVendor()!=null?po.getVendor().getEmail():"";                    
                    terms=po.getTerm()!=null?po.getTerm().getTermname():po.getVendor()!=null?po.getVendor().getDebitTerm().getTermname():"";    
                    billTo="Supplier";
                    isInclude=false;
                    billAddr = po.getVendor().getAddress()!=null?po.getVendor().getAddress():"";
                    shipAddr=po.getShipTo()==null?"":po.getShipTo();
                    billtoAddress=po.getBillTo()==null?"":po.getBillTo();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(po.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = po.getMemo();
                }else if(mode==StaticValues.AUTONUM_SALESORDER){
                    customerName = sOrder.getCustomer().getName();
                    customerEmail= sOrder.getCustomer()!=null?sOrder.getCustomer().getEmail():"";
                    customerAccountCode=sOrder.getCustomer().getAcccode();
                    shipAddr=CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddr)){ //For old record in which addresses were saved in invoice table 
                        shipAddr=sOrder.getShipTo()==null?"":sOrder.getShipTo();
                    } 
                    terms=sOrder.getTerm()!=null?sOrder.getTerm().getTermname():sOrder.getCustomer()!=null?sOrder.getCustomer().getCreditTerm().getTermname():"";       
                    billTo="Bill To";
                    isInclude=false;
                    //headerDetails[0] = "Quotation No.";
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(sOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in invoice table 
                    billAddr = sOrder.getBillTo()!=null?sOrder.getBillTo():"";
                    }
                    filter_names.add("salesOrder.ID");
                    filter_params.add(sOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = sOrder.getMemo();
                }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                   if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        config = new com.krawler.utils.json.base.JSONObject(pOrder.getTemplateid().getConfigstr());
                        isCompanyLogo = false;
                        if (config.has("showTemplateLogo") && config.getBoolean("showTemplateLogo")) {
                            isCompanyTemplateLogo = true;
                        } else if (config.has("showLogo") && config.getBoolean("showLogo")) {
                            isCompanyLogo = true;
                        }                        
                        String invoicePostText=pOrder.getPostText()!=null?pOrder.getPostText():"";
                        postText = !(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?pOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        preText = (StringUtil.isNullOrEmpty(CompanyPDFPRETEXT))?pOrder.getTemplateid().getPreText():CompanyPDFPRETEXT;
                    }
                    customerName = pOrder.getVendor().getName();
                    customerAccountCode=pOrder.getVendor().getAcccode();
                    customerEmail= pOrder.getVendor()!=null?pOrder.getVendor().getEmail():"";
                    terms=pOrder.getTerm()!=null?pOrder.getTerm().getTermname():pOrder.getVendor()!=null?pOrder.getVendor().getDebitTerm().getTermname():"";                        
                    billTo="Supplier";
                    isInclude=false;
                    //headerDetails[0] = "SO No.";
                    addrParams.put("vendorid", pOrder.getVendor().getID());
                    addrParams.put("companyid", pOrder.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", true);
                    billAddr = accountingHandlerDAOobj.getVendorAddress(addrParams);//for supplier billaddr will be vendor address
                    shipAddr=CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddr)){ //For old record in which addresses were saved in invoice table 
                        shipAddr=pOrder.getShipTo()==null?"":pOrder.getShipTo();
                    } 
                    
                    billtoAddress = CommonFunctions.getBillingShippingAddressWithAttn(pOrder.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billtoAddress)) { //For old record in which addresses were saved in invoice table 
                    billtoAddress=pOrder.getBillTo()==null?"":pOrder.getBillTo();
                    }
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(pOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = pOrder.getMemo();
                }else if(mode==StaticValues.AUTONUM_QUOTATION){
                    customerName = quotation.getCustomer().getName();
                    customerAccountCode=quotation.getCustomer().getAcccode();
                    customerEmail= quotation.getCustomer()!=null?quotation.getCustomer().getEmail():"";
                    shipAddr=CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddr)){ //For old record in which addresses were saved in quotation table 
                        shipAddr=quotation.getShipTo()==null?"":quotation.getShipTo();
                    } 
                    terms=quotation.getCustomer()!=null?quotation.getCustomer().getCreditTerm().getTermname():"";
                    billTo="Bill To";
                    isInclude=false;
                    billAddr = CommonFunctions.getBillingShippingAddressWithAttn(quotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billAddr)) { //For old record in which addresses were saved in quotation table 
                       billAddr=quotation.getBillTo()!=null?quotation.getBillTo():"";
                    }
                    filter_names.add("quotation.ID");
                    filter_params.add(quotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = quotation.getMemo();
                }else if(mode==StaticValues.AUTONUM_VENQUOTATION){
                    customerName = venquotation.getVendor().getName();
                    customerAccountCode = venquotation.getVendor().getAcccode();
                    customerEmail= venquotation.getVendor()!=null?venquotation.getVendor().getEmail():"";
                    //shipTo=venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
                    terms=venquotation.getVendor()!=null?venquotation.getVendor().getDebitTerm().getTermname():"";
                    billTo="Supplier";
                    isInclude=false;
                    addrParams.put("vendorid", venquotation.getVendor().getID());
                    addrParams.put("companyid", venquotation.getCompany().getCompanyID());
                    addrParams.put("isBillingAddress", true);
                    billAddr= accountingHandlerDAOobj.getVendorAddress(addrParams);//for supplier billaddr will be vendor address
                    shipAddr = CommonFunctions.getBillingShippingAddressWithAttn(venquotation.getBillingShippingAddresses(), false);
                    if (StringUtil.isNullOrEmpty(shipAddr)) { //Old record in which addresses were saved in venquotation table 
                        shipAddr = venquotation.getShipTo() == null ? "" : venquotation.getShipTo();
                    }
                    
                    billtoAddress = CommonFunctions.getBillingShippingAddressWithAttn(venquotation.getBillingShippingAddresses(), true);//true used for billing address and false used for shipping address
                    if (StringUtil.isNullOrEmpty(billtoAddress)) { //For old record in which addresses were saved in venquotation table 
                    billtoAddress=venquotation.getBillTo()==null?"":venquotation.getBillTo();
                    }
                    filter_names.add("vendorquotation.ID");
                    filter_params.add(venquotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = venquotation.getMemo();
                } else if(mode==StaticValues.AUTONUM_RFQ){
//                    customerName = RFQ.getVendor().getName();
//                    customerEmail= venquotation.getVendor()!=null?venquotation.getVendor().getEmail():"";
                    //shipTo=venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
//                    terms=venquotation.getVendor()!=null?venquotation.getVendor().getDebitTerm().getTermname():"";
                    billTo="Supplier";
                    isInclude=false;
//                    billAddr= venquotation.getVendor()!=null?venquotation.getVendor().getAddress():"";
                    filter_names.add("requestforquotation.ID");
                    filter_params.add(RFQ.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getRFQDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = RFQ.getMemo();
                }
//                cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
                 addShipTo=!(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode==StaticValues.AUTONUM_INVOICE);
            if(mode!=StaticValues.AUTONUM_RFQ) {
                PdfPTable addressMainTable = null,addressMainTable1 = null;
                if (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID()) && mode==StaticValues.AUTONUM_PURCHASEORDER) {
                    addressMainTable = ExportRecordHandler.getAddressTableForBCHL(accPurchaseOrderobj, customerName,billAddr,customerEmail,pOrder,currencyid);
                }else{
                    if (isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                         linkHeader= "";
                         addressMainTable = ExportRecordHandler.getAddressTableForSATS(customerName, billAddr, customerEmail, billTo, shipAddr, addShipTo, customerAccountCode, terms,shiplenght);
                    } if (isSATSCompany && (mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                         linkHeader= "";
                        addressMainTable = ExportRecordHandler.getAddressTableForSATSForPO(customerName, billAddr, customerEmail, billTo, shipAddr, addShipTo, customerAccountCode, terms,shiplenght);
                    } else {
                        if ((mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_VENQUOTATION) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                            addressMainTable1 = ExportRecordHandler.getSupplierAddress(customerName, billAddr, customerEmail, billTo);
                            addressMainTable = ExportRecordHandler.getAddressTable(null, billtoAddress, null, "Bill To", shipAddr, addShipTo);
                        } else {
                            addressMainTable = ExportRecordHandler.getAddressTable(customerName, billAddr, customerEmail, billTo, shipAddr, addShipTo);
                        }
                    }
                }
                if (!addShipTo) {                    
                    PdfPTable shipToTable = ExportRecordHandler.getDetailsTableForVRNet(authHandlerDAOObj, salesPerson, entryDate,invno,"","", terms,currencyid,formatter);
                    PdfPCell cel3 = new PdfPCell(shipToTable);
                    cel3.setBorder(0);
                    addressMainTable.addCell(cel3);
                }
                if((mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER||(!isSATSCompany && mode==StaticValues.AUTONUM_PURCHASEORDER)||mode==StaticValues.AUTONUM_VENQUOTATION) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()))||(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))){
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
                String referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);                
                if(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()) && mode==StaticValues.AUTONUM_INVOICE){
                    PdfPTable detailsTable=ExportRecordHandler.getDetailsTableForVRNET(headerDetailsForVrnet, referenceNumber, terms, dueDate, formatter, salesPerson);
                     mainCell12 = new PdfPCell(detailsTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                   if(addShipTo) 
                        mainTable.addCell(mainCell12);
                }else{
                    PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,referenceNumber,terms,dueDate,shipDate,formatter,isInclude, shipvia, fob);
                     mainCell12 = new PdfPCell(detailsTable);
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                  if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {  
                    mainTable.addCell(mainCell12);
                  }
                }
               
            }
                if (isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                    List lst = resultCustColumn.getEntityList();
                    int colcount = lst.size();
                    if (colcount > 0) {
                        PdfPTable CustomTable = ExportRecordHandler.getCustomTableForSATS(lst,jeId,accountingHandlerDAOobj);
                        PdfPCell cel3 = new PdfPCell(CustomTable);
                        cel3.setBorder(0);
                        mainCell12 = new PdfPCell(CustomTable);
                        mainCell12.setBorder(0);
//                        mainCell12.setPaddingTop(5);
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

                boolean companyFlag=(storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())&&(mode==StaticValues.AUTONUM_INVOICE||mode==StaticValues.AUTONUM_QUOTATION||mode==StaticValues.AUTONUM_SALESORDER));
                List<String> headerList = new ArrayList<String>();
                if(addShipTo){
                headerList.add(messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)));
				if(!linkHeader.equalsIgnoreCase("")){
					headerList.add(linkHeader);
				}
                }else{
                    headerList.add("Item");
                }               
                 //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name in header               
                if (!(companyFlag)) {
                    if (isDependentType && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                        headerList.add(messageSource.getMessage("acc.rem.itemName", null, RequestContextUtils.getLocale(request)));
                    } else {
                        headerList.add(messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)));
                    }

                }
                 
                if (addShipTo) {
                    if (isDependentType && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                        headerList.add(messageSource.getMessage("acc.rem.itemName.desc", null, RequestContextUtils.getLocale(request)));
                    } else {
                        headerList.add(messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)));
                    }
                     
                            if(invoiceType.equals(Constants.Acc_Retail_Invoice_Fixed) || invoiceType.equals(Constants.Acc_Car_Park_Operator)){
                                headerList.add(messageSource.getMessage("acc.rem.187.1", null, RequestContextUtils.getLocale(request)));
                            }else if(invoiceType.equals(Constants.Acc_Retail_Invoice_Variable)){
                                headerList.add(messageSource.getMessage("acc.rem.187.2", null, RequestContextUtils.getLocale(request)));
                            }else{
                                headerList.add(messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)));
                            }

                    if (mode != StaticValues.AUTONUM_RFQ) {
                        if (isDependentType && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                           if(invoiceType.equals(Constants.Acc_Retail_Invoice_Fixed) || invoiceType.equals(Constants.Acc_Car_Park_Operator)){
                                headerList.add(messageSource.getMessage("acc.rem.188.3", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                                headerList.add(messageSource.getMessage("acc.rem.193.1", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                            }else  if(invoiceType.equals(Constants.Acc_Retail_Invoice_Variable)){
                                headerList.add(messageSource.getMessage("acc.rem.188.4", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                                headerList.add(messageSource.getMessage("acc.rem.193.1", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                            }else{
                            headerList.add(messageSource.getMessage("acc.rem.188.1", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                            headerList.add(messageSource.getMessage("acc.rem.193.1", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                            }
                        } else {
                            headerList.add(messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                            headerList.add(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                        }
                    }
                } else {
                    headerList.add(messageSource.getMessage("acc.rem.prodDesc.Mixed", null, RequestContextUtils.getLocale(request)));
                    headerList.add("Part No.");
                    headerList.add(messageSource.getMessage("acc.rem.187.Mixed", null, RequestContextUtils.getLocale(request)));
                    if (mode != StaticValues.AUTONUM_RFQ) {
                        headerList.add(messageSource.getMessage("acc.rem.188.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                        headerList.add(messageSource.getMessage("acc.rem.193.Mixed", null, RequestContextUtils.getLocale(request)) + " " + authHandlerDAOObj.getCurrency(currencyid));
                }
                    
                }
                String[] header = (String[])headerList.toArray(new String[0]);
                globalTableHeader=header;
                PdfPTable table = null;
                if(mode==StaticValues.AUTONUM_RFQ){
                     table = ExportRecordHandler.getBlankTableForReportForRFQ();
                     productHeaderTableGlobalNo=1;
                }
                else{
                     table=ExportRecordHandler.getTable(linkHeader,true);
                     productHeaderTableGlobalNo=2;
                }
                    if(companyFlag)
                     {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns 
                          productHeaderTableGlobalNo=5;
                         if(mode==StaticValues.AUTONUM_INVOICE)
                             table=ExportRecordHandler.getBlankTableReportForINVOICE();
                          else
                              table=ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                if(mode==StaticValues.AUTONUM_RFQ)
                     table = ExportRecordHandler.getBlankTableForReportForRFQ();
                else
                    table=ExportRecordHandler.getTable(linkHeader,true);
                
                  if(companyFlag)
                     {  //add data for Company VRNET and sales,Invoice and Customer Quotation if Invoice then 6 column and for sales and customer quotation 5 columns  
                         if(mode==StaticValues.AUTONUM_INVOICE)
                             table=ExportRecordHandler.getBlankTableReportForINVOICE();
                          else
                              table=ExportRecordHandler.getBlankTableReportForNonINVOICE();
                    }
                HashMap<String, Object> requestParams=new HashMap<String, Object>();
                    requestParams.put(Constants.companyKey,CompanyID );
                    requestParams.put(Constants.globalCurrencyKey, baseCurrency);
                    requestParams.put(Constants.df,formatter);               
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
                int index=0;
                while (itr.hasNext()) {
                    String prodName = "";
                    boolean isSubproduct=false;
                    String prodDesc = "";
                    String partNo = "";
                     Phrase phrase1 =new Phrase();
                     Phrase phrase2 = new Phrase();
                    double quantity = 0, discountQuotation = 0, discountOrder = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    String linkTo="-";
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        if(!StringUtil.isNullOrEmpty(row.getDescription())){
                            prodDesc = row.getDescription();
                        }else{
                            if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInventory().getProduct().getDescription();
                            }
                        }                         
                        prodName = row.getInventory().getProduct().getName();
                        if (row.getInventory().getProduct().getParent() != null) {
                            isSubproduct = true;
                        }
                       if(!addShipTo) {
                        if (row.getDeliveryOrderDetail() != null && !StringUtil.isNullOrEmpty(row.getDeliveryOrderDetail().getPartno().trim())) {
                            String partno = row.getDeliveryOrderDetail().getPartno();
//                            prodDesc += "\n\n Part No. :";
                            partNo =  partno;
                        }
                       }
                        quantity =  (row.getInventory().isInvrecord() && (row.getInvoice().getPendingapproval()==0))? row.getInventory().getQuantity() : row.getInventory().getActquantity();
                        rate = row.getRate() ;
                        discount = row.getDiscount();
                        uom = row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        if (row.getDeliveryOrderDetail() != null) {
                            linkTo = row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber();
                        } else if (row.getSalesorderdetail() != null) {
                            linkTo = row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber();
                        } else if (row.getQuotationDetail() != null) {
                            linkTo = row.getQuotationDetail().getQuotation().getquotationNumber();
                        }
                    }  else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                        row3 = (BillingSalesOrderDetail) itr.next();
                        prodName = row3.getProductDetail();
                        prodDesc = "-";
                        quantity =  row3.getQuantity();
                        rate = row3.getRate() ;
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountOrder = (row3.getDiscountispercent() == 1)? rateInCurr*quantity *row3.getDiscount()/100 : row3.getDiscount();
                    }else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                        row4 = (BillingPurchaseOrderDetail) itr.next();
                        prodName = row4.getProductDetail();
                        prodDesc = "-";
                        quantity = row4.getQuantity();
                        rate = row4.getRate();
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountOrder = (row4.getDiscountispercent() == 1)? rateInCurr*quantity *row4.getDiscount()/100 : row4.getDiscount();
                    } else if(mode==StaticValues.AUTONUM_SALESORDER){
                        row5 = (SalesOrderDetail) itr.next();
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
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row5.getDiscountispercent() == 1)? rateInCurr*quantity *row5.getDiscount()/100 : row5.getDiscount();
                        uom = row5.getProduct().getUnitOfMeasure()==null?"":row5.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
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
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountOrder = (row6.getDiscountispercent() == 1)? rateInCurr*quantity *row6.getDiscount()/100 : row6.getDiscount();
                        uom = row6.getProduct().getUnitOfMeasure()==null?"":row6.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    }else if(mode==StaticValues.AUTONUM_QUOTATION){
                        row7 = (QuotationDetail) itr.next();
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
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row7.getDiscountispercent() == 1)? rateInCurr*quantity *row7.getDiscount()/100 : row7.getDiscount();
                        uom = row7.getProduct().getUnitOfMeasure()==null?"":row7.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    }else if(mode==StaticValues.AUTONUM_VENQUOTATION){
                        row8 = (VendorQuotationDetail) itr.next();
                        if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                            prodDesc = row8.getDescription();
                        }else{
                            if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                prodDesc = row8.getProduct().getDescription();
                            }
                        }                        
                        prodName = row8.getProduct().getName();
                        quantity =  row8.getQuantity();
                        rate = row8.getRate() ;
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                        double rateInCurr = rate;//(Double) bAmt.getEntityList().get(0);
                        discountQuotation = (row8.getDiscountispercent() == 1)? rateInCurr*quantity *row8.getDiscount()/100 : row8.getDiscount();
                        uom = row8.getProduct().getUnitOfMeasure()==null?"":row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    }else if(mode==StaticValues.AUTONUM_RFQ){
                        row9 = (RequestForQuotationDetail) itr.next();
                       if (!StringUtil.isNullOrEmpty(row9.getProduct().getDescription())) {
                            prodDesc = row9.getProduct().getDescription();
                        }
                        prodName = row9.getProduct().getName();
                        quantity =  row9.getQuantity();
//                        rate = row8.getRate() ;
//                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
//                        double rateInCurr = (Double) bAmt.getEntityList().get(0);
//                        discountQuotation = (row8.getDiscountispercent() == 1)? rateInCurr*quantity *row8.getDiscount()/100 : row8.getDiscount();
                        uom = row9.getProduct().getUnitOfMeasure()==null?"":row9.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    }else {
                        row1 = (BillingInvoiceDetail) itr.next();
                        prodName = row1.getProductDetail();
                        prodDesc = "-";
                        quantity =  row1.getQuantity();
                        rate = row1.getRate() ;
                        discount = row1.getDiscount()!=null?row1.getDiscount():null;
//                        uom = row1.getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getInventory().getProduct().getUnitOfMeasure().getName();
                         if (row1.getSalesOrderDetail() != null) {
                            linkTo = row1.getSalesOrderDetail().getSalesOrder().getSalesOrderNumber();
                        }
                    }
                    if (!isSubproduct) {
                        invcell = createCell((++index) + ".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    }else{
                         invcell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                    } 
                   if(addShipTo){ 
                    if(!linkHeader.equalsIgnoreCase("")){
                        invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);                    
                    }  
                   }
                    //for Company VRNET in sales,Invoice and Customer Quotation Report exclude product name 
                    if(!(companyFlag)) {
                          if (isSubproduct) {
                            invcell = createCell(" --- "+prodName, fontSmallRegular, Element.ALIGN_LEFT,  Rectangle.RIGHT, 5);
                          }else{
                              invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                          }
                         table.addCell(invcell);
                      }              
                    if (isSubproduct) {
                        invcell = createCell(" --- "+prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.RIGHT, 5);
                    } else {
                        invcell = createCell(prodDesc, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    }
                   table.addCell(invcell);   
                 if(!addShipTo){   
                   invcell = createCell(partNo, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                   table.addCell(invcell);   
                 }

                    //String qtyStr = Double.toString(quantity);
                   // if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                        String qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    //}
                    if (quantity > 0) {
                        if(invoiceType.equals(Constants.Acc_Retail_Invoice_Variable)){
                            quantity=quantity/100;
                        invcell = createCell(qtyStr + "%" + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }else{
                            invcell = createCell(qtyStr + " " + uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }
                    } else {
                        invcell = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    }
                    table.addCell(invcell);
                    if(mode!=StaticValues.AUTONUM_RFQ) {
                        if( mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_VENQUOTATION){
                            bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                            double rateInBase = rate;//(Double) bAmt.getEntityList().get(0);
                           rate = rateInBase;
                        }
                        if (rate > 0) {
                            invcell = createCell(authHandler.formattedAmount(rate, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        } else {
                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }
                        table.addCell(invcell);

                        amount1 = rate*quantity;
                        if (amount1 > 0) {
                            invcell = createCell(authHandler.formattedAmount(amount1, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        } else {
                            invcell = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        }
                        table.addCell(invcell);

                        if (discount != null) {
                            amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (row.getDiscount().getDiscountValue()) : (row1.getDiscount().getDiscountValue());
                        }
                        if (discountQuotation != 0){
                            amount1 -= discountQuotation;
                        }
                        if (discountOrder != 0){
                            amount1 -= discountOrder;
                            discountQuotation = discountOrder;
                        }
                        double rowTaxPercent=0;
                        double rowTaxAmount=0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName="";
                        if (row!= null&&row.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row.getTax().getID());                        
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row.getTax().getName();
                            if (row.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. - DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount()+row.getRowTermTaxAmount();
                        }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        else if (row1!= null&&row1.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row1.getTax().getName();
                            if (row1.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. - DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                        }
                            } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1 * rowTaxPercent / 100;
                            }
                        }
                        else if (row3!= null&&row3.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row3.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row3.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row3.getRowTaxAmount();
                            }
                        }  else if (row4!= null&&row4.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row4.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row4.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row4.getRowTaxAmount();
                            }
                        }  else if (row5!= null&&row5.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row5.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row5.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row5.getRowTaxAmount();
                            }
                        }  else if (row6!= null&&row6.getTax() != null) {
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row6.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row6.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row6.getRowTaxAmount();
                            }
                        }  else if (row7!= null&&row7.getTax() != null){
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row7.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row7.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row7.getRowTaxAmount();
                            }
                        }  else if (row8!= null&&row8.getTax() != null){
                            requestParams.put("transactiondate", entryDate);
                            requestParams.put("taxid", row8.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row8.getTax().getName();
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row8.getRowTaxAmount();
                        }
                        }

                        if(discount!=null || discountQuotation!=0){  //For Discount Row
                            table=ExportRecordHandler.getDiscountRowTable(authHandlerDAOObj, table,currencyid,discount,discountQuotation,mode,linkHeader);
                        }

                        if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
                            table=ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,currencyid,amount1,rowTaxPercent,mode,linkHeader,rowTaxAmount);
                        }
                         amount1+=rowTaxAmount;//amount1+=amount1*rowTaxPercent/100;
                         total += amount1;
                         
                    } else {
//                        invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
//                        invcell = createCell("", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                        table.addCell(invcell);
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
                int cellCount=0,lastRowCellCount=0;
                if((!linkHeader.equalsIgnoreCase(""))){// for link header column in Invoice 
                    if(companyFlag)
                     {      // if Company VRNET and sales,Invoice and Customer Quotation then 7 column 
                            cellCount=60;             
                            lastRowCellCount=54;
                     }else{
                            // if Company VRNET and sales,Invoice and Customer Quotation then 6 column    
                            cellCount=70;             
                            lastRowCellCount=63;
                            // for 10 blank rows 10*7=70 and last row 7*9=63
                    }                    
                }else{ 
                    if(companyFlag)
                     { //if Company VRNET and sales,Invoice and Customer Quotation then 5 columns     
                            cellCount=50;             
                            lastRowCellCount=45;
                     }else{//if Company VRNET and sales,Invoice and Customer Quotation then 6 columns
                        cellCount=60;
                        lastRowCellCount=54;
                     }   
                }
                
                for (int j = 1; j <= cellCount; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);                    
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    if(j>lastRowCellCount && !companyFlag){
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT+Rectangle.BOTTOM);
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
                    float addBlankRows = (document.getPageSize().getHeight() - belowProdTableContent - aboveProdTableContent - table.getTotalHeight())/blankRowHeight;
                    int noOfCols = cellCount/10;
                    int BlankCellCnt = (int) (addBlankRows*noOfCols);
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
                table.setWidths(new float[]{40,60});
                
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }
                if(mode!=StaticValues.AUTONUM_RFQ) {
                    PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    //cell3.setColspan(6);
                 if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {   
                    table.addCell(cell3);
                 }else if (!invoiceType.equals(Constants.Acc_Marine_InvoiceId)){
                    table.addCell(cell3);
                }
    //                if(mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER){
    //                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, total, cEntry.getCurrency().getCurrencyID(), entryDate, 0);
    //                    double baseTotalAmount = (Double) bAmt.getEntityList().get(0);
    //                    total = baseTotalAmount;
    //                }
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {
                        table.addCell(cell3);
                    }else if (!invoiceType.equals(Constants.Acc_Marine_InvoiceId)){
                    table.addCell(cell3);
                }
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
                    String mainTaxName="";
                    if(mainTax!=null){ //Get tax percent
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", mainTax.getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                        mainTaxName=mainTax.getName();
                    }

                    double term=0;
                    if(mode==StaticValues.AUTONUM_INVOICE){
//                        ExportRecordHandler.appendTermDetails(accInvoiceDAOobj,authHandlerDAOObj,inv, table, currencyid,mode);
                        totalDiscount = inv.getDiscount();
                        totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : 0;
                        mainTaxName=inv.getTax() != null ?inv.getTax().getName():"";
                        totalAmount = inv.getCustomerEntry().getAmount();
                    } else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_PURCHASEORDER||mode==StaticValues.AUTONUM_SALESORDER||mode==StaticValues.AUTONUM_QUOTATION||mode==StaticValues.AUTONUM_VENQUOTATION){
                        totalAmount = total;
                        term=0;
                             
//                           total+=term;
//                           totalAmount+=term;
                        if(mode==StaticValues.AUTONUM_QUOTATION && quotation.getDiscount() != 0){
                            if(!quotation.isPerDiscount()){
                                    discountTotalQuotation = quotation.getDiscount();
                                    total = total - quotation.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * quotation.getDiscount()/100;
                                    total -= (total * quotation.getDiscount()/100);
                                    totalAmount = total;
                            }
                        } else if(mode==StaticValues.AUTONUM_VENQUOTATION && venquotation.getDiscount() != 0){
                            if(!venquotation.isPerDiscount()){
                                    discountTotalQuotation = venquotation.getDiscount();
                                    total = total - venquotation.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * venquotation.getDiscount()/100;
                                    total -= (total * venquotation.getDiscount()/100);
                                    totalAmount = total;
                            }
                        } else if(mode==StaticValues.AUTONUM_PURCHASEORDER && pOrder.getDiscount() != 0){
                            if(!pOrder.isPerDiscount()){
                                    discountTotalQuotation = pOrder.getDiscount();
                                    total = total - pOrder.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * pOrder.getDiscount()/100;
                                    total -= (total * pOrder.getDiscount()/100);
                                    totalAmount = total;
                            }
                        } else if(mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER && po.getDiscount() != 0){
                            if(!po.isPerDiscount()){
                                    discountTotalQuotation = po.getDiscount();
                                    total = total - po.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * po.getDiscount()/100;
                                    total -= (total * po.getDiscount()/100);
                                    totalAmount = total;
                            }
                        } else if(mode==StaticValues.AUTONUM_SALESORDER && sOrder.getDiscount() != 0){
                            if(!sOrder.isPerDiscount()){
                                    discountTotalQuotation = sOrder.getDiscount();
                                    total = total - sOrder.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * sOrder.getDiscount()/100;
                                    total -= (total * sOrder.getDiscount()/100);
                                    totalAmount = total;
                            }
                        } else if(mode==StaticValues.AUTONUM_BILLINGSALESORDER && so.getDiscount() != 0){
                            if(!so.isPerDiscount()){
                                    discountTotalQuotation = so.getDiscount();
                                    total = total - so.getDiscount();
                                    totalAmount = total;
                            } else {
                                    discountTotalQuotation = total * so.getDiscount()/100;
                                    total -= (total * so.getDiscount()/100);
                                    totalAmount = total;
                            }
                        }
                        totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                        total+=term;
                        totalAmount+=term;
                        totalAmount = total + totaltax;
                    }else {
                        totalDiscount = inv1.getDiscount();
                        totaltax = inv1.getTaxEntry() != null ? inv1.getTaxEntry().getAmount() : 0;
                        mainTaxName=inv1.getTax() != null ?inv1.getTax().getName():"";
                        totalAmount =  (inv1.getCustomerEntry().getAmount());
                    }
                    totalAmounttodisplay=totalAmount;
//                    if(mode!=StaticValues.AUTONUM_SALESORDER){
//                            cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
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
                        taxNameStr.append(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)));
    //                }
                    cell3 = createCell(taxNameStr.toString(), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    //cell3.setColspan(6);
                    if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {
                            table.addCell(cell3);
                        } else if (!invoiceType.equals(Constants.Acc_Marine_InvoiceId)) {
                        table.addCell(cell3);
                    }
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                     if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {
                            table.addCell(cell3);
                        }else if (!invoiceType.equals(Constants.Acc_Marine_InvoiceId)){
                    table.addCell(cell3);
                }
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        ExportRecordHandler.appendTermDetails(accInvoiceDAOobj, authHandlerDAOObj, inv, table, currencyid, mode, CompanyID);
                    }
                    if (mode == StaticValues.AUTONUM_VENQUOTATION) {
                        term = ExportRecordHandler.appendTermDetailsVendorQuotation(accPurchaseOrderobj, authHandlerDAOObj, row8.getVendorquotation(), table, currencyid, mode, CompanyID);
                    }
                    if (mode == StaticValues.AUTONUM_QUOTATION) {
                        term = ExportRecordHandler.appendTermDetailsQuotation(accSalesOrderDAOobj, authHandlerDAOObj, row7.getQuotation(), table, currencyid, mode, CompanyID);
                    }
                    if (mode == StaticValues.AUTONUM_PURCHASEORDER) {
                        term = ExportRecordHandler.appendTermDetailsPurchaseOrder(accPurchaseOrderobj, authHandlerDAOObj, row6.getPurchaseOrder(), table, currencyid, mode, CompanyID);
                    }

                    if (mode == StaticValues.AUTONUM_SALESORDER) {
                        term = ExportRecordHandler.appendTermDetailsSalesOrder(accSalesOrderDAOobj, authHandlerDAOObj, row5.getSalesOrder(), table, currencyid, mode, CompanyID);
                    }
    //                for (int i = 0; i < 5; i++) {
    //                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
    //                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
    //                    invcell.setPadding(5);
    //                    invcell.setBorder(0);
    //                    table.addCell(invcell);
    //                }
                    cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                     if (isSATSCompany && mode == StaticValues.AUTONUM_INVOICE) {
                            cell3 = createCell(messageSource.getMessage("acc.rem.197.1", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                        }
                    //cell3.setColspan(6);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);

                   // addTableRow(mainTable, table);
                     if (isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                            addShipTo=false;
                        }
                    
                    PdfPTable summeryTable=ExportRecordHandler.getSummeryTable(table,mainTaxName,addShipTo);

                    mainCell12 = new PdfPCell(summeryTable);
                    mainCell12.setBorder(0);
                     if (!(isSATSCompany && mode == StaticValues.AUTONUM_INVOICE)) {
                    mainCell12.setPaddingTop(5);
                     }
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);                
                    mainTable.addCell(mainCell12);

                    KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                    String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId, CompanyID);
                    String currencyname = currency.getName();
                    cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                    PdfPTable table2 = new PdfPTable(1);
                    table2.addCell(cell3);
                    PdfPCell mainCell62 = new PdfPCell(table2);
                    mainCell62.setBorder(0);
                    mainCell62.setPadding(10);
                    mainCell62.setPaddingTop(5);
                   if(addShipTo) 
                    mainTable.addCell(mainCell62);
                }
                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
//                Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+":  ",fontSmallBold);
                String DescType=pref.getDescriptionType(); 
                Phrase phrase2 = new Phrase(DescType+": "+ memo,fontSmallRegular);
//                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
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
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);                

                CreditNote creNote = null;
                DebitNote dbNote = null;
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                com  = null;
                Account cEntry = null;
                invno = "";
                double cndnTotalAmount = 0;
                Date entryDate = null;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;
                double discountMain = 0;
                double subTotal = 0;

                if(mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingCreditNote.class.getName(), billid);
                    biCreNote = (BillingCreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=biCreNote.getJournalEntry().getDetails();
                    customerObj = new Customer();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    customer=(Customer)session.get(Customer.class,acc.getID());
                        customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
                        if(customerObj!=null)
                                break;
                    }
                    com = biCreNote.getCompany();
                    invno = biCreNote.getCreditNoteNumber();
                    entryDate = biCreNote.getJournalEntry().getEntryDate();
                }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingDebitNote.class.getName(), billid);
                    biDeNote = (BillingDebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = biDeNote.getCompany();
                    Set<JournalEntryDetail> entryset=biDeNote.getJournalEntry().getDetails();
                    vendorObj=new Vendor();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                        vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
                        if(vendorObj!=null)
                            break;
                    }
                    invno = biDeNote.getDebitNoteNumber();
                    entryDate = biDeNote.getJournalEntry().getEntryDate();
                }
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                    creNote = (CreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=(creNote.getJournalEntry() != null)?creNote.getJournalEntry().getDetails():null;
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
                     cndnTotalAmount=creNote.getCnamount();
//                    entryDate = (creNote.isNormalCN())?creNote.getJournalEntry().getEntryDate():creNote.getCreationDate();
                    entryDate = creNote.getCreationDate();
                //inv = (Invoice) session.get(Invoice.class, billid);
                }else if(mode == StaticValues.AUTONUM_DEBITNOTE ){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = dbNote.getCompany();
                    Set<JournalEntryDetail> entryset=(dbNote.isNormalDN())?dbNote.getJournalEntry().getDetails():null;
                    vendorObj=dbNote.getVendor();
                   
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
//                    entryDate = (dbNote.isNormalDN())?dbNote.getJournalEntry().getEntryDate():dbNote.getCreationDate();
                    entryDate = dbNote.getCreationDate();
                    cndnTotalAmount=dbNote.getDnamount();
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
                if(mode == StaticValues.AUTONUM_CREDITNOTE||mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    theader = messageSource.getMessage("acc.accPref.autoCN", null, RequestContextUtils.getLocale(request));
                }else if(mode == StaticValues.AUTONUM_DEBITNOTE||mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                     theader = messageSource.getMessage("acc.accPref.autoDN", null, RequestContextUtils.getLocale(request));
                }
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
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

                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,invno,theader,formatter);

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
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);

                customerName = "";
                String vendorAccountCode = "";
                String customerEmail = "";
                String terms = ""; 
                String billTo="";
                String shipAddress = "";
                String billAddress = "";
                String memo = "";
                Iterator itr = null;
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                
                if(mode==StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail= customerObj!=null?customerObj.getEmail():"";
                    addrParams.put("isBillingAddress", false);
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
                    shipAddress=accountingHandlerDAOobj.getCustomerAddress(addrParams); 
                    terms=customerObj!=null?customerObj.getCreditTerm().getTermname():"";    
                    billTo="Bill To";
                    addrParams.put("isBillingAddress", true);
                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(biCreNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo =biCreNote.getMemo();
                }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail= vendorObj!=null?vendorObj.getEmail():"";
                    terms=vendorObj!=null?vendorObj.getDebitTerm().getTermname():"";
                    billTo="Supplier";
                    addrParams.put("isBillingAddress", true);
                    addrParams.put("companyid", vendorObj.getCompany().getCompanyID());
                    addrParams.put("vendorid", vendorObj.getID());
                    billAddress =accountingHandlerDAOobj.getVendorAddress(addrParams);
                    filter_names.add("debitNote.ID");
                    filter_params.add(biDeNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getBillingDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biDeNote.getMemo();
                }else if(mode==StaticValues.AUTONUM_CREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    customerEmail= customerObj!=null?customerObj.getEmail():"";
                    addrParams.put("customerid", customerObj.getID());
                    addrParams.put("companyid", customerObj.getCompany().getCompanyID());
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
//                    shipAddress= accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    terms=customerObj!=null?customerObj.getCreditTerm().getTermname():"";
                    billTo="Bill To";
                    addrParams.put("isBillingAddress", true);
//                    billAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo =creNote.getMemo();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    customerEmail= vendorObj!=null?vendorObj.getEmail():"";
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

                PdfPTable addressMainTable=ExportRecordHandler.getAddressTable(customerName,billAddress,customerEmail,billTo,shipAddress,true);

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
                if((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag)
                {
                    String[] header = {messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.het.11", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.dnList.gridAmt", null, RequestContextUtils.getLocale(request))};
                table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{20,40,40});
               
                globalTableHeader=header;
                productHeaderTableGlobalNo=3;      
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }
                }
                else{
            
                String[] header = {messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{6, 20, 42, 12, 16});
               
                globalTableHeader=header;
                productHeaderTableGlobalNo=4;      
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
                    double cndnDiscount=0;
                    if (mode == StaticValues.AUTONUM_CREDITNOTE ) {
                        row = (CreditNoteDetail) itr.next();
                        if(!otherwiseFlag){    
                            if (!StringUtil.isNullOrEmpty(row.getInvoiceRow().getInventory().getProduct().getDescription())) {
                                prodDesc = row.getInvoiceRow().getInventory().getProduct().getDescription();
                            }
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
                                uom = row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                            } catch(Exception ex){//In case of exception use uom="";
                            } 
                        }
                        else if(otherwiseFlag){
                            cndnName = row.getInvoice().getInvoiceNumber();
                            cndnDiscount=row.getDiscount().getDiscount();
                            cndnTotalOtherwiseAmount+=cndnDiscount;
                        }
                    }else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                        row1 = (DebitNoteDetail) itr.next();
                        if(!otherwiseFlag){   
                         if (!StringUtil.isNullOrEmpty(row1.getGoodsReceiptRow().getInventory().getProduct().getDescription())) {
                            prodDesc = row1.getGoodsReceiptRow().getInventory().getProduct().getDescription();
                        }
                        prodName = row1.getGoodsReceiptRow().getInventory().getProduct().getName();
                        quantity =  row1.getQuantity();
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
                            uom = row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA();
                        } catch(Exception ex){//In case of exception use uom="";
                        }
                    }
                        else if(otherwiseFlag){
                            cndnName = row1.getGoodsReceipt().getGoodsReceiptNumber();
                            cndnDiscount=row1.getDiscount().getDiscount();
                            cndnTotalOtherwiseAmount+=cndnDiscount;
                        }
                    }else if(mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                        row2 = (BillingCreditNoteDetail) itr.next();                         
                        prodName = row2.getInvoiceRow().getProductDetail();
                        prodDesc = "-";
                        quantity =  row2.getQuantity();
                        if(row2.getDiscount()!=null){
                            if (row2.getTotalDiscount() != null) {
                                discountMain = discountMain + row2.getTotalDiscount();
                                total = total - row2.getTotalDiscount();
                            }    
                            discount = row2.getDiscount();
                        }                        
                        if(row2.getTaxAmount()!=null){
                            taxMain = taxMain + row2.getTaxAmount();
                            total = total + row2.getTaxAmount();
                        }                        
                    }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                        row3 = (BillingDebitNoteDetail) itr.next();
                         prodName = row3.getGoodsReceiptRow().getProductDetail();
                         prodDesc = "-";
                        quantity =  row3.getQuantity();
                        if(row3.getDiscount()!=null){
                            if (row3.getTotalDiscount() != null) {
                                discountMain = discountMain + row3.getTotalDiscount();
                                total = total - row3.getTotalDiscount();
                            }    
                            discount = row3.getDiscount();
                        }
                        if(row3.getTaxAmount()!=null){
                            taxMain = taxMain + row3.getTaxAmount();
                            total = total + row3.getTaxAmount();
                        }
                   }

                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
  
                    if((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag)
                    {
                        invcell = createCell(cndnName, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(authHandlerDAOObj.getFormattedCurrency(cndnDiscount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        ExportRecordHandler.addTableRow(mainTable, table);
                        table = new PdfPTable(3);
                        table.setWidthPercentage(100);
                        table.setWidths(new float[]{20,40,40});
                    } 
                    else{
                        invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(prodDesc, fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        
                        invcell = createCell((double)quantity+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(invcell);
                        invcell = createCell(authHandlerDAOObj.getFormattedCurrency(discount.getDiscountValue(), currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
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


  
                if((mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_DEBITNOTE) && otherwiseFlag)
                {
                    
                    for (int i = 1; i <= 15; i++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if(i>12)
                        {
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT+Rectangle.BOTTOM);
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
                    table.setWidths(new float[]{7, 20,25, 24, 24});
                    
                    PdfPCell cell3 = createCell(messageSource.getMessage("acc.pdf.24", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalOtherwiseAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    
                    cell3 = createCell(messageSource.getMessage("acc.customerList.gridAmountDue", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalAmount-cndnTotalOtherwiseAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                    cell3 = createCell(messageSource.getMessage("acc.product.gridTotal", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM, 5);
                    cell3.setColspan(4);
                    table.addCell(cell3);
                    cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(cndnTotalAmount, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                    table.addCell(cell3);
                }
                else{
                    for (int i = 1; i <= 25; i++) {
                        invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if(i>20)
                            invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT+Rectangle.BOTTOM);
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
                table.setWidths(new float[]{7, 20,25, 24, 24});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);                
                cell3.setColspan(4);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(subTotal, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                double term=0;
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                        term = ExportRecordHandler.appendTermDetailsCreditNote(accCreditNoteDAOobj, authHandlerDAOObj, row.getCreditNote(), table, currencyid, mode, CompanyID);
                    }
                if (mode == StaticValues.AUTONUM_DEBITNOTE) {
                        term = ExportRecordHandler.appendTermDetailsDebitNote(accDebitNoteobj, authHandlerDAOObj, row1.getDebitNote(), table, currencyid, mode, CompanyID);
                    }
                
                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);                
                cell3.setColspan(4);
                table.addCell(cell3);               
                invcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , discountMain, currencyid, CompanyID);
                invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                invcell.setPadding(5);
                invcell.setBorder(Rectangle.BOX);
                table.addCell(invcell);
                cell3 = createCell(messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                cell3.setColspan(4);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT+Rectangle.BOTTOM , 5);
                cell3.setColspan(4);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total+term, currencyid, CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                
                }

                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainCell5.setPaddingLeft(10);
                mainCell5.setPaddingRight(10);
                mainTable.addCell(mainCell5);

                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
                //Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+" : ",fontSmallBold);
               String DescType=pref.getDescriptionType(); 
                Phrase phrase2 = new Phrase(DescType+": "+memo,fontSmallRegular);
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
                } else {
                    gr1 = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), billid);
                     if(gr1.getApprover()!=null)
                        approverName=gr1.getApprover().getFirstName() +" "+gr1.getApprover().getLastName();
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

                com =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCompany():gr1.getCompany();
                String company[] = new String[4];
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
                if (isSATSCompany && mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    invCell = createCell(theader, fontTbl1, Element.ALIGN_LEFT, 0, 0);
                }
                tab2.addCell(invCell);
                 String grno=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getGoodsReceiptNumber():gr1.getBillingGoodsReceiptNumber();
//                Date entryDate=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getJournalEntry().getEntryDate():gr1.getJournalEntry().getEntryDate();
                Date entryDate=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCreationDate():gr1.getJournalEntry().getEntryDate();
                PdfPTable tab4 = ExportRecordHandler.getDateTable(entryDate, grno, theader, formatter);
                if (isSATSCompany && (mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                    tab4 = ExportRecordHandler.getDateTableForSATS(entryDate, grno, "Doc.", formatter);
                } else {
                    tab4 = ExportRecordHandler.getDateTable(entryDate, grno, theader, formatter);
                }
                if (isCompanyTemplateLogo) {
                    tab2= ExportRecordHandler.getDateTable2(entryDate,grno,theader,formatter,invCell);   

                }
                
                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                if (!isCompanyTemplateLogo) {
                    userTable2.setWidths(new float[]{60, 40});
                    if (isSATSCompany && (mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                        tab3 = ExportRecordHandler.getCompanyInfoForSATS(company);
                    } else {
                        tab3 = ExportRecordHandler.getCompanyInfo(company);
                    }
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
                //table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
//                table1.addCell(cel2);

                if (isSATSCompany && mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    table1.addCell(cel2);
                    table1.addCell(cell1);
                } else {
                    table1.addCell(cell1);
                    table1.addCell(cel2);
                }
                
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
//                cell2=createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request))+"  ",fontSmallBold,Element.ALIGN_LEFT,0,5);
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
//                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.from", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
//                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                tab5.addCell(cell3);
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);
                String vendorName = "";
                String vendorTerms = "";
                String billTo="Supplier";
                String billAddress="";
                String shipAddress="";
                String venAddress="";
                Date dueDate = null;
                boolean isInclude=false; //Hiding or Showing P.O. NO field in single PDF 
                Date shipDate = null;   
                String shipvia = null;   
                String fob = null;   
                String linkHeader="";
                
                String[] headerDetails = {"P.O. No.", "Terms", "Due Date", "Ship Date", "Ship Via", "FOB"};
                if(mode==StaticValues.AUTONUM_GOODSRECEIPT){
                    vendorName = gr.getVendor()==null?gr.getVendorEntry().getAccount().getName():gr.getVendor().getName();
                    vendorTerms=gr.getVendor()==null?"":gr.getVendor().getDebitTerm().getTermname();
                    shiplenght=gr.getShiplength();
                    if(pref.isWithInvUpdate()){
                        linkHeader="PO/GR/VQ No.";
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
                    shipAddress=CommonFunctions.getBillingShippingAddressWithAttn(gr.getBillingShippingAddresses(),false);
                    if(StringUtil.isNullOrEmpty(shipAddress)){ //For old record in which addresses were saved in invoice table 
                    shipAddress=gr.getShipTo()==null?"":gr.getShipTo();
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
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                grRequestParams.put("order_by", order_by);
                grRequestParams.put("order_type", order_type);

                KwlReturnObject idresult = null;
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
                        idresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);}
            } else {
                    filter_names.add("billingGoodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                }
                Iterator itr = idresult.getEntityList().iterator();                
                
                if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                PdfPTable addressMainTable;
                PdfPTable addressMainTable1=null;
                if ((mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT || mode == StaticValues.AUTONUM_GOODSRECEIPT) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID())) || (storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))) {
                    if (isSATSCompany && (mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                        linkHeader = "";
                        addressMainTable = ExportRecordHandler.getAddressTableForSATS(vendorName, billAddress, "", billTo, shipAddress, addShipTo, transactionAccountCode, vendorTerms, shiplenght);
                    } else {
                        addressMainTable1 = ExportRecordHandler.getSupplierAddress(vendorName, venAddress, "", "Supplier");
                        addressMainTable = ExportRecordHandler.getAddressTable(null, billAddress, null, "Bill To", shipAddress, true);
                    }
                }else
                   addressMainTable=ExportRecordHandler.getAddressTable(vendorName,billAddress,"",billTo,shipAddress,true);
                   
                if(!isSATSCompany && (mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT||mode==StaticValues.AUTONUM_GOODSRECEIPT) && !((storageHandlerImpl.GetVRnetCompanyId().contains(com.getCompanyID()))||(storageHandlerImpl.GetBCHLCompanyId().contains(com.getCompanyID())))){
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
             if (!isSATSCompany && (mode != StaticValues.AUTONUM_GOODSRECEIPT)) {   
                mainTable.addCell(mainCell12);
             }

//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainTable.addCell(mainCell14);

//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
                
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
                        if(isDependentType && mode==StaticValues.AUTONUM_GOODSRECEIPT)
                            header=new String[]{messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),linkHeader,messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID),messageSource.getMessage("acc.rem.export.dependentTypeValue", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))+" "+authHandlerDAOObj.getCurrency(rowCurrenctID)};
                        else
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
//                Iterator itr =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getRows().iterator():gr1.getRows().iterator();
                
                GoodsReceiptDetail row=null;
                BillingGoodsReceiptDetail row1=null;
                ExpenseGRDetail exprow=null;
                int index = 0;
                while (itr.hasNext()) {
                    String linkTo="-";
                    table.setSplitLate(false);
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
                    } else {
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
                        double rowTaxAmount=0;
                        boolean isRowTaxApplicable=false;
                        String rowTaxName="";
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (exprow!= null&&exprow.getTax() != null) { 
//                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getCreationDate());
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
//                                  grcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(row.getDescription(),baseUrl));
                                 grcell =new PdfPCell(ExportRecordHandler.getHtmlCellTable(row.getInventory().getProduct().getDescription(),baseUrl));
//                                 grcell.setBorder(0);
                                  //grcell.setBorder(0);
//                                 grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                                grcell = createCell(row.getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                            }else if (!StringUtil.isNullOrEmpty(row.getInventory().getProduct().getDescription())) {
                                grcell = createCell(row.getInventory().getProduct().getDescription(), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
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
                        String qtyString="";
                        if(mode == StaticValues.AUTONUM_GOODSRECEIPT){
                            qtyString=Double.toString((row.getInventory().isInvrecord()&& (row.getGoodsReceipt().getPendingapproval()==0))? row.getInventory().getQuantity() : row.getInventory().getActquantity());
                        }else if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                                qtyString=Double.toString((Double)row1.getQuantity());                                
                        }                       
                        grcell = createCell(qtyString+ " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) :""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandler.formattedAmount((mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate()), CompanyID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
//                        grcell = calculateDiscount(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount(), rowCurrenctID);
//                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                        grcell.setPadding(5);
//                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                        table.addCell(grcell);
                        if(isDependentType && mode == StaticValues.AUTONUM_GOODSRECEIPT){
                            String dependentType=row.getDependentType();
                          
                            try{
                              dependentType=String.valueOf(Integer.parseInt(dependentType));
                            }catch(Exception e){
                                 MasterItemPrice masterItemPrice = (MasterItemPrice) kwlCommonTablesDAOObj.getClassObject(MasterItemPrice.class.getName(), dependentType);
                                 if(masterItemPrice!=null){
                                     dependentType=masterItemPrice.getValue();
                                 }
                            }
                             grcell = createCell((mode == StaticValues.AUTONUM_GOODSRECEIPT ? dependentType : ""), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                             table.addCell(grcell);
                        }
                        double amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * ((row.getInventory().isInvrecord()&& (row.getGoodsReceipt().getPendingapproval()==0))? row.getInventory().getQuantity() : row.getInventory().getActquantity()) : row1.getRate() * row1.getQuantity();
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
//                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("transactiondate", row.getGoodsReceipt().getCreationDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                            if(taxObj[1] != null){
                                isRowTaxApplicable = true;
                            }
                            rowTaxName=row.getTax().getName();
                            if(row.isWasRowTaxFieldEditable()){ //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row.getRowTaxAmount() + row.getRowTermTaxAmount();
                        }
                            }else{// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
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
                            if(row1.isWasRowTaxFieldEditable()){ //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                                if (isRowTaxApplicable) {
                                    rowTaxAmount = row1.getRowTaxAmount();
                        }
                            }else{// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                                rowTaxAmount = amount1*rowTaxPercent/100;
                            }
                        }
                         if(!StringUtil.isNullOrEmpty(rowTaxName)){   //For Tax Row
//                            table=ExportRecordHandler.getTaxRowTable(authHandlerDAOObj, table,rowTaxName,rowCurrenctID,amount1,rowTaxPercent,mode,linkHeader);
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

                if (isExpenseInv) {
                    for (int j = 1; j <= 56; j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > 52) {
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM);
                        }
                        table.addCell(grcell);
                    }
                } else {
                    int closeLine=0;
                    int closeLine1=0;
                    int closeLine2=0;
                    if(isDependentType && mode == StaticValues.AUTONUM_GOODSRECEIPT){
                        closeLine=12;
                        closeLine1=14;
                        closeLine2=13;
                    }
                     for (int j = 1; j <=((linkHeader.equalsIgnoreCase(""))?84+closeLine:98+closeLine1); j++) {
                        grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        if (j > ((linkHeader.equalsIgnoreCase(""))?78+closeLine:91+closeLine2)) {
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
//                if(row!=null){
//                 if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
//                    ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode);
//                 }   
//                }
//                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
//                table.addCell(cell3);
//                grcell = ExportRecordHandler.calculateDiscount(authHandlerDAOObj , mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getDiscount():gr1.getDiscount(), rowCurrenctID);
//                grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                grcell.setBorder(Rectangle.BOX);
//                grcell.setPadding(5);
//                table.addCell(grcell); 
                cell3 = createCell(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.BOX, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTaxEntry().getAmount():0):(gr1.getTaxEntry()!=null?gr1.getTaxEntry().getAmount():0),rowCurrenctID, CompanyID), fontSmallRegular,Element.ALIGN_RIGHT, Rectangle.BOX,5);
                table.addCell(cell3); 
                if(row!=null){
                 if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    ExportRecordHandler.appendTermDetailsGoodsReceipt(accGoodsReceiptobj, authHandlerDAOObj, row.getGoodsReceipt(), table, currencyid, mode, CompanyID);
                 }   
                }
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
                mainCell62.setPadding(10);
                mainCell62.setPaddingTop(5);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{100});
                helpTable.setWidthPercentage(100);
                //Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+":  ",fontSmallBold);
                String DescType=pref.getDescriptionType();
                Phrase phrase2 = new Phrase(DescType+": "+(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getMemo():gr1.getMemo()),fontSmallRegular);
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

            } else if (mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT || mode == StaticValues.AUTONUM_INVOICERECEIPT || mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT || mode == StaticValues.AUTONUM_PAYMENT||mode == StaticValues.AUTONUM_BILLINGPAYMENT) {

                Receipt rc = null;
                BillingReceipt rc1 = null;
                Invoice inv = null;
                BillingInvoice inv1 = null;
                Payment pc = null;
                BillingPayment pc1= null;
                boolean iscontraentryflag = false;
               
                iscontraentryflag = Boolean.parseBoolean(request.getParameter("contraentryflag")); 
                
                if (mode == StaticValues.AUTONUM_RECEIPT) {
                    rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
                    currencyid = (rc.getCurrency()==null)? currencyid : rc.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
//                    }
//                    config = new com.krawler.utils.json.base.JSONObject(rc.getTemplateid().getConfigstr());
                } else if (mode == StaticValues.AUTONUM_BILLINGRECEIPT){
                    rc1 = (BillingReceipt) kwlCommonTablesDAOObj.getClassObject(BillingReceipt.class.getName(), billid);
                    currencyid = (rc1.getCurrency()==null)? currencyid : rc1.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
//                    }
//                    config = new com.krawler.utils.json.base.JSONObject(rc1.getTemplateid().getConfigstr());
                } else if (mode == StaticValues.AUTONUM_INVOICERECEIPT ){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);    
                    inv = (Invoice) cap.getEntityList().get(0);
                    config = new com.krawler.utils.json.base.JSONObject(inv.getTemplateid().getConfigstr());
                   currencyid = (inv.getCurrency()==null)? currencyid : inv.getCurrency().getCurrencyID();
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    config = new com.krawler.utils.json.base.JSONObject(inv1.getTemplateid().getConfigstr());
                    currencyid = (inv1.getCurrency()==null)? currencyid : inv1.getCurrency().getCurrencyID();
                } else if (mode == StaticValues.AUTONUM_PAYMENT) {
                    pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
                    currencyid = (pc.getCurrency()==null)? currencyid : pc.getCurrency().getCurrencyID();
//                    if (iscontraentryflag) {
//                        rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
//                    }
                } else if (mode == StaticValues.AUTONUM_BILLINGPAYMENT) {
                    pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
                    currencyid = (pc1.getCurrency()==null)? currencyid : pc1.getCurrency().getCurrencyID();
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
                int receiptType=0;
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList();
                HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
                List<String> debitAccountNameRow= new ArrayList<String>();
                List<String> creditAccountNameRow= new ArrayList<String>();
                List<Double> debitAccountAmount = new ArrayList<Double>();
                List<Double> creditAccountAmount = new ArrayList<Double>();
                HashMap pdoMAp = new HashMap();
                HashMap<String,Object> addrParams=new HashMap<String, Object>();
                addrParams.put("isDefaultAddress", true);
                if(mode == StaticValues.AUTONUM_BILLINGRECEIPT) {
                    receiptNumber = rc1.getBillingReceiptNumber();
                    journalEntryDate = rc1.getJournalEntry().getEntryDate();
                    PayDetail = rc1.getPayDetail();
                    memo = rc1.getMemo();
                    com = rc1.getCompany();
                    ismanycrdb=rc1.isIsmanydbcr();
                   
                    if(ismanycrdb){
                        filter_names.add("billingReceipt.ID");
                        filter_params.add(rc1.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject pdoresult = accReceiptDAOobj.getBillingReceiptDetailOtherwise(rRequestParams);
                        List<BillingReceiptDetailOtherwise> list1=pdoresult.getEntityList();
                        Iterator pdoRow = list1.iterator();
                        if (pdoRow != null && list1.size() > 0) {
                            for (BillingReceiptDetailOtherwise pdo : list1) {
                                if (rc1.getID().equals(pdo.getBillingReceipt().getID())) {
                                        if (rc1.isIsmanydbcr()) {
                                                if (pdo.isIsdebit()) {
                                                    debitAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    debitAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                } else {
                                                    creditAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    creditAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                }
                                        }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow",debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount",debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow",creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount",creditAccountAmount);
                        pdoMAp.put("creditdebitflag",2);
                        
                    }
                    AccountName = rc1.getPayDetail() != null ? rc1.getPayDetail().getPaymentMethod().getAccount().getName():"";
                  
//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = rc1.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                        }
//                    }
                } else if (mode == StaticValues.AUTONUM_RECEIPT) {
                    receiptNumber = rc.getReceiptNumber();
//                    journalEntryDate = rc.getJournalEntry().getEntryDate();
                    journalEntryDate = rc.getCreationDate();
                    PayDetail = rc.getPayDetail();
                    memo = rc.getMemo(); 
                    com = rc.getCompany();
                    receiptType=rc.getReceipttype();
                    ismanycrdb=rc.isIsmanydbcr();
                    Customer cust=rc.getCustomer();
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
                    
                    accname=(rc.getReceipttype()==9||rc.getReceipttype()==2)? customerNames : ( (customer!=null)? cust.getName() : (vendor!=null)? vendor.getName():"");
                    
                    filter_names.add("receipt.ID");
                        filter_params.add(rc.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject pdoresult = accReceiptDAOobj.getReceiptDetailOtherwise(rRequestParams);
                        List<ReceiptDetailOtherwise> list1=pdoresult.getEntityList();
                        Iterator pdoRow=list1.iterator();
                    
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
                    amount = totalamount;

                    if (ismanycrdb) {
                        if (pdoRow != null && list1.size() > 0) {
                            for (ReceiptDetailOtherwise pdo : list1) {
                                if (rc.getID().equals(pdo.getReceipt().getID())) {
                                        if (rc.isIsmanydbcr()) {
                                                if (pdo.isIsdebit()) {
                                                    debitAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    debitAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                } else {
                                                    creditAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    creditAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                }
                                        }else{
                                                creditAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                creditAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                            
                                        }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow",debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount",debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow",creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount",creditAccountAmount);
                        pdoMAp.put("creditdebitflag",2);
                        
                    }
                    AccountName = rc.getPayDetail() != null ? rc.getPayDetail().getPaymentMethod().getAccount().getName():"";
                 
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
                } else if (mode == StaticValues.AUTONUM_BILLINGINVOICERECEIPT){
                    receiptNumber = inv1.getBillingInvoiceNumber();
                    journalEntryDate = inv1.getJournalEntry().getEntryDate();
//                    PayDetail = inv1.getPayDetail();
                    memo = inv1.getMemo();
                    com = inv1.getCompany();
                    AccountName = Constants.CashInHand;
                } else if (mode == StaticValues.AUTONUM_PAYMENT){
                    receiptNumber = pc.getPaymentNumber();
//                    journalEntryDate = pc.getJournalEntry().getEntryDate();
                    journalEntryDate = pc.getCreationDate();
                    PayDetail = pc.getPayDetail();
                    memo = pc.getMemo();
                    com = pc.getCompany();
                    ismanycrdb=pc.isIsmanydbcr();
                    receiptType=pc.getReceipttype();
                    Vendor vendor=pc.getVendor();
                    if (vendor != null) {
                        customer = vendor.getName();
                        addrParams.put("vendorid", vendor.getID());
                        addrParams.put("companyid", vendor.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", true);
                        address = accountingHandlerDAOobj.getVendorAddress(addrParams);
                    }
                    
                    Customer cust=null;
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
                    accname=(pc.getReceipttype()==9 ||pc.getReceipttype()==2)? vendorNames : ((vendor!=null)? vendor.getName() : (cust!=null)? cust.getName():"");
                    
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
                    amount=totalamount;
                    
                    if(ismanycrdb){
                        if (pdoRow != null && list1.size() > 0) {
                            for (PaymentDetailOtherwise pdo : list1) {
                                if (pc.getID().equals(pdo.getPayment().getID())) {
                                        if (pc.isIsmanydbcr()) {
                                                if (pdo.isIsdebit()) {
                                                    debitAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    debitAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                } else {
                                                    creditAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    creditAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                }
                                        }else{
                                            debitAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                            debitAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                        }
                                            
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow",debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount",debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow",creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount",creditAccountAmount);
                        pdoMAp.put("creditdebitflag",1);
                        
                    }
                   AccountName = pc.getPayDetail() != null ? pc.getPayDetail().getPaymentMethod().getAccount().getName():"";  
                            
//                    if (iscontraentryflag) {
//                        Set<JournalEntryDetail> entryset = pc.getJournalEntry().getDetails();
//                        Iterator itr = entryset.iterator();
//                        while (itr.hasNext()) {
//                            AccountName = ((JournalEntryDetail) itr.next()).getAccount().getName();
//                        }
//                    }
                } else if (mode == StaticValues.AUTONUM_BILLINGPAYMENT){
                    receiptNumber = pc1.getBillingPaymentNumber();
                    journalEntryDate = pc1.getJournalEntry().getEntryDate();
                    PayDetail = pc1.getPayDetail();
                    memo = pc1.getMemo();
                    com = pc1.getCompany();
                    ismanycrdb=pc1.isIsmanydbcr();
                        if(ismanycrdb){
                        filter_names.add("billingPayment.ID");
                        filter_params.add(pc1.getID());   
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);
                        KwlReturnObject pdoresult = accVendorPaymentobj.getBillingPaymentDetailOtherwise(rRequestParams);
                        List<BillingPaymentDetailOtherwise> list1=pdoresult.getEntityList();
                        Iterator pdoRow=list1.iterator(); 
                        if (pdoRow != null && list1.size() > 0) {
                            for (BillingPaymentDetailOtherwise pdo : list1) {
                                if (pc1.getID().equals(pdo.getBillingPayment().getID())) {
                                        if (pc1.isIsmanydbcr()) {
                                                if (pdo.isIsdebit()) {
                                                    debitAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    debitAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                } else {
                                                    creditAccountAmount.add(pdo.getAmount() + (pdo.getTax()!=null ? pdo.getTaxamount():0));
                                                    creditAccountNameRow.add(pdo.getAccount().getName()+(pdo.getTax()!=null ? ','+pdo.getTax().getName():""));
                                                }
                                        }
                                }
                            }
                        }
                        pdoMAp.put("debitAccountNameRow",debitAccountNameRow);
                        pdoMAp.put("debitAccountAmount",debitAccountAmount);
                        pdoMAp.put("creditAccountNameRow",creditAccountNameRow);
                        pdoMAp.put("creditAccountAmount",creditAccountAmount);
                        pdoMAp.put("creditdebitflag",1);
                        
                    }
                    AccountName = pc1.getPayDetail() != null ? pc1.getPayDetail().getPaymentMethod().getAccount().getName():"";
                  
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
                
                ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);
                ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer); 
                ExportRecordHandler.generateReceiptPDF(authHandlerDAOObj, kwlCommonTablesDAOObj, EnglishNumberToWordsOjb, 
                        messageSource, mainTable,request, com, logoPath, currencyid, receiptNumber, 
                        journalEntryDate, formatter, accname, address, amount, mode, PayDetail, memo, 
                        config, AccountName,iscontraentryflag,preText,baseUrl,ismanycrdb, pdoMAp );
                


            }else if(mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN){
                    ExportRecordHandler.addHeaderContents(CompanyPDFHeader,document, writer);    
                    ExportRecordHandler.addHeaderFooter(CompanyPDFFooter,document, writer);                    
                    com  = null;
                    Account cEntry;
                    invno = "";
                    Date entryDate = null;
                    String company[] = new String[4];
                    customerName = "";
                    String customerEmail = "";
                    String billAddres = "";
                    String billTo="";
                    String shipAddress = "";
                    String memo = "";
                    String orderID ="";
                    String theader = "";
                    String datetheader = "";
                    String pointPern = "";
                    String recQuantity = "";
                    String status="";
                    Date shipDate = null;
                    String shipvia = "";
                    String fob = "";
                    HashMap<String, Object> addrParams = new HashMap<String, Object>();
                    addrParams.put("isDefaultAddress", true);
                    
                    
                    if(mode == StaticValues.AUTONUM_DELIVERYORDER){
                        DeliveryOrder deliveryOrder=null;
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
                        String invoicePostText=deliveryOrder.getPostText()==null?"":deliveryOrder.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT)&& deliveryOrder.getTemplateid() != null)?deliveryOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        cEntry = deliveryOrder.getCustomer().getAccount();
                        invno = deliveryOrder.getDeliveryOrderNumber();
                        entryDate = deliveryOrder.getOrderDate();
                        customerName = deliveryOrder.getCustomer().getName();
                        status=deliveryOrder.getStatus() != null ? deliveryOrder.getStatus().getValue():"";
                        customerEmail= deliveryOrder.getCustomer()!=null?deliveryOrder.getCustomer().getEmail():"";
                        billTo = "Bill To";
                        if (deliveryOrder.getBillingShippingAddresses() != null) { //If DO have address from show address then showing that address otherwise showing customer address
                            billAddres = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), true);
                            shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(deliveryOrder.getBillingShippingAddresses(), false);
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
                        theader = messageSource.getMessage("acc.accPref.autoDO", null, RequestContextUtils.getLocale(request));
                        datetheader = messageSource.getMessage("acc.accPref.autoDateDO", null, RequestContextUtils.getLocale(request));
                        pointPern = "acc.common.to";
                        recQuantity = "acc.accPref.NewdeliQuant";
                    } else if(mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                        GoodsReceiptOrder grOrder=null;
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
                        
                        String invoicePostText=grOrder.getPostText()==null?"":grOrder.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?grOrder.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        cEntry = grOrder.getVendor().getAccount();
                        invno = grOrder.getGoodsReceiptOrderNumber();
                        entryDate = grOrder.getOrderDate();
                        status=grOrder.getStatus()!=null?grOrder.getStatus().getValue():""; //Added check to avoid NPException when status is null
                        customerName = grOrder.getVendor().getName();
                        customerEmail= grOrder.getVendor()!=null?grOrder.getVendor().getEmail():"";
                        billTo="Supplier";
                        if (grOrder.getBillingShippingAddresses() != null) { //If GRO have address from show address then showing that address otherwise showing customer address
                            billAddres = CommonFunctions.getBillingShippingAddressWithAttn(grOrder.getBillingShippingAddresses(), true);
                            shipAddress = CommonFunctions.getBillingShippingAddressWithAttn(grOrder.getBillingShippingAddresses(), false);
                        } else {
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
                        theader = messageSource.getMessage("acc.accPref.autoGRO", null, RequestContextUtils.getLocale(request));
                        datetheader =theader;
                        pointPern = "acc.common.from";
                        recQuantity = "acc.accPref.recQuant";
                    } else if(mode == StaticValues.AUTONUM_SALESRETURN){
                        SalesReturn salesReturn=null;
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
                        
                        String invoicePostText=salesReturn.getPostText()==null?"":salesReturn.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?salesReturn.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        cEntry = salesReturn.getCustomer().getAccount();
                        invno = salesReturn.getSalesReturnNumber();
                        entryDate = salesReturn.getOrderDate();
                        customerName = salesReturn.getCustomer().getName();
                        status=salesReturn.getStatus() != null ? salesReturn.getStatus().getValue():"";
                        customerEmail= salesReturn.getCustomer()!=null?salesReturn.getCustomer().getEmail():"";
                        addrParams.put("customerid", salesReturn.getCustomer().getID());
                        addrParams.put("companyid", com.getCompanyID());
                        addrParams.put("isBillingAddress", false);
                        shipAddress = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                        billTo="Bill To";
                        addrParams.put("isBillingAddress", true);
                        billAddres = accountingHandlerDAOobj.getCustomerAddress(addrParams);
                        memo = salesReturn.getMemo();
                        shipDate = salesReturn.getShipdate();
                        shipvia = salesReturn.getShipvia();
                        fob = salesReturn.getFob();
                        orderID = salesReturn.getID();
                        theader = messageSource.getMessage("acc.accPref.autoSR", null, RequestContextUtils.getLocale(request));
                        datetheader =theader;
                        pointPern = "acc.common.to";
                        recQuantity = "acc.accPref.returnQuant";
                    }else if(mode == StaticValues.AUTONUM_PURCHASERETURN){
                        PurchaseReturn purchaseReturn=null;
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
                        
                        String invoicePostText=purchaseReturn.getPostText()==null?"":purchaseReturn.getPostText();
                        postText =!(StringUtil.isNullOrEmpty(invoicePostText))?invoicePostText:(StringUtil.isNullOrEmpty(CompanyPDFPOSTTEXT))?purchaseReturn.getTemplateid().getPostText():CompanyPDFPOSTTEXT;
                        cEntry = purchaseReturn.getVendor().getAccount();
                        invno = purchaseReturn.getPurchaseReturnNumber();
                        entryDate = purchaseReturn.getOrderDate();
                        customerName = purchaseReturn.getVendor().getName();
                        status=purchaseReturn.getStatus() != null ? purchaseReturn.getStatus().getValue():"";
                        customerEmail= purchaseReturn.getVendor()!=null?purchaseReturn.getVendor().getEmail():"";
                        addrParams.put("vendorid", purchaseReturn.getVendor().getID());
                        addrParams.put("companyid", purchaseReturn.getCompany().getCompanyID());
                        addrParams.put("isBillingAddress", false);
                        shipAddress = accountingHandlerDAOobj.getVendorAddress(addrParams);
                        billTo="Supplier";
                        addrParams.put("isBillingAddress", true);
                        billAddres = accountingHandlerDAOobj.getVendorAddress(addrParams);
                        memo = purchaseReturn.getMemo();
                        shipDate = purchaseReturn.getShipdate();
                        shipvia = purchaseReturn.getShipvia();
                        fob = purchaseReturn.getFob();
                        orderID = purchaseReturn.getID();
                        theader = messageSource.getMessage("acc.accPref.autoPR", null, RequestContextUtils.getLocale(request));
                        datetheader =theader;
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

                    invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                    tab2.addCell(invCell);
                PdfPTable tab4= ExportRecordHandler.getDateTable(entryDate,invno,datetheader,formatter);   
                if (isCompanyTemplateLogo) {
                    tab2= ExportRecordHandler.getDateTable2(entryDate,invno,datetheader,formatter,invCell);   

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
//                    PdfPCell cell3 = createCell(messageSource.getMessage(pointPern, null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);

                    HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    order_by.add("srno");
                    order_type.add("asc");
                    invRequestParams.put("order_by", order_by);
                    invRequestParams.put("order_type", order_type);
                    KwlReturnObject idresult = null;

                    
                    
                    Iterator itr = null;

                    if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                        filter_names.add("deliveryOrder.ID");
                    } else if(mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                        filter_names.add("grOrder.ID");
                    } else if(mode == StaticValues.AUTONUM_SALESRETURN) {
                        filter_names.add("salesReturn.ID");
                    } else if(mode == StaticValues.AUTONUM_PURCHASERETURN) {
                        filter_names.add("purchaseReturn.ID");
                    }
                    
                    filter_params.add(orderID);
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                        idresult = accInvoiceDAOobj.getDeliveryOrderDetails(invRequestParams);
                    } else if(mode == StaticValues.AUTONUM_GOODSRECEIPTORDER){
                        idresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(invRequestParams);
                    } else if(mode == StaticValues.AUTONUM_SALESRETURN ) {
                        idresult = accInvoiceDAOobj.getSalesReturnDetails(invRequestParams);
                    }else if(mode == StaticValues.AUTONUM_PURCHASERETURN ) {
                        idresult = accGoodsReceiptobj.getPurchaseReturnDetails(invRequestParams);
                    }
                                        
                    itr = idresult.getEntityList().iterator();
                    

//                    cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                    tab5.addCell(cell3);
//                    cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
//                    tab5.addCell(cell3);
//                    cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
//                    tab5.addCell(cell3);

                    if(!StringUtil.isNullOrEmpty(preText)){
                             ExportRecordHandler.getHtmlCell(preText.trim(),mainTable,baseUrl);
                    }
                    
                    PdfPTable addressMainTable=ExportRecordHandler.getAddressTable(customerName,billAddres,customerEmail,billTo,shipAddress,true);
                    
                    PdfPCell mainCell14 = new PdfPCell(addressMainTable);
                    mainCell14.setBorder(0);      
                    mainCell14.setPaddingTop(5);
                    mainCell14.setPaddingLeft(10);
                    mainCell14.setPaddingRight(10);
                    mainTable.addCell(mainCell14);
                                                          
                    String linkHeader="";
                    if(mode == StaticValues.AUTONUM_DELIVERYORDER ) {
                        String[] headerDetails = {"CI/SO. No.", "Status","Ship Date", "Ship Via", "FOB"};
                        String referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);
                        PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,"-",status,null,shipDate,formatter,false, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                        linkHeader="CI/SO No.";
                    }else if(mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                        String[] headerDetails = new String[]{"VI/PO. No.", "Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                        String referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);
                        PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,"-",status,null,shipDate,formatter,false, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
		        linkHeader="VI/PO No.";
                    } else if(mode == StaticValues.AUTONUM_SALESRETURN) {
                        String[] headerDetails = {"DO/CI No.", "Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                        String referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);
                        PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,referenceNumber,status,null,shipDate,formatter,true, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                    } else if(mode == StaticValues.AUTONUM_PURCHASERETURN) {
                        String[] headerDetails = {"GR/VI No.", "Status", "Due Date", "Ship Date", "Ship Via", "FOB"};
                        String referenceNumber=ExportRecordHandler.getReferenceNumber(idresult.getEntityList(),mode);
                        PdfPTable detailsTable=ExportRecordHandler.getDetailsTable(headerDetails,referenceNumber,status,null,shipDate,formatter,true, shipvia, fob);
                        mainCell12 = new PdfPCell(detailsTable);
                    }                    
                                                        
                    mainCell12.setBorder(0);
                    mainCell12.setPaddingTop(5);
                    mainCell12.setPaddingLeft(10);
                    mainCell12.setPaddingRight(10);
                    mainCell12.setPaddingBottom(5);
                    mainTable.addCell(mainCell12);

                    
                    String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)),linkHeader,messageSource.getMessage("acc.rem.prodName", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.prodDesc", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)),messageSource.getMessage(recQuantity, null, RequestContextUtils.getLocale(request))};
                    PdfPTable table = ExportRecordHandler.getBlankTableForDO();
                    productHeaderTableGlobalNo=3;
                    PdfPCell invcell = null;
                    for (int i = 0; i < header.length; i++) {
                         invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                         invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                         invcell.setBackgroundColor(Color.LIGHT_GRAY);
                         invCell.setBorder(0);
                         invcell.setPadding(3);
                         table.addCell(invcell);
                    }
                    globalTableHeader=header;
                    ExportRecordHandler.addTableRow(mainTable, table); //Break table after adding header row
                    table = ExportRecordHandler.getBlankTableForDO();

                    int index=0;
                    while (itr.hasNext()) {
                             String prodName = "";
                             String prodDesc = "";
                             double quantity = 0,deliverdQuantity=0;
                             String uom = "";
                             String linkTo="-";
                             
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
                                    String partno = row8.getPartno(); 
                                    prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                                    prodName += "\n" + partno;
                                    prodName += "\n" ;
                                }
                                prodName += "\n" ;
                                quantity = row8.getActualQuantity();
                                deliverdQuantity = row8.getDeliveredQuantity();
                                uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
								if(row8.getCidetails()!=null){
                                    linkTo=row8.getCidetails().getInvoice().getInvoiceNumber();
                                }else if(row8.getSodetails()!=null){
                                    linkTo=row8.getSodetails().getSalesOrder().getSalesOrderNumber();                                    
                                } 
                            } else if (mode == StaticValues.AUTONUM_SALESRETURN) {
                                SalesReturnDetail row8 = (SalesReturnDetail) itr.next();
                                prodName = row8.getProduct().getName();
                                if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                                    prodDesc = row8.getDescription();
                                }else{
                                    if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                         prodDesc= row8.getProduct().getDescription();
                                     }
                                }
                                if(row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())){
                                    String partno = row8.getPartno(); 
                                    prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                                    prodName += "\n" + partno;
                                    prodName += "\n" ;
                                }
                                prodName += "\n" ;
                                quantity = row8.getActualQuantity();
                                deliverdQuantity = row8.getReturnQuantity();
                                uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                            } else if (mode == StaticValues.AUTONUM_PURCHASERETURN) {
                                PurchaseReturnDetail row8 = (PurchaseReturnDetail) itr.next();
                                prodName = row8.getProduct().getName();
                                if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                                    prodDesc = row8.getDescription();
                                }else{
                                    if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                         prodDesc= row8.getProduct().getDescription();
                                     }
                                }
                                if(row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())){
                                    String partno = row8.getPartno(); 
                                    prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                                    prodName += "\n" + partno;
                                    prodName += "\n" ;
                                }
                                prodName += "\n" ;
                                quantity = row8.getActualQuantity();
                                deliverdQuantity = row8.getReturnQuantity();
                                uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                            } else if(mode == StaticValues.AUTONUM_GOODSRECEIPTORDER) {
                                GoodsReceiptOrderDetails row8 = (GoodsReceiptOrderDetails) itr.next();
                                prodName = row8.getProduct().getName();
                                if(!StringUtil.isNullOrEmpty(row8.getDescription())){
                                    prodDesc = row8.getDescription();
                                }else{
                                    if (!StringUtil.isNullOrEmpty(row8.getProduct().getDescription())) {
                                         prodDesc= row8.getProduct().getDescription();
                                     }
                                }
                                if(row8.getPartno() != null && !StringUtil.isNullOrEmpty(row8.getPartno().trim())){
                                    String partno = row8.getPartno(); 
                                    prodName += "\n\n" + messageSource.getMessage("acc.do.partno", null, RequestContextUtils.getLocale(request)) + " :";
                                    prodName += "\n" + partno;
                                    prodName += "\n" ;
                                }
                                prodName += "\n" ;
                                quantity = row8.getActualQuantity();
                                deliverdQuantity = row8.getDeliveredQuantity();
                                uom = row8.getProduct().getUnitOfMeasure() == null ? "" : row8.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                                if(row8.getVidetails()!=null){
                                    linkTo=row8.getVidetails().getGoodsReceipt().getGoodsReceiptNumber();
                                }else if(row8.getPodetails()!=null){
                                    linkTo=row8.getPodetails().getPurchaseOrder().getPurchaseOrderNumber();
                                }  
                            }  
                             
                             
                             invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                             invCell.setBorder(0);
                             table.addCell(invcell);                             
                             invcell = createCell(linkTo, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                             invCell.setBorder(0);
                             table.addCell(invcell);
                             invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                             invCell.setBorder(0);
                             table.addCell(invcell);
                             invcell = new PdfPCell(ExportRecordHandler.getHtmlCellTable(prodDesc,baseUrl));      
                             invcell.setBorder(0);
                             table.addCell(invcell);
                             String qtyStr = Double.toString(quantity);
//                             if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION || mode==StaticValues.AUTONUM_DELIVERYORDER ) {
//                                            qtyStr = Double.toString((double)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
//                                        }
                             invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                             invCell.setBorder(0);
                             table.addCell(invcell);
                             invcell = createCell(Double.toString((double)deliverdQuantity)+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
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
                            invcell.setBorder(Rectangle.BOTTOM+Rectangle.LEFT+Rectangle.RIGHT);
                            table.addCell(invcell);
                    }
                    ExportRecordHandler.addTableRow(mainTable, table);
                    PdfPTable helpTable = new PdfPTable(new float[]{100});
                    helpTable.setWidthPercentage(100);
//                    Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+":  ",fontSmallBold);
                    String DescType=pref.getDescriptionType();
                    Phrase phrase2 = new Phrase(DescType+": "+memo,fontSmallRegular);
//                    PdfPCell pcell1 = new PdfPCell(phrase1);
                    PdfPCell pcell2 = new PdfPCell(phrase2);
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
              if (mode != StaticValues.AUTONUM_RECEIPT && mode != StaticValues.AUTONUM_BILLINGRECEIPT && mode != StaticValues.AUTONUM_INVOICERECEIPT && mode != StaticValues.AUTONUM_BILLINGINVOICERECEIPT && mode != StaticValues.AUTONUM_PAYMENT && mode != StaticValues.AUTONUM_BILLINGPAYMENT && mode != StaticValues.AUTONUM_DEBITNOTE && mode != StaticValues.AUTONUM_CREDITNOTE && mode != StaticValues.AUTONUM_BILLINGCREDITNOTE && mode != StaticValues.AUTONUM_BILLINGDEBITNOTE){
                PdfPTable table3 = new PdfPTable(2); //for 2 column
//                table3.setWidthPercentage(50);
                            
                 KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(),  userId);                                      
                //  KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(),  sessionHandlerImpl.getUserid(request));
                 User userDetails = (User) userResult.getEntityList().get(0);
                 String username = userDetails.getFirstName()+" "+userDetails.getLastName();
                PdfPCell cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingTop(-10);
//                cell3.setPaddingBottom(30);
                table3.addCell(cell3);
                cell3 =  (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN)? createCell("ORDER ACCEPTANCE", fontSmallBold1, Element.ALIGN_CENTER, 0, 0):createCell("", fontSmallBold1, Element.ALIGN_CENTER, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(-10);
                table3.addCell(cell3);
                if(addShipTo){
                cell3 = createCell("Prepared By: "+username, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }else{
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }
                cell3.setPaddingLeft(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);               
                if(addShipTo){
                cell3 = (mode == StaticValues.AUTONUM_DELIVERYORDER || mode == StaticValues.AUTONUM_GOODSRECEIPTORDER || mode == StaticValues.AUTONUM_SALESRETURN || mode == StaticValues.AUTONUM_PURCHASERETURN)?createCell("Kindly return within TWO DAYS of receipt with your signature and company stamp. Thank you.", fontSmallRegularsmall, Element.ALIGN_CENTER, 0, 0):createCell("", fontSmallRegularsmall, Element.ALIGN_CENTER, 0, 0);
                }else{
                    cell3 = createCell("", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }
                cell3.setPaddingRight(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                
//                cell3.setPaddingTop(-9);
                table3.addCell(cell3);
                
                if(addShipTo){ 
                    cell3 = createCell("Approved By: "+approverName, fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                }else{
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
                cell3 = createCell("Name:", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingRight(10);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                PdfPCell mainCell63 = new PdfPCell(table3);
                mainCell63.setBorder(0);
                if (!(isSATSCompany && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT || mode == StaticValues.AUTONUM_PURCHASEORDER))) {
                    mainTable.addCell(mainCell63);
                }                
             } 
             if(mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){// || mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE || mode == StaticValues.AUTONUM_PURCHASEORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
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
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)) +" : ______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                cell3.setPaddingLeft(10);
                cell3.setPaddingBottom(5);
                cell3.setPaddingTop(5);
                table3.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request))+" : _______________________________", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
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
                    mainTable=  new PdfPTable(1);
                    mainTable.setWidthPercentage(100);
                    ExportRecordHandler.getHtmlCell(postText.trim(), mainTable, baseUrl);
                  
                }
            }else{
                  if (!StringUtil.isNullOrEmpty(postText)) {
                    ExportRecordHandler.getHtmlCell(postText.trim(), mainTable, baseUrl);
                }
                    if (isDependentType && (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_GOODSRECEIPT)) {
                    PdfPTable SATSFooter = ExportRecordHandler.getFooterTableForSATS(customerName,invno,totalAmounttodisplay);
                    PdfPCell cel3 = new PdfPCell(SATSFooter);
                    cel3.setBorder(0);
                    mainTable.addCell(cel3);
                }  if (isDependentType && (mode == StaticValues.AUTONUM_PURCHASEORDER)) {
                    PdfPTable SATSFooter = ExportRecordHandler.getFooterTableForSATSForPO(customerName,invno,totalAmounttodisplay);
                    PdfPCell cel3 = new PdfPCell(SATSFooter);
                    cel3.setBorder(0);
                    mainTable.addCell(cel3);
                }
            }
                        
            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("Export:"+ex.getMessage(), ex);
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