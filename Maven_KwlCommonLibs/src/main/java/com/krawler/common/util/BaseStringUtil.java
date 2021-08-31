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

import static com.krawler.common.util.BaseStringUtil.abbreviate;
import static com.krawler.common.util.BaseStringUtil.addToMultiMap;
import static com.krawler.common.util.BaseStringUtil.getExtension;
import static com.krawler.common.util.BaseStringUtil.isNullOrEmpty;
import static com.krawler.common.util.BaseStringUtil.join;
import static com.krawler.common.util.BaseStringUtil.parseLine;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author schemers
 */
public class BaseStringUtil {

    private static final Log LOG = LogFactory.getLog(BaseStringUtil.class);

    // CRM - Specific Functions
    public static String sizeRenderer(String value) {
        Double size = Double.parseDouble(value);
        String text = "";
        Double val;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (size >= 1 && size < 1024) {
            text = size + " Bytes";
        } else if (size > 1024 && size < 1048576) {
            val = (size / 1024);
            text = decimalFormat.format(val);
            text += " KB";
        } else if (size > 1048576) {
            val = (size / 1048576);
            text = decimalFormat.format(val);
            text += " MB";
        }
        return text;
    }

    public static Boolean isLessthanDates(java.util.Date olddate, Date newdate) {
        boolean flag = false;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");
            Calendar olddt = Calendar.getInstance();
            olddt.setTime(sdf.parse(sdf.format(olddate)));

            Calendar newdt = Calendar.getInstance();
            newdt.setTime(sdf.parse(sdf.format(newdate)));
            flag = olddt.before(newdt);
        } catch (ParseException ex) {
            LOG.info("Can't parse the date in isLessthanDates() : ", ex);
        }
        return flag;
    }

    public static String filterQuery(ArrayList filter_names, String appendCase) {
        StringBuilder filterQuery = new StringBuilder();
        // String filterQuery = "";
        String oper = "";
        String op = "";
        for (int i = 0; i < filter_names.size(); i++) {
            if (filter_names.get(i).toString().length() >= 5) {
                op = filter_names.get(i).toString().substring(0, 5);
            }
            if (op.equals("ISNOT")) {
                oper = " is not ";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            } else if (op.equals("NOTIN")) {
                oper = " not in(" + i + ")";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            } else {
                if (filter_names.get(i).toString().length() >= 4) {
                    op = filter_names.get(i).toString().substring(0, 4);
                }
                if (op.equals("LIKE")) {
                    oper = " like ";
                    String opstr = filter_names.get(i).toString();
                    filter_names.set(i, opstr.substring(4, opstr.length()));
                } else {
                    op = filter_names.get(i).toString().substring(0, 2);
                    if (op.equals("<=")) {
                        oper = " <= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals(">=")) {
                        oper = " >= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals("IS")) {
                        oper = " is ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals("IN")) {
                        oper = " in (" + i + ") ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else {

                        op = filter_names.get(i).toString().substring(0, 1);
                        if (op.equals("!")) {
                            oper = " != ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else if (op.equals("<")) {
                            oper = " < ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else if (op.equals(">")) {
                            oper = " > ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else {
                            oper = " = ";
                        }
                    }
                }
            }

            if (i == 0) {
                // filterQuery += " where "+filter_names.get(i)+" = ? ";
                if (!op.equals("IN") && !op.equals("NOTIN")) {
                    filterQuery.append(" " + appendCase + " " + filter_names.get(i) + oper + " ? ");
                } else {
                    filterQuery.append(" " + appendCase + " " + filter_names.get(i) + oper);
                }
            } else {
                // filterQuery += " and "+filter_names.get(i)+" = ? ";
                if (!op.equals("IN") && !op.equals("NOTIN")) {
                    filterQuery.append(" and " + filter_names.get(i) + oper + " ? ");
                } else {
                    filterQuery.append(" and " + " " + filter_names.get(i) + oper);
                }
            }
        }
        return filterQuery.toString();
    }

    public static String orderQuery(ArrayList field_names, ArrayList field_order) {
        StringBuilder orderQuery = new StringBuilder();
        if (field_names != null) {
            for (int i = 0; i < field_names.size(); i++) {
                if (i == 0) {
                    orderQuery.append(" order by ");
                    orderQuery.append(" " + field_names.get(i) + " " + field_order.get(i));
                } else {
                    orderQuery.append(", " + field_names.get(i) + " " + field_order.get(i));
                }
            }
        }
        return orderQuery.toString();
    }

    public static String groupQuery(ArrayList field_names) {
        StringBuilder orderQuery = new StringBuilder();
        if (field_names != null) {
            for (int i = 0; i < field_names.size(); i++) {
                if (i == 0) {
                    orderQuery.append(" group by ");
                    orderQuery.append(" " + field_names.get(i));
                } else {
                    orderQuery.append(", " + field_names.get(i));
                }
            }
        }
        return orderQuery.toString();
    }

    /**
     * A user-friendly equal that handles one or both nulls easily
     *
     * @return
     */
    public static boolean equal(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2;
        }
        return s1.equals(s2);
    }

    public static String padString(String s1, int length, String pad_string, int pad_type) {
        String padStr = "";
        if (length < s1.length()) {
            padStr = s1;
        } else {
            int z = (length - s1.length()) / pad_string.length();
            for (int i = 0; i <= z; i++) {
                padStr += pad_string;
            }
            if (pad_type == 1) {
                padStr += s1;
                padStr = padStr.substring((padStr.length() - length));
            } else {
                padStr = s1 + padStr;
                padStr = padStr.substring(0, length);
            }
        }
        return padStr;
    }

    public static long hexadecimalToDecimal(String hex) throws NumberFormatException {
        long res = 0;
        if (hex.isEmpty()) {
            throw new NumberFormatException("Empty string is not a hexadecimal number");
        }
        for (int i = 0; i < hex.length(); i++) {
            char n = hex.charAt(hex.length() - (i + 1));
            int f = (int) n - 48;
            if (f > 9) {
                f = f - 7;
                if (f > 15) {
                    f = f - 32;
                }
            }
            if (f < 0 || f > 15) {
                throw new NumberFormatException("Not a hexadecimal number");
            } else {
                res += f * Math.round(Math.pow(2.0, (4 * i)));
            }
        }
        return res;
    }

    public static String stripControlCharacters(String raw) {
        if (raw == null) {
            return null;
        }
        int i;
        for (i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            // invalid control characters
            if (c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D) {
                break;
            }
            // byte-order markers and high/low surrogates
            if (c == 0xFFFE || c == 0xFFFF || (c > 0xD7FF && c < 0xE000)) {
                break;
            }
        }
        if (i >= raw.length()) {
            return raw;
        }
        StringBuilder sb = new StringBuilder(raw.substring(0, i));
        for (; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= 0x20 || c == 0x09 || c == 0x0A || c == 0x0D) {
                if (c != 0xFFFE && c != 0xFFFF && (c <= 0xD7FF || c >= 0xE000)) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static boolean isAsciiString(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            if ((c < 0x20 || c >= 0x7F) && c != '\r' && c != '\n' && c != '\t') {
                return false;
            }
        }
        return true;
    }

    /**
     * add the name/value mapping to the map. If an entry doesn't exist, value
     * remains a String. If an entry already exists as a String, convert to
     * String[] and add new value. If entry already exists as a String[], grow
     * array and add new value.
     *
     * @param result result map
     * @param name
     * @param value
     */
    public static void addToMultiMap(Map<String, Object> result, String name, String value) {
        Object currentValue = result.get(name);
        if (currentValue == null) {
            result.put(name, value);
        } else if (currentValue instanceof String) {
            result.put(name, new String[]{(String) currentValue, value});
        } else if (currentValue instanceof String[]) {
            String[] ov = (String[]) currentValue;
            String[] nv = new String[ov.length + 1];
            System.arraycopy(ov, 0, nv, 0, ov.length);
            nv[ov.length] = value;
            result.put(name, nv);
        }
    }

    /**
     * Convert an array of the form:
     *
     * a1 v1 a2 v2 a2 v3
     *
     * to a map of the form:
     *
     * a1 -> v1 a2 -> [v2, v3]
     */
    public static Map<String, Object> keyValueArrayToMultiMap(String[] args, int offset) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = offset; i < args.length; i += 2) {
            String n = args[i];
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("not enough arguments");
            }
            String v = args[i + 1];
            addToMultiMap(attrs, n, v);
        }
        return attrs;
    }
    private static final int TERM_WHITESPACE = 1;
    private static final int TERM_SINGLEQUOTE = 2;
    private static final int TERM_DBLQUOTE = 3;

    /**
     * open the specified file and return the first line in the file, without
     * the end of line character(s).
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readSingleLineFromFile(String file) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            return in.readLine();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * read a line from "in", using readLine(). A trailing '\\' on the line will
     * be treated as continuation and the next line will be read and appended to
     * the line, without the \\.
     *
     * @param in
     * @return complete line or null on end of file.
     * @throws IOException
     */
    public static String readLine(BufferedReader in) throws IOException {
        String line;
        StringBuilder sb = null;

        while ((line = in.readLine()) != null) {
            if (line.length() == 0) {
                break;
            } else if (line.charAt(line.length() - 1) == '\\') {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(line.substring(0, line.length() - 1));
            } else {
                break;
            }
        }

        if (line == null) {
            if (sb == null) {
                return null;
            } else {
                return sb.toString();
            }
        } else {
            if (sb == null) {
                return line;
            } else {
                sb.append(line);
                return sb.toString();
            }
        }
    }

    /**
     * split a line into array of Strings, using a shell-style syntax for
     * tokenizing words.
     *
     * @param line
     * @return
     */
    public static String[] parseLine(String line) {
        ArrayList<String> result = new ArrayList<String>();

        int i = 0;

        StringBuilder sb = new StringBuilder(32);
        int term = TERM_WHITESPACE;
        boolean inStr = false;

        scan:
        while (i < line.length()) {
            char ch = line.charAt(i++);
            boolean escapedTerm = false;

            if (ch == '\\' && i < line.length()) {
                ch = line.charAt(i++);
                switch (ch) {
                    case '\\':
                        break;
                    case 'n':
                        ch = '\n';
                        escapedTerm = true;
                        break;
                    case 't':
                        ch = '\t';
                        escapedTerm = true;
                        break;
                    case 'r':
                        ch = '\r';
                        escapedTerm = true;
                        break;
                    case '\'':
                        ch = '\'';
                        escapedTerm = true;
                        break;
                    case '"':
                        ch = '"';
                        escapedTerm = true;
                        break;
                    default:
                        escapedTerm = Character.isWhitespace(ch);
                        break;
                }
            }

            if (inStr) {
                if (!escapedTerm && ((term == TERM_WHITESPACE && Character.isWhitespace(ch)) || (term == TERM_SINGLEQUOTE && ch == '\'') || (term == TERM_DBLQUOTE && ch == '"'))) {
                    inStr = false;
                    result.add(sb.toString());
                    sb = new StringBuilder(32);
                    term = TERM_WHITESPACE;
                    continue scan;
                }
                sb.append(ch);
            } else {
                if (!escapedTerm) {
                    switch (ch) {
                        case '\'':
                            term = TERM_SINGLEQUOTE;
                            inStr = true;
                            continue scan;
                        case '"':
                            term = TERM_DBLQUOTE;
                            inStr = true;
                            continue scan;
                        default:
                            if (Character.isWhitespace(ch)) {
                                continue scan;
                            }
                            inStr = true;
                            sb.append(ch);
                            break;
                    }
                } else {
                    // we had an escaped terminator, start a new string
                    inStr = true;
                    sb.append(ch);
                }
            }
        }

        if (sb.length() > 0) {
            result.add(sb.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    private static void dump(String line) {
        String[] result = parseLine(line);
        System.out.println("line: " + line);
        for (int i = 0; i < result.length; i++) {
            System.out.println(i + ": (" + result[i] + ")");
        }
        System.out.println();
    }

    private static Date getUserTimeZoneDate(String userTimezone) { // use for unit testing of time zone related to creation date of modules
        // records
        Date date = new Date();
        System.out.println(date);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        // SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone(userTimezone)); // replace timezine
        // with user
        // timezone
        String datestr = sdf.format(date);
        System.out.println(datestr);
        String[] datearr = datestr.split(" ");

        // datearr[4] contains user time zone format which is replaced by space.
        // so that new date function use system time zone format which is
        // already set at user session creation time
        String newstr = datestr.replace(datearr[4], " ");

        Date newdate = new Date(newstr);
        System.out.println(newdate);
        return newdate;
    }
    // A pattern that matches the beginning of a string followed by ${KEY_NAME}
    // followed
    // by the end. There are three groups: the beginning, KEY_NAME and the end.
    // Pattern.DOTALL is required in case one of the values in the map has a
    // newline
    // in it.
    private static Pattern templatePattern = Pattern.compile("(.*)\\$\\{([^\\)]+)\\}(.*)", Pattern.DOTALL);

    /**
     * Substitutes all occurrences of the specified values into a template. Keys
     * for the values are specified in the template as
     * <code>${KEY_NAME}</code>.
     *
     * @param template the template
     * @param vars a
     * <code>Map</code> filled with keys and values. The keys must be
     * <code>String</code>s.
     * @return the template with substituted values
     */
    public static String fillTemplate(String template, Map vars) {
        if (template == null) {
            return null;
        }

        String line = template;
        Matcher matcher = templatePattern.matcher(line);

        // Substitute multiple variables per line
        while (matcher.matches()) {
            String key = matcher.group(2);
            Object value = vars.get(key);
            if (value == null) {
                LOG.info("fillTemplate(): could not find key '" + key + "'");
                value = "";
            }
            line = matcher.group(1) + value + matcher.group(3);
            matcher.reset(line);
        }
        return line;
    }

    /**
     * Joins an array of
     * <code>short</code>s, separated by a delimiter.
     */
    public static String join(String delimiter, short[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            buf.append(array[i]);
            if (i + 1 < array.length) {
                buf.append(delimiter);
            }
        }
        return buf.toString();
    }

    /**
     * Joins an array of objects, separated by a delimiter.
     */
    public static String join(String delimiter, Object[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            buf.append(array[i]);
            if (i + 1 < array.length) {
                buf.append(delimiter);
            }
        }
        return buf.toString();
    }

    public static <E> String join(String delimiter, Collection<E> col) {
        if (col == null) {
            return null;
        }
        Object[] array = new Object[col.size()];
        col.toArray(array);
        return join(delimiter, array);
    }

    /**
     * Returns the simple class name (the name after the last dot) from a
     * fully-qualified class name. Behavior is the same as {@link #getExtension}
     * .
     */
    public static String getSimpleClassName(String className) {
        return getExtension(className);
    }

    /**
     * Returns the simple class name (the name after the last dot) for the
     * specified object.
     */
    public static String getSimpleClassName(Object o) {
        if (o == null) {
            return null;
        }
        return getExtension(o.getClass().getName());
    }

    /**
     * Returns the extension portion of the given filename. <ul> <li>If
     * <code>filename</code> contains one or more dots, returns all characters
     * after the last dot.</li> <li>If
     * <code>filename</code> contains no dot, returns
     * <code>filename</code>.</li> <li>If
     * <code>filename</code> is
     * <code>null</code>, returns
     * <code>null</code>.</li> <li>If
     * <code>filename</code> ends with a dot, returns an empty
     * <code>String</code>.</li> </ul>
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return filename;
        }
        if (lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1, filename.length());
    }

    /**
     * Returns
     * <code>true</code> if the secified string is
     * <code>null</code> or its length is
     * <code>0</code>.
     */
    public static boolean isNullOrEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }
    private static final String[] JS_CHAR_ENCODINGS = {"\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007", "\\b", "\\t", "\\n", "\\u000B", "\\f", "\\r", "\\u000E", "\\u000F", "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015", "\\u0016", "\\u0017", "\\u0018", "\\u0019", "\\u001A", "\\u001B", "\\u001C", "\\u001D", "\\u001E", "\\u001F"};

    public static String jsEncode(Object obj) {
        if (obj == null) {
            return "";
        }
        String replacement, str = obj.toString();
        StringBuilder sb = null;
        int i, last, length = str.length();
        for (i = 0, last = -1; i < length; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\':
                    replacement = "\\\\";
                    break;
                case '"':
                    replacement = "\\\"";
                    break;
                case '\u2028':
                    replacement = "\\u2028";
                    break;
                case '\u2029':
                    replacement = "\\u2029";
                    break;
                default:
                    if (c >= ' ') {
                        continue;
                    }
                    replacement = JS_CHAR_ENCODINGS[c];
                    break;
            }
            if (sb == null) {
                sb = new StringBuilder(str.substring(0, i));
            } else {
                sb.append(str.substring(last, i));
            }
            sb.append(replacement);
            last = i + 1;
        }
        return (sb == null ? str : sb.append(str.substring(last, i)).toString());
    }

    public static String jsEncodeKey(String key) {
        return '"' + key + '"';
    }
    //
    // HTML methods
    //
    private static final Pattern PAT_AMP = Pattern.compile("&", Pattern.MULTILINE);
    private static final Pattern PAT_LT = Pattern.compile("<", Pattern.MULTILINE);
    private static final Pattern PAT_GT = Pattern.compile(">", Pattern.MULTILINE);
    private static final Pattern PAT_DBLQT = Pattern.compile("\"", Pattern.MULTILINE);

    /**
     * Escapes special characters with their HTML equivalents.
     */
    public static String escapeHtml(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        String s = replaceAll(text, PAT_AMP, "&amp;");
        s = replaceAll(s, PAT_LT, "&lt;");
        s = replaceAll(s, PAT_GT, "&gt;");
        s = replaceAll(s, PAT_DBLQT, "&quot;");
        return s;
    }

    private static String replaceAll(String text, Pattern pattern, String replace) {
        Matcher m = pattern.matcher(text);
        StringBuffer sb = null;
        while (m.find()) {
            if (sb == null) {
                sb = new StringBuffer();
            }
            m.appendReplacement(sb, replace);
        }
        if (sb != null) {
            m.appendTail(sb);
        }
        return sb == null ? text : sb.toString();
    }

    public static boolean stringCompareInLowercase(String strToCompareWith, String strTobeCompare) {
        return strToCompareWith.equalsIgnoreCase(strTobeCompare);

    }

    public static String getMySearchString(String searchString, String appendCase, String[] searchParams) {
        StringBuilder myResult = new StringBuilder();

        if (!isNullOrEmpty(searchString)) {
            for (int i = 0; i < searchParams.length; i++) {
                myResult.append(" ");
                if (i == 0) {
                    myResult.append(appendCase);
                    myResult.append(" (( ");
                } else {
                    myResult.append(" ( ");
                }
                myResult.append(searchParams[i] + " like ? or " + searchParams[i] + " like ?");
                if (i + 1 < searchParams.length) {
                    myResult.append(") ");
                } else {
                    myResult.append(")) ");
                }
                if (i + 1 < searchParams.length) {
                    myResult.append(" or ");
                }
            }
        } else {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static String serverHTMLStripper(String stripTags) throws IllegalStateException, IndexOutOfBoundsException {
        Pattern p = Pattern.compile("<[^>]*>");
        Matcher m = p.matcher(stripTags);
        StringBuffer sb = new StringBuffer();
        if (!isNullOrEmpty(stripTags)) {
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            stripTags = sb.toString();
        }
        return stripTags.trim();
    }

    public static String checkForNull(String rsString) {
        return rsString != null ? rsString : "";
    }

    public static boolean serverValidateEmail(String email) {
        boolean result = true;
        String emailCheck = "^[\\w_\\-%\\.]+@[\\w_\\-%\\.]+\\.[a-zA-Z]{2,6}$";
        if (!isNullOrEmpty(email)) {
            result = email.matches(emailCheck);
        }
        return result;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getSearchString(String searchString, String appendCase, String[] searchParams) {
        StringBuilder myResult = new StringBuilder();

        if (!isNullOrEmpty(searchString)) {
            for (int i = 0; i < searchParams.length; i++) {
                myResult.append(" ");
                if (i == 0) {
                    myResult.append(appendCase);
                    myResult.append(" (( ");
                } else {
                    myResult.append(" ( ");
                }
                myResult.append(searchParams[i] + " like ? or " + searchParams[i] + " like ?");
                if (i + 1 < searchParams.length) {
                    myResult.append(") ");
                } else {
                    myResult.append(")) ");
                }
                if (i + 1 < searchParams.length) {
                    myResult.append(" or ");
                }
            }
        } else {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    public static String abbreviate(String str, int offset, int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if ((str.length() - offset) < (maxWidth - 3)) {
            offset = str.length() - (maxWidth - 3);
        }
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if ((offset + (maxWidth - 3)) < str.length()) {
            return "..." + abbreviate(str.substring(offset), maxWidth - 3);
        }
        return "..." + str.substring(str.length() - (maxWidth - 3));
    }

    public static String makeExternalRequest(String urlstr, String postdata) {
        String result = "";
        try {
            URL url = new URL(urlstr);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                DataOutputStream d = new DataOutputStream(conn.getOutputStream());
                String data = postdata;
                OutputStreamWriter ow = new OutputStreamWriter(d);
                ow.write(data);
                ow.close();
                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder stringbufff = new StringBuilder();
                while ((inputLine = input.readLine()) != null) {
                    stringbufff.append(inputLine);
                }
                result = stringbufff.toString();
                input.close();
            } catch (IOException ex) {
                System.out.print(ex);
            }

        } catch (MalformedURLException ex) {
            System.out.print(ex);
        } finally {
            return result;
        }
    }

    public static String convertToTwoDecimal(double value) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        return df.format(value);
    }

    private static String ellipsisString(String string, int len) {
        String name = string;
        if (name.length() > len && (!name.contains(" "))) {
            name = name.substring(0, (len - 3));
            name += "...";
        }
        return name;
    }

    public static Date stdNewDate(String tzDiff) {
        Calendar cal = Calendar.getInstance();
        Calendar cal1 = new GregorianCalendar(TimeZone.getTimeZone("GMT" + tzDiff));
        try {
            Date dt = new Date();
            cal1.setTimeInMillis(dt.getTime());
            cal.set(Calendar.YEAR, cal1.get(Calendar.YEAR));
            cal.set(Calendar.MONTH, cal1.get(Calendar.MONTH));
            cal.set(Calendar.DAY_OF_MONTH, cal1.get(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, cal1.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, cal1.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, cal1.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, cal1.get(Calendar.MILLISECOND));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return cal.getTime();
        }
    }
}
