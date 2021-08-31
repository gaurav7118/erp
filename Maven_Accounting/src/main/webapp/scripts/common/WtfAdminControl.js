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
Wtf.common.MainAdmin = function(config){
    Wtf.common.MainAdmin.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.common.MainAdmin,Wtf.Panel,{
    onRender : function(config){
        Wtf.common.MainAdmin.superclass.onRender.call(this,config);

        this.adminUser = new Wtf.common.UserGrid({
            layout : 'fit',
            iconCls:'useradmin',
            border:false
        });

        this.add(this.adminUser);
    }
});

var profile = new Wtf.common.MainAdmin({
    id: 'mainAdmin',
    companyid: "",
    layout : 'fit',
    border: false
});
Wtf.getCmp("tabcompanyadminpanel").add(profile);
Wtf.getCmp("tabcompanyadminpanel").doLayout();
