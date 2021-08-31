/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount.impl;

import com.itextpdf.text.BaseColor;
import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.cyclecount.CycleCountService;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountBlankSheet {

    private static String imgPath = "";
    private static String companyName = "";
    private static Map config = null;
    private PdfPTable header = null;
    private PdfPTable footer = null;
    private static final long serialVersionUID = -8401651817881523209L;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
    private static String errorMsg = "";
    private CycleCountService cycleCountService;
    private StockService stockService;
    private static FontFamilySelector fontFamilySelector = new FontFamilySelector();
    private static final DateFormat yyyyMMdd_HIPHON = new SimpleDateFormat("yyyy-MM-dd");

    public void setCycleCountService(CycleCountService cycleCountService) {
        this.cycleCountService = cycleCountService;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    static {
        FontFamily fontFamily = new FontFamily();
        fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
        fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK));
        fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Times New Roman", 14, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.REPORT_TITLE, FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK));
        fontFamily.addFont(FontContext.SMALL_TEXT, FontFactory.getFont("Times New Roman", 12, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.TABLE_HEADER, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
        fontFamily.addFont(FontContext.TABLE_DATA, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
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
        for (File file : files) {
            try {
                BaseFont bfnt = BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontFamily = new FontFamily();
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

    public ByteArrayOutputStream getPdfData(HttpServletRequest request, Map config) throws ServiceException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        Document document = null;
        try {
            this.config = config;
            boolean landscape = true;
            boolean uomschema = false;
            boolean isLocationCompulsory = false;
            boolean isBatchCompulsory = false;
            boolean isBinCompulsory = false;
            boolean isRowCompulsory = false;
            boolean isRackCompulsory = false;
            boolean isSerialCompulsory = false;
            String colHeader = "";
            String colHeaderFinal = "";
            String fieldListFinal = "";
            String fieldList = "";
            String width = "";
            String align = "";
            String alignFinal = "";
            String widthFinal = "";
            String colHeaderArrStr[] = null;
            String dataIndexArrStr[] = null;
            String widthArrStr[] = null;
            String alignArrStr[] = null;
            int strLength = 0;
            float totalWidth = 0;


            document = null;
            Rectangle rec = null;
            Rectangle recPage = null;
            if (landscape) {
                recPage = new Rectangle(PageSize.A4.rotate());
            } else {
                recPage = new Rectangle(PageSize.A4);
            }
            recPage.setBackgroundColor(Color.WHITE);
            document = new Document(recPage, 15, 15, 60, 30);
            rec = document.getPageSize();
            totalWidth = rec.getWidth();

            // Set font style and size for document
            Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
            font.setColor(Color.BLACK);
            
            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new CycleCountBlankSheet.EndPage());
            document.open();
            // set flags for location, batch, rack, row, bin, serial activation details
            uomschema = config.containsKey("uomschema") ? (Boolean) config.get("uomschema") : false;
            isLocationCompulsory = config.containsKey("islocationcompulsory") ? (Boolean) config.get("islocationcompulsory") : false;
            isBatchCompulsory = config.containsKey("isbatchcompulsory") ? (Boolean) config.get("isbatchcompulsory") : false;
            isBinCompulsory = config.containsKey("isbincompulsory") ? (Boolean) config.get("isbincompulsory") : false;
            isRowCompulsory = config.containsKey("isrowcompulsory") ? (Boolean) config.get("isrowcompulsory") : false;
            isRackCompulsory = config.containsKey("israckcompulsory") ? (Boolean) config.get("israckcompulsory") : false;
            isSerialCompulsory = config.containsKey("isserialcompulsory") ? (Boolean) config.get("isserialcompulsory") : false;

            // get available column count for print
            int colCount = 0;
            if(!uomschema){
                colCount = 3; //Packaging, Casing Uom Count, Inner Uom Count
            } else{
                colCount = 1; //UOM
            }
            if(isLocationCompulsory){
                colCount++; // Location
            }
            if(isBatchCompulsory){
                colCount++; // Batch
            }
            if(isRackCompulsory){
                colCount++; // Rack
            }
            if(isRowCompulsory){
                colCount++; // Row
            }
            if(isBinCompulsory){
                colCount++; // Bin
            }
            if(isSerialCompulsory){
                colCount++; // Serial
            }
            colCount += 6; //S.No., Product ID, Product Description, Stock UOM Count, Quantity, Remark

            PdfPTable productMainRow = new PdfPTable(colCount);
            productMainRow.setWidthPercentage(100);

            PdfPCell productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("S.No.", FontContext.TABLE_HEADER, font)));
            productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Product ID", FontContext.TABLE_HEADER, font)));
            productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Product Description", FontContext.TABLE_HEADER, font)));
            productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            if(!uomschema){
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Packaging", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Casing Uom Count", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Inner Uom Count", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            } else{
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("UOM", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            }
            if(isLocationCompulsory){
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Location", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
            }
            if (isRowCompulsory) {
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Row", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
            }
            if (isRackCompulsory) {
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Rack", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
            }
            if (isBinCompulsory) {
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Bin", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
            }
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("System Quantity", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Actual Quantity", FontContext.TABLE_HEADER, font)));
            productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
            if (isBatchCompulsory) {
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Batch", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
                }
            if (isSerialCompulsory) {
                productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Serials", FontContext.TABLE_HEADER, font)));
                productCell.setBorder(PdfPCell.BOX);
                productMainRow.addCell(productCell);
                }
            productCell = new PdfPCell(new Paragraph(fontFamilySelector.process("Remark", FontContext.TABLE_HEADER, font)));
            productCell.setBorder(PdfPCell.BOX);
            productMainRow.addCell(productCell);
//            document.add(productMainRow);

            Set<Store> storeSet = new HashSet<>();
            storeSet.add((Store) config.get("store"));
            java.util.List<Object[]> ccProducts = cycleCountService.getCycleCountProducts((Company) config.get("company"), (Date) config.get("businessDate"));
            Company company = (Company) config.get("company");
            String companyId = company.getCompanyID();
            int detailRowCount = 1;
            for (Object[] p : ccProducts) {
                Product productObj = (Product) p[0];
                String pid = productObj.getID();
                String productCode = productObj.getProductid();
                String productDesc = productObj.getDescription();
                String uom = productObj.getUnitOfMeasure() != null ? productObj.getUnitOfMeasure().getNameEmptyforNA() : "-";
                boolean batchForProduct = productObj.isIsBatchForProduct();
                boolean serialForProduct = productObj.isIsSerialForProduct();
                String casingUomName = p[1] != null ? (String) p[1] : "-";
                String innerUomName = p[2] != null ? (String) p[2] : "-";
                String looseUomName = p[3] != null ? (String) p[3] : "-";
                double casingUomValue = p[4] != null ? (Double) p[4] : 0.0;
                double innerUomValue = p[5] != null ? (Double) p[5] : 0.0;
                double looseUomValue = p[6] != null ? (Double) p[6] : 0.0;
                boolean locationForProduct = productObj.isIslocationforproduct();
                boolean rowForProduct = productObj.isIsrowforproduct();
                boolean rackForProduct = productObj.isIsrackforproduct();
                boolean binForProduct = productObj.isIsbinforproduct();

                String packaging = Packaging.packagingPreview(casingUomName, casingUomValue, innerUomName, innerUomValue, looseUomName, looseUomValue);

                PdfPTable productDetailRow = new PdfPTable(colCount);
                productDetailRow.setWidthPercentage(100);

                java.util.List<Stock> productBatchList = stockService.getBatchSerialListByStoreProductLocation(pid, storeSet, null,null,false);
                if (!productBatchList.isEmpty()) {
                    for (Stock stock : productBatchList) {

                        productDetailRow = new PdfPTable(colCount);
                        
                        PdfPCell productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(detailRowCount), FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(productCode, FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(productDesc, FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        if(!uomschema){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(packaging, FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(casingUomValue), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(innerUomValue), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else{
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(uom, FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        if(isLocationCompulsory && locationForProduct){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getLocation().getName(), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if(isLocationCompulsory && locationForProduct){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        if (isRowCompulsory && rowForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getRow().getName(), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if(isRowCompulsory && !rowForProduct){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        if (isRackCompulsory && rackForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getRack().getName(), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if(isRackCompulsory && !rackForProduct){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        if (isBinCompulsory && binForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getBin().getName(), FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if(isBinCompulsory && !binForProduct){
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(authHandler.roundQuantity(stock.getQuantity(), companyId)), FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        if (isBatchCompulsory && batchForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getBatchName(), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if (isBatchCompulsory && !batchForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        if (isSerialCompulsory && serialForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(stock.getSerialNames(), FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        } else if (isSerialCompulsory && !serialForProduct) {
                            productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                            productDetailCell.setBorder(PdfPCell.BOX);
                            productMainRow.addCell(productDetailCell);
                        }
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);

                        detailRowCount++;
                    }
                } else {
                    PdfPCell productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(detailRowCount), FontContext.TABLE_DATA, font)));
                    productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(productCode, FontContext.TABLE_DATA, font)));
                    productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(productDesc, FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    if(!uomschema){
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(packaging, FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(casingUomValue), FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(innerUomValue), FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    } else{
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process(uom, FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    if(isLocationCompulsory){
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    if (isRowCompulsory) {
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    if (isRackCompulsory) {
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    if (isBinCompulsory) {
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                    productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("0", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA, font)));
                    productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    if (isBatchCompulsory) {
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                    }
                    if (isSerialCompulsory) {
                        productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("NA", FontContext.TABLE_DATA, font)));
                        productDetailCell.setBorder(PdfPCell.BOX);
                        productMainRow.addCell(productDetailCell);
                }
                    productDetailCell = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA, font)));
                    productDetailCell.setBorder(PdfPCell.BOX);
                    productMainRow.addCell(productDetailCell);
                    detailRowCount++;
                }

            }
            document.add(productMainRow);


        } catch (Exception e) {
            throw ServiceException.FAILURE("CycleCountBlankSheet.getPdfData", e);
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return baos;
    }

    public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Rectangle page = document.getPageSize();
            try {
                getHeaderFooter(document);
            } catch (ServiceException ex) {
                Logger.getLogger(CycleCountBlankSheet.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Add page header
            header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());

            // Add page footer
            footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());

            // Add page border
            if (false) {
                int bmargin = 8;  //border margin
                PdfContentByte cb = writer.getDirectContent();
                cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                cb.setColorStroke(Color.LIGHT_GRAY);
                cb.stroke();
            }

        }
    }

    public void getHeaderFooter(Document document) throws ServiceException {
        try {
//            fontSmallRegular.setColor(tColor);
            java.util.Date dt = new java.util.Date();
            String date = "yyyy-MM-dd";
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
            String DateStr = dtf.format(dt);

            // -------- header ----------------
            //Report Subtitle(s)
            String[] SubTitles = new String[]{((Store) config.get("store")).getFullName(), yyyyMMdd_HIPHON.format(config.get("businessDate"))};
            header = new PdfPTable(1);
            PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process("Cycle Count Sheet", FontContext.REPORT_TITLE, Color.BLACK)));
            cell.setBorder(0);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(cell);

            for (int i = 0; i < SubTitles.length; i++) {
                cell = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(SubTitles[i], FontContext.FOOTER_NOTE)))));
                cell.setBorder(0);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.addCell(cell);
            }


            PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
            headerSeparator.setBorder(PdfPCell.BOX);
            headerSeparator.setPadding(0);
            headerSeparator.setColspan(3);
            header.addCell(headerSeparator);
            // -------- header end ----------------

            // -------- footer  -------------------
            footer = new PdfPTable(2);
            PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
            footerSeparator.setBorder(PdfPCell.BOX);
            footerSeparator.setPadding(0);
            footerSeparator.setColspan(3);
            footer.addCell(footerSeparator);

            String PageDate = DateStr;
            PdfPCell pagerDateCell = new PdfPCell(new Phrase(fontFamilySelector.process(PageDate, FontContext.FOOTER_NOTE, Color.GRAY)));
            pagerDateCell.setBorder(0);
            footer.addCell(pagerDateCell);

            String FootPager = String.valueOf(document.getPageNumber());//current page no
            PdfPCell footerPageNocell = new PdfPCell(new Phrase(fontFamilySelector.process(FootPager, FontContext.FOOTER_NOTE, Color.GRAY)));
            footerPageNocell.setBorder(0);
            footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            footer.addCell(footerPageNocell);
            // -------- footer end   -----------
        } catch (Exception e) {
            throw ServiceException.FAILURE("CycleCountBlankSheet.getHeaderFooter", e);
        }
    }
}
