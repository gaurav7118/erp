Wtf.CommonExportDetails = function(config) {
    Wtf.apply(this, config);
    Wtf.CommonExportDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.CommonExportDetails, Wtf.Panel, {
    onRender: function(config){
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            value:WtfGlobal.getDates(true)
        });
        
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            name:'enddate',
            value:WtfGlobal.getDates(false)
        });
        
        this.statusFilter = new Wtf.form.ComboBox({
            fieldLabel: "<span wtf:qtip='" + "Status Filter" + "'>" + "Status Filter" + "</span>",
//            emptyText: "All",
            selectOnFocus: true,
            typeAhead: true,
            triggerAction: 'all',
            mode: 'local',
            value: '0',
            store: new Wtf.data.JsonStore({
                fields: ['id', 'name'],
                data: [
                    {id: "0", name: "All"},
                    {id: "1", name: "Pending"},
                    {id: "2", name: "In Process"},
                    {id: "3", name: "Completed"},
                    {id: "4", name: "Downloaded"},
                    {id: "5", name: "Cancelled"}
                ],
                autoLoad: true
            }),
            forceSelection: true,
            displayField: 'name',
            valueField: 'id',
            listWidth: 150,
            width: 150
        });
            
        
        this.fetchButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //"Fetch",
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope:this,
            iconCls:'accountingbase fetch',
            handler:function(){
                if(this.startDate.getValue()>this.endDate.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
                    return;
                }
                this.initialLoad();
           }
        }),
        
        this.columnRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'filename'},
            {name: 'description'},
            {name: 'period'},
            {name: 'type'},
            {name: 'requestTime'},
            {name: 'status'},
        ]);
        
        this.dataStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.columnRec),
            url: "CommonExportController/getExportLog.do?"
        });
        
        this.columnCm = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer({
        	width:20
        }),
        {
            header: "Report",  //"File Name",
            sortable:true,
            width:200,
            dataIndex: "description",
            renderer: function(value, metadata, arguement){
                if(arguement.data.type == 'pdf' || arguement.data.type =='detailedPDF'){
                    return "<div class=\"pdficon\" wtf:qtip=\"PDF File\" style=\"height:16px;padding-left: 20px;\"/>"+value+"</div>";
                } else if(arguement.data.type == 'xls' || arguement.data.type == 'detailedXls'){
                    return "<div class=\"xlsicon\" wtf:qtip=\"XLS File\" style=\"height:16px;padding-left: 20px;\"/>"+value+"</div>";
                } else if(arguement.data.type == 'json'){
                    return "<div class=\"jsonicon\" wtf:qtip=\"JSON File\" style=\"height:16px;padding-left: 20px;\"/>"+value+"</div>";
                } else if(arguement.data.type == 'csv' || arguement.data.type == 'detailedCSV'){
                    return "<div class=\"csvicon\" wtf:qtip=\"CSV File\" style=\"height:16px;padding-left: 20px;\"/>"+value+"</div>";
                }
                
            }
        },{
            header:"Duration",
            sortable:true,
            align:'center',
            dataIndex:"period"
        },{
            header:WtfGlobal.getLocaleText("acc.ExportDetails.RequestTime"),
            sortable:true,
            align:'center',
            dataIndex:"requestTime"
        },{
            header: WtfGlobal.getLocaleText("acc.GIRO.Status"),
            align:'center',
            dataIndex: "status",
            renderer:function(v){
                if(v == 1){
                    return "<font color='red'>Pending</font>";
                }else if(v == 2){
                    return "<font color='maroon'>In Progress</font>";
                }else if(v == 3){
                    return "<font color='green'>Completed</font>";
                }else if(v == 4){
                    return "<font color='green'>Downloaded</font>";;
                }else if(v == 5){
                    return "<font color='red'>Cancelled</font>";
                }
            }
        },{
            header:"Download",
//            sortable:true,
//            width:20,
            dataIndex:"imported",
            align: "Left",
            renderer : function(val, m, rec){
                if(rec.data.status == 3){
                    return "<div class=\"pwnd downloadIcon original\" wtf:qtip=\"Download File\" style=\"height:16px;\">&nbsp;</div>";
                }else if(rec.data.status == 2){
                    return "File Is Getting Prepared";
                } else if(rec.data.status == 1) {
                    return "File Is In Queue ";
                } else if(rec.data.status == 4){
                    return "File Downloaded";
                } else if(rec.data.status == 5){
                    return "Retry";
                }
                
            }
        }
        ]);
        
        this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
        
        this.grid = new Wtf.grid.GridPanel({
            store: this.dataStore,
            sm:this.sm,
            cm: this.columnCm,
            border : false,
            loadMask : true,
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.ExportDetails.Nofiles")
            }),
            bbar: this.pag=new Wtf.PagingToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.dataStore,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:true//,
            })
        });
        
        this.grid.on('rowclick',this.handleRowClick,this);
        
        this.wrapperBody = new Wtf.Panel({
            border: false,
            layout: "fit",
            tbar : ["Status Filter",this.statusFilter,"-",this.fetchButton],
            items : this.grid
        });
        
        this.initialLoad();
        this.add(this.wrapperBody);
        
        Wtf.CommonExportDetails.superclass.onRender.call(this, config);
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".original")){
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            if(rec.status == 3){
                Wtf.get('downloadframe').dom.src = 'CommonExportController/downloadExportedFileData.do?storagename='+rec.filename+'&filename='+rec.filename+'&type='+rec.type+'&recordId='+rec.id;
            } else if(rec.status == 5){
                Wtf.get('downloadframe').dom.src = 'CommonExportController/downloadExportedFileData.do?storagename='+rec.filename+'&filename='+rec.filename+'&type='+rec.type+'&recordId='+rec.id;
            }
            this.sm.load();
        }
    },
    
    initialLoad: function(){
        this.dataStore.baseParams = {
            startdate: WtfGlobal.convertToGenericStartDate(this.getDates(true)),
            enddate: WtfGlobal.convertToGenericEndDate(this.getDates(false))
        }
        this.dataStore.load({params : {
            start: 0,
            limit: this.pag.pageSize,
            statusFilter : this.statusFilter.getValue()
        }});
    },

    getDefaultDates:function(start){
        var d=Wtf.serverDate;
        if(start){
            d = new Date(d.getFullYear(),d.getMonth(),1);
        }
        return d;
    },

    getDates:function(start){
        var d=Wtf.serverDate;
        if(start){
            d = this.startDate.getValue();
            d = new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0);
        } else {
            d = this.endDate.getValue();
            d = new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59);
        }
        return d;
    }
});