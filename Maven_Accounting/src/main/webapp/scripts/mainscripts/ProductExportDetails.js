Wtf.ProductExportDetails = function(config) {
    Wtf.apply(this, config);
    Wtf.ProductExportDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ProductExportDetails, Wtf.Panel, {
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
            {name: 'type'},
            {name: 'requestTime'},
            {name: 'status'}
        ]);
        
        this.dataStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.columnRec),
            url: "ACCCombineReports/getProductExportDetails.do"
        });
        
        this.columnCm = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer({
        	width:20
        }),
        {
            header: WtfGlobal.getLocaleText("acc.importLog.fileName"),  //"File Name",
            sortable:true,
            width:200,
            dataIndex: "filename"
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
                if(v == 2){
                    return "Completed";
                }else{
                    return "Pending";
                }
            }
        },{
            header:WtfGlobal.getLocaleText("acc.ExportDetails.File"),
//            sortable:true,
//            width:20,
            dataIndex:"imported",
            align: "Left",
            renderer : function(val, m, rec){
                if(rec.data.status == 2){
                    return "<div class=\"pwnd downloadIcon original\" wtf:qtip=\"Download File\" style=\"height:16px;\">&nbsp;</div>";
                }else{
                    return "In Process...";
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
            tbar : [WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",WtfGlobal.getLocaleText("acc.common.to"),this.endDate,"-",this.fetchButton],
            items : this.grid
        });
        
        this.initialLoad();
        this.add(this.wrapperBody);
        
        Wtf.ProductExportDetails.superclass.onRender.call(this, config);
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".original")){
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            if(rec.status == 2){
                Wtf.get('downloadframe').dom.src = 'ImportRecords/downloadExportedFileData.do?storagename='+rec.filename+'&filename='+rec.filename+'&type='+rec.type;
            }
        }
    },
    
    initialLoad: function(){
        this.dataStore.baseParams = {
            startdate: WtfGlobal.convertToGenericStartDate(this.getDates(true)),
            enddate: WtfGlobal.convertToGenericEndDate(this.getDates(false))
        }
        this.dataStore.load({params : {
            start: 0,
            limit: this.pag.pageSize
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