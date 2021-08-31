/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.exception.SeqFormatException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface SeqDAO {

    public SeqFormat getSeqFormat(String seqFormatId) throws ServiceException;

    public SeqFormat getDefaultSeqFormat(Company company, SeqModule seqModule) throws ServiceException;

    public SeqModule getSeqModule(Integer seqModuleId) throws ServiceException;

    public List<SeqFormat> getSeqFormats(Company company, SeqModule seqModule, Boolean isActive, String searchString, Paging paging) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public SeqNumber getLastUsedSeqNumber(SeqFormat seqFormat) throws ServiceException;

    public List<SeqModule> getSeqModules(Company company, Boolean active, String searchString, Paging paging) throws ServiceException;
    
    public List checkInvSequenceFormat(Map<String, Object> filterParams) throws ServiceException ;
    
    public String deleteInvSequenceFormatNumber(String id) throws ServiceException;
         
    public boolean getExistingSeqNumber(String seqFormat,Company company,ModuleConst moduleConst) throws ServiceException, SeqFormatException;
    
    public List<String> getExistingISTSeqNumbers(Map<String, Object> seqParams) throws ServiceException;
}
