/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.documentdesigner;



import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 *
 * @author krawler
 */
public class DocumentDsignerDaoImpl implements  DocumentDsignerDAO{

    private SessionFactory sessionFactory;


    public DocumentDsignerDaoImpl(){
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    

    public String saveDocument(String moduleid, String templateid,  String json,String html,String companyid, String userid,
            String headerjson,String headerhtml,String footerjson,String footerhtml, String templatesubtype, String sqlquery,String pageproperty, Boolean isPreview,String countryid, String stateid){
        String result="";
        Session session=null;
        try {

              SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

               String id=templateid;
               String company=companyid;
               String createdon=""+sdf.format(new Date());
               String createdby=userid;
               String pagelayoutproperty=pageproperty;
               String pagefooterjson=footerjson;
               String pagefooterhtml=footerhtml;
               String pagefootersqlquery="";
               String pageheaderjson=headerjson;
               String pageheaderhtml=headerhtml;
               String pageheadersqlquery="";
               JSONArray jsonArr=new JSONArray(json);

                String deleteQuery="delete from customdesigntemplate where id='1'";

                String query="update customdesigntemplate set moduleid=:moduleid,html=:html,json=:json,sqlquery=:sqlquery,"
                        + "company=:company,createdon=:createdon,createdby=:createdby,pagelayoutproperty=:pagelayoutproperty,"
                        + "pagefooterjson=:pagefooterjson,pagefooterhtml=:pagefooterhtml,pagefootersqlquery=:pagefootersqlquery,"
                        + "templatesubtype=:templatesubtype,pageheaderjson=:pageheaderjson,pageheaderhtml=:pageheaderhtml,pageheadersqlquery=:pageheadersqlquery,countryid=:countryid,stateid=:stateid"
                        + " where id='"+id+"'";
                
                 
                session =sessionFactory.openSession();

                if(isPreview){
                    deleteQuery="delete from previewcustomdesigntemplate where company='"+companyid+"'";
                    
                    query="insert into previewcustomdesigntemplate(id,createdon,company,html,json,sqlquery,pagelayoutproperty,"
                          +"pagefooterjson,pagefooterhtml,pagefootersqlquery,pageheaderjson,pageheaderhtml,pageheadersqlquery) "
                          +"values(UUID(),NOW(),:company,:html,:json,:sqlquery,:pagelayoutproperty,"
                          +":pagefooterjson,:pagefooterhtml,:pagefootersqlquery,:pageheaderjson,:pageheaderhtml,:pageheadersqlquery)";
                }
                
                SQLQuery sqlQuery=session.createSQLQuery(deleteQuery);
                sqlQuery.executeUpdate();
                session.beginTransaction().commit();
                
                sqlQuery=session.createSQLQuery(query);
                if(!isPreview){
                    sqlQuery.setParameter("moduleid", moduleid);
                    sqlQuery.setParameter("createdon", createdon);
                    sqlQuery.setParameter("createdby", createdby);
                    sqlQuery.setParameter("templatesubtype", templatesubtype);
                    sqlQuery.setParameter("countryid", countryid);
                    sqlQuery.setParameter("stateid", stateid);
                }
                    sqlQuery.setParameter("html", html);
                    sqlQuery.setParameter("json",json );
                    sqlQuery.setParameter("sqlquery",sqlquery );
    //                sqlQuery.setParameter("isdefault",isdefault );
                    sqlQuery.setParameter("company",company );
                    sqlQuery.setParameter("pagelayoutproperty",pagelayoutproperty );
                    sqlQuery.setParameter("pagefooterjson",pagefooterjson );
                    sqlQuery.setParameter("pagefooterhtml", pagefooterhtml);
                    sqlQuery.setParameter("pagefootersqlquery", pagefootersqlquery);
                    sqlQuery.setParameter("pageheaderjson", pageheaderjson);
                    sqlQuery.setParameter("pageheaderhtml", pageheaderhtml);
                    sqlQuery.setParameter("pageheadersqlquery", pageheadersqlquery);
//                sqlQuery.setParameter("isnewdesign", 1);
                sqlQuery.executeUpdate();
                session.beginTransaction().commit();

            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
        }
        return result;
        
    }

      public String previewDocument(String templateid){
         String result="";
         JSONObject json=new JSONObject();
         Session session=null;
          SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

          try {
               String id=templateid;
               String templatename="";
               String sqlquery="";
               String company="9f931b2d-1a19-44c4-b180-1d801f7ec71c";
               String createdon=""+sdf.format(new Date());
               String createdby="";
               String pagelayoutproperty="";
               String pagefooterjson="";
               String pagefooterhtml="";
               String pagefootersqlquery="";
               String templatesubtype="";
               String pageheaderjson="";
               String pageheaderhtml="";
               String pageheadersqlquery="";

               String htmlBody="<input type=text>";

               String query="select * from customdesigntemplate where id='"+id+"'";

              session =sessionFactory.openSession();
              SQLQuery sqlQuery=session.createSQLQuery(query);
              sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

              List<Map> list=sqlQuery.list();

              if(list.size()>0)
              {

                  Map map=list.get(0);
                  htmlBody=map.get("html").toString();

              }



              

              
              json.append("success", true);
              json.append("htmlbody", htmlBody);
              result=json.toString();
              


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
        }
        return result;

      }


       public String loadDocument(String templateid,String companyid){
         String result="";
         JSONObject json=new JSONObject();
         Session session=null;
          SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

          try {
               String id=templateid;
               String templatename="";
               String sqlquery="";
               String company="";
               String createdon=""+sdf.format(new Date());
               String createdby="";
               String pagelayoutproperty="";
               String pagefooterjson="";
               String pagefooterhtml="";
               String pagefootersqlquery="";
               String templatesubtype="";
               String pageheaderjson="";
               String pageheaderhtml="";
               String pageheadersqlquery="";
               boolean isDefultTemplate = isDefaultTemplate(templateid);
               
               String jsonBody="";
               String htmlBody="<input type=text>";

               String query="select * from customdesigntemplate where id='"+id+"'";

              session =sessionFactory.openSession();
              SQLQuery sqlQuery=session.createSQLQuery(query);
              sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
              
              List<Map> list=sqlQuery.list();

              if(list.size()>0)
              {

                  Map map=list.get(0);
                  if (isDefultTemplate) {
                      htmlBody = map.get("html").toString().replaceAll("video.jsp?", "defaulttemplateimageload.jsp?cid="+companyid+"&");
                      jsonBody = map.get("json").toString().replaceAll("video.jsp?", "defaulttemplateimageload.jsp?cid="+companyid+"&");
                  } else {
                      htmlBody = map.get("html").toString();
                      jsonBody = map.get("json").toString();
                  }

              }

              json.append("success", "true");
              json.append("htmlbody", htmlBody);
              json.append("jsonBody", jsonBody);
              result=json.toString();



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
        }
        return result;

      }

    public String getTemplateName(String templateid) {
        String templatename = "";
        Session session = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            String id = templateid;
            String query = "select templatename from customdesigntemplate where id='" + id + "'";
            session = sessionFactory.openSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

            List<Map> list = sqlQuery.list();

            if (list.size() > 0) {

                Map map = list.get(0);
                templatename = map.get("templatename").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" " + e);
        } finally {
            session.close();
        }
        return templatename;

    }
       
