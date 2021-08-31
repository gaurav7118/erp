Ext.define("Ext.dashboard.Panel",{extend:"Ext.panel.Panel",xtype:"dashboard-panel",cls:Ext.baseCSSPrefix+"dashboard-panel",anchor:"100%",layout:"fit",frame:true,closable:true,collapsible:true,animCollapse:true,titleCollapse:true,stateful:true,draggable:{moveOnDrag:false},animateClose:true,loadMask:true,loadMessage:"Loading...",minHeight:90,resizable:true,resizeHandles:"s",doClose:function(){var A=this;if(A.animateClose){if(!A.closing){A.closing=true;A.el.animate({opacity:0,callback:A.finishClose,scope:A})}}else{A.finishClose()}},finishClose:function(){var B=this,A=B.closeAction;B.closing=false;B.fireEvent("close",B);Ext.suspendLayouts();B[A]();Ext.resumeLayouts(true);if(A==="hide"){B.el.setOpacity(1)}},afterRender:function(){this.callParent();if(this.loading){this.onViewBeforeLoad()}},getLoadMask:function(){var C=this,B=C.rendered&&C.loadMask,A;if(B&&!B.isComponent){A={target:C};if(B===true){B=A}else{Ext.apply(A,B)}C.loadMask=B=Ext.ComponentManager.create(A,"loadmask")}return B||null},onAdd:function(A){this.callParent(arguments);A.on({beforeload:"onViewBeforeLoad",load:"onViewLoaded",scope:this})},onViewBeforeLoad:function(){this.loading=true;var A=this.getLoadMask();if(A){A.show()}},onViewLoaded:function(){this.loading=false;var B=this.getLoadMask();if(B){B.hide()}var A=this.items.getAt(0);if(A.getTitle){var C=A.getTitle();if(C){this.setTitle(C)}}},setBox:function(A){this.setSize(A.width,A.height)},getState:function(){var A=this,B=A.callParent()||{};if(!B.collapsed){A.addPropertyToState(B,"height",A.rendered?A.getHeight():A.height||A.minHeight||100)}return B}})