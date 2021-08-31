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
package com.krawler.common.util;

import com.krawler.common.service.ServiceException;
import com.krawler.esp.utils.ConfigReader;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.ReplicateScaleFilter;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Snap {

    public static File generateOutput(String filePath) throws ServiceException {
        File outputFile = null;
        try {
            String opFile = ConfigReader.getinstance().get("imageOutputFile");
            outputFile = new File(opFile);
//            outputFile = new File("E:\\temp\\test1.png");
            outputFile.deleteOnExit();
            JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setPage("file:" + filePath);
            final JFrame frame = new JFrame();
            frame.pack();
            try {
                Thread.sleep(5000);
            } catch (NumberFormatException nfe) {
            }
            frame.add(pane);
            frame.pack();
            Dimension prefSize = new Dimension(700, 800);
//            Dimension prefSize = pane.getPreferredSize();
            pane.setSize(prefSize);
            BufferedImage img = new BufferedImage(prefSize.width, prefSize.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D) img.getGraphics();
            SwingUtilities.paintComponent(g, pane, frame, 0, 0, prefSize.width, prefSize.height);
            ImageIO.write(img, "png", outputFile);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return outputFile;
    }

    public static boolean generateThumbnail(String filePath, String thumbnailPath) throws ServiceException {
        boolean flg = false;
        try {
            File original = generateOutput(filePath);
            resizeImage(original.getPath(), 280, 320, thumbnailPath, false);
            flg = true;
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return flg;
    }

    public static void resizeImage(String imagePath, int Width, int Height, String fName, boolean ori) throws ServiceException {
        try {
            Image sourceImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(imagePath)).getImage();
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
            BufferedImage resizedImage = scaleCompanyImage(sourceImage, Width, Height);
            ImageIO.write(resizedImage, "PNG", new File(fName));
            sourceImage.flush();
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    private static BufferedImage scaleCompanyImage(Image sourceImage, int width, int height) {
        ImageFilter filter = new ReplicateScaleFilter(width, height);
        ImageProducer producer = new FilteredImageSource(sourceImage.getSource(), filter);
        Image rImage = Toolkit.getDefaultToolkit().createImage(producer);
        return toBufferedCompanyImage(rImage);
    }

    private static BufferedImage toBufferedCompanyImage(Image image) {
        image = new ImageIcon(image).getImage();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }
}
