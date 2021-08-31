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
package com.krawler.common.util;

public interface globalString {
    /*
     * String _packagePath = "com.krawler.esp.hibernate.impl"; String _TPL_ROOT
     * ="com/krawler/portal/tools/dependencies/"; String _tplModel = _TPL_ROOT +
     * "model.ftl"; String _tplHbmXml = _TPL_ROOT + "hbm_xml.ftl"; String
     * generateDirPath =
     * "/home/krawler/jsonic/src/java/com/krawler/esp/hibernate/impl/"; String
     * cfgSourceFilePath = "/home/krawler/jsonic/src/java/hibernate.cfg.xml";
     * String cfgClassesFilePath =
     * "/tomcat/webapps/jsonic/WEB-INF/classes/hibernate.cfg.xml"; String
     * packagePath = "com/krawler/esp/hibernate/impl/"; String
     * _reportHardcodeStr = "X_X"; String _storePath =
     * "/home/krawler/jsonic/store/"; String module_Properties =
     * "/tomcat/webapps/jsonic/WEB-INF/classes/com/krawler/portal/tools/module.properties";
     * String module_Build_XML =
     * "/tomcat/webapps/jsonic/WEB-INF/classes/com/krawler/portal/tools/";
     * String module_Source_Dir =
     * "/home/krawler/jsonic/src/java/com/krawler/esp/hibernate/impl/"; String
     * module_Classes_Dir =
     * "/tomcat/webapps/jsonic/WEB-INF/classes/com/krawler/esp/hibernate/impl/";
     * String module_Classes_Dest_Dir =
     * "/tomcat/webapps/jsonic/WEB-INF/classes/"; String indexPath =
     * "/home/krawler/jsonic/Log/segments";
     */

    String _jspFileContent = "<%@ page contentType=\"text/html\"%>\n"
            + "<%@ page pageEncoding=\"UTF-8\"%>\n"
            + "<%@ page import=\"com.krawler.common.session.SessionExpiredException\"%>\n"
            + "<%@ page import=\"com.krawler.esp.database.dbcon\"%>\n"
            + "<jsp:useBean id=\"sessionbean\" scope=\"session\" class=\"com.krawler.spring.sessionHandler.sessionHandlerImpl\" />\n\n<%\n%>";
    String _jspFilePath = "/home/krawler/Pravin/NetBeansProjects/HibernateFramework/web/jspfiles/buttonJsps";
    String _packagePath = "com.krawler.esp.hibernate.impl";
    String _TPL_ROOT = "com/krawler/portal/tools/dependencies/";
    String _tplModel = _TPL_ROOT + "model.ftl";
    String _tplImplModel = _TPL_ROOT + "model_impl.ftl";
    String _tplHbmXml = _TPL_ROOT + "hbm_xml.ftl";
    String generateDirPath = "/home/krawler-user/Projects/HibernateFramework/src/java/com/krawler/esp/hibernate/impl/";
    String cfgSourceFilePath = "/home/krawler-user/Projects/HibernateFramework/src/java/hibernate.cfg.xml";
    String cfgClassesFilePath = "/home/krawler-user/Projects/HibernateFramework/build/web/WEB-INF/classes/hibernate.cfg.xml";
    String packagePath = "com/krawler/esp/hibernate/impl/";
    String _reportHardcodeStr = "X_X";
    String _storePath = "/home/krawler-user/store/";
    String module_Properties = "/home/krawler-user/Projects/HibernateFramework/build/web/WEB-INF/classes/com/krawler/portal/tools/module.properties";
    String module_Build_XML = "/home/krawler-user/Projects/HibernateFramework/build/web/WEB-INF/classes/com/krawler/portal/tools/";
    String module_Source_Dir = "/home/krawler-user/Projects/HibernateFramework/src/java/com/krawler/esp/hibernate/impl/";
    String module_Classes_Dir = "/home/krawler-user/Projects/HibernateFramework/build/web/WEB-INF/classes/com/krawler/esp/hibernate/impl/";
    String module_Classes_Dest_Dir = "/home/krawler-user/Projects/HibernateFramework/build/web/WEB-INF/classes/";
    String indexPath = "/home/krawler-user/Log/segments";
}
