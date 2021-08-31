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
package com.krawler.esp.handlers;

import com.krawler.common.service.ServiceException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import java.awt.*;
import java.awt.image.*;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletException;
import org.apache.commons.fileupload.FileUploadException;
import sun.awt.image.BufferedImageGraphicsConfig;

public class FileUploadHandler {

    public HashMap getItems(HttpServletRequest request) throws ServiceException, ServletException {
        HashMap itemMap = null;
        try {
            String filePath = StorageHandler.GetProfileImgStorePath() + "User_Profile_Images/";
            java.io.File destDir = new java.io.File(filePath);
            if (!destDir.exists()) { //Create profile images folder if not present
                destDir.mkdirs();
            }
            FileItemFactory factory = new DiskFileItemFactory(4096, new File(filePath));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(1000000);
            List fileItems = upload.parseRequest(request);
            Iterator iter = fileItems.iterator();
            itemMap = new HashMap();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (item.isFormField()) {
                    itemMap.put(item.getFieldName(), item.getString());
                } else {
                    itemMap.put(item.getFieldName(), item);
                }
            }
        } catch (FileUploadException ex) {
            throw new ServletException("acc.field.Imagesizeshouldbeupto1MB");
        } catch (Exception e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("FileUploadHandler.getItems", e);
        }
        return itemMap;
    }

    public void uploadFile(FileItem fileItem, String fileName, String destinationDirectory) throws ServiceException {
        try {
            File destDir = new File(destinationDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            fileItem.write(new File(destinationDirectory, fileName));
        } catch (Exception e) {
            throw ServiceException.FAILURE("FileUploadHandler.uploadFile", e);
        }
    }

    public void uploadImage(FileItem fileItem, String fileName, String destinationDirectory, int width, int height, boolean company, boolean original) throws ServiceException {
        try {
            File destDir = new File(destinationDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File temp = new File(destinationDirectory, "temp_" + fileItem.getName());
            fileItem.write(temp);
            String fName = (fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName);
            imgResize(temp.getAbsolutePath(), width, height, destinationDirectory + "/" + fName, company, original);
            temp.delete();
        } catch (Exception e) {
            throw ServiceException.FAILURE("FileUploadHandler.uploadImage", e);
        }
    }

    public static String getImageExt() {
        return ".png";
    }

    public static String getCompanyImageExt() {
        return ".png";
    }

    private static BufferedImage createImage(String path, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        image = createCompatibleImage(image);
        image = createBlurImage(image);
        image = resizeImage(image, width, height);
        return image;
    }

    private static BufferedImage createCompatibleImage(BufferedImage image) {
        GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage result = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(image, null);
        g2.dispose();
        return result;
    }

    public static BufferedImage createBlurImage(BufferedImage image) {
        float ninth = 1.0f / 9.0f;
        float[] blurKernel = {
            ninth, ninth, ninth,
            ninth, ninth, ninth,
            ninth, ninth, ninth
        };

        Map map = new HashMap();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RenderingHints hints = new RenderingHints(map);
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
        return op.filter(image, null);
    }

    private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public final void imgResize(String sourcePath, int Width, int Height,
            String destPath, boolean isCompany, boolean ori) throws IOException {
        try {
            String ext = getImageExt();
            String type = "jpeg";
            int typeRGB = BufferedImage.TYPE_INT_RGB;
            Image sourceImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(sourcePath)).getImage();
            if (isCompany) {
                ext = getCompanyImageExt();
                type = "PNG";
                typeRGB = BufferedImage.TYPE_INT_ARGB;
                int imageWidth = sourceImage.getWidth(null);
                int imageHeight = sourceImage.getHeight(null);
                if (ori) {
                    Width = imageWidth;
                    Height = imageHeight;
                } else {
                    Width = imageWidth < Width ? imageWidth : Width;
                    Height = imageHeight < Height ? imageHeight : Height;
                    float imageRatio = ((float) imageWidth / (float) imageHeight);
                    float framemageratio = ((float) Width / (float) Height);
                    if (imageRatio > framemageratio) {
                        float value = Width / imageRatio;
                        Height = (int) value;

                    } else {
                        float value = Height * imageRatio;
                        Width = (int) value;
                    }
                }
            }

//			BufferedImage resizedImage = this.scaleImage(sourceImage, Width,
//					Height, typeRGB);
//			ImageIO.write(resizedImage, type, new File(destPath+ext));
            BufferedImage resizedImage = createImage(sourcePath, Width, Height);
            ImageIO.write(resizedImage, "PNG", new File(destPath + ext));
        } catch (Exception e) {
            Logger.getInstance(FileUploadHandler.class).error(e, e);
        }
    }

    private BufferedImage scaleImage(Image sourceImage, int width, int height, int typeRGB) {
        ImageFilter filter = new ReplicateScaleFilter(width, height);
        ImageProducer producer = new FilteredImageSource(sourceImage.getSource(), filter);
        Image resizedImage = Toolkit.getDefaultToolkit().createImage(producer);

        return this.toBufferedImage(resizedImage, typeRGB);
    }

    private BufferedImage toBufferedImage(Image image, int type) {
        image = new ImageIcon(image).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), type);
        Graphics g = bufferedImage.createGraphics();
        if (type == BufferedImage.TYPE_INT_RGB) {
            g.setColor(Color.white);
            g.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
        }
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }
}
