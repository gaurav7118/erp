Ext.define("Ext.grid.column.Template",{extend:"Ext.grid.column.Column",alias:["widget.templatecolumn"],requires:["Ext.XTemplate"],alternateClassName:"Ext.grid.TemplateColumn",initComponent:function(){var A=this;A.tpl=(!Ext.isPrimitive(A.tpl)&&A.tpl.compile)?A.tpl:new Ext.XTemplate(A.tpl);A.hasCustomRenderer=true;A.callParent(arguments)},defaultRenderer:function(C,D,A){var B=Ext.apply({},A.data,A.getAssociatedData());return this.tpl.apply(B)},updater:function(A,B){Ext.fly(A).down(this.getView().innerSelector,true).innerHTML=Ext.grid.column.CheckColumn.prototype.defaultRenderer.call(this,B)}})