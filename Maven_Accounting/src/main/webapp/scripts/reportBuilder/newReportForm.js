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
Wtf.newCustomReport=function(config){
    this.autoScroll=true;
    this.border=false;
    this.width='99%';
    this.bodyStyle = 'background:white;';
    Wtf.apply(this,config);
    Wtf.form.Field.prototype.msgTarget = "under";
    var defConf = {
        ctCls: 'reportfieldContainer',
        labelStyle: 'font-size:11px; text-align:right;'
    };
    this.attachheight=130;
    this.hfheight=150;
    this.count=1;
    this.letterHead="";
    this.postText="";
    this.preText="";
    this.hfieldset=new Wtf.Panel({
        columnWidth: 0.59,
        border: false,
        height : this.attachheight,
        items:[{
            xtype:'fieldset',
            title: WtfGlobal.getLocaleText("acc.pdf.2"),  //'Header Fields',
            cls: "customFieldSet",
            defaults : defConf,
            autoHeight : true,
            items:[
                this.headernote=new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.pdf.3"),  //'Header Note',
                    labelSeparator:'',
                    maxLength:100,
                    validator:WtfGlobal.validateHTField,
                    maxLengthText:WtfGlobal.getLocaleText("acc.template.headernote.valmsg"),//'You cannot enter more than 100 characters',
                    emptyText:WtfGlobal.getLocaleText("acc.template.insertnote")//'Insert Note'
                }),
                this.reporttitle = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.pdf.4"),  //'Report Title',
                    labelSeparator:'',
                    maxLength:40,
                    validator:WtfGlobal.validateHTField,
                    maxLengthText:WtfGlobal.getLocaleText("acc.template.reporttitle.valmsg"),//'You cannot enter more than 40 characters',
                    emptyText:WtfGlobal.getLocaleText("acc.template.inserttitle")//'Insert Title'
                })]
            }
        ]
    });
    if(this.editTemplateConfig!=undefined) {
        var configure = eval('('+ this.editTemplateConfig +')');
        var bgvalue="#"+configure['bgColor'];
        var tvalue="#"+configure['textColor'];
    } else {
       bgvalue="#FFFFFF";
       tvalue="#000000";
    }
    this.bclrPicker=new Wtf.Panel({
        border:false,
        html:' <div id = "bimg_div'+this.id+'" style="cursor:pointer; height:12px; width:12px; margin:auto; padding:auto; border:thin solid; border-color:'+tvalue
                +'; background-color:'+bgvalue+';" onclick=\"showPaletteBg(\''+this.id+'\')\"></div>'
    });

    this.tclrPicker=new Wtf.Panel({
        border:false,
        html:'<div id = "timg_div'+this.id+'" style="cursor:pointer; height:12px; width:12px; margin:auto; padding:auto; border:thin solid; border-color:'+tvalue
                +'; background-color:'+tvalue+';" onclick=\"showPaletteTxt(\''+this.id+'\')\"></div>'
    });
    this.tcc=tvalue;
    this.bcc=bgvalue;
    var baseCls = 'checkboxtopalign';
    this.fpager= new Wtf.form.Checkbox({
                    name:'pager',
                     boxLabel:WtfGlobal.getLocaleText("acc.pdf.7"),  //'Paging',
                    id:this.id+'footpagercheckbox',
                    labelSeparator:'',
                    cls:baseCls,
                    listeners:{check:this.checkfPager, scope:this}
    });
    this.hpager= new Wtf.form.Checkbox({
                    name:'pager',
                   boxLabel:WtfGlobal.getLocaleText("acc.pdf.7"),  //'Paging',
                    labelSeparator:'',
                    cls:baseCls,
                    listeners:{check:this.checkhPager, scope:this}
    });
    this.hdater= new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.6"),  //'Date',
                    labelSeparator:'',
                    cls:baseCls,
                    listeners:{check:this.checkhDater, scope:this}
    });
    this.fdater=new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.6"),  //'Date',
                    labelSeparator:'',
                    cls:baseCls,
                    listeners:{check:this.checkfDater, scope:this}
    });

    var doctype = this.templatetype == 0 ? "report" : (this.templatetype == 1? "quotation":"");
    var reportFields=[];
    
    for(var reportFieldCnt=0;reportFieldCnt<this.reportFieldConfig.length;reportFieldCnt++){
        var reportConfigObj=this.reportFieldConfig[reportFieldCnt];
        reportFields.push({
                            id:'reportField'+reportConfigObj.keyid,
                            fieldLabel:reportConfigObj.keyname,
                            maxLength:100,
                            xtype:'textfield',
                            labelWidth: 200,
                            allowBlank: false,
                            value:reportConfigObj.width,
                            allowNegative:false,
                            labelSeparator:'',
                            emptyText:WtfGlobal.getLocaleText("acc.field.Enterthewidth")
                        });
    }
    
    this.customForm=new Wtf.FormPanel({
        fileUpload: true,
        autoScroll: true,
        border: false,
        width:'100%',
        frame:false,
        method :'POST',
        scope: this,
        labelWidth: 40,
        items:[{
            border: false,
            html: '<center><div style="padding-top:10px;color:#154288;font-weight:bold"> '+WtfGlobal.getLocaleText("acc.pdf.1")+'</div><hr style = "width:95%;"></center>'
        },{
            layout:'column',
            border: false,
            items:[this.hfieldset,{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:50%;margin-top:15%;',
                items:[this.hdater]
                },{
                columnWidth: 0.19,
                border: false,
                bodyStyle : 'margin-left:15%;margin-top:15%;',
                items:[this.hpager]
            }]
        },{ 
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },{
            layout: 'column',
            border: false,
            items:[{
                columnWidth: 0.49,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.8"),  //'Page Border',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                        this.pborder = new Wtf.form.Radio({
                        id:'pbordertrue'+this.id,
                        name:'pborder',
                        inputValue :'true',
                       boxLabel:WtfGlobal.getLocaleText("acc.pdf.10"),  //'With Border',
                        cls:baseCls,
                        labelSeparator:'',
                        checked:true
                        }),
                    
                        this.pnoborder = new Wtf.form.Radio({
                        name:'pborder',
                        inputValue :'false',
                        labelSeparator:'',
                        cls:baseCls,
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.11")  //'No Border'
                        })
                    ]
                },{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.12"),  //'Data and Grid Border',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                        this.dborder = new Wtf.form.Radio({
                        id:'gridbordertrue'+this.id,
                        name:'dborder',
                        inputValue :'true',
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.10"),  //'With Border',
                        labelSeparator:'',
                        cls:baseCls,
                        checked:true
                    }),
                        this.dnoborder = new Wtf.form.Radio({
                        name:'dborder',
                        inputValue :'false',
                        labelSeparator:'',
                        cls:baseCls,
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.11")  //'No Border'
                    })]
                },
                {
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.18"),  //'Select Background Color',
                    cls: "customFieldSet",
                    id: this.id + 'bcolorPicker',
                    autoHeight : true,
                    items:[this.bclrPicker]
                }]
            },{
                columnWidth: 0.49,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.9"),  //'Page View',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                        this.potrait = new Wtf.form.Radio({
                        name:'pview',
                        inputValue :'false',
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.13"),  //'Potrait'
                        cls:baseCls,
                        labelSeparator:''
                    }),
                        this.landscape = new Wtf.form.Radio({
                        name:'pview',
                        id:'pageviewtrue'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                       boxLabel:WtfGlobal.getLocaleText("acc.pdf.14"),  //'Landscape',
                        cls:baseCls,
                        checked:true
                    })]
                },{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.15"),  //'Company Logo',
                    defaults : 'reportfieldTemplate',
                    cls:"customFieldSet",
                    autoHeight : true,
                    items:[
                        this.hidelogo = new Wtf.form.Radio({
                        name:'complogo',
                        inputValue :'false',
                        id:'hidelogo'+this.id,
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.16"),  //'Hide Logo',
                        cls:baseCls,
                        labelSeparator:'',
                        checked:true
                    }),
                        this.showlogo = new Wtf.form.Radio({
                        name:'complogo',
                        id:'companylogo'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                        cls:baseCls,
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.17"),  //'show Logo'
                        checked: false
                    }),this.showTemplatelogo = new Wtf.form.Radio({
                        name:'complogo',
                        id:'templatelogo'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                        cls:baseCls,
                        boxLabel:WtfGlobal.getLocaleText("acc.field.TemplateHeaderImage"),
                        checked: false
                    }),
                    {xtype:'radio',       
                        name:'complogo',
                        id:'letterhead'+this.id,
                        inputValue :'true',
                        labelSeparator:'',
                        cls:baseCls,
                        boxLabel:this.templatetype==1?WtfGlobal.getLocaleText("acc.template.letterhead"):"",//Letter Head
                        hidden:this.templatetype==1?false:true,
                        checked: false,
                        listeners: {
                        	check: function(cb, checked) {
                        	Wtf.getCmp('addletterhad').setVisible(checked);
                        	}
                        }
                },{
                        xtype:"button",
                        border:false,
                        cls:"letterHeadButton",
                        text:WtfGlobal.getLocaleText("acc.ADDTEXT"),//"Add",
                        id:'addletterhad',
                        hidden:true,
                        scope:this,
                        handler:function(){
                            this.getLetterHeadEditor(this.letterHead);
                        }
                    }]
                },
                {
                    xtype:'fieldset',
                      title: WtfGlobal.getLocaleText("acc.pdf.19"),  //'Select Text Color',
                    cls: "customFieldSet",
                    id:this.id+'tcolorPicker',
                    autoHeight : true,
                    items:[this.tclrPicker]
                }              
                ]
            }]
        },
        {
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },{
            layout:'column',
            border: false,
            items:[{
                columnWidth: 0.59,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.20"),  //'Footer Fields',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                        this.footernote = new Wtf.form.TextField({
                            id:'footernote'+this.id,
                            fieldLabel:WtfGlobal.getLocaleText("acc.pdf.21"),  //'Footer Note',
                            maxLength:100,
                            validator:WtfGlobal.validateHTField,
                            maxLengthText:WtfGlobal.getLocaleText("acc.template.headernote.valmsg"),//'You cannot enter more than 100 characters',
                            labelSeparator:'',
                            emptyText:WtfGlobal.getLocaleText("acc.pdf.22")  //'Insert Note'
                        })]
                }]
            },{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:55%;margin-top:15%;',
                items:[this.fdater]
            },{
                columnWidth: 0.20,
                border: false,
                bodyStyle : 'margin-left:15%;margin-top:15%;',
                items:[this.fpager]
            }]
        },{
            layout:'column',
            border: false,
            hidden:true,
            items:[{
                columnWidth: 0.59,
                border: false,
                items:[{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.reportField"),  //'Footer Fields',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:reportFields
                }]
            }]
        },
        {
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },{
        	xtype:'panel',
        	layout:'table',
        	border:false,
        	width:720,
        	layoutConfig: {
                columns: 10
            },
        	items:[{
        			colspan:8,
        			width:550,
        			border:false,
        			items:[{
        				xtype:'panel',
        				border:false,
        				buttonAlign:'center',
        				buttons:[
        				         {
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.template.viewtemplatelist")+'</b>',//'<b>View Template List<b>',
        				             cls:'exportpdfbut',
        				             scope:this,
        				             handler:function(){
        				                 this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt); //remove current tab [Report Layout Builder]
        				                 if(this.templatetype==1){
                                                             if(this.dashboardCall){
                                                                    this.parentWindow.show();
                                                                    this.templateStore.reload();
                                                             }else {
        				                	 this.tabObj.addInvoiceTemplate(false);
                                                             }
                                                         }else if(mainPanel.activeTab.mainTab != undefined) {
        				                     if(typeof mainPanel.activeTab.mainTab.activeTab.exportfile=="undefined") {
        				                         mainPanel.activeTab.exportfile("pdf"); // maintabs export
        				                     } else {
        				                         mainPanel.activeTab.mainTab.activeTab.exportfile("pdf"); // subtabs export
        				                     }
        				                 } else {
        				                     mainPanel.activeTab.items.items[0].exportfile("pdf"); // for reports export
        				                 }
        				             }
        				         }, 
        				         {
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.pdf.23")+'</b>',//'<b>Export Report<b>',
        				             cls:'exportpdfbut',
        				             hidden:this.editTemplateConfig!=undefined && this.templatetype!=1?false:true,
        				             scope:this,
        				             handler:function(){
        				                 if (this.customForm.getForm().isValid()) {
        				                     this.exportPdf();
        				                     this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
        				                 } else
        				                     WtfComMsgBox(788,0);
        				             }
        				         },
        				         {
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.template.savetemplate")+'</b>',//'<b>Save Template<b>',
        				             cls:'exportpdfbut',
        				             scope:this,
        				             handler:function(){
        				                 if (this.customForm.getForm().isValid())
        				                     if(this.editTemplateConfig==undefined) {
        				                         this.saveTemplate();
        				                     } else {
        				                         this.overwriteTemplate(0);
        				                     }
        				                 else
        				                     WtfComMsgBox(788,0);
        				             }
        				         },
        				         {
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.template.savetemplatenexportreport")+'</b>',//'<b>Save Template and Export Report</b>',
        				             cls:'exportpdfbut',
        				             scope:this,
        				             hidden:this.editTemplateConfig!=undefined && this.templatetype!=1?false:true,
        				             handler:function(){
        				                 if (this.customForm.getForm().isValid())
        				                     if(this.editTemplateConfig==undefined) {
        				                         this.saveTemplate();
        				                     } else {
        				                         this.overwriteTemplate(1);
        				                         this.exportPdf();
        				                         this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
        				                     }
        				                  else
        				                     WtfComMsgBox(788,0);
        				             }
        				         },
        				         {
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.template.pretext")+'</b>',//'<b>Pre Text</b>',
        				             cls:'exportpdfbut',
        				             hidden:this.templatetype==1?false:true,
        				             scope:this,
        				             handler:function(){
        				         	this.getPreTextEditor(this.preText);
        				                 
        				             }
        				         },{
        				             xtype:'button',
        				             text:'<b>'+WtfGlobal.getLocaleText("acc.template.posttext")+'</b>',//'<b>Post Text</b>',
        				             hidden:this.templatetype==1?false:true,
        				             cls:'exportpdfbut',
        				             scope:this,
        				             /*Since there is no need to show this button in other templates except quotation template this id is used in showHelp function for pre text and post text
        				             so if I already opened general template the button is created(it is hidden still exist in DOM) so problem in opening helptext in quotation template
        				             so id is variable*/
        				             id:this.templatetype==1?'pretextposttexthelp':'', 
        				             handler:function(){
        				         		this.getPostTextEditor(this.postText);
        			                 }
        				         }]
        				
        			}]
        		},{
        			colspan:2,
        			border:false,
        			bodyStyle:'align:right',
        			hidden:true,
        			width:50,
        			items:[{
        	        	xtype:'panel',
         	        	border:false,
        	    		html : "<img src='images/help.png' title="+WtfGlobal.getLocaleText("acc.invoice.msg10")+" onclick = 'showHelp(88)' />"
        	    	}]
        		
        	}]
        }]
        
    });   
    if(this.editTemplateConfig!=undefined)
            this.editForm(this.editTemplateConfig,this.letterhead,this.pretext,this.posttext);
    Wtf.newCustomReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.newCustomReport,Wtf.Panel,{ 
    onRender: function(conf){
        Wtf.newCustomReport.superclass.onRender.call(this, conf);
        if(this.reportType==2)
            Wtf.getCmp(this.id + 'adjustColWidth').hide();
        this.add(this.customForm);

        Wtf.getCmp(this.id+'footpagercheckbox').on("render",function(){
            var task = new Wtf.util.DelayedTask(function() {
                this.doLayout();
            }, this);
            task.delay(100);
        },this)
    },
    editForm: function(configstr,letterhade,pretext,posttext) {
        var config = eval('('+configstr +')');
        this.headernote.setValue(config['headNote']);
        this.reporttitle.setValue(config['title']);
        this.footernote.setValue(config['footNote']);
        this.fpager.setValue(config['footPager']);
        this.hpager.setValue(config['headPager']);
        this.hdater.setValue(config['headDate']);
        this.fdater.setValue(config['footDate']);
        if(config['pageBorder']=="true")
            this.pborder.setValue(true);
        else
            this.pnoborder.setValue(true);
        if(config['gridBorder']=="true")
            this.dborder.setValue(true);
        else
            this.dnoborder.setValue(true);
        if(config['landscape']=="true")
            this.landscape.setValue(true);
        else {
            this.landscape.setValue(false);
            this.potrait.setValue(true);
        }
        if(config['showLogo']=="true")
            this.showlogo.setValue(true);
        else
            this.hidelogo.setValue(true);
        if(config['lHead']=="true"){
        	this.hidelogo.setValue(false);
            this.showlogo.setValue(false);
        	Wtf.getCmp('letterhead'+this.id).setValue(true);     
        }
        if (config['showTemplateLogo'] == "true") {
            this.hidelogo.setValue(false);
            this.showlogo.setValue(false);
            Wtf.getCmp('letterhead' + this.id).setValue(false);
            this.showTemplatelogo.setValue(true);
        }
        if(letterhade!=""){
        	this.letterHead=letterhade;
        }
        if(pretext!=""){
        	this.preText=pretext;
        }
        if(posttext!=""){
        	this.postText=posttext;
        }
        
    },
    removesubtitle:function(){
        this.attachheight -=25;
        this.hfieldset.setHeight(this.attachheight);
        if(this.count>5)
            document.getElementById('subtitlelink'+this.id).style.display='block';
        this.count--;
        if(this.count==1)
            document.getElementById('subtitlelink'+this.id).innerHTML = WtfGlobal.getLocaleText(""),//"Add Subtitle";
        this.doLayout();
    },
    checkhDater:function(cbox,checked){
        if(checked)
        this.fdater.reset();
    },
    checkfDater:function(cbox,checked){
        if(checked)
        this.hdater.reset();
    },
    checkhPager:function(cbox,checked){
        if(checked)
        this.fpager.reset();
    },
    checkfPager:function(cbox,checked){
        if(checked)
        this.hpager.reset();
    },
    saveTemplate:function(){
    if(this.editTemplateConfig==undefined) {
        var nameField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridName")+"*",   //'Name',
            id:'repTemplateName',
            validator: WtfGlobal.validateUserName,
            allowBlank: false,
            maxLength:40,
            width:255
        });
        var descField = new Wtf.form.TextArea({
            id:'repDescField',
            height: 187,
            hideLabel:true,
            cls:'descArea',
            fieldClass : 'descLabel',
            maxLength:250,
            width:356
        });
        
        var Template = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.nee.21"),  //'New Report Template',
                width: 390,
                layout: 'border',
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                modal: true,
                height: 330,
                frame: true,
                border:false,
                items:[{
                    region: 'north',
                    height: 45,
                    width: '95%',
                    id:'northRegion',
                    border:false,
                    items:[{
                        layout:'form',
                        border:false,
                        labelWidth:100,
                        frame:true,
                        items:[nameField]
                    }]
                },{
                    region: 'center',
                    width: '95%',
                    height:'100%',
                    id: 'centerRegion',
                    layout:'fit',
                    border:false,
                    items:[{
                        xtype:'fieldset',
                       title:WtfGlobal.getLocaleText("acc.product.description"),   //"Description",
                        cls: 'textAreaDiv',
                        labelWidth:0,
                        frame:false,
                        border:false,
                        items:[descField]
                    }]
                }],
                buttons:[{
                    text:WtfGlobal.getLocaleText("acc.common.saveBtn"),   //'Save',
                    handler: function() {
                        if(!nameField.isValid()) 
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.CouldnotsavetemplatePleasetryagain")],1);
                         else {
                           this.saveReportTemplate(Template,nameField,descField);
                            if(this.templatetype==0) {
                                this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                                if(this.reportFlag != 1) {
                                    (this.selectExport != undefined)?this.tabObj.exportSelected("pdf"):this.tabObj.exportfile("pdf");
                                } else {// for reports export
                                    this.tabObj.exportfile("pdf");
                                }
                            } else {
                                this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                                    if(!this.dashboardCall){
                                        this.tabObj.addInvoiceTemplate(true);
                                    }
                                }
                            }
                    },
                    scope: this
                },{
                     text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),   //'Cancel',
                    handler:function() {
                        Template.close();
                    }
                }]
            });
            Template.show();
        }
    },
    overwriteTemplate: function(type) {
        Wtf.Ajax.requestEx({
               url : "ExportPDF/editReportTemplate.do",
            params: {
                action: 3,
                data: this.generateData(),
                userid:loginid,
                templateCongdata:this.generateReportConfigData(),
                pretext:this.getPreText()!=undefined?this.getPreText():"",
                posttext:this.getPostText()!=undefined?this.getPostText():"",
                letterhead:this.getLetterHead()!=undefined?this.getLetterHead():"",
                edit:this.overwriteflag
            },
            method:'POST'
        },
        this,
        function(res) {
            if(res.success) {
            	if(this.templatetype==0) {
                    this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                    if(this.reportFlag != 1) {
                        (this.selectExport != undefined)?this.tabObj.exportSelected("pdf"):this.tabObj.exportfile("pdf");
                    } else {// for reports export
                        this.tabObj.exportfile("pdf");
                    }
                } else {
                    this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                    if(this.dashboardCall){
                        this.parentWindow.show();
                        this.templateStore.reload();
                    }else {
                        var tempid=res.tempid;
                        this.tabObj.addInvoiceTemplate(true,tempid);
                    }
                }
                if(type==0)
                    ResponseAlert(2);
                else
                    ResponseAlert(250);
            }
        },
        function() {
            WtfComMsgBox(787,1);
        });
    }, 
    saveReportTemplate:function(win, nameField, descField){
        var tname = WtfGlobal.HTMLStripper(nameField.getValue());
        var description = WtfGlobal.HTMLStripper(descField.getValue());
        if(tname == null && tname == "") {
            WtfComMsgBox(789,1);
            return;
        }
        Wtf.Ajax.requestEx({
              url : "ExportPDF/saveReportTemplate.do",
            params: {
                name: tname,
                data: this.generateData(),
                templateCongdata:this.generateReportConfigData(),
                pretext:this.getPreText()!=undefined?this.getPreText():"",
                posttext:this.getPostText()!=undefined?this.getPostText():"",
                letterhead:this.getLetterHead()!=undefined?this.getLetterHead():"",
                desc: description,
                templatetype : this.templatetype,
                userid:loginid
            },
            method:'POST'
        },
        this,
        function(res) {
            if(res.success){
                ResponseAlert(1);
                if(this.dashboardCall){
                    this.parentWindow.show();
                    this.templateStore.reload();
                }else {
                    var tempid=res.tempid;
                    this.tabObj.addInvoiceTemplate(true,tempid);
                }

            }
        },
        function() {
            WtfComMsgBox(791,1);
        });
        win.close();
    },  
    exportPdf:function() {
        var data=this.generateData();
        var url = this.url+"?config="+data+"&reportid="+this.reportid+"&name="+this.name+"&filetype=pdf&gridconfig="
                    +encodeURIComponent(this.gridconfig)+"&mapid="+this.mapid+"&year="+this.year+"&flag="+this.flag;
		if(this.selectExport != undefined) {
            url += "&selectExport="+this.selectExport;
        }
        if(this.comboName != undefined && this.comboValue != undefined) {
            url += "&comboName="+this.comboName+"&filterCombo="+this.comboValue+"&comboDisplayValue="+this.comboDisplayValue;
        }
        if(this.field != undefined) {
            url += "&field="+this.field+"&direction="+this.dir;
        }
		if(this.searchJson=="" && (this.frm=="" || this.to =="")) {
            url += "&isarchive=false&isconverted=0&transfered=0";
        } else if(this.searchJson!="") {
            url += "&searchJson="+encodeURIComponent(this.searchJson)+"&isarchive=false&isconverted=0&transfered=0";
        } else if(this.frm!="" && this.to !="") {
            url += "&frm=" +this.frm+"&to="+this.to+"&cd="+this.cd+"";
        }
        Wtf.get('downloadframe').dom.src = url;
    },   
    generateData:function(){
         var subtitles="";
         var tboxes=this.hfieldset.findByType('textfield');
         var headNote=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[0].getValue()));
         var title=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[1].getValue()));
         var sep="";
         for(i=2; i<tboxes.length; i++){
            subtitles += sep + WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(tboxes[i].getValue()));
            sep="~";
         }
         var headDate=this.hdater.getValue();
         var headPager=this.hpager.getValue();
         var footDate=this.fdater.getValue();
         var footPager=this.fpager.getValue();
         var footNote=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(Wtf.getCmp('footernote'+this.id).getValue()));
         var pb=Wtf.getCmp('pbordertrue'+this.id). getGroupValue();
         var gb=(Wtf.getCmp('gridbordertrue'+this.id). getGroupValue());
         var pv=(Wtf.getCmp('pageviewtrue'+this.id). getGroupValue());
         var cl=(Wtf.getCmp('companylogo'+this.id).getValue());
         var tl=(Wtf.getCmp('templatelogo'+this.id).getValue());
         var lHead=(Wtf.getCmp('letterhead'+this.id).getValue())
         var tColor = this.tcc.substring(1);
         var bColor = this.bcc.substring(1);
         var data = '{"landscape":"'+pv+'","pageBorder":"'+pb+'","gridBorder":"'+gb+'","title":"'+title +'","subtitles":"'+subtitles +'","headNote":"'+headNote+'","showLogo":"'+cl +'","showTemplateLogo":"'+tl +'","headDate":"'+headDate+'","footDate":"'+footDate+'","footPager":"'+footPager+'","headPager":"'+headPager+'","footNote":"'+footNote+'","textColor":"'+tColor+'","bgColor":"'+bColor+'","lHead":"'+lHead+'"}';
         return data;
    },
    generateReportConfigData:function(){
         var data = '[';
         
         for(var reportFieldCnt=0;reportFieldCnt<this.reportFieldConfig.length;reportFieldCnt++){
             var configObj=this.reportFieldConfig[reportFieldCnt];
             data+="{keyid:"+configObj.keyid+", keyname:"+configObj.keyname+", width:"+Wtf.getCmp('reportField'+configObj.keyid).getValue()+"},"             
             
    }
    data=data.substring(0,data.length-1);
    data+="]";
         
         
         
         return data;
    },

    showColorPanelBg: function(obj) {
        var colorPicker = new Wtf.menu.ColorItem({
            id: 'coloritem'
        });

        var contextMenu = new Wtf.menu.Menu({
            id: 'contextMenu',
            items: [ colorPicker ]
        });
        contextMenu.showAt(Wtf.get(this.id + 'bcolorPicker').getXY());
        colorPicker.on('select', function(palette, selColor){
                this.bcc= '#' + selColor;
                Wtf.get("bimg_div"+this.id).dom.style.backgroundColor = this.bcc;
        },this);
    },
    showColorPanelTxt: function(obj) {
        var colorPicker = new Wtf.menu.ColorItem({
            id: 'coloritem'
        });
        var contextMenu = new Wtf.menu.Menu({
            id: 'contextMenu',
            items: [ colorPicker ]
        });
        contextMenu.showAt(Wtf.get(this.id + 'tcolorPicker').getXY());
        colorPicker.on('select', function(palette, selColor){
                this.tcc= '#' + selColor;
                Wtf.get("timg_div"+this.id).dom.style.backgroundColor = this.tcc;
        },this);
    },
    
    getPreTextEditor: function(pretext)
    {
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:pretext
    	});
    	
    	 _tw.on("okClicked", function(obj){
             this.preText = obj.getEditorVal().textVal;
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.preText=this.preText.replace(styleExpression,"");
                 
             
         }, this);
         _tw.show();
         return this.preText;
    },
    getPostTextEditor: function(posttext)
    {
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:posttext
    	});
    	
    	 _tw.on("okClicked", function(obj){
             this.postText = obj.getEditorVal().textVal;
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.postText=this.postText.replace(styleExpression,"");
                 
             
         }, this);
         _tw.show();
        return this.postText;
    },
    getLetterHeadEditor: function(letterhade)
    {
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:letterhade
    	});
    	
    	 _tw.on("okClicked", function(obj){
             this.letterHead = obj.getEditorVal().textVal;
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.letterHead=this.letterHead.replace(styleExpression,"");   
             
         }, this);
         _tw.show();
         return this.letterHead;
    },
    getPreText: function()
    {
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.preText=this.preText.replace(styleExpression,"");
             return this.preText;
    },
    getPostText: function()
    {
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.postText=this.postText.replace(styleExpression,"");
             return this.postText;
    },
    getLetterHead: function()
    {
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.letterHead=this.letterHead.replace(styleExpression,"");
             return this.letterHead;
    }
});

