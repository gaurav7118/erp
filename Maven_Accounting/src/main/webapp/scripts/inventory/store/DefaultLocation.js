Wtf.DefaultLoationWin = function (config){
    Wtf.apply(this,config);
    Wtf.DefaultLoationWin.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            handler:function (){
                
            },
            scope:this
        },
        {
            text:"Cancel",
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.DefaultLoationWin,Wtf.Window,{
    initComponent:function (){
        Wtf.DefaultLoationWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddLocationFormatForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle = 'Set Default Location';
        var windetail='';
        var image='';
        windetail='Select Location';
        image='images/project.gif';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.locationCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"moduleId",
            fieldLabel : 'Location',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.sequenceMasterStore,
            displayField: 'name',
            valueField:'id',
            msgTarget: 'side',
            emptyText:"Select Location"
        });
        
        this.AddLocationFormatForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            iconCls:'win',
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url:"INVSeq/addSeqFormat.do",
            labelWidth:130,
            items:[
            this.locationCombo,
            
            ]
        });
        
    }
});
