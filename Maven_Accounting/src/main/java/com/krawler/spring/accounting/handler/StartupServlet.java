/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.spring.accounting.customreports.AccCustomReportController;
import com.krawler.spring.accounting.customreports.AccCustomReportService;
import com.krawler.spring.accounting.reports.CommonExportService;
import com.krawler.spring.exportFunctionality.ExportLog;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author krawler
 */
public class StartupServlet extends HttpServlet {

    private CommonExportService commonExportService;

    @Override
    public void init() throws ServletException {
        ApplicationContext ac;
        System.out.println("Free memory (bytes): "
                + Runtime.getRuntime().freeMemory());

        ac = (ApplicationContext) getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        this.commonExportService = (CommonExportService) ac.getBean("CommonExportService");

        System.out.println("Inside StartupServlet");
        try {
            List<ExportLog> pendingExports = commonExportService.getPendingExports();
            for (ExportLog pendingFile : pendingExports) {
                JSONObject requestJSON = new JSONObject(pendingFile.getRequestJSON());
                requestJSON.put("exportid", pendingFile.getId());
                requestJSON.put("fromStartupServlet",true);
                commonExportService.exportFileService(requestJSON);
            }
        } catch (Exception ex) {
            Logger.getLogger(CommonExportService.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Free memory (bytes): "
                + Runtime.getRuntime().freeMemory());

    }

}
