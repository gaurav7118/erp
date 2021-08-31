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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

public class Props2jsController extends AbstractController {

    private static final String COMPRESSED_EXT = ".zgz";
    MessageSource messageSource;
    private Map<Locale, Map<String, byte[]>> buffers = new HashMap<Locale, Map<String, byte[]>>();

    private synchronized byte[] getBuffer(Locale locale, String uri) throws IOException {
        // get locale buffers
        Map<String, byte[]> localeBuffers = buffers.get(locale);
        if (localeBuffers == null) {
            localeBuffers = new HashMap<String, byte[]>();
            buffers.put(locale, localeBuffers);
        }

        // get byte buffer
        byte[] buffer = localeBuffers.get(uri);
        if (buffer == null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream out = uri.endsWith(COMPRESSED_EXT) ? new PrintStream(new GZIPOutputStream(bos)) : new PrintStream(bos);
            out.println("// Locale: " + locale);

            // This gets the base directory for the resource bundle
            // basename. For example, if the URI is:
            //
            // .../msgs/I18nMsg.js
            //
            // then the basedir is "/msgs/" and if the URI is:
            //
            // .../keys/ZmKeys.js
            //
            // then the basedir is "/keys/".
            //
            // NOTE: The <url-pattern>s in the web.xml file restricts
            // which URLs map to this servlet so there's no risk
            // that the basedir will be other than what we expect.
            int lastSlash = uri.lastIndexOf('/');
            int prevSlash = uri.substring(0, lastSlash).lastIndexOf('/');
            String basedir = uri.substring(prevSlash, lastSlash + 1);

            String filenames = uri.substring(uri.lastIndexOf('/') + 1);
            String classnames = filenames.substring(0, filenames.indexOf('.'));
            StringTokenizer tokenizer = new StringTokenizer(classnames, ",");
            while (tokenizer.hasMoreTokens()) {
                String classname = tokenizer.nextToken();
                load(out, locale, basedir, classname);
            }

            // save buffer
            out.close();
            buffer = bos.toByteArray();
            localeBuffers.put(uri, buffer);
        }

        return buffer;
    } // getBuffer(Locale,String):byte[]

    private void load(PrintStream out, Locale locale, String basedir,
            String classname) {
        String basename = basedir + classname;

        out.println();
        out.println("// Basename: " + basename);

        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(basename, locale);
            Props2Js.convert(out, bundle, classname);
        } catch (MissingResourceException e) {
            out.println("// resource bundle not found");
            Logger.getLogger(Props2jsController.class.getName()).log(Level.SEVERE, null, "unable to load resource bundle: " + basename);
//			logger.error("unable to load resource bundle: " + basename);
        } catch (IOException e) {
            out.println("// error: " + e.getMessage());
        }
    } // load(PrintStream,String)

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();

        OutputStream out = response.getOutputStream();
        response.setContentType("text/javascript");
        byte[] buffer = getBuffer(RequestContextUtils.getLocale(request), uri);
        out.write(buffer);
        out.close();
        return null;
    }
}