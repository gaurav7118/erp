<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.LineNumberReader"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.Reader"%>


<%
    
    
    out.print("Script Execution Started<br/><br/>");
    
    Connection conn = null;

    try {

        String delimiter = ";";
        boolean fullLineDelimiter = false;

        /*
        
         Code to fetch Request parameters
        
         */
        String serverip = request.getParameter("serverip");
        String port = request.getParameter("port");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String filename = request.getParameter("filename");
        String path = request.getParameter("path");
        filename = filename.trim();

        /*
        
         Code to check if all required data is present in request
         URL : dbchangesJSP.jsp?serverip=192.168.0.70&port=3306&dbname=mrp&username=krawler&password=krawler&filename=dbchanges
        
         */
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }

        /*
        
         Code to create database connection
        
         */
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        /*
         Set connection auto commit FALSE.
         Because, during sql exception, earlier database changes will not be committed in DB
         */
        conn.setAutoCommit(false);

        /*
        
         Check if file already executed on DB
         If not then make entry
         */
        Statement statementExists = conn.createStatement();
        statementExists.execute("select * from deployment_db_scripts where scriptname = '" + filename + "' ");
        ResultSet rsExists = statementExists.getResultSet();
        if (rsExists.next()) {
            throw new Exception("Dbchanges script with name " + filename + " has been already executed on database.");
        } else {
            Statement statementInsert = conn.createStatement();
            statementInsert.execute("insert into deployment_db_scripts(scriptname, status) values('" + filename + "', 0) ");
            statementInsert.close();
        }

        rsExists.close();
        statementExists.close();

        /*
        
         Code to read file from specific location
        
         */
        
        
        
        Reader reader = new FileReader(path + filename + ".sql");

        /*
         Count to get no. of DBchanges executed
         */
        int count = 1;

        /*
        
         Code to execute sql file
        
         */
        StringBuffer command = null;
        try {

            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            
            /*
             Loop to fetch line wise records and to build query using delimeter as ";"
             Execute query sequencially, if any error occurs, exception will be thrown and script execution will be terminated.
             DB Changes are committed at the batch of 250 records per batch.
             If any error occurs, need to execute queries agin from the first query of that batch
            
             */

            while ((line = lineReader.readLine()) != null) {

                if ((count % 250) == 0) {
                    conn.commit();
                    out.print("<br/><br/>Connection committed. If any exception occurs after this line, execute queries again from this line<br/><br/>");
                }
                if (command == null) {
                    command = new StringBuffer();
                }

                /*
                 If line contains "--", "//", "#" symbol then skip that particular line
                
                 */
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("#")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(delimiter)
                        || fullLineDelimiter
                        && trimmedLine.equals(delimiter)) {
                    command.append(line.substring(0, line
                            .lastIndexOf(delimiter)));
                    command.append(" ");

                    Statement statement = conn.createStatement();

                    out.print("<br/>" + count + ". " + command + "<br/>");

                    boolean hasResults = false;

                    if (false) {
                        hasResults = statement.execute(command.toString());
                    } else {

                        try {

                            /*
                            
                             Statement to execute query on database.
                            
                             */
                            hasResults = statement.execute(command.toString());
                            
                            
                            /*
                            
                            Alter and Create queries auto commit connection, so providing line using following message upto which DB changes got executed.
                            
                            */
                            String commandString = command.toString().trim().toLowerCase();
                            
                            if (commandString.startsWith("alter") || commandString.startsWith("create")) {
                                conn.commit();
                                out.print("<br/><br/><b>Connection committed. If any exception occurs after this line, execute queries again from this line</b><br/><br/>");
                            }

                        } catch (SQLException e) {
                            /*
                             Script execution will be stopped after this exception.
                            
                             */

                            e.fillInStackTrace();
                            out.println("Error executing: " + command + "<br/>");
                            out.print("Error executing: " + e.getMessage() + "<br/><br/>");
                            throw e;

                        }
                    }

                    count++;

                    /*
                    
                     For select type of queries, following is the code.
                     System will provide result set and accordingly records are printed in tabular form.
                    
                     */
                    ResultSet rs = statement.getResultSet();
                    if (hasResults && rs != null) {
                        ResultSetMetaData md = rs.getMetaData();
                        String tableString = "<br/><table border = 1 style='margin-left:50px;border-collapse: collapse;'>";
                        tableString += "<thead>";
                        tableString += "<tr>";
                        int cols = md.getColumnCount();
                        for (int i = 1; i <= cols; i++) {
                            String name = md.getColumnLabel(i);
                            //out.print(name + "\t");
                            tableString += "<th>" + name + "</th>";

                        }
                        tableString += "</tr>";
                        tableString += "</thead>";
                        tableString += "<tbody>";

                        out.print("");
                        while (rs.next()) {
                            tableString += "<tr>";
                            for (int i = 1; i <= cols; i++) {
                                String value = rs.getString(i);
                                tableString += "<td>" + value + "</td>";
                                //out.print(value + "\t");
                            }
                            tableString += "</tr>";
                            //out.print("");
                        }

                        tableString += "</tbody>";
                        tableString += "</table>";

                        out.println(tableString + "<br/>");

                    }

                    command = null;
                    try {

                        statement.close();
                        rs.close();
                    } catch (Exception e) {
                        
                    }

                } else {
                    command.append(line);
                    command.append(" ");
                }

            }

        } catch (SQLException e) {
            e.fillInStackTrace();
            out.print("Error executing: " + command + "<br/>");
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            out.print("Error executing: " + command + "<br/>");
            throw e;
        } catch (Exception e) {
            e.fillInStackTrace();
            out.print("Error executing: " + command + "<br/>");
            throw e;
        }

        /*
        
         Code to set flag in DB if script has been executed successfully.
        
         */
        Statement statementUpdate = conn.createStatement();
        statementUpdate.execute("update deployment_db_scripts set status = 1 where scriptname = '" + filename + "'");
        statementUpdate.close();

        conn.commit();

        reader.close();

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString() + "<br/>");
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

    out.print("<br/><br/>Script Execution Done");
%>