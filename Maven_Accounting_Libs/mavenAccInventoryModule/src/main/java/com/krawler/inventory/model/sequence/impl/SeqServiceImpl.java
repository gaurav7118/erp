/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.sequence.*;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public class SeqServiceImpl implements SeqService {

    private SeqDAO seqDAO;

    public void setSeqDAO(SeqDAO seqDAO) {
        this.seqDAO = seqDAO;
    }

    @Override
    public SeqModule getSeqModule(ModuleConst moduleConstant) throws ServiceException, SeqFormatException {
        if (moduleConstant == null) {
            throw new SeqFormatException("Module constant is null");
        }
        return seqDAO.getSeqModule(moduleConstant.ordinal());
    }

    @Override
    public SeqModule getSeqModule(Integer moduleId) throws ServiceException, SeqFormatException {
        if (moduleId == null) {
            throw new SeqFormatException("Module constant is null");
        }
        SeqModule seqModule = null;
        for (ModuleConst mc : ModuleConst.values()) {
            if (moduleId == mc.ordinal()) {
                ModuleConst moduleConst = mc;
                seqModule = getSeqModule(moduleConst);
                break;
            }
        }
        return seqModule;
    }

    @Override
    public List<SeqModule> getSeqModules(Company company, Boolean isActive, String searchString, Paging paging) throws ServiceException, SeqFormatException {
        return seqDAO.getSeqModules(company, isActive, searchString, paging);
    }

    @Override
    public List<SeqModule> getSeqModules(Company company, String searchString, Paging paging) throws ServiceException, SeqFormatException {
        return getSeqModules(company, null, searchString, paging);
    }

    @Override
    public SeqFormat getSeqFormat(String seqFormatId) throws ServiceException, SeqFormatException {
        if (StringUtil.isNullOrEmpty(seqFormatId)) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        return seqDAO.getSeqFormat(seqFormatId);
    }

    @Override
    public List checkInvSequenceFormat(Map<String, Object> filterParams) throws ServiceException{
        return seqDAO.checkInvSequenceFormat(filterParams);
    }

    @Override
    public String deleteInvSequenceFormatNumber(String id) throws ServiceException {
        return seqDAO.deleteInvSequenceFormatNumber(id);
    }    
    
    @Override
    public SeqFormat getDefaultSeqFormat(Company company, SeqModule seqModule) throws ServiceException, SeqFormatException {
        if (seqModule == null) {
            throw new SeqFormatException("SeqModule is null");
        }
        SeqFormat seqFormat = seqDAO.getDefaultSeqFormat(company, seqModule);
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.DEFAULT_NOT_FOUND);
        }
        return seqFormat;
    }

    @Override
    public SeqFormat getDefaultSeqFormat(Company company, ModuleConst moduleConst) throws ServiceException, SeqFormatException {
        SeqModule seqModule = getSeqModule(moduleConst);
        SeqFormat seqFormat = seqDAO.getDefaultSeqFormat(company, seqModule);
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.DEFAULT_NOT_FOUND);
        }
        return seqFormat;
    }

    @Override
    public List<SeqFormat> getActiveSeqFormats(Company company, SeqModule seqModule, String searchString, Paging paging) throws ServiceException {
        return seqDAO.getSeqFormats(company, seqModule, true, searchString, paging);
    }

    @Override
    public List<SeqFormat> getSeqFormats(Company company, SeqModule seqModule, String searchString, Paging paging) throws ServiceException {
        return seqDAO.getSeqFormats(company, seqModule, null, searchString, paging);
    }

    @Override
    public void addSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isValidData()) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_VALID);
        }
        String formatString = seqFormat.getFormat(0);
        List<SeqFormat> sequenceFormats = seqDAO.getSeqFormats(seqFormat.getCompany(), seqFormat.getSeqModule(), null, null, null);
        int count = 0;
        for (SeqFormat sf : sequenceFormats) {
            count++;
            String existingSeqFormat = sf.getFormat(0);
            if (formatString.equalsIgnoreCase(existingSeqFormat)) {
                throw new SeqFormatException(SeqFormatException.Type.ALREADY_EXISTS);
            }
        }
        if (count == 0) {
            seqFormat.setDefaultFormat(true);
        }
        seqDAO.saveOrUpdate(seqFormat);

        SeqNumber seqNumber = new SeqNumber(seqFormat, seqFormat.getStartFrom() - 1);
        seqDAO.saveOrUpdate(seqNumber);
    }

    @Override
    public void setSeqFormatAsDefault(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isActive()) {
            throw new SeqFormatException(SeqFormatException.Type.INACTIVE_FOR_DEFAULT);
        }
        SeqFormat sf = seqDAO.getDefaultSeqFormat(seqFormat.getCompany(), seqFormat.getSeqModule());
        if (sf != null) {
            sf.setDefaultFormat(false);
            seqDAO.saveOrUpdate(seqFormat);
        }
        seqFormat.setDefaultFormat(true);
        seqDAO.saveOrUpdate(seqFormat);
    }

    @Override
    public void activateSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (seqFormat.isActive()) {
            throw new SeqFormatException(SeqFormatException.Type.ALREADY_ACTIVE);
        }
        long maxSeq = seqFormat.getMaxSeqNumber();
        long lastSeq = getLastUsedSeqNumber(seqFormat);
        if (lastSeq >= maxSeq) {
            throw new SeqFormatException(SeqFormatException.Type.EXPIRED);
        }
        seqFormat.setActive(true);
        seqDAO.saveOrUpdate(seqFormat);
    }

    @Override
    public void deactivateSeqFormat(User user, SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isActive()) {
            throw new SeqFormatException(SeqFormatException.Type.ALREADY_INACTIVE);
        }
        seqFormat.setActive(false);
        seqDAO.saveOrUpdate(seqFormat);
    }

    @Override
    public void updateSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isValidData()) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_VALID);
        }
        long maxSeq = seqFormat.getMaxSeqNumber();
        SeqNumber seqNumber = seqDAO.getLastUsedSeqNumber(seqFormat);
        long lastSeq = seqNumber.getSerialNumber();
        if (lastSeq >= maxSeq) {
            throw new SeqFormatException(SeqFormatException.Type.EXPIRED);
        }
        seqNumber.setSerialNumber(lastSeq + 1);
        seqDAO.saveOrUpdate(seqNumber);
    }

    @Override
    public long getLastUsedSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isValidData()) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_VALID);
        }
        SeqNumber seqNumber = seqDAO.getLastUsedSeqNumber(seqFormat);
        return seqNumber.getSerialNumber();
    }

    @Override
    public long getNextSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isValidData()) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_VALID);
        }
        long maxSeq = seqFormat.getMaxSeqNumber();
        long lastSeq = getLastUsedSeqNumber(seqFormat);
        if (lastSeq >= maxSeq) {
            throw new SeqFormatException(SeqFormatException.Type.EXPIRED);
        }
        return lastSeq + 1;
    }

    @Override
    public String getNextFormatedSeqNumber(SeqFormat seqFormat) throws ServiceException, SeqFormatException {
        if (seqFormat == null) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_FOUND);
        }
        if (!seqFormat.isValidData()) {
            throw new SeqFormatException(SeqFormatException.Type.NOT_VALID);
        }
        long maxSeq = seqFormat.getMaxSeqNumber();
        long lastSeq = getLastUsedSeqNumber(seqFormat);
        if (lastSeq >= maxSeq) {
            throw new SeqFormatException(SeqFormatException.Type.EXPIRED);
        }
        return seqFormat.getFormat(lastSeq + 1);
    }

    @Override
    public boolean isExistingSeqNumber(String seqNo, Company company, ModuleConst moduleConst) throws ServiceException, SeqFormatException {
        SeqFormat seqForm = new SeqFormat();
        String seqNumber = "";
        boolean isExist = false;
        if (!StringUtil.isNullOrEmpty(seqNo)) {
            isExist = seqDAO.getExistingSeqNumber(seqNo, company, moduleConst);
        }
        return isExist;
    }
    @Override
    public List<String> getExistingSeqNumbers(Map<String, Object> seqParams) throws ServiceException, SeqFormatException {
        
        return seqDAO.getExistingISTSeqNumbers(seqParams);
    }
}
