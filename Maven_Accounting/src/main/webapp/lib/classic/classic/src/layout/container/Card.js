Ext.define("Ext.layout.container.Card",{extend:"Ext.layout.container.Fit",alternateClassName:"Ext.layout.CardLayout",alias:"layout.card",type:"card",hideInactive:true,deferredRender:false,getRenderTree:function(){var A=this,B=A.getActiveItem();if(B){if(B.hasListeners.beforeactivate&&B.fireEvent("beforeactivate",B)===false){B=A.activeItem=A.owner.activeItem=null}else{if(B.hasListeners.activate){B.on({boxready:function(){B.fireEvent("activate",B)},single:true})}}if(A.deferredRender){if(B){return A.getItemsRenderTree([B])}}else{return A.callParent(arguments)}}},renderChildren:function(){var A=this,B=A.getActiveItem();if(!A.deferredRender){A.callParent()}else{if(B){A.renderItems([B],A.getRenderTarget())}}},isValidParent:function(C,D,A){var B=C.el?C.el.dom:Ext.getDom(C);return(B&&B.parentNode===(D.dom||D))||false},getActiveItem:function(){var C=this,B=C.activeItem===undefined?(C.owner&&C.owner.activeItem):C.activeItem,A=C.parseActiveItem(B);if(A&&C.owner.items.indexOf(A)!==-1){C.activeItem=A}return A==null?null:(C.activeItem||C.owner.activeItem)},parseActiveItem:function(A){var B;if(A&&A.isComponent){B=A}else{if(typeof A==="number"||A===undefined){B=this.getLayoutItems()[A||0]}else{if(A===null){B=null}else{B=this.owner.getComponent(A)}}}return B},configureItem:function(A){A.setHiddenState(A!==this.getActiveItem());this.callParent(arguments)},onAdd:function(A,B){this.callParent([A,B]);this.setItemHideMode(A)},onRemove:function(A){var B=this;B.callParent([A]);B.resetItemHideMode(A);if(A===B.activeItem){B.activeItem=undefined}},getAnimation:function(B,A){var C=(B||{}).cardSwitchAnimation;if(C===false){return false}return C||A.cardSwitchAnimation},getNext:function(){var C=arguments[0],A=this.getLayoutItems(),B=Ext.Array.indexOf(A,this.activeItem);return A[B+1]||(C?A[0]:false)},next:function(){var B=arguments[0],A=arguments[1];return this.setActiveItem(this.getNext(A),B)},getPrev:function(){var C=arguments[0],A=this.getLayoutItems(),B=Ext.Array.indexOf(A,this.activeItem);return A[B-1]||(C?A[A.length-1]:false)},prev:function(){var B=arguments[0],A=arguments[1];return this.setActiveItem(this.getPrev(A),B)},setActiveItem:function(B){var E=this,A=E.owner,D=E.activeItem,G=A.rendered,C,F;B=E.parseActiveItem(B);C=A.items.indexOf(B);if(C===-1){C=A.items.items.length;Ext.suspendLayouts();B=A.add(B);Ext.resumeLayouts()}if(B&&D!==B){if(B.fireEvent("beforeactivate",B,D)===false){return false}if(D&&D.fireEvent("beforedeactivate",D,B)===false){return false}if(G){Ext.suspendLayouts();if(!B.rendered){E.renderItem(B,E.getRenderTarget(),A.items.length)}if(D){if(E.hideInactive){F=D.el.contains(Ext.Element.getActiveElement());D.hide();if(D.hidden){D.hiddenByLayout=true;D.fireEvent("deactivate",D,B)}else{return false}}}if(B.hidden){B.show()}if(B.hidden){E.activeItem=B=null}else{E.activeItem=B;if(F){if(!B.defaultFocus){B.defaultFocus=":focusable"}B.focus()}}Ext.resumeLayouts(true)}else{E.activeItem=B}B.fireEvent("activate",B,D);return E.activeItem}return false},resetItemHideMode:function(A){A.hideMode=A.originalHideMode;delete A.originalHideMode},setItemHideMode:function(A){A.originalHideMode=A.hideMode;A.hideMode="offsets"}})