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

/**
 *
 * @author trainee
 */
import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class BitwiseAndFunction extends StandardSQLFunction implements SQLFunction {

    public BitwiseAndFunction(String name) {
        super(name);
    }

    public BitwiseAndFunction(String name, Type type) {
        super(name, type);
    }

    public String render(List args, SessionFactoryImplementor factory) throws QueryException {
        if (args.size() != 2) {
            throw new IllegalArgumentException("the function must be passed 2 arguments");
        }
        StringBuffer buffer = new StringBuffer(args.get(0).toString());
        buffer.append(" & ").append(args.get(1));
        return buffer.toString();
    }
}