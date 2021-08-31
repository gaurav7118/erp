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

Wtf.account.IndiaCompanyPreferences = function (config) {
    Wtf.account.IndiaCompanyPreferences.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true,
        'loadMasterGroup': true
    });
}
Wtf.extend(Wtf.account.IndiaCompanyPreferences, Wtf.Panel, {
    initComponent: function () {
        Wtf.account.IndiaCompanyPreferences.superclass.initComponent.call(this);
        this.mainForm();
        this.add(this.formIndiaPanel);
    },
    mainForm: function () {// It is must to not push component in main array insted of hide it. 
        
        // *****************Component for First column***********************//
        this.firstColumnArray = new Array();

        var gstItemsArr = new Array();
        this.isGSTApplicable =new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText('acc.companypreferences.isGSTapplicable'),//"Is GST Applicable",
            width:200,
            name:'isGSTApplicable',
            checked:Wtf.isGSTApplicable
        });
        gstItemsArr.push(this.isGSTApplicable);
        this.GSTIN =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin')+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstininfo")),
            width : 200,
            name:'GSTIN',
            value:Wtf.GSTIN,
            regex:/[A-Z | 0-9]{10}\d{3}[A-Z]{2}/,
            invalidText:"Invalid GSTIN"
        });
        gstItemsArr.push(this.GSTIN);
        
        this.GSTFieldSet=new Wtf.form.FieldSet({
            id: 'GSTFieldSet',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText('acc.companypreferences.isGST_details'),//"GST (Goods and Service Tax details)"
            items:gstItemsArr
        });
        
        this.firstColumnArray.push(this.GSTFieldSet);
        
        // *****************Component for second column***********************//
        this.secondColumnArray = new Array();
        
        this.GSTINHide =new Wtf.form.TextField({
            fieldLabel:"GSTINHide",
            width : 200,
            name:'GSTINHide',
//            value:Wtf.GSTIN,
            hidden:true,
            hideLabel:true
        });
        
        this.secondColumnArray.push(this.GSTINHide);
        

        this.formIndiaPanel=new Wtf.form.FormPanel({
            style: 'background: white;',
            border:false,
            autoHeight: true,     
            buttonAlign:'left',
            autoScroll:true,
            defaults:{
                labelWidth:200,
                border:false
            },
            items:[{
                layout:'column',
                defaults:{
                    border:false,
                    bodyStyle:'padding:10px'
                },
                items:[{
                    columnWidth:.49,
                    layout:'form',
                    items:this.firstColumnArray
                },{
                    columnWidth:.49,
                    layout:'form',
                    items:this.secondColumnArray
                }]
            }]
        });
    }
},this);

