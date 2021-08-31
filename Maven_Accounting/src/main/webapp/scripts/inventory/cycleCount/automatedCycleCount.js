
function cycleAutomatedReport(){
    var main = Wtf.getCmp("as");
    var newTab = Wtf.getCmp("cycleCntAutomatedRpt")

    if(newTab == null)
    {
        newTab = new Wtf.automatedCycleCount({
            id:"cycleCntAutomatedRpt",
            title:"Cycle Count Status Report",
            layout:"fit",
            closable:true
        });
        main.add(newTab);
    }
    main.setActiveTab(newTab);
    main.doLayout();
}


Wtf.automatedCycleCount = function(config){
    Wtf.automatedCycleCount.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.automatedCycleCount, Wtf.Panel, {
    onRender: function(config) {
        Wtf.automatedCycleCount.superclass.onRender.call(this, config);
        this.maxval=0;
        this.cycleDate = new Wtf.ExDateFieldQtip({
            emptyText:'date...',
            readOnly:true,
            width : 200,
            name : 'frmdate',
            minValue: Wtf.archivalDate,
            value:new Date(),
            format: companyDateFormat//'Y-m-d'
        });

        this.search = new Wtf.Button({
            anchor : '90%',
            text: 'Search',
            iconCls : 'pwnd editicon',
            scope:this,
            handler:function(){
                this.loadGrid();
            }
        });

        this.checkStatus = new Wtf.Button({
            anchor : '90%',
            text: 'Check status',
            iconCls : 'pwnd editicon',
            scope:this,
            disabled: true,
            handler:function(){
               
                var userVal=this.totalcount.getValue();
                for(var i=0; i<this.ds.getCount(); i++){
                    var rec=this.ds.getAt(i);
                    if(userVal == rec.data.TotalCount){
                        rec.set('status',"D");
                    }else if(userVal > rec.data.TotalCount){
                        rec.set('status',"P");
                    }else{
                        rec.set('status',"E");
                    }
                }
                this.cm.setColumnHeader(4,("Total Count (Max Value:  "+userVal+"  )"));
            }
        });

        this.totalcount = new Wtf.form.NumberField({
            disabled: true
        });

        this.extraitemStore=new Wtf.data.SimpleStore({
            fields:['extraitemid','extraitemname'],
            data:[['','Consider'],['0','Do not consider']]

        });
        this.extraitemCombo = new Wtf.form.ComboBox({
            fieldLabel : 'extraitem*',
            hiddenName : 'extraitem',
            store : this.extraitemStore,
            typeAhead:true,
            forceSelection:true,
            displayField:'extraitemname',
            valueField:'extraitemid',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText:'Select ...',
            allowBlank:false

        });
        this.extraitemCombo.setValue('');

        this.quantityStore=new Wtf.data.SimpleStore({
            fields:['quantityid','quantityname'],
            data:[['0','non zero'],['','All']]

        });
        this.quantityCombo = new Wtf.form.ComboBox({
            fieldLabel : 'Quantity*',
            hiddenName : 'quantity',
            store : this.quantityStore,
            typeAhead:true,
            forceSelection:true,
            displayField:'quantityname',
            valueField:'quantityid',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText:'Select actual quantity...',
            allowBlank:false

        });
        this.quantityCombo.setValue('');

        this.ds = new Wtf.data.GroupingStore({
            url: 'jspfiles/inventory/reportHandler.jsp',
            baseParams: {
                flag: 20
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count',
                maxval: 'maxval'
            },
            ['StoreCode', 'AnalysisCode', 'StoreDescription', 'TotalCount','status'])
        });
        this.ds.on("load", function(ds, rec, o) {
            
            this.totalcount.enable();
            this.checkStatus.enable();
            this.maxval = this.ds.reader.jsonData.maxval;
            this.totalcount.setValue(this.maxval);

            this.cm.setColumnHeader(4,("Total Count (Max Value:  "+this.maxval+"  )"));
        },
        this);

        

        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer()
            ,{
                header: "Store Code",
                dataIndex: "StoreCode"
            }
            ,{
                header: "Store Analysis Code",
                dataIndex: "AnalysisCode"
            },{
                header: "Store Description",
                dataIndex: "StoreDescription"
            },{
                header: "Total Count",
                align:"right",
                dataIndex: "TotalCount"
            },{
                header:"Status",
                dataIndex:"status",
                renderer:function (val){
                    if(val == "D"){
                        return "<label style='color:green'>Done</label>";
                    } else if(val=="P"){
                        return "<label style='color:red'>Pending</label>";
                    }else{
                        return "<label style='color:blue'>Extra</label>";
                    }
                }
            }
            ]);
        this.cm.defaultSortable = true;

        var tbarArray = [];
        tbarArray.push("Business Date: ", this.cycleDate,"-","Quantity:",this.quantityCombo,"-","Extra Items:",this.extraitemCombo,"-",this.search,"->","Max Count:",this.totalcount,this.checkStatus);

        this.summary = new Wtf.grid.GroupSummary({});
        this.grid = new Wtf.grid.GridPanel({
            cm: this.cm,
            store: this.ds,
            loadMask: true,
            tbar: tbarArray,
            viewConfig: {
                forceFit: true
            }
        });
        this.add(this.grid);
        this.loadGrid();
    },

    loadGrid: function() {
        Wtf.Ajax.timeout = 600000;
        this.ds.load({
            params:{
                countdate: this.cycleDate.getValue().format('Y-m-d'),
                quantity: this.quantityCombo.getValue(),
                extraitem: this.extraitemCombo.getValue()
            }
        });
    }
});

