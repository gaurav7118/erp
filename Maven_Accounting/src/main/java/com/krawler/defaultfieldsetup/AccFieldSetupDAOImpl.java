
package com.krawler.defaultfieldsetup;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.Modules;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.util.*;

/**
 *
 * @author krawler
 */
public class AccFieldSetupDAOImpl extends BaseDAO implements AccFieldSetupDAO {
    
  @Override
    public KwlReturnObject saveMobileConfigs(HashMap<String, Object> requestParams)throws ServiceException {
        List list = new ArrayList();
        boolean issuccess=false;
        String msg="";
        try {
            String id = UUID.randomUUID().toString();
            String moduleid = (String) requestParams.get(Constants.moduleid);
            Modules module = (Modules) get(Modules.class, moduleid);
            String companyid = (String) requestParams.get(Constants.companyKey);
            Company companyobj = (Company) get(Company.class, companyid);
            String query = "from MobileFieldSetup where moduleid.id=? and company.companyID=?";
            List<MobileFieldSetup> mobileFieldMappings = executeQuery(query, new String[]{moduleid, companyid});
            
            String data = (String) requestParams.get(Constants.data);
            String type = (String) requestParams.get(Constants.type);

            if (mobileFieldMappings.size() > 0) {
                for (MobileFieldSetup mbf : mobileFieldMappings) {
                    id = mbf.getId();
                    if (!StringUtil.isNullOrEmpty(id)) {
                        if (type.equals(Constants.SummaryView)) {
                            mbf.setSummaryreportjson(data);
                        } else if (type.equals(Constants.DetailView)) {
                            mbf.setDetailreportjson(data);
                        } else if (type.equals(Constants.AddEditView)) {
                            mbf.setFormfieldjson(data);
                        }
                        saveOrUpdate(mbf);
                        msg = "Configuration has been updated successfully.";
                        list.add(mbf);
                    }
                }
            } else {
                MobileFieldSetup mobilefieldsetup = new MobileFieldSetup();
                if (type.equals(Constants.SummaryView)) {
                    mobilefieldsetup.setSummaryreportjson(data);
                } else if (type.equals(Constants.DetailView)) {
                    mobilefieldsetup.setDetailreportjson(data);
                } else if (type.equals(Constants.AddEditView)) {
                    mobilefieldsetup.setFormfieldjson(data);
                }
                mobilefieldsetup.setId(id);
                mobilefieldsetup.setCompany(companyobj);
                mobilefieldsetup.setModuleid(module);
                saveOrUpdate(mobilefieldsetup);
                msg = "Configuration has been saved successfully.";
                list.add(mobilefieldsetup);
            }

            issuccess=true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccFieldSetupDAOImpl.saveMobileConfigs : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(issuccess, msg, null, list, list.size());
    }
  
 @Override
    public KwlReturnObject getFieldsConfigObj(Map<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            List<Integer> indexList = new ArrayList<Integer>();
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            if (requestParams.containsKey(Constants.hqlquery)) {
                hql = (String) requestParams.get(Constants.hqlquery);
            }

            if (requestParams.get(Constants.filter_names) != null && requestParams.get(Constants.filter_values) != null) {
                name = new ArrayList((List<String>) requestParams.get(Constants.filter_names));
                value = new ArrayList((List<Object>) requestParams.get(Constants.filter_values));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");
                while (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    indexList.add(index);
                    ind = hql.indexOf("(", ind + 1);
                }
                Collections.reverse(indexList);
                for (Integer ctr : indexList) {
                    value.remove(ctr.intValue());
                }
            }
            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery(hql, value.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccFieldSetupDAOImpl.getFieldsConfigObj : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
 
}
