
package com.krawler.defaultfieldsetup;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccFieldSetupDAO {

    public KwlReturnObject saveMobileConfigs(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getFieldsConfigObj(Map<String, Object> requestParams)throws ServiceException;
}
