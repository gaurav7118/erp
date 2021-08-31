<%-- 
    Document   : CheckLineLevelRecordWhereInvoiceTermIsUsed
    Created on : Jul 24, 2018, 1:00:58 PM
    Author     : krawler
--%>

<%@page import="java.io.IOException"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.*"%>

<%!
    public int checkAvailableTermTaxCountAtLineLevel(String termId, Connection conn, List taxList) throws Exception {
        int availableAtLineLevel = 0;

        String taxTermMappingQuery = "SELECT ttm.tax FROM taxtermsmapping ttm WHERE ttm.invoicetermssales='" + termId + "'";
        PreparedStatement psTaxTermMapping = conn.prepareStatement(taxTermMappingQuery);
        ResultSet rsTaxTermMapping = psTaxTermMapping.executeQuery();
        if (rsTaxTermMapping != null) {
            while (rsTaxTermMapping.next()) {
                if (taxList.contains(rsTaxTermMapping.getString("tax"))) {
                    availableAtLineLevel++;
                }
            }
        }
        
        return availableAtLineLevel;
    }
%>

<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement psGlobal = null, psDetail = null, psTerm = null;
        ResultSet rsGlobal = null, rsDetail = null, rsTerm = null;

        String queryCompany = "", query = "", detailQuery = "", termQuery = "";
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            queryCompany = "select companyid, currency,subdomain from company where subdomain=?";
            ps = conn.prepareStatement(queryCompany);
            ps.setString(1, subDomain);
            rs = ps.executeQuery();
        } else {
            queryCompany = "select companyid,currency, subdomain from company";
            ps = conn.prepareStatement(queryCompany);
            rs = ps.executeQuery();
        }
        String companyid, subdomain, currencyid;
        if (rs != null) {
%>

<div align="center">    
    <table border="1">
        <tr><th>Sub domain</th><th>Module Name</th><th>Transaction Number</th></tr>
        <%
            while (rs.next()) {
                companyid = rs.getString("companyid");
                subdomain = rs.getString("subdomain");
                currencyid = rs.getString("currency");

                //Check Sales Invoice Transaction
                try {
                    query = "SELECT inv.id,inv.invoicenumber FROM invoice inv INNER JOIN invoicetermsmap itm on itm.invoice=inv.id WHERE inv.company='" + companyid + "' and inv.applytaxtoterms='T' group by inv.id ";
                    psGlobal = conn.prepareStatement(query);
                    rsGlobal = psGlobal.executeQuery();
                    if (rsGlobal != null) {
                        while (rsGlobal.next()) {
                            String invId = rsGlobal.getString("id");
                            String invoicenumber = rsGlobal.getString("invoicenumber");
                            List<String> taxList = new ArrayList();
                            int availableAtLineLevel = 0;

                            detailQuery = "SELECT invd.tax FROM invoicedetails invd WHERE invd.invoice ='" + invId + "'";
                            psDetail = conn.prepareStatement(detailQuery);
                            rsDetail = psDetail.executeQuery();
                            if (rsDetail != null) {
                                while (rsDetail.next()) {
                                    taxList.add(rsDetail.getString("tax"));
                                }
                            }
                            termQuery = "SELECT itm.term FROM invoicetermsmap itm WHERE itm.invoice='" + invId + "'";
                            psTerm = conn.prepareStatement(termQuery);
                            rsTerm = psTerm.executeQuery();
                            if (rsTerm != null) {
                                while (rsTerm.next()) {
                                    availableAtLineLevel = 0;
                                    String termid = rsTerm.getString("term");
                                    try {
                                        availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                                    } catch (Exception ex) {
                                        out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Invoice - " + ex.getMessage());
                                    }

                                    if (availableAtLineLevel >= 2) {
                                        break;
                                    }
                                }
                            }
                            if (availableAtLineLevel >= 2) {
        %>                                
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Sales Invoice</td>
            <td><% out.println(invoicenumber);%></td>
        </tr>
        <%
                            }
                        }
                    }

            //Check Purchase Invoice Transaction
            query = "SELECT inv.id,inv.grnumber FROM goodsreceipt inv INNER JOIN receipttermsmap itm on itm.goodsreceipt=inv.id WHERE inv.company='" + companyid + "' and inv.applytaxtoterms='T' group by inv.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String piId = rsGlobal.getString("id");
                    String grnumber = rsGlobal.getString("grnumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT invd.tax FROM grdetails invd WHERE invd.goodsreceipt ='" + piId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT itm.term FROM receipttermsmap itm WHERE itm.goodsreceipt='" + piId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Purchase Invoice -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Purchase Invoice</td>
            <td><% out.println(grnumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Sales Order Transaction
            query = "SELECT so.id,so.sonumber FROM salesorder so INNER JOIN salesordertermmap stm on stm.salesorder=so.id WHERE so.company='" + companyid + "' and so.applytaxtoterms='T' group by so.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String soId = rsGlobal.getString("id");
                    String sonumber = rsGlobal.getString("sonumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT sod.tax FROM sodetails sod WHERE sod.salesorder ='" + soId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT stm.term FROM salesordertermmap stm WHERE stm.salesorder='" + soId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Sales Order -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Sales Order</td>
            <td><% out.println(sonumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Purchase Order Transaction
            query = "SELECT po.id,po.ponumber FROM purchaseorder po INNER JOIN purchaseordertermmap ptm on ptm.purchaseorder=po.id WHERE po.company='" + companyid + "' and po.applytaxtoterms='T' group by po.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String poId = rsGlobal.getString("id");
                    String ponumber = rsGlobal.getString("ponumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT sod.tax FROM podetails sod WHERE sod.purchaseorder ='" + poId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT ptm.term FROM purchaseordertermmap ptm WHERE ptm.purchaseorder='" + poId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Purchase Order -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Purchase Order</td>
            <td><% out.println(ponumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Customer Quotation Transaction
            query = "SELECT q.id,q.quotationnumber FROM quotation q INNER JOIN quotationtermmap qtm on qtm.quotation=q.id WHERE q.company='" + companyid + "' and q.applytaxtoterms='T' group by q.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String quotationId = rsGlobal.getString("id");
                    String quotationnumber = rsGlobal.getString("quotationnumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT qod.tax FROM quotationdetails qod WHERE qod.quotation ='" + quotationId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT qtm.term FROM quotationtermmap qtm WHERE qtm.quotation='" + quotationId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Customer Quotation -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Customer Quotation</td>
            <td><% out.println(quotationnumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Vendor Quotation Transaction
            query = "SELECT v.id,v.quotationnumber FROM vendorquotation v INNER JOIN vendorquotationtermmap vtm on vtm.vendorquotation=v.id WHERE v.company='" + companyid + "' and v.applytaxtoterms='T' group by v.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String quotationId = rsGlobal.getString("id");
                    String quotationnumber = rsGlobal.getString("quotationnumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT qod.tax FROM vendorquotationdetails qod WHERE qod.vendorquotation ='" + quotationId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT vtm.term FROM vendorquotationtermmap vtm WHERE vtm.vendorquotation='" + quotationId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Vendor Quotaion - " + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Vendor Quotation</td>
            <td><% out.println(quotationnumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Goods Receipt Order Transaction
            query = "SELECT gro.id,gro.gronumber FROM grorder gro INNER JOIN goodsreceiptordertermmap grotm on grotm.goodsreceiptorder=gro.id WHERE gro.company='" + companyid + "' and gro.applytaxtoterms='T' group by gro.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String quotationId = rsGlobal.getString("id");
                    String gronumber = rsGlobal.getString("gronumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT grod.tax FROM grodetails grod WHERE grod.grorder ='" + quotationId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT grotm.term FROM goodsreceiptordertermmap grotm WHERE grotm.goodsreceiptorder='" + quotationId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Goods Receipt Order -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Goods Receipt Order</td>
            <td><% out.println(gronumber);%></td>
        </tr>
        <%
                    }
                }
            }

            //Check Delivery Order Transaction
            query = "SELECT do.id,do.donumber FROM deliveryorder do INNER JOIN deliveryordertermmap dotm on dotm.deliveryorder=do.id WHERE do.company='" + companyid + "' and do.applytaxtoterms='T' group by do.id ";
            psGlobal = conn.prepareStatement(query);
            rsGlobal = psGlobal.executeQuery();
            if (rsGlobal != null) {
                while (rsGlobal.next()) {
                    String quotationId = rsGlobal.getString("id");
                    String donumber = rsGlobal.getString("donumber");
                    List<String> taxList = new ArrayList();
                    int availableAtLineLevel = 0;

                    detailQuery = "SELECT dod.tax FROM dodetails dod WHERE dod.deliveryorder ='" + quotationId + "'";
                    psDetail = conn.prepareStatement(detailQuery);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail != null) {
                        while (rsDetail.next()) {
                            taxList.add(rsDetail.getString("tax"));
                        }
                    }
                    termQuery = "SELECT dotm.term FROM deliveryordertermmap dotm WHERE dotm.deliveryorder='" + quotationId + "'";
                    psTerm = conn.prepareStatement(termQuery);
                    rsTerm = psTerm.executeQuery();
                    if (rsTerm != null) {
                        while (rsTerm.next()) {
                            availableAtLineLevel = 0;
                            String termid = rsTerm.getString("term");
                            try {
                                availableAtLineLevel = checkAvailableTermTaxCountAtLineLevel(termid, conn, taxList);
                            } catch (Exception ex) {
                                out.println("Exception occuring while executing script - checkAvailableTermTaxCountAtLineLevel - Delivery Order -" + ex.getMessage());
                            }
                            if (availableAtLineLevel >= 2) {
                                break;
                            }
                        }
                    }
                    if (availableAtLineLevel >= 2) {
        %>
        <tr>
            <td><% out.println(subdomain);%></td>
            <td>Delivery Order</td>
            <td><% out.println(donumber);%></td>
        </tr>
        <%
                            }
                        }
                    }

                } catch (Exception e) {
                    out.println("Exception occuring while executing script - " + subdomain + "  " + e.getMessage());
                    e.printStackTrace();
                }
            }
        %>
    </table>
</div>
<%
        }
    } catch (Exception ex) {
        out.println("Exception occuring while executing script - " + ex.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>

