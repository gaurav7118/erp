Ext.theme.addManifest({xtype:"widget.menu",folder:"menu",delegate:"."+Ext.baseCSSPrefix+"menu-item-link",filename:"menu-item-active",config:{floating:false,width:200,items:[{text:"&nbsp;",cls:Ext.baseCSSPrefix+"menu-item-active"}]}},{xtype:"widget.button",ui:"default"},{xtype:"widget.button",ui:"default-toolbar"},{xtype:"widget.toolbar",ui:"default"},{xtype:"widget.panel",ui:"default"},{xtype:"widget.header",ui:"default"},{xtype:"widget.window",ui:"default"},{xtype:"widget.tab",ui:"default"},{xtype:"widget.tabbar",ui:"default"},{xtype:"widget.progressbar",ui:"default"},{xtype:"widget.buttongroup",ui:"default"},{xtype:"widget.tooltip",filename:"tip",ui:"default"},{xtype:"widget.tooltip",ui:"form-invalid"},{xtype:"widget.gridcolumn",folder:"grid",filename:"column-header",config:{text:"test",afterRender:function(){var B=this,A=B.el;A.addCls(Ext.baseCSSPrefix+"column-header-align-"+B.align).addClsOnOver(B.overCls);A.setStyle({position:"relative"})}}},{xtype:"widget.gridcolumn",folder:"grid",filename:"column-header-over",config:{text:"test",afterRender:function(){var B=this,A=B.el;A.addCls(Ext.baseCSSPrefix+"column-header-align-"+B.align).addClsOnOver(B.overCls);A.setStyle({position:"relative"});A.addCls(Ext.baseCSSPrefix+"column-header-over")}}},{xtype:"widget.datepicker",folder:"datepicker",filename:"datepicker-header",delegate:"."+Ext.baseCSSPrefix+"datepicker-header"},{xtype:"widget.datepicker",folder:"datepicker",filename:"datepicker-footer",delegate:"."+Ext.baseCSSPrefix+"datepicker-footer"},{xtype:"widget.roweditorbuttons",ui:"default"})