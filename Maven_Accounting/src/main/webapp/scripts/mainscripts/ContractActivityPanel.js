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

Wtf.ContractActivityPanel = function(config) {
    this.record = config.record;
    Wtf.apply(this, config);
    Wtf.ContractActivityPanel.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ContractActivityPanel, Wtf.Panel, {
    initComponent: function(){
        Wtf.ContractActivityPanel.superclass.initComponent.call(this);
        

//================================================================== Contract Activity Detail Grid ======================================================================
        
        this.activityContractRec= Wtf.data.Record.create([
            {name:'activityid'},
            {name:'contractid'},
            {name:'flag'},
            {name:'relatedname'},
            {name:'relatednameid'},
            {name:'ownerid'},
            {name:'owner'},
            {name:'relatedtoold'},
            {name:'subject'},
            {name:'statusid'},
            {name:'status'},
            {name:'type'},
            {name:'typeid'},
            {name:'calid'},
            {name:'calname'},
            {name:'startdate', type:'date'},
            {name:'startdat'},
            {name:'enddate', type:'date'},
            {name:'enddat'},
            {name:'priorityid'},
            {name:'priority'},
            {name:'phone'},
            {name:'location'},
            {name:'description'},
            {name:'email'},
            {name:'isallday'},
            {name:'createdon'},
            {name:'relatedto'},
            {name:'commentcount'},
            {name:'totalcomment'},
            {name:'validflag'}
        ]);
        
        this.activityContractStore = new Wtf.data.Store({
            url: "ACCSalesOrderCMN/getContractActivityDetails.do",
            baseParams:{
                contractid: this.record.data.cid,
                customerid:this.record.data.accid
            },
            sortInfo:{
               field: 'startdate',
               direction: "ASC"  
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.activityContractRec)
        });
        
        this.activityContractStore.load();

        this.activityContractDetailsGrid = new Wtf.grid.GridPanel({            
            store:this.activityContractStore,
            id:"contractactivitygrid",
            border:false,
            layout:'fit',
            height:230,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.Nodatatodisplay"))
            },
            forceFit:true,
            columns:[{
                hidden:true,
                dataIndex:'id'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.TaskEvent"), // 'Task/Event',
                dataIndex:'flag'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Owner"), // 'Owner',
                dataIndex:'owner'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Subject"), // 'Subject',
                dataIndex:'subject'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Status"), // 'Status',
                dataIndex:'status'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Type"), // 'Type',
                dataIndex:'type'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Calendar"), // 'Calendar',
                dataIndex:'calname'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.StartDate"), // 'Start Date',
                dataIndex:'startdate',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.EndDate"), // 'End Date',
                dataIndex:'enddate',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Priority"), // 'Priority',
                dataIndex:'priority'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Phone"), // 'Phone',
                dataIndex:'phone'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"), // 'Location',
                dataIndex:'location'
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"), // 'Description',
                dataIndex:'description'
            }]
            });
            
    
//================================================================== Contract Maintenance Detail Grid ======================================================================

        this.maintainanceformGridRec= Wtf.data.Record.create([
                {name:'maintainanceid'},
                {name:'maintainanceno'},
                {name:'maintainanceamt'},
                {name:'contractno'},
                {name:'memo'},                        
                {name:'status'},
                {name:'accountid'},
                {name:'accountname'},
                {name:'casid'},
                {name:'companyid'},
                {name:'remark'},
                {name:'recommendedForSales'}
        ]);
                    
        this.maintainanceformGridStore = new Wtf.data.Store({
                url: "ACCSalesOrderCMN/getMaintainanceFormListDetails.do",
                baseParams:{
                    contractid: this.record.data.cid
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:'count'
                },this.maintainanceformGridRec)
        });
        
        this.maintainanceformGridStore.load();

        this.maintainanceFormGrid = new Wtf.grid.GridPanel({
            store:this.maintainanceformGridStore,
            id:"maintainancefrmgrid",
            border:false,
            layout:'fit',
            height:230,
//            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.Nodatatodisplay"))
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.MaintenanceNo"), // 'Maintenance No.',
                    dataIndex:'maintainanceno',
                    sortable: true
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.MaintenanceAmount"), // 'Maintenance Amount',
                    dataIndex:'maintainanceamt'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.LeaseSalesContractID"), // 'Lease/ Sales Contract ID',
                    dataIndex:'contractno'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.AccountName"), // 'Customer Name',
                    dataIndex:'accountname'
                },{
                    dataIndex:'caseid',
                    hidden:true
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Memo"), // 'Memo',
                    dataIndex:'memo',
                    renderer: function(value) {
                        value = value.replace(/\'/g, "&#39;");
                        value = value.replace(/\"/g, "&#34");
                        return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
                    },
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Status"), // 'Status',
                    dataIndex:'status'
                    /*renderer:function(val){
                        if(val=="1"){
                            return "Open";
                        } else if(val=="0") {
                            return "Closed";
                        }
                    }*/
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Remark"), // 'Remark',
                    dataIndex:'remark'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractActivityPanel.RecommendedforSales"), // 'Recommended for Sales',
                    dataIndex:'recommendedForSales',
                    renderer:function(val){
                        if(val=="true"){
                            return "Yes";
                        } else if(val=="false") {
                            return "No";
                        }
                    }
                }
            ]
        });
        


//================================================================= For Defining All Field Sets ===================================================================
        
        
        this.fieldSet1=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractActivityPanel.ContractActivitiesDetails"), // 'Contract Activities Details',
            items:[
                this.activityContractDetailsGrid
            ]
        });
        
        this.fieldSet2=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractActivityPanel.ContractMaintenanceDetails"), // 'Contract Maintenance Details',
            items:[
                this.maintainanceFormGrid
            ]
        });
        

//================================================================ For Adding All Field Set in View ================================================================
       
        this.add({
            layout:"table",
            layoutConfig: {
                columns: 1
            },
            autoWidth:true,
            autoScroll:true,
            bodyStyle:'overflow-y: scroll',
            items:[
                {
                    colspan: 1,
                    width:1200,
                    height:280,
                    border:false,
                    items:this.fieldSet1,
                    bodyStyle:"margin-left:20px;margin-top:30px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:280,
                    border:false,
                    items:this.fieldSet2,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                }]
        });
    }
});