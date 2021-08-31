/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.exception.SeqFormatException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface SeqService {

    public SeqModule getSeqModule(ModuleConst moduleConstant) throws ServiceException, SeqFormatException;

    public SeqModule getSeqModule(Integer moduleId) throws ServiceException, SeqFormatException;

    public List<SeqModule> getSeqModules(Company company, Boolean isActive, String searchString, Paging paging) throws ServiceException, SeqFormatException;

    public List<SeqModule> getSeqModules(Company company, String searchString, Paging paging) throws ServiceException, SeqFormatException;

    public SeqFormat getSeqFormat(String seqFormatId) throws ServiceException, SeqFormatException;

    public List checkInvSequenceFormat(Map<String, Object> filterParams) throws ServiceException;

    public String deleteInvSequenceFormatNumber(String id) throws ServiceException;

    public SeqFormat getDefaultSeqFormat(Company company, SeqModule seqModule) throws ServiceException, SeqFormatException;

    public SeqFormat getDefaultSeqFormat(Company company, ModuleConst moduleConst) throws ServiceException, SeqFormatException;

    public List<SeqFormat> getActiveSeqFormats(Company company, SeqModule seqModule, String searchString, Paging paging) throws ServiceException;

    public List<SeqFormat> getSeqFormats(Company company, SeqModule seqModule, String searchString, Paging paging) throws ServiceException;

    public void addSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public void setSeqFormatAsDefault(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public void activateSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public void deactivateSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public long getLastUsedSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public long getNextSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public String getNextFormatedSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException;

    public boolean isExistingSeqNumber(String seqNo, Company company, ModuleConst moduleConst) throws ServiceException, SeqFormatException;
    
    public List<String> getExistingSeqNumbers(Map<String, Object> seqParams) throws ServiceException, SeqFormatException;

    public void updateSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException;
}
