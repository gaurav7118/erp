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
Wtf.account.TaxWindow = function(config){

    Wtf.account.TaxWindow.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.TaxWindow,Wtf.account.GridUpdateWindow,{
    onRender:function(config){
        Wtf.account.TaxWindow.superclass.onRender.call(this,config);
        this.grid.on('afteredit', this.handleEdit, this);
        this.grid.on('beforeedit',this.checkrecord,this);
    },
    handleEdit:function(obj){
        if(obj.field == 'percent' && obj.value != obj.originalValue)
            obj.record.set('applydate', Wtf.account.companyAccountPref.fyfrom); 
    },
    checkrecord: function (obj) {
        if (obj.record.data.taxid != undefined && obj.record.data.taxid != "") {
            if (obj.field == "accountid") {
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA) {
                    for (var taxcount = 0; taxcount < Wtf.MalaysianGSTForm03Taxes.length; taxcount++) {
                        if (Wtf.MalaysianGSTForm03Taxes[taxcount] == obj.record.data.taxname) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "<b>" + obj.record.data.taxname + " </b> " + WtfGlobal.getLocaleText("acc.common.taxes.editaccountName.alertForMalaysia")], 2);
                            obj.cancel = true;
                        }
                    }
                } else {
                    obj.cancel = true;
                }
            } else if (obj.field != "taxcode" && obj.field != "activated") {
                obj.cancel = true;
            }
        }
    }
});