       public boolean isDefaultTemplate(String templateid) {
        boolean isDefault = false;
        JSONObject json = new JSONObject();
        Session session = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String id = templateid;
            String jsonBody = "";
            String htmlBody = "<input type=text>";

            String query = "select isdefaulttemplate from customdesigntemplate where id='" + id + "' and isdefaulttemplate = true";

            session = sessionFactory.openSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

            List<Map> list = sqlQuery.list();

            if (list.size() > 0) {
                isDefault = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" " + e);
        } finally {
            session.close();
        }
        return isDefault;
    }
    public String renameNewDocument(String moduleid, String templatename,String templateid, String companyid, String userid) {
        String result = "";
        Session session = null;
        try {
            String query="update customdesigntemplate set templatename='" + templatename + "'  where id='" + templateid + "' and moduleid='" + moduleid + "' and company='" + companyid + "'";
            session =sessionFactory.openSession();
            
            SQLQuery sqlQuery=null;
            sqlQuery=session.createSQLQuery(query);
            sqlQuery.executeUpdate();
            session.beginTransaction().commit();

            result = "{'valid':true,'data':{'msg':'acc.customedesigner.Templaterenamedsuccessfully','success':true},'success':true}";
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" " + e);
            result = "{'valid':true,'data':{'msg':'Error occurred while renaming template.','success':false},'success':false}";
        } finally {
            session.close();
        }
        return result;
    }
   public String createNewDocument(String moduleid, String templatename,String templatesubtype,String companyid, String userid)
    {

        String result="";
        Session session=null;
        JSONObject json=new JSONObject();
        try {

              SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

               String id=UUID.randomUUID().toString();
               String sqlquery="";
               String company=companyid;
               String createdon=""+sdf.format(new Date());
               String updatedon = "" + sdf.format(new Date());
               String createdby=userid;

                String query="insert into customdesigntemplate (id,templatename,moduleid,html,json,sqlquery,isdefault,company,createdon,updatedon,createdby,pagelayoutproperty,pagefooterjson,pagefooterhtml,pagefootersqlquery,templatesubtype,pageheaderjson,pageheaderhtml,pageheadersqlquery,isnewdesign)"
                                                    +" values (:id,:templatename,:moduleid,:html,:json,:sqlquery,:isdefault,:company,:createdon,:updatedon,:createdby,:pagelayoutproperty,:pagefooterjson,:pagefooterhtml,:pagefootersqlquery,:templatesubtype,:pageheaderjson,:pageheaderhtml,:pageheadersqlquery,:isnewdesign) ";


                session =sessionFactory.openSession();


                SQLQuery sqlQuery=null;

            sqlQuery=session.createSQLQuery(query);
            sqlQuery.setParameter("id", id);
            sqlQuery.setParameter("templatename", templatename);
            sqlQuery.setParameter("moduleid", moduleid);
            sqlQuery.setParameter("html", "");
            sqlQuery.setParameter("json","[]" );
            sqlQuery.setParameter("sqlquery",sqlquery );
            sqlQuery.setParameter("isdefault","1" );
            sqlQuery.setParameter("company",company );
            sqlQuery.setParameter("createdon", createdon);
            sqlQuery.setParameter("updatedon", updatedon);
            sqlQuery.setParameter("createdby", createdby);
            sqlQuery.setParameter("pagelayoutproperty","" );
            sqlQuery.setParameter("pagefooterjson","" );
            sqlQuery.setParameter("pagefooterhtml", "");
            sqlQuery.setParameter("pagefootersqlquery", "");
            sqlQuery.setParameter("templatesubtype",templatesubtype);
            sqlQuery.setParameter("pageheaderjson", "");
            sqlQuery.setParameter("pageheaderhtml", "");
            sqlQuery.setParameter("pageheadersqlquery", "");
            sqlQuery.setParameter("isnewdesign", 1);
            sqlQuery.executeUpdate();
            session.beginTransaction().commit();

               result="{'valid':true,'data':{'msg':'acc.field.Templatesavedsuccessfully','success':true},'success':true}";



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
            result="{'valid':true,'data':{'msg':'Error occurred while saving template.','success':false},'success':false}";
        }
        finally{
            session.close();
        }
        return result;

       
    }

   public String saveGlobalHeaderFooter(String companyid,String json,String html,String isheader)
   {

        String result="";
        Session session=null;
        
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            session = sessionFactory.openSession();
            SQLQuery sqlQuery = null;
            String id = UUID.randomUUID().toString();
            String selectQuery = "select id from globalheaderfooter where companyid = '" + companyid + "' and isheader =" + isheader;
            sqlQuery = session.createSQLQuery(selectQuery);
            List list = sqlQuery.list();
            if (list.size()>0) {
                    String query = "update globalheaderfooter set html = :html, json = :json where companyid = '" + companyid + "' and isheader =" + isheader;
                    sqlQuery = session.createSQLQuery(query);
                    sqlQuery.setParameter("html", html);
                    sqlQuery.setParameter("json", json);
                    sqlQuery.executeUpdate();
                
            } else {
                String query = "insert into globalheaderfooter (id,companyid,html,json,isheader)"
                        + " values (:id,:companyid,:html,:json,:isheader) ";

//                session = sessionFactory.openSession();

                sqlQuery = session.createSQLQuery(query);
                sqlQuery.setParameter("id", id);
                sqlQuery.setParameter("companyid", companyid);
                sqlQuery.setParameter("html", html);
                sqlQuery.setParameter("json", json);
                sqlQuery.setParameter("isheader", isheader);

                sqlQuery.executeUpdate();
//                session.beginTransaction().commit();
            }
            session.beginTransaction().commit();
   
            result="{'valid':true,'data':{'msg':'Template saved successfully.','success':true},'success':true}";



        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
            result="{'valid':true,'data':{'msg':'Error occurred while saving template.','success':false},'success':false}";
        }
        finally{
            session.close();
        }
        return result;

   }

   public String importGlobalHeader(String companyid)
   {
         String result="";
         JSONObject jsonObject=new JSONObject();
         Session session=null;
          SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

          try {
               
                      String json = "";
                      String html = "";

                      String query = "select * from globalheaderfooter where companyid='" + companyid + "' and isheader=1";

                      session = sessionFactory.openSession();
                      SQLQuery sqlQuery = session.createSQLQuery(query);
                      sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

                      List<Map> list = sqlQuery.list();

                      if (list.size() > 0) {

                          Map map = list.get(0);
                          html = map.get("html").toString();
                          json = map.get("json").toString();

                          jsonObject.append("success", "true");
                          jsonObject.append("html", html);
                          jsonObject.append("json", json);
                          jsonObject.append("msg", "Global header found.");
                          jsonObject.append("headeravailable", "1");

                      }
                      else
                      {
                          jsonObject.append("success", "true");
                          jsonObject.append("html", html);
                          jsonObject.append("json", json);
                          jsonObject.append("msg", "Global header is not set for this company.");
                          jsonObject.append("headeravailable", "0");
                      }


        } catch (Exception e) {
              try {
                  jsonObject.append("success", "false");
                  jsonObject.append("msg", "Global header is not set for this company.");
              } catch (Exception e1) {
              }
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
            result = jsonObject.toString();
        }
        return result;

       
   }

   public String importGlobalFooter(String companyid)
   {
       String result="";
         JSONObject jsonObject=new JSONObject();
         Session session=null;
          SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");

          try {

                      String json = "";
                      String html = "";

                      String query = "select * from globalheaderfooter where companyid='" + companyid + "' and isheader=0";

                      session = sessionFactory.openSession();
                      SQLQuery sqlQuery = session.createSQLQuery(query);
                      sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

                      List<Map> list = sqlQuery.list();

                      if (list.size() > 0) {

                          Map map = list.get(0);
                          html = map.get("html").toString();
                          json = map.get("json").toString();

                          jsonObject.append("success", "true");
                          jsonObject.append("html", html);
                          jsonObject.append("json", json);
                          jsonObject.append("msg", "Global footer found.");
                          jsonObject.append("footeravailable", "1");

                      }
                      else
                      {
                          jsonObject.append("success", "true");
                          jsonObject.append("html", html);
                          jsonObject.append("json", json);
                          jsonObject.append("msg", "Global footer is not set for this company.");
                          jsonObject.append("footeravailable", "0");
                      }


        } catch (Exception e) {
              try {
                  jsonObject.append("success", "false");
                  jsonObject.append("msg", "Global footer is not set for this company.");
              } catch (Exception e1) {
              }
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
            result = jsonObject.toString();
        }
        return result;
       
   }
   
    public String importGlobalHeaderFooter(String companyid, int header) {
        String result = "";
        JSONObject jsonObject = new JSONObject();
        Session session = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {

            String json = "";
            String html = "";

            String query = "select * from globalheaderfooter where companyid='" + companyid + "' and isheader=" + header;

            session = sessionFactory.openSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

            List<Map> list = sqlQuery.list();

            if (list.size() > 0) {

                Map map = list.get(0);
                html = map.get("html").toString();
                json = map.get("json").toString();

                jsonObject.append("success", "true");
                jsonObject.append("html", html);
                jsonObject.append("json", json);
                if (header == 1) {
                    jsonObject.append("msg", "Global header found.");
                    jsonObject.append("headerfooteravailable", "1");
                } else {
                    jsonObject.append("msg", "Global footer found.");
                    jsonObject.append("headerfooteravailable", "1");
                }

            } else {
                jsonObject.append("success", "true");
                jsonObject.append("html", html);
                jsonObject.append("json", json);
                if (header == 1) {
                    jsonObject.append("msg", "Global header is not set for this company.");
                    jsonObject.append("headerfooteravailable", "0");
                } else {
                    jsonObject.append("msg", "Global footer is not set for this company.");
                    jsonObject.append("headerfooteravailable", "0");
                }

            }


        } catch (Exception e) {
            try {
                jsonObject.append("success", "false");
                if (header == 1) {
                    jsonObject.append("msg", "Global header is not set for this company.");
                } else {
                    jsonObject.append("msg", "Global footer is not set for this company.");
                }

            } catch (Exception e1) {
            }
            e.printStackTrace();
            System.out.println(" " + e);
        } finally {
            session.close();
            result = jsonObject.toString();
        }
        return result;

    }
    
    public boolean isDuplicateTemplate(String companyid, String moduleid, String templatename) {
        List<Map> ll = new ArrayList();
        boolean isDuplicate = false;
        Session session = null;
        
        try {
            String query = "";
            query = "select * from customdesigntemplate where templatename='" + templatename + "' and moduleid='" + moduleid + "' and ( company='" + companyid + "' or company='" + Constants.defaultTemplateCompanyid + "' )";
            session = sessionFactory.openSession();
            SQLQuery sqlQuery = session.createSQLQuery(query);
            sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

            ll = sqlQuery.list();
            if (!ll.isEmpty()) {
                isDuplicate = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" "+e);
        }
        finally{
            session.close();
        }
        return isDuplicate;
    }
}