function Addsubtitle(objid){
    Wtf.getCmp(objid).Addsubtitle();
}

function removesubtitle(objid,thisid){
    Wtf.getCmp(objid).ownerCt.remove(Wtf.getCmp(objid),true);
    Wtf.getCmp(thisid).removesubtitle();
}
function showPaletteBg(cid){
        Wtf.getCmp(cid).showColorPanelBg(Wtf.get("bimg_div"+cid));
}
function showPaletteTxt(cid){
        Wtf.getCmp(cid).showColorPanelTxt(Wtf.get("timg_div"+cid));
}


Wtf.EditorWindowQuotation = function(conf) {
    Wtf.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    Wtf.EditorWindowQuotation.superclass.constructor.call(this, {
        width: 820,
        height: 600,
        resizable: false,
        iconCls: "pwnd favwinIcon",
        layout: "fit",
        title: (this.title && this.title != "") ? this.title :WtfGlobal.getLocaleText("acc.template.edityourcontent"), //"Edit Your Content",
        modal: true,
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.OK"),//"OK",
            scope: this,
            handler: this.okClicked
        }, {
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),//"Cancel",
            scope: this,
            handler: this.cancelClicked
        }]
    });
};
Wtf.EditorWindowQuotation = function(conf) {
    Wtf.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    Wtf.EditorWindowQuotation.superclass.constructor.call(this, {
        width: 670,
        height: 400,
        resizable: false,
       iconCls :getButtonIconCls(Wtf.etype.deskera),
        layout: "fit",
        title: (this.title && this.title != "") ? this.title :WtfGlobal.getLocaleText("acc.template.edityourcontent"),// "Edit Your Content",
        modal: true,
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.OK"),//"OK",
            scope: this,
            handler: this.okClicked
        }, {
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),//"Cancel",
            scope: this,
            handler: this.cancelClicked
        }]
    });
};

