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
package com.krawler.crm.exhibernate;

import java.sql.Types;
import org.hibernate.Hibernate;

/**
 *
 * @author trainee
 */
public class ExMySQLInnoDBDialect extends org.hibernate.dialect.MySQLInnoDBDialect {

    public ExMySQLInnoDBDialect() {
        super();

        registerFunction("bitwise_and",
                new BitwiseAndFunction("bitwise_and",
                Hibernate.INTEGER));
        //Registration of TEXT data type needed when we create table using sql query having any column as 'TEXT' [e.g. temp table in Import Process]
        registerColumnType(Types.LONGVARCHAR, 65535, "text");
        registerHibernateType(Types.LONGVARCHAR, Hibernate.TEXT.getName());
    }

    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
}