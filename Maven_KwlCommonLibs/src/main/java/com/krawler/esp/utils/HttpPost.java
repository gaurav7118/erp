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
package com.krawler.esp.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class HttpPost {

    public String invoke(String soapMessage) {
        try {
            int statusCode = -1;
            String mUri = "http://localhost:7070/service/soap/";
            // the content-type charset will determine encoding used
            // when we set the request body
            PostMethod method = new PostMethod(mUri);
            // method.setRequestHeader("Content-type",
            // getSoapProtocol().getContentType());
            method.setRequestBody(soapMessage);
            method.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_AUTO);

            // if (getSoapProtocol().hasSOAPActionHeader())
            // method.setRequestHeader("SOAPAction", mUri);

            // execute the method.
            HttpClient mClient = new HttpClient();
            statusCode = mClient.executeMethod(method);

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Release the connection.
            method.releaseConnection();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary
            // data

            String responseStr = toString(responseBody);
            return responseStr;
        } catch (IOException ex) {
            return ex.toString();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    public static String toString(byte[] message)
            throws java.io.UnsupportedEncodingException {
        if (message == null || message.length == 0) {
            return "";
        } else if (message[message.length - 1] == '\0') {
            return new String(message, 0, message.length - 1, getCharset());
        } else {
            return new String(message, getCharset());
        }
    }

    public static String getCharset() {
        return "UTF-8";
    }

    public static void main(String args[]) {
        String d = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Header>"
                + "<context xmlns=\"urn:zimbra\">"
                + "<format type=\"js\"/>"
                + "</context>"
                + "</soap:Header>"
                + "<soap:Body>"
                + "<AuthRequest xmlns=\"urn:zimbraAccount\">"
                + "<account by=\"name\">user1</account>"
                + "<password>test123</password>"
                + "</AuthRequest>"
                + "</soap:Body>" + "</soap:Envelope>";
        // GetResponse(d);
        HttpPost p = new HttpPost();
        p.invoke(d);
    }

    public static String GetResponse(String postdata) {
        try {
            String s = URLEncoder.encode(postdata, "UTF-8");
            // URL u = new URL("http://google.com");
            URL u = new URL("http://localhost:7070/service/soap/");
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setAllowUserInteraction(false);

            DataOutputStream dstream = new DataOutputStream(uc.getOutputStream());

            // The POST line
            dstream.writeBytes(s);
            dstream.close();

            // Read Response
            InputStream in = uc.getInputStream();
            int x;
            while ((x = in.read()) != -1) {
                System.out.write(x);
            }
            in.close();

            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuffer buf = new StringBuffer();
            String line;
            while ((line = r.readLine()) != null) {
                buf.append(line);
            }
            return buf.toString();
        } catch (Exception e) {
            // throw e;
            return e.toString();
        }
    }
}