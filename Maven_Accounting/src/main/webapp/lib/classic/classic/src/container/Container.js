Ext.define("Ext.container.Container",{extend:"Ext.Component",xtype:"container",alternateClassName:["Ext.Container","Ext.AbstractContainer"],requires:["Ext.util.MixedCollection","Ext.layout.container.Auto","Ext.ZIndexManager","Ext.util.ItemCollection"],mixins:["Ext.mixin.Queryable","Ext.mixin.Container"],renderTpl:"{%this.renderContainer(out,values)%}",autoDestroy:true,defaultType:"panel",detachOnRemove:true,items:undefined,layout:"auto",suspendLayout:false,_applyDefaultsOptions:{defaults:true,strict:false},ariaRole:"presentation",baseCls:Ext.baseCSSPrefix+"container",layoutCounter:0,add:function(){var J=this,G=Ext.Array.slice(arguments),E=(typeof G[0]==="number")?G.shift():-1,C=J.getLayout(),D=false,L,H,B,A,M,K,F,I;if(G.length===1&&Ext.isArray(G[0])){H=G[0];L=true}else{H=G}if(J.rendered){Ext.suspendLayouts()}F=H=J.prepareItems(H,true);A=H.length;if(!L&&A===1){F=H[0]}for(B=0;B<A;B++){M=H[B];if(!M){Ext.raise("Cannot add null item to Container with itemId/id: "+J.getItemId())}if(M.destroyed){Ext.raise("Cannot add destroyed item '"+M.getId()+"' to Container '"+J.getId()+"'")}K=(E<0)?J.items.length:(E+B);I=!!M.instancedCmp;delete M.instancedCmp;if(M.floating){(J.floatingItems||(J.floatingItems=new Ext.util.ItemCollection())).add(M);M.onAdded(J,K,I);delete M.$initParent;if(J.hasListeners.add){J.fireEvent("add",J,M,K)}}else{if((!J.hasListeners.beforeadd||J.fireEvent("beforeadd",J,M,K)!==false)&&J.onBeforeAdd(M)!==false){J.items.insert(K,M);M.onAdded(J,K,I);delete M.$initParent;J.onAdd(M,K);C.onAdd(M,K);D=true;if(J.hasListeners.add){J.fireEvent("add",J,M,K)}}}}if(D){J.updateLayout()}if(J.rendered){Ext.resumeLayouts(true)}return F},onAdded:function(B,C,A){this.callParent([B,C,A]);this.containerOnAdded(B,A)},onRemoved:function(A){this.containerOnRemoved(A);this.callParent(arguments)},afterComponentLayout:function(){var B=this.floatingItems,A,D,C;this.callParent(arguments);if(B){B=B.items;A=B.length;for(D=0;D<A;D++){C=B[D];if(!C.rendered&&C.autoShow){C.show()}}}},afterLayout:function(C){var B=this,A=B.getScrollable();++B.layoutCounter;if(A&&B.layoutCounter>1){A.refresh()}if(B.hasListeners.afterlayout){B.fireEvent("afterlayout",B,C)}},beforeDestroy:function(){var B=this,A=B.items,C=B.floatingItems,D;if(A){while((D=A.first())){B.doRemove(D,true)}}if(C){while((D=C.first())){B.doRemove(D,true)}}Ext.destroy(B.layout);B.callParent()},destroy:function(){var A=this;A.callParent();if(A.items){A.items.destroy()}if(A.floatingItems){A.floatingItems.destroy()}A.refs=A.items=A.floatingItems=A.layout=null},beforeRender:function(){var B=this,A=B.getLayout(),C;B.preventChildDisable=true;B.callParent();B.preventChildDisable=false;if(!A.initialized){A.initLayout()}C=A.targetCls;if(C){B.applyTargetCls(C)}},cascade:function(I,J,A){var H=this,D=H.items?H.items.items:[],E=D.length,C=0,G,F=A?A.concat(H):[H],B=F.length-1;if(I.apply(J||H,F)!==false){for(;C<E;C++){G=D[C];if(G.cascade){G.cascade(I,J,A)}else{F[B]=G;I.apply(J||G,F)}}}return this},contains:function(C,B){var A=false;if(B){this.cascade(function(D){if(D.contains&&D.contains(C)){A=true;return false}});return A}else{return this.items.contains(C)||(this.floatingItems&&this.floatingItems.contains(C))}},disable:function(B,G){var E=this,D=E.disabled,F,A,C;E.callParent([B,G]);if(!G&&!E.preventChildDisable&&!D){F=E.getChildItemsToDisable();A=F.length;for(C=0;C<A;C++){F[C].disable(B,true)}}return E},enable:function(B,G){var E=this,D=E.disabled,F,A,C;E.callParent([B,G]);if(D){F=E.getChildItemsToDisable();A=F.length;for(C=0;C<A;C++){F[C].enable(B,true)}}return E},getChildByElement:function(E,A){var G,C,B=0,D=this.getRefItems(),F=D.length;E=Ext.getDom(E);for(;B<F;B++){G=D[B];C=G.getEl();if(C&&((C.dom===E)||C.contains(E))){return(A&&G.getChildByElement)?G.getChildByElement(E,A):G}}return null},getComponent:function(B){if(Ext.isObject(B)){B=B.getItemId()}var C=this.items.get(B),A=this.floatingItems;if(!C&&A&&typeof B!=="number"){C=A.get(B)}return C},getFocusEl:function(){var A=this.getDefaultFocus();if(A){return A}else{if(this.focusable){return this.getTargetEl()}}return undefined},getLayout:function(){var B=this,A=B.layout;if(!A||!A.isLayout){B.setLayout(A)}return B.layout},getRefItems:function(C){var G=this,D=G.items.items,B=D.length,E=0,F,A=[];for(;E<B;E++){F=D[E];A[A.length]=F;if(C&&F.getRefItems){A.push.apply(A,F.getRefItems(true))}}if(G.floatingItems){D=G.floatingItems.items;B=D.length;for(E=0;E<B;E++){F=D[E];A[A.length]=F;if(C&&F.getRefItems){A.push.apply(A,F.getRefItems(true))}}}return A},getDefaultFocus:function(){var B=this.defaultFocus,A;if(B){A=this.down(B)}return A},initComponent:function(){var A=this;A.callParent();A.getLayout();A.constructing=true;A.initItems();if(A.disabled){A.disabled=false;A.disable(true)}delete A.constructing},initItems:function(){var B=this,A=B.items;if(!A||!A.isMixedCollection){B.items=new Ext.util.ItemCollection();if(A){if(!Ext.isArray(A)){A=[A]}B.add(A)}}},initInheritedState:function(H,C){var G=this,D=G.controller,E=G.layout,F=G.session,I=G.viewModel,B=G.reference,A=G.referenceHolder;G.callParent([H,C]);if(G.collapsed){H.collapsed=true}G.initContainerInheritedState(H,C);if(E&&E.initInheritedState){E.initInheritedState(H,C)}},insert:function(C,B){var A;if(B&&B.isComponent){A=this.items.indexOf(B);if(A!==-1){return this.move(A,C)}}return this.add(C,B)},lookupComponent:function(A){if(!A.isComponent){if(typeof A==="string"){A=Ext.ComponentManager.get(A)}else{A=Ext.ComponentManager.create(A,this.defaultType)}}return A},move:function(B,E){var D=this,A=D.items,C;if(B.isComponent){B=A.indexOf(B)}C=A.getAt(B);if(B!==E){C=A.removeAt(B);if(C===false){return false}E=Math.min(E,A.getCount());A.insert(E,C);D.onMove(C,B,E);if(D.hasListeners.childmove){D.fireEvent("childmove",D,C,B,E)}D.updateLayout()}return C},moveBefore:function(A,B){if(A!==B){A=this.layout.moveItemBefore(A,B)}return A},moveAfter:function(C,D){var B=this.layout,A;if(C!==D){A=D?B.getMoveAfterIndex(D):0;C=B.moveItemBefore(C,this.items.getAt(A))}return C},nextChild:function(H,C){var F=this,D=F.items,G=D.indexOf(H),E=0,B=D.length,A;if(G!==-1){if(C){for(;E<B;E++){A=D.getAt(G+E);if(!A||Ext.ComponentQuery.is(A,C)){break}}}else{A=D.getAt(G+1)}}return A||null},onAdd:Ext.emptyFn,onBeforeAdd:function(B){var A=B.ownerCt;if(A&&A!==this){A.remove(B,false)}},onMove:Ext.emptyFn,onRemove:Ext.emptyFn,onPosition:function(){this.callParent(arguments);this.repositionFloatingItems()},onResize:function(){this.callParent(arguments);this.repositionFloatingItems()},prevChild:function(H,C){var F=this,D=F.items,G=D.indexOf(H),E=0,B=D.length,A;if(G!==-1){if(C){for(;E<B;E++){A=D.getAt(G-E);if(!A||Ext.ComponentQuery.is(A,C)){break}}}else{A=D.getAt(G-1)}}return A||null},remove:function(B,A){var C=this,D;if(C.destroyed||C.destroying){return }D=C.getComponent(B);if(!arguments.length){Ext.log.warn("Ext.container.Container: remove takes an argument of the component to remove. cmp.remove() is incorrect usage.")}if(D&&(!C.hasListeners.beforeremove||C.fireEvent("beforeremove",C,D)!==false)){C.doRemove(D,A);if(C.hasListeners.remove){C.fireEvent("remove",C,D)}if(!C.destroying&&!D.floating){C.updateLayout()}}return D},removeAll:function(D){var H=this,F,B=H.floatingItems,C=[],E=0,A,G;if(B){F=H.items.items.concat(B.items)}else{F=H.items.items.slice()}A=F.length;Ext.suspendLayouts();H.removingAll=true;for(;E<A;E++){G=F[E];H.remove(G,D);if(G.ownerCt!==H){C.push(G)}}H.removingAll=false;Ext.resumeLayouts(!!A);return C},setLayout:function(E){var D=this,B=D.layout,F=B&&B.isLayout,A,C;if(typeof E==="string"){E={type:E}}C=E.type;if(F&&(!C||(C===B.type))){delete E.type;B.setConfig(E)}else{if(F){B.setOwner(null)}A=D.self.prototype.layout;if(typeof A==="string"){E.type=C||A}else{Ext.merge(Ext.merge({},A),E)}E=this.layout=Ext.Factory.layout(E);E.setOwner(this)}if(D.rendered){D.updateLayout()}},setActiveItem:function(A){return this.getLayout().setActiveItem(A)},privates:{applyDefaults:function(A){var B=this,C=B.defaults;if(C){if(Ext.isFunction(C)){C=C.call(B,A)}if(Ext.isString(A)){A=Ext.ComponentManager.get(A)}if(A.isComponent){A.setConfig(C,null,B._applyDefaultsOptions)}else{A=B.getConfigurator().merge(B,Ext.Object.fork(C),A)}}return A},applyReference:function(A){return this.setupReference(A)},applyTargetCls:function(A){this.layoutTargetCls=A},detachComponent:function(A){Ext.getDetachedBody().appendChild(A.getEl())},doRemove:function(C,B){B=B===true||(B!==false&&this.autoDestroy);var F=this,E=F.layout,A=E&&F.rendered,D=C.destroying||B,G=C.floating;if(G){F.floatingItems.remove(C)}else{F.items.remove(C)}if(A&&!G){if(E.running){Ext.Component.cancelLayout(C,D)}E.onRemove(C,D)}C.onRemoved(D);F.onRemove(C,D);if(B){C.destroy()}else{if(A&&!G){E.afterRemove(C)}if(F.detachOnRemove&&C.rendered){F.detachComponent(C)}}},finishRenderChildren:function(){this.callParent();var A=this.getLayout();if(A){A.finishRender()}},getChildItemsToDisable:function(){return this.query("[isFormField],[isFocusableContainer],button")},getContentTarget:function(){return this.getLayout().getContentTarget()},getDefaultContentTarget:function(){return this.el},getScrollerEl:function(){return this.layout.getScrollerEl()||this.callParent()},prepareItems:function(B,D){if(Ext.isArray(B)){B=B.slice()}else{B=[B]}var F=this,C=0,A=B.length,E;for(;C<A;C++){E=B[C];if(E==null){Ext.Array.erase(B,C,1);--C;--A}else{if(D){E=this.applyDefaults(E)}E.$initParent=F;if(E.isComponent){E.instancedCmp=true}B[C]=F.lookupComponent(E);delete E.$initParent}}return B},repositionFloatingItems:function(){var B=this.floatingItems,A,D,C;if(B){B=B.items;A=B.length;for(D=0;D<A;D++){C=B[D];if(C.el&&!C.hidden){C.setPosition(C.x,C.y)}}}},_noMargin:{"margin-top":"","margin-right":"","margin-bottom":"","margin-left":""},resetItemMargins:function(){var A=this.items.items,C=A.length,B=this._noMargin,D;while(C--){D=A[C];D.margin$=null;D.el.setStyle(B)}},setupRenderTpl:function(A){this.callParent(arguments);this.getLayout().setupRenderTpl(A)}}})