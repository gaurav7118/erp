
function inventoryConfig(){
    var inventoryConfigTab = Wtf.getCmp('inventoryconfig');
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.viewinventryconfigration)) {
    if(inventoryConfigTab == null){
        inventoryConfigTab = new Wtf.InventoryConfigurator({
            title:"Inventory Configuration",
            id:'inventoryconfig',
            layout:"fit",
            closable:true
        });
        Wtf.getCmp("as").add(inventoryConfigTab);
    }
    Wtf.getCmp("as").setActiveTab(inventoryConfigTab);
    Wtf.getCmp("as").doLayout();
    }
    else{
         WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.viewinventryconfigration"));
    }
       
}

Wtf.InventoryConfigurator = function (config){
    Wtf.apply(this,config);
    Wtf.InventoryConfigurator.superclass.constructor.call(this);
}

Wtf.extend(Wtf.InventoryConfigurator,Wtf.Panel,{
    initComponent:function (){
        Wtf.InventoryConfigurator.superclass.initComponent.call(this);
        this.getMasterGrid();
        this.getMasterDataGrid();
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.masterDataGrid,
            
            //this.masterGrid
            ]
        });
              
        
        this.add(this.mainPanel);
    },
    getMasterGrid:function (){
        
                
        var linkData = {
            links:[

            {
                fn:"callfunction('setup')",
                id:"setup",
                text:"<span wtf:qtip=Setup>Setup</span>",
                viewperm:true
            },

            {
                fn:"callfunction('sequence')",
                id:"sequence",
                text:"<span wtf:qtip=Sequence>Sequence</span>",
                viewperm:true
            },
                      
            ]
        };
            
        var tpl = new Wtf.XTemplate(
            '<div class ="dashboardcontent linkspanel" style="float:left;width:100%">',
            '<ul id="accinventoryconfig">',
            '<tpl for="links">',
            '<tpl if="viewperm">',
            '<li id = "{id}">',
            '<a onclick="{fn}" href="#" >{text}</a>',
            '</li>',
            '</tpl>',
            '</tpl>',
            '</ul>',
            '</div>'
            );
           
        this.masterLinks = new Wtf.Panel({
            region:"center",
            Title:'asdf',
            //            id:"paymnetConfigure"+this.helpmodeid,
            bodyStyle:'background:white;',
            layout:'fit',
            //            height:500,
            border: false,
            split: true,
            loadMask:true
        });
        
        this.masterLinks.on('render', function(){
            tpl.overwrite(this.masterLinks.body, linkData);
        }, this);
       
        this.masterGrid = new Wtf.Panel({
            //sm:this.masterSm = new Wtf.grid.RowSelectionModel(),
            region:"west",
            width:200,
            //store:this.masterStore,
            //sortable:true,
            //cm:this.masterColumn,
            items:[this.masterLinks],
            layout:'fit'
        //loadMask:true,
       
        //,
        //            bbar:[
        //                this.masterAdd,
        //                "-",
        //                this.masterEdit
        //            ]
        }); 
        
        
    },
    getMasterDataGrid:function (){
        this.masterDataGrid = new Wtf.TabPanel({
            //sm:this.masterDataSm = new Wtf.grid.RowSelectionModel(),
            //store:this.masterDataStore,
            region:"center",
            id:'masterdatagrid',
            items:[
            new Wtf.SequenceFormatGrid({
                title : "Sequence Management",
                id: "SequenceFormatTabId",
                layout: "fit",
                closable:false
            })
            ,
            new Wtf.Company({
                title : "Inventory Settings",
                id: "inventorysettingsTabId",
                layout: "fit",
                closable:false
            })
            ]
      
        });
        //        
        this.masterDataGrid.on('render',function(){
            openMSSequenceFormatTab();
        },this);
    }
    
})

function callfunction(winid){
   
    switch(winid){
        case "setup":
            openInventorySettingsTab();
            break;
        case "sequence":
            
            openMSSequenceFormatTab();
            
            break;
        
    }
}





