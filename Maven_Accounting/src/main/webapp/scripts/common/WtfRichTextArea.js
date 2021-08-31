/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



//Componet for Rich Text Area Fiel By Extend HTML Editor
//For Global Level
Wtf.form.RichPanel = function(config){
    Wtf.apply(this, config);
    this.createTinyMCE();
    this.createWindow();
    this.readOnly = true;
    Wtf.form.RichPanel.superclass.constructor.call(this,config );
}
Wtf.extend(Wtf.form.RichPanel, Wtf.form.HtmlEditor,{
    enableFormat : false,
    
    enableFontSize : false,
    
    enableColors : false,
    
    enableAlignments : false,
    
    enableLists : false,
    
    enableSourceEdit : false,
    
    enableLinks : false,
    enableFont : false,
    autoHeight:false,
    onRender:function(config){
        Wtf.form.RichPanel.superclass.onRender.call(this, config);
        this.tb.add(
            "->",{
                itemId : "tinyBtn",
                cls : 'x-btn-icon x-edit-'+this.id,
                enableToggle:false,
                scope: this,
                handler:this.showTinyMCE,
                clickEvent:'mousedown',
                tooltip: this.disabled?"":"Edit",
                tabIndex:-1
            });
        if(this.disabled)
            this.tb.disable();   
        this.setHeight(100);
    },
    createTooltip : function(){
        
    },
    initEditor : function(){
        var dbody = this.getEditorBody();
        var ss = this.el.getStyles('font-size', 'font-family', 'background-image', 'background-repeat');
        ss['background-attachment'] = 'fixed'; 
        dbody.bgProperties = 'fixed'; 
        Wtf.DomHelper.applyStyles(dbody, ss);
        this.iframe.onclick = function(){
            return false;
        }
        this.iframe.contentWindow.document.onmousedown = function(){
            return false;
        };
        this.initialized = true;

        this.fireEvent('initialize', this);
        this.pushValue();
    },

    createTinyMCE: function(){
        this.tinyvar = new Wtf.ux.TinyMCE({
            border:true,
            allowBlank:false,
            tinymceSettings: {
                height:305,     //(this.htmlViewSource.getSize().height-110),
                theme: "modern",  
                skin: "lightgray",
                parentid: this.id,
                style:"position: fixed;", // TinyMCE4 update : No such config
                plugins:  "compat3x,print,code,directionality,hr,link,anchor,insertdatetime,visualblocks,visualchars,preview,fullscreen,table,contextmenu,paste,advlist,textcolor,colorpicker",  // Removed in TinyMce4: style,advhr,advimage,advlink,iespell,xhtmlxtras,spellchecker
                external_plugins: {
                    "iespell":"../tinymce/plugins/oldPlugins/iespell/editor_plugin.js"
                },
                menubar: "edit insert view format table tools",                                 
                toolbar1: " undo redo cite | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist | iespell link ",// | image link ",
                toolbar2: "fontselect fontsizeselect | underline | forecolor backcolor | removeformat | charmap | outdent indent | tablecontrols ",//+cbuttons,

                extended_valid_elements: "a[name|href|target|title|onclick|class|url],img[class|src|style|border=0|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],hr[class|width|size|noshade],font[face|size|color|style],span[class|align|style]",
                //	            template_external_list_url: "example_template_list.js",
                    pdw_toggle_on : 0,
                    pdw_toggle_toolbars : "3",
                    entity_encoding:"numeric",
                    browser_spellcheck: true,   // 
                    languages : 'en',
                    forced_root_block : 'p',
                    force_p_newlines : true,
                    remove_linebreaks : false,
                    force_br_newlines : false,
                    paste_retain_style_properties :"all" ,
                    paste_data_images: true,                    
                    paste_as_text:false,                         
                    paste_word_valid_elements:"img",                        
                    image_advtab: true, //(plugin:image)                     
                    image_title: true,  //(plugin:image)                    
                    imagetools_toolbar: 'rotateleft rotateright | flipv fliph | editimage imageoptions',    //(plugin:imagetools)
                    default_link_target: "_blank",  //(plugin:link)         
                    link_assume_external_targets: true, //(plugin:link)      
                    nonbreaking_force_tab: true    //(plugin:nonbreaking)              
                }
            });
    },
    createWindow:function(){
         this.tinyWin = new Wtf.Window({
            height: 450,
            width: 800,
            closeAction:"hide",
            modal:true,
            title:this.fieldLabel,
            items:[this.tinyvar],
            scope:this,
            bbar:
            [{  
                text: 'Save',
                iconCls: 'pwnd save',
                hidden:this.readOnly,
                handler: function(){ 
                    this.doc.firstChild.innerHTML=this.tinyvar.getValue();
                    this.tinyWin.hide();   
                },
                scope:this
            },{
                text: 'Cancel',
                handler: function()
                {
                    this.tinyWin.hide();   
                },
                scope:this
            }]
        });
    },
    showTinyMCE: function(){
        var delayMillis = 1000; 
        var tinyvar = this.tinyvar;
        var val = this.doc.firstChild.innerHTML;
        setTimeout(function() {
            tinyvar.setValue(val);
        }, delayMillis);
        this.tinyWin.show();
    },
 
    reset: function() {
        this.tinyvar.setValue("");
    },
    getValue:function(){
        return this.doc.firstChild.innerHTML;
    },
    setValue:function(val){
        Wtf.form.RichPanel.superclass.setValue.call(this, val);
    }
    
});

