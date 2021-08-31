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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Props2Js {

    /**
     * This class converts
     * <code>ResourceBundle</code> and
     * <code>Properties</code> objects to native JavaScript. The class iterates
     * over the keys and generates a line of JavaScript for each value. For
     * example, if the properties file "/path/Messages.properties" contains the
     * following:
     *
     * <pre>
     * one = One
     * two : Two\
     * Two
     * three = Three\
     * 		Three\
     * 		Three
     * </pre>
     *
     * the generated JavaScript would look like this:
     *
     * <pre>
     * function Messages() {}
     * Messages.one = &quot;One&quot;;
     * Messages.two = &quot;TwoTwo&quot;;
     * Messages.three = &quot;ThreeThreeThree&quot;;
     * </pre>
     *
     */
    //
    // Constructors
    //
    private Props2Js() {
    }

    //
    // Public static functions
    //
    public static void convert(OutputStream ostream, ResourceBundle bundle,
            String classname) throws IOException {
        convert(ostream, new ResourceBundleMap(bundle), classname);
    }

    public static void convert(OutputStream ostream, Properties props,
            String classname) throws IOException {
        convert(ostream, new PropertiesMap(props), classname);
    }

    //
    // Private static functions
    //
    private static void convert(OutputStream ostream, StringMap map,
            String classname) {
        PrintStream out = ostream instanceof PrintStream ? (PrintStream) ostream
                : new PrintStream(ostream);
        convert(out, map, classname);
    }

    private static void convert(PrintStream out, StringMap map, String classname) {
        out.println("function " + classname + "(){}");
        out.println();
        for (String key : map.keySet()) {
            String value = map.getString(key);

            out.print(classname);
            out.print("[\"");
            Props2Js.printEscaped(out, key);
            out.print("\"] = \"");
            Props2Js.printEscaped(out, value);
            out.println("\";");
        }
    } // load(PrintStream,String)

    private static void printEscaped(PrintStream out, String s) {
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\t':
                    out.print("\\t");
                    break;
                case '\n':
                    out.print("\\n");
                    break;
                case '\r':
                    out.print("\\r");
                    break;
                case '\\':
                    out.print("\\\\");
                    break;
                case '"':
                    out.print("\\\"");
                    break;
                default: {
                    if (c < 32 || c > 127) {
                        String cs = Integer.toString(c, 16);
                        out.print("\\u");
                        int cslen = cs.length();
                        for (int j = cslen; j < 4; j++) {
                            out.print('0');
                        }
                        out.print(cs);
                    } else {
                        out.print(c);
                    }
                }
            }
        }
    } // printEscaped(PrintStream,String)

    //
    // Classes
    //
    protected abstract static class StringMap {
        // Public methods

        public abstract Set<String> keySet();

        public abstract String getString(String key);

        // Protected static methods
        protected static Set<String> enums2set(Enumeration enumerations) {
            Set<String> set = new TreeSet<String>();
            while (enumerations.hasMoreElements()) {
                String key = (String) enumerations.nextElement();
                set.add(key);
            }
            return set;
        }
    }

    protected static class ResourceBundleMap extends StringMap {
        // Data

        private ResourceBundle bundle;

        // Constructors
        public ResourceBundleMap(ResourceBundle bundle) {
            this.bundle = bundle;
        }

        // StringMap methods
        public Set<String> keySet() {
            return StringMap.enums2set(this.bundle.getKeys());
        }

        public String getString(String key) {
            return this.bundle.getString(key);
        }
    }

    protected static class PropertiesMap extends StringMap {
        // Data

        private Properties properties;

        // Constructors
        public PropertiesMap(Properties properties) {
            this.properties = properties;
        }

        // StringMap methods
        public Set<String> keySet() {
            return enums2set(this.properties.propertyNames());
        }

        public String getString(String key) {
            return this.properties.getProperty(key);
        }
    }

    //
    // MAIN
    //
    public static void main(String[] argv) throws Exception {

        /*
         * // data String basename = null; Locale locale = null; File ifile =
         * null; File ofile = null; String classname = null;
         *
         * // process arguments for (int i = 0; i < argv.length; i++) { String
         * arg = argv[i]; if (arg.startsWith("-")) { String option =
         * arg.substring(1); if (option.equals("b")) { basename = argv[++i];
         * continue; } if (option.equals("l")) { locale = getLocale(argv[++i]);
         * continue; } if (option.equals("i")) { ifile = new File(argv[++i]); if
         * (!ifile.exists() || !ifile.isFile()) { System.err.println("error:
         * invalid input file"); System.exit(1); } continue; } if
         * (option.equals("o")) { ofile = new File(argv[++i]); if
         * (ofile.exists() && !ofile.isFile()) { System.err.println("error:
         * invalid output file"); System.exit(1); } continue; } if
         * (option.equals("c")) { classname = argv[++i]; continue; } } //
         * unknown argument System.err.println("error: unknown argument (" + arg
         * + ")"); printHelp(); } if (locale == null && ifile == null) {
         * System.err.println("error: must specify -l or -i"); System.exit(1); }
         * if (locale != null && ifile != null) { System.err.println("error:
         * specify only -l or -i"); System.exit(1); } if (basename == null &&
         * locale != null) { System.err.println("error: must specify -b");
         * System.exit(1); }
         *
         * OutputStream out = ofile != null ? new FileOutputStream(ofile) :
         * System.out;
         *
         * // convert resource bundle if (locale != null) { ResourceBundle
         * bundle = ResourceBundle.getBundle(basename, locale);
         *
         * Props2Js.convert(out, bundle, classname); }
         *
         * // convert properties file else { InputStream in = new
         * FileInputStream(ifile); Properties props = new Properties();
         * props.load(in); in.close();
         *
         * Props2Js.convert(out, props, classname); }
         *
         * if (ofile != null) { out.close();
			}
         */
        java.util.Locale[] l = java.util.Locale.getAvailableLocales();

        System.out.print(l[2].getCountry());
        System.out.print(l[2].getLanguage());
        //System.out.print(l[84].getVariant());


    }

    private static Locale getLocale(String arg) {
        StringTokenizer tokenizer = new StringTokenizer(arg, "_");
        String language = tokenizer.hasMoreTokens() ? tokenizer.nextToken()
                : null;
        String country = tokenizer.hasMoreTokens() ? tokenizer.nextToken()
                : null;
        String variant = tokenizer.hasMoreTokens() ? tokenizer.nextToken()
                : null;
        if (language != null) {
            if (country != null) {
                if (variant != null) {
                    return new Locale(language, country, variant);
                }
                return new Locale(language, country);
            }
            return new Locale(language);
        }
        return null;
    }

    private static void printHelp() {
        System.err.println("usage: java " + Props2Js.class.getName()
                + " options");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -b basename  Resource bundle basename");
        System.err.println("  -l locale    Resource bundle locale");
        System.err.println("  -i filename  Properties input file");
        System.err.println("  -o filename  JavaScript output file");
        System.err.println("  -c classname JavaScript class to generate");
        System.err.println();
        System.err.println("notes:");
        System.err.println("  Options -b and -l must be used together.");
        System.err.println("  Either option -l or -i must be specified.");
        System.err.println("  If option -i not specified, writes to stdout.");
        System.exit(1);
    }
}
