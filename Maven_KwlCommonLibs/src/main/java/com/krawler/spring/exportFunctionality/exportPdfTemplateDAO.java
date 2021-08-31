/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.exportFunctionality;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Karthik
 */
public interface exportPdfTemplateDAO {

    public KwlReturnObject saveReportTemplate(HashMap<String, Object> requestParams) throws ServiceException;

    public void saveReportTemplateConfig(String templateId, String configjson) throws ServiceException;

    public void deleteReportTemplateConfig(String templateId) throws ServiceException;

    public KwlReturnObject getAllReportTemplate(String userid, int templatetype) throws ServiceException;

    public KwlReturnObject getReportConfigForTemplate(String templateId) throws ServiceException;

    public KwlReturnObject deleteReportTemplate(String tempid) throws ServiceException;

    public KwlReturnObject editReportTemplate(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getEmailTemplateFiles(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getContractFiles(HashMap<String, Object> requestParams) throws ServiceException;

    void saveEmailTemplateFile(String id, String name, String extn, Date createdOn, int type, String creatorId) throws ServiceException;

    void saveContractsFile(String id, String name, String extn, Date createdOn, int type, String creatorId, String contractId) throws ServiceException;

    public KwlReturnObject getDuplicateTemplateName(String name, String compId) throws ServiceException;
}
