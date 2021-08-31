<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.io.BufferedWriter"%>
<%@page import="java.io.FileWriter"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%

    try {
        FileReader reader = new FileReader("/home/krawler/Desktop/erp_access_02092016.log");
        BufferedReader bufferedReader = new BufferedReader(reader);

        FileWriter writer = new FileWriter("/home/krawler/Desktop/erp_access_02092016_1.log", true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);


        String line;

        Map<String, String> UniqueRecords = new HashMap<String, String>();

        Pattern MY_PATTERN = Pattern.compile("POST.*.do.*HTTP\\/1.1");

        while ((line = bufferedReader.readLine()) != null) {
            Matcher m = MY_PATTERN.matcher(line);
            while (m.find()) {
                String s = m.group(0);
                Pattern MY_PATTERN1 = Pattern.compile("(/[^/]*){2}/.*");
                Matcher m1 = MY_PATTERN1.matcher(s);
                while (m1.find()) {
                    String s1 = m1.group(0).replace(m1.group(1), "");
                    if (!UniqueRecords.containsKey(s1)) {
                        UniqueRecords.put(s1, s1);
                        bufferedWriter.write(s1);
                        bufferedWriter.newLine();
                    }
                }
            }

        }
        reader.close();
        bufferedWriter.close();
    } catch (Exception ex) {
        out.print(ex.getMessage());
    } finally {
    }

%>