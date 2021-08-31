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
Wtf.customReport=function(config){
    this.autoScroll=true;
    this.border=false;
    this.width='99%';
    this.bodyStyle = 'background:white;';
    Wtf.apply(this,config);
    Wtf.form.Field.prototype.msgTarget = "under";
    var defConf = {
        xtype:'radio',
        labelSeparator:'',
        ctCls: 'reportfieldContainer',
        labelStyle: 'font-size:11px; text-align:right;'
    };
    this.attachheight=130;
    this.hfheight=150;
    this.subtitle = new Wtf.Panel({
        bodyStyle : 'margin-bottom:3px;padding-left:105px;',
        border:false,
        html:"<a id = 'subtitlelink"+this.id+"'class='attachmentlink' href=\"#\" onclick=\"Addsubtitle(\'"+this.id+"\')\">"+WtfGlobal.getLocaleText("acc.pdf.5")+"</a>"
    });
    this.count=1;
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
                    maxLength:40,
                    validator:WtfGlobal.validateHTField,
                    maxLengthText:WtfGlobal.getLocaleText("acc.field.Youcannotentermorethan40characters"),
                    emptyText:WtfGlobal.getLocaleText("acc.pdf.22")
                }),
                this.reporttitle = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.pdf.4"),  //'Report Title',
                    maxLength:40,
                    validator:WtfGlobal.validateHTField,
                    maxLengthText:WtfGlobal.getLocaleText("acc.field.Youcannotentermorethan40characters"),
                    emptyText:WtfGlobal.getLocaleText("acc.pdf.4")  //'Insert Title'
                })]
            },this.subtitle
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

    this.fpager= new Wtf.form.Checkbox({
                    name:'pager',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.7"),  //'Paging',
                    labelSeparator:'',
                    listeners:{check:this.checkfPager, scope:this}
    });
    this.hpager= new Wtf.form.Checkbox({
                    name:'pager',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.7"),  //'Paging',
                    labelSeparator:'',
                    listeners:{check:this.checkhPager, scope:this}
    });
    this.hdater= new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.6"),  //'Date',
                    labelSeparator:'',
                    listeners:{check:this.checkhDater, scope:this}
    });
    this.fdater=new Wtf.form.Checkbox({
                    name:'dater',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.6"),  //'Date',
                    labelSeparator:'',
                    listeners:{check:this.checkfDater, scope:this}
    });
    this.dateRange=new Wtf.form.Checkbox({
                    name:'includedaterange',
                    boxLabel:WtfGlobal.getLocaleText("acc.pdf.25"),  //'Include Date Range',
                    labelSeparator:''
    });


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
                bodyStyle : 'margin-top:15%;',
                items:[this.hdater,this.dateRange]
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
                            checked:true
                        }),
                        this.pnoborder = new Wtf.form.Radio({
                            name:'pborder',
                            inputValue :'false',
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
                            checked:true
                    }),
                        this.dnoborder = new Wtf.form.Radio({
                            name:'dborder',
                            inputValue :'false',
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
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.13")  //'Potrait'
                    }),
                        this.landscape = new Wtf.form.Radio({
                        name:'pview',
                        id:'pageviewtrue'+this.id,
                        inputValue :'true',
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.14"),  //'Landscape',
                        checked:true
                    })]
                },{
                    xtype:'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pdf.15"),  //'Company Logo',
                    cls: "customFieldSet",
                    defaults : defConf,
                    autoHeight : true,
                    items:[
                        this.hidelogo = new Wtf.form.Radio({
                        name:'complogo',
                        inputValue :'false',
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.16"),  //'Hide Logo',
                        checked:true
                    }),
                        this.showlogo = new Wtf.form.Radio({
                        name:'complogo',
                        id:'companylogo'+this.id,
                        inputValue :'true',
                        boxLabel:WtfGlobal.getLocaleText("acc.pdf.17")  //'show Logo'
                    })]
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
                            maxLength:40,
                            validator:WtfGlobal.validateHTField,
                            maxLengthText:WtfGlobal.getLocaleText("acc.field.Youcannotentermorethan40characters"),
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
        },
        {
            border: false,
            html: '<center><hr style = "width:95%;"></center>'
        },
        {
            xtype:'button',
            text:'<b>'+WtfGlobal.getLocaleText("acc.pdf.23")+'<b>',
            cls:'exportpdfbut',
            scope:this,
            handler:function(){
                if (this.customForm.getForm().isValid())
                 Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.nee.19"),function(btn,text){
                            if(btn =='yes') {
                                if(this.editTemplateConfig==undefined) {
                                    this.saveTemplate();
                                } else {
                                    this.overwriteTemplate();
                                    this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                                }
                            } else {
                                this.exportPdf();
                                this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
                            }
                        },this);
                else
                Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nee.20"));
            }
        }]
    });

    if(this.editTemplateConfig!=undefined)
            this.editForm(this.editTemplateConfig);
    Wtf.customReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.customReport,Wtf.Panel,{
    onRender: function(conf){
        Wtf.customReport.superclass.onRender.call(this, conf);
        if(this.reportType==2)
            Wtf.getCmp(this.id + 'adjustColWidth').hide();
        this.add(this.customForm);
    },
    editForm: function(configstr) {
        var config = eval('('+configstr +')');
        this.headernote.setValue(config['headNote']);
        this.reporttitle.setValue(config['title']);
        this.footernote.setValue(config['footNote']);
        this.fpager.setValue(config['footPager']);
        this.hpager.setValue(config['headPager']);
        this.hdater.setValue(config['headDate']);
        this.fdater.setValue(config['footDate']);
        this.dateRange.setValue(config['dateRange']);
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
    },
    removesubtitle:function(){
        this.attachheight -=25;
        this.hfieldset.setHeight(this.attachheight);
        if(this.count>5)
            document.getElementById('subtitlelink'+this.id).style.display='block';
        this.count--;
        if(this.count==1)
            document.getElementById('subtitlelink'+this.id).innerHTML = WtfGlobal.getLocaleText("acc.pdf.5");
        this.doLayout();
    },
    Addsubtitle:function(){
        var textfield = new Wtf.form.TextField({
            labelSeparator:'',
            emptyText:WtfGlobal.getLocaleText("acc.pdf.5"), //'Add Subtitle',
            maxLength:40,
            name: 'subtitle'+(this.count++)
        });
        this.attachheight = this.attachheight+25;
        var pid = 'subtitle'+this.count+this.id;
        this.hfieldset.insert(this.count,new Wtf.Panel({
            id : pid,
            cls:'subtitleAddRemove',
            border: false,
            html:'<a href=\"#\" class ="attachmentlink" style ="margin-left:5px" onclick=\"removesubtitle(\''+pid+'\',\''+this.id+'\')\">'+WtfGlobal.getLocaleText("acc.field.Remove")+'</a>',
            items:textfield
            })
        );
        this.hfieldset.setHeight(this.attachheight);
        document.getElementById('subtitlelink'+this.id).innerHTML = WtfGlobal.getLocaleText("acc.field.Addanothersubtitle");
        if(this.count>5)
            document.getElementById('subtitlelink'+this.id).style.display='none';

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
            fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridName"),   //'Name',
            id:'repTemplateName',
            validator: WtfGlobal.validateUserName,
            allowBlank: false,
            width:255
        });
        var descField = new Wtf.form.TextArea({
            id:'repDescField',
            height: 187,
            hideLabel:true,
            cls:'descArea',
            fieldClass : 'descLabel',
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
                        if(!nameField.isValid()) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.CouldnotsavetemplatePleasetryagain")],1);
                            return;
                        }
                        this.saveReportTemplate(Template,nameField,descField);
                        this.exportPdf();
                        this.ownerCt.ownerCt.ownerCt.remove(this.ownerCt.ownerCt);
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
    overwriteTemplate: function() {
        Wtf.Ajax.requestEx({
//            url: Wtf.req.base + 'template.jsp',
            url : "ExportPDF/editReportTemplate.do",
            params: {
                action: 3,
                data: this.generateData(),
                userid:loginid,
                edit:this.overwriteflag
            },
            method:'POST'
        },
        this,
        function(res) {
            if(res.success) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.nee.templateUpdatedSuccessfully")],0);
                this.exportPdf();
            }
        },
        function() {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.CouldnotsavetemplatePleasetryagain")],1);
        });
    },
    saveReportTemplate:function(win, nameField, descField){
        var tname = WtfGlobal.HTMLStripper(nameField.getValue());
        var description = WtfGlobal.HTMLStripper(descField.getValue());
        if(tname == null && tname == "") {
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.ThereporttemplatehasnotbeensavedPleasecheckentriesandtryagain"));
            return;
        }
        Wtf.Ajax.requestEx({
//            url: Wtf.req.base + 'template.jsp',
            url : "ExportPDF/saveReportTemplate.do",
            params: {
                action: 0,
                name: tname,
                data: this.generateData(),
                desc: description,
                userid:loginid
            },
            method:'POST'
        },
        this,
        function(res,req) {
            if(res.success){ 
                if(res.duplicate){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.tmp.templateNameAlreadyExists")],2);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.nee.22")],0);
                    }
            }
        },
        function() {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.CouldnotcreatetemplatePleasetryagain")],1);
        });
        win.close();
    },
    exportPdf:function() {
        var data=this.generateData();
        var url ="../../export.jsp?"+this.mode+"&config="+data+"&filename="+this.filename+"&filetype="+ this.type+"&stdate="+this.stdate+"&enddate="+this.enddate+"&accountid="+this.accountid
                             +"&get="+this.get+"&gridconfig="+encodeURIComponent(this.gridconfig)+"";
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
         var dateRange=this.dateRange.getValue();
         var footNote=WtfGlobal.ScriptStripper(WtfGlobal.HTMLStripper(Wtf.getCmp('footernote'+this.id).getValue()));

         var pb=Wtf.getCmp('pbordertrue'+this.id). getGroupValue();
         var gb=(Wtf.getCmp('gridbordertrue'+this.id). getGroupValue());
         var pv=(Wtf.getCmp('pageviewtrue'+this.id). getGroupValue());
         var cl=(Wtf.getCmp('companylogo'+this.id). getGroupValue());
         var tColor = this.tcc.substring(1);
         var bColor = this.bcc.substring(1);
         var data = '{"landscape":"'+pv+'","pageBorder":"'+pb+'","gridBorder":"'+gb+'","title":"'+title +'","subtitles":"'+subtitles +'","headNote":"'+headNote+'","showLogo":"'+cl +'","headDate":"'+headDate+'","footDate":"'+footDate+'","footPager":"'+footPager+'","headPager":"'+headPager+'","dateRange":"'+dateRange+'","footNote":"'+footNote+'","textColor":"'+tColor+'","bgColor":"'+bColor+'"}';
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
