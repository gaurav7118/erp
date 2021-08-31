/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.barcode;

import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.awt.image.BufferedImage;
import java.io.*;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.EAN128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.fourstate.RoyalMailCBCBean;
import org.krysalis.barcode4j.impl.fourstate.USPSIntelligentMailBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.postnet.POSTNETBean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author krawler
 */
public class accBarcodeGeneratorController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;;
    }

    public ModelAndView generateBarcodeUsingSerialNos(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        String barcodeType = request.getParameter("barcodeType");
        String barcodeGenCode = request.getParameter("base");   //1-Serial No., 2-Product ID, 3-SKU, 4-Barcode
        String ids[] = request.getParameterValues("barcodeNos");
        String productsID[] = request.getParameterValues("productsID");
        String barcodeName[] = request.getParameterValues("barcodeName");
        String prodPrice[]=request.getParameterValues("prodPrice");
        String mrpOfProd[]=request.getParameterValues("mrpOfProd");
        int dpi=Integer.parseInt(request.getParameter("dpi"));
        int height=Integer.parseInt(request.getParameter("height"));
        double barcdLabelHeight = Double.parseDouble(request.getParameter("barcdLabelHeight"));
        String barcdQty[]={};
        if(!barcodeGenCode.equals("1") && !barcodeGenCode.equals("3")){ //In Case of Serial No./SKU, We Generate the barcode on the selection of Serial No./SKU Code So need not to consider Barcode Qty.
            barcdQty= request.getParameterValues("quantity");
        }
        int qty=0;
        int count = 0;
        int barcodesCntr=0;
        boolean isDirectoryCreated = true;
        boolean isSuccess = false;
        JSONObject myjobj = new JSONObject();
         JSONArray myjarr = new JSONArray();
        try {
//            final int dpi = 300;
            org.krysalis.barcode4j.impl.AbstractBarcodeBean abstractBarcodeBean = null;
            if (ids != null) {
                for (int i = 0; i < ids.length; i++) {
                    count = 0;
                    if(!barcodeGenCode.equals("1") && !barcodeGenCode.equals("3")){  //1-Serial No.  &  3-SKU
                        qty= Integer.parseInt(barcdQty[i]);
                    }else{
                        qty=1;
                    }
                    while (count < qty) {
                        if (!ids[i].equals("")) {
//                            File outputFile = new File("/home/krawler/Nitin/barcodes/spring/" + barcodeType + "/" + ids[i] + count + ".png");
                            String directory = storageHandlerImpl.GetProfileImgStorePath() + "/"+Constants.ProductImages;
                            File outputFile = new File(directory);

                            if (!outputFile.isDirectory()) {
                                 isDirectoryCreated = outputFile.mkdirs();
                            }
                            if (!isDirectoryCreated) {
                                throw new AccountingException("Path " + directory + " doesn't exist on server.");
                            }
                            String filename = ids[i] + count + ".png";
                            if(filename.contains("/")){
                                filename = filename.replace("/", "-");
                            }
                            
                            outputFile = new File(directory + filename);

                            OutputStream out = new FileOutputStream(outputFile);
                            if (barcodeType.equals("CODE128")) { 
                                /* CODE128 Encodable Character Set : 
                                 * All 128 characters of ASCII & Values 128-255 in accordance with ISO 8859-1. 
                                 * There are referred to as extended ASCII. Generate high-readability Code 128 barcodes in JPEG, GIF & PNG image formats.
                                 */
                                abstractBarcodeBean = new Code128Bean();   
                                //msg = "For CODE-128 Type Barcode, Product ID / Serial Number / Barcode number should contain characters among of [(0-9), (A-Z), ($, %, +, -, .)]";
                            } else if (barcodeType.equals("CODE39")) {
                                /* CODE39 Encodable Character Set : 
                                 * Alphanumeric data: 0-9, A-Z)
                                 * Special characters: space $ % + - . 
                                 * Start/stop character *
                                 */
                                abstractBarcodeBean = new Code39Bean(); 
                                //msg = "For CODE39 Type Barcode, Product ID / Serial Number / Barcode number should contain characters among of [(0-9), (A-Z), ($, %, +, -, .)]";
                            } else if (barcodeType.equals("EAN128")) {
                                /* EAN-128 Encodable Character Set :
                                 * 0-9
                                 * length should be atleast 8 characters.
                                 */
                                abstractBarcodeBean = new EAN128Bean();
                                //msg = "For EAN-128 Type Barcode, Product ID / Serial Number must be a 8 or more digits numeric value.";
                            } else if (barcodeType.equals("EAN13")) {
                                /* EAN13 Encodable Character Set :
                                 * 0-9
                                 * length should be 12 or 13 digits.
                                 */
                                abstractBarcodeBean = new EAN13Bean(); 
                                //msg = "For EAN-13 Type Barcode, Product ID / Serial Number / Barcode number must be a 12 or 13 digits numeric value.";
                            } else if (barcodeType.equals("EAN8")) {
                                /* EAN8 Encodable Character Set :
                                 * 0-9
                                 * length should be 7 or 8 digits.
                                 */
                                abstractBarcodeBean = new EAN8Bean(); 
                                //msg = "For EAN-8, Product ID / Serial Number / Barcode number must be a 7 or 8 digits numeric value.";
                            } else if (barcodeType.equals("CODEBAR")) {
                                /* CODEBAR Encodable Character Set :
                                 * 0-9
                                 * - (Dash), $ (Dollar), : (Colon), / (Slash), . (Point), + (Plus)
                                 */
                                abstractBarcodeBean = new CodabarBean();  //numeric
                                //msg = "";
                            } else if (barcodeType.equals("UPCA")) {
                                /* UPCA Encodable Character Set :
                                 * 0-9
                                 * length should be 11 or 12 digits.
                                 */
                                abstractBarcodeBean = new UPCABean(); 
                                //msg = "";
                            } else if (barcodeType.equals("UPCE")) {
                                abstractBarcodeBean = new UPCEBean(); //"UPC-A message cannot be compacted to UPC-E. Message: 37312343339",
                                //Message must be 7 or 8 characters long.,
                                //"Valid number systems for UPC-E are 0 or 1. Found: 3"(i.e. start with 0 or 1)
                                //"Invalid checksum. Expected 8 but was 3"
                            } else if (barcodeType.equals("POSTNET")) {
                                /* POSTNET Encodable Character Set :
                                 * 0-9
                                 */
                                abstractBarcodeBean = new POSTNETBean(); 
                                //msg="";
                            } else if (barcodeType.equals("INTERLEAVED2OF5")) {
                                /* INTERLEAVED2OF5 Encodable Character Set :
                                 * 0-9
                                 * length should be 11 or 12 digits.
                                 */
                                abstractBarcodeBean = new Interleaved2Of5Bean();  
                                //msg="";
                            } else if (barcodeType.equals("ROYALMAILCUST")) {    //Currently not available from UI side. //Numeric
                                abstractBarcodeBean = new RoyalMailCBCBean();  
                                //msg="";
                            } else if (barcodeType.equals("USPSINTGNTMAIL")) {
                                /* USPSINTGNTMAIL Encodable Character Set :
                                 * 0-9
                                 * length should be within 20-31 digits.
                                 */
                                abstractBarcodeBean = new USPSIntelligentMailBean(); 
                                //msg="";
                            } else if (barcodeType.equals("DATAMATRICS")) {
                                /*
                                 * All ISO-8859-1 characters are valid message characters.
                                 * Using only numeric characters allows for smaller symbol sizes.
                                 */
                                abstractBarcodeBean = new DataMatrixBean(); //2d
                                //msg="Some internal probelm at the time of DataMatrix Barcode Generation.";
                            }


                            //Configure the barcode generator
                            abstractBarcodeBean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); //makes the narrow bar 
                            //width exactly one pixel
//            code128Bean.setFontSize(8);
                            abstractBarcodeBean.setFontSize(barcdLabelHeight);
                            if (barcodeType.equals("EAN13")){
                                abstractBarcodeBean.doQuietZone(true);  //True to show 1st digit of Barcode in EAN-13
                            } else {
                                abstractBarcodeBean.doQuietZone(false);  
                            }
                            abstractBarcodeBean.setHeight(height);
                            //abstractBarcodeBean.setMsgPosition(HumanReadablePlacement.HRP_TOP); //We can define the position of Human Readable Barcode Text
                            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                                    out, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

                            //Generate the barcode
                            abstractBarcodeBean.generateBarcode(canvas, ids[i]);

                            canvas.finish();
                            out.close();
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("imgName", ids[i] + count + ".png");
                            jsonObj.put("prodName", barcodeName[i]);
                            jsonObj.put("productsID", productsID[i]);
                            jsonObj.put("prodPrice", prodPrice[i]);
                            jsonObj.put("mrpOfProd", mrpOfProd[i]);
                            myjarr.put(jsonObj);
                            barcodesCntr++;
                        }
                        count++;

                    }
                }
            }
            isSuccess = true;
        } catch (IOException ex) {
            Logger.getLogger(accBarcodeGeneratorController.class.getName()).log(Level.SEVERE, null, ex);
            msg = ex.toString();
            isSuccess = false;
        } catch (AccountingException ex) {
            msg = ex.getMessage();
            isSuccess = false;
        } catch (Exception e) {
            Logger.getLogger(accBarcodeGeneratorController.class.getName()).log(Level.SEVERE, null, e);
            msg = "Invalid Barcode Number.";
            isSuccess = false;
        } finally {
            try {
                myjobj.put("data", myjarr);
                myjobj.put("success", isSuccess);
                myjobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accBarcodeGeneratorController.class.getName()).log(Level.SEVERE, null, ex);
                msg = ex.toString();
            }
        }
        return new ModelAndView("jsonView", "model", myjobj.toString());
    }
}