Wtf.extend(Wtf.EditorWindowQuotation, Wtf.Window, {
    onRender: function(conf) {
        Wtf.EditorWindowQuotation.superclass.onRender.call(this, conf);
        var _iArr = [];
        this.createEditor();
        if(this.headerImage) {
            var _iRec = Wtf.data.Record.create([{
                name: "id"
            },{
                name: "name"
            },{
                name: "url"
            },{
                name: "height"
            }]);
            var _is = new Wtf.data.Store({
                url: Wtf.req.springBase+'emailMarketing/action/getThemeImages.do',
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, _iRec)
            });
            _is.load();
            var _iCM = new Wtf.grid.ColumnModel([{
                header: WtfGlobal.getLocaleText("acc.masterconfig.AddEditWin.AddEditMasterData.nameText"),//"Name",
                dataIndex: "name"
            }]);
            var _iSM = new Wtf.grid.RowSelectionModel({
                singleSelect: true
            });
            _iSM.on("rowSelect", function(obj, ri, rec){
                document.getElementById("email_campaign_header_image").src = rec.data.url;
            }, this);
            this.imageGrid = new Wtf.grid.GridPanel({
                store: _is,
                cm: _iCM,
                height: 200,
                cls: "noborderGrid",
                layout: "fit",
                sm: _iSM,
                border: false,
                viewConfig: {
                    forceFit: true
                }
            });
            _iArr[_iArr.length] = new Wtf.Panel({
                layout: "column",
                region: "north",
                border: false,
                height: 200,
                items: [{
                    border: false,
                    height: 200,
                    columnWidth: 0.2,
                    items: this.imageGrid
                },{
                    columnWidth: 0.79,
                    autoScroll: true,
                    border: false,
                    height: 200,
                    bodyStyle: "text-align: center",
                    html: "<img id='email_campaign_header_image' style='margin-top: 5px' src='' />"
                }]
            });
        }
        
        _iArr[_iArr.length] = new Wtf.Panel({
            layout: "form",
            border:false,
            region: "center",
            items: [this.mce]
         });
        this.add(new Wtf.Panel({
            layout: "border",
            border: false,
            items: _iArr
        }));
    },
    
    
    createEditor: function(){
        this.mce = new Wtf.form.HtmlEditor({
            value: this.val,
            width:650,
            height:330,
            hideLabel:true,
            autoScroll:true,
            plugins: [
	            new Wtf.ux.form.HtmlEditor.insertImage({
	            	 imageStoreURL:'ExportPDF/getEmailTemplateFiles.do?type=img',
		             imageUploadURL:'ExportPDF/saveEmailTemplateFiles.do?type=img'
	            }),
	            new Wtf.ux.form.HtmlEditor.HR({}),
	            new Wtf.ux.form.HtmlEditor.SpecialCharacters({})
            ]
        });
       
       this.mce.on('activate',function(){
           
       },this);
        this.mce.on('render', function () {
            Wtf.EventManager.addListener(this.mce.getEditorBody(), 'paste', function () {
                this.onTextPaste.defer(1, this, [this.mce]);
            }, this);
        }, this);
        this.mce.on('push', function () {
            this.mce.getEditorBody().innerHTML = this.mce.getEditorBody().innerHTML.replace(/(\r\n|\n|\r)/gm,"<br>");
        }, this);

    },
    
    onTextPaste: function (editor) {
        if (editor) {
            editor.getEditorBody().innerHTML = editor.getEditorBody().innerHTML.replace(/(\r\n|\n|\r)/gm,"<br>");
        }
    },
   
    okClicked: function(obj) {
            if(this.fireEvent("okClicked", this))
                this.close();
    },
    cancelClicked: function(obj) {
        this.close();
    },
    getEditorVal: function(){
        var valObj = {};
        valObj["textVal"] = this.mce.getValue();
        if(this.headerImage) {
            valObj["imageRec"] = this.imageGrid.getSelectionModel().getSelected();
        }
        return valObj;
    }
});