Wtf.reg("richtextarea",Wtf.form.RichPanel);



//For line level
Wtf.RichTextArea = function(config) {
    Wtf.apply(this, config);
    this.createTinyMCE();
    this.createWindow();
    this.showTinyMCE();
    this.setValue();
    Wtf.RichTextArea.superclass.constructor.call(this,config );
}

Wtf.extend(Wtf.RichTextArea, Wtf.Window,{
    closeAction:"hide",
    setValue:function(){
        var delayMillis = 1000; 
        var tinyvar = this.tinyvar;
        var val = this.val;

        setTimeout(function() {
            tinyvar.setValue(val);
        }, delayMillis);
    },
    createTinyMCE: function(){
        this.tinyvar = new Wtf.ux.TinyMCE({
            name: 'remark',
            border:true,
            allowBlank:false,
            tinymceSettings: {
                height:310,     //(this.htmlViewSource.getSize().height-110),
                theme: "modern",  
                skin: "lightgray",
                parentid: this.id,
                style:"position: fixed;", // TinyMCE4 update : No such config
                plugins:  "compat3x,print,code,directionality,hr,link,anchor,insertdatetime,visualblocks,visualchars,preview,fullscreen,table,contextmenu,paste,advlist,textcolor,colorpicker",  //  Removed in TinyMce4: style,advhr,advimage,advlink,iespell,xhtmlxtras,spellchecker
                external_plugins: {
                    "iespell":"../tinymce/plugins/oldPlugins/iespell/editor_plugin.js"
                },
                menubar: "edit insert view format table tools",                                 
                toolbar1: " undo redo cite | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist | iespell | link ",
                toolbar2: "fontselect fontsizeselect | underline | forecolor backcolor | removeformat | charmap | outdent indent | tablecontrols ",//+cbuttons,
                        
                extended_valid_elements: "a[name|href|target|title|onclick|class|url],img[class|src|style|border=0|alt|title|hspace|vspace|width|height|align|onmouseover|onmouseout|name],hr[class|width|size|noshade],font[face|size|color|style],span[class|align|style]",
                //	            template_external_list_url: "example_template_list.js",
                pdw_toggle_on : 0,
                pdw_toggle_toolbars : "3",
                entity_encoding:"numeric",
                browser_spellcheck: true,   // 
                languages : 'en',
                forced_root_block : 'p',
                force_p_newlines : true,
                remove_linebreaks : false,
                force_br_newlines : false,
                paste_retain_style_properties :"all" ,
                paste_data_images: true,                    
                paste_as_text:false,                         
                paste_word_valid_elements:"img",                        
                image_advtab: true, //(plugin:image)                     
                image_title: true,  //(plugin:image)                    
                imagetools_toolbar: 'rotateleft rotateright | flipv fliph | editimage imageoptions',    //(plugin:imagetools)
                default_link_target: "_blank",  //(plugin:link)         
                link_assume_external_targets: true, //(plugin:link)      
                nonbreaking_force_tab: true    //(plugin:nonbreaking)              
            }
        });
    },
     createWindow:function(){
         this.win = new Wtf.Window({
            height: 450,
            width: 800,
            closeAction:"hide",
            modal:true,
            title:this.fieldLabel,
            items:[this.tinyvar],
            scope:this,
            bbar:
            [{
                text: 'Save',
                iconCls: 'pwnd save',
                hidden:this.readOnly,
                handler: function(){ 
                    this.rec.set(this.fieldName,this.tinyvar.getValue());
                    this.win.hide();   
                },
                scope:this
            },{
                text: 'Cancel',
                handler: function()
                {
                    this.win.hide();   
                },
                scope:this
            }]
        });
    },
     showTinyMCE: function(){
        this.win.show();
    }
});

