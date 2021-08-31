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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class ConfigReader {

    private static final ConfigReader DEFAULT = new ConfigReader();

    /**
     * Return the default configuration.
     */
    public static ConfigReader getinstance() {
        return DEFAULT;
    }
    private List resourceNames = new ArrayList();
    private Properties properties;
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public ConfigReader() {
        resourceNames.add("Krawler-web-app.xml");
//        resourceNames.add(new File("/home/krawler/AccountingProjects/Financials2/Maven_Accounting/src/main/resources/Krawler-web-app.xml")); // Resource entry for JUnit
    }

    public synchronized void addConfResource(String name) {
        addConfResourceInternal(name);
    }

    public synchronized void addConfResource(File file) {
        addConfResourceInternal(file);
    }

    private synchronized void addConfResourceInternal(Object name) {
        resourceNames.add(resourceNames.size() - 1, name); // add second to
        // last
        properties = null; // trigger reload
    }

    /**
     * Returns the value of the
     * <code>name</code> property, or null if no such property exists.
     */
    public String get(String name) {
        return getProps().getProperty(name);
    }

    /**
     * Sets the value of the
     * <code>name</code> property.
     */
    public void set(String name, Object value) {
        getProps().setProperty(name, value.toString());
    }

    /**
     * Returns the value of the
     * <code>name</code> property. If no such property exists, then
     * <code>defaultValue</code> is returned.
     */
    public String get(String name, String defaultValue) {
        return getProps().getProperty(name, defaultValue);
    }

    /**
     * Returns the value of the
     * <code>name</code> property as an integer. If no such property is
     * specified, or if the specified value is not a valid integer, then
     * <code>defaultValue</code> is returned.
     */
    public int getInt(String name, int defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Sets the value of the
     * <code>name</code> property to an integer.
     */
    public void setInt(String name, int value) {
        set(name, Integer.toString(value));
    }

    /**
     * Returns the value of the
     * <code>name</code> property as a long. If no such property is specified,
     * or if the specified value is not a valid long, then
     * <code>defaultValue</code> is returned.
     */
    public long getLong(String name, long defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the
     * <code>name</code> property as a float. If no such property is specified,
     * or if the specified value is not a valid float, then
     * <code>defaultValue</code> is returned.
     */
    public float getFloat(String name, float defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(valueString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the
     * <code>name</code> property as an boolean. If no such property is
     * specified, or if the specified value is not a valid boolean, then
     * <code>defaultValue</code> is returned. Valid boolean values are "true"
     * and "false".
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String valueString = get(name);
        if ("true".equals(valueString)) {
            return true;
        } else if ("false".equals(valueString)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the
     * <code>name</code> property as an array of strings. If no such property is
     * specified, then
     * <code>null</code> is returned. Values are whitespace or comma delimted.
     */
    public String[] getStrings(String name) {
        String valueString = get(name);
        if (valueString == null) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(valueString,
                ", \t\n\r\f");
        List values = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken());
        }
        return (String[]) values.toArray(new String[values.size()]);
    }

    /**
     * Returns the value of the
     * <code>name</code> property as a Class. If no such property is specified,
     * then
     * <code>defaultValue</code> is returned.
     */
    public Class getClass(String name, Class defaultValue) {
        String valueString = get(name);
        if (valueString == null) {
            return defaultValue;
        }
        try {
            return Class.forName(valueString);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of the
     * <code>name</code> property as a Class. If no such property is specified,
     * then
     * <code>defaultValue</code> is returned. An error is thrown if the returned
     * class does not implement the named interface.
     */
    public Class getClass(String propertyName, Class defaultValue, Class xface) {
        try {
            Class theClass = getClass(propertyName, defaultValue);
            if (theClass != null && !xface.isAssignableFrom(theClass)) {
                throw new RuntimeException(theClass + " not " + xface.getName());
            }
            return theClass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of the
     * <code>name</code> property to the name of a class. First checks that the
     * class implements the named interface.
     */
    public void setClass(String propertyName, Class theClass, Class xface) {
        if (!xface.isAssignableFrom(theClass)) {
            throw new RuntimeException(theClass + " not " + xface.getName());
        }
        set(propertyName, theClass.getName());
    }

    /**
     * Returns the URL for the named resource.
     */
    public URL getResource(String name) {
        return classLoader.getResource(name);
    }

    /**
     * Returns an input stream attached to the configuration resource with the
     * given
     * <code>name</code>.
     */
    public InputStream getConfResourceAsInputStream(String name) {
        try {
            URL url = getResource(name);

            if (url == null) {
                System.err.print(url + "Not Found");
                return null;
            } else {
                System.err.print("found resource " + name + " at " + url);
            }

            return url.openStream();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a reader attached to the configuration resource with the given
     * <code>name</code>.
     */
    public Reader getConfResourceAsReader(String name) {
        try {
            URL url = getResource(name);

            if (url == null) {
                System.err.print(name + " not found");
                return null;
            } else {
                System.err.print("found resource " + name + " at " + url);
            }

            return new InputStreamReader(url.openStream());
        } catch (Exception e) {
            return null;
        }
    }

    private synchronized Properties getProps() {
        if (properties == null) {
            Properties defaults = new Properties();
            Properties newProps = new Properties(defaults);
            ListIterator i = resourceNames.listIterator();
            while (i.hasNext()) {
                if (i.nextIndex() == 0) { // load defaults
                    loadResource(defaults, i.next(), false);
                } else if (i.nextIndex() == resourceNames.size() - 1) { // load
                    // site
                    loadResource(newProps, i.next(), true);
                } else { // load intermediate
                    loadResource(newProps, i.next(), false);
                }
            }
            properties = newProps;
        }
        return properties;
    }

    private void loadResource(Properties properties, Object name,
            boolean quietFail) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = null;

            if (name instanceof String) { // a CLASSPATH resource
                URL url = getResource((String) name);
                if (url != null) {
                    System.err.print("parsing " + url);
                    doc = builder.parse(url.toString());
                }
            } else if (name instanceof File) { // a file resource
                File file = (File) name;
                if (file.exists()) {
                    System.err.print("parsing " + file);
                    doc = builder.parse(file);
                }
            }

            if (doc == null) {
                if (quietFail) {
                    return;
                }
                throw new RuntimeException(name + " not found");
            }

            Element root = doc.getDocumentElement();
            if (!"krawler-conf".equals(root.getTagName())) {
                System.err.print("bad conf file: top-level element not <krawler-conf>");
            }
            NodeList props = root.getChildNodes();
            for (int i = 0; i < props.getLength(); i++) {
                Node propNode = props.item(i);
                if (!(propNode instanceof Element)) {
                    continue;
                }
                Element prop = (Element) propNode;
                if (!"property".equals(prop.getTagName())) {
                    System.err.print("bad conf file: element not <property>");
                }
                NodeList fields = prop.getChildNodes();
                String attr = null;
                String value = null;
                for (int j = 0; j < fields.getLength(); j++) {
                    Node fieldNode = fields.item(j);
                    if (!(fieldNode instanceof Element)) {
                        continue;
                    }
                    Element field = (Element) fieldNode;
                    if ("name".equals(field.getTagName())) {
                        attr = ((Text) field.getFirstChild()).getData();
                    }
                    if ("value".equals(field.getTagName())
                            && field.hasChildNodes()) {
                        value = ((Text) field.getFirstChild()).getData();
                    }
                }
                if (attr != null && value != null) {
                    properties.setProperty(attr, value);
                }
            }

        } catch (Exception e) {
            System.err.print("error parsing conf file: " + e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Writes non-default properties in this configuration.
     */
    public void write(OutputStream out) throws IOException {
        Properties properties = getProps();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element conf = doc.createElement("krawler-conf");
            doc.appendChild(conf);
            conf.appendChild(doc.createTextNode("\n"));
            for (Enumeration e = properties.keys(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                String value = (String) properties.get(name);

                Element propNode = doc.createElement("property");
                conf.appendChild(propNode);

                Element nameNode = doc.createElement("name");
                nameNode.appendChild(doc.createTextNode(name));
                propNode.appendChild(nameNode);

                Element valueNode = doc.createElement("value");
                valueNode.appendChild(doc.createTextNode(value));
                propNode.appendChild(valueNode);

                conf.appendChild(doc.createTextNode("\n"));
            }

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For debugging. List non-default properties to the terminal and exit.
     */
    public static void main(String[] args) throws Exception {
        getinstance().write(System.out);
    }
}
