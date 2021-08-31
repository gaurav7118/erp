Ext.define("Ext.util.sizemonitor.Abstract",{mixins:["Ext.mixin.Templatable"],requires:["Ext.TaskQueue"],config:{element:null,callback:Ext.emptyFn,scope:null,args:[]},width:0,height:0,contentWidth:0,contentHeight:0,constructor:function(A){this.refresh=Ext.Function.bind(this.refresh,this);this.info={width:0,height:0,contentWidth:0,contentHeight:0,flag:0};this.initElement();this.initConfig(A);this.bindListeners(true)},bindListeners:Ext.emptyFn,applyElement:function(A){if(A){return Ext.get(A)}},updateElement:function(A){A.append(this.detectorsContainer);A.addCls(Ext.baseCSSPrefix+"size-monitored")},applyArgs:function(A){return A.concat([this.info])},refreshMonitors:Ext.emptyFn,forceRefresh:function(){Ext.TaskQueue.requestRead("refresh",this)},getContentBounds:function(){return this.detectorsContainer.getBoundingClientRect()},getContentWidth:function(){return this.detectorsContainer.offsetWidth},getContentHeight:function(){return this.detectorsContainer.offsetHeight},refreshSize:function(){var D=this.getElement();if(!D||D.destroyed){return false}var B=D.getWidth(),J=D.getHeight(),A=this.getContentWidth(),I=this.getContentHeight(),H=this.contentWidth,F=this.contentHeight,C=this.info,E=false,G;this.width=B;this.height=J;this.contentWidth=A;this.contentHeight=I;G=((H!==A?1:0)+(F!==I?2:0));if(G>0){C.width=B;C.height=J;C.contentWidth=A;C.contentHeight=I;C.flag=G;E=true;this.getCallback().apply(this.getScope(),this.getArgs())}return E},refresh:function(A){if(this.refreshSize()||A){Ext.TaskQueue.requestWrite("refreshMonitors",this)}},destroy:function(){var B=this,A=B.getElement();B.bindListeners(false);if(A&&!A.destroyed){A.removeCls(Ext.baseCSSPrefix+"size-monitored")}delete B._element;B.callParent()}})