Wtf.monthlyCycleCountCsv = function(config) {
    Wtf.apply(this, config);

    var archivalDate=Date.parseDate(Wtf.archivalDate,"Y-m-d").add(Date.MONTH, 1);

    var monthCal = new Wtf.MonthField({
        fieldLabel:'Month*',
        width : 200,
        minValue: archivalDate.format('F Y'),
        value: getSysMonth(),
        format: 'F Y'
    });
    var storeCmbRecordExport = new Wtf.data.Record.create([{
        name: 'id'
    },{
        name: 'abbrev'
    },{
        name: 'description'
    }]);

    var storeCmbStoreExport = new Wtf.data.Store({
        url:  'jspfiles/inventory/pettyCash.jsp',
        baseParams:{
            flag:7
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        }, storeCmbRecordExport),
        sortInfo: {
            field: 'description',
            direction: 'ASC'
        }
    });

    var storeCmbExport = new Wtf.form.ComboBox({
        fieldLabel : 'Store*',
        store : storeCmbStoreExport,
        typeAhead:true,
        forceSelection:true,
        displayField:'description',
        valueField:'id',
        mode: 'local',
        width : 200,
        triggerAction: 'all',
        emptyText:'Select store...',
        allowBlank:false
    });

    storeCmbStoreExport.on("load", function(ds, rec, o) {
        if(rec.length > 0) {
            storeCmbStoreExport.insert(0, new storeCmbRecordExport({
                id: "",
                abbrev: "",
                description: "All"
            }));
            storeCmbExport.setValue("", true);

        } else {
            storeCmbExport.setValue(rec[0].data.id, true);
        }
    }, this);
    storeCmbStoreExport.load();

    Wtf.monthlyCycleCountCsv.superclass.constructor.call(this, {
        title: "Export GL of month",
        modal: true,
        bodyStyle : 'font-size:10px;',
        minWidth:75,
        width : 400,
        height: 250,
        resizable :false,
        buttonAlign : 'right',
        layout : 'border',
        items: [{
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml("GL export of Month End", "Select a month name to export.") //Change image
        },{
            region : 'center',
            border : false,
            bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
            layout : 'fit',
            items : [
            this.cycleCountGlExport = new Wtf.form.FormPanel({
                border : false,
                bodyStyle : 'background:transparent;',
                items : [monthCal, storeCmbExport]
            })
            ]
        }],
        buttons: [{
            text: 'Submit',
            scope: this,
            handler: function() {
                if(!this.cycleCountGlExport.form.isValid()){
                    msgBoxShow(["Info", "The selected month field should be equal to or greater than "+archivalDate.format('F Y')], 1);
                    return;
                }
                Wtf.Ajax.requestEx({
                    url : 'jspfiles/inventory/reportHandler.jsp',
                    params : {
                        flag : 37,
                        storeid : storeCmbExport.getValue(),
                        monthname : monthCal.getValue().format('m Y')
                    },
                    method : 'POST',
                    timeout : 45000
                }, 
                this, 
                function (result, response){
                    var resultObject = Wtf.decode(result);
                    if(resultObject.success){
                        this.close();
                        var url =  "ExportDataServlet.jsp?" +"&mode=71" +
                        "&reportname=" + encodeURI("Monthly Cycle count report " + monthCal.getValue().format('m Y'))+
                        "&exporttype=" + "csv" +
                        "&storeid=" + storeCmbExport.getValue()+
                        "&monthname="+encodeURI(monthCal.getValue().format('m Y'));
                        setDldUrl(url);
                    } else {
                        msgBoxShow(["Error", "No records to export"], 0, 1);
                        return;
                    }
                },
                function (){
                    msgBoxShow(["Error", "Problem occurred while exporting"],0,1);
                });
            }
        },{
            text: 'Cancel',
            scope: this,
            handler : function() {
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.monthlyCycleCountCsv, Wtf.Window, {
    onRender: function(config) {
        Wtf.monthlyCycleCountCsv.superclass.onRender.call(this, config);
    }
});
