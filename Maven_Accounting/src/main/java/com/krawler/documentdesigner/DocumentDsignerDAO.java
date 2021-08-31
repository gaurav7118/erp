/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.documentdesigner;

/**
 *
 * @author krawler
 */
public interface DocumentDsignerDAO {
    
    public String saveDocument(String moduleid, String templateid,  String json, String html,String companyid,String userid,
            String headerjson,String headerhtml,String footerjson,String footerhtml, String templatesubtype, String sqlquery,String pagelayoutproperty, Boolean isPreview, String countryid, String stateid);
    public String previewDocument(String templateid);
    public String loadDocument(String templateid,String companyid);
    public String getTemplateName(String templateid);
    public boolean isDefaultTemplate(String templateid);
    public String createNewDocument(String moduleid, String templatename,String templatesubtype,String companyid, String userid);
    public String renameNewDocument(String moduleid, String templatename,String templateid,String companyid, String userid);
    public String saveGlobalHeaderFooter(String companyid,String json,String html,String isheader);
    public String importGlobalHeader(String companyid);
    public String importGlobalFooter(String companyid);
    public String importGlobalHeaderFooter(String companyid, int header);
    boolean isDuplicateTemplate(String companyid, String moduleid, String templatename);
    
}
