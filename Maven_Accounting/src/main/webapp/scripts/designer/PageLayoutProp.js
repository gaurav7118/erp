/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.PageLayoutPropWin= function(config){
    var alignstore = Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
            {"val":"L", "name":"Left"},
            {"val":"C", "name":"Center"},
            {"val":"R", "name":"Right"}
        ]
    });
    var pagenoformat = Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
            {"val":"1", "name":"1,2,3, ..."},            
            {"val":"2", "name":"Page X of Y"}
        ]
    });
    var pagenoalignment = Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
//            { "val":"TL", "name":"Top Left"},
//            { "val":"TC", "name":"Top Center"},
//            { "val":"TR", "name":"Top Right"},
            {"val":"BL", "name":"Bottom Left"},
            {"val":"BC", "name":"Bottom Center"},
            {"val":"BR", "name":"Bottom Right"}
        ]
    });
   //for page font
       var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
            {"val":"sans-serif", "name":"Sans-Serif"},
            {"val":"arial", "name":"Arial"},
            {"val":"verdana", "name":"Verdana"},
            {"val":"times new roman", "name":"Times New Roman"},
            {"val":"tahoma", "name":"Tahoma"},
        ]
    });
    var fontsyleflag=false;
    var pagefontstyle,pagefont="";
    var pageFooterConfig = pagelayoutproperty[0].pagefooter;
    var pageNumberConfig = pagelayoutproperty[0].pagenumber;
    if(pagelayoutproperty[0].pagefontstyle){
        pagefontstyle=pagelayoutproperty[0].pagefontstyle;
        pagefont=pagefontstyle.fontstyle?pagefontstyle.fontstyle:"";
    }
    var footerText = pageFooterConfig && pageFooterConfig.text ? pageFooterConfig.text : "";
    var ispageFooter = pageFooterConfig && pageFooterConfig.ispagefooter ? pageFooterConfig.ispagefooter : false;
    var hAlign = pageFooterConfig && pageFooterConfig.halign ? pageFooterConfig.halign : "";
    var footerHeight = pageFooterConfig && pageFooterConfig.footerheight ? pageFooterConfig.footerheight : "0";
    var headerHeight = pageFooterConfig && pageFooterConfig.headerheight ? pageFooterConfig.headerheight : "0";
    var ispageNumber = pageNumberConfig && pageNumberConfig.ispagenumber ? pageNumberConfig.ispagenumber : false;
    var pageNoFormat = pageNumberConfig && pageNumberConfig.pagenumberformat ? pageNumberConfig.pagenumberformat : "";
    var pageNoAlign = pageNumberConfig && pageNumberConfig.pagenumberalign ? pageNumberConfig.pagenumberalign : "";
    if(pagefont){
        fontsyleflag=true;
    }
    Ext.apply(this,{
        constrainHeader :true,
        buttons: [{
            text: 'OK',
            scope: this,
            id:'pagelayoutbutton',
            handler: function() {
                pagelayoutproperty[0]=savePageForm();
                Ext.getCmp('pagelayoutwin').close();
            }
        },{
            text: 'Cancel',
            scope: this,
            handler: function() {
                Ext.getCmp('pagelayoutwin').close();
            }
        }],
        items: [
        this.portrait = {                        
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Portrait',
            autoHeight:true,
            collapsed: true,
            hidden:config.isnewdocumentdesigner,
            id:'portrait',
            name:'portrait',                        
            //                frame: true,
            //                width: 200,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105
            //                    anchor: '45%'
            },                  
            items :[
            {   
                fieldLabel: 'Top Margin',
                name: 'top',
                id:'portraittop',
                dataIndex: 'top',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
                                
            },{
                                
                fieldLabel: 'Right Margin',
                name: 'right',
                dataIndex: 'right',
                xtype: 'numberfield',
                id:'portraitright',
                value: 10,
                minValue: 10,
                maxValue: 100,
                step:1
                                
            },{
                                
                fieldLabel: 'Bottom Margin',
                name: 'bottom',
                id:'portraitbottom',
                dataIndex: 'bottom',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
            },{
                                
                fieldLabel: 'Left Margin',
                name: 'left',
                id:'portraitleft',
                dataIndex: 'left',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
                                
            }
            ],
            listeners: {
                collapse: function(p) {
                    p.cascade(function () {
                        Ext.getCmp('landscape').enable();
                    });
                },
                expand: function(p) {
                    p.cascade(function () {
                        Ext.getCmp('portrait').enable();
                        Ext.getCmp('landscape').disable();
                    });
                }
            }
                        
        },this.landscape = {
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Landscape',
            autoHeight:true,
            id:'landscape', 
            name:'landscape',
            collapsed: true,
            hidden:config.isnewdocumentdesigner,
            //                frame: true,
            //                width: 200,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105
            //                    anchor: '45%'
            },                 
            items :[
            {   
                fieldLabel: 'Top Margin',
                name: 'top',
                id:'landscapetop',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
                                
            },{
                                
                fieldLabel: 'Right Margin',
                name: 'right',
                xtype: 'numberfield',
                id:'landscaperight',
                value: 10,
                minValue: 10,
                maxValue: 100,
                step:1
                                
            },{
                                
                fieldLabel: 'Bottom Margin',
                name: 'bottom',
                id:'landscapebottom',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
            },{
                fieldLabel: 'Left Margin',
                name: 'left',
                id:'landscapeleft',
                xtype: 'numberfield',
                value: 10,
                minValue: 10,
                maxValue: 100
                                
            }
            ],
            listeners: {
                collapse: function(i) {
                    i.cascade(function () {
                        Ext.getCmp('portrait').enable();
                    });
                },
                expand: function(i) {
                    i.cascade(function () {
                        Ext.getCmp('landscape').enable();
                        Ext.getCmp('portrait').disable();                           
                    });
                }
            }
        },this.pagefooter = {
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Page Footer',
            autoHeight:true,
            hidden:config.isnewdocumentdesigner,
            id:'pagefootercheck', 
            name:'pagefootercheck',
            collapsed: !ispageFooter,
            //                frame: true,
            //                width: 200,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105,
                anchor: '45%'
            },  
            items :[
//                {   
//                flex: 1,
//                xtype: 'htmleditor',
//                id: 'pageFooterFormat',
//                style: 'background-color: white;',
//                height:100,
//                layout:'fit',
//                margin: '0 0 10 0',
//                value : footerText,
//                enableLinks : false,
//                enableLists : false,
//                enableSourceEdit : false,
//                enableAlignments : false   
//            },
            {
                xtype: 'combo',
                fieldLabel: 'Horizontal Alignment',
                id:'horizontalalign',
                displayField: 'name',
                valueField: 'val',
                store: alignstore,
                value : hAlign,
                emptytext:'Left',
                labelWidth:150,
                minWidth:300
                
            },{
                xtype: 'combo',
                fieldLabel: 'Vertical Alignment',
                id:'verticalalign',
                displayField: 'name',
                valueField: 'val',
                store: alignstore,
                emptytext:'Left',
                labelWidth:150,
                minWidth:300,
                disabled:true
                
               },{
                xtype: 'numberfield',
                fieldLabel: 'Height of Header',
                id: 'headerheight',
                dataIndex: 'headerheight',
                labelWidth:150,
                minWidth:300,
                value:headerHeight,
                minValue: 30,
                maxValue: 800
            },{
                xtype: 'numberfield',
                fieldLabel: 'Height of Footer',
                id: 'footerheight',
                dataIndex: 'footerheight',
                labelWidth:150,
                minWidth:300,
                value:footerHeight,
                minValue: 30,
                maxValue: 800
            }],
            listeners: {
                collapse: function(i) {
                        
                },
                expand: function(i) {
                        
                }
            }
        //            }]           
        },this.pagenumber ={
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Page Numbering ',
            autoHeight:true,
            id:'pagenumbercheck', 
            name:'pagenumbercheck',
            hidden:config.isnewdocumentdesigner,
            collapsed: !ispageNumber,
            //                frame: true,
            //                width: 200,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105,
                anchor: '45%'
            },  
            items :[{
                xtype: 'combo',
                fieldLabel: 'Page Number Format',
                id:'pagenumberformat',
                displayField: 'name',
                valueField: 'val',
                store: pagenoformat,
                value : pageNoFormat,
                emptytext:'Left',
                labelWidth:150,
                minWidth:300
                
            },{
                xtype: 'combo',
                fieldLabel: 'Page Number Alignment',
                id:'pagenumberalignment',
                displayField: 'name',
                valueField: 'val',
                store: pagenoalignment,
                value : pageNoAlign,                
                emptytext:'Left',
                labelWidth:150,
                minWidth:300                
            }]            
        },this.pagefont={//Applying pagefont to the whole page
            xtype:'fieldset',
            checkboxToggle:true,
            title: 'Page Font Style',
            autoHeight:true,
            id:'pagefontstyleid', 
            name:'pagefont',
            collapsed: !fontsyleflag,
            //                frame: true,
            //                width: 200,
            fieldDefaults: {
                labelAlign: 'left',
                labelWidth: 105,
                anchor: '45%'
            },
            items :[{
                xtype: 'combo',
                fieldLabel: 'Page Font',
                id:'pagefontid',
                displayField: 'name',
                valueField: 'val',
                store: pagefontstore,
                value : pagefont,
                emptytext:'sans-serif',
                labelWidth:150,
                minWidth:300
            }]            
        }]     
    },config);
    Ext.PageLayoutPropWin.superclass.constructor.call(this, config);
}

Ext.extend(Ext.PageLayoutPropWin, Ext.Window, {
    title: 'Page Layout',
    id:'pagelayoutwin',
    bodyStyle: 'background:#f1f1f1;padding:15px',
    resizable: true,
    width: 500,
    height: 400,
    modal: true,
    autoScroll:true,
    layout : 'form',
    scope: this
})